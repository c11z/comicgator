package daos

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.google.inject.{Inject, Singleton}
import daos._
import models.{Feed, FeedComic}
import org.bson.types.ObjectId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import utils.Name


trait FeedDAO {
  def select(geekId: ObjectId): Future[JsValue]
  def newFeed(geekId: ObjectId): Future[ObjectId]
  def saveFeed(geekId: ObjectId, feedId: ObjectId, feed: Feed): Future[ObjectId]
  def saveFeedComic(feedId: ObjectId, feedComic: FeedComic): Unit
  def latestFeedStrip(): Unit
  def replayFeedStrip(): Unit
}


@Singleton
class FeedDAOImpl @Inject()(@NamedDatabase("cdb") protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with FeedDAO {
  import driver.api._

  /* TODO: Need to figure out the correct interface for the front end.
   * I am confident that the underlying data model is correct, but the react/redux state model might require objects
   * instead of lists and it would be helpful if the api catered to that.
   */
  def select(geekId: ObjectId): Future[JsValue] = {
    db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(feed_select))) AS feeds FROM (
        SELECT
          f.id,
          f.name,
          f.created_at,
          f.updated_at
        FROM cg.feed f
        WHERE f.geek_id = ${geekId.toString}
        ORDER BY f.id ASC
      ) feed_select""".as[JsValue].head)
  }

  def newFeed(geekId: ObjectId): Future[ObjectId] = {
    db.run(sql"""
      INSERT INTO cg.feed (
        geek_id,
        name)
      VALUES (
        ${geekId.toString},
        ${Name.generate})
      RETURNING id""".as[ObjectId].head)
  }

  def saveFeed(geekId: ObjectId, feedId: ObjectId, feed: Feed): Future[ObjectId] = {
    db.run(sql"""
      UPDATE cg.feed f
      SET name = ${feed.name}
      WHERE f.id = ${feedId.toString}
      AND f.geek_id = ${geekId.toString}
      RETURNING id""".as[ObjectId].head)
  }

  /*
   * Touch feed comic to trigger update timestamp
   */
  def touchFeedComic(feedId: ObjectId): Unit = {
    db.run(sqlu"""UPDATE cg.feed SET feed_id = ${feedId.toString} WHERE feed_id = ${feedId.toString}""")
  }

  /*
   *
   */
  def getUpdatedFeeds: Future[Vector[ObjectId]] = {
    db.run(sql"""SELECT """.as[ObjectId])
  }

  /*
   * Batch job
   * First: insert on conflict update feed comic.
   * Second: if feed comic has latest flag, insert highest comic strip number into feed strip.
   */
  def saveFeedComic(feedId: ObjectId, feedComic: FeedComic): Unit = {
    db.run(sqlu"""
      DO $$$$
      DECLARE
        _is_latest BOOLEAN := ${feedComic.isLatest};
        _is_replay BOOLEAN := ${feedComic.isReplay};
      BEGIN
        INSERT INTO cg.feed_comic (feed_id, comic_id, is_latest, is_replay, mark, step, interlude, start_at, next_at)
        VALUES (
          ${feedId.toString},
          ${feedComic.comicId.toString},
          ${feedComic.isLatest},
          ${feedComic.isReplay},
          ${feedComic.initialMark},
          ${feedComic.step.getOrElse(0)},
          '1 day',
          ${feedComic.startAt.getOrElse(LocalDateTime.now()).format(ISO_DATE_TIME)},
          ${feedComic.startAt.getOrElse(LocalDateTime.now()).format(ISO_DATE_TIME)}
        )
        ON CONFLICT (feed_id, comic_id)
        DO UPDATE SET (is_latest, is_replay, mark, step, interlude, start_at, next_at) = (
          ${feedComic.isLatest},
          ${feedComic.isReplay},
          ${feedComic.initialMark},
          ${feedComic.step.getOrElse(0)},
          '1 day',
          ${feedComic.startAt.getOrElse(LocalDateTime.now()).format(ISO_DATE_TIME)},
          ${feedComic.startAt.getOrElse(LocalDateTime.now()).format(ISO_DATE_TIME)}
        );

        IF _is_latest
        THEN
          INSERT INTO cg.feed_strip (feed_id, strip_id)
          VALUES (
            ${feedId.toString},
            (
              SELECT s.id
              FROM cg.strip s
              WHERE s.comic_id = ${feedComic.comicId.toString}
              ORDER BY s.number DESC
              LIMIT 1
            )
          )
          ON CONFLICT (feed_id, strip_id)
          DO UPDATE SET (feed_id) = (${feedId.toString});
        END IF;
      END $$$$""")
  }

  /*
   * Finds feed comics flagged as latest. For each feed comic finds the highest feed strip number, then inserts
   * all strips that have a higher number.
   */
  def latestFeedStrip(): Unit = {
    db.run(sqlu"""
      DO $$$$
      DECLARE
        rec cg.feed_comic%ROWTYPE;
      BEGIN
        FOR rec IN EXECUTE 'SELECT * FROM cg.feed_comic fc WHERE fc.is_latest = TRUE'
        LOOP
          INSERT INTO cg.feed_strip (feed_id, strip_id)
            (
              SELECT
                rec.feed_id,
                s.id
              FROM cg.strip s
              WHERE s.comic_id = rec.comic_id
              AND s.number > (
                SELECT s.number
                FROM cg.feed_strip fs
                  JOIN cg.strip s ON fs.strip_id = s.id
                WHERE s.comic_id = rec.comic_id
                ORDER BY s.number DESC
                LIMIT 1
              )
            );
        END LOOP;
      END $$$$""")
  }

  /*
   * Finds feed comics that have replay flag and the next datetime is before the start of the transaction. Inserts
   * strips into feed strip for the next step above mark. Then updates mark += step and adds the interlude to next_at.
   */
  def replayFeedStrip(): Unit = {
    db.run(sqlu"""
      DO $$$$
      DECLARE
        rec cg.feed_comic%ROWTYPE;
      BEGIN
        FOR rec IN EXECUTE 'SELECT * FROM cg.feed_comic fc WHERE fc.is_replay = TRUE AND next_at < CURRENT_TIMESTAMP'
        LOOP
          INSERT INTO cg.feed_strip (feed_id, strip_id) (
            SELECT
              rec.feed_id,
              s.id
            FROM cg.strip s
            WHERE s.comic_id = rec.comic_id
                  AND s.number > rec.mark AND s.number <= rec.mark + rec.step)
          ON CONFLICT (feed_id, strip_id)
            DO UPDATE SET (feed_id) = (rec.feed_id);

          UPDATE cg.feed_comic
          SET (mark, next_at) = (rec.mark + rec.step, rec.next_at + rec.interlude)
          WHERE feed_id = rec.feed_id
          AND comic_id = rec.comic_id;
        END LOOP;
      END $$$$""")
  }
}
