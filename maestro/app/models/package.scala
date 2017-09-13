import org.bson.types.ObjectId
import play.api.libs.json.{JsPath, Reads}

package object models {
  val ObjectIdReader: Reads[ObjectId] = JsPath.read[String].map(new ObjectId(_))
}
