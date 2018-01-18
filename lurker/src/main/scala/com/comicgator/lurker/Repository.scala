package com.comicgator.lurker

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import org.bson.types.ObjectId
import play.api.libs.json.{JsValue, Json}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Repository for database persistence queries.
  */
object Repository extends Conf {
  implicit val getJsonResult: GetResult[JsValue] = GetResult(
    r => Json.parse(r.nextString))
  implicit val getObjectIdResult: GetResult[ObjectId] = GetResult(
    r => new ObjectId(r.nextString))
  implicit val getRSSResult: GetResult[Item] = GetResult(
    r =>
      Item(
        new ObjectId(r.nextString),
        r.nextString,
        new ObjectId(r.nextString),
        r.nextString,
        r.nextString,
        r.nextString,
        r.nextString,
        r.nextInt,
        r.nextString,
        r.nextString,
        r.nextString,
        r.nextTimestamp.toLocalDateTime
    )
  )

  private val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig("comic_database")
  private val profile: JdbcProfile = dbConfig.profile
  private val db: JdbcProfile#Backend#Database = dbConfig.db
  import profile.api._

  def shutdown(): Unit = db.close()

  /*
   * Inserts comic
   * @param comic Comic
   * @return
   */
  def insertComic(comic: Comic)(implicit ec: ExecutionContext): Future[Int] = {
    db.run(sql"""
      INSERT INTO cg.comic (
        id,
        hostname,
        title,
        creator,
        first_url)
      VALUES (
        ${comic.id.toString},
        ${comic.hostname},
        ${comic.title},
        ${comic.creator},
        ${comic.firstUrl})
      ON CONFLICT (id) DO UPDATE SET (
        id,
        hostname,
        title,
        creator,
        first_url) = (
        ${comic.id.toString},
        ${comic.hostname},
        ${comic.title},
        ${comic.creator},
        ${comic.firstUrl})""".as[Int].head)
  }

  /*
   * Inserts comic strip
   * @param strip Strip
   * @return
   */
  def insertStrip(strip: Strip)(implicit ec: ExecutionContext): Future[Int] = {
    db.run(sql"""
      INSERT INTO cg.strip (
        comic_id,
        checksum,
        number,
        url,
        image_urls,
        thumbnail_image_url,
        title,image_alt,
        image_title,
        bonus_image_url,
        is_special)
      VALUES (
        ${strip.comicId.toString},
        ${strip.scrap.checksum},
        ${strip.scrap.number},
        ${strip.scrap.url},
        ${strip.scrap.imageUrls.mkString("{\"", "\",\"", "\"}")}::TEXT[],
        ${strip.thumbnailUrl},
        ${strip.scrap.title}::TEXT,
        ${strip.scrap.imageAlt}::TEXT,
        ${strip.scrap.imageTitle}::TEXT,
        ${strip.scrap.bonusImageUrl}::TEXT,
        ${strip.scrap.isSpecial.toString}::BOOLEAN)
      ON CONFLICT (comic_id, number) DO UPDATE SET (
        comic_id,
        checksum,
        number,
        url,
        image_urls,
        thumbnail_image_url,
        title,image_alt,
        image_title,
        bonus_image_url,
        is_special) = (
        ${strip.comicId.toString},
        ${strip.scrap.checksum},
        ${strip.scrap.number},
        ${strip.scrap.url},
        ${strip.scrap.imageUrls.mkString("{\"", "\",\"", "\"}")}::TEXT[],
        ${strip.thumbnailUrl},
        ${strip.scrap.title}::TEXT,
        ${strip.scrap.imageAlt}::TEXT,
        ${strip.scrap.imageTitle}::TEXT,
        ${strip.scrap.bonusImageUrl}::TEXT,
        ${strip.scrap.isSpecial.toString}::BOOLEAN)""".as[Int].head)
  }

  /*
   * Finds the most recent comic strip.
   * @param comicId ObjectId
   * @param firstUrl String, first strip url of the comic
   * @return
   */
  def lastStrip(comicId: ObjectId, firstUrl: String)(
      implicit ec: ExecutionContext): Future[(String, Int)] = {
    db.run(
        sql"""
      SELECT s.url, s.number FROM cg.strip s JOIN cg.comic c ON s.comic_id = c.id
      WHERE c.id = ${comicId.toString} ORDER BY s.number DESC LIMIT 1"""
          .as[(String, Int)]
          .headOption)
      .map {
        case Some((url, number)) => (url, number)
        case None => (firstUrl, 1)
      }
  }

  /*
   * Finds feed comics flagged as latest. For each feed comic finds the highest feed strip number, then inserts
   * all strips that have a higher number.
   */
  def latestFeedStrip()(implicit ec: ExecutionContext): Future[Int] = {
    db.run(sqlu"""
      DO $$$$
      DECLARE
        rec cg.feed_comic%ROWTYPE;
      BEGIN
        FOR rec IN EXECUTE 'SELECT * FROM cg.feed_comic fc
        WHERE fc.is_latest = TRUE'
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
      END $$$$""").map(_ => 1)
  }

  /*
   * Finds feed comics that have replay flag and the next datetime is before the start of the transaction. Inserts
   * strips into feed strip for the next step above mark. Then updates mark += step and adds the interlude to next_at.
   */
  def replayFeedStrip()(implicit ec: ExecutionContext): Future[Int] = {
    db.run(sqlu"""
      DO $$$$
      DECLARE
        rec cg.feed_comic%ROWTYPE;
      BEGIN
        FOR rec IN EXECUTE 'SELECT * FROM cg.feed_comic fc
        WHERE fc.is_replay = TRUE AND next_at < CURRENT_TIMESTAMP'
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
      END $$$$""").map(_ => 1)
  }

  def readyFeeds(init: LocalDateTime)(implicit ec: ExecutionContext): Future[Vector[Item]] = {
    db.run(sql"""
      WITH
        ready_feeds AS (
          SELECT DISTINCT fs.feed_id
          FROM cg.feed_strip fs
          WHERE fs.updated_at > ${init.format(ISO_DATE_TIME)}::TIMESTAMP
        )
      SELECT
        f.id,
        f.name,
        c.id,
        c.title,
        c.hostname,
        c.creator,
        s.title,
        s.number,
        s.url,
        s.image_title,
        s.image_alt,
        fs.updated_at
      FROM cg.feed_strip fs
      LEFT JOIN cg.feed f ON fs.feed_id = f.id
      LEFT JOIN cg.strip s ON fs.strip_id = s.id
      LEFT JOIN cg.comic c ON s.comic_id = c.id
      WHERE fs.feed_id IN (SELECT feed_id FROM ready_feeds)
      AND fs.updated_at > (CURRENT_TIMESTAMP - INTERVAL '3 day')
    """.as[Item])
  }
}
