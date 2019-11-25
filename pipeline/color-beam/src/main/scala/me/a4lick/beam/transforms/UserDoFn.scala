package me.a4lick.beam.transforms

import com.spotify.scio.transforms.DoFnWithResource.ResourceType
import com.spotify.scio.transforms.ScalaAsyncDoFn
import me.a4lick.beam.api.ColorApi
import play.api.libs.json.JsValue

import scala.concurrent.Future

class UserDoFn(val host: String, val port: Int) extends ScalaAsyncDoFn[String, JsValue, ColorApi] {
  override def getResourceType: ResourceType = ResourceType.PER_CLASS
  override def createResource(): ColorApi = new ColorApi(host, port)
  override def processElement(input: String): Future[JsValue] = getResource.requestUser(input)
}
