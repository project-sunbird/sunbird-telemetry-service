package org.sunbird.kafka;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public class KafkaClient {

	private static String BOOTSTRAP_SERVERS = System.getenv("sunbird_telemetry_kafka_servers_config");
	private static String topic = System.getenv("sunbird_telemetry_kafka_topic");
	private static Producer<Long, String> producer;
	
	static {
		if (StringUtils.isNotBlank(BOOTSTRAP_SERVERS) && StringUtils.isNotBlank(topic)) {
			createProducer();
		}
	}
	
	private static void createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaClientProducer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<Long, String>(props);
	}

	public static Producer<Long, String> getProducer() {
		return producer;
	}
	
	public static String getTopic() {
		return topic;
	}
}
