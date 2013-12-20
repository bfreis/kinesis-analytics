package controllers

import play.api.mvc._
import analytics.Pageviews
import play.api.libs.Jsonp
import play.api.libs.json.Json

object Dashboard extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def jsonp(callback: String, result: => Seq[Int]) = {
    val json = Json.toJson(result)
    Action {
      if (callback == null) {
        Ok(json)
      } else {
        Ok(Jsonp(callback, json))
      }
    }
  }

  def minute(callback: String) = jsonp(callback, Pageviews.getPerSecondLastMinute)
  def hour(callback: String) = jsonp(callback, Pageviews.getPerMinuteLastHour)
  def day(callback: String) = jsonp(callback, Pageviews.getPerHourLastDay)
}
