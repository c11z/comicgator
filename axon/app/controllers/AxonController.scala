package controllers

import javax.inject._

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.db.NamedDatabase
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Web API for Comic Gator MVP
  */
@Singleton
class AxonController @Inject()(
    @NamedDatabase("cdb") protected val dbConfigProvider: DatabaseConfigProvider,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._
  implicit val getJsonResult: AnyRef with GetResult[JsValue] =
    GetResult(r => Json.parse(r.nextString))

  def health: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(Json.obj("status" -> "ok", "tagline" -> "as in web COMIC aggreGATOR"))
  }

  def comics: Action[AnyContent] = Action.async { request =>
    val query: Future[Option[JsValue]] = db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(comic_select))) AS comics FROM (
        SELECT
          c.id,
          c.hostname,
          c.title,
          c.creator,
          COALESCE(sc.strip_count, 0) AS strip_count,
          c.is_advertised,
          c.patreon_url,
          c.store_url,
          c.banner_image,
          c.created_at,
          c.updated_at
        FROM cg.comic c
          LEFT JOIN (
            SELECT s.comic_id, count(s.id) AS strip_count
            FROM cg.strip s
            GROUP BY s.comic_id
          ) sc ON c.id = sc.comic_id
        ORDER BY c.id ASC
      ) comic_select""".as[JsValue].headOption)
    query.map{ result =>
      Ok(result.getOrElse(Json.arr()))
    }
  }
}
