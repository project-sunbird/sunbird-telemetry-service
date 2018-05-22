package util;

import java.lang.reflect.Method;
import org.sunbird.global.BaseGlobal;
import play.Application;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;

/**
 * This class will work as a filter.all the method are implemented under BaseGlobal.like onRequest,
 * onStart,onError.
 *
 * @author Manzarul
 */
public class Global extends BaseGlobal {

  @Override
  public void onStart(Application app) {
    super.onStart(app);
  }

  /**
   * This method will be called on each request.
   *
   * @param request Request
   * @param actionMethod Method
   * @return Action
   */
  @SuppressWarnings("rawtypes")
  public play.mvc.Action onRequest(Request request, Method actionMethod) {

    return super.onRequest(request, actionMethod);
  }

  /** This method will be used to send the request header missing error message. */
  @Override
  public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
    return super.onError(request, t);
  }
}
