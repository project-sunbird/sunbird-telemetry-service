/** */
package util;

import controllers.BaseController;
import java.lang.reflect.Method;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.sunbird.actor.service.SunbirdMWService;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.ExecutionContext;
import org.sunbird.common.responsecode.ResponseCode;
import play.Application;
import play.GlobalSettings;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Results;

/**
 * This class will work as a filter.
 *
 * @author Manzarul
 */
public class Global extends GlobalSettings {

  private class ActionWrapper extends Action.Simple {
    public ActionWrapper(Action<?> action) {
      this.delegate = action;
    }

    @Override
    public Promise<Result> call(Http.Context ctx) throws Throwable {
      Http.Response response = ctx.response();
      response.setHeader("Access-Control-Allow-Origin", "*");
      return delegate.call(ctx);
    }
  }

  /**
   * This method will be called on application start up. it will be called only time in it's life
   * cycle.
   *
   * @param app Application
   */
  public void onStart(Application app) {
    SunbirdMWService.init();
    ProjectLogger.log("Server started.");
  }

  /**
   * This method will be called on each request.
   *
   * @param request Request
   * @param actionMethod Method
   * @return Action
   */
  @SuppressWarnings("rawtypes")
  public Action onRequest(Request request, Method actionMethod) {

    String messageId = request.getHeader(JsonKey.MESSAGE_ID);
    if (StringUtils.isEmpty(messageId)) {
      UUID uuid = UUID.randomUUID();
      messageId = uuid.toString();
    }
    ExecutionContext.setRequestId(messageId);
    return new ActionWrapper(super.onRequest(request, actionMethod));
  }

  /** This method will be used to send the request header missing error message. */
  @Override
  public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
    Response response = null;
    ProjectCommonException commonException = null;
    if (t instanceof ProjectCommonException) {
      commonException = (ProjectCommonException) t;
      response =
          BaseController.createResponseOnException(request.path(), (ProjectCommonException) t);
    } else if (t instanceof akka.pattern.AskTimeoutException) {
      commonException =
          new ProjectCommonException(
              ResponseCode.operationTimeout.getErrorCode(),
              ResponseCode.operationTimeout.getErrorMessage(),
              ResponseCode.SERVER_ERROR.getResponseCode());
    } else {
      commonException =
          new ProjectCommonException(
              ResponseCode.internalError.getErrorCode(),
              ResponseCode.internalError.getErrorMessage(),
              ResponseCode.SERVER_ERROR.getResponseCode());
    }
    response = BaseController.createResponseOnException(request.path(), commonException);
    return Promise.<Result>pure(
        Results.status(ResponseCode.SERVER_ERROR.getResponseCode(), Json.toJson(response)));
  }
}
