package controllers

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.duration.DurationInt

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import play.api.mvc.Controller

/**
 * @author mahesh
 */

abstract class BaseController extends Controller {
	implicit val timeout: Timeout = 20 seconds;
	implicit val config: Config = play.Play.application.configuration.underlying();


}