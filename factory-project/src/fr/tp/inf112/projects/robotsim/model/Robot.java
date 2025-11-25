package fr.tp.inf112.projects.robotsim.model;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	@Serial
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	private final Battery battery;
	
	private int speed;
	
	private List<Component> targetComponents;
	
	@JsonIgnore
	private transient Iterator<Component> targetComponentsIterator;
	
	private Component currTargetComponent;
	
	@JsonIgnore
	private transient Iterator<Position> currentPathPositionsIter;
	
	@JsonIgnore
	private transient boolean blocked;
	
	private Position memorizedTargetPosition;
	
	//Le Path Finder n'est pas pour à l'affichage
	@JsonIgnore
	private FactoryPathFinder pathFinder;

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		memorizedTargetPosition = null;
	}
	
	public Robot() {
		this(null, null, null, null, null);
	}
	
	@Override
	public String toString() {
		return super.toString() + " battery=" + battery != null ? battery.toString() : "null" + "]";
	}
	
	@JsonGetter("speed")
	protected int getSpeed() {
		return speed;
	}
	
	@JsonGetter("battery")
	public Battery getBattery() {
		return battery;
	}
	
	@JsonIgnore
	public FactoryPathFinder getPathFinder() {
		return pathFinder;
	}
	
	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	@JsonGetter("memorizedTargetPosition")
	public Position getMemorizedTargetPosition() {
		return memorizedTargetPosition;
	}
	
	@JsonGetter("targetComponents")
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	@JsonGetter("currTargetComponent")
	public Component getCurrTargetComponent() {
		return currTargetComponent;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	@JsonIgnore
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
			computePathToCurrentTargetComponent();
		} else if (currentPathPositionsIter == null) {
			computePathToCurrentTargetComponent();
		}
		
		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}

	private int moveToNextPathPosition() {
		final Motion motion = computeMotion();
		int displacement = motion == null ? 0 : getFactory().moveComponent(motion, this);

		if (displacement != 0) {
			notifyObservers();
		}
		else if (isLivelyLocked()) {
			final Position freeNeighbouringPosition = findFreeNeighbouringPosition();
			if (freeNeighbouringPosition != null) {
				this.memorizedTargetPosition = freeNeighbouringPosition;
				displacement = moveToNextPathPosition();
				computePathToCurrentTargetComponent();
			}
		}
		return displacement;
	}

	private Position findFreeNeighbouringPosition() {
		int xCur = this.getPosition().getxCoordinate();
		int yCur = this.getPosition().getyCoordinate();
		List<Position> nearPositions = List.of(
				new Position(xCur + getSpeed(), yCur),
				new Position(xCur, yCur + getSpeed()),
				new Position(xCur - getSpeed(), yCur),
				new Position(xCur, yCur - getSpeed())
		);
		for (Position near : nearPositions) {
			PositionedShape nearPositionShape = new RectangularShape(near.getxCoordinate(), near.getyCoordinate(), 2, 2);
			if (getFactory().getMobileComponentAt(nearPositionShape, this) == null && !getFactory().hasObstacleAt(nearPositionShape)) {
				return near;
			}
		}

		return null;
	}

	private void computePathToCurrentTargetComponent() {
		
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	private Motion computeMotion() {
		
		if (!currentPathPositionsIter.hasNext()) {
			// There is no free path to the target
			blocked = true;
			return null;
		}

		final Position targetPosition = getTargetPosition();
		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
														   targetPosition.getyCoordinate(),
				   										   2, 2);
		
		// If there is another robot, memorize the target position for the next run
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.memorizedTargetPosition = targetPosition;
			return null;
		}

		// Reset the memorized position
		this.blocked = false;
		this.memorizedTargetPosition = null;
		return new Motion(getPosition(), targetPosition);
	}
	
	@JsonIgnore
	private Position getTargetPosition() {
		// If a target position was memorized, it means that the robot was blocked during the last iteration 
		// so it waited for another robot to pass. So try to move to this memorized position otherwise move to  
		// the next position from the path
		return this.memorizedTargetPosition == null ? currentPathPositionsIter.next() : this.memorizedTargetPosition;
	}
	
	@JsonIgnore
	public boolean isLivelyLocked() {
		final Position memorizedTargetPosition = getMemorizedTargetPosition();
		if (memorizedTargetPosition == null) {
			return false;
		}

		final Component otherRobot = getFactory().
				getMobileComponentAt(memorizedTargetPosition, this);
		return otherRobot != null &&
				getPosition().equals(((Robot) otherRobot).getMemorizedTargetPosition());
	}

	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	@Override
	@JsonIgnore
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}
}
