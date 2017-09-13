package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsResult, JsValue, Reads}

case class Geek(email: String, isReceptive: Boolean)


object Geek {
  implicit object Reader extends Reads[Geek] {
    def reads(json: JsValue): JsResult[Geek] = (
      (JsPath \ "email").read[String] and
      (JsPath \ "is_receptive").read[Boolean]
    )(Geek.apply _).reads(json)
  }
}

