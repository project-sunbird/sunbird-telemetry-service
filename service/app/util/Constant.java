/** */
package util;

/** @author Manzarul This class will keep all the constant values. */
public class Constant {

  public static final String CONTENT_TYPE = "content-type";
  public static final String ACCEPT_ENCODING = "accept-encoding";
  public static final String OPERATION_NAME = "dispatchtelemetry";
  public static final String HEADERS = "headers";
  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_OCTET = "application/octet-stream";
  public static final String BODY = "body";
  public static final String GZIP = "gzip";
  public static final String APPLICATION_ZIP = "application/zip";
  public static final String INVALID_HEADER_MSG = "Please provide valid headers.";
  public static final String DISPATCH_TELEMETRY_TO_KAFKA = "dispatchtelemetrytokafka";
  public static final String INVALID_FILE_MSG =
      "Please provide valid binary gzip file. File structure is invalid.";
  public static final String INVALID_REQ_BODY_MSG =
      "provide valid request body. Request body is incorrect.";
  public static final String INVALID_CONTENT_TYPE_MSG = "Please provide valid contnet-type.";
  public static final String SUNBIRD_TELEMETRY_DISPATCH_ENV = "sunbird_telemetry_dispatchers";
}
