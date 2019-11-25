package me.a4lick.beam.options

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.{Default, Description, Validation}

trait ColorEsToBqOptions
  extends DataflowPipelineOptions
  with DataflowJobOptions
  with BigQueryTableOptions
  with PubsubTopicAndSubscriptionOptions
  with ElasticsearchOptions {

  @Description("File Input")
  def getFileInput: String
  def setFileInput(topic: String)

  @Description("API Host")
  def getApiHost: String
  def setApiHost(topic: String)

  @Description("API Port")
  def getApiPort: Int
  def setApiPort(topic: Int)

  @Description("Date Start")
  @Validation.Required
  @Default.String("2017-08-15 00:00:00")
  def getDateStart: String
  def setDateStart(value: String)

  @Description("Date End")
  @Validation.Required
  @Default.String("2017-08-16 00:00:00")
  def getDateEnd: String
  def setDateEnd(value: String)
}
