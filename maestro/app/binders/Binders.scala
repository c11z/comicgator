package binders

import org.bson.types.ObjectId
import play.api.mvc.{PathBindable, QueryStringBindable}

object Binders {
  implicit def objectIdPathBinder: PathBindable[ObjectId] = new PathBindable[ObjectId] {
    override def bind(key: String, value: String): Either[String, ObjectId] = {
      try {
        Right(new ObjectId(value))
      } catch {
        case e: IllegalArgumentException => Left("Id must be an ObjectId")
      }
    }
    override def unbind(key: String, value: ObjectId): String = value.toString
  }

  implicit def objectIdVectorPathBinder: QueryStringBindable[Vector[ObjectId]] = new QueryStringBindable[Vector[ObjectId]] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Vector[ObjectId]]] = {
      try {
        params.get(key) match {
          case Some(x) => Some(Right(x.head.split(",").toVector.map(new ObjectId(_))))
          case None => None
        }
      } catch {
        case e: IllegalArgumentException => Some(Left("Id must be an ObjectId"))
      }
    }
    override def unbind(key: String, value: Vector[ObjectId]): String = key + "=" + value.mkString(",")
  }
}
