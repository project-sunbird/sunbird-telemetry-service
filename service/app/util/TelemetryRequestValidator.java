package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;

/**
 * This class will do the request validation for incoming telemetry data.
 *
 * @author Manzarul
 */
public class TelemetryRequestValidator {
  private static ObjectMapper mapper = new ObjectMapper();
  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();

  /**
   * This method will do the telemetry request data validation. it will take request data and data
   * type as an input. assumption is there is only two type coming 1. json and anothe gzip file.
   *
   * @param request data requested by client.
   * @param reqType json or gzip
   */
  public static void validateTelemetryRequest(Request request, String reqType) {
    if (Constant.GZIP.equalsIgnoreCase(reqType)) {
      validateGZipRequest(request);
    } else {
      validateJsonRequest(request);
    }
  }

  private static void validateJsonRequest(Request request) {
    validateRequestFormat(request);
  }

  private static void validateGZipRequest(Request request) {
    byte requestedData[] = (byte[]) request.get(Constant.BODY);
    if (requestedData == null || requestedData.length == 0) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateGZipRequest: Requested data = " + request,
          LoggerEnum.INFO.name());
      throw createProjectException(Message.INVALID_FILE_MSG_ERROR);
    }
  }

  private static void validateRequestFormat(Request request) {
    String requestedData = (String) request.get(Constant.BODY);
    Map<String, Object> map = null;
    try {
      map = mapper.readValue(requestedData, new TypeReference<HashMap<String, Object>>() {});
    } catch (Exception e) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateRequestFormat: Json requested data : " + map,
          LoggerEnum.INFO.name());
      ProjectLogger.log(
          "TelemetryRequestValidator:validateRequestFormat: Error message : " + e.getMessage(), e);
    }
    if (map == null) {
      throw createProjectException("");
    }
    boolean response = isEventsPresent(map);
    if (!response) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateRequestFormat: Request or Events key is missing : "
              + map,
          LoggerEnum.INFO.name());
      throw createProjectException("");
    }
  }

  private static void validateMaxSize(List<Map<String, Object>> eventList) {
    if (Constant.TELEMETRY_DATA_SIZE < eventList.size()) {
      // later we can have check for max number of events.
    }
  }

  private static ProjectCommonException createProjectException(String message) {
    if (StringUtils.isBlank(message)) {
      return new ProjectCommonException(
          ResponseCode.invalidRequestData.getErrorCode(),
          ResponseCode.invalidRequestData.getErrorMessage(),
          ERROR_CODE);
    }
    return new ProjectCommonException(
        ResponseCode.invalidRequestData.getErrorCode(), message, ERROR_CODE);
  }

  /**
   * This method checks telemetry request structure. Request can come in two formats: Format 1:
   * {"events":[...]}, Format 2: {"request":{"events":[...]}}
   *
   * @param requestMap Telemetry request
   * @return True, if events is present in one of the mentioned formats. Otherwise, return false.
   */
  private static boolean isEventsPresent(Map<String, Object> requestMap) {
    Map<String, Object> eventsMap = (Map<String, Object>) requestMap.get(JsonKey.REQUEST);
    if (MapUtils.isEmpty(eventsMap)) {
      eventsMap = requestMap;
    }
    List<Map<String, Object>> eventMap = (List<Map<String, Object>>) eventsMap.get(JsonKey.EVENTS);
    if (CollectionUtils.isEmpty(eventMap)) {
      return false;
    }
    return true;
  }
}
