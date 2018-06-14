package tactical.utils.planner.unified;

import java.awt.Color;
import java.awt.Graphics;

import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class OrLine implements UnifiedRenderable {
	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {
		int drawY = UnifiedViewPanel.yOffset + y * 50;
		g.setColor(Color.BLACK);
		g.drawString("OR", panelWidth / 2 - 10, drawY + 30);
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 1;
	}
}