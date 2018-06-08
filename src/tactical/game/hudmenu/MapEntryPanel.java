package tactical.game.hudmenu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.ui.PaddedGameContainer;

public class MapEntryPanel extends Panel
{
	private String entryString = null;
	private char[] entryCharArray = null;
	private int maxTime = 0;
	private int elapsedTime = 0;
	private boolean waiting = true;
	private Color fadeColor = new Color(255, 255, 255, 255);

	public MapEntryPanel(String text) {
		super(PanelType.PANEL_MAP_ENTRY);
		entryString = text;
		entryCharArray = entryString.toCharArray();
		maxTime = 50 * entryCharArray.length + 2000;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {

		if (!waiting)
		{
			graphics.setFont(SPEECH_FONT);
			int previousLocation = 50;
			for (int i = 0; i < Math.min(entryCharArray.length, elapsedTime / 50); i++)
			{
				if ((elapsedTime / 50) == i + 1)
				{
					graphics.setColor(Color.darkGray);
				}
				else if ((elapsedTime / 50) == i + 2)
				{
					graphics.setColor(Color.lightGray);
				}
				else if (elapsedTime + 300 >= maxTime)
				{
					fadeColor.a = (maxTime - elapsedTime) / 300.0f;
					graphics.setColor(fadeColor);
				}
				else
					graphics.setColor(Color.white);
				graphics.drawString("" + entryCharArray[i], previousLocation, PaddedGameContainer.GAME_SCREEN_SIZE.height - 30);
				previousLocation += SPEECH_FONT.getWidth(entryCharArray[i] + "");
			}
		}
	}

	@Override
	public MenuUpdate update(int delta) {
		elapsedTime += delta;

		if (waiting && elapsedTime > 1000)
		{
			elapsedTime = 0;
			waiting = false;
		}

		if (!waiting && elapsedTime > maxTime)
		{
			return MenuUpdate.MENU_CLOSE;
		}
		return super.update(delta);
	}
}
