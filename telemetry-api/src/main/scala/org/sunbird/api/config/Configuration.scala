package org.sunbird.api.config

import com.typesafe.config.ConfigFactory

object Configuration {

  private val config = ConfigFactory.load()
  val host: String = config.getString("telemetry.service.host")
  val port: Int = config.getInt("telemetry.service.port")
  val requestTimeout: Int = config.getInt("telemetry.service.request.timeout")

}
