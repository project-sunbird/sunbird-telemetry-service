package org.sunbird.model

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

object Model {}

case class Request(events: Option[Array[Map[String, Any]]])
case class Params(err: String, status: String, errmsg: String, client_key: Option[String] = None, resmsgid: String = "", msgid: String = "")
case class Response(id: String, ver: String = "1.0", ts: String = System.currentTimeMillis().toString, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]) {
  implicit val formats: DefaultFormats = DefaultFormats
  override def toString: String = write(this)
}

object ResponseCode extends Enumeration {
  type Code = Value
  val OK, CLIENT_ERROR, SERVER_ERROR, REQUEST_TIMEOUT, RESOURCE_NOT_FOUND = Value
}

object APIIds {
  val TELEMETRY_API = "sunbird.telemetry"
}

object APIStatus {
  val SUCCESSFUL = "successful"
  val FAILED = "failed"
  val WARNING = "warning"
}

