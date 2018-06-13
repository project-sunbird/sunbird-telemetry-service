package org.sunbird.api

object Model {

}


case class Request(id: String, ver: String, ets: Long, events: Option[Array[Map[String, AnyRef]]]);

case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: String, client_key: Option[String] = None);
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]);



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