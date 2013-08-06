
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSpec
import models.CardType
import models.Vote
import play.api.libs.json.JsValue
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsBoolean
import scala.util.parsing.json.JSONArray
import play.api.libs.json.Json



@RunWith(classOf[JUnitRunner])
class SampleTest extends FunSpec with ShouldMatchers {
  describe("JSon LIbrary") {

    it("Show the json content") {
      val x = List(Vote("hayssam@saleh.fr", CardType.coffee), Vote("hayssam@saleh.fr", CardType.eight))
      println(Json.stringify(Json.toJson(x)))
    }

    it("should throw NoSuchElementException if an empty stack is popped") {
    }
  }
}
