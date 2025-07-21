package tactical.utils.planner.cinematic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tactical.engine.log.LoggingUtils;
import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.planner.AttributeTransferHandler;
import tactical.utils.planner.AutoCompletion;
import tactical.utils.planner.MapListRenderer;
import tactical.utils.planner.MapListRenderer.MapCell;
import tactical.utils.planner.PlannerAttributeList;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerContainerDef;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerLineDef;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.PlannerTimeBarViewer.CameraLocation;

public class CinematicCreatorPanel implements ActionListener, ChangeListener, ItemListener,
	ListSelectionListener, MouseMotionListener, MouseListener
{
	private static final Logger LOGGER = LoggingUtils.createLogger(CinematicCreatorPanel.class);
	private PlannerFrame plannerFrame;
	private CinematicMapDisplayPanel mdp = null;
	private JLabel locationLabel = new JLabel("");
	private JLabel rememberedLocationLabel = new JLabel("");
	private JScrollPane mapScrollPane;
	private JSlider timeSlider = new JSlider();
	private JList<MapCell> cinematicList = new JList<>();
	private JScrollPane cinematicListScrollPane = new JScrollPane(cinematicList);
	private Vector<MapCell> cinematicListItems = new Vector<>();
	private JComboBox<String> cinematicIds = new JComboBox<String>();
	private JButton cinButton;
	private JLabel currentActionLabel = new JLabel("Action: NONE");
	private JLabel currentTimeLabel = new JLabel("Time: 0");
	private JButton duplicateButton = new JButton("Duplicate Action");
	private JButton removeButton = new JButton("Remove Action");
	private JPanel currentActionPanel = new JPanel();
	private PlannerAttributeList attributeList;
	private int selectedCinIndex = 0;
	private boolean movingToNextCin = false;
	private boolean ignoreAttributeListChanges = false;
	private JPanel uiAspect;
	private int currentTilePixelX = 0, currentTilePixelY = 0;
	private PlannerTab pt = null;
	private JPanel listPanel = null;

	public CinematicCreatorPanel(PlannerFrame plannerFrame)
	{
		uiAspect = new JPanel(new BorderLayout());
		this.plannerFrame = plannerFrame;

		// this.add(locationInfoPanel, BorderLayout.LINE_START);

		MapCell mc = new MapCell();
		mc.name = "Selected Location: NONE";
		cinematicListItems.add(mc);

		mc = new MapCell();
		mc.name = "System";
		cinematicListItems.add(mc);

		mc = new MapCell();
		mc.name = "Camera";
		cinematicListItems.add(mc);

		mc = new MapCell();
		mc.name = "Sound";
		cinematicListItems.add(mc);

		cinematicList.setCellRenderer(new MapListRenderer());
		cinematicList.setListData(cinematicListItems);
		cinematicList.addListSelectionListener(this);

				
		listPanel = new JPanel(new BorderLayout());
		listPanel.add(cinematicListScrollPane, BorderLayout.CENTER);
		pt =  plannerFrame.getPlannerTabAtIndex(PlannerFrame.TAB_CIN);
		// This value needs to be updated
		initializeCinematicIdList();
		AutoCompletion.enable(cinematicIds);
		listPanel.add(cinematicIds, BorderLayout.PAGE_START);
		listPanel.setPreferredSize(new Dimension(200, 50));
		// uiAspect.add(listPanel, BorderLayout.LINE_START);

		locationLabel.setFont(locationLabel.getFont().deriveFont(18f));
		rememberedLocationLabel.setFont(rememberedLocationLabel.getFont().deriveFont(18f));
		JPanel locationPanel = new JPanel(new BorderLayout());
		locationPanel.setPreferredSize(new Dimension(0, 20));
		locationPanel.add(locationLabel, BorderLayout.LINE_START);
		locationPanel.add(rememberedLocationLabel, BorderLayout.LINE_END);
		uiAspect.add(locationPanel, BorderLayout.PAGE_END);

		JPanel timelinePanel = new JPanel(new BorderLayout());

		cinButton = new JButton("Go to nearest instruction");
		cinButton.addActionListener(this);
		cinButton.setActionCommand("cin");
		cinButton.setEnabled(false);
		listPanel.add(cinButton, BorderLayout.PAGE_END);
		
		JPanel backPanel = new JPanel(new BorderLayout());
		backPanel.add(listPanel, BorderLayout.LINE_END);
		uiAspect.add(backPanel, BorderLayout.CENTER);
		
		// timelinePanel.add(cinButton, BorderLayout.LINE_START);

		timeSlider.setMinimum(0);
		timeSlider.setMaximum(100000);
		timeSlider.setMajorTickSpacing(5000);
		timeSlider.setMinorTickSpacing(1000);
		timeSlider.setPaintLabels(true);
		timeSlider.setPaintTicks(true);
		timeSlider.addChangeListener(this);
		timeSlider.setEnabled(false);
		timelinePanel.add(timeSlider, BorderLayout.CENTER);
		uiAspect.add(timelinePanel, BorderLayout.PAGE_START);

		JPanel actionPanel = new JPanel();
		actionPanel.setPreferredSize(new Dimension(200, 100));
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.PAGE_AXIS));
		actionPanel.add(currentActionLabel);
		actionPanel.add(currentTimeLabel);
		actionPanel.add(duplicateButton);
		actionPanel.add(removeButton);
		currentActionPanel.setLayout(new BoxLayout(currentActionPanel, BoxLayout.PAGE_AXIS));
		currentActionPanel.setMinimumSize(new Dimension(100, 400));
		actionPanel.add(currentActionPanel);
		duplicateButton.addActionListener(this);
		duplicateButton.setActionCommand("dupaction");
		removeButton.addActionListener(this);
		removeButton.setActionCommand("remaction");
		this.duplicateButton.setEnabled(false);
		this.removeButton.setEnabled(false);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(actionPanel, BorderLayout.CENTER);
		attributeList = new PlannerAttributeList(this, this);
		attributeList.setPreferredSize(new Dimension(100, 400));
		attributeList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		rightPanel.add(attributeList, BorderLayout.PAGE_START);
		uiAspect.add(rightPanel, BorderLayout.LINE_END);
	}

	public void initializeCinematicIdList() {
		DefaultComboBoxModel<String> ids = new DefaultComboBoxModel<String>(pt.getItemList());
		ids.addElement("Create new cinematic...");
		cinematicIds.setModel(ids);
		cinematicIds.setSelectedIndex(-1);
	}

	public void loadMap(PlannerMap map)
	{
		pt =  plannerFrame.getPlannerTabAtIndex(PlannerFrame.TAB_CIN);
		cinematicIds.removeItemListener(this);
		if (mapScrollPane != null)
		{
			//uiAspect.remove(mapScrollPane);
			((JPanel) ((BorderLayout) this.getUiAspect().getLayout()).getLayoutComponent(BorderLayout.CENTER)).remove(mapScrollPane);
			uiAspect.removeMouseMotionListener(this);
			
			// ((JPanel) ((BorderLayout) this.getUiAspect().getLayout()).getLayoutComponent(BorderLayout.CENTER)).remove(listPanel);			
			
		}
		
		mdp = new CinematicMapDisplayPanel(map, this);
		mapScrollPane = new JScrollPane(mdp);
		
		((JPanel) ((BorderLayout) this.getUiAspect().getLayout()).getLayoutComponent(BorderLayout.CENTER)).add(mapScrollPane, BorderLayout.CENTER);
		
		mapScrollPane.getVerticalScrollBar().setUnitIncrement(map.getMapHeightInPixels() / 20);
		mapScrollPane.getHorizontalScrollBar().setUnitIncrement(map.getMapWidthInPixels() / 20);
		cinematicIds.addItemListener(this);
		cinematicIds.setSelectedIndex(-1);
		itemStateChanged(null);
		uiAspect.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		locationLabel.setText("  Abs X: " + e.getX() + " Abs Y: " + e.getY() +
				" Tile X: " + (e.getX() / mdp.getPlannerMap().getTileRenderWidth()) / 2 +
				" Tile Y: " + (e.getY() / mdp.getPlannerMap().getTileRenderHeight()) / 2 +
				" Tiles Pixel X: " + (e.getX() / (mdp.getPlannerMap().getTileRenderWidth() * 2) * (mdp.getPlannerMap().getTileRenderWidth() * 2)) +
				" Tiles Pixel Y: " + (e.getY() / (mdp.getPlannerMap().getTileRenderHeight() * 2) * (mdp.getPlannerMap().getTileRenderHeight() * 2)) +
				" Walkable: " + mdp.getPlannerMap().isMarkedMoveableForTown((e.getX() / mdp.getPlannerMap().getTileRenderWidth()) / 2,
						(e.getY() / mdp.getPlannerMap().getTileRenderHeight()) / 2));
		rememberedLocationLabel.setText("Remembered Tile Pixel X: " + currentTilePixelX + " Remembered Tile Pixel Y: " + currentTilePixelY + "  ");
	}

	public void middleButtonPushed(MouseEvent e) {
		currentTilePixelX = (e.getX() / (mdp.getPlannerMap().getTileRenderWidth() * 2) * (mdp.getPlannerMap().getTileRenderWidth() * 2));
		currentTilePixelY = (e.getY() / (mdp.getPlannerMap().getTileRenderHeight() * 2) * (mdp.getPlannerMap().getTileRenderHeight() * 2));

		if (pt.getCurrentPC() == null)
		{
			pt.setSelectedListItem(cinematicIds.getSelectedIndex(), null);
			System.out.println("Set the selected index " + pt.getCurrentPC());
		}

		PlannerLine pl = pt.getCurrentPC().getLines().get(selectedCinIndex);

		// Check to see if there is a move action selected, if so
		// then asdasdasdopen up the edit window with the new values entered. If
		// the edit is cancelled then reset the original values
		if (pl.getPlDef().getName().equalsIgnoreCase("Move"))
		{
			int origX = (Integer) pl.getValues().get(1);
			int origY = (Integer) pl.getValues().get(2);

			pl.getValues().set(1, currentTilePixelX);
			pl.getValues().set(2, currentTilePixelY);

			if (!editCinematicLine())
			{
				pl.getValues().set(1, origX);
				pl.getValues().set(2, origY);
			}
		}
	}

	public void locationClicked(MapObject mo)
	{
		cinematicListItems.get(0).name = "Selected Location: " + mo.getKey();
		cinematicListItems.get(0).values.clear();

		for (Entry<String, String> param : mo.getParams().entrySet())
		{
			cinematicListItems.get(0).values.add(param.getKey() + ": " + param.getValue());
		}

		if (mo.getKey().equalsIgnoreCase("trigger") || mo.getKey().equalsIgnoreCase("battletrigger"))
		{
			JButton button = new JButton("Go to trigger");
			button.setActionCommand("trigger");
			button.addActionListener(this);
			cinematicListItems.get(0).button = button;
		}
		else if (mo.getKey().equalsIgnoreCase("enemy"))
		{
			JButton button = new JButton("Go to enemy");
			button.setActionCommand("enemy");
			button.addActionListener(this);
			cinematicListItems.get(0).button = button;
		}

		cinematicList.setListData(cinematicListItems);

		uiAspect.revalidate();
		this.cinematicList.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		MapObject mo = this.mdp.getSelectedMO();

		if (arg0.getActionCommand().equalsIgnoreCase("trigger"))
		{
			if (pt.setSelectedListItem(Integer.parseInt(mo.getParam("triggerid")), null))
			{
				plannerFrame.setSelectedTabIndex(PlannerFrame.TAB_TRIGGER);
				pt.setSelectedListItem(Integer.parseInt(mo.getParam("triggerid")), null);
				LOGGER.fine("TRIGGER ID " + Integer.parseInt(mo.getParam("triggerid")));
			}
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("enemy"))
		{
			if (pt.setSelectedListItem(Integer.parseInt(mo.getParam("enemyid")), null))
			{
				plannerFrame.setSelectedTabIndex(PlannerFrame.TAB_ENEMY);
				LOGGER.fine("TRIGGER ID " + Integer.parseInt(mo.getParam("enemyid")));
			}
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("cin"))
		{
			CinematicTimeline ct = mdp.getTimeline();
			for (int i = 0; i < ct.cinematicTime.size(); i++)
			{
				if (ct.cinematicTime.get(i) >= timeSlider.getValue())
				{
					plannerFrame.setSelectedTabIndex(PlannerFrame.TAB_CIN);
					pt.setSelectedListItem(selectedCinIndex, i);
					// pt.updateAttributeList(i);
					break;
				}
			}
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("prevaction"))
		{
			CinematicTimeline ct = mdp.getTimeline();
			LOGGER.fine("NEXT ACTION " + selectedCinIndex);
			if (selectedCinIndex > 0)
			{
				movingToNextCin = true;
				if (ct.cinematicTime.get(selectedCinIndex).intValue() != ct.cinematicTime.get(selectedCinIndex - 1).intValue())
					timeSlider.setValue(ct.cinematicTime.get(--selectedCinIndex).intValue());
				else
				{
					--selectedCinIndex;
					stateChanged(null);
				}
			}
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("nextaction"))
		{
			CinematicTimeline ct = mdp.getTimeline();
			System.out.println("NEXT ACTION " + selectedCinIndex);
			if (selectedCinIndex + 1 < ct.cinematicTime.size())
			{
				movingToNextCin = true;
				if (ct.cinematicTime.get(selectedCinIndex).intValue() != ct.cinematicTime.get(selectedCinIndex + 1).intValue())
					timeSlider.setValue(ct.cinematicTime.get(++selectedCinIndex).intValue());
				else
				{
					++selectedCinIndex;
					stateChanged(null);
				}
				System.out.println("AFTER SELECTED " + selectedCinIndex);
			}
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("editaction"))
		{
			editCinematicLine();
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("dupaction"))
		{
			pt.getCurrentPC().duplicateLine(attributeList.getSelectedIndex());

			selectedCinIndex++;
			movingToNextCin = true;
			long maxTime = 0;
			try {
				maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
			} catch (IOException ex) {
				showBadCinematicMessage();
			}
			timeSlider.setMaximum((int) maxTime);
			timeSlider.revalidate();
			timeSlider.repaint();
		}
		else if (arg0.getActionCommand().equalsIgnoreCase("remaction"))
		{
			pt.getCurrentPC().removeLine(attributeList.getSelectedIndex());

			selectedCinIndex = Math.max(0, selectedCinIndex - 1);
			movingToNextCin = true;
			long maxTime = 0;
			try {
				maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
			} catch (IOException ex) {
				showBadCinematicMessage();
			}
			timeSlider.setMaximum((int) maxTime);
			timeSlider.revalidate();
			timeSlider.repaint();
		}

	}

	private boolean editCinematicLine()
	{
		if (!timeSlider.isEnabled())
			return false;

		pt.setSelectedListItem(cinematicIds.getSelectedIndex(), null);

		PlannerLine pl = pt.getCurrentPC().getLines().get(selectedCinIndex);
		PlannerContainerDef pcdef = pt.getPlannerContainerDef();
		pl.setupUI(this, 1, pcdef.getReferenceStore(), false, null);
		int rc = JOptionPane.showConfirmDialog(uiAspect, pl.getUiAspect(), "Edit cinematic action", JOptionPane.OK_OPTION);
		if (rc == JOptionPane.NO_OPTION)
			return false;

		pl.commitChanges(plannerFrame.getReferenceStore());

		try {
			long maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
			timeSlider.setMaximum((int) maxTime);
			timeSlider.revalidate();
			timeSlider.repaint();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, 
					"There was an error parsing the cinematic you just edited. Please fill all required fields and make sure actor names are correct");
			return editCinematicLine();
		}
		return true;
	}

	public void addCinematicLineByName(String name, int actorIndex, int xLoc, int yLoc)
	{
		if (!timeSlider.isEnabled())
			return;

		PlannerContainerDef pcdef = pt.getPlannerContainerDef();
		PlannerLine pl = new PlannerLine(getCinematicLineDefByName(name), false);
		// TODO WHAT IS THAT?
		// I think this sets the hero that this cinematic should be added
		// for if an actor is currently selected
		if (actorIndex != -1)
			pl.getValues().add(cinematicListItems.get(actorIndex + 4).name);
		if (xLoc != -1 && yLoc != -1)
		{
			pl.getValues().add(xLoc);
			pl.getValues().add(yLoc);
		}
		pl.setupUI(this, 1, pcdef.getReferenceStore(), false, null);
		// pl.getUiAspect().setPreferredSize(new Dimension(480, pl.getUiAspect().getPreferredSize().height + 50));
		//JScrollPane jsp = new JScrollPane(pl.getUiAspect());
		//jsp.setPreferredSize(new Dimension(600, jsp.getPreferredSize().height));
		int ret = JOptionPane.showConfirmDialog(uiAspect, pl.getUiAspect(), "Add cinematic action", JOptionPane.OK_CANCEL_OPTION);

		if (ret == JOptionPane.YES_OPTION)
		{
			CinematicTimeline ct = mdp.getTimeline();

			try
			{
				pl.commitChanges(plannerFrame.getReferenceStore());
			}
			catch (IllegalArgumentException ex)
			{
				JOptionPane.showMessageDialog(uiAspect, "The cinematic item was not saved as a non-optional field was left blank");
				return;
			}
			if (ct.cinematicTime.size() == 0)
			{
				pt.getCurrentPC().addLine(pl);
				selectedCinIndex = 0;
			}
			else
			{
				/*
				boolean found = false;
				for (int i = 0; i < ct.cinematicTime.size(); i++)
				{
					if (ct.cinematicTime.get(i) > timeSlider.getValue())
					{
						pt.getCurrentPC().addLine(pl, i + 1);
						found = true;
						break;
					}
				}

				if (!found)
					pt.getCurrentPC().addLine(pl);
					*/
				pt.getCurrentPC().addLine(pl, ++selectedCinIndex);
			}
			movingToNextCin = true;
			try {
				long maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
				timeSlider.setMaximum((int) maxTime);
				timeSlider.revalidate();
				timeSlider.repaint();
			} catch (Exception ex) {				
				JOptionPane.showMessageDialog(null, 
						"There was an error parsing the cinematic you just created. Please fill all required fields and make sure actor names are correct");
				if (!editCinematicLine()) {
					pt.getCurrentPC().removeLine(selectedCinIndex);
					selectedCinIndex = Math.max(selectedCinIndex - 1, 0);
				}
			}
			
		}
	}

	private PlannerLineDef getCinematicLineDefByName(String name)
	{
		PlannerContainerDef pcdef = pt.getPlannerContainerDef();

		for (PlannerLineDef pld : pcdef.getAllowableLines())
		{
			if (pld.getName().equalsIgnoreCase(name))
				return pld;
		}
		return null;
	}

	public PlannerFrame getPlannerFrame() {
		return plannerFrame;
	}

	private class ActorAction implements Comparable<ActorAction>
	{
		public String actor;
		public ArrayList<String> actions;

		public ActorAction(String actor, ArrayList<String> actions) {
			super();
			this.actor = actor;
			this.actions = actions;
		}

		@Override
		public int compareTo(ActorAction o) {
			return actor.compareTo(o.actor);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		cinematicListItems.get(1).values = mdp.getSystemValuesAtTime(timeSlider.getValue());
		cinematicListItems.get(2).values = mdp.getCameraValuesAtTime(timeSlider.getValue());
		cinematicListItems.get(3).values = mdp.getSoundValuesAtTime(timeSlider.getValue());
		mdp.setStaticSpritesAtTime(timeSlider.getValue());

		System.out.println("Before remove " + cinematicListItems.size());
		while (cinematicListItems.size() > 4)
			cinematicListItems.remove(4);

		System.out.println("After remove " + cinematicListItems.size());

		ArrayList<String> names = new ArrayList<String>();
		ArrayList<ArrayList<String>> lol = mdp.getActorValuesAtTime(timeSlider.getValue(), names);
		System.out.println(names.size());

		ArrayList<ActorAction> aas = new ArrayList<ActorAction>();
		for (int i = 0; i < names.size(); i++)
		{
			aas.add(new ActorAction(names.get(i), lol.get(i)));
		}
		Collections.sort(aas);

		for (ActorAction aa : aas)
		{
			MapCell mc = new MapCell();
			mc.name = aa.actor;
			mc.values = aa.actions;
			cinematicListItems.add(mc);
		}

		cinematicList.setListData(cinematicListItems);

		int selected = cinematicList.getSelectedIndex();
		if (selected < cinematicListItems.size())
			cinematicList.setSelectedIndex(selected);
		else
			cinematicList.setSelectedIndex(1);

		if (!movingToNextCin)
		{
			if (timeSlider.getValue() == 0)
				selectedCinIndex = 0;
			else
			{
				CinematicTimeline ct = mdp.getTimeline();
				if (ct.cinematicTime.size() <= 1)
					selectedCinIndex = 0;
				else
				{
					for (int i = 0; i < ct.cinematicTime.size(); i++)
					{
						if (ct.cinematicTime.get(i) < timeSlider.getValue())
							selectedCinIndex = i;
						else
							break;
					}

					System.out.println("SELECTED");
				}
			}
		}
		ignoreAttributeListChanges = true;
		attributeList.updateAttributeList(mdp.getCurrentPC(), -1, new MapAttributeTransferHandler(mdp.getCurrentPC()));
		attributeList.setSelectedIndex(selectedCinIndex);
		ignoreAttributeListChanges = false;
		movingToNextCin = false;

		CinematicTimeline ct = mdp.getTimeline();
		CameraLocation cameraLocations = null;
		for (CameraLocation cl : ct.cameraLocations)
		{
			if (cl.time > timeSlider.getValue())
				break;
			else
				cameraLocations = cl;
		}

		Point cameraPoint;
		if (cameraLocations.following != null)
		{
			Point actorPoint = mdp.getActorLocationAtTime(timeSlider.getValue(), cameraLocations.following).currentPoint;

			if (actorPoint.x < 160)
				actorPoint.x = 0;
			else
				actorPoint.x = (Math.max(0, actorPoint.x - 160));

			if (actorPoint.y < 120)
				actorPoint.y = 0;
			else
				actorPoint.y = (Math.max(0, actorPoint.y - 120));
			cameraPoint = new Point(actorPoint);
			mdp.setCameraLocation(actorPoint);
		}
		else if (cameraLocations.endLocX != -1)
		{
			cameraPoint = new Point((int) (cameraLocations.locX + ((cameraLocations.endLocX - cameraLocations.locX) * 1.0f * (timeSlider.getValue() - cameraLocations.time) / cameraLocations.duration)),
					(int) (cameraLocations.locY + ((cameraLocations.endLocY - cameraLocations.locY) * 1.0f * (timeSlider.getValue() - cameraLocations.time) / cameraLocations.duration)));
			mdp.setCameraLocation(cameraPoint);
		}
		else
		{
			cameraPoint = new Point(cameraLocations.locX, cameraLocations.locY);
			mdp.setCameraLocation(cameraPoint);
		}

		cinematicListItems.get(2).values.add(0, "Location: " + cameraPoint.x + " " + cameraPoint.y);

		setCurrentActionLabel();

		uiAspect.revalidate();
		this.cinematicList.repaint();
		mdp.repaint();

		/*
		StringBuffer sb = mdp.getValuesAtTime(timeSlider.getValue());
		String[] lines = sb.toString().split("\n");
		locationInfoPanel.removeAll();
		JButton cinButton = new JButton("Go to nearest instruction");
		cinButton.addActionListener(this);
		cinButton.setActionCommand("cin");
		locationInfoPanel.add(cinButton);
		for (String s : lines)
			locationInfoPanel.add(new JLabel(s));
		locationInfoPanel.revalidate();
		locationInfoPanel.repaint();
		mdp.repaint();
		*/
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e == null || e.getStateChange() == ItemEvent.SELECTED) {
			if (cinematicIds.getSelectedIndex() <  pt.getListPC().size()) {
				loadCinematicItem(-1);
			// Create a new cinematic
			} else {
				if (pt.addNewContainer() == null)
					cinematicIds.setSelectedIndex(-1);
				else {
					reloadCinematicItem();
					cinematicIds.setSelectedIndex(pt.getListPC().size() - 1);
				}
			}
		}
	}

	public void reloadCinematicItem()
	{
		// This value needs to be updated
		initializeCinematicIdList();
		loadCinematicItem(timeSlider.getValue());
	}
	
	public void clearSelectedCinematic() {
		cinematicIds.setSelectedIndex(-1);
	}

	private void loadCinematicItem(int selectedTime)
	{		
		if (cinematicIds.getSelectedIndex() == -1)
		{
			this.timeSlider.setEnabled(false);
			this.cinButton.setEnabled(false);
			mdp.setSelectedActor(-1);
			this.duplicateButton.setEnabled(false);
			this.removeButton.setEnabled(false);
			this.currentActionLabel.setText("Action: NONE");
			mdp.setCameraLocation(null);
			currentActionPanel.removeAll();
		}
		else
		{
			currentActionPanel.removeAll();
			currentTimeLabel.setText("Time: 0");
			this.duplicateButton.setEnabled(true);
			this.removeButton.setEnabled(true);
			long maxTime = 0;
			try {
				maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
			} catch (IOException ex) {
				showBadCinematicMessage();
			}
			timeSlider.setMaximum((int) maxTime);
			if (selectedTime == -1)
				this.timeSlider.setValue(0);
			else
			{
				if (selectedTime > maxTime)
					timeSlider.setValue((int) maxTime);
				else
					timeSlider.setValue(selectedTime);
			}
			this.timeSlider.setEnabled(true);
			this.cinButton.setEnabled(true);
			selectedCinIndex = 0;
			this.setCurrentActionLabel();
			mdp.getTimeline().dumpTimeline();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting())
		{
			if (e.getSource() == cinematicList)
			{
				if (cinematicList.getSelectedIndex() > 3)
				{
					mdp.setSelectedActor(cinematicList.getSelectedIndex() - 4);
				}
				else
					mdp.setSelectedActor(-1);
			}
			else
			{
				if (!ignoreAttributeListChanges)
				{
					CinematicTimeline ct = mdp.getTimeline();

					movingToNextCin = true;
					int newSelectedIndex = attributeList.getSelectedIndex();

					System.out.println(ct.cinematicTime.size() + " " + newSelectedIndex + " " + attributeList.getListLength());

					if (ct.cinematicTime.get(selectedCinIndex).intValue() != ct.cinematicTime.get(newSelectedIndex).intValue())
					{
						selectedCinIndex = newSelectedIndex;
						timeSlider.setValue(ct.cinematicTime.get(selectedCinIndex).intValue());
					}
					else
					{
						selectedCinIndex = newSelectedIndex;
						stateChanged(null);
					}
				}
			}
		}
	}

	public void setActorSelected(int index)
	{
		if (index != -1)
			cinematicList.setSelectedIndex(index + 4);
		else
			cinematicList.setSelectedIndex(1);
	}

	private void setCurrentActionLabel()
	{
		PlannerContainer pc = pt.getListPC().get(cinematicIds.getSelectedIndex());
		if (selectedCinIndex < pc.getLines().size())
		{
			PlannerLine pl = pc.getLines().get(selectedCinIndex);
			currentActionLabel.setText("Action: " + pl.getPlDef().getName());
			currentActionPanel.removeAll();
			for (int i = 0; i < pl.getValues().size(); i++)
			{
				currentActionPanel.add(new JLabel(pl.getPlDef().getPlannerValues().get(i).getDisplayTag()));
				JLabel valueLabel = new JLabel(pl.getValues().get(i).toString());
				valueLabel.setForeground(Color.BLUE);
				currentActionPanel.add(valueLabel);
			}

			currentActionPanel.repaint();
		}
		currentActionLabel.repaint();

		currentTimeLabel.setText("Time: " + timeSlider.getValue());
	}

	private class MapAttributeTransferHandler extends AttributeTransferHandler
	{
		private static final long serialVersionUID = 1L;

		public MapAttributeTransferHandler(PlannerContainer currentPC) {
			super(currentPC);
		}

		@Override
		public boolean importData(TransferSupport ts) {
			boolean rc = super.importData(ts);
			if (rc)
			{
				// attributeList.updateAttributeList(mdp.getCurrentPC(), newIndex, new MapAttributeTransferHandler(mdp.getCurrentPC()));
				// attributeList.validate();

				
				long maxTime = 0;
				try {
					maxTime = mdp.loadCinematicItem(cinematicIds.getSelectedIndex());
				} catch (IOException ex) {
					showBadCinematicMessage();
				}
				timeSlider.setMaximum((int) maxTime);
				timeSlider.revalidate();
				timeSlider.repaint();

				attributeList.setSelectedIndex(newIndex);
			}


			return rc;
		}
	}

	public JPanel getUiAspect() {
		return uiAspect;
	}
	
	public int getSelectedCinematicId() {
		return cinematicIds.getSelectedIndex();
	}
	
	private void showBadCinematicMessage() {
		JOptionPane.showMessageDialog(null,
				"An error occurred while parsing the cinematics, if you have just edited or added a new\n"
				+ "event make sure that you have filled out all of the values. If this does not fix the\n"
				+ "the problem then it is possible that your cinematic file has been corrupted.");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getClickCount() == 2 || e.getButton() == MouseEvent.BUTTON3) {
			editCinematicLine();
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		
	}
}
