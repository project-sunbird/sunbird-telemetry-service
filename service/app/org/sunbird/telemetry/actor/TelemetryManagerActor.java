package org.sunbird.telemetry.actor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	private static int defaultSize = 1000;
	private static final String defaultDispacherName = "ekstep";
	private ObjectMapper mapper = new ObjectMapper();

	public TelemetryManagerActor() {
		getMaxRequstSize();
		getDispatchers();
	}

	private void dispatch(String dispatcherName, List<String> events) throws Exception {
		IDispatcher dispatcher = TelemetryDispatcherFactory.getDispatcher(dispatcherName);
		dispatcher.dispatch(events);
	}

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		if ("dispatchTelemetry".equals(operation)) {
			List<String> events = getEvents(request.getRequest());
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

	private List<String> getEvents(Map<String, Object> map) throws Exception {
		String contentType = (String) map.get("content-type");
		List<String> events = null;
		switch (contentType) {
		case "application/json":
			Request request = (Request) map.get("body");
			events = getEvents(request);
			break;
		case "application/zip":
			byte[] body = (byte[]) map.get("body");
			events = getEvents(body);
			break;
		default:
			emptyRequestError("Please provide valid contnet-type.");
		}
		return events;
	}

	private void emptyRequestError(String message) throws ProjectCommonException {
		throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(), message,
				ResponseCode.CLIENT_ERROR.getResponseCode());
	}

	private List<String> getEvents(Request request) throws Exception {
		if (null == request)
			emptyRequestError("Please provide valid request body. request body is empty.");

		List<String> events = new ArrayList<String>();
		if (request != null && request.getRequest() != null && !request.getRequest().isEmpty()) {
			List<Object> objList = (List<Object>) request.getRequest().get("events");
			if (objList != null && !objList.isEmpty()) {
				for (Object obj : objList) {
					events.add(mapper.writeValueAsString(obj));
				}
			}
		}
		return events;
	}

	private List<String> getEvents(byte[] body) throws Exception {
		// TODO: for huge request also we are getting null body. We should check it and
		// give valid error message.
		if (null == body)
			emptyRequestError("Please provide valid request body. request body is incorrect.");
		try {
			List<String> events = new ArrayList<String>();
			InputStream is = new ByteArrayInputStream(body);
			BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while ((temp = bfReader.readLine()) != null) {
				Map<String, Object> row = mapper.readValue(temp, Map.class);
				Map<String, Object> data = (Map<String, Object>) row.get("data");
				if (data != null) {
					List<Object> objList = (List<Object>) data.get("events");
					events.addAll(getEvents(objList));
					if (events.size() > defaultSize) {
						throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
								"Too many events to process. Max limit for a request is " + defaultSize,
								ResponseCode.CLIENT_ERROR.getResponseCode());
					}
				}
			}
			return events;
		} catch (ProjectCommonException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
					"Please provide valid binary gzip file. File structure is invalid.",
					ResponseCode.CLIENT_ERROR.getResponseCode());
		}
	}

	private List<String> getEvents(List<Object> objList) throws Exception {
		List<String> events = new ArrayList<String>();
		if (null != objList && !objList.isEmpty()) {
			for (Object event : objList) {
				if (null != event) {
					events.add(mapper.writeValueAsString(event));
				}
			}
		}
		return events;
	}

	private void getDispatchers() {
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

	private void getMaxRequstSize() {
		try {
			String maxCountStr = System.getenv("sunbird_telemetry_request_max_count");
			if (StringUtils.isNotBlank(maxCountStr)) {
				defaultSize = Integer.parseInt(maxCountStr);
				ProjectLogger.log("Updated default telemetry_request_max_count to " + defaultSize,
						LoggerEnum.INFO.name());
			} else {
				ProjectLogger.log("Default telemetry_request_max_count is " + defaultSize, LoggerEnum.INFO.name());
			}
		} catch (Exception e) {
			ProjectLogger.log(
					"Error while setting default telemetry_request_max_count. Using default value: " + defaultSize,
					LoggerEnum.ERROR.name());
		}
	}
}
