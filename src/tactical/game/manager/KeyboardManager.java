package tactical.game.manager;

import tactical.engine.message.Message;
import tactical.game.listener.KeyboardListener;

/**
 * Manager to pass along keyboard input to listeners and delay keyboard 
 * input between KeyboardListeners so actions aren't triggered
 * multiple times when a keyboard key is pressed.
 * 
 * @author Broked
 */
public class KeyboardManager extends Manager
{
	@Override
	public void initialize() {
		
	}
	
	public void update()
	{
		KeyboardListener kl = stateInfo.getKeyboardListener();
		
		if (kl != null)
		{
			if (System.currentTimeMillis() > stateInfo.getInputDelay())
				if (kl.handleKeyboardInput(stateInfo.getInput(), stateInfo))
					stateInfo.setInputDelay(System.currentTimeMillis() + 200);
		}
	}

	@Override
	public void recieveMessage(Message message) {
		
	}
}
