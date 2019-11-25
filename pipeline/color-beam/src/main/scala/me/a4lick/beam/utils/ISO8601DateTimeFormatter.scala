package me.a4lick.beam.utils

import org.joda.time.format.DateTimeFormatterBuilder

import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone, Instant}
import play.api.libs.json._

object ISO8601DateTimeFormatter {

  val formatInstant: Format[Instant] = new Format[Instant] {

    def reads(json: JsValue): JsResult[Instant] = json match {
      case JsString(date) => JsSuccess(parseInstant(date))
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date"))))
    }

    def writes(o: Instant): JsValue = JsString(formatInstant(o))
  }

  private val dateTimeFormatter = new DateTimeFormatterBuilder()
    .append(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss"))
    .appendOptional(new DateTimeFormatterBuilder()
      .appendLiteral('.')
      .append(DateTimeFormat.forPattern("SSS")).toParser)
    .appendOptional(new DateTimeFormatterBuilder()
      .append(DateTimeFormat.forPattern("Z")).toParser)
    .appendOptional(new DateTimeFormatterBuilder()
      .appendLiteral(' ')
      .append(DateTimeFormat.forPattern("ZZZ")).toParser).toFormatter

  def formatInstant(date: Instant): String = dateTimeFormatter.withZoneUTC.print(date)

  def parseInstant(date: String): Instant = dateTimeFormatter.withZone(DateTimeZone.UTC).parseDateTime(date).withZone(DateTimeZone.getDefault).toInstant
}
