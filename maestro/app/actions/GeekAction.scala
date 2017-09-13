package actions

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import daos.GeekDAO
import org.bson.types.ObjectId
import play.api.mvc._
import play.api.mvc.Results.Forbidden

class GeekRequest[A](val geekId: ObjectId, request: Request[A]) extends WrappedRequest[A](request)

class GeekAction @Inject() (geekDAO: GeekDAO)
  extends ActionBuilder[GeekRequest] with ActionRefiner[Request, GeekRequest] {
  def refine[A](request: Request[A]): Future[Either[Result, GeekRequest[A]]] = {
    request.cookies.get("cg_token") match {
      case Some(token) => geekDAO.checkSession(token.value).map {
        case Some(id) => Right(new GeekRequest[A](id, request))
        case None => Left(Forbidden)
      }
      case None => Future.successful(Left(Forbidden))
    }
  }
}
