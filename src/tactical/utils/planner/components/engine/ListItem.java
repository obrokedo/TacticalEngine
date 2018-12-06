package tactical.utils.planner.components.engine;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.GUIContext;

import tactical.utils.planner.components.SlickComponent;

public class ListItem extends SlickComponent {

	public ListItem(GUIContext container) {
		super(container);
	}

	@Override
	public void render(GUIContext gui, Graphics g) throws SlickException {
		g.drawString("CLOWNMAN", x, y);
	}	
}
