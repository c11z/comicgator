package errors

import javax.inject.Singleton

import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import play.Logger
import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = Future.successful {
    Logger.error(exception.getMessage, exception)
    exception match {
      case ex: CGException => Status(ex.code)(ex.details)
      case _ => InternalServerError
    }
  }
}
