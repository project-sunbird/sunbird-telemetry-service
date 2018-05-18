package util;

import javax.inject.Inject;
import play.api.mvc.EssentialFilter;
import play.http.HttpFilters;

public class Filters implements HttpFilters {

  private final LoggingFilter loggingFilter;

  @Inject
  public Filters(LoggingFilter loggingFilter) {
    this.loggingFilter = loggingFilter;
  }

  @Override
  public EssentialFilter[] filters() {
    return new EssentialFilter[] {loggingFilter};
  }
}
