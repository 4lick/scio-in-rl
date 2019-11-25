package me.a4lick.beam.api

import me.a4lick.beam.client.StandaloneHttpClient
import play.api.libs.json.JsValue

import scala.concurrent.Future

class ColorApi(host: String, port: Int) extends StandaloneHttpClient(host) {
    def requestUser(id: String): Future[JsValue] = sendAndReceiveAsJsValue(s"/users/$id", port)
}