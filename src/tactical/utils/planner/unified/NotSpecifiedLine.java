package tactical.utils.planner.unified;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

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
	public Group parentGroup;
	// This is ugly as shit, but I don't wanna deal with it otherwise
	public int indent, y;
	public MapObject mo;
	public PlannerContainer pc;
	public PlannerLine pl;
	public int idx;
	public UnifiedViewPanel uvp;

	public NotSpecifiedLine(boolean forTrigger, boolean optional, 
			MapObject mo, PlannerContainer pc, PlannerLine pl, int idx, UnifiedViewPanel uvp) {
		super();
		this.forTrigger = forTrigger;
		this.optional = optional;
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
		g.fillRect(10 + 50 * indent, y * 50 + UnifiedViewPanel.yOffset, panelWidth - 20 - 50 * indent, 50);
		g.setColor(Color.black);
		g.drawRect(10 + 50 * indent, y * 50 + UnifiedViewPanel.yOffset, panelWidth - 20 - 50 * indent, 50);		
		
		if (forTrigger) {
			if (optional) {
				renderUndefinedString("You may provide an optional trigger for the previous action or", 230, 170, panelWidth, y, g);
			} else {
				renderUndefinedString("Please specifiy a trigger for the previous action or", 215, 110, panelWidth, y, g);					
			}
				
		} else {
			if (optional) {
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
				panelWidth / 2 - firstXOffset, UnifiedViewPanel.yOffset + y * 50 + 30);
		
		g.setColor(Color.BLUE);
		
		g.drawString("Create one now", 
				panelWidth / 2 + secondXOffset, UnifiedViewPanel.yOffset + y * 50 + 30);
	}
	
	public void checkClick(int x, int y) {
		Rectangle r = new Rectangle(11 + 50 * indent, this.y * 50 + UnifiedViewPanel.yOffset + 1, uvp.getRenderPanel().getWidth() - 22 - 50 * indent, 48);
		JPanel p = new JPanel(new BorderLayout());
		
		
		if (r.contains(x, y)) {
			if (forTrigger) {
				createNewContainer(PlannerFrame.TAB_TRIGGER, "searchtrigger");							
			}
			else {					
				createNewContainer(PlannerFrame.TAB_TEXT, "textid");					
			}
		}
		
	}
	
	private void createNewContainer(int tabIdx, String mapObjectString) {
		PlannerContainer newPC = uvp.getTabsWithMapRefs().get(tabIdx).addNewContainer();
		if (newPC != null) {
			 uvp.showScrollableOptionPane(new SingleEditPanel(newPC), false);
			// Must be a search area
			if (mo != null) {
				mo.getParams().put(mapObjectString, "" + ( uvp.getTabsWithMapRefs().get(tabIdx).getListPC().size() - 1));
			} else if (pl != null) {
				pl.getValues().set(idx, new PlannerReference((String) newPC.getDefLine().getValues().get(0)));
			} else if (pc != null) {
				pc.getDefLine().getValues().set(idx, new PlannerReference((String) newPC.getDefLine().getValues().get(0)));
			} else {
				JOptionPane.showMessageDialog(uvp.getRenderPanel(), "Due to the complexity of the 'Run Triggers' command you will need to set the newly created trigger yourself");
			}
			
			for (PlannerLine pl : newPC.getLines()) {
				pl.commitChanges();
			}
			newPC.getDefLine().commitChanges();
			
			uvp.setupPanel((String)  uvp.getDrivers().getSelectedItem());
		}	
	}

	@Override
	public int getHeight() {			
		return 1;
	}
}