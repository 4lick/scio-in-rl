package me.a4lick.beam.options

import org.apache.beam.sdk.options.{Default, Description, PipelineOptions}

trait DataflowJobOptions extends PipelineOptions {

  @Description("Whether to keep jobs running after local process exit")
  @Default.Boolean(false)
  def getKeepJobsRunning: Boolean
  def setKeepJobsRunning(keepJobsRunning: Boolean)

  @Description("Whether to exit after the job is submitted")
  @Default.Boolean(false)
  def getExitAfterSubmit: Boolean
  def setExitAfterSubmit(exitAfterSubmit: Boolean)

  @Description("Number of workers to use when executing the injector pipeline")
  @Default.Integer(1)
  def getInjectorNumWorkers: Int
  def setInjectorNumWorkers(numWorkers: Int)

}
