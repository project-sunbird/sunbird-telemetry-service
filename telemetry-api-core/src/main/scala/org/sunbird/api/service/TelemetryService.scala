package org.sunbird.api.service

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.actorRef2Scala
import javax.inject._
import play.api.Configuration
import java.util.HashMap
import org.apache.kafka.clients.producer.ProducerConfig
import java.lang.Long
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.sunbird.api.JSONUtils
import java.util.UUID
import java.util.concurrent.Future
import org.apache.kafka.clients.producer.RecordMetadata
import org.sunbird.api.ResponseCode._
import org.sunbird.api.ResponseCode
import org.sunbird.api.Response
import org.sunbird.api.Params

object TelemetryService {

    case class TelemetryRequest(did: String, channel: String, appId: String, body: Array[Map[String, AnyRef]], config: Config)
    case class TelemetryParams(did: String, channel: String, appId: String, msgid: String);
    case class TelemetryBatch(events: Array[Map[String, AnyRef]], params: TelemetryParams, id: Option[String] = Option("api.telemetry"), ver: Option[String] = Option("3.0"));
}

class TelemetryService @Inject() (configuration: Configuration) extends Actor {
    import TelemetryService._;

    val bootstrapServers = configuration.getString("kafka.brokerList").get;
    val topic = configuration.getString("kafka.topic").get;
    Console.println("bootstrapServers", bootstrapServers, "topic", topic);

    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L.asInstanceOf[Long]);
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")

    lazy val producer = new KafkaProducer[String, String](props);

    def receiveTelemetry(did: String, channel: String, appId: String, events: Array[Map[String, AnyRef]])(implicit config: Config): Response = {
        val recordId = UUID.randomUUID().toString();
        val event = TelemetryBatch(events, TelemetryParams(did, channel, appId, recordId))
        try {
            producer.send(new ProducerRecord[String, String](topic, recordId, JSONUtils.serialize(event))).get
            Response("sunbird.telemetry", "1.0", "", Params("","","","",""), OK.toString(), None);
        } catch {
            case ex: Exception =>
                ex.printStackTrace();
                Response("sunbird.telemetry", "1.0", "", Params("","","","",""), SERVER_ERROR.toString(), None);
        }

    }

    def receive = {
        case TelemetryRequest(did: String, channel: String, appId: String, events: Array[Map[String, AnyRef]], config: Config) => sender() ! receiveTelemetry(did, channel, appId, events)(config);
    }
}