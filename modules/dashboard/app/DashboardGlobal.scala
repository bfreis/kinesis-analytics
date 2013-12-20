import analytics.Pageviews
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessorFactory, IRecordProcessorCheckpointer, IRecordProcessor}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration, Worker}
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason
import com.amazonaws.services.kinesis.model.Record
import java.util
import java.util.UUID
import model.Hit
import play.api.{Application, GlobalSettings}
import play.Logger
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala._

object DashboardGlobal extends GlobalSettings {
  override def onStart(app: Application) {
    java.security.Security.setProperty("networkaddress.cache.ttl", "60")

    val applicationName = app.configuration.getString("dashboard.kinesis.app").get
    val streamName = app.configuration.getString("kinesis.stream").get

    val factory = new AnalyticsRecordProcessorFactory
    val conf = new KinesisClientLibConfiguration(
      applicationName,
      streamName,
      new DefaultAWSCredentialsProviderChain,
      "worker:" + UUID.randomUUID.toString)
      .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON)
    new Thread(new Worker(factory, conf)).start()
  }
}

class AnalyticsRecordProcessor extends IRecordProcessor {
  var shardId = ""

  def shutdown(checkpointer: IRecordProcessorCheckpointer, shutdownReason: ShutdownReason) = {
    Logger.debug("[" + shardId + "] Shutting down: " + shutdownReason + ", thread = " + Thread.currentThread().getId + ", " + Thread.currentThread().getName)
    if (shutdownReason == ShutdownReason.TERMINATE) {
      checkpointer.checkpoint()
    }
  }

  def processRecords(records: util.List[Record], checkpointer: IRecordProcessorCheckpointer) = {
    Logger.debug("[" + shardId + "] Processing: " + records.size() + ", thread = " + Thread.currentThread().getId + ", " + Thread.currentThread().getName)
    for (r: Record <- records) {
      try {
        val hit = Hit.unmarshall(r.getData)
        Pageviews.count(hit.timestamp)
      } catch {
        case e: Exception => Logger.error("Error processing " + r, e)
      }
    }
    checkpointer.checkpoint()
  }

  def initialize(p1: String) = {
    shardId = p1
    Logger.debug("[" + shardId + "] Initializing , thread = " + Thread.currentThread().getId + ", " + Thread.currentThread().getName)
  }
}

class AnalyticsRecordProcessorFactory extends IRecordProcessorFactory {
  def createProcessor = new AnalyticsRecordProcessor
}
