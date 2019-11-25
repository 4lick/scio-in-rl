package me.a4lick.beam.options

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.api.services.bigquery.model.TableSchema
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.{Default, Description}

trait BigQueryTableOptions extends DataflowPipelineOptions {

  @Description("BigQuery Project ID")
  def getBigQueryProjectId: String
  def setBigQueryProjectId(projectId: String)

  @Description("BigQuery dataset name")
  @Default.String("dataflow_examples")
  def getBigQueryDataset: String
  def setBigQueryDataset(dataset: String)

  @Description("BigQuery table name")
  def getBigQueryTable: String
  def setBigQueryTable(table: String)

  @JsonIgnore
  @Description("BigQuery table schema")
  def getBigQuerySchema: TableSchema
  def setBigQuerySchema(schema: TableSchema)
}
