package me.a4lick.beam.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsNull, JsValue}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

class StandaloneHttpClient(host: String) {
  import scala.concurrent.ExecutionContext.Implicits._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def sendAndReceiveAsJsValue(path: String, port: Int): Future[JsValue] = {

    // Create Akka system for thread and streaming management
    implicit val system: ActorSystem = ActorSystem()

    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val client = StandaloneAhcWSClient()

    val url = s"$host:$port$path"

    val response = client.url(url).get().map { response => {
        client.close()
        system.terminate()
        if (response.status == 200)
          response.body[JsValue]
        else {
          log.error(s"ID Not Found ")
          JsNull
        } // TODO : Refacto with Option
      }
    }

    response
  }

}
