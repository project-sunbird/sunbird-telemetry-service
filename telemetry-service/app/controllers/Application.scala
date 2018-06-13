package controllers

import play.api.mvc._
import play.api.libs.json._
import akka.actor._
import akka.dispatch.Futures

import scala.concurrent.Future
import javax.inject._
import akka.pattern._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.sunbird.api.{APIIds, APIStatus, JSONUtils, Params, Request, Response}
import org.sunbird.api.ResponseCode._
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
	    /*
        val body = JSONUtils.deserialize[Request](bodyStr);
		val events = body.events;
		if (events.isEmpty || events.getOrElse(Array()).length == 0) {
			val response = Response(APIIds.TELEMETRY_API, API_VERSION, "", Params("","",CLIENT_ERROR.toString, APIStatus.FAILED ,"Please include events in the request."), CLIENT_ERROR.toString(), None);
			Futures.successful(play.api.mvc.Results.BadRequest(JSONUtils.serialize(response)).withHeaders(CONTENT_TYPE -> "application/json"));
		} else {
			val result = ask(telemetryServiceActor, TelemetryRequest(did, channel, appId, body.events.get, config)).mapTo[Response];
			result.map( { x =>
				x.responseCode match {
					case "OK" => Ok(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
					case "SERVER_ERROR" => InternalServerError(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
				}
			});
		}*/
	    val result = ask(telemetryServiceActor, TelemetryRequestPassthrough(bodyStr, config)).mapTo[Response];
		result.map( { x =>
			x.responseCode match {
				case "OK" => Ok(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
				case "SERVER_ERROR" => InternalServerError(JSONUtils.serialize(x)).withHeaders(CONTENT_TYPE -> "application/json")
			}
		});
	}
}