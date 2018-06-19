package org.sunbird.api.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import org.sunbird.api.config.Configuration
import org.sunbird.telemetry.sink.KafkaProducer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object TelemetryService extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatchers.lookup("rest-dispatcher")

  private val kafkaProducerActor =
    system.actorOf(
      props = FromConfig.getInstance.props(KafkaProducer.props(materializer)),
      name = "kafkaProducerActor")

  val telemetryService = new TelemetryEndPoints(kafkaProducerActor)

  val bindingFuture = Http().bindAndHandle(telemetryService.telemetryServiceRoutes, interface = Configuration.host, port = Configuration.port)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"Webserver is listening on localhost:8080")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = {
      system.terminate()
    }
  })

}
