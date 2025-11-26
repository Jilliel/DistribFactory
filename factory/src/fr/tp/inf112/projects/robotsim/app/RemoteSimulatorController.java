package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactorySimulationEventConsumer;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

public class RemoteSimulatorController extends SimulatorController {
	
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());

	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	
	private boolean running = false;
	
	public RemoteSimulatorController(CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
	}
	
	public RemoteSimulatorController(final Factory factoryModel, final CanvasPersistenceManager persistenceManager) {
		super(factoryModel, persistenceManager);
	}
	
	@Override
	public boolean isAnimationRunning() {
		return running;
	}
	
	@Override
	public void startAnimation() {
		try {
			LOGGER.info("Envoie d'une requête start au service.");
			final URI uri = new URI("http", null, "localhost", 8080, "/start", "id="+getCanvas().getId(), null);
			HttpResponse<String> response = HTTP_CLIENT.send(
					HttpRequest.newBuilder().uri(uri).GET().build(), 
					HttpResponse.BodyHandlers.ofString()
			);
			LOGGER.info("Réponse de la requête start: " + response.toString());
			running = true;
			updateViewer();
		} catch (URISyntaxException | IOException | InterruptedException ex) {
			return;
		}
	}

	@Override
	public void stopAnimation() {
		try {
			LOGGER.info("Envoie d'une requête stop au service.");
			final URI uri = new URI("http", null, "localhost", 8080, "/stop", "id="+getCanvas().getId(), null);
			HttpResponse<String> response = HTTP_CLIENT.send(
					HttpRequest.newBuilder().uri(uri).GET().build(), 
					HttpResponse.BodyHandlers.ofString()
			);
			LOGGER.info("Réponse de la requête stop: " + response.toString());
			running = false;
		} catch (URISyntaxException | IOException | InterruptedException ex) {
			return;
		}
	}
	
	public void setCanvas(final String canvasJson) {
		try {			
			final PolymorphicTypeValidator typeValidator =
					BasicPolymorphicTypeValidator.builder()
					.allowIfSubType(PositionedShape.class.getPackageName())
					.allowIfSubType(Component.class.getPackageName())
					.allowIfSubType(BasicVertex.class.getPackageName())
					.allowIfSubType(ArrayList.class.getName())
					.allowIfSubType(LinkedHashSet.class.getName())
					.build();
			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.activateDefaultTyping(typeValidator,ObjectMapper.DefaultTyping.NON_FINAL);
			SimpleModule module = new SimpleModule();
			module.addDeserializer(BasicVertex.class, new BasicVertexDeserializer());
			objectMapper.registerModule(module);
			Factory factory = objectMapper.readValue(canvasJson, Factory.class);
			
			List<Observer> observers = ((Factory) getCanvas()).getObservers();
			super.setCanvas(factory);
			for (final Observer observer: observers) {
				factory.addObserver(observer);
			}
			factory.notifyObservers();			
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Une erreur est survenue lors de la récupération de la Factory");
		}
	}
	
	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		FactorySimulationEventConsumer consumer = new FactorySimulationEventConsumer(this);
		consumer.consumeMessages();
	}

	protected Factory getFactory() {
		try {
			final URI uri = new URI("http", null, "localhost", 8080, "/get", "id="+getCanvas().getId(), null);
			HttpResponse<String> response = HTTP_CLIENT.send(
					HttpRequest.newBuilder().uri(uri).GET().build(), 
					HttpResponse.BodyHandlers.ofString()
			);
			final PolymorphicTypeValidator typeValidator =
					BasicPolymorphicTypeValidator.builder()
					.allowIfSubType(PositionedShape.class.getPackageName())
					.allowIfSubType(Component.class.getPackageName())
					.allowIfSubType(BasicVertex.class.getPackageName())
					.allowIfSubType(ArrayList.class.getName())
					.allowIfSubType(LinkedHashSet.class.getName())
					.build();
			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.activateDefaultTyping(typeValidator,ObjectMapper.DefaultTyping.NON_FINAL);
			SimpleModule module = new SimpleModule();
			module.addDeserializer(BasicVertex.class, new BasicVertexDeserializer());
			objectMapper.registerModule(module);
			Factory factory = objectMapper.readValue(response.body(), Factory.class);
			return factory;
		} catch (URISyntaxException | IOException | InterruptedException ex) {
			LOGGER.log(Level.WARNING, "Une erreur est survenue lors de la récupération de la Factory");
			return null;
		}
	}
}
