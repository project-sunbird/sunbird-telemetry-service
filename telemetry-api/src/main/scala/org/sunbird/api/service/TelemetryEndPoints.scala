package org.sunbird.api.service

import akka.Done
import akka.actor.ActorRef
import akka.http.scaladsl.server
import akka.stream.Materializer
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.{DefaultFormats, Serialization}
import org.slf4j.LoggerFactory
import org.sunbird.api.config.Configuration
import org.sunbird.model._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait JsonSupport extends Json4sSupport {
  implicit val serialization: Serialization = org.json4s.jackson.Serialization
  implicit val json4sFormats: DefaultFormats = org.json4s.DefaultFormats
}

class TelemetryEndPoints(kafkaProducer: ActorRef)(implicit val ec: ExecutionContext, mat: Materializer) extends JsonSupport {

  implicit val timeout: Timeout = Timeout(Configuration.requestTimeout.milliseconds)
  private val logger = LoggerFactory.getLogger(classOf[TelemetryEndPoints])

  val telemetryServiceRoutes: server.Route =
    pathPrefix("v1") {
      path("telemetry") {
        post {
          entity(as[Request]) { event =>
            complete {
              if (event.events.getOrElse(Array()).length != 0) {
                val result = (kafkaProducer ? event).mapTo[Done]
                result.map {
                  res => {
                    Response(id = APIIds.TELEMETRY_API,
                      params = Params("", APIStatus.SUCCESSFUL, ""),
                      responseCode = ResponseCode.OK.toString,
                      result = None)
                  }
                }.recoverWith {
                  case ex: Exception => Future {
                    logger.error("Exception occurred when publishing the message to Kafka", ex)
                    Response(id = APIIds.TELEMETRY_API,
                      params = Params(ResponseCode.SERVER_ERROR.toString, APIStatus.FAILED, "Exception occurred when publishing the message to Kafka"),
                      responseCode = ResponseCode.SERVER_ERROR.toString,
                      result = None)
                  }
                }
              } else {
                logger.error("Events not included in the envelope")
                Response(id = APIIds.TELEMETRY_API,
                  params = Params(ResponseCode.CLIENT_ERROR.toString, APIStatus.FAILED, "Please include events in the request."),
                  responseCode = ResponseCode.CLIENT_ERROR.toString,
                  result = None)
              }
            }
          }
        }
      }
    }
}

