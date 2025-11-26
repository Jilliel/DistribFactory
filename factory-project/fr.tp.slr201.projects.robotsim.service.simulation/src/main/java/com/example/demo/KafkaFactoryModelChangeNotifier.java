package com.example.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

public class KafkaFactoryModelChangeNotifier implements FactoryModelChangedNotifier {
	
	private final Factory factory;
	
	private final KafkaTemplate<String, Factory> simulationEventTemplate;
	
	public KafkaFactoryModelChangeNotifier(Factory factory, KafkaTemplate<String, Factory> template) {
		super();
		this.factory = factory;
		this.simulationEventTemplate = template;
	}

	@Override
	public List<Observer> getObservers() {
		return null;
	}

	@Override
	//Cette est bien appelé par la factory simulée
	public void notifyObservers() {
		final Message<Factory> factoryMessage = MessageBuilder
				.withPayload(factory)
				.setHeader(KafkaHeaders.TOPIC, "simulation-" + factory.getId().hashCode())
				.build();
		final CompletableFuture<SendResult<String, Factory>> sendResult = simulationEventTemplate.send(factoryMessage);
		sendResult.whenComplete((result, ex) -> {
			if (ex != null) {
				throw new RuntimeException(ex);
			}
		});
	}
	
	@Override
	public boolean addObserver(Observer observer) {
		return true;
	}

	@Override
	public boolean removeObserver(Observer observer) {
		return true;
	}

}
