package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;
	
	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	@JsonManagedReference("cmp")
    private final List<Component> components;
	
	@JsonIgnore
	private transient boolean simulationStarted;
	
	@JsonIgnore
	private transient FactoryModelChangedNotifier notifier;
	
	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);
		
		components = new ArrayList<>();
		notifier = new LocalFactoryModelChangedNotifier(); 
		simulationStarted = false;
	}
	
	public Factory() {
		this(0, 0, null);
	}
	
	public void setNotifier(FactoryModelChangedNotifier notifier) {
		this.notifier = notifier;
	}
	
	@JsonIgnore
	public List<Observer> getObservers() {
		if (notifier != null) {
			return notifier.getObservers();
		}
		return new ArrayList<>();
		
	}

	@Override
	public boolean addObserver(Observer observer) {
		if (notifier == null) {
			notifier = new LocalFactoryModelChangedNotifier();
		}
		return notifier.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
		if (notifier != null) {
			return notifier.removeObserver(observer);
		}
		return false;
	}
	
	@Override
	public void notifyObservers() {
		if (notifier != null) {
			notifier.notifyObservers();
		}
	}
	
	public boolean addComponent(final Component component) {
		if (components.add(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}
	
	@JsonGetter("components")
	public List<Component> getComponents() {
		return components;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@JsonIgnore
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}

	@Override
	public String toString() {
		return super.toString() + " components=" + components + "]";
	}
	
	@JsonIgnore
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	public void startSimulation() {
		if (!isSimulationStarted()) {
			this.simulationStarted = true;
			notifyObservers();
			behave();
		}
	}

	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		for (final Component component : getComponents()) {
			Thread componentThd = new Thread(component);
			componentThd.start();
		}
		return true;
	}

	@Override
	@JsonIgnore
	public Style getStyle() {
		return DEFAULT;
	}
	
	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	@JsonIgnore
	public Component getMobileComponentAt(	final Position position,
											final Component ignoredComponent) {
		if (position == null) {
			return null;
		}
		
		return getMobileComponentAt(new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2), ignoredComponent);
	}
	
	@JsonIgnore
	public Component getMobileComponentAt(	final PositionedShape shape,
											final Component ignoredComponent) {
		if (shape == null) {
			return null;
		}
		
		for (final Component component : getComponents()) {
			if (component != ignoredComponent && component.isMobile() && component.overlays(shape)) {
				return component;
			}
		}
		
		return null;
	}

	public synchronized int moveComponent(final Motion motion, final Component component) {
		if (getMobileComponentAt(motion.getTargetPosition(), component) == null)
			return motion.moveToTarget();
		return 0;
	}
}
