package controllers

import play.api.mvc.{Action, Controller}
import java.util.UUID
import model.Hit
import com.amazonaws.services.kinesis.AmazonKinesisClient
import play.api.Play
import com.amazonaws.services.kinesis.model.PutRecordRequest

object Collector extends Controller {
  val kinesis = new AmazonKinesisClient
  val streamName = Play.current.configuration.getString("kinesis.stream").get

  def collect = Action { request =>
    val referer = request.headers.get("Referer").getOrElse("-")
    val ua = request.headers.get("User-Agent").getOrElse("-")
    val ip = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress)
    val sid = request.session.get("sid").getOrElse {
      UUID.randomUUID().toString
    }

    val hit = Hit(referer, ua, ip, sid)

    kinesis.putRecord(new PutRecordRequest()
      .withStreamName(streamName)
      .withPartitionKey(sid)
      .withData(hit.marshall))

    val response = Ok(";").as("application/javascript")
    if (request.session.isEmpty) {
      response.withSession("sid" -> sid)
    } else {
      response
    }
  }
}
