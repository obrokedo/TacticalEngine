package tactical.game.input;

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

/**
 * It's questionable if this class is really necessary... It's not terribly heavy
 * so maybe it's not to big a deal. I'm just not sure what problem we were looking to solve
 * when we made this. My guess is that we were probably dropping key inputs because input wasn't polling enough.
 * Alternatively we may have been attempting to not need to check whether each character was pressed each time.
 * Potentially clearing the input each update and then cache inputs that have already been checked...
 * 
 * @author user
 *
 */
public class UserInput implements KeyListener
{
	private ArrayList<Integer> keysHeld;
	private int updateDelta = 0;
	private static final int UPDATE_TIME = 50;
	private static final int DIRECTION_KEYS[] = {Input.KEY_RIGHT, Input.KEY_LEFT, Input.KEY_UP, Input.KEY_DOWN};

	public UserInput() {
		super();
		keysHeld = new ArrayList<Integer>();
	}

	public void update(int delta, Input realInput)
	{
		updateDelta += delta;
		if (updateDelta >= UPDATE_TIME)
		{
			updateDelta -= UPDATE_TIME;
			
			// There are some shitty timing windows where a
			// key can get in a pressed state even after release.
			// Make sure keys held are still held
			Iterator<Integer> keysHeldId = keysHeld.iterator();
			while(keysHeldId.hasNext()) {
				if (!realInput.isKeyDown(keysHeldId.next()))
					keysHeldId.remove();
			}
		}
	}
	
	public void setInitialMovementInput(Input input) {
		checkKeyPressed(input, KeyMapping.BUTTON_DOWN);
		checkKeyPressed(input, KeyMapping.BUTTON_RIGHT);
		checkKeyPressed(input, KeyMapping.BUTTON_LEFT);
		checkKeyPressed(input, KeyMapping.BUTTON_UP);
	}
	
	private void checkKeyPressed(Input input, int key) {
		if (input.isKeyDown(key)) {
			this.keysHeld.add(key);
		}
	}

	public void clear()
	{
		keysHeld.clear();
	}

	public boolean isKeyDown(int keyCode)
	{
		return keysHeld.contains(keyCode);
	}

	public int getMostRecentDirection()
	{
		int recent = -1;
		int recentIndex = -1;
		for (int dir : DIRECTION_KEYS)
		{
			if (keysHeld.indexOf(dir) > recentIndex)
			{
				recentIndex = keysHeld.indexOf(dir);
				recent = dir;
			}
		}

		return recent;
	}

	@Override
	public void setInput(Input input) {
	}

	@Override
	public boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void inputEnded() {

	}

	@Override
	public void inputStarted() {

	}
	
	@Override
	public void keyPressed(int key, char c) {
		keysHeld.add(key);
	}

	@Override
	public void keyReleased(int key, char c) {
		keysHeld.remove((Object) key);
	}
}
