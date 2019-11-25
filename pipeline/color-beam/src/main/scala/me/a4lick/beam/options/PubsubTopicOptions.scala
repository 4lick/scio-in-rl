package me.a4lick.beam.options

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.{Default, Description}

trait PubsubTopicOptions extends DataflowPipelineOptions {

  @Description("Pub/Sub topic")
  def getPubsubTopic: String
  def setPubsubTopic(topic: String)

  @Description("Delete topic at shutdown")
  @Default.Boolean(true)
  def getDeleteTopicAtShutdown: Boolean
  def setDeleteTopicAtShutdown(delete: Boolean)
}

