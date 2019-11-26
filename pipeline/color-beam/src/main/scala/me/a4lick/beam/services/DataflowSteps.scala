package me.a4lick.beam.services

import java.util.UUID

import com.spotify.scio.values.WindowedValue
import me.a4lick.beam.models.ColorEvent.ColorEvent
import me.a4lick.beam.transforms.UserDoFn
import org.apache.beam.sdk.transforms.ParDo
import org.joda.time.Instant
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json}

object DataflowSteps {

  private val log = LoggerFactory.getLogger(this.getClass)

  def parseJson(wv: WindowedValue[String]): WindowedValue[JsValue] = {
    if (log.isInfoEnabled) log.info("Received message: {}", wv.value)
    wv.copy(value = Json.parse(wv.value))
  }

  def toUserId(wv: WindowedValue[JsValue]): WindowedValue[String] = {
    val json = wv.value
    val userId = (json \ "user_id").as[Int].toString
    wv.copy(value = userId)
  }

  def toColorEvent = (t: (Int, (Iterable[(String, String, String)], Iterable[(String, String, Instant)]))) => {
    val userId = t._1
    val user = t._2._1.toSet.head
    val events = t._2._2.toSet

    events.map(e => {
      val now = new Instant(System.currentTimeMillis)

      ColorEvent(event_id = getUUID,
        event_timestamp = now,
        event_processed_at = now,
        event_source = "BATCH_FILE",
        name = user._1,
        email = user._2,
        country = user._3,
        color = e._1,
        source = e._2,
        created = e._3,
        user_id = userId)
    })
  }

  def getUserInfo(host: String, port: Integer): ParDo.SingleOutput[String, JsValue] = {
    ParDo.of(new UserDoFn(host, port))
  }

  def getUUID: String = UUID.randomUUID().toString.replace("-", "")
}
