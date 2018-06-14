package tactical.utils.planner.mapedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.planner.PlannerContainerDef;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerLineDef;
import tactical.utils.planner.PlannerReference;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.PlannerValueDef;


public class MapEditorPanel implements ActionListener {
	private static final String COMMAND_DISPLAY_ENEMY = "dispenemy";
	private static final String COMMAND_DISPLAY_TERRAIN = "dispterrain";
	private static final String COMMAND_DISPLAY_OTHER = "dispother";
	private static final String COMMAND_DISPLAY_UNUSED = "dispunused";
	private static final String COMMAND_DISPLAY_INTERACTABLE = "dispinter";

	private MapEditorRenderPanel mapPanel;
	private PlannerMap plannerMap;
	private PlannerFrame plannerFrame;
	private JPanel backPanel;
	private JScrollPane mapScrollPane;
	private JPanel sidePanel;
	private JComboBox<String> moCombo;
	private ArrayList<ArrayList<PlannerReference>> listOfLists;
	private boolean displayEnemy = true, displayTerrain = true,
		displayOther = true, displayUnused = true, displayInteractable = true;
	// private Hashtable<String, ArrayList<String>> entrancesByMap = new Hashtable<>();

	public MapEditorPanel(PlannerFrame plannerFrame, ArrayList<ArrayList<PlannerReference>> listOfLists)
	{
		mapPanel = new MapEditorRenderPanel(this);
		backPanel = new JPanel(new BorderLayout());
		mapScrollPane = new JScrollPane(mapPanel);
		backPanel.add(mapScrollPane, BorderLayout.CENTER);
		sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.setPreferredSize(new Dimension(200, 100));
		backPanel.add(sidePanel, BorderLayout.LINE_START);
		JPanel locationVisiblePanel = new JPanel();
		locationVisiblePanel.setBackground(Color.DARK_GRAY);

		locationVisiblePanel.add(createCheckBox("Enemies", COMMAND_DISPLAY_ENEMY));
		locationVisiblePanel.add(createCheckBox("Terrain", COMMAND_DISPLAY_TERRAIN));
		locationVisiblePanel.add(createCheckBox("Triggerables", COMMAND_DISPLAY_INTERACTABLE));
		locationVisiblePanel.add(createCheckBox("Others", COMMAND_DISPLAY_OTHER));
		locationVisiblePanel.add(createCheckBox("Untyped/Locations", COMMAND_DISPLAY_UNUSED));

		backPanel.add(locationVisiblePanel, BorderLayout.PAGE_START);
		this.plannerFrame = plannerFrame;
		this.listOfLists = listOfLists;
	}

	private JCheckBox createCheckBox(String text, String actionCommand)
	{
		JCheckBox cb = new JCheckBox(text, true);
		cb.setActionCommand(actionCommand);
		cb.setForeground(Color.white);
		cb.addActionListener(this);
		return cb;
	}

	public void loadMap(PlannerMap map, String mapName, ArrayList<PlannerTab> tabsWithMapReferences)
	{
		plannerMap = map;
		mapPanel.setPlannerMap(plannerMap);
		mapPanel.setTabsWithMapReferences(tabsWithMapReferences);
		mapScrollPane.getVerticalScrollBar().setUnitIncrement(mapPanel.getPreferredSize().height / 20);
		mapScrollPane.getHorizontalScrollBar().setUnitIncrement(mapPanel.getPreferredSize().width / 20);
		mapScrollPane.revalidate();
		this.backPanel.revalidate();
		mapPanel.repaint();
		sidePanel.removeAll();

		// entrancesByMap.clear();
		/*
		File mapDir = new File(PlannerFrame.PATH_MAPS);
		PlannerMap tempPlannerMap = new PlannerMap();
		for (File f : mapDir.listFiles())
		{
			if (!f.getName().endsWith(".tmx"))
				continue;

			try {
				MapParser.parseMap(f.getAbsolutePath(), tempPlannerMap, new PlannerTilesetParser(), null);
			} catch (Throwable t) {
				continue;
			}

			for (MapObject mo : tempPlannerMap.getMapObjects())
			{
				if (mo.getKey() == null)
					continue;

				if (mo.getKey().equalsIgnoreCase("start"))
				{
					String startLoc = mo.getParam("exit");
					ArrayList<String> entrances = entrancesByMap.get(f.getName().replace(".tmx", ""));
					if (entrances == null)
					{
						entrances = new ArrayList<String>();
						entrances.add(startLoc);
						entrancesByMap.put(f.getName().replace(".tmx", ""), entrances);
					}
					else
						entrances.add(startLoc);
				}
			}
		}
		*/
	}

	public JComponent getUIAspect()
	{
		return backPanel;
	}

	public void mouseDown(MapObject mo)
	{
		sidePanel.removeAll();
		JLabel type;

		if (mo.getKey() != null && mo.getKey().length() > 0)
			type = new JLabel(" " + mo.getKey().toUpperCase() + " (" + (mo.getName() == null ? "Unnamed" : mo.getName()) + ")");
		else
			type = new JLabel(" UNASSIGNED TYPE (" + (mo.getName() == null ? "Unnamed" : mo.getName()) + ")");

		type.setAlignmentX(Component.LEFT_ALIGNMENT);
		type.setFont(type.getFont().deriveFont(Font.BOLD));
		sidePanel.add(type);


		// Convert fields with REFERS that are by ID to their long name. Otherwise
		// add the value to the panel
		PlannerContainerDef pcdef = this.plannerFrame.getContainerDefByName("mapedit");
		PlannerLineDef plannerLineDef = getLineDefByName(pcdef, mo.getKey());

		if (plannerLineDef != null)
		{
			for (PlannerValueDef val : plannerLineDef.getPlannerValues())
			{
				String valueSet = null;
				if (val.getValueType() == PlannerValueDef.TYPE_INT && val.getRefersTo() != PlannerValueDef.REFERS_NONE)
				{
					int index = 0;
					if (mo.getParam(val.getTag()) != null)
						index = Integer.parseInt(mo.getParam(val.getTag()));
					if (index < 0 || index >= listOfLists.get(val.getRefersTo() - 1).size())
						valueSet = "NO VALUE SELECTED";
					else
						valueSet = listOfLists.get(val.getRefersTo() - 1).get(index).getName();
				}
				else
					valueSet = mo.getParam(val.getTag());
	
				JLabel entLabel = new JLabel(" " + val.getTag() + " = " + valueSet);
				sidePanel.add(entLabel);
				entLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			}
		}


		/*
		for (Entry<String, String> ent : mo.getParams().entrySet())
		{
			JLabel entLabel = new JLabel(" " + ent.getKey() + " = " + ent.getValue());
			sidePanel.add(entLabel);
			entLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		*/

		Vector<String> comboItems = new Vector<>();
		for (PlannerLineDef pld : pcdef.getAllowableLines())
		{
			comboItems.add(pld.getName());
		}
		moCombo = new JComboBox<>(comboItems);
		moCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
		moCombo.setMaximumSize(new Dimension(moCombo.getPreferredSize().width, 30));
		if (mo.getKey() != null && mo.getKey().length() > 0)
			moCombo.setSelectedItem(mo.getKey());
		
		sidePanel.add(moCombo);
		JButton editButton = new JButton("Edit Values");
		editButton.addActionListener(this);
		editButton.setActionCommand("editmo");

		sidePanel.add(editButton);
		sidePanel.add(Box.createGlue());

		sidePanel.revalidate();
		sidePanel.repaint();
	}

	private PlannerLineDef getLineDefByName(PlannerContainerDef pcdef, String name)
	{
		for (PlannerLineDef pld : pcdef.getAllowableLines())
		{
			if (pld.getName().equalsIgnoreCase(name))
				return pld;
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent aEv) {
		String command = aEv.getActionCommand();
		if (COMMAND_DISPLAY_ENEMY.equalsIgnoreCase(command))
		{
			displayEnemy = ((JCheckBox) aEv.getSource()).isSelected();
			mapPanel.repaint();
		}
		else if (COMMAND_DISPLAY_OTHER.equalsIgnoreCase(command))
		{
			displayOther = ((JCheckBox) aEv.getSource()).isSelected();
			mapPanel.repaint();
		}
		else if (COMMAND_DISPLAY_TERRAIN.equalsIgnoreCase(command))
		{
			displayTerrain = ((JCheckBox) aEv.getSource()).isSelected();
			mapPanel.repaint();
		}
		else if (COMMAND_DISPLAY_UNUSED.equalsIgnoreCase(command))
		{
			displayUnused = ((JCheckBox) aEv.getSource()).isSelected();
			mapPanel.repaint();
		}
		else if (COMMAND_DISPLAY_INTERACTABLE.equalsIgnoreCase(command))
		{
			displayInteractable = ((JCheckBox) aEv.getSource()).isSelected();
			mapPanel.repaint();
		}
		else if ("setmo".equalsIgnoreCase(command))
		{
			setMapObject();
		}
		else if ("editmo".equalsIgnoreCase(command))
		{
			editMapObject();
		}
	}

	public void setMapObject()
	{
		MapObject mo = this.mapPanel.getSelectedMapObject();
		if (mo.getKey() != null && mo.getKey().length() > 0)
		{
			if (mo.getKey().equalsIgnoreCase((String) moCombo.getSelectedItem()))
					return;
			int rc = JOptionPane.showConfirmDialog(mapPanel, "This map object already has a type set, if you continue the\n"
					+ "the values that are currently saved in this object will be discarded.\n"
					+ "Are you sure you want to continue?", "Confirm Old Value Override", JOptionPane.YES_NO_OPTION);
			if (rc != JOptionPane.OK_OPTION)
			{
				return;
			}
		}

		mo.setKey((String) moCombo.getSelectedItem());
		mo.getParams().clear();
		this.mouseDown(mo);
	}
	
	public void editMapObject() {
		setMapObject();

		MapObject mo = this.mapPanel.getSelectedMapObject();
		editMapObject(mo);
	}

	public void editMapObject(MapObject mo)
	{
		PlannerContainerDef pcdef = this.plannerFrame.getContainerDefByName("mapedit");
		PlannerLineDef pld = getLineDefByName(pcdef, mo.getKey());

		if (pld == null)
		{
			JOptionPane.showMessageDialog(mapPanel, "Unable to edit this location, either the object type has\n"
					+ "not been set or it is set to an unsupported value.");
			return;
		}

		PlannerLine pl = new PlannerLine(pld, false);

		for (PlannerValueDef val : pl.getPlDef().getPlannerValues())
		{
			if (val.getValueType() == PlannerValueDef.TYPE_INT && val.getRefersTo() != PlannerValueDef.REFERS_NONE)
			{
				try {
					pl.getValues().add(Integer.parseInt(mo.getParam(val.getTag())) + 1);
				}
				catch (NumberFormatException e) {
					pl.getValues().add(0);
				}
			}
			else if (val.getValueType() == PlannerValueDef.TYPE_INT) {
				try {
					pl.getValues().add(Integer.parseInt(mo.getParam(val.getTag())));
				}
				catch (NumberFormatException e) {
					pl.getValues().add(0);
				}
			}
			else
				pl.getValues().add(mo.getParam(val.getTag()));
		}

		try
		{
			pl.setupUI(pcdef.getAllowableLines(), this, 1, pcdef.getListOfLists(), false, true, null);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this.getUIAspect(), "Unable to edit the selected location: have you loaded the associated text file yet?",
					"Unable to load location", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int ret = JOptionPane.showConfirmDialog(mapPanel, pl.getUiAspect(), "Edit Map Location Attributes", JOptionPane.OK_CANCEL_OPTION);

		if (ret == JOptionPane.OK_OPTION)
		{
			pl.commitChanges();

			mo.getParams().clear();
			for (int i = 0; i < pld.getPlannerValues().size(); i++)
			{
				PlannerValueDef pvd = pld.getPlannerValues().get(i);
				if (pvd.getValueType() == PlannerValueDef.TYPE_STRING) {
					if (pvd.getRefersTo() != PlannerValueDef.REFERS_NONE)
						mo.getParams().put(pvd.getTag(), ((PlannerReference) pl.getValues().get(i)).getName());
					else 
						mo.getParams().put(pvd.getTag(), (String) pl.getValues().get(i));
				}
				else if (pvd.getValueType() == PlannerValueDef.TYPE_BOOLEAN)
					mo.getParams().put(pvd.getTag(), ((boolean) pl.getValues().get(i)) + "");
				else
				{
					if (pvd.getRefersTo() != PlannerValueDef.REFERS_NONE)
						mo.getParams().put(pvd.getTag(),  listOfLists.get(pvd.getRefersTo() - 1).indexOf(pl.getValues().get(i)) + "");
					else
						mo.getParams().put(pvd.getTag(), ((int) pl.getValues().get(i)) + "");
				}
			}
			this.mouseDown(mo);
		}
	}

	public boolean isDisplayEnemy() {
		return displayEnemy;
	}

	public boolean isDisplayTerrain() {
		return displayTerrain;
	}

	public boolean isDisplayOther() {
		return displayOther;
	}

	public boolean isDisplayUnused() {
		return displayUnused;
	}

	public boolean isDisplayInteractable() {
		return displayInteractable;
	}

	public PlannerFrame getPlannerFrame() {
		return plannerFrame;
	}
}
