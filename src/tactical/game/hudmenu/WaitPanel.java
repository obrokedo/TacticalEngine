package tactical.game.hudmenu;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.game.ui.PaddedGameContainer;

public class WaitPanel extends Panel
{
	public WaitPanel() {
		super(PanelType.PANEL_WAIT);
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics)
	{
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(160, 340, 630, 60, graphics, null);
		graphics.setColor(Panel.COLOR_FOREFRONT);
		graphics.drawString("Waiting for other players...", 180, 330);
	}
}
