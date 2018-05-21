package org.sunbird.telemetry.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.kafka.client.KafkaClient;
import util.Constant;

/**
 * This class will responsible for writing data into kafka.
 *
 * @author mahesh
 */
@ActorConfig(
  tasks = {Constant.DISPATCH_TELEMETRY_TO_KAFKA},
  asyncTasks = {}
)
public class KafkaTelemetryDispatcher extends BaseActor {

  private static String BOOTSTRAP_SERVERS = System.getenv("sunbird_telemetry_kafka_servers_config");
  private static String topic = System.getenv("sunbird_telemetry_kafka_topic");
  private ObjectMapper mapper = new ObjectMapper();
  private static Consumer<Long, String> consumer;
  private static Producer<Long, String> producer;

  static {
    initKafkaClient();
  }

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    if (Constant.DISPATCH_TELEMETRY_TO_KAFKA.equals(operation)) {
      List<String> events = getEvents(request);
      dispatchEvents(events);
      Response response = new Response();
      response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
      sender().tell(response, self());
    } else {
      onReceiveUnsupportedMessage(operation);
    }
  }

  /**
   * This method will receive list of event. Each event will be a string object. and sent to kafka ,
   * the send method is Asynchronous, at same time user can send multiple records without blocking.
   *
   * @param events List<String>
   */
  private void dispatchEvents(List<String> events) {
    for (String event : events) {
      ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, event);
      if (producer != null) {
        producer.send(record);
      } else {
        ProjectLogger.log("Kafka producer is not initialize==", LoggerEnum.INFO.name());
      }
    }
    ProjectLogger.log("Kafka telemetry dispatcher status: successful.", LoggerEnum.INFO.name());
  }

  /**
   * This method will extract requested telemetry data from Request object. data is coming inside
   * body key. it can have value as string or byte [].
   *
   * @param request Request
   * @return List<String>
   * @throws Exception
   */
  private List<String> getEvents(Request request) throws Exception {
    List<String> events = null;
    Object body = request.get(JsonKey.BODY);
    if (body instanceof String) {
      events = getEvents((String) body);
    } else if (body instanceof byte[]) {
      events = getEvents((byte[]) body);
    } else {
      emptyRequestError(Constant.INVALID_CONTENT_TYPE_MSG);
    }

    return events;
  }

  private void emptyRequestError(String message) throws ProjectCommonException {
    throw new ProjectCommonException(
        ResponseCode.invalidRequestData.getErrorCode(),
        message,
        ResponseCode.CLIENT_ERROR.getResponseCode());
  }

  private List<String> getEvents(String body) throws Exception {
    if (StringUtils.isBlank(body)) emptyRequestError(Constant.INVALID_REQ_BODY_MSG);

    Request request = mapper.readValue(body, Request.class);
    List<String> events = new ArrayList<String>();
    if (request != null && MapUtils.isNotEmpty(request.getRequest())) {
      List<Object> objList = (List<Object>) request.getRequest().get(JsonKey.EVENTS);
      if (CollectionUtils.isNotEmpty(objList)) {
        for (Object obj : objList) {
          events.add(mapper.writeValueAsString(obj));
        }
      }
    }
    return events;
  }

  private List<String> getEvents(byte[] body) throws Exception {
    if (null == body) emptyRequestError(Constant.INVALID_REQ_BODY_MSG);
    try {
      List<String> events = new ArrayList<String>();
      InputStream is = new ByteArrayInputStream(body);
      BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));
      String temp = null;
      while ((temp = bfReader.readLine()) != null) {
        Map<String, Object> row = mapper.readValue(temp, Map.class);
        Map<String, Object> data = (Map<String, Object>) row.get("data");
        if (data != null) {
          List<Object> objList = (List<Object>) data.get("events");
          events.addAll(getEvents(objList));
        }
      }
      return events;
    } catch (ProjectCommonException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ProjectCommonException(
          ResponseCode.invalidRequestData.getErrorCode(),
          Constant.INVALID_FILE_MSG,
          ResponseCode.CLIENT_ERROR.getResponseCode());
    }
  }

  private List<String> getEvents(List<Object> objList) throws Exception {
    List<String> events = new ArrayList<String>();
    if (null != objList && !objList.isEmpty()) {
      for (Object event : objList) {
        if (null != event) {
          events.add(mapper.writeValueAsString(event));
        }
      }
    }
    return events;
  }

  /** This method will do the initialization of kafka consumer and producer. */
  private static void initKafkaClient() {
    ProjectLogger.log(
        "BootStrap server value from ENV ==" + BOOTSTRAP_SERVERS, LoggerEnum.INFO.name());
    ProjectLogger.log("Kafka topic value from ENV ===" + topic, LoggerEnum.INFO.name());
    try {
      producer = KafkaClient.createProducer(BOOTSTRAP_SERVERS, Constant.KAFKA_CLIENT_PRODUCER);
      consumer = KafkaClient.createConsumer(BOOTSTRAP_SERVERS, Constant.KAFKA_CLIENT_CONSUMER);
    } catch (Exception e) {
      ProjectLogger.log("", e);
    }
  }
}
