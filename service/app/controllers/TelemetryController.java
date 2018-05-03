/**
 * 
 */
package controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.telemetry.actor.TelemetryManagerActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controller.mapper.RequestMapper;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * This controller will handle all sunbird telemetry request data.
 * 
 * @author Manzarul
 *
 */
public class TelemetryController extends BaseController {

	private ObjectMapper mapper = new ObjectMapper();
	TelemetryManagerActor telemetryManager = new TelemetryManagerActor();
	private static int defaultSize = 1000;

	static {
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

	/**
	 * This method will receive the telemetry data and send it to EKStep to process
	 * it.
	 * 
	 * @return F.Promise<Result>
	 */
	public F.Promise<Result> save() {
		Request request = null;
		try {
			JsonNode requestData = null;
			String contentTypeHeader = request().getHeader("content-type");
			String encodingHeader = request().getHeader("accept-encoding");
			if ("application/json".equalsIgnoreCase(contentTypeHeader)) {
				ProjectLogger.log("Receiving telemetry in json format.", requestData, LoggerEnum.INFO.name());
				requestData = request().body().asJson();
				request = (Request) RequestMapper.mapRequest(requestData, Request.class);
			} else if ("application/zip".equalsIgnoreCase(contentTypeHeader)
					&& StringUtils.containsIgnoreCase(encodingHeader, "gzip")) {
				ProjectLogger.log("Receiving telemetry in gzip format.", LoggerEnum.INFO.name());
				request = getRequest(request().body().asRaw().asBytes());
			} else {
				throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
						"Please provide valid headers.", ResponseCode.CLIENT_ERROR.getResponseCode());
			}

			Response response = telemetryManager.save(request);
			Result result = createCommonResponse(request().path(), response);
			return Promise.<Result>pure(result);
		} catch (Exception e) {
			return Promise.<Result>pure(createCommonExceptionResult(request().path(), e));
		}

	}

	private Request getRequest(byte[] bytes) {
		List<String> allEvents = new ArrayList<String>();
		try {
			InputStream is = new ByteArrayInputStream(bytes);
			BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while ((temp = bfReader.readLine()) != null) {
				Map<String, Object> row = mapper.readValue(temp, Map.class);
				Map<String, Object> data = (Map<String, Object>) row.get("data");
				if (data != null) {
					List<Map<String, Object>> events = (List<Map<String, Object>>) data.get("events");
					if (null != events && !events.isEmpty()) {
						for (Map<String, Object> event : events) {
							if (null != event)
								allEvents.add(mapper.writeValueAsString(event));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
					"Please provide valid binary gzip file. File structure is invalid.",
					ResponseCode.CLIENT_ERROR.getResponseCode());
		}

		if (allEvents.isEmpty()) {
			throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
					"Please provide valid binary gzip file. File is empty.",
					ResponseCode.CLIENT_ERROR.getResponseCode());
		} else if (allEvents.size() > defaultSize) {
			throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
					"Too many events to process. Max limit for a request is " + defaultSize,
					ResponseCode.CLIENT_ERROR.getResponseCode());
		} else {
			Request request = new Request();
			Map<String, Object> reqMap = new HashMap<String, Object>();
			reqMap.put("events", allEvents);
			request.setRequest(reqMap);
			return request;
		}
	}
}
