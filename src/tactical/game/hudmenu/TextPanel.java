package tactical.game.hudmenu;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.game.ui.PaddedGameContainer;

public class TextPanel extends Panel
{
	public static int LOCATION_RIGHT = 0;
	public static int LOCATION_CENTER = 1;

	private String text;
	private int location;

	public TextPanel(String text, int location) {
		super(PanelType.PANEL_TEXT);
		this.text = text;

		this.location = location;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics)
	{
		int x = 275;

		if (location == LOCATION_CENTER)
			x = 140;
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, PaddedGameContainer.GAME_SCREEN_SIZE.height - 140, PaddedGameContainer.GAME_SCREEN_SIZE.width - 280 , 135, graphics, null);
		graphics.setColor(Panel.COLOR_FOREFRONT);
		graphics.drawString(text, x + 15, PaddedGameContainer.GAME_SCREEN_SIZE.height - 125);
	}
}
