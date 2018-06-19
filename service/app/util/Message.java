package util;

/**
 * This class will contains all the messages used inside telemetry micro service.
 *
 * @author Manzarul
 */
public class Message {

  public static final String INVALID_FILE_MSG_ERROR =
      "Please provide valid binary gzip file. File structure is invalid.";
  public static final String INVALID_REQ_BODY_MSG_ERROR =
      "Please provide valid request body. Request body is incorrect.";
  public static final String INVALID_CONTENT_TYPE_MSG_ERROR = "Please provide valid content-type.";
  public static final String TELEMETRY_PROCESSING_ERROR =
      "Error in ekstep telemetry dispatcher. Please try again later.";
  public static final String INVALID_HEADER_MSG_ERROR = "Please provide valid headers.";
  public static final String DEFAULT_MSG_ERROR = "Please provide valid data.";
  public static final String TELEMETRY_EVENT_MISSING_MSG_ERROR =
      "Telemetry events data is missing.";
}
