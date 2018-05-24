package controllers;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.controllers.BaseController;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Result;
import util.Constant;
import util.Message;

/**
 * Telemetry controller handles Telemetry APIs.
 *
 * @author Mahesh Kumar Gangula
 */
public class TelemetryController extends BaseController {
  /**
   * Save telemetry information. Request body either with content type application/json or content
   * encoding gzip.
   *
   * @return Return a promise for telemetry API result.
   */
  public F.Promise<Result> save() {
    try {
      String contentTypeHeader = request().getHeader(HttpHeaders.CONTENT_TYPE);
      String encodingHeader = request().getHeader(HttpHeaders.ACCEPT_ENCODING);
      Request request = new Request();
      request.setOperation(Constant.DISPATCH_TELEMETRY_OPERATION_NAME);
      request.put(Constant.HEADERS, request().headers());
      if (Constant.APPLICATION_JSON.equalsIgnoreCase(contentTypeHeader)) {
        ProjectLogger.log(
            "TelemetryController:save: Received telemetry in JSON format.", LoggerEnum.INFO.name());
        request.put(Constant.BODY, Json.stringify(request().body().asJson()));
      } else if ((Constant.APPLICATION_OCTET.equalsIgnoreCase(contentTypeHeader)
              || Constant.APPLICATION_ZIP.equalsIgnoreCase(contentTypeHeader))
          && StringUtils.containsIgnoreCase(encodingHeader, Constant.GZIP)) {
        ProjectLogger.log(
            "TelemetryController:save: Received telemetry in gzip format.", LoggerEnum.INFO.name());
        byte[] body = request().body().asRaw().asBytes();
        request.put(Constant.BODY, body);
      } else {
        throw new ProjectCommonException(
            ResponseCode.invalidRequestData.getErrorCode(),
            Message.INVALID_HEADER_MSG,
            ResponseCode.CLIENT_ERROR.getResponseCode());
      }
      return actorResponseHandler(getActorRef(), request, timeout, "", request());

    } catch (Exception e) {
      ProjectLogger.log(e.getMessage(), e);
      return Promise.<Result>pure(
          createCommonExceptionResult(request().path(), e, request().method()));
    }
  }
}
