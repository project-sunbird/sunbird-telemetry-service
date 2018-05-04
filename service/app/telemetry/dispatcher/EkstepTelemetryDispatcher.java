package telemetry.dispatcher;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.PropertiesCache;
import org.sunbird.common.models.util.RestUtil;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class EkstepTelemetryDispatcher implements IDispatcher {

	@Override
	public void dispatch(List<String> events) throws Exception {
		ProjectLogger.log("EkstepAPIDispatcher got events: " + events.size(), LoggerEnum.INFO.name());
		String requestBody = "{\"events\": " + "["+ StringUtils.join(events, ",") + "]" + "}";
		HttpResponse<JsonNode> apiResult = RestUtil.execute(Unirest.post(telemetryAPIURL()).body(requestBody));
		System.out.println("Response code: "+ apiResult.getStatus());
		System.out.println("Result: "+ apiResult.getBody().toString());
//		if(RestUtil.isSuccessful(apiResult)) {
//			
//		}
	}

	private String telemetryAPIURL() {
		String apiUrl = RestUtil.getBasePath();
		apiUrl += PropertiesCache.getInstance().getProperty(JsonKey.EKSTEP_TELEMETRY_API_URL);
		return apiUrl;
	}

}
