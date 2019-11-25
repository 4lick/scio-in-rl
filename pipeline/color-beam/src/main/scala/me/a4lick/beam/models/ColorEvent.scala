package me.a4lick.beam.models

import com.spotify.scio.bigquery.types.BigQueryType
import org.joda.time.Instant

object ColorEvent {

  @BigQueryType.toTable
  case class ColorEvent(event_id: String,
      event_timestamp: Instant,
      event_processed_at: Instant,
      name: String,
      email: String,
      country: String,
      color: String,
      source: String,
      created: Instant,
      user_id: Int)
}
