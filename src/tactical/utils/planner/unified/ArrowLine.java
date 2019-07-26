package tactical.utils.planner.unified;

import java.awt.Color;
import java.awt.Graphics;

import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class ArrowLine implements UnifiedRenderable {
	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {
		g.setColor(Color.black);
		int drawY = UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 5;
		g.fillPolygon(new int[] {panelWidth / 2 - 10, panelWidth / 2 + 10,
		                      panelWidth / 2 + 10, panelWidth / 2 + 20,
		                      panelWidth / 2, panelWidth / 2 - 20,
		                      panelWidth / 2 - 10}, 
						new int[] { drawY, drawY, drawY + 10, drawY + 10,
								drawY + 25, drawY + 10, drawY + 10}, 7);
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 1;
	}
}
