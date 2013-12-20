package analytics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala._
import play.Logger

object Pageviews {
  private val perSecondLastMinute = new PageviewHistogram(1000, 60)
  private val perMinuteLastHour = new PageviewHistogram(1000 * 60, 60)
  private val perHourLastDay = new PageviewHistogram(1000 * 60 * 60, 24)

  def count(timestamp: Long) {
    Logger.info("Counting {}", String.valueOf(timestamp))
    perSecondLastMinute.count(timestamp)
    perMinuteLastHour.count(timestamp)
    perHourLastDay.count(timestamp)
  }
  def getPerSecondLastMinute = perSecondLastMinute.histo()
  def getPerMinuteLastHour = perMinuteLastHour.histo()
  def getPerHourLastDay = perHourLastDay.histo()
}

class PageviewHistogram(private val factorFromMs: Long, private val keep: Int) {
  private val histogram = new ConcurrentHashMap[Long, AtomicInteger]()

  def count(timestamp: Long) {
    val date = timestamp / factorFromMs
    val now = System.currentTimeMillis() / factorFromMs

    if (now - date <= keep) {
      val i = histogram.putIfAbsent(date, new AtomicInteger(1))
      if (i != null) {
        i.incrementAndGet()
      }
    }

    for (old: Long <- histogram.keySet()) {
      if (now - old > keep) {
        histogram.remove(old)
      }
    }
  }

  def histo() = {
    val now = System.currentTimeMillis() / factorFromMs
    ((now - keep) to now).map(x => {
      Option(histogram.get(x)).map(_.get()).getOrElse(0)
    })
  }
}
