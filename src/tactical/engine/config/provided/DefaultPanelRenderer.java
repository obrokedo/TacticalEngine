package tactical.engine.config.provided;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.config.PanelRenderer;
import tactical.loading.ResourceManager;

public class DefaultPanelRenderer implements PanelRenderer {
	

	@Override
	public void initializeResources(ResourceManager rm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(int x, int y, int width, int height, Graphics graphics, Color color) {
		graphics.setColor(Color.gray);
		graphics.fillRect(x, y, width, height);
		
		if (color == null) {
			graphics.setColor(new Color(0, 32, 96));
		} else {
			graphics.setColor(color);
		}
		graphics.fillRect(x + 2, y + 2, width - 4, height - 4);		
		
		
	}
}
