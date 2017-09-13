package controllers

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import models._
import org.bson.types.ObjectId
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LurkerController @Inject() (system: ActorSystem,
                                  @Named("lurker-actor") val lurkerActor: ActorRef)
                                 (implicit exec: ExecutionContext) extends Controller {
  /**
   *
   */
  def full(comicIds: Option[Vector[ObjectId]], isDelta: Boolean): Action[AnyContent] = Action.async { request =>
    (comicIds, isDelta) match {
      case (None, false) => lurkerActor ! AllFull
      case (None, true) => lurkerActor ! AllRecent
      case (Some(c), false) => lurkerActor ! SelectFull(c)
      case (Some(c), true) => lurkerActor ! SelectRecent(c)
    }

    val deltaMessage: String = isDelta match {
      case true => "delta"
      case false => "full"
    }

    val comicMessage: String = comicIds match {
      case Some(c) => s"Comic Ids ${c.mkString(", ")}"
      case None => "All Comics"
    }

    Future.successful(
      Accepted(
        Json.obj(
          "status" -> "Lurker Awakened",
          "message" -> s"Running $deltaMessage ETL on $comicMessage."
        )
      )
    )
  }
}
