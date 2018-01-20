package com.comicgator.lurker

import java.security.MessageDigest
import java.util.logging.{Level, Logger}

import com.typesafe.scalalogging.LazyLogging
import org.bson.types.ObjectId
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.collection.JavaConverters._

object Mech extends Conf with LazyLogging {
  // Turn off CSS error noise from HtmlUnit
  Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF)

  /*
   * Runs etl process for each comic concurrently.
   * @param comics Vector[Comic] Comics to run etl on.
   * @return
   */
  def etl(comics: Vector[Comic])(
      implicit ec: ExecutionContext): Future[Vector[Boolean]] = {
    Future.sequence {
      for (comic <- comics)
        yield
          for {
            comicInsert <- Repository.insertComic(comic)
            (startUrl, count) <- startingPoint(comicInsert, comic)
            scraps <- extract(startUrl, count, comic.strategy)
            strips <- transform(comic, scraps)
            stripInsert <- load(strips)
          } yield {
            logger.info(s"Inserted ${stripInsert.sum} ${comic.title} strips")
            true
          }
    }
  }

  /*
   * Starting point determines which page to start scraping with.
   * @param comic Comic
   * @return
   */
  private def startingPoint(comicInsert: Int, comic: Comic)(
      implicit ec: ExecutionContext): Future[(String, Int)] = {
    if (comicInsert == 1) logger.debug(s"${comic.id} inserted successfully")
    logger.info(s"Starting comic: ${comic.title}")
    if (IS_DELTA) {
      Repository.lastStrip(comic.id, comic.firstUrl)
    } else {
      Future.successful(comic.firstUrl, 1)
    }
  }

  /*
   * extract
   * @param startUrl String beginning url to be scraped.
   * @param startCount Int Associated number of that strip.
   * @param strategy patterns for scraping.
   * @return Future List of Strip objects
   */
  private def extract(startUrl: String, startCount: Int, strategy: Strategy)(
      implicit ec: ExecutionContext): Future[Vector[Scrap]] = Future {
    val driver: WebDriver = new HtmlUnitDriver()

    /*
     * A crawl closure to recurse on after the client is initialized.
     * @param target String of the url currently targeted.
     * @param count Int counter for associating numbers to the strips.
     * @param scraps Collection of Scrap objects returned from the crawl.
     * @return List of Scrap objects.
     */
    @tailrec
    def loop(target: Option[String],
             count: Int,
             scraps: Vector[Scrap]): Vector[Scrap] = {
      target match {
        case Some(url) =>
          val (next, scrap) = scrape(driver, url, count, strategy)
          // Batch ETLs
          if ((count - startCount) < ETL_BATCH_SIZE) {
            // Be like human sleep sometime between 0 and 5 seconds
            Thread.sleep(Random.nextInt(5) * 1000L)
            loop(next, count + 1, scraps :+ scrap)
          } else {
            scraps :+ scrap
          }
        case None =>
          scraps
      }
    }
    loop(Some(startUrl), startCount, Vector.empty[Scrap])
  }

  /*
   * Scrape function uses webdriver to render pages and extract desired
   * information from the DOM.
   * @param driver WebDriver using Selenium.
   * @param url String location of content.
   * @param count The comic strip number being tracked.
   * @param strategy Strategy object containing xpath selectors.
   * @return (Option[String], Scrap) an optional next url and the resultant
   *         Scrap object.
   */
  private def scrape(driver: WebDriver,
                     url: String,
                     count: Int,
                     strategy: Strategy): (Option[String], Scrap) = {
    logger.info(s"Scraping url: $url")

    /*
     * A closure that that generally apply xPath selectors to a WebElement
     * and extract desired data defined by a partial function.
     * @param selectorsO Optional Vector of xPath selectors
     * @param htmlectomy Partial Function that takes a WebElement and extracts
     *        Optional String information from it.
     * @return
     */
    def judge(selectorsO: Option[Vector[String]],
              htmlectomy: WebElement => Option[String]): Option[String] = {
      selectorsO match {
        case None => None
        case Some(selectors) =>
          selectors
            .flatMap { (selector: String) =>
              val elements: Vector[WebElement] =
                driver.findElements(By.xpath(selector)).asScala.toVector
              elements.map { (element: WebElement) =>
                htmlectomy(element)
              }
            }
            .headOption
            .flatten
      }
    }

    driver.navigate().to(url)
    val checksum = MessageDigest
      .getInstance("MD5")
      .digest(driver.getPageSource.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString
    val next = judge(
      Some(strategy.next),
      webElement => Option(webElement.getAttribute("href"))) match {
      case Some(n) if n.nonEmpty && (n == url || n == url + "#") => None
      case _ @n => n
    }
    val (image, isSpecial) = judge(
      Some(strategy.image),
      webElement => Option(webElement.getAttribute("src"))) match {
      case None => (Vector(""), true) // Get Screenshot here
      case Some(i) => (Vector(i), false)
    }
    val title = judge(strategy.title, webElement => Option(webElement.getText))
    val bonusImage = judge(
      strategy.bonusImage,
      webElement => {
        webElement.getAttribute("src") match {
          case null => None
          case "" => None
          case s: String => Some(s)
        }
      })
    val imageTitle = judge(
      strategy.imageTitle,
      webElement => {
        webElement.getAttribute("title") match {
          case null => None
          case "" => None
          case s: String => Some(s)
        }
      })
    val imageAlt = judge(
      strategy.imageAlt,
      webElement => webElement.getAttribute("alt") match {
        case null => None
        case "" => None
        case s: String => Some(s)
      })

    val scrap = Scrap(count,
                      checksum,
                      url,
                      image,
                      title,
                      imageTitle,
                      imageAlt,
                      bonusImage,
                      isSpecial)
    logger.debug(s"Scraped Scrap: $scrap")
    (next, scrap)
  }

  /*
   * Transform, collect all strip information and do any image processing.
   * @param comic Comic object for strip list.
   * @param scraps Scrap Vector of results.
   * @return
   */
  private def transform(comic: Comic, scraps: Vector[Scrap])(
      implicit ec: ExecutionContext): Future[Vector[Strip]] =
    Future {
      scraps.map { scrap =>
        // TODO: Generate thumbnail and other potential post processing
        val strip = Strip(ObjectId.get, comic.id, "", scrap)
        logger.debug(s"Transformed strip: $strip")
        strip
      }
    }

  /*
   * Calls repo to insert comic strips into database
   * @param strips Vector[Strip]
   * @return Vector of Int, indicating success of insert.
   */
  private def load(strips: Vector[Strip])(
      implicit ec: ExecutionContext): Future[Vector[Int]] =
    Future.sequence(strips.map { strip =>
      logger.debug(s"Inserting strip ${strip.scrap.url}")
      Repository.insertStrip(strip)
    })
}
