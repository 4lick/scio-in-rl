package me.a4lick.beam.jobs

import com.spotify.scio.ScioContext
import com.spotify.scio.util.MultiJoin
import me.a4lick.beam.models.ColorEvent.ColorEvent
import me.a4lick.beam.options.ColorEsToBqOptions
import me.a4lick.beam.services.DataflowSteps._
import me.a4lick.beam.utils.ISO8601DateTimeFormatter
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition
import org.joda.time.{Duration, Instant}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Format

import scala.language.higherKinds

object ColorEventStreamToBq {

  val WINDOW_SIZE = 1

  implicit val dateFormat: Format[Instant] = ISO8601DateTimeFormatter.formatInstant

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def main(cmdlineArgs: Array[String]): Unit = {
    val (opts, _) = ScioContext.parseArguments[ColorEsToBqOptions](cmdlineArgs)

    val host = opts.getApiHost
    val port = opts.getApiPort

    log.debug("API : http://{host}:${port}")

    val bqTable = s"${opts.getBigQueryProjectId}:${opts.getBigQueryDataset}.${opts.getBigQueryTable}"

    // initialize input
    val sc = ScioContext(opts)

    val input = sc
      .withName("Read From PubSub")
      .pubsubSubscription[String](opts.getPubsubSubscription)

    val eventFrom = input
      .withName("Parse Json")
      .withFixedWindows(Duration.standardSeconds(1))
      .toWindowed
      .map(parseJson)

    val userInfo = eventFrom
      .withName("Extract User Id")
      .map(toUserId)
      .withName("Get User Info")
      .toSCollection
      .applyTransform(getUserInfo(host, port))
      .map(json => {
        val userId = (json \ "id").as[Int]
        val name = (json \ "name").as[String]
        val email = (json \ "email").as[String]
        val country = (json \ "country").as[String]

        userId -> (name, email, country)
      })

    val eventInfo = eventFrom
      .toSCollection
      .map(json => {
        val userId = (json \ "user_id").as[Int]
        val color = (json \ "color").as[String]
        val source = (json \ "source").as[String]
        val created = (json \ "created").as[Instant]

        userId -> (color, source, created)
      })

    def toColorEvent = (t: (Int, (Iterable[(String, String, String)], Iterable[(String, String, Instant)]))) => {
      val userId = t._1
      val user = t._2._1.toSet.head
      val events = t._2._2.toSet

      events.map(e => {
        val now = new Instant(System.currentTimeMillis)

        ColorEvent(event_id = getUUID,
          event_timestamp = now,
          event_processed_at = now,
          event_source = "STREAM",
          name = user._1,
          email = user._2,
          country = user._3,
          color = e._1,
          source = e._2,
          created = e._3,
          user_id = userId)
      })
    }

    MultiJoin.cogroup(userInfo, eventInfo)
      .map(toColorEvent)
      .flatten
      .withName("Write To BQ")
      .saveAsTypedBigQuery(bqTable, WriteDisposition.WRITE_APPEND)

    sc.run().waitUntilFinish()
  }
}
