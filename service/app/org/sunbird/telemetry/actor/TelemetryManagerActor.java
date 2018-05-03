package org.sunbird.telemetry.actor;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;

import telemetry.dispatcher.IDispatcher;
import telemetry.dispatcher.TelemetryDispatcherFactory;

public class TelemetryManagerActor {

	String[] dispatcherNames = new String[] { "ekstep" };
	

	public TelemetryManagerActor() {
		String dispatchersStr = System.getenv("sunbird_telemetry_dispatchers");
		if (StringUtils.isNotBlank(dispatchersStr)) {
			dispatcherNames = dispatchersStr.split(",");
		}
		ProjectLogger.log("Telemetry dispatcher names.", dispatcherNames, LoggerEnum.INFO.name());
	}
	
	@SuppressWarnings("unchecked")
	public Response save(Request request) throws Exception {
		Map<String, Object> reqMap = request.getRequest();
		List<String> events = (List<String>) reqMap.get("events");
		for (String name: dispatcherNames) {
			IDispatcher dispatcher = TelemetryDispatcherFactory.getDispatcher(name);
			dispatcher.dispatch(events);
		}
		Response response = new Response();
	    response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
	    return response;
	}
}
