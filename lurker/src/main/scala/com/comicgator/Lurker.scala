package com.comicgator

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import org.bson.types.ObjectId
import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import play.api.libs.functional.syntax._

import scala.io.StdIn
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

package object lurker {
  implicit val getJsonResult: GetResult[JsValue] = GetResult(r => Json.parse(r.nextString))
  implicit val getObjectIdResult: GetResult[ObjectId] = GetResult(r => new ObjectId(r.nextString))
  implicit val ObjectIdReader: Reads[ObjectId] = JsPath.read[String].map(new ObjectId(_))
}
//#greeter-companion
//#greeter-messages
object Greeter {
  //#greeter-messages
  def props(message: String, printerActor: ActorRef): Props =
    Props(new Greeter(message, printerActor))

  //#greeter-messages
  final case class WhoToGreet(who: String)

  case object Greet

}

//#greeter-messages
//#greeter-companion

//#greeter-actor
class Greeter(message: String, printerActor: ActorRef) extends Actor {

  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = s"$message, $who"
    case Greet =>
      //#greeter-send-message
      printerActor ! Greeting(greeting)
    //#greeter-send-message
  }
}

//#greeter-actor

//#printer-companion
//#printer-messages
object Printer {
  //#printer-messages
  def props: Props = Props[Printer]

  //#printer-messages
  final case class Greeting(greeting: String)

}

//#printer-messages
//#printer-companion

class Printer extends Actor with ActorLogging {

  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info(s"Greeting received (from ${sender()}): $greeting")
  }
}

trait Profile {
  val profile: JdbcProfile
}

/**
  * Comic Class representing comic meta data and strategies for parsing strips from comic website.
  * @param id Object id uniquely identifying a Comic.
  * @param hostname String usually the domain and tld of the comic website, useful for matching or creating urls.
  * @param title String represents the general name of the comic.
  * @param creator String the name of the comic creator.
  * @param bannerImage String location of the pre-generated image representing the comic in-app.
  * @param firstUrl String a url of the very first strip of the comic. ETL starts here if delta is false.
  */
case class Comic(id: ObjectId, hostname: String, title: String, creator: String, isAdvertised: Boolean,
                 patreonUrl: Option[String], store_url: Option[String], bannerImage: String, firstUrl: String,
                 strategy: Strategy)


case class Strategy(next: Vector[String], image: Vector[String], title: Option[Vector[String]],
                    imageTitle: Option[Vector[String]], imageAlt: Option[Vector[String]],
                    bonusImage: Option[Vector[String]])


object Comic {
  import lurker.ObjectIdReader
//  implicit val objectIdReader = lurker.ObjectIdReader
  implicit val strategyReader: Reads[Strategy] = (
    (JsPath \ "next").read[Vector[String]] and
      (JsPath \ "image").read[Vector[String]] and
      (JsPath \ "title").readNullable[Vector[String]] and
      (JsPath \ "image_title").readNullable[Vector[String]] and
      (JsPath \ "image_alt").readNullable[Vector[String]] and
      (JsPath \ "bonus_image").readNullable[Vector[String]])(Strategy.apply _)

  implicit val comicReader: Reads[Comic] = (
    (JsPath \ "id").read[ObjectId] and
      (JsPath \ "hostname").read[String] and
      (JsPath \ "title").read[String] and
      (JsPath \ "creator").read[String] and
      (JsPath \ "is_advertised").read[Boolean] and
      (JsPath \ "patreon_url").readNullable[String] and
      (JsPath \ "store_url").readNullable[String] and
      (JsPath \ "banner_image").read[String] and
      (JsPath \ "first_url").read[String] and
      (JsPath \ "strategy").read[Strategy])(Comic.apply _)
}

trait DbModule extends Profile {
  val db: JdbcProfile#Backend#Database

  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T] = {
    db.run(databaseOperation)
  }

}

trait PersistenceModule {
  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T]
}

class Persistence() extends PersistenceModule with DbModule {
  private val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig("comic_database")
  override implicit val profile: JdbcProfile = dbConfig.profile
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db
}


class Repository()(implicit ex: ExecutionContext, persistence: Persistence) {
  import persistence._
  import persistence.profile.api._

  def insertComic(comic: Comic): Future[Option[Int]] = {
    db.run(sql"""
      INSERT INTO cg.comic (
        id,
        hostname,
        title,
        creator,
        is_advertised,
        patreon_url,
        store_url,
        banner_image,
        first_url)
      VALUES (
        ${comic.id.toString},
        ${comic.hostname},
        ${comic.title},
        ${comic.creator},
        ${comic.isAdvertised.toString}::BOOLEAN,
        ${comic.patreonUrl}::TEXT,
        ${comic.store_url}::TEXT,
        ${comic.bannerImage},
        ${comic.firstUrl})
      ON CONFLICT (id) DO UPDATE SET (
        id,
        hostname,
        title,
        creator,
        is_advertised,
        patreon_url,
        store_url,
        banner_image,
        first_url) = (
        ${comic.id.toString},
        ${comic.hostname},
        ${comic.title},
        ${comic.creator},
        ${comic.isAdvertised.toString}::BOOLEAN,
        ${comic.patreonUrl}::TEXT,
        ${comic.store_url}::TEXT,
        ${comic.bannerImage},
        ${comic.firstUrl})""".as[Int].headOption)}
}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("lurker")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, getClass)

  implicit val persistence = new Persistence
  import Greeter._

  // Create the 'helloAkka' actor system

  try {
    //#create-actors
    // Create the printer actor
    val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

    // Create the 'greeter' actors
    val howdyGreeter: ActorRef =
      system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
    val helloGreeter: ActorRef =
      system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
    val goodDayGreeter: ActorRef =
      system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")
    //#create-actors

    //#main-send-messages
    howdyGreeter ! WhoToGreet("Akka")
    howdyGreeter ! Greet

    howdyGreeter ! WhoToGreet("Lightbend")
    howdyGreeter ! Greet

    helloGreeter ! WhoToGreet("Scala")
    helloGreeter ! Greet

    goodDayGreeter ! WhoToGreet("Play")
    goodDayGreeter ! Greet
    //#main-send-messages

    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  } finally {
    system.terminate()
  }
}
