package tactical.game.menu;

import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel;
import tactical.game.input.UserInput;
import tactical.game.listener.MenuListener;

/**
 * A special type of panel container that allows for user interaction and generally
 * maintains a certain state about the users action. These menus will have user "focus"
 * which means that certain other actions will not be possible while one of these menus
 * is up.
 */
public abstract class Menu extends Panel
{
	protected MenuListener listener = null;

	public enum MenuUpdate
	{
		MENU_NO_ACTION,
		MENU_CLOSE,
		MENU_ACTION_SHORT,
		MENU_ACTION_LONG,
		MENU_NEXT_ACTION
	}

	public Menu(PanelType menuType) {
		super(menuType);
	}

	public abstract MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo);

	public MenuUpdate update(long delta, StateInfo stateInfo)
	{
		return MenuUpdate.MENU_NO_ACTION;
	}

	public MenuListener getMenuListener()
	{
		return listener;
	}

	public Object getExitValue()
	{
		return null;
	}

	public boolean displayWhenNotTop()
	{
		return true;
	}
	
	public void dispose() {
		
	}
}
