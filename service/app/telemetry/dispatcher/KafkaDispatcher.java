package telemetry.dispatcher;

import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.kafka.KafkaClient;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public class KafkaDispatcher implements IDispatcher {

	@Override
	public void dispatch(List<String> events) throws Exception {
		ProjectLogger.log("KafkaDispatcher got events: " + events.size(), LoggerEnum.INFO.name());
		for (String event : events) {
			ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(KafkaClient.getTopic(), event);
			KafkaClient.getProducer().send(record);
		}
	}
}
