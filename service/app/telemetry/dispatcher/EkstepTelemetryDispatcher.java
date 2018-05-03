package telemetry.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.models.util.HttpUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.PropertiesCache;

public class EkstepTelemetryDispatcher implements IDispatcher {

	@Override
	public void dispatch(List<String> events) throws Exception {
		ProjectLogger.log("EkstepAPIDispatcher got events: " + events.size(), LoggerEnum.INFO.name());
		String requestBody = "{\"events\": " + "["+ StringUtils.join(events, ",") + "]" + "}";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(JsonKey.AUTHORIZATION, JsonKey.BEARER + System.getenv(JsonKey.EKSTEP_AUTHORIZATION));
		headers.put("Content-Type", "application/json");
		if (StringUtils.isBlank((String) headers.get(JsonKey.AUTHORIZATION))) {
			headers.put(JsonKey.AUTHORIZATION, PropertiesCache.getInstance().getProperty(JsonKey.EKSTEP_AUTHORIZATION));
		}
		System.out.println("Headers: " + headers);
		System.out.println("Body: "+ requestBody);
		String response = HttpUtil.sendPostRequest(getTelemetryUrl(), requestBody, headers);
		System.out.println("Response: "+ response);
		ProjectLogger.log("EkstepAPIDispatcher execution response: "+ response, LoggerEnum.INFO.name());
	}

	/**
	 * This method will return telemetry url.
	 * 
	 * @return
	 */
	private String getTelemetryUrl() {
		String telemetryBaseUrl = System.getenv(JsonKey.EKSTEP_BASE_URL);
		if (ProjectUtil.isStringNullOREmpty(telemetryBaseUrl)) {
			telemetryBaseUrl = PropertiesCache.getInstance().getProperty(JsonKey.EKSTEP_BASE_URL);
		}
		telemetryBaseUrl = telemetryBaseUrl
				+ PropertiesCache.getInstance().getProperty(JsonKey.EKSTEP_TELEMETRY_API_URL);
		ProjectLogger.log("telemetry url==" + telemetryBaseUrl);
		return telemetryBaseUrl;
	}

}
