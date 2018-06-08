package tactical.game.listener;

import tactical.engine.state.StateInfo;
import tactical.game.input.UserInput;

public interface KeyboardListener 
{
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo);
}
