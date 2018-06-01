package org.sunbird.telemetry.dispatcher;

import com.google.common.net.HttpHeaders;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import java.util.HashMap;
import java.util.Map;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.RestUtil;
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
    Map<String, String> headers = getHeaders(Constant.APPLICATION_JSON);
    BaseRequest request = Unirest.post(API_URL).headers(headers).body(body);
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
      throw new ProjectCommonException(
          Constant.TELEMETRY_DISPATCHER_ERROR,
          Message.TELEMETRY_PROCESSING_ERROR,
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
  }
}
