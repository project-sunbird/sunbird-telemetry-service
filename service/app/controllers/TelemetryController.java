/**
 * 
 */
package controllers;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.telemetry.dispatcher.EkstepTelemetryDispatcher;

import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Result;

/**
 * This controller will handle all sunbird telemetry request data.
 * 
 * @author Mahesh Kumar Gangula
 *
 */
public class TelemetryController extends BaseController {

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
			request.setOperation("dispatchtelemetry");
			request.put("headers", request().headers());
			if ("application/json".equalsIgnoreCase(contentTypeHeader)) {
				ProjectLogger.log("Receiving telemetry in json format.", LoggerEnum.INFO.name());
				request.put("body", Json.stringify(request().body().asJson()));
			} else if ("application/zip".equalsIgnoreCase(contentTypeHeader)
					&& StringUtils.containsIgnoreCase(encodingHeader, "gzip")) {
				ProjectLogger.log("Receiving telemetry in gzip format.", LoggerEnum.INFO.name());
				byte[] body = request().body().asRaw().asBytes();
				request.put("body", body);
			} else {
				throw new ProjectCommonException(ResponseCode.invalidRequestData.getErrorCode(),
						"Please provide valid headers.", ResponseCode.CLIENT_ERROR.getResponseCode());
			}
			return actorResponseHandler(request, timeout, "", request());

		} catch (Exception e) {
			e.printStackTrace();
			return Promise.<Result>pure(createCommonExceptionResult(request().path(), e));
		}

	}
}
