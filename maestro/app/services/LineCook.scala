package services

import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import daos.FeedDAO
import models._
import play.api.Environment

class LineCook @Inject() (val system: ActorSystem,
                          @Named("line-cook-actor") val lineCookActor: ActorRef)
                         (implicit ec: ExecutionContext) {
  if (ConfigFactory.load().getBoolean("is_line_cook")) {
    system.scheduler.schedule(1.minute, 1.minute, lineCookActor, RunReplay)
    system.scheduler.schedule(1.minute, 1.hour, lineCookActor, RunLatest)
  } else {
    system.scheduler.schedule(1.minute, 1.hour, lineCookActor, NoOpLineCook)
  }
}

@Singleton
class LineCookActor @Inject() (system: ActorSystem,
                               environment: Environment,
                               feedDAO: FeedDAO
                              ) extends Actor with ActorLogging {
  override def preStart: Unit = {
    log.info("Starting LineCook")
  }

  def receive: PartialFunction[Any, Unit] = {
    case RunReplay =>
      log.info("Running replay.")
      feedDAO.replayFeedStrip()
    case RunLatest =>
      log.info("Running Latest.")
      feedDAO.latestFeedStrip()
    case NoOpLineCook => log.info("No cooking today.")
  }
}
