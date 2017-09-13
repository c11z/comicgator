package models

import org.bson.types.ObjectId

sealed trait LurkerMessage {
  def isDelta: Boolean
  def comicIds: Vector[ObjectId]
}


case object NoOpLurker extends LurkerMessage {
  val isDelta = true
  val comicIds = Vector.empty[ObjectId]
}


case object AllFull extends LurkerMessage {
  val isDelta = false
  val comicIds = Vector.empty[ObjectId]
}


case object AllRecent extends LurkerMessage {
  val isDelta = true
  val comicIds = Vector.empty[ObjectId]
}


case class SelectFull(c: Vector[ObjectId]) extends LurkerMessage {
  val isDelta = false
  val comicIds = c
}


case class SelectRecent(c: Vector[ObjectId]) extends LurkerMessage {
  val isDelta = true
  val comicIds = c
}
