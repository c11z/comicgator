package com.comicgator.lurker

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.Source

object Main extends Conf with LazyLogging {
  // Use single execution context for future sync.
  implicit val ec: ExecutionContext = ExecutionContext.global

  sys.ShutdownHookThread {
    shutdown()
  }

  def shutdown(): Unit = {
    // Safely close database connection pool.
    logger.info("Shutting down Database Repository.")
    Repository.shutdown()
    logger.info("Stopping Health Server.")
    HealthServer.stop()
  }

  def main(args: Array[String]): Unit = {
    HealthServer.start()
    try {
      while (true) {
        val init = LocalDateTime.now()
        logger.info(s"init: ${init.format(ISO_DATE_TIME)}")
        // Load comic configuration from json file.
        val comics: Vector[Comic] =
          Json
            .fromJson[Vector[Comic]](
            Json.parse(Source.fromResource("comics.json").getLines().mkString))
            .get

        // Launch etl process.
        // Await sequenced future results before shutting down.
        Await.result(Mech.etl(comics), Duration.Inf)

        // Run Maintenance queries to iterate feeds and refresh views
        val maintenance: Future[Int] = for {
          replay <- Repository.replayFeedStrip
          latest <- Repository.latestFeedStrip
        } yield {
          val successCount = replay + latest
          logger.info(s"Ran $successCount maintenance queries.")
          successCount
        }
        Await.result(maintenance, Duration.Inf)

        // Generate updated RSS feeds and send to Google Storage
        val feedSuccess: Future[Boolean] = {
          Repository.readyFeeds(init).map { (allItems: Vector[Item]) =>
            allItems
              .groupBy(item => item.feedId)
              .map {
                case (feedId, items) =>
                  val feedXML = Syndication.makeFeed(items)
                  val blob = Storage.putFeed(feedId, feedXML)
                  logger.info(
                    s"http://$FEED_STORAGE_BUCKET/${feedId.toString}/rss.xml")
                  blob.exists()
              }
              .forall(identity)
          }
        }

        Await.result(feedSuccess, Duration.Inf)
        logger.info(s"ETL complete, sleeping for $INTERLUDE ms.")
        Thread.sleep(INTERLUDE)
      }
    }  catch {
      case ex: Throwable =>
        logger.error(ex.toString)
        shutdown()
    }
  }

}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object HealthServer extends LazyLogging {
  implicit val system: ActorSystem = ActorSystem("lurker")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def start(): Unit = {
    val route =
      path("health") {
        get {
          logger.info("GET /health -- 200 (OK)")
          complete(HttpEntity(ContentTypes.`application/json`, """{"status": "OK", "message":"Rock On"}"""))
        }
      }

    Http().bindAndHandle(route, "0.0.0.0", 8080)

    println(s"Server online at http://0.0.0.0:8080/")
  }

  def stop(): Unit = {
    system.terminate()
  }
}
