package models

import org.bson.types.ObjectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
 * Comic Class representing comic meta data and strategies for parsing strips from comic website.
 * @param id Object id uniquely identifying a Comic.
 * @param hostname String usually the domain and tld of the comic website, useful for matching or creating urls.
 * @param title String represents the general name of the comic.
 * @param creator String the name of the comic creator.
 * @param bannerImage String location of the pre-generated image representing the comic in-app.
 * @param firstUrl String a url of the very first strip of the comic. ETL starts here if delta is false.
 */
case class Comic(id: ObjectId, hostname: String, title: String, creator: String, isAdvertised: Boolean,
                 patreonUrl: Option[String], store_url: Option[String], bannerImage: String, firstUrl: String,
                 strategy: Strategy)


case class Strategy(next: Vector[String], image: Vector[String], title: Option[Vector[String]],
                    imageTitle: Option[Vector[String]], imageAlt: Option[Vector[String]],
                    bonusImage: Option[Vector[String]])


object Comic {
  implicit val objectIdReader = ObjectIdReader

  implicit val strategyReader: Reads[Strategy] = (
      (JsPath \ "next").read[Vector[String]] and
        (JsPath \ "image").read[Vector[String]] and
        (JsPath \ "title").readNullable[Vector[String]] and
        (JsPath \ "image_title").readNullable[Vector[String]] and
        (JsPath \ "image_alt").readNullable[Vector[String]] and
        (JsPath \ "bonus_image").readNullable[Vector[String]])(Strategy.apply _)

  implicit val comicReader: Reads[Comic] = (
      (JsPath \ "id").read[ObjectId] and
        (JsPath \ "hostname").read[String] and
        (JsPath \ "title").read[String] and
        (JsPath \ "creator").read[String] and
        (JsPath \ "is_advertised").read[Boolean] and
        (JsPath \ "patreon_url").readNullable[String] and
        (JsPath \ "store_url").readNullable[String] and
        (JsPath \ "banner_image").read[String] and
        (JsPath \ "first_url").read[String] and
        (JsPath \ "strategy").read[Strategy])(Comic.apply _)
}
