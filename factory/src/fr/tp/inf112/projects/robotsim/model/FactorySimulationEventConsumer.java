package fr.tp.inf112.projects.robotsim.model;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import fr.tp.inf112.projects.robotsim.app.RemoteSimulatorController;

public class FactorySimulationEventConsumer {
	
	private static final Logger LOGGER = Logger.getLogger(FactorySimulationEventConsumer.class.getName());
	
	private final KafkaConsumer<String, String> consumer;
	
	private final RemoteSimulatorController controller;
	
	public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
		this.controller = controller;
		final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		this.consumer = new KafkaConsumer<>(props);
		final String topicName = SimulationServiceUtils.getTopicName((Factory) controller.getCanvas());
		System.out.println(topicName);
		this.consumer.subscribe(Collections.singletonList(topicName));
	}
	
	public class SimulationServiceUtils {
		public static final String BOOTSTRAP_SERVERS = "localhost:9092";
		private static final String GROUP_ID = "Factory-Simulation-Group";
		private static final String AUTO_OFFSET_RESET = "earliest";
		private static final String TOPIC = "simulation-";
		
		public static String getTopicName(final Factory factoryModel) {
			return TOPIC + factoryModel.getId().hashCode();
		}
		public static Properties getDefaultConsumerProperties() {
			final Properties props = new Properties();
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
			props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
			props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
			return props;
		}
	}
	
	public void consumeMessages() {
		try {
			while (controller.isAnimationRunning()) {
				final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				for (final ConsumerRecord<String, String> record : records) {
					LOGGER.info("Received JSON Factory text '" + record.value() + "'.");
					controller.setCanvas(record.value());
				}
			}
		} catch(Exception ex) {
			LOGGER.log(Level.WARNING, "Une erreur est survenue: " + ex);
		}
		finally {
			consumer.close();
		}
	}
}
