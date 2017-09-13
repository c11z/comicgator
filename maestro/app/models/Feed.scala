package models

import java.time.LocalDateTime

import org.bson.types.ObjectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * Feed class
  *
  * @param name String name of feed.
  * @param comics Vector of FeedComic.
  */
case class Feed(name: String, comics: Vector[FeedComic])

case class FeedComic(comicId: ObjectId, isLatest: Boolean, isReplay: Boolean, mark: Option[Int], step: Option[Int],
                     startAt: Option[LocalDateTime]) {
  // Mark indicates last successful strip, for initial run in order to include the desired starting strip we subtract 1.
  val initialMark: Int = mark.getOrElse(1) -1
}

object Feed {
  implicit val objectIdReader = ObjectIdReader

  implicit val feedComicReader: Reads[FeedComic] = (
    (JsPath \ "comic_id").read[ObjectId] and
      (JsPath \ "is_latest").read[Boolean] and
      (JsPath \ "is_replay").read[Boolean] and
      (JsPath \ "mark").readNullable[Int] and
      (JsPath \ "step").readNullable[Int] and
      (JsPath \ "start_at").readNullable[LocalDateTime])(FeedComic.apply _)

  implicit val feedReader: Reads[Feed] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "comics").read[Vector[FeedComic]])(Feed.apply _)
}
