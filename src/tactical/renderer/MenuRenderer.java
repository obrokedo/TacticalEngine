package tactical.renderer;

import org.newdawn.slick.Graphics;

import tactical.engine.message.Message;
import tactical.game.manager.Manager;
import tactical.game.menu.Menu;

public class MenuRenderer extends Manager {

	@Override
	public void initialize() {

	}

	public void render(Graphics graphics)
	{
		if (stateInfo.areMenusDisplayed())
		for (Menu m : stateInfo.getMenus())
		{
			if (m.displayWhenNotTop() || m == stateInfo.getTopMenu())
				m.render(stateInfo.getPaddedGameContainer(), graphics);
		}
	}

	@Override
	public void recieveMessage(Message message)
	{

	}
}
