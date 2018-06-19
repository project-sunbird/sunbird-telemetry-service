package org.sunbird.telemetry.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.RestUtil;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.util.ConfigUtil;
import util.Constant;
import util.Message;

/**
 * Dispatcher responsible for storing Telemetry in Ekstep platform.
 *
 * @author Manzarul
 */
public class EkstepTelemetryDispatcher {
  private static final String API_URL =
      RestUtil.getBasePath() + ConfigUtil.getConfig().getString(Constant.EKSTEP_TELEMETRY_API_URL);

  public static boolean dispatch(Map<String, String[]> reqHeaders, String body) throws Exception {
    Map<String, String> headers = getHeaders(MediaType.APPLICATION_JSON);
    BaseRequest request = Unirest.post(API_URL).headers(headers).body(getEvents(body));
    executeRequest(request);
    return true;
  }

  public static boolean dispatch(Map<String, String[]> reqHeaders, byte[] body) throws Exception {
    Map<String, String> headers = getHeaders(Constant.GZIP);
    BaseRequest request = Unirest.post(API_URL).headers(headers).body(body);
    executeRequest(request);
    return true;
  }

  private static Map<String, String> getHeaders(String requestContentType) {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put(HttpHeaders.CONTENT_TYPE, requestContentType);
    if (Constant.APPLICATION_ZIP.equalsIgnoreCase(requestContentType)) {
      headerMap.put(HttpHeaders.CONTENT_ENCODING, Constant.GZIP);
    }

    return headerMap;
  }

  private static void executeRequest(BaseRequest request) {
    HttpResponse<JsonNode> result;
    try {
      result = RestUtil.execute(request);
      ProjectLogger.log(
          "EkstepTelemetryDispatcher:executeRequest: Ekstep telemetry dispatcher status = "
              + result.getStatus(),
          LoggerEnum.INFO.name());
      if (!RestUtil.isSuccessful(result)) {
        String err = RestUtil.getFromResponse(result, "params.err");
        String message = RestUtil.getFromResponse(result, "params.errmsg");
        throw new ProjectCommonException(err, message, ResponseCode.SERVER_ERROR.getResponseCode());
      }
    } catch (Exception e) {
      ProjectLogger.log(
          "EkstepTelemetryDispatcher:executeRequest: Generic exception in executeRequest = "
              + e.getMessage(),
          LoggerEnum.ERROR.name());
      throw new ProjectCommonException(
          Constant.TELEMETRY_DISPATCHER_ERROR,
          Message.TELEMETRY_PROCESSING_ERROR,
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
  }

  @SuppressWarnings("unchecked")
  private static String getEvents(String body) throws Exception {
    if (StringUtils.isNotBlank(body)) {
      ObjectMapper mapper = new ObjectMapper();
      Request request = mapper.readValue(body, Request.class);
      if (request != null && MapUtils.isNotEmpty(request.getRequest())) {
        Map<String, Object> map = (Map<String, Object>) request.getRequest();
        if (map != null) {
          return mapper.writeValueAsString(map);
        }
      } else {
        // Here data is coming without request body.
        return body;
      }
    }
    throw emptyRequestError(Message.INVALID_REQ_BODY_MSG_ERROR);
  }

  private static ProjectCommonException emptyRequestError(String message) {
    return new ProjectCommonException(
        ResponseCode.invalidRequestData.getErrorCode(),
        message,
        ResponseCode.CLIENT_ERROR.getResponseCode());
  }
}
