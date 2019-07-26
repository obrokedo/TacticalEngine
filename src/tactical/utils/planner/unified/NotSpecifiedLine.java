package tactical.utils.planner.unified;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tactical.map.MapObject;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerReference;
import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class NotSpecifiedLine implements UnifiedRenderable {

	public boolean forTrigger = false;
	public boolean optional = false;
	public boolean additional = false;
	public Group parentGroup;
	// This is ugly as shit, but I don't wanna deal with it otherwise
	public int indent, y;
	public MapObject mo;
	public PlannerContainer pc;
	public PlannerLine pl;
	public int idx;
	public UnifiedViewPanel uvp;

	public NotSpecifiedLine(boolean forTrigger, boolean optional, 
			boolean additional, MapObject mo, PlannerContainer pc, PlannerLine pl, int idx, UnifiedViewPanel uvp) {
		super();
		this.forTrigger = forTrigger;
		this.optional = optional;
		this.additional = additional;
		this.mo = mo;
		this.pc = pc;
		this.pl = pl;
		this.idx = idx;
		this.uvp = uvp;
	}

	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.setColor(Color.white);
		g.fillRect(10 + 50 * indent, y * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset, panelWidth - 20 - 50 * indent, RENDERABLE_HEIGHT);
		g.setColor(Color.black);
		g.drawRect(10 + 50 * indent, y * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset, panelWidth - 20 - 50 * indent, RENDERABLE_HEIGHT);		
		
		if (forTrigger) {
			if (optional) {
				if (additional)
					renderUndefinedString("You may provide an additional trigger for the previous action or", 240, 170, panelWidth, y, g);
				else
					renderUndefinedString("You may provide an optional trigger for the previous action or", 230, 170, panelWidth, y, g);
			} else {
				renderUndefinedString("Please specifiy a trigger for the previous action or", 215, 110, panelWidth, y, g);					
			}
				
		} else {
			if (optional) {
				if (additional)
					renderUndefinedString("You may provide an additional speech for the previous action or", 175, 98, panelWidth, y, g);
				else
				renderUndefinedString("You may provide an optional speech for the previous action or", 175, 98, panelWidth, y, g);					
			} else {
				renderUndefinedString("Please specifiy a speech for the previous action or", 210, 115, panelWidth, y, g);
			}
		}
		
		this.indent = indent;
		this.y = y;
	}
	
	public void renderUndefinedString(String text, int firstXOffset, int secondXOffset, int panelWidth, int y, Graphics g) {
		g.drawString(text, 
				panelWidth / 2 - firstXOffset, UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 20);
		
		g.setColor(Color.BLUE);
		
		g.drawString("Create one now", 
				panelWidth / 2 + secondXOffset, UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 20);
	}
	
	public void checkClick(int x, int y) {
		Rectangle r = new Rectangle(11 + 50 * indent, this.y * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1, uvp.getRenderPanel().getWidth() - 22 - 50 * indent, RENDERABLE_HEIGHT - 2);
		JPanel p = new JPanel(new BorderLayout());
		
		
		if (r.contains(x, y)) {
			if (forTrigger) {
				uvp.createNewContainer(PlannerFrame.TAB_TRIGGER,  
						pc -> attachNewContainerToParent(PlannerFrame.TAB_TRIGGER, "searchtrigger", pc));							
			}
			else {					
				uvp.createNewContainer(PlannerFrame.TAB_TEXT,
						pc -> attachNewContainerToParent(PlannerFrame.TAB_TEXT, "textid", pc));					
			}
		}
		
	}
	
	public void attachNewContainerToParent(int tabIdx, String mapObjectString, PlannerContainer newPC) {
		// Must be a search area
		if (mo != null) {
			mo.getParams().put(mapObjectString, "" + ( uvp.getTabsWithMapRefs().get(tabIdx).getListPC().size() - 1));
		} else if (pl != null) {
			updateReference(pl, idx, newPC);
		} else if (pc != null) {
			updateReference(pc.getDefLine(), idx, newPC);
		} else {
			JOptionPane.showMessageDialog(uvp.getRenderPanel(), "Due to the complexity of the 'Run Triggers' command you will need to set the newly created trigger yourself");
		}
	}
	
	private void updateReference(PlannerLine pl, int index, PlannerContainer newPC) {
		if (pl.getValues().get(idx) instanceof PlannerReference) {
			pl.getValues().set(idx, new PlannerReference((String) newPC.getDefLine().getValues().get(0)));
		} else {
			ArrayList<PlannerReference> values = (ArrayList<PlannerReference>) pl.getValues().get(idx);
			if (values.size() == 1 && values.get(0).getName().length() == 0)
				values.set(0, new PlannerReference((String) newPC.getDefLine().getValues().get(0)));
			else
				values.add(new PlannerReference((String) newPC.getDefLine().getValues().get(0)));
		}
	}

	@Override
	public int getHeight() {			
		return 1;
	}
}