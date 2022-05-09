package tactical.utils.planner.unified;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.planner.AutoCompletion;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerReference;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.mapedit.MapEditorPanel;

public class UnifiedViewPanel extends JPanel implements ActionListener, ItemListener, MouseListener {
	private static final long serialVersionUID = 1L;

	private UnifiedViewRenderPanel renderPanel;
	private JComboBox<String> drivers;
	public static final int yOffset = 10;
	private PlannerMap plannerMap;
	private ArrayList<PlannerTab> tabsWithMapRefs;
	private MapEditorPanel mapEditorPanel;	
	
	private MapObject moToEdit;
	
	private List<UnifiedRenderable> renderables;
	
	public UnifiedViewPanel(MapEditorPanel mapEditorPanel) {
		super(new BorderLayout());
		setup();
		this.mapEditorPanel = mapEditorPanel;
	}
	
	private void setup() {
		renderPanel = new UnifiedViewRenderPanel();
		renderPanel.addMouseListener(this);
		
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Select Driving Action: "));
		drivers = new JComboBox<>();
		drivers.setMaximumRowCount(30);
		AutoCompletion.enable(drivers);
		drivers.addItemListener(this);
		topPanel.add(drivers);
		topPanel.add(new JLabel(" Or "));
		JButton newCondButton = new JButton("Create New Condition");
		newCondButton.setActionCommand("createcond");
		newCondButton.addActionListener(this);
		topPanel.add(newCondButton);
		add(topPanel, BorderLayout.PAGE_START);
		JScrollPane renderScroll = new JScrollPane(renderPanel);
		renderScroll.getVerticalScrollBar().setUnitIncrement(20);
		add(renderScroll, BorderLayout.CENTER);
	}
	
	public class UnifiedViewRenderPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			if (renderables != null) {
				int height = 0;
				for (UnifiedRenderable ur : renderables) {
					ur.render(0, height, this.getWidth(), g);
					height += ur.getHeight();
				}
			}
		}
	}
			
	public void showScrollableOptionPane(JPanel panel, boolean forNewItem) {
		JScrollPane jsp = new JScrollPane(panel);
		jsp.getVerticalScrollBar().setUnitIncrement(40);
		if (forNewItem)
			jsp.setPreferredSize(new Dimension(1000, 600));
		else
			jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width + 50, 
				Math.min((int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 200), jsp.getPreferredSize().height)));			
		
		
		JOptionPane.showMessageDialog(renderPanel, jsp, "Edit", JOptionPane.PLAIN_MESSAGE);
	}	
	
	public interface UnifiedRenderable {
		public static int RENDERABLE_HEIGHT = 30;
		
		public void render(int indent, int y, int panelWidth, Graphics g);
		public int getHeight();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			String item = ((String) e.getItem());
			if (item == null)
				return;
			setupPanel(item);			
		}
	}
	
	public void setupPanel(String item) {
		if (item.contains("- Conditions are met")) {
			setupCondition(tabsWithMapRefs.get(PlannerFrame.TAB_CONDITIONS).
					getPlannerContainerByReference(new PlannerReference(item.substring(0, item.indexOf(" - Conditions are met")))));
		} else if (item.contains(" - Searched")) {
			setupSearchArea(parseMapObjectId(item, "- Searched"));
		} else if (item.contains(" - Talk with NPC")) {				
			setupNPC(parseMapObjectId(item, " - Talk with NPC"));
		}
		

		renderPanel.setPreferredSize(new Dimension(renderPanel.getPreferredSize().width, getRenderableHeight()));
		renderPanel.revalidate();
		
		renderPanel.repaint();
	}
	
	private MapObject parseMapObjectId(String item, String prefix) {
		int id = Integer.parseInt(item.substring(item.indexOf("(") + 1, item.indexOf(")")));
		item = item.substring(0, item.indexOf(prefix));
		
		return plannerMap.getMapObjects().get(id);
	}
	
	public int getRenderableHeight() {
		int height = 0;
		if (renderables == null)
			return 0;
		for (UnifiedRenderable ur : renderables)
			height += ur.getHeight();
		return (height  + 1) * UnifiedRenderable.RENDERABLE_HEIGHT;
	}

	public void loadMap(PlannerMap plannerMap, ArrayList<PlannerTab> tabsWithMapRefs) {
		this.plannerMap = plannerMap;
		this.tabsWithMapRefs = tabsWithMapRefs;
	}
	
	public void resetPanel() {
		drivers.removeAllItems();
		drivers.setSelectedIndex(-1);
		panelSelected();
	}
	
	public void panelSelected() {
		String valueToSelect = null;
		if (drivers.getSelectedItem() != null)
			valueToSelect = (String) drivers.getSelectedItem();
		drivers.removeAllItems();
		for (PlannerContainer pc : tabsWithMapRefs.get(PlannerFrame.TAB_CONDITIONS).getListPC()) {
			drivers.addItem((String) pc.getDefLine().getValues().get(0) + " - Conditions are met");
		}		
		
		int idx = 0;
		
		for (MapObject mo : plannerMap.getMapObjects()) {
			String text = null;
			if ("searcharea".equalsIgnoreCase(mo.getKey())) {
				text = mo.getName() + " - Searched (" + idx + ")";
				drivers.addItem(text);				
			} else if ("npc".equalsIgnoreCase(mo.getKey())) {
				text = mo.getName() + " - Talk with NPC (" + idx + ")";
				drivers.addItem(text);
			}
			
			if (mo == moToEdit) {
				if (text != null)
					valueToSelect = text;
				else {				
					PlannerContainer pc = plannerMap.getPCReferencingMapObject(mo);
					
					/*
					PlannerLine pl = pc.getDefLine();
					PlannerContainerDef pcdef = pc.getPcdef();
					pl.setupUI(pcdef.getAllowableLines(), null, 1, pcdef.getListOfLists(), false, null);
					int rc = JOptionPane.showConfirmDialog(this, pl.getUiAspect(), "Edit cinematic action", JOptionPane.OK_OPTION);
					if (rc == JOptionPane.NO_OPTION)
						return;
					*/
					
					if (pc != null)
						valueToSelect = (String) pc.getDefLine().getValues().get(0) + " - Conditions are met";
				}
			}
			idx++;
		}
		
		if (valueToSelect != null)
			drivers.setSelectedItem(valueToSelect);
		
		moToEdit = null;
		
		renderPanel.setPreferredSize(new Dimension(renderPanel.getPreferredSize().width, getRenderableHeight()));
		renderPanel.revalidate();		
		renderPanel.repaint();
	}
	
	private void setupSearchArea(MapObject mo) {
		renderables = new ArrayList<>();
		Group group = new Group(false, false);
		group.groupRenderables.add(new Line("When a hero searches location: " + 
				mo.getName(), mo, null, null, this));		
		renderables.add(group);
		renderables.add(new ArrowLine());
		
		
		String searchTrigger = mo.getParam("searchtrigger");
		if (searchTrigger != null) {
			try {
				int id = Integer.parseInt(searchTrigger);
				if (id == -1) {
					addBadSearchTriggerLine(mo);
				} else {
					PlannerContainer pc = tabsWithMapRefs.get(PlannerFrame.TAB_TRIGGER).getListPC().get(id);
					renderables.add(setupTrigger(new PlannerReference((String) pc.getDefLine().getValues().get(0))));
				}
			} catch (NumberFormatException e) {
				addBadSearchTriggerLine(mo);
			}
		}
		else {
			addBadSearchTriggerLine(mo);
		}
				
		
	}

	private void addBadSearchTriggerLine(MapObject mo) {
		renderables.add(new NotSpecifiedLine(true, false, false, mo, null, null, 0, this));
	}
	
	private void setupNPC(MapObject mo) {
		renderables = new ArrayList<>();
		Group group = new Group(false, false);
		group.groupRenderables.add(new Line("When a hero speaks with NPC: " + 
				mo.getName(), mo, null, null, this));		
		renderables.add(group);
		renderables.add(new ArrowLine());
		
		String speechId = mo.getParam("textid");
		if (speechId != null) {
			try {
				int id = Integer.parseInt(speechId);
				if (id == -1) {
					addBadTextId(mo);
				} else {
					PlannerContainer pc = tabsWithMapRefs.get(PlannerFrame.TAB_TEXT).getListPC().get(id);
					renderables.add(setupSpeech(new PlannerReference((String) pc.getDefLine().getValues().get(0))));
				}
			} catch (NumberFormatException e) {
				addBadTextId(mo);
			}
		}
		else {
			addBadTextId(mo);
		}
	}

	private void addBadTextId(MapObject mo) {
		renderables.add(new NotSpecifiedLine(false, false, false, mo, null, null, 0, this));
	}
	
	private void setupCondition(PlannerContainer pc) {		
		renderables = new ArrayList<>();
		Group group = new Group(false, false);
		group.groupRenderables.add(new Line("When conditions are met for: " + 
				pc.getDefLine().getValues().get(0), null, pc, null, this));		
		renderables.add(group);
		renderables.add(new ArrowLine());
		ArrayList<PlannerReference> triggers = (ArrayList<PlannerReference>) pc.getDefLine().getValues().get(1);
		PlannerReference trigger = triggers.get(0);
		if (trigger.getName().trim().length() == 0) {
			renderables.add(new NotSpecifiedLine(true, false, false, null, pc, null, 1, this));
			return;
		} else {
			renderables.add(setupTrigger(trigger));
		}
		
		for (int i = 1; i < triggers.size(); i++) {
			trigger = triggers.get(i);
			if (trigger.getName().trim().length() > 0) {
				renderables.add(new ArrowLine());
				renderables.add(setupTrigger(trigger));
			}
		}
		renderables.add(new ArrowLine());
		renderables.add(new NotSpecifiedLine(true, true, true, null, pc, null, 1, this));
	}
	
	private Group setupTrigger(PlannerReference ref) {
		PlannerContainer pc = tabsWithMapRefs.get(PlannerFrame.TAB_TRIGGER).
				getPlannerContainerByReference(ref);
		Group group = new Group(false, false);
		group.groupRenderables.add(new Line("Execute trigger: " + ref.getName(), null, pc, null, this));
		
		if (ref.getName().trim().length() == 0) {
			group.groupRenderables.add(new ArrowLine());
			group.groupRenderables.add(new NotSpecifiedLine(true, false, false, null, null, null, 0, this));
			return group;
		}
		
		// Check for speech or runtriggers
		for (PlannerLine pl : pc.getLines()) {
			if (pl.getPlDef().getName().equalsIgnoreCase("Show Text")) {
				if (pl.getValues().size() > 0 && ((PlannerReference) pl.getValues().get(0)).getName().trim().length() > 0) {
					PlannerReference plannerRef = (PlannerReference) pl.getValues().get(0);
					addArrowLineArrowGroup(group.groupRenderables, "A 'Show Text' action causes...", pc, pl, 
							pr -> setupSpeech(pr), plannerRef);
				} else {
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add(new Line("A 'Show Text' action causes...", null, pc, pl, this));
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add((new NotSpecifiedLine(false, false, true, null, pc, pl, 0, this)));
				}
			} else if (pl.getPlDef().getName().equalsIgnoreCase("Run Triggers")) {
				if (pl.getValues().size() > 0 && ((ArrayList<PlannerReference>) pl.getValues().get(0)).get(0).getName().trim().length() > 0) {
					ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(0);
					for (int i = 0; i < refs.size(); i++) {
						if (refs.get(i).getName() == null || refs.get(i).getName().trim().length() == 0) {
							refs.remove(i);
							i--;
						}
					}
					
					for (PlannerReference plannerRef : refs) {
						addArrowLineArrowGroup(group.groupRenderables, "A 'Run Triggers' action causes...", pc, pl, 
								pr -> setupTrigger(pr), plannerRef);
					}
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add((new NotSpecifiedLine(true, true, true, null, pc, pl, 0, this)));
				} else {
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add(new Line("A 'Run Triggers' action causes...", null, pc, pl, this));
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add((new NotSpecifiedLine(true, false, false, null, pc, pl, 0, this)));
				}
			} else if (pl.getPlDef().getName().equalsIgnoreCase("Show Map Event")) {
				if (pl.getValues().size() > 0 && ((PlannerReference) pl.getValues().get(1)).getName().trim().length() > 0) {
					PlannerReference plannerRef = (PlannerReference) pl.getValues().get(1);
					addArrowLineArrowGroup(group.groupRenderables, "A 'Show Map Event' action causes...", pc, pl, 
							pr -> setupTrigger(pr), plannerRef);
				} else {
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add(new Line("A 'Show Map Event' action causes...", null, pc, pl, this));
					group.groupRenderables.add(new ArrowLine());
					group.groupRenderables.add((new NotSpecifiedLine(true, false, false, null, pc, pl, 0, this)));
				}
			}
		}
		return group;
	}
	
	private Group addArrowLineArrowGroup(List<UnifiedRenderable> listToAdd, String text, PlannerContainer pc, PlannerLine pl,
			Function<PlannerReference, Group> addFunction, PlannerReference ref) {
		listToAdd.add(new ArrowLine());
		listToAdd.add(new Line(text, null, pc, pl, this));
		listToAdd.add(new ArrowLine());
		Group retGroup = addFunction.apply(ref);
		listToAdd.add(retGroup);
		return retGroup;
	}
	
	private Group setupSpeech(PlannerReference ref) {
		PlannerContainer pc = tabsWithMapRefs.get(PlannerFrame.TAB_TEXT).
				getPlannerContainerByReference(ref);
		Group group = new Group(false, false);
		Line showSpeechLine = new Line("Show speech: " + ref.getName(), null, pc, null, this);
		group.groupRenderables.add(showSpeechLine);
		
		if (ref.getName().trim().length() == 0) {
			group.groupRenderables.add(new ArrowLine());
			group.groupRenderables.add(new NotSpecifiedLine(false, false, false, null, null, null, 0, this));
			return group;
		}
		
		group.groupRenderables.add(new ArrowLine());
		
		int idx = 0;
		for (PlannerLine pl : pc.getLines()) {
			Group speechGroup = new Group(false, false);
			String text;
			if (pl.getPlDef().getName().equalsIgnoreCase("Message Text")) {
				text = (String) pl.getValues().get(6);
			} else if (pl.getPlDef().getName().equalsIgnoreCase("Conversation")) {
				text = (String) pl.getValues().get(9);
			} else
				text = (String) pl.getValues().get(7);
			
			Line subLine = new Line("Show " + pl.getPlDef().getName() + ": " + text, null, pc, pl, this); 
			showSpeechLine.childSpeechLine.add(subLine);
			
			speechGroup.groupRenderables.add(subLine);
			speechGroup.groupRenderables.add(new ArrowLine());						
			
			getMultiTriggerId(2, pc, pl, speechGroup);		
			
			if (pl.getPlDef().getName().equalsIgnoreCase("Yes or No Text")) {
				speechGroup.groupRenderables.add(new OrLine());
				getMultiTriggerId(3, pc, pl, speechGroup);		
			}
			
			group.groupRenderables.add(speechGroup);
			idx++;
			
			if (idx < pc.getLines().size())
				group.groupRenderables.add(new OrLine());
		}
		
		return group;
	}

	private void getMultiTriggerId(int idx, PlannerContainer pc, PlannerLine pl, Group speechGroup) {
		ArrayList<PlannerReference> speechRefs = (ArrayList<PlannerReference>) pl.getValues().get(idx);
		if (pl.getValues().size() <= idx) { 
			speechRefs = new ArrayList<>();
			speechRefs.add(new PlannerReference(""));
		}
		else {
			speechRefs = (ArrayList<PlannerReference>) pl.getValues().get(idx);
		}
		
		if (speechRefs.get(0).getName().trim().length() == 0)
			speechGroup.groupRenderables.add(new NotSpecifiedLine(true, true, false, null, pc, pl, idx, this));
		else {
			for (int index = 0 ; index < speechRefs.size(); index++) {
				PlannerReference pr = speechRefs.get(index);			
					if (index != 0)
						speechGroup.groupRenderables.add(new ArrowLine());
					speechGroup.groupRenderables.add(new Line("A 'Run Trigger on Text end' causes...", null, pc, pl, this));
					speechGroup.groupRenderables.add(new ArrowLine());
					speechGroup.groupRenderables.add(setupTrigger(pr));
			}
			speechGroup.groupRenderables.add(new ArrowLine());
			speechGroup.groupRenderables.add(new NotSpecifiedLine(true, true, true, null, pc, pl, idx, this));			
		}
	}

	public void setMoToEdit(MapObject moToEdit) {
		this.moToEdit = moToEdit;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		checkGroupClicked(x, y, renderables);
	}
	
	private void checkGroupClicked(int x, int y, List<UnifiedRenderable> urs) {
		for (UnifiedRenderable ur : urs) {					
			if (ur instanceof Line) {
				((Line) ur).checkClick(x, y);
			} else if (ur instanceof Group) {
				checkGroupClicked(x, y, ((Group) ur).groupRenderables);
			} else if (ur instanceof NotSpecifiedLine)
				((NotSpecifiedLine) ur).checkClick(x, y);
		}
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
		
	}

	public UnifiedViewRenderPanel getRenderPanel() {
		return renderPanel;
	}

	public JComboBox<String> getDrivers() {
		return drivers;
	}

	public PlannerMap getPlannerMap() {
		return plannerMap;
	}

	public ArrayList<PlannerTab> getTabsWithMapRefs() {
		return tabsWithMapRefs;
	}

	public MapEditorPanel getMapEditorPanel() {
		return mapEditorPanel;
	}

	public MapObject getMoToEdit() {
		return moToEdit;
	}

	public List<UnifiedRenderable> getRenderables() {
		return renderables;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("createcond".equalsIgnoreCase(e.getActionCommand())) {
			PlannerContainer pc = createNewContainer(PlannerFrame.TAB_CONDITIONS, null);
			if (pc != null) {
				String newEntry = (String) pc.getDefLine().getValues().get(0) + " - Conditions are met";
				drivers.addItem(newEntry);
				drivers.setSelectedItem((String) pc.getDefLine().getValues().get(0) + " - Conditions are met");
			}
		}
		
	}
	
	public PlannerContainer createNewContainer(int tabIdx, Consumer<PlannerContainer> callBack) {
		PlannerContainer newPC = getTabsWithMapRefs().get(tabIdx).addNewContainer();
		if (newPC != null) {
			showScrollableOptionPane(new SingleEditPanel(newPC), true);
			
			if (callBack != null) {
				callBack.accept(newPC);
			}
						
			for (PlannerLine pl : newPC.getLines()) {
				pl.commitChanges(newPC.getPcdef().getReferenceStore());
			}
			newPC.getDefLine().commitChanges(newPC.getPcdef().getReferenceStore());
			
			setupPanel((String)  getDrivers().getSelectedItem());
		}
		return newPC;
	}
}