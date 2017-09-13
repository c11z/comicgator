package controllers

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import actions.GeekAction
import com.eclipsesource.schema._
import daos.{ComicDAO, FeedDAO}
import models.Feed
import org.bson.types.ObjectId
import play.Logger
import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}

@Singleton
class FeedController @Inject() (environment: Environment,
                                GeekAction: GeekAction,
                                feedDAO: FeedDAO) (implicit exec: ExecutionContext) extends Controller {
  val validator = SchemaValidator()

  lazy val schema: SchemaType = {
    val filename = environment.getFile("/conf/schemas/feed.schema.json")
    val t = scala.io.Source.fromFile(filename).mkString
    val j = Json.parse(t)
    Json.fromJson[SchemaType](j).get
  }

  /**
    *
    */
  def getAll: Action[AnyContent] = GeekAction.async { request =>
    feedDAO.select(request.geekId).map(Ok(_))
  }

  /**
    *
    */
  def post: Action[JsValue] = GeekAction.async(parse.json) { request =>
    feedDAO.newFeed(request.geekId).map {
      feedId => Created.withHeaders(("Location", s"/feeds/${feedId.toString}"))
    }
  }

  /**
    *
    */
  def put(feedId: ObjectId): Action[JsValue] = GeekAction.async(parse.json) {request =>
    validator.validate(schema, request.body, Feed.feedReader).fold(
      invalid = {
        errors =>
          Logger.error(Json.prettyPrint(errors.toJson))
          Future.successful(BadRequest(errors.toJson))
      },
      valid = { feed =>
        feedDAO.saveFeed(request.geekId, feedId, feed).map{
          feedId => NoContent.withHeaders(("Location", s"/feeds/${feedId.toString}"))
        }
      }
    )
  }

  def delete(feedId: ObjectId): Action[AnyContent] = GeekAction.async {Future.successful(NotImplemented)}
}


