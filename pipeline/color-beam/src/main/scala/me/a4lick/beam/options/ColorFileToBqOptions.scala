package me.a4lick.beam.options

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.Description

trait ColorFileToBqOptions
  extends DataflowPipelineOptions
  with DataflowJobOptions
  with BigQueryTableOptions {

  @Description("File Input")
  def getFileInput: String
  def setFileInput(topic: String)

  @Description("API Host")
  def getApiHost: String
  def setApiHost(topic: String)

  @Description("API Port")
  def getApiPort: Int
  def setApiPort(topic: Int)
}
