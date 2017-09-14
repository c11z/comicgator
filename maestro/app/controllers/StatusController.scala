package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class StatusController @Inject() extends Controller {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def health: Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "ok", "tagline" -> "as in web COMIC aggreGATOR"))
  }
}