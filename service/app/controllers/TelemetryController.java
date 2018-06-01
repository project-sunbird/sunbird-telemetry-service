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
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import util.Constant;
import util.Message;
import util.TelemetryRequestValidator;

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
      RequestBody reqBody = request().body();
      if (reqBody == null) {
        // throwing invalid data exception with proper error msg.
        throw createClientExcptionWithInvalidData(Message.INVALID_REQ_BODY_MSG_ERROR);
      }
      if (Constant.APPLICATION_JSON.equalsIgnoreCase(contentTypeHeader)) {
        ProjectLogger.log(
            "TelemetryController:save: Received telemetry in JSON format.", LoggerEnum.INFO.name());
        request.put(Constant.BODY, Json.stringify(request().body().asJson()));
        // doing validation for request body should not be empty, should have event array
        TelemetryRequestValidator.validateTelemetryRequest(request, Constant.APPLICATION_JSON);
      } else if ((Constant.APPLICATION_OCTET.equalsIgnoreCase(contentTypeHeader)
              || Constant.APPLICATION_ZIP.equalsIgnoreCase(contentTypeHeader))
          && StringUtils.containsIgnoreCase(encodingHeader, Constant.GZIP)) {
        ProjectLogger.log(
            "TelemetryController:save: Received telemetry in gzip format.", LoggerEnum.INFO.name());
        RawBuffer buffer = reqBody.asRaw();
        if (buffer == null) {
          throw createClientExcptionWithInvalidData(Message.INVALID_FILE_MSG_ERROR);
        }
        byte[] body = buffer.asBytes();
        request.put(Constant.BODY, body);
        // doing validation for request body should not be empty, should have event array
        TelemetryRequestValidator.validateTelemetryRequest(request, Constant.GZIP);
      } else {
        throw createClientExcptionWithInvalidData(Message.INVALID_HEADER_MSG_ERROR);
      }
      return actorResponseHandler(getActorRef(), request, timeout, "", request());

    } catch (Exception e) {
      ProjectLogger.log(e.getMessage(), e);
      return Promise.<Result>pure(
          createCommonExceptionResult(request().path(), e, request().method()));
    }
  }

  private ProjectCommonException createClientExcptionWithInvalidData(String message) {
    if (StringUtils.isEmpty(message)) {
      message = Message.DEFAULT_MSG_ERROR;
    }
    return new ProjectCommonException(
        ResponseCode.invalidRequestData.getErrorCode(),
        message.trim(),
        ResponseCode.CLIENT_ERROR.getResponseCode());
  }
}
