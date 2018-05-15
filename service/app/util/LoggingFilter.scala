package util

import play.api.libs.iteratee.Iteratee
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}
import scala.concurrent.ExecutionContext.Implicits.global

class LoggingFilter extends EssentialFilter {
  override def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    override def apply(request: RequestHeader): Iteratee[Array[Byte], Result] = {
      val startTime = System.currentTimeMillis();
      next(request).map {
        result =>
          println(request.path + " request processed in " + (System.currentTimeMillis() - startTime) + "ms.");
          result;
      };
    }
  }
}