package com.comicgator.lurker

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

object Main extends App with Conf with LazyLogging {
  // Use single execution context for future sync.
  implicit val ec: ExecutionContext = ExecutionContext.global
  // Load comic configuration from json file.
  private val comics: Vector[Comic] = Json
    .fromJson[Vector[Comic]](
      Json.parse(Source.fromResource("comics.json").getLines().mkString))
    .get

  // Launch etl process.
  // Await sequenced future results before shutting down.
  Await.result(Mech.etl(comics), Duration.Inf)

  // Run Maintenance queries to iterate feeds and refresh views
  val maintenance: Future[Int] = for {
    refresh <- Repository.refreshComicStripCount
    replay <- Repository.replayFeedStrip
    latest <- Repository.latestFeedStrip
  } yield {
    val successCount = refresh + replay + latest
    logger.info(s"Ran $successCount maintenance queries.")
    successCount
  }
  Await.result(maintenance, Duration.Inf)

  // Generate updated RSS feeds
//  val storeFeeds = Repository.readyRSS.map { (rssV: Vector[RSS]) =>
//    rssV.groupBy(rss => rss.feedId).foreach {
//      case (feedId, rss) =>
//        rss.foreach(r => Storage.putFeed(feedId, r.comicTitle))
//    }
//  }
//  Await.result(storeFeeds, Duration.Inf)

  // Safely close database connection pool.
  Repository.shutdown()
  // Exit successfully.
  sys.exit(0)
}
