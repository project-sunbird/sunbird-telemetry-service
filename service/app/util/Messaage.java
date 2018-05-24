package util;

/**
 * This class will contains all the messages used inside telemetry micro service.
 *
 * @author Manzarul
 */
public class Messaage {

  public static final String INVALID_FILE_MSG =
      "Please provide valid binary gzip file. File structure is invalid.";
  public static final String INVALID_REQ_BODY_MSG =
      "Please provide valid request body. Request body is incorrect.";
  public static final String INVALID_CONTENT_TYPE_MSG = "Please provide valid content-type.";
  public static final String TELEMETRY_PROCESSING_ERROR =
      "Error in ekstep telemetry dispatcher. Please try again later.";
}
