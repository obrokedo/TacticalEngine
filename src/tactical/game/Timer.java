package tactical.game;

import java.util.function.BooleanSupplier;

/**
 * Keeps track of time that has passed in the game engine and indicates
 * when a specified amount of time has passed
 *
 * @author Broked
 *
 */
public class Timer
{
	
	private long timerUpdate;
	private long timerDelta;
	private BooleanSupplier performMethod;

	public Timer(long timerUpdate)
	{
		this.timerUpdate = timerUpdate;
	}

	public void update(long delta)
	{
		timerDelta += delta;
		if (performMethod != null) {
			while (perform()) {
				performMethod.getAsBoolean();
			}
		}
	}

	public boolean perform()
	{
		if (timerDelta >= timerUpdate)
		{
			timerDelta = 0;
			return true;
		}

		return false;
	}
}
