package daos

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.google.inject.{Inject, Singleton}
import daos._
import models.Comic
import org.bson.types.ObjectId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.db.NamedDatabase
import slick.driver.JdbcProfile

trait ComicDAO {
  def select(geekId: ObjectId): Future[JsValue]

  def save(comic: Comic): Unit
}


@Singleton
class ComicDAOImpl @Inject()(@NamedDatabase("cdb") protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with ComicDAO {
  import driver.api._

  def select(geekId: ObjectId): Future[JsValue] = {
    db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(comic_select))) AS comics FROM (
        SELECT
          c.id,
          c.hostname,
          c.title,
          c.creator,
          c.is_advertised,
          c.patreon_url,
          c.store_url,
          c.banner_image,
          COALESCE(cg.is_viewed, FALSE) AS is_viewed,
          COALESCE(cg.is_starred, FALSE) AS is_starred,
          c.created_at,
          c.updated_at
        FROM cg.comic c
          LEFT JOIN cg.comic_geek cg ON c.id = cg.comic_id
        WHERE cg.geek_id = ${geekId.toString} OR cg.geek_id IS NULL
        ORDER BY c.id ASC
      ) comic_select""".as[JsValue].head)
  }

  def save(comic: Comic): Unit = {
    db.run(sqlu"""
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
        ${comic.firstUrl})""")
  }
}
