package me.a4lick.beam.options

import org.apache.beam.sdk.options._

trait PubsubTopicAndSubscriptionOptions extends PubsubTopicOptions {

  @Description("Pub/Sub subscription")
  def getPubsubSubscription: String
  def setPubsubSubscription(subscription: String)

  @Description("Delete subscription at shutdown")
  @Default.Boolean(true)
  def getDeleteSubscriptionAtShutdown: Boolean
  def setDeleteSubscriptionAtShutdown(delete: Boolean)

}
