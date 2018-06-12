package controllers

import play.api.mvc._
import play.api.libs.json._

import akka.actor._
import scala.concurrent.Future
import javax.inject._
import akka.actor.ActorSystem
import akka.pattern._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor.Props
import akka.routing.FromConfig
import org.sunbird.api.{APIIds, APIRequest, JSONUtils, ResponseCode, Response}
import org.sunbird.api.ResponseCode._
import org.sunbird.api.service.TelemetryService
import org.sunbird.api.service.TelemetryService._

/**
 * @author mahesh
 */

@Singleton
class Application @Inject() (@Named("telemetry-actor") telemetryServiceActor: ActorRef) extends BaseController {
	implicit val className = "controllers.Application";

	def checkAPIhealth() = Action.async { implicit request =>
    	    val result = Future { """{"result": "successful"}""" };
		result.map(x => Ok(x).withHeaders(CONTENT_TYPE -> "application/json"))
	}
	
	def telemetry() = Action.async { implicit request =>
	    val channel = request.headers.get("X-Channel-Id").getOrElse("")
        val appId = request.headers.get("X-App-Id").getOrElse("")
        val did = request.headers.get("X-Device-Id").getOrElse("")
	    val bodyStr: String = Json.stringify(request.body.asJson.get);
        val body = JSONUtils.deserialize[APIRequest](bodyStr);
	    val result = ask(telemetryServiceActor, TelemetryRequest(did, channel, appId, body.request.events, config)).mapTo[Response];
		result.map( { x =>
		    x.responseCode match {
		        case "OK" => Ok(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
		        case "SERVER_ERROR" => InternalServerError(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
		    }
		})
	}
}