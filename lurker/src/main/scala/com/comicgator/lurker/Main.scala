package com.comicgator.lurker

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

object Main extends App with Conf with LazyLogging {
  // Use single execution context for future sync.
  implicit val ec: ExecutionContext = ExecutionContext.global
  logger.info("Waiting on you to initialize the database...")
  Thread.sleep(7 * 60 * 1000) // 7 minutes

  while (true) {
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
      Repository.readyFeeds.map { (allItems: Vector[Item]) =>
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
    logger.info("Finished Loop")
    Thread.sleep(10 * 60 * 1000) // 10 minutes
  }
  // Safely close database connection pool.
  Repository.shutdown()
  // Exit successfully.
  sys.exit(0)
}
