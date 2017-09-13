package controllers

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import models.{RunLatest, RunReplay}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

@Singleton
class LineCookController @Inject() (system: ActorSystem,
                                    @Named("line-cook-actor") val lineCookActor: ActorRef)
                                   (implicit exec: ExecutionContext) extends Controller {
  def replay: Action[AnyContent] = Action.async { request =>
    lineCookActor ! RunReplay

    Future.successful(
      Accepted(
        Json.obj(
          "status" -> "Special Menu",
          "message" -> s"Running Replay Pipeline."
        )
      )
    )

  }
  def latest: Action[AnyContent] = Action.async { request =>
   lineCookActor ! RunLatest

    Future.successful(
      Accepted(
        Json.obj(
          "status" -> "Order's Up",
          "message" -> s"Running Latest Pipeline."
        )
      )
    )

  }
}
