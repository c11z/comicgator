package controllers

import javax.inject._

import actions.GeekAction
import daos.ComicDAO
import org.bson.types.ObjectId
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ComicController @Inject() (GeekAction: GeekAction, comicDAO: ComicDAO) (implicit exec: ExecutionContext) extends Controller {
  /**
   *
   */
  def getOne(comicId: ObjectId): Action[AnyContent] = GeekAction.async {Future.successful(NotImplemented)}

  /**
   *
   */
  def getAll: Action[AnyContent] = GeekAction.async { request =>
    comicDAO.select(request.geekId).map(Ok(_))
  }
}
