package tactical.utils.planner.unified;

import java.awt.Color;
import java.awt.Graphics;

import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class OrLine implements UnifiedRenderable {
	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {
		int drawY = UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT;
		g.setColor(Color.BLACK);
		g.drawString("OR", panelWidth / 2 - 10, drawY + 20);
	}

	@Override
	public int getHeight() {
		
		return 1;
	}
}