package controllers

import javax.inject._

import actions.GeekAction
import daos.StripDAO
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
class StripController @Inject()(GeekAction: GeekAction, stripDAO: StripDAO)(implicit exec: ExecutionContext) extends Controller {
  /**
   *
   */
  def getOne(stripId: ObjectId): Action[AnyContent] = Action.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def getAll(comicId: ObjectId): Action[AnyContent] = GeekAction.async { request =>
    stripDAO.select(comicId, request.geekId).map(Ok(_))
  }
}
