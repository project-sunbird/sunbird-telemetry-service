/** */
package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
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
  private static final int TELEMETRY_DATA_SIZE = 1000;
  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();

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

  private static void validateGZReq(Request request) {}

  private static void validateProperData(Request request) {
    String requestedData = (String) request.get(Constant.BODY);
    Map<String, Object> map = null;
    try {
      map = mapper.readValue(requestedData, new TypeReference<HashMap<String, Object>>() {});
    } catch (Exception e) {
      ProjectLogger.log(e.getMessage(), e);
    }
    if (map == null) {
      throw createProjectException("");
    }
    Map<String, Object> innerMap = (Map<String, Object>) map.get(JsonKey.REQUEST);
    if (innerMap == null || innerMap.size() == 0) {
      throw createProjectException("");
    }
    List<Map<String, Object>> eventMap = (List<Map<String, Object>>) innerMap.get(JsonKey.EVENTS);
    if (eventMap == null || eventMap.size() == 0) {
      throw createProjectException("Telemetry events data is missing.");
    }
    validateMaxSize(eventMap);
  }

  private static void validateMaxSize(List<Map<String, Object>> eventList) {
    if (TELEMETRY_DATA_SIZE < eventList.size()) {
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
