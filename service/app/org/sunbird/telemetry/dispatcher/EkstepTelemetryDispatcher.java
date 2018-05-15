package org.sunbird.telemetry.dispatcher;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.PropertiesCache;
import org.sunbird.common.models.util.RestUtil;
import org.sunbird.common.responsecode.ResponseCode;

public class EkstepTelemetryDispatcher {

  public static boolean dispatch(Map<String, String[]> reqHeaders, String body) throws Exception {
    Map<String, String> headers = getHeaders(reqHeaders);
    BaseRequest request = Unirest.post(telemetryAPIURL()).headers(headers).body(body);
    executeRequest(request);
    return true;
  }

  public static boolean dispatch(Map<String, String[]> reqHeaders, byte[] body) throws Exception {
    Map<String, String> headers = getHeaders(reqHeaders);
    BaseRequest request = Unirest.post(telemetryAPIURL()).headers(headers).body(body);
    executeRequest(request);
    return true;
  }

  private static Map<String, String> getHeaders(Map<String, String[]> headers) {
    return headers
        .entrySet()
        .stream()
        .filter(
            x ->
                Arrays.asList("content-encoding", "accept-encoding", "content-type")
                    .contains(x.getKey().toLowerCase()))
        .collect(Collectors.toMap(e -> e.getKey(), e -> StringUtils.join(e.getValue(), ",")));
  }

  private static void executeRequest(BaseRequest request) throws Exception {
    HttpResponse<JsonNode> result = null;
    try {
      result = RestUtil.execute(request);
    } catch (Exception e) {
      throw new ProjectCommonException(
          "TELEMETRY_DISPACHER_ERROR",
          "Error while processing Ekstep telemetry dispacher. Please try again later.",
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
    if (!RestUtil.isSuccessful(result)) {
      // TODO: 1. Always returning server_error. Need to improve.
      // TODO: 2. Errors list is not returning which is there in ekstep response. We
      // should return this.
      String err = RestUtil.getFromResponse(result, "params.err");
      String message = RestUtil.getFromResponse(result, "params.errmsg");
      throw new ProjectCommonException(err, message, ResponseCode.SERVER_ERROR.getResponseCode());
    }
  }

  private static String telemetryAPIURL() {
    String apiUrl = RestUtil.getBasePath();
    apiUrl += PropertiesCache.getInstance().getProperty(JsonKey.EKSTEP_TELEMETRY_API_URL);
    return apiUrl;
  }
}
