package util;

/**
 * This class will keep all the environment variable used inside sunbird telemetry service.
 *
 * @author Manzarul
 */
public class EnvConstant {

  public static final String SUNBIRD_TELEMETRY_DISPATCH = "sunbird_telemetry_dispatchers";
  public static final String SUNBIRD_TELEMETRY_KAFKA_TOPIC = "sunbird_telemetry_kafka_topic";
  /** This will have on and off value. */
  public static final String EKSTEP_TELEMETRY_STORAGE_TOGGLE = "ekstep_telemetry_storage_toggle";

  public static final String SUNBIRD_TELEMETRY_KAFKA_SERVICE_CONFIG =
      "sunbird_telemetry_kafka_servers_config";
}
