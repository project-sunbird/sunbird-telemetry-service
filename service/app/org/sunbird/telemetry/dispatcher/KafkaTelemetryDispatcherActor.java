package org.sunbird.telemetry.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
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
import org.sunbird.util.ConfigUtil;
import util.Constant;
import util.EnvConstant;
import util.Message;

/**
 * KafkaTelemetryDispatcherActor handles request to dispatch a telemetry message on Kafka.
 *
 * @author Mahesh Kumar Gangula
 */
@ActorConfig(
  tasks = {Constant.DISPATCH_TELEMETRY_TO_KAFKA},
  asyncTasks = {}
)
public class KafkaTelemetryDispatcherActor extends BaseActor {

  private static Config config = ConfigUtil.getConfig();
  private static String BOOTSTRAP_SERVERS =
      config.getString(EnvConstant.SUNBIRD_TELEMETRY_KAFKA_SERVICE_CONFIG);
  private static String topic = config.getString(EnvConstant.SUNBIRD_TELEMETRY_KAFKA_TOPIC);
  private ObjectMapper mapper = new ObjectMapper();
  private static Producer<Long, String> producer;

  @Override
  public void onReceive(Request request) throws Throwable {
    if (producer == null) {
      initKafkaClient();
    }
    String operation = request.getOperation();
    if (Constant.DISPATCH_TELEMETRY_TO_KAFKA.equals(operation)) {
      Response response = new Response();
      response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
      sender().tell(response, self());
      List<String> events = getEvents(request);
      dispatchEvents(events);
    } else {
      onReceiveUnsupportedMessage(operation);
    }
  }

  /**
   * Dispatches a telemetry message on Kafka for each event in received list.
   *
   * @param events List of telemetry events
   */
  private void dispatchEvents(List<String> events) {
    for (String event : events) {
      ProducerRecord<Long, String> record = new ProducerRecord<>(topic, event);
      if (producer != null) {
        producer.send(record);
      } else {
        ProjectLogger.log(
            "KafkaTelemetryDispatcherActor:dispatchEvents: Kafka producer is not initialised.",
            LoggerEnum.INFO.name());
      }
    }
    ProjectLogger.log(
        "KafkaTelemetryDispatcherActor:dispatchEvents: Events successfully dispatched.",
        LoggerEnum.INFO.name());
  }

  /**
   * Construct a list of telemetry events based on request data.
   *
   * @param request Request with telemetry events in JSON string or gzip byte array format
   * @return List of telemetry events
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
      emptyRequestError(Message.INVALID_CONTENT_TYPE_MSG_ERROR);
    }

    return events;
  }

  private void emptyRequestError(String message) throws ProjectCommonException {
    throw new ProjectCommonException(
        ResponseCode.invalidRequestData.getErrorCode(),
        message,
        ResponseCode.CLIENT_ERROR.getResponseCode());
  }

  @SuppressWarnings("unchecked")
  private List<String> getEvents(String body) throws Exception {
    if (StringUtils.isBlank(body)) emptyRequestError(Message.INVALID_REQ_BODY_MSG_ERROR);

    Request request = mapper.readValue(body, Request.class);
    List<String> events = new ArrayList<>();
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

  @SuppressWarnings("unchecked")
  private List<String> getEvents(byte[] body) {
    if (null == body) emptyRequestError(Message.INVALID_REQ_BODY_MSG_ERROR);
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
          Message.INVALID_FILE_MSG_ERROR,
          ResponseCode.CLIENT_ERROR.getResponseCode());
    }
  }

  private List<String> getEvents(List<Object> objList) throws Exception {
    List<String> events = new ArrayList<>();
    if (null != objList && !objList.isEmpty()) {
      for (Object event : objList) {
        if (null != event) {
          events.add(mapper.writeValueAsString(event));
        }
      }
    }
    return events;
  }

  /** Initialises Kafka producer required for dispatching messages on Kafka. */
  private static void initKafkaClient() {
    ProjectLogger.log(
        "KafkaTelemetryDispatcherActor:initKafkaClient: Bootstrap servers = " + BOOTSTRAP_SERVERS,
        LoggerEnum.INFO.name());
    ProjectLogger.log(
        "KafkaTelemetryDispatcherActor:initKafkaClient: topic = " + topic, LoggerEnum.INFO.name());
    try {
      producer = KafkaClient.createProducer(BOOTSTRAP_SERVERS, Constant.KAFKA_CLIENT_PRODUCER);
    } catch (Exception e) {
      ProjectLogger.log("KafkaTelemetryDispatcherActor:initKafkaClient: An exception occurred.", e);
    }
  }
}
