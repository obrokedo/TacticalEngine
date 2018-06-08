package tactical.game.listener;

import tactical.engine.state.StateInfo;

public interface MenuListener
{
	public void valueSelected(StateInfo stateInfo, Object value);

	public void menuClosed();
}
