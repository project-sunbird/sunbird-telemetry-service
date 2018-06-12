package controllers

import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import javax.inject.Singleton
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.pattern._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor.Props
import akka.routing.FromConfig
import org.sunbird.api.{APIIds, Request}

/**
 * @author mahesh
 */

@Singleton
class Application @Inject() (system: ActorSystem) extends BaseController {
	implicit val className = "controllers.Application";

	def checkAPIhealth() = Action.async { implicit request =>
    	val result = Future { """{"result": "successful"}""" };
			result.map(x => Ok(x).withHeaders(CONTENT_TYPE -> "application/json"))
	}
}