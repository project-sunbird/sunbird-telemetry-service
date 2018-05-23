package util;

import java.lang.reflect.Method;
import org.sunbird.global.BaseGlobal;
import play.Application;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;

/**
 * Perform specific tasks to perform on application start, request, error etc.
 *
 * @author Manzarul
 */
public class Global extends BaseGlobal {

  @Override
  public void onStart(Application app) {
    super.onStart(app);
  }

  @SuppressWarnings("rawtypes")
  public play.mvc.Action onRequest(Request request, Method actionMethod) {
    return super.onRequest(request, actionMethod);
  }

  @Override
  public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
    return super.onError(request, t);
  }
}
