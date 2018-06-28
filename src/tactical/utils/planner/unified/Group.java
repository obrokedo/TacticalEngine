package tactical.utils.planner.unified;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class Group implements UnifiedRenderable {
	public List<UnifiedRenderable> groupRenderables;
	public boolean editable = true;
	public boolean showTitle = true;
	
	public Group(boolean editable, boolean showTitle) {
		super();
		this.editable = editable;
		this.showTitle = showTitle;
		groupRenderables = new ArrayList<>();
	}

	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {
		int height = getHeight();
		g.setColor(Color.white);
		// g.fillRect(10 + 50 * indent, y * 50 + yOffset, panelWidth - 20 - 50 * indent, (height + (editable ? 1 : 0)) * 50);
		g.setColor(Color.black);
		g.drawRect(10 + 50 * indent, y * 50 + UnifiedViewPanel.yOffset, panelWidth - 20 - 50 * indent, (height + (editable ? 1 : 0)) * 50);
		
		int idx = 0;
		for (UnifiedRenderable ur : groupRenderables) {
			int nestedIndent = indent;
			if (ur instanceof Group || ur instanceof NotSpecifiedLine) {
				nestedIndent++;
			}
			ur.render(nestedIndent, y + idx, panelWidth, g);
			
			idx += ur.getHeight();
		}
	}
	
	@Override
	public int getHeight() {
		int height = 0;
		for (UnifiedRenderable ur : groupRenderables)
			height += ur.getHeight();
		return height;
	}
}