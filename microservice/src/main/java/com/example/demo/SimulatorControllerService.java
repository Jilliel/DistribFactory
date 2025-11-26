package com.example.demo;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
public class SimulatorControllerService {
	
	private static final Logger LOGGING = Logger.getLogger(SimulatorControllerService.class.getName());
	
	private Map<String, Factory> currents = new HashMap<>();
	
	@Autowired
	private KafkaTemplate<String, Factory> simulationEventTemplate;
	
	public static void main(String[] args) {
		SpringApplication.run(SimulatorControllerService.class, args);
	}
	
	@GetMapping("/start")
	public boolean startSimulation(@RequestParam(value="id", defaultValue="unk") String simulationID) {
        try {
        	if (!currents.containsKey(simulationID)) {   
        		LOGGING.info("No simulation with id " + simulationID + " loaded.");
        		Socket serverSocket = new Socket(InetAddress.getLocalHost(), 9999);
        		
        		OutputStream outputStream = serverSocket.getOutputStream();
        		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        		objectOutputStream.writeObject(simulationID);
        		
        		InputStream inputStream = serverSocket.getInputStream();
        		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        		Factory factory = (Factory) objectInputStream.readObject();
        		
        		FactoryModelChangedNotifier notifier = new KafkaFactoryModelChangeNotifier(factory, simulationEventTemplate);
        		factory.setNotifier(notifier);
        		currents.put(simulationID, factory);
        	
        		serverSocket.close();
        		LOGGING.info("Simulation with id " + simulationID + " has been loaded.");
        	}
            
            Factory factory = currents.get(simulationID);
            
            LOGGING.info("Starting the simulation");
            factory.startSimulation();
            LOGGING.info("Simulation started !");
            return true;
        }
        catch (IOException | ClassNotFoundException ex) {
        	LOGGING.log(Level.SEVERE, "An error occured while starting the simulation", ex);
            return false;
        }
	}
	
	@GetMapping("/get")
	public Factory getSimulation(@RequestParam(value="id", defaultValue="unk") String simulationID) {
		LOGGING.info("Retriving the simulation " + simulationID);
		if (currents.containsKey(simulationID)) {
			LOGGING.info("Simulation with ID " + simulationID + " retrived and sent");
			return currents.get(simulationID);
		} else {
			LOGGING.log(Level.WARNING, "No simulation found with id " + simulationID);
			return null;
		}
	}
	
	@GetMapping("/stop")
	public void stopSimulation(@RequestParam(value="id", defaultValue="unk") String simulationID) {
		LOGGING.info("Retriving the simulation " + simulationID);
		if (currents.containsKey(simulationID)) {
			LOGGING.info("Simulation with ID " + simulationID + " stopped");
			currents.get(simulationID).stopSimulation();
		} else {
			LOGGING.log(Level.WARNING, "No simulation found with id " + simulationID);
		}
	}
}
