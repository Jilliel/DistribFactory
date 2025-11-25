package fr.tp.inf112.projects.robotsim.model;

import java.util.List;

import fr.tp.inf112.projects.canvas.controller.Observer;

public interface FactoryModelChangedNotifier {
	
	List<Observer> getObservers();
	
	void notifyObservers();
	
	boolean addObserver(Observer observer);
	
	boolean removeObserver(Observer observer);
}
