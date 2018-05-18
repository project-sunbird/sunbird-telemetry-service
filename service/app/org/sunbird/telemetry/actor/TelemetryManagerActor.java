package org.sunbird.telemetry.actor;

import akka.actor.ActorRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.actor.service.SunbirdMWService;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.telemetry.dispatcher.EkstepTelemetryDispatcher;
import util.Constant;

/**
 * @author Mahesh Kumar Gangula This actor class will receiving telemetry request and write to
 *     EKstep.
 */
@ActorConfig(
  tasks = {"dispatchtelemetry"},
  asyncTasks = {}
)
public class TelemetryManagerActor extends BaseActor {

  List<String> dispatchers = new ArrayList<String>();
  private static final String defaultDispacher = "ekstep";

  public TelemetryManagerActor() {
    getDispatchers();
  }

  private void dispatch(String dispatcher, Request baseRequest) throws Exception {
    Request request = getDispatcherRequest(baseRequest, dispatcher);
    ActorRef actor = (ActorRef) SunbirdMWService.getRequestRouter();
    if (null != actor) {
      actor.tell(request, getSelf());
    }
  }

  private Request getDispatcherRequest(Request request, String dispatcher) {
    Request dispRequest = new Request();
    dispRequest.setRequest(request.getRequest());
    dispRequest.setOperation(Constant.OPERATION_NAME + "to" + dispatcher);
    return dispRequest;
  }

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    if (Constant.OPERATION_NAME.equals(operation)) {
      Object body = request.get("body");
      Map<String, String[]> headers = (Map<String, String[]>) request.get(Constant.HEADERS);
      if (body instanceof String) {
        EkstepTelemetryDispatcher.dispatch(headers, (String) body);
      } else if (body instanceof byte[]) {
        EkstepTelemetryDispatcher.dispatch(headers, (byte[]) body);
      } else {
        onReceiveUnsupportedMessage(operation);
      }
      for (String name : dispatchers) {
        dispatch(name, request);
      }

      Response response = new Response();
      response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
      sender().tell(response, self());
    } else {
      onReceiveUnsupportedOperation(operation);
    }
  }

  private void getDispatchers() {
    String dispatchersStr = System.getenv(Constant.SUNBIRD_TELEMETRY_DISPATCH_ENV);
    if (StringUtils.isNotBlank(dispatchersStr)) {
      for (String name : dispatchersStr.toLowerCase().split(",")) {
        if (!defaultDispacher.equals(name)) {
          dispatchers.add(name);
        }
      }
    }
    ProjectLogger.log("Telemetry dispatcher names.", dispatchers, LoggerEnum.INFO.name());
  }
}
