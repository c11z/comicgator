import javax.inject.Inject

import akka.stream.Materializer
import play.api.Logger
import play.api.http.{DefaultHttpFilters, EnabledFilters}
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.routing.Router
import play.filters.gzip.GzipFilter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class LoggingFilter @Inject()(
    implicit val mat: Materializer,
    ec: ExecutionContext
) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(
      requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader).map { result =>
      val action: String = Try(requestHeader.attrs(Router.Attrs.HandlerDef)).toOption match {
        case Some(handlerDef) => handlerDef.controller + "." + handlerDef.method
        case None => "public"
      }
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      Logger.info(
        s"${requestHeader.method} $action took ${requestTime}ms and returned ${result.header.status}")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}

class Filters @Inject()(
    defaultFilters: EnabledFilters,
    gzip: GzipFilter,
    log: LoggingFilter
) extends DefaultHttpFilters(defaultFilters.filters :+ gzip :+ log: _*)
