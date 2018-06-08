package org.sunbird.telemetry.actor;

import akka.actor.ActorRef;
import com.typesafe.config.Config;
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
import org.sunbird.util.ConfigUtil;
import util.Constant;
import util.EnvConstant;

/**
 * TelemetryManagerActor handles Telemetry requests.
 *
 * @author Mahesh Kumar Gangula
 */
@ActorConfig(
  tasks = {Constant.DISPATCH_TELEMETRY_OPERATION_NAME},
  asyncTasks = {}
)
public class TelemetryManagerActor extends BaseActor {

  List<String> dispatchers = new ArrayList<String>();
  private static final String defaultDispacher = "ekstep";
  private static Config config = ConfigUtil.getConfig();

  public TelemetryManagerActor() {
    getDispatchers();
  }

  private void dispatch(String dispatcher, Request baseRequest) {
    Request request = getDispatcherRequest(baseRequest, dispatcher);
    ActorRef actor = (ActorRef) SunbirdMWService.getRequestRouter();
    if (null != actor) {
      // actor.tell(request, getSelf());
    }
  }

  private Request getDispatcherRequest(Request request, String dispatcher) {
    Request dispatcherRequest = new Request();
    dispatcherRequest.setRequest(request.getRequest());
    dispatcherRequest.setOperation(Constant.DISPATCH_TELEMETRY_OPERATION_NAME + "to" + dispatcher);
    return dispatcherRequest;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    String ekstepStorageToggle = config.getString(EnvConstant.EKSTEP_TELEMETRY_STORAGE_TOGGLE);
    if (Constant.DISPATCH_TELEMETRY_OPERATION_NAME.equals(operation)) {
      Object body = request.get(JsonKey.BODY);
      Map<String, String[]> headers = (Map<String, String[]>) request.get(Constant.HEADERS);
      // if ekstep_telemetry_storage_toggle is on then only send the telemetry value
      // to ekstep.
      if (StringUtils.equalsIgnoreCase(Constant.ON, ekstepStorageToggle)) {
        if (body instanceof String) {
          EkstepTelemetryDispatcher.dispatch(headers, (String) body);
        } else if (body instanceof byte[]) {
          EkstepTelemetryDispatcher.dispatch(headers, (byte[]) body);
        } else {
          onReceiveUnsupportedMessage(operation);
        }
      }
      Response response = new Response();
      response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
      sender().tell(response, self());

      for (String name : dispatchers) {
        dispatch(name, request);
      }
    } else {
      onReceiveUnsupportedOperation(operation);
    }
  }

  private void getDispatchers() {
    String dispatchersStr = config.getString(EnvConstant.SUNBIRD_TELEMETRY_DISPATCH);
    ProjectLogger.log(
        "TelemetryManagerActor:getDispatchers: dispatchersStr = " + dispatchersStr,
        LoggerEnum.INFO);

    if (StringUtils.isNotBlank(dispatchersStr)) {
      for (String name : dispatchersStr.toLowerCase().split(",")) {
        if (!defaultDispacher.equals(name)) {
          dispatchers.add(name);
        }
      }
    }
    ProjectLogger.log(
        "TelemetryManagerActor:getDispatchers: Telemetry dispatcher size = "
            + dispatchers.size()
            + " names = ",
        dispatchers,
        LoggerEnum.INFO.name());
  }
}
