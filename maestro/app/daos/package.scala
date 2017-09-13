import org.bson.types.ObjectId
import play.api.libs.json.Json
import slick.jdbc.GetResult

package object daos {
  implicit val getJsonResult = GetResult( r => Json.parse(r.nextString))
  implicit val getObjectIdResult = GetResult( r => new ObjectId(r.nextString))
}
