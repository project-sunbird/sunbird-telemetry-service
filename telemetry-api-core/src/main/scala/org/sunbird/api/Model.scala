package org.sunbird.api

object Model {

}


case class Request(id: String, ver: String, ets: Long, events: Array[Map[String, AnyRef]]);
case class APIRequest(request: Request);

case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: String, client_key: Option[String] = None);
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]);



object ResponseCode extends Enumeration {
  type Code = Value
  val OK, CLIENT_ERROR, SERVER_ERROR, REQUEST_TIMEOUT, RESOURCE_NOT_FOUND = Value
}

object APIIds {
  val DEFINITION_SAVE = "org.sunbird.definition.save"
  val DEFINITION_READ = "org.sunbird.definition.read"
  val DATANODE_CREATE = "org.sunbird.data.create"
  val DATANODE_READ = "org.sunbird.data.read"
}