package controllers

import play.api.mvc._
import play.api.libs.json._
import akka.actor._

import scala.concurrent.Future
import javax.inject._
import akka.pattern._
import akka.actor.Props
import akka.dispatch.Futures
import akka.routing.FromConfig
import javax.xml.ws.RequestWrapper
import org.sunbird.actor.AppActors
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.sunbird.api.{APIIds, APIStatus, JSONUtils, Params, Request, Response}
import org.sunbird.api.ResponseCode._
import org.sunbird.api.service.TelemetryService
import org.sunbird.api.service.TelemetryService._

/**
 * @author mahesh
 */

class Application @Inject() (system: ActorSystem) extends BaseController {
	implicit val className = "controllers.Application";

	def checkAPIhealth() = Action.async { implicit request =>
    	    val result = Future { """{"result": "successful"}""" };
		result.map(x => Ok(x).withHeaders(CONTENT_TYPE -> "application/json"))
	}
	
	def telemetry() = Action.async { implicit request =>
		val startTime = System.currentTimeMillis();
		val channel = request.headers.get("X-Channel-Id").getOrElse("")
		val appId = request.headers.get("X-App-Id").getOrElse("")
		val did = request.headers.get("X-Device-Id").getOrElse("")
		val bodyStr: String = Json.stringify(request.body.asJson.get);

		val body = JSONUtils.deserialize[Request](bodyStr);
		val events = body.events;
		if (events.isEmpty || events.getOrElse(Array()).length == 0) {
			val response = Response(APIIds.TELEMETRY_API, API_VERSION, "", Params("", "", CLIENT_ERROR.toString, APIStatus.FAILED, "Please include events in the request."), CLIENT_ERROR.toString(), None);
			Futures.successful(play.api.mvc.Results.BadRequest(JSONUtils.serialize(response)).withHeaders(CONTENT_TYPE -> "application/json"));
		} else {
			val result = ask(AppActors.getActor("telemetryActor"), TelemetryRequest(did, channel, appId, body.events.get, config)).mapTo[Response];
			result.map({ x =>
				x.responseCode match {
					case "OK" => Ok(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
					case "SERVER_ERROR" => InternalServerError(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
				}
			});
		}
	}
}