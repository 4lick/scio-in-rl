package me.a4lick.beam.jobs

import java.util.UUID

import com.spotify.scio.ScioContext
import me.a4lick.beam.options.ColorFileToBqOptions
import me.a4lick.beam.services.DataflowSteps._
import me.a4lick.beam.utils.ISO8601DateTimeFormatter
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition
import org.joda.time.{Duration, Instant}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._
import com.spotify.scio.sql._
import me.a4lick.beam.models.ColorEvent.ColorEvent

import scala.language.higherKinds

object ColorEventSqlToBq {

  implicit val dateFormat: Format[Instant] = ISO8601DateTimeFormatter.formatInstant

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  case class User(id: Int, name: String, email: String, country: String)

  case class Event(userId: Int, color: String, source: String, created: Instant)

  def main(cmdlineArgs: Array[String]): Unit = {
    val (opts, _) = ScioContext.parseArguments[ColorFileToBqOptions](cmdlineArgs)

    val input = opts.getFileInput
    val host = opts.getApiHost
    val port = opts.getApiPort

    log.debug("API : {host}:${port}")

    val bqTable = s"${opts.getBigQueryProjectId}:${opts.getBigQueryDataset}.${opts.getBigQueryTable}"

    val sc = ScioContext(opts)

    val eventFrom = sc.withName("Read From Extract File")
      .textFile(input)
      .withName("Parse Json")
      .withFixedWindows(Duration.standardSeconds(1))
      .toWindowed
      .map(parseJson)

    val u = eventFrom
    .withName("Extract UserId")
      .map(toUserId)
      .withName("Get User Info")
      .toSCollection
      .applyTransform(getUserInfo(host, port))
      .map(json => {
        val userId = (json \ "id").as[Int]
        val name = (json \ "name").as[String]
        val email = (json \ "email").as[String]
        val country = (json \ "country").as[String]

        User(userId, name, email, country)
      })

    val e = eventFrom
      .toSCollection
      .map(json => {
        val userId = (json \ "user_id").as[Int]
        val color = (json \ "color").as[String]
        val source = (json \ "source").as[String]
        val created = (json \ "created").as[Instant]

        Event(userId, color, source, created)
      })

    val result = tsql"SELECT u.*, e.color, e.source, e.created FROM $u INNER JOIN $e ON $u.id = $e.userId"
      .as[(Int, String, String, String, String, String, Instant)]

    result.map(e => {
      val now = new Instant(System.currentTimeMillis)
      val id = UUID.randomUUID().toString.replace("-", "")

      ColorEvent(event_id = id,
        event_timestamp = now,
        event_processed_at = now,
        event_source = "BATCH_SQL",
        user_id = e._1,
        name = e._2,
        email = e._3,
        country = e._4,
        color = e._5,
        source = e._6,
        created = e._7)
    }).saveAsTypedBigQuery(bqTable, WriteDisposition.WRITE_APPEND)

    sc.run()
  }
}
