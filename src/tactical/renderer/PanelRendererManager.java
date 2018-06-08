package tactical.renderer;

import org.newdawn.slick.Graphics;

import tactical.engine.message.Message;
import tactical.game.hudmenu.Panel;
import tactical.game.manager.Manager;

// This is a catch all for any sort of menu that is going to be rendered but not have focus
public class PanelRendererManager extends Manager
{
	public PanelRendererManager() {

	}

	public void render(Graphics graphics)
	{
		// displayMenubar();
		if (stateInfo.arePanelsDisplayed())
			for (Panel m : stateInfo.getPanels())
				m.render(stateInfo.getPaddedGameContainer(), graphics);
	}

	@Override
	public void initialize() {

	}

	@Override
	public void recieveMessage(Message message) {

	}
}
