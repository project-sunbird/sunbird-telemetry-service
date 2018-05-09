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
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controller.mapper.RequestMapper;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Result;

/**
 * This controller will handle all sunbird telemetry request data.
 * 
 * @author Manzarul
 *
 */
public class TelemetryController extends BaseController {

	private ObjectMapper mapper = new ObjectMapper();


	/**
	 * This method will receive the telemetry data and send it to EKStep to process
	 * it.
	 * 
	 * @return F.Promise<Result>
	 */
	public F.Promise<Result> save() {
		try {
			String contentTypeHeader = request().getHeader("content-type");
			String encodingHeader = request().getHeader("accept-encoding");
			Request request = new Request();
			request.setOperation("dispatchTelemetry");
			request.put("content-type", contentTypeHeader);
			if ("application/json".equalsIgnoreCase(contentTypeHeader)) {
				ProjectLogger.log("Receiving telemetry in json format.", LoggerEnum.INFO.name());
				Request body = (Request) RequestMapper.mapRequest(request().body().asJson(), Request.class);
				request.put("body", body);
			} else if ("application/zip".equalsIgnoreCase(contentTypeHeader)
					&& StringUtils.containsIgnoreCase(encodingHeader, "gzip")) {
				ProjectLogger.log("Receiving telemetry in gzip format.", LoggerEnum.INFO.name());
				byte[] body = request().body().asRaw().asBytes();
				request.put("body", body);
			} else {
				throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
						"Please provide valid headers.", ResponseCode.CLIENT_ERROR.getResponseCode());
			}
			request.setOperation("dispatchTelemetry");
			return actorResponseHandler(request, timeout, "", request());

		} catch (Exception e) {
			return Promise.<Result>pure(createCommonExceptionResult(request().path(), e));
		}

	}
}
