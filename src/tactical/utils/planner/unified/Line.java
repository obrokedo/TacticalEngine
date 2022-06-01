package tactical.utils.planner.unified;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;

import tactical.map.MapObject;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerContainerDef;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerReference;
import tactical.utils.planner.unified.UnifiedViewPanel.UnifiedRenderable;

public class Line implements UnifiedRenderable {
	public String text;
	public boolean removable;
	public MapObject mo;
	public PlannerContainer pc;
	public PlannerLine pl;
	// This is ugly as shit, but I don't wanna deal with it otherwise
	public int indent, y;
	public UnifiedViewPanel uvp;
	public boolean conditional = false;
	public int height = 1;
	public ArrayList<String> actions = new ArrayList<String>();
	public ArrayList<QuestButton> quests = new ArrayList<QuestButton>();
	public ArrayList<Line> childSpeechLine = new ArrayList<>();
	public boolean highlight = false;
	
	private class QuestButton {
		Rectangle bounds;
		String text;
		boolean marked;
		
		public QuestButton(Rectangle bounds, String text, boolean marked) {
			super();
			this.bounds = bounds;
			this.text = text;
			this.marked = marked;
		}		
		
		public void render(Graphics g) {
			if (marked)
				g.setColor(Color.green);
			else
				g.setColor(Color.WHITE);
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g.setColor(Color.BLACK);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g.drawString(text, bounds.x + 5, bounds.y + 18);
				
		}
	}
	
	public Line(String text, MapObject mo, PlannerContainer pc, PlannerLine pl,
			UnifiedViewPanel uvp) {
		super();
		this.text = text;
		this.mo = mo;
		this.pc = pc;
		this.pl = pl;
		this.uvp = uvp;		
		
		if (this.text.startsWith("Execute trigger:")) {
			if (pc != null) {
				conditional = hasValuesSpecified(1) || hasValuesSpecified(2);
				if (conditional)
					height++;
			}
			
			for (PlannerLine p : pc.getLines()) {
				actions.add(p.getPlDef().getName());				
			}
		} else if (this.text.startsWith("Show speech:") && pc.getLines().size() > 1) {
			ArrayList<PlannerReference> questsPR = new ArrayList<>();
			for (int i = 0; i < pc.getLines().size(); i++) {
				questsPR.addAll((ArrayList<PlannerReference>) pc.getLines().get(i).getValues().get(0));
				questsPR.addAll((ArrayList<PlannerReference>) pc.getLines().get(i).getValues().get(1));
			}
			
			HashSet<String> uniqueQuests = new HashSet<String>();
			for (PlannerReference pr : questsPR)
				uniqueQuests.add(pr.getName());
			
			height += 2;
			
			int count = 0;
			for (String quest : uniqueQuests) {
				if (quest.isEmpty())
					continue;
				quests.add(new QuestButton(new Rectangle(30 + 50 * indent + 185 * count++, (y + 2) * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1, 180, RENDERABLE_HEIGHT - 4), 
						quest, false));
			}
			
			determineActiveChild();
		}
		
		height += actions.size();
	}
	
	@Override
	public void render(int indent, int y, int panelWidth, Graphics g) {		
		this.y = y;
		
		g.setFont(g.getFont().deriveFont(Font.BOLD, 13));
		
		if (highlight)
			g.setColor(Color.GREEN);
		else
			g.setColor(Color.WHITE);
		g.fillRect(11 + 50 * indent, y * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1, panelWidth - 22 - 50 * indent, RENDERABLE_HEIGHT * height - 2);		
		
		g.setColor(Color.black);
		
		g.drawString(text, 30 + 50 * indent, UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 20);
		
		if (this.text.startsWith("Show speech:") && pc.getLines().size() > 1) {
			g.setColor(new Color(128, 179, 255));
			g.fillRect(11 + 50 * indent, (y + 1) * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1, panelWidth - 22 - 50 * indent, RENDERABLE_HEIGHT * 2);
			g.setColor(Color.black);
			g.drawString("This 'speech' has multiple elements. The speech that will be shown is based on which appears first in the list as well as the quests requirements. "
					+ "Use the buttons below to view which speech will be shown given a quest combination.", 30 + 50 * indent, UnifiedViewPanel.yOffset + (y + 1) * RENDERABLE_HEIGHT + 20);
			
			if (quests.size() == 0) {
				g.drawString("There are no quests configured for this speech! The first dialogue option will ALWAYS be used.", 30 + 50 * indent, UnifiedViewPanel.yOffset + (y + 2) * RENDERABLE_HEIGHT + 20);
				childSpeechLine.get(0).highlight = true;
			}
			
			for (QuestButton qb : quests) {
				qb.bounds.y = (y + 2) * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1;
				qb.render(g);
			}
		}
		
		g.setColor(Color.DARK_GRAY);
		int count = 0;
		for (String action : actions) {
			g.drawString("    " + action, 30 + 50 * indent, UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 18 + RENDERABLE_HEIGHT * ++count);
		}
		
		if (conditional) {						
			g.setColor(Color.red);
			g.drawString("(This trigger may not run because it has required/excluded quests)", 30 + 50 * indent, 
					UnifiedViewPanel.yOffset + y * RENDERABLE_HEIGHT + 18 + RENDERABLE_HEIGHT * ++count);
		}		
		
		this.indent = indent;		
	}

	protected boolean hasValuesSpecified(int valueIdx) {
		boolean conditional = false;
		ArrayList<PlannerReference> quests = (ArrayList<PlannerReference>) pc.getDefLine().getValues().get(valueIdx);
		if (quests.size() >= 1 && quests.get(0).getName().length() > 0) {
			conditional = true;
		}
		return conditional;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	private void determineActiveChild() {
		ArrayList<String> active = new ArrayList<>();
		ArrayList<String> unactive = new ArrayList<>();
		
		for (QuestButton qb : quests) {
			if (qb.marked)
				active.add(qb.text);
			else
				unactive.add(qb.text);
		}
		
		boolean found = false;
		for (Line subLine : childSpeechLine) {
			if (found) {
				subLine.highlight = false;
				continue;
			}
			
			ArrayList<String> required = new ArrayList<>();
			ArrayList<String> cannotBeDone = new ArrayList<>();
			
			
			for (PlannerReference pr : (ArrayList<PlannerReference>) subLine.pl.getValues().get(0)) {
				if (pr.getName().isEmpty())
					continue;
				required.add(pr.getName());
			}
			for (PlannerReference pr : (ArrayList<PlannerReference>) subLine.pl.getValues().get(1)) {
				if (pr.getName().isEmpty())
					continue;
				cannotBeDone.add(pr.getName());
			}
			
			if ((required.size() == 0 || active.containsAll(required)) 
					&& (cannotBeDone.size() == 0 || unactive.containsAll(cannotBeDone))) {
				subLine.highlight = true;
				found = true;
			} else {
				subLine.highlight = false;
			}
		}
	}
	
	private boolean checkButtonClicked(int x, int y) {
		// Handle clicking quest buttons
			boolean buttonClicked = false;
			
			for (int i = 0; i < quests.size(); i++) {
				QuestButton qb = quests.get(i);
				if (qb.bounds.contains(x, y)) {												
					quests.get(i).marked = !quests.get(i).marked;
					
					buttonClicked = true;
					
				}
			}				
			
			if (buttonClicked) {				
				determineActiveChild();
				return true;
			}
		return false;
	}
	
	public void checkClick(int x, int y) {
		
		if (checkButtonClicked(x, y))
			return;
		
		Rectangle r = new Rectangle(11 + 50 * indent, this.y * RENDERABLE_HEIGHT + UnifiedViewPanel.yOffset + 1, 
				uvp.getRenderPanel().getWidth() - 22 - 50 * indent, RENDERABLE_HEIGHT * height - 2);
		if (r.contains(x, y)) {
			if (mo != null) {
				uvp.getMapEditorPanel().editMapObject(mo);
				uvp.setupPanel((String) uvp.getDrivers().getSelectedItem());
			} else if (pl != null) {					
				editPL(pl);
			} else {
				
				// PlannerLine pl = pc.getDefLine();
				// editPL(pl);
				
				SingleEditPanel sep = new SingleEditPanel(pc);												
				uvp.showScrollableOptionPane(sep, false);
				for (PlannerLine pl : pc.getLines())
					pl.commitChanges(pc.getPcdef().getReferenceStore());
				pc.getDefLine().commitChanges(pc.getPcdef().getReferenceStore());
				uvp.panelSelected();
				uvp.setupPanel((String) uvp.getDrivers().getSelectedItem());
			}
		}
	}

	private void editPL(PlannerLine pl) {
		PlannerContainerDef pcdef = pc.getPcdef();			
		pl.setupUI(null, 1, pcdef.getReferenceStore(), false, pc.getParentTab().getPlannerFrame());

		uvp.showScrollableOptionPane(pl.getUiAspect(), false);
		
		pl.commitChanges(pc.getPcdef().getReferenceStore());
		
		uvp.panelSelected();
		uvp.setupPanel((String) uvp.getDrivers().getSelectedItem());

	}
	
	public boolean isMayNotRun() {
		return hasValuesSpecified(1) || hasValuesSpecified(2);
	}
}
