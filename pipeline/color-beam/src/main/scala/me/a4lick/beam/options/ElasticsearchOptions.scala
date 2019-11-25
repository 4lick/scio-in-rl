package me.a4lick.beam.options

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.{Description, Validation}

trait ElasticsearchOptions extends DataflowPipelineOptions {

  @Description("Elasticsearch Address")
  @Validation.Required
  def getElasticsearchAddress: String
  def setElasticsearchAddress(host: String)

  @Description("Elasticsearch Index Prefix")
  @Validation.Required
  def getElasticsearchIndexPrefix: String
  def setElasticsearchIndexPrefix(prefix: String)

  @Description("Elasticsearch Document Type")
  @Validation.Required
  def getElasticsearchDocumentType: String
  def setElasticsearchDocumentType(documentType: String)
}