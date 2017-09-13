package errors

import play.api.libs.json.{JsValue, Json}

sealed trait CGException {
  self: Throwable =>
  val code: Int
  lazy val details: JsValue = code match {
    case 500 =>
      Json.obj(
        "code" -> code,
        "status" -> "Internal Server Error",
        "message" ->
        """Something horrible has happened to our code. You viewing this does not fix the code.
          | In fact you are likely pissed off at the moment. Take solace, *top men* are now aware.""".stripMargin
      )
    case 400 =>
      Json.obj(
        "code" -> code,
        "status" -> "Bad Request",
        "message" -> "Doh!"
      )
  }

}

case class MailGunEmailInvalidException(message: Option[String] = None)
  extends Exception(message.getOrElse("woops")) with CGException {
  override val code: Int = 400
}

case class MailGunException(message: String)
  extends Exception(message) with CGException {
  override val code: Int = 500
}

case class CDBException(method: String) extends Exception(method) with CGException {
  override val code: Int = 500
}
