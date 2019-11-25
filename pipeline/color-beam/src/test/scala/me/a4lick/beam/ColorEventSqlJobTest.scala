package me.a4lick.beam

import com.spotify.scio.bigquery.BigQueryIO
import com.spotify.scio.io.TextIO
import com.spotify.scio.testing.PipelineSpec
import me.a4lick.beam.jobs.ColorEventSqlToBq
import me.a4lick.beam.models.ColorEvent.ColorEvent
import me.a4lick.beam.services.DataflowSteps.getUUID
import me.a4lick.beam.utils.ISO8601DateTimeFormatter
import org.joda.time.Instant

final class ColorEventSqlJobTest extends PipelineSpec {

  val now = new Instant(System.currentTimeMillis)

  val input = Seq(
    """{"color":"indigo","source":"site","created":"2017-07-19T10:57:28-07:00","user_id":1046}""",
    """{"color":"yellow","source":"mobile","created":"2017-12-21T03:01:24-08:00","user_id":1035}""",
    """{"color":"green","source":"site","created":"2017-03-24T05:02:22-07:00","user_id":1098}"""
  )

  val expected = Seq(
    ColorEvent(event_id = getUUID,
      event_timestamp = now,
      event_processed_at = now,
      name = "Tasha",
      email = "tristique.neque.venenatis@egetmetusIn.org",
      country = "Latvia",
      color = "indigo",
      source = "site",
      created =  ISO8601DateTimeFormatter.parseInstant("2017-07-19T17:57:28.000"),
      user_id = 1046),
    ColorEvent(event_id = getUUID,
      event_timestamp = now,
      event_processed_at = now,
      name = "Vivien",
      email = "nec.tellus@dictum.ca",
      country = "Bouvet Island",
      color = "indigo",
      source = "mobile",
      created =  ISO8601DateTimeFormatter.parseInstant("2017-07-19T17:57:28.000"),
      user_id = 1035),
    ColorEvent(event_id = getUUID,
      event_timestamp = now,
      event_processed_at = now,
      name = "Nero",
      email = "Curabitur.dictum@lacus.net",
      country = "South Sudan",
      color = "green",
      source = "site",
      created =  ISO8601DateTimeFormatter.parseInstant("2017-05-02T12:28:03-07:00"),
      user_id = 1098)
  )

  "From GCS to BQ with SQL" should "work..." in {
    JobTest[ColorEventSqlToBq.type]
      .args("--fileInput=events.json",
        "--apiHost=http://localhost",
        "--apiPort=3000",
        "--bigQueryProjectId=test",
        "--bigQueryDataset=TEST",
        "--bigQueryTable=color_event")
      .input(TextIO("events.json"), input)
      .output(BigQueryIO[ColorEvent]("test:TEST.color_event")) { coll =>
        coll.map(println(_))
        coll.map(_.email) should containInAnyOrder[String](expected.map(_.email))
      }
      .run()
  }
}
