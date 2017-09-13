package services

import java.security.MessageDigest
import javax.inject.{Inject, Named, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import daos.{ComicDAO, StripDAO}
import models._
import org.bson.types.ObjectId
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.Environment
import play.api.libs.json.Json
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

class Lurker @Inject() (val system: ActorSystem,
                        @Named("lurker-actor") val lurkerActor: ActorRef)
                       (implicit ec: ExecutionContext) {
  if (ConfigFactory.load().getBoolean("is_lurker")) {
    system.scheduler.schedule(1.minute, 1.hour, lurkerActor, AllRecent)
  } else {
    system.scheduler.schedule(1.minute, 1.hour, lurkerActor, NoOpLurker)
  }
}


@Singleton
class LurkerActor @Inject() (environment: Environment,
                             stripDAO: StripDAO,
                             comicDAO: ComicDAO) extends Actor with ActorLogging {
  val ETL_BATCH_SIZE = ConfigFactory.load().getInt("etl_batch_size")
  val comics: Vector[Comic] = {
    val file = environment.getFile("conf/comics.json")
    val content = Source.fromFile(file).getLines().mkString
    val comics = Json.fromJson[Vector[Comic]](Json.parse(content)).get
    comics.foreach(comic => comicDAO.save(comic))
    comics
  }

  override def preStart(): Unit = {
    log.info("Starting Lurker")
  }

  def receive: PartialFunction[Any, Unit] = {
    case AllFull =>
      log.info("Running Full ETL...")
      etl(comics, isDelta = false)
    case AllRecent =>
      log.info("Running Delta ETL...")
      etl(comics, isDelta = true)
    case m: SelectFull =>
      val selectComics = comics.filter(c => m.comicIds.contains(c.id))
      log.info(s"Running Full ELT on ${selectComics.map(_.title).mkString(", ")}...")
      etl(selectComics, m.isDelta)
    case m: SelectRecent =>
      val selectComics = comics.filter(c => m.comicIds.contains(c.id))
      log.info(s"Running Delta ETL on ${selectComics.map(_.title).mkString(", ")}...")
      etl(comics.filter(c => m.comicIds.contains(c.id)), m.isDelta)
    case NoOpLurker => log.info("No lurking today.")
  }

  private def etl(comics: Vector[Comic], isDelta: Boolean): Unit = {
    for (comic <- comics) yield for {
      (startUrl, count) <- startingPoint(comic, isDelta)
      scraps <- extract(startUrl, count, comic.strategy)
      strips <- transform(comic, scraps)
    } yield load(strips)
  }

  /**
   * starting point determines which page to start scraping with
   * @param comic Comic
   * @param isDelta Boolean
   * @return
   */
  private def startingPoint(comic: Comic, isDelta: Boolean): Future[(String, Int)] = {
    log.info(s"Starting comic: ${comic.title}")
    isDelta match {
      case true => stripDAO.lastStrip(comic.id, comic.firstUrl)
      case false => Future.successful(comic.firstUrl, 1)
    }
  }

  /**
   * extract
   * @param startUrl String beginning url to be scraped.
   * @param startCount Int Associated number of that strip.
   * @param strategy patterns for scraping.
   * @return Future List of Strip objects
   */
  private def extract(startUrl: String, startCount: Int, strategy: Strategy): Future[Vector[Scrap]] = Future {
    val driver = new HtmlUnitDriver()

    /**
     * A crawl closure to recurse on after the client is initialized.
     * @param target String of the url currently targeted.
     * @param count Int counter for associating numbers to the strips.
     * @param scraps Collection of Scrap objects to be returned when the crawl is complete.
     * @return List of Scrap objects.
     */
    @tailrec
    def loop(target: Option[String], count: Int, scraps: Vector[Scrap]): Vector[Scrap] = {
      target match {
        case Some(url) =>
          val (next, scrap) = scrape(driver, url, count, strategy)
          // Batch ETLs
          if ((count - startCount) < ETL_BATCH_SIZE) {
            randomSleep()
            loop(next, count + 1, scraps :+ scrap)
          } else {
            scraps :+ scrap
          }
        case None =>
          log.info("Lurk Accomplished")
          scraps
      }
    }
    loop(Some(startUrl), startCount, Vector.empty[Scrap])
  }

  private def scrape(driver: WebDriver, url: String, count: Int, strategy: Strategy): (Option[String], Scrap) = {
    log.info(s"Scraping url: $url")

    def judge(selectorsO: Option[Vector[String]], htmlectomy: WebElement => Option[String]): Option[String] = {
      selectorsO match {
        case None => None
        case Some(selectors) =>
          selectors.flatMap { (selector: String) =>
            val elements: Vector[WebElement] = driver.findElements(By.xpath(selector)).asScala.toVector
            elements.map { (element: WebElement) =>
              htmlectomy(element)
            }
          }.headOption.flatten
      }
    }

    driver.navigate().to(url)
    val checksum = MessageDigest
      .getInstance("MD5")
      .digest(driver.getPageSource.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString
    val next = judge(Some(strategy.next), webElement => Option(webElement.getAttribute("href"))) match {
      case Some(n) if n.nonEmpty && (n == url || n == url + "#") => None
      case _@n => n
    }
    val (image, isSpecial) = judge(Some(strategy.image), webElement => Option(webElement.getAttribute("src"))) match {
      case None => (Vector(""), true) // Get Screenshot here
      case Some(i) => (Vector(i), false)
    }
    val title = judge(strategy.title, webElement => Option(webElement.getText))
    val bonusImage = judge(strategy.bonusImage, webElement => Option(webElement.getAttribute("src")))
    val imageTitle = judge(strategy.imageTitle, webElement => Option(webElement.getAttribute("title")))
    val imageAlt = judge(strategy.imageAlt, webElement => Option(webElement.getAttribute("alt")))

    val scrap = Scrap(count, checksum, url, image, title, imageTitle, imageAlt, bonusImage, isSpecial)
    log.info(s"Obtained scrap: $scrap")
    (next, scrap)
  }

  private def randomSleep(): Unit = {
    Thread.sleep(Random.nextInt(5) * 1000L)
  }

  /**
   * transform, collect all strip information and do any image processing.
   * @param comic Comic object for strip list.
   * @param scraps Scrap Vector of results.
   * @return
   */
  private def transform(comic: Comic, scraps: Vector[Scrap]): Future[Vector[Strip]] = Future {
    scraps.map { scrap =>
      // TODO: Generate thumbnail and other potential post processing
      val strip = Strip(ObjectId.get, comic.id, "", scrap)
      log.info(s"Successfully transformed strip: $strip")
      strip
    }
  }

  private def load(strips: Vector[Strip]) = {
    strips.foreach { strip =>
      stripDAO.save(strip)
    }
  }
}
