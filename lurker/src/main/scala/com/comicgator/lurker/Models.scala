package com.comicgator.lurker

import java.time.LocalDateTime

import org.bson.types.ObjectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * Comic Class representing comic meta data and strategies for parsing
  * strips from comic website.
  *
  * @param id Object id uniquely identifying a Comic.
  * @param hostname String usually the domain and tld of the comic website,
  *                 useful for matching or creating urls.
  * @param title String represents the general name of the comic.
  * @param creator String the name of the comic creator.
  *                    the comic in-app.
  * @param firstUrl String a url of the very first strip of the comic.
  *                 ETL starts here if delta is false.
  */
case class Comic(id: ObjectId,
                 hostname: String,
                 title: String,
                 creator: String,
                 firstUrl: String,
                 strategy: Strategy)

case class Strategy(next: Vector[String],
                    image: Vector[String],
                    title: Option[Vector[String]],
                    imageTitle: Option[Vector[String]],
                    imageAlt: Option[Vector[String]],
                    bonusImage: Option[Vector[String]])

object Comic {
  implicit val ObjectIdReader: Reads[ObjectId] =
    JsPath.read[String].map(new ObjectId(_))

  implicit val strategyReader: Reads[Strategy] =
    ((JsPath \ "next").read[Vector[String]] and
      (JsPath \ "image").read[Vector[String]] and
      (JsPath \ "title").readNullable[Vector[String]] and
      (JsPath \ "image_title").readNullable[Vector[String]] and
      (JsPath \ "image_alt").readNullable[Vector[String]] and
      (JsPath \ "bonus_image").readNullable[Vector[String]])(Strategy.apply _)

  implicit val comicReader: Reads[Comic] = ((JsPath \ "id").read[ObjectId] and
    (JsPath \ "hostname").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "creator").read[String] and
    (JsPath \ "first_url").read[String] and
    (JsPath \ "strategy").read[Strategy])(Comic.apply _)
}

/**
  * Object describing Comic Strip.
  * @param id Object id uniquely identifying a Strip.
  * @param comicId Comic Associated with the Strip.
  * @param thumbnailUrl String image url for a thumbnail to be eventually
  *                     generated by Lurker.
  * @param scrap Scrap class of values reaped from the html.
  */
case class Strip(id: ObjectId,
                 comicId: ObjectId,
                 thumbnailUrl: String,
                 scrap: Scrap)

/**
  * Object representing the outcome of the scraped comic strip webpage.
  * @param number Counter for tracking order of comics by release.
  * @param checksum String MD5 hash of the image url used for uniquely
  *                 identifying the comic.
  * @param imageUrls Vector of image url strings. (Required)
  * @param title String the text of the title.
  * @param bonusImageUrl String the url of the bonus image.
  * @param imageTitle String the text of the title text of the comic image.
  * @param imageAlt String the text of the alt text of the comic image.
  */
case class Scrap(number: Int,
                 checksum: String,
                 url: String,
                 imageUrls: Vector[String],
                 title: Option[String],
                 imageTitle: Option[String],
                 imageAlt: Option[String],
                 bonusImageUrl: Option[String],
                 isSpecial: Boolean)

/**
  * RSS Feed Item.
  * @param feedId ObjectId
  * @param feedName String
  * @param comicId ObjectId
  * @param comicTitle String
  * @param comicHostname String
  * @param comicCreator String
  * @param stripTitle String
  * @param stripNumber Int
  * @param stripUrl String
  * @param stripImageTitle String
  * @param stripImageAlt String
  * @param feedStripUpdatedAt LocalDateTime
  */
case class Item(feedId: ObjectId,
                feedName: String,
                comicId: ObjectId,
                comicTitle: String,
                comicHostname: String,
                comicCreator: String,
                stripTitle: String,
                stripNumber: Int,
                stripUrl: String,
                stripImageTitle: String,
                stripImageAlt: String,
                feedStripUpdatedAt: LocalDateTime) {
  val channelLink = "http://comicgator.com"
  val ttl = 60
  val webmaster = "mr@comicgator.com"
}
