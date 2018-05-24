package util;

/**
 * This class will keep all the constant values.
 *
 * @author Manzarul
 */
public class Constant {

  public static final String CONTENT_TYPE = "content-type";
  public static final String ACCEPT_ENCODING = "accept-encoding";
  public static final String DISPATCH_TELEMETRY_OPERATION_NAME = "dispatchtelemetry";
  public static final String HEADERS = "headers";
  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_OCTET = "application/octet-stream";
  public static final String BODY = "body";
  public static final String GZIP = "gzip";
  public static final String APPLICATION_ZIP = "application/zip";
  public static final String INVALID_HEADER_MSG = "Please provide valid headers.";
  public static final String DISPATCH_TELEMETRY_TO_KAFKA = "dispatchtelemetrytokafka";

  public static final String SUNBIRD_TELEMETRY_DISPATCH_ENV = "sunbird_telemetry_dispatchers";
  public static final String KAFKA_CLIENT_PRODUCER = "KafkaClientProducer";
  public static final String TELEMETRY_DISPATCHER_ERROR = "TELEMETRY_DISPATCHER_ERROR";

  public static final String ON = "on";

  /** This will have on and off value. */
  public static final String EKSTEP_TELEMETRY_STORAGE_TOGGLE = "ekstep_telemetry_storage_toggle";

  public static final String EKSTEP_TELEMETRY_API_URL = "ekstep_telemetry_api_url";
  public static final String SUNBIRD_TELEMETRY_KAFKA_SERVICE_CONFIG =
      "sunbird_telemetry_kafka_servers_config";
  public static final String SUNBIRD_TELEMETRY_KAFKA_TOPIC = "sunbird_telemetry_kafka_topic";
}
