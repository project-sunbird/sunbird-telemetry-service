package org.sunbird.telemetry.dispatcher;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.kafka.KafkaClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author mahesh
 *
 */

@ActorConfig(tasks = { "dispatchtelemetrytokafka" }, asyncTasks = {})
public class KafkaTelemetryDispatcher extends BaseActor {

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		if ("dispatchtelemetrytokafka".equals(operation)) {
			List<String> events = getEvents(request);
			dispatchEvents(events);
			Response response = new Response();
			response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
			sender().tell(response, self());
		} else {
			onReceiveUnsupportedMessage(operation);
		}
	}

	private void dispatchEvents(List<String> events) {
		for (String event : events) {
			ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(KafkaClient.getTopic(), event);
			KafkaClient.getProducer().send(record);
		}
	}

	private List<String> getEvents(Request request) throws Exception {
		List<String> events = null;
		Object body = request.get("body");
		if (body instanceof String) {
			events = getEvents((String) body);
		} else if (body instanceof byte[]) {
			events = getEvents((byte[]) body);
		} else {
			emptyRequestError("Please provide valid contnet-type.");
		}

		return events;
	}

	private void emptyRequestError(String message) throws ProjectCommonException {
		throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(), message,
				ResponseCode.CLIENT_ERROR.getResponseCode());
	}

	private List<String> getEvents(String body) throws Exception {
		if (StringUtils.isBlank(body))
			emptyRequestError("Please provide valid request body. request body is empty.");

		Request request = mapper.readValue(body, Request.class);
		List<String> events = new ArrayList<String>();
		if (request != null && MapUtils.isNotEmpty(request.getRequest())) {
			List<Object> objList = (List<Object>) request.getRequest().get("events");
			if (CollectionUtils.isNotEmpty(objList)) {
				for (Object obj : objList) {
					events.add(mapper.writeValueAsString(obj));
				}
			}
		}
		return events;
	}

	private List<String> getEvents(byte[] body) throws Exception {
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

}
