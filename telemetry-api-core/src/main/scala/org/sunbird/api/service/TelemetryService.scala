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
import org.sunbird.api._
import java.util.UUID

import org.apache.kafka.clients.producer.RecordMetadata
import org.sunbird.api.ResponseCode._

import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.kafka.clients.producer.Callback

import scala.concurrent.Promise
import akka.dispatch.Futures
import akka.pattern.Patterns

import scala.concurrent.Future

object TelemetryService {

    case class TelemetryRequest(did: String, channel: String, appId: String, body: Array[Map[String, AnyRef]], config: Config)
    case class TelemetryParams(did: String, channel: String, appId: String, msgid: String);
    case class TelemetryBatch(events: Array[Map[String, AnyRef]], params: TelemetryParams, id: Option[String] = Option("api.telemetry"), ver: Option[String] = Option("3.0"), ets: Option[Long] = Option(System.currentTimeMillis()));
}

class TelemetryService @Inject() (configuration: Configuration) extends Actor {
    import TelemetryService._;
    
    implicit val config = ConfigFactory.systemEnvironment().withFallback(configuration.underlying);    
    val bootstrapServers = config.getString("sunbird_telemetry_kafka_servers_config");
    val topic = config.getString("sunbird_telemetry_kafka_topic");
    Console.println("bootstrapServers", bootstrapServers, "topic", topic);

    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L.asInstanceOf[Long]);
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    //props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")

    lazy val producer = new KafkaProducer[String, String](props);

    def receiveTelemetry(did: String, channel: String, appId: String, events: Array[Map[String, AnyRef]])(implicit config: Config) = {
        val recordId = UUID.randomUUID().toString();
        val event = TelemetryBatch(events, TelemetryParams(did, channel, appId, recordId))
        val promise: Promise[Response] = Futures.promise();
        
        producer.send(new ProducerRecord[String, String](topic, recordId, JSONUtils.serialize(event)), new Callback {
            override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
                if (null != exception) {
                    promise.success(Response(APIIds.TELEMETRY_API, "1.0", "", Params(recordId,"","",APIStatus.SUCCESSFUL,""), SERVER_ERROR.toString(), None));
                } else {
                    promise.success(Response(APIIds.TELEMETRY_API, "1.0", "", Params(recordId,"","",APIStatus.SUCCESSFUL,""), OK.toString(), None));
                }
            }
        });
        Patterns.pipe(promise.future, this.context.dispatcher).to(sender())
    }

    def receive = {
        case TelemetryRequest(did: String, channel: String, appId: String, events: Array[Map[String, AnyRef]], config: Config) => receiveTelemetry(did, channel, appId, events)(config);
    }
}