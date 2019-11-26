package me.a4lick.beam.jobs

import java.util.UUID

import com.spotify.scio.ScioContext
import com.spotify.scio.bigquery.types.BigQueryType
import com.spotify.scio.transforms.DoFnWithResource.ResourceType
import com.spotify.scio.transforms.ScalaAsyncDoFn
import com.spotify.scio.util.MultiJoin
import com.spotify.scio.values.WindowedValue
import me.a4lick.beam.api.ColorApi
import me.a4lick.beam.io.ElasticsearchIO
import me.a4lick.beam.models.ColorEvent.ColorEvent
import me.a4lick.beam.options.ColorEsToBqOptions
import me.a4lick.beam.utils.ISO8601DateTimeFormatter
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition
import org.apache.beam.sdk.transforms.ParDo
import org.joda.time.{Duration, Instant}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Format, JsValue, Json}

import scala.concurrent.Future
import scala.language.higherKinds

object ColorEventEsToBq {

//  @BigQueryType.toTable
//  case class ColorEvent(event_id: String,
//                        event_timestamp: Instant,
//                        event_processed_at: Instant,
//                        name: String,
//                        email: String,
//                        country: String,
//                        color: String,
//                        source: String,
//                        created: Instant,
//                        user_id: Int)

  implicit val dateFormat: Format[Instant] = ISO8601DateTimeFormatter.formatInstant

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def main(cmdlineArgs: Array[String]): Unit = {
    val (opts, _) = ScioContext.parseArguments[ColorEsToBqOptions](cmdlineArgs)
    val host = opts.getApiHost
    val port = opts.getApiPort

    log.debug("API : http://{host}:${port}")

    val bqTable = s"${opts.getBigQueryProjectId}:${opts.getBigQueryDataset}.${opts.getBigQueryTable}"

//         |    "query": {
//         |        "range" : {
//         |            "created" : {
//         |                "gte": "${opts.getDateStart}",
//         |                "lte": "${opts.getDateEnd}",
//         |                "format": "yyyy-MM-dd_HH:mm:ss"
//         |            }
//         |        }
//         |    }

    val esQuery =
      s"""
         |{
         |  "_source": {
         |    "includes": [
         |      "color",
         |      "created",
         |      "source",
         |      "user_id"
         |    ]
         |  },
         |  "query": {
         |    "bool": {
         |      "must": [
         |        {
         |          "match_all": {}
         |        }
         |      ]
         |    }
         |  }
         |}
       """.stripMargin

    val connectionConfiguration: ElasticsearchIO.ConnectionConfiguration =
      ElasticsearchIO.ConnectionConfiguration
        .create(Array(s"${opts.getElasticsearchAddress}"),
          s"${opts.getElasticsearchIndexPrefix}",
          s"${opts.getElasticsearchDocumentType}")

    // initialize input
    val sc = ScioContext(opts)

    val input = sc.customInput("Read From ES",
      ElasticsearchIO
        .read()
        .withConnectionConfiguration(connectionConfiguration) //.withConnectionConfiguration(ElasticsearchV5IO.ConnectionConfiguration.create(Array("http://host:9200"), "color", "event"))
        .withQuery(esQuery))

    val eventFrom = input
      .withName("Parse Json")
      .withFixedWindows(Duration.standardSeconds(1))
      .toWindowed
      .map(parseJsonStep)

    val userInfo = eventFrom
    .withName("Extract UserId")
      .map(toUserId)
      .withName("Get UserInfo")
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

    val toColorEvent = (t: (Int, (Iterable[(String, String, String)], Iterable[(String, String, Instant)]))) => {

        val userId = t._1
        val user = t._2._1.toSet.head
        val events = t._2._2.toSet

        events.map(e => {
          val now = new Instant(System.currentTimeMillis)

          ColorEvent(event_id = buildEventId(),
            event_timestamp = now,
            event_processed_at = now,
            event_source = "BATCH_ES",
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
      .flatMap(toColorEvent)
      .withName("Write TO BQ")
      .saveAsTypedBigQuery(bqTable, WriteDisposition.WRITE_APPEND)

    sc.run()
  }

  private def getUserInfo(host: String, port: Integer): ParDo.SingleOutput[String, JsValue] = {
    ParDo.of(new UserDoFn(host, port))
  }

  private def parseJsonStep(wv: WindowedValue[String]): WindowedValue[JsValue] = {
    if (log.isInfoEnabled) log.info("Received message: {}", wv.value)
    wv.copy(value = Json.parse(wv.value))
  }

  private def toUserId(wv: WindowedValue[JsValue]): WindowedValue[String] = {
    val json = wv.value
    val userId = (json \ "user_id").as[Int].toString
    wv.copy(value = userId)
  }

  def buildEventId(): String = UUID.randomUUID().toString.replace("-", "")

  class UserDoFn(val host: String, val port: Int) extends ScalaAsyncDoFn[String, JsValue, ColorApi] {
    override def getResourceType: ResourceType = ResourceType.PER_CLASS
    override def createResource(): ColorApi = new ColorApi(host, port)
    override def processElement(input: String): Future[JsValue] = getResource.requestUser(input)
  }
}
