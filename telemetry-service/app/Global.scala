import akka.actor.Props
import akka.routing.{FromConfig}
import play.api._
import play.api.mvc._

import com.typesafe.config.Config
import org.sunbird.actor.AppActors
import org.sunbird.api.service.TelemetryService

object Global extends WithFilters() {

    override def beforeStart(app: Application) {
        val config: Config = play.Play.application.configuration.underlying()
        val telemetryActor = app.actorSystem.actorOf(Props[TelemetryService].withRouter(FromConfig).withDispatcher("telemetry-dispatcher"), name = "telemetryService")
        AppActors.setActors(Map("telemetryActor" -> telemetryActor));
        Logger.info("Application has started...")
    }


    override def onStop(app: Application) {
        Logger.info("Application shutdown...")
    }

}