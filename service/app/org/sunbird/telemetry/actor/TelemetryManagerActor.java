package org.sunbird.telemetry.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;

import telemetry.dispatcher.IDispatcher;
import telemetry.dispatcher.TelemetryDispatcherFactory;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

@ActorConfig(tasks = { "dispatchTelemetry" }, asyncTasks = {})
public class TelemetryManagerActor extends BaseActor {

	List<String> dispatcherNames = new ArrayList<String>();
	private static final String defaultDispacherName = "ekstep";

	public TelemetryManagerActor() {
		String dispatchersStr = System.getenv("sunbird_telemetry_dispatchers");
		if (StringUtils.isNotBlank(dispatchersStr)) {
			for (String name : dispatchersStr.toLowerCase().split(",")) {
				if (!defaultDispacherName.equals(name)) {
					dispatcherNames.add(name);
				}
			}
		}
		ProjectLogger.log("Telemetry dispatcher names.", dispatcherNames, LoggerEnum.INFO.name());
	}

	private void dispatch(String dispatcherName, List<String> events) throws Exception {
		IDispatcher dispatcher = TelemetryDispatcherFactory.getDispatcher(dispatcherName);
		dispatcher.dispatch(events);
	}

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		if ("dispatchTelemetry".equals(operation)) {
			Map<String, Object> reqMap = request.getRequest();
			@SuppressWarnings("unchecked")
			List<String> events = (List<String>) reqMap.get("events");
			dispatch(defaultDispacherName, events);
			for (String name : dispatcherNames) {
				dispatch(name, events);
			}
			Response response = new Response();
			response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
			sender().tell(response, self());
		} else {
			onReceiveUnsupportedMessage(operation);
		}
	}
}
