package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
      validateGZReq(request);
    } else {
      validateJsonReq(request);
    }
  }

  private static void validateJsonReq(Request request) {
    validateProperData(request);
  }

  private static void validateGZReq(Request request) {
    byte requestedData[] = (byte[]) request.get(Constant.BODY);
    if (requestedData == null || requestedData.length == 0) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateGZReq requested data = " + request,
          LoggerEnum.INFO.name());
      throw createProjectException(Message.INVALID_FILE_MSG_ERROR);
    }
  }

  private static void validateProperData(Request request) {
    String requestedData = (String) request.get(Constant.BODY);
    Map<String, Object> map = null;
    try {
      map = mapper.readValue(requestedData, new TypeReference<HashMap<String, Object>>() {});
    } catch (Exception e) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateProperData json requested data : " + map,
          LoggerEnum.INFO.name());
      ProjectLogger.log(
          "TelemetryRequestValidator:validateProperData Error message " + e.getMessage(), e);
    }
    if (map == null) {
      throw createProjectException("");
    }
    Map<String, Object> innerMap = (Map<String, Object>) map.get(JsonKey.REQUEST);
    if (innerMap == null || innerMap.size() == 0) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateProperData Request object missing : " + map,
          LoggerEnum.INFO.name());
      throw createProjectException("");
    }
    List<Map<String, Object>> eventMap = (List<Map<String, Object>>) innerMap.get(JsonKey.EVENTS);
    if (eventMap == null || eventMap.size() == 0) {
      ProjectLogger.log(
          "TelemetryRequestValidator:validateProperData Events object missing : " + map,
          LoggerEnum.INFO.name());
      throw createProjectException(Message.TELEMETRY_EVENT_MISSING_MSG_ERROR);
    }
    validateMaxSize(eventMap);
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
}
