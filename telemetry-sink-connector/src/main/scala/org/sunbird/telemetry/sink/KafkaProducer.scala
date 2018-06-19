package org.sunbird.telemetry.sink

import java.util.UUID

import akka.Done
import akka.actor.{Actor, Props}
import akka.kafka.ProducerMessage.MultiResultPart
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory
import org.sunbird.model._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class KafkaProducer(implicit val mat: ActorMaterializer) extends Actor {

  implicit val ec: ExecutionContext = context.dispatcher
  private val producerSettings = ProducerSettings(context.system, new StringSerializer, new StringSerializer)

  implicit val formats: DefaultFormats = DefaultFormats
  implicit val timeout: Timeout = Timeout(60 seconds)

  override def receive: Receive = {
    case msg: Request => {
      val result = sendMsgToKafka(msg.events.getOrElse(Array()))(mat)
      result pipeTo sender
    }
  }

  def sendMsgToKafka(events: Array[Map[String, Any]])(implicit mat: ActorMaterializer): Future[Done] = {

    Source.single(
      ProducerMessage.Message(new ProducerRecord[String, String]("telemetry", 0,
        UUID.randomUUID().toString, write(events)), 100)
    ).via(Producer.flexiFlow(producerSettings))
      .map {
        case ProducerMessage.Result(metadata, message) =>
          val record = message.record
          s"${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"
        case ProducerMessage.MultiResult(parts, passThrough) =>
          parts
            .map {
              case MultiResultPart(metadata, record) =>
                s"${metadata.topic}/${metadata.partition} ${metadata.offset}: ${record.value}"
            }.mkString(", ")
        case ProducerMessage.PassThroughResult(passThrough) =>
          KafkaProducer.logger.info(s"Message passed through")
          s"passed through"
      }
      .runWith(Sink.ignore)
  }

}

object KafkaProducer {
  private val logger = LoggerFactory.getLogger(classOf[KafkaProducer])
  def props(implicit mat: ActorMaterializer): Props = Props(new KafkaProducer()(mat)).withDispatcher("telemetry-kafka-dispatcher")
}

