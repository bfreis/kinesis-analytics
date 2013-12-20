import java.util.Date
import model.Hit
import org.specs2.mutable._

class HitSpec extends Specification {
  "hit" should {
    "marshall and unmarshall" in {
      val hit = Hit("the referer", "the ua", "the ip", "the sid", System.currentTimeMillis())
      Hit.unmarshall(hit.marshall) must equalTo(hit)
    }
  }
}
