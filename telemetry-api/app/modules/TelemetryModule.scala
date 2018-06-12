import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import org.sunbird.api.service.TelemetryService

class TelemetryModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[TelemetryService]("telemetry-actor")
  }
}