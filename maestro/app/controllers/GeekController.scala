package controllers

import javax.inject._

import org.bson.types.ObjectId
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.
 */
@Singleton
class GeekController @Inject() ()(implicit exec: ExecutionContext) extends Controller {
  /**
   *
   */
  def getOne(geekId: ObjectId): Action[AnyContent] = Action.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def put: Action[AnyContent] = Action.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def starComic(comic: ObjectId): Action[AnyContent] = Action.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def starStrip(stripId: ObjectId): Action[AnyContent] = Action.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def viewStrip(stripId: ObjectId): Action[AnyContent] = Action.async {Future.successful(NotImplemented)}
}
