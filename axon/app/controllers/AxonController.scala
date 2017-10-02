package controllers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import javax.inject._

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.db.NamedDatabase
import slick.jdbc.{GetResult, JdbcProfile}
import scala.concurrent.{ExecutionContext, Future}

import com.eclipsesource.schema.{SchemaType, SchemaValidator}
import org.bson.types.ObjectId
import play.api.Logger

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
  implicit val getJsonResult: GetResult[JsValue] =
    GetResult(r => Json.parse(r.nextString))
  implicit val getObjectIdResult: GetResult[ObjectId] =
    GetResult(r => new ObjectId(r.nextString))

  private val validator = new SchemaValidator()
  def health: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(Json.obj("status" -> "ok", "tagline" -> "as in web COMIC aggreGATOR"))
  }

  /*
   * Handle GET request for comics, queries minimal metadata from comics and
   * joins with the comic_strip_count materialized view to obtain a strip_count.
   */
  def getAllComics: Action[AnyContent] = Action.async { request =>
    val query: Future[Option[JsValue]] = db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(comic_select))) AS comics FROM (
        SELECT
          c.id,
          c.hostname,
          c.title,
          c.creator,
          csc.strip_count,
        FROM cg.comic c
          LEFT JOIN cg.comic_strip_count csc ON c.id = csc.comic_id
        ORDER BY c.id ASC
      ) comic_select""".as[JsValue].headOption)
    query.map { result =>
      Ok(result.getOrElse(Json.arr()))
    }
  }

  /**
    * Basic feed request structure.
    * @param email String
    * @param comicId ObjectId
    * @param isLatest Boolean
    * @param isReplay Boolean
    * @param mark Option Int
    * @param step Option Int
    * @param startAt Option LocalTimeDate
    */
  case class Feed(email: String,
                  comicId: ObjectId,
                  isLatest: Boolean,
                  isReplay: Boolean,
                  mark: Option[Int],
                  step: Option[Int],
                  startAt: Option[LocalDateTime]) {
    // Mark indicates last successful strip, for initial run in order to
    // include the desired starting strip we subtract 1.
    val initialMark: Int = mark.getOrElse(1) - 1
    // Default should be 8am on the day before it is created that way it will run soon.
    val defaultStartAt: LocalDateTime = startAt.getOrElse(
      LocalDateTime.now().minusDays(1).withHour(8).withMinute(0).withSecond(0))
  }

  object Feed {
    implicit val objectIdReader: Reads[ObjectId] = JsPath.read[String].map(new ObjectId(_))

    implicit val feedReader: Reads[Feed] =
      ((JsPath \ "email").read[String] and
        (JsPath \ "comic_id").read[ObjectId] and
        (JsPath \ "is_latest").read[Boolean] and
        (JsPath \ "is_replay").read[Boolean] and
        (JsPath \ "mark").readNullable[Int] and
        (JsPath \ "step").readNullable[Int] and
        (JsPath \ "start_at").readNullable[LocalDateTime])(Feed.apply _)
  }

  private val feedSchema: SchemaType = Json
    .fromJson[SchemaType](
      Json.parse("""{
      |  "$schema": "http://json-schema.org/draft-04/schema#",
      |  "properties": {
      |    "email": {
      |      "type": "string"
      |    },
      |    "comic_id": {
      |      "type": "string",
      |      "pattern": "^[0-9a-f]{24}$"
      |    },
      |    "is_latest": {
      |      "type": "boolean"
      |    },
      |    "is_replay": {
      |      "type": "boolean"
      |    },
      |    "mark": {
      |      "type": "integer, null",
      |      "minimum": 1
      |    },
      |    "step": {
      |      "type": "integer",
      |      "minimum": 1,
      |      "maximum": 30
      |    }
      |  },
      |  "required": [
      |    "email",
      |    "comic_id",
      |    "is_latest",
      |    "is_replay"
      |  ],
      |  "additionalProperties": false
      |}""".stripMargin))
    .get

  /*
   * Handle post of general single comic feed. Forces user and feed
   * insert/update, then inserts into the comic feed table. Default
   * isReceptive is true for marketing purposes.
   */
  def postFeed: Action[JsValue] = Action.async(parse.json) { request =>
    val va = validator.validate(feedSchema, request.body, Feed.feedReader)
    va.fold(
      invalid = { errors =>
        Logger.error(s"${errors.toString}")
        Future.successful(
          BadRequest(Json.obj("status" -> "Bad Request",
                              "message" -> "Invalid json request body.")))
      },
      valid = { feed =>
        val isReceptive = true
        (for {
            geekId <- db.run(sql"""
              INSERT INTO cg.geek (email, is_receptive)
              VALUES (${feed.email}, $isReceptive::BOOLEAN)
              ON CONFLICT (email) DO
              UPDATE SET (email, is_receptive) =
              (${feed.email}, $isReceptive::BOOLEAN)
              RETURNING id""".as[ObjectId].head)
            feedId <- db.run(sql"""
              INSERT INTO cg.feed (geek_id, name)
              VALUES (${geekId.toString}, 'Feed Name')
              RETURNING id""".as[ObjectId].head)
            feedComicInsert <- db.run(sql"""
              INSERT INTO cg.feed_comic (
                feed_id,
                comic_id,
                is_latest,
                is_replay,
                mark,
                step,
                interlude,
                start_at,
                next_at)
              VALUES (
                ${feedId.toString},
                ${feed.comicId.toString},
                ${feed.isLatest},
                ${feed.isReplay},
                ${feed.initialMark},
                ${feed.step.getOrElse(0)},
                '1 day',
                ${feed.defaultStartAt.format(ISO_DATE_TIME)},
                ${feed.defaultStartAt.format(ISO_DATE_TIME)}
              )
              ON CONFLICT (feed_id, comic_id)
              DO UPDATE SET (
                is_latest,
                is_replay,
                mark, step,
                interlude,
                start_at,
                next_at) = (
                ${feed.isLatest},
                ${feed.isReplay},
                ${feed.initialMark},
                ${feed.step.getOrElse(0)},
                '1 day',
                ${feed.defaultStartAt.format(ISO_DATE_TIME)},
                ${feed.defaultStartAt.format(ISO_DATE_TIME)}
              )""".as[Int].head)
        } yield feedId).map { feedId =>
          NoContent
            .withHeaders(("Location", s"http://localhost:9000/feeds/${feedId.toString}"))
        }
      }
    )
  }

  def getOneFeed: Action[AnyContent] = Action.async { request =>
    val query: Future[Option[JsValue]] = db.run(sql"""
      SELECT array_to_json(array_agg(row_to_json(feed_select))) AS feeds FROM (
        SELECT
          c.id,
          c.hostname,
          c.title,
          c.creator,
          csc.strip_count,
        FROM cg.comic c
          LEFT JOIN cg.comic_strip_count csc ON c.id = csc.comic_id
        ORDER BY c.id ASC
      ) feed_select""".as[JsValue].headOption)
    query.map { result =>
      Ok(result.getOrElse(Json.arr()))
    }
  }

}
