package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

public abstract class Component implements Figure, Serializable, Runnable {
	
	private static final long serialVersionUID = -5960950869184030220L;

	private String id;
	
	@JsonBackReference("cmp")
	private final Factory factory;
	
	private final PositionedShape positionedShape;
	
	private final String name;

	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}
	
	public Component() {
		this(null, null, null);
	}
	
	@JsonGetter("id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@JsonGetter("positionedShape")
	public PositionedShape getPositionedShape() {
		return positionedShape;
	}
	
	@JsonIgnore
	public Position getPosition() {
		PositionedShape shape = getPositionedShape();
		return shape != null ? shape.getPosition() : new Position(0, 0);
	}
	
	@JsonGetter("factory")
	protected Factory getFactory() {
		return factory;
	}

	@Override
	@JsonIgnore
	public int getxCoordinate() {
		PositionedShape shape = getPositionedShape();
		return shape != null ? shape.getxCoordinate() : 0;
	}

	protected boolean setxCoordinate(int xCoordinate) {
		PositionedShape shape = getPositionedShape();
		if ( shape != null && shape.setxCoordinate( xCoordinate ) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	@Override
	@JsonIgnore
	public int getyCoordinate() {
		PositionedShape shape = getPositionedShape();
		return shape != null ? shape.getyCoordinate() : 0;
	}

	protected boolean setyCoordinate(final int yCoordinate) {
		PositionedShape shape = getPositionedShape();
		if (shape != null && shape.setyCoordinate(yCoordinate) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	protected void notifyObservers() {
		Factory factory = getFactory();
		if (factory != null) {			
			factory.notifyObservers();
		}
	}
	
	@JsonGetter("name")
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		PositionedShape shape = getPositionedShape();
		return getClass().getSimpleName() + " [name=" + name != null ? name : "null" + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", shape=" + shape != null ? shape.toString() : "null";
	}
	
	@JsonIgnore
	public int getWidth() {
		PositionedShape shape = getPositionedShape();
		return shape != null ? shape.getWidth() : 0;
	}
	
	@JsonIgnore
	public int getHeight() {
		PositionedShape shape = getPositionedShape();
		return shape != null ? shape.getHeight() : 0;
	}
	
	public boolean behave() {
		return false;
	}
	
	@JsonIgnore
	public boolean isMobile() {
		return false;
	}
	
	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}
	
	public boolean overlays(final PositionedShape shape) {
		PositionedShape s = getPositionedShape();
		return s != null ? s.overlays(shape) : false;
	}
	
	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}
	
	@Override
	@JsonIgnore
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}
	
	@Override
	@JsonIgnore
	public Shape getShape() {
		return getPositionedShape();
	}
	
	@JsonIgnore
	public boolean isSimulationStarted() {
		Factory factory = getFactory();
		return factory != null ? factory.isSimulationStarted() : false;
	}

	@Override
	public void run() {
		while (isSimulationStarted()) {
			behave();
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
        }
	}
}
