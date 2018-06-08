package tactical.game.trigger;

import tactical.engine.state.StateInfo;

public interface Conditional {
	//TODO Realllllly not happy with this, but the alternative would be to put the MovingSprite in the state info and
	// check that way.
	public boolean conditionIsMet(String location, boolean locationEntered, boolean immediate, 
			boolean onMapLoad, boolean searching, StateInfo stateInfo);
}
