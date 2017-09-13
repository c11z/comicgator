package daos

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.google.inject.{Inject, Singleton}
import daos._
import models.Strip
import org.bson.types.ObjectId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.db.NamedDatabase
import slick.driver.JdbcProfile

trait StripDAO {
  def select(comicId: ObjectId, geekId: ObjectId): Future[JsValue]

  def save(strip: Strip): Unit

  def lastStrip(comicId: ObjectId, firstUrl: String): Future[(String, Int)]
}


@Singleton
class StripDAOImpl @Inject()(@NamedDatabase("cdb") protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with StripDAO {
  import driver.api._

  def select(comicId: ObjectId, geekId: ObjectId): Future[JsValue] = {
    db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(strip_select))) AS strips FROM (
        SELECT
          s.id,
          s.comic_id,
          s.checksum,
          s.number,
          s.url,
          s.image_urls,
          s.thumbnail_image_url,
          s.title,
          s.image_alt,
          s.image_title,
          s.bonus_image_url,
          s.is_special,
          COALESCE(sg.is_viewed, FALSE) AS is_viewed,
          COALESCE(sg.is_starred, FALSE) AS is_starred,
          s.created_at,
          s.updated_at
        FROM cg.strip s LEFT JOIN cg.strip_geek sg ON s.id = sg.strip_id
        WHERE s.comic_id = ${comicId.toString} AND (sg.geek_id = ${geekId.toString} OR sg.geek_id IS NULL)
        ORDER BY s.number ASC
      ) strip_select""".as[JsValue].head)
  }


  def save(strip: Strip): Unit = {
    db.run(sqlu"""
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
        ${strip.scrap.isSpecial.toString}::BOOLEAN)""")
  }

  def lastStrip(comicId: ObjectId, firstUrl: String): Future[(String, Int)] = {
    db.run(sql"""
      SELECT s.url, s.number FROM cg.strip s JOIN cg.comic c ON s.comic_id = c.id
      WHERE c.id = ${comicId.toString} ORDER BY s.number DESC LIMIT 1""".as[(String, Int)].headOption)
        .map {
          case Some((url, number)) => (url, number)
          case None => (firstUrl, 1)
        }
  }
}
