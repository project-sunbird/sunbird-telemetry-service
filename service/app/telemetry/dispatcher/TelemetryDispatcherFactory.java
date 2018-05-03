package telemetry.dispatcher;

import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.responsecode.ResponseCode;

public class TelemetryDispatcherFactory {

	private static IDispatcher kafka = new KafkaDispatcher();
	private static IDispatcher ekstep = new EkstepTelemetryDispatcher();

	public static IDispatcher getDispatcher(String name) {
		IDispatcher dispatcher = null;
		switch (name) {
		case "kafka":
			dispatcher = kafka;
			break;
		case "ekstep":
			dispatcher = ekstep;
			break;
		default:
			throw new ProjectCommonException("INVALID_TELEMETRY_DISPACHER", "Unknown dispatcher distination found.",
					ResponseCode.SERVER_ERROR.getResponseCode());
		}
		return dispatcher;
	}
}
