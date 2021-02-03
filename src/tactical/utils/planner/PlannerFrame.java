package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.googlecode.jfilechooserbookmarks.DefaultBookmarksPanel;

import tactical.engine.TacticalGame;
import tactical.engine.log.LoggingUtils;
import tactical.engine.state.MenuState;
import tactical.loading.MapParser;
import tactical.loading.PlannerMap;
import tactical.loading.PlannerTilesetParser;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.cinematic.CinematicCreatorPanel;
import tactical.utils.planner.mapedit.MapEditorPanel;
import tactical.utils.planner.unified.UnifiedViewPanel;

public class PlannerFrame extends JFrame implements ActionListener,
		ChangeListener {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggingUtils.createLogger(PlannerFrame.class);

	private Hashtable<String, PlannerContainerDef> containersByName;
	public static ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType;
	private JTabbedPane jtp;
	private File triggerFile;
	private ArrayList<PlannerTab> plannerTabs = new ArrayList<PlannerTab>();
	private static String version = TacticalGame.VERSION;
	private CinematicCreatorPanel cinematicMapPanel;
	private MapEditorPanel mapEditorPanel;
	private UnifiedViewPanel unifiedViewPanel;
	private PlannerMap plannerMap;
	private JMenuItem exportMapItem;
	private JMenuItem playCinematicMenuItem;
	private JMenuItem changeAssociatedMapMenuItem;
	private MenuState menuState;
	private PlannerIO plannerIO = new PlannerIO();
	private JList<String> errorList = new JList<>();
	private JScrollPane errorScroll;
	public static boolean SHOW_CIN = false;
	public static boolean SHOW_CIN_LOCATION = true;

	public static final int TAB_TRIGGER = 0;
	public static final int TAB_CIN = 2;
	public static final int TAB_TEXT = 3;
	public static final int TAB_CONDITIONS = 1;
	public static final int TAB_HERO = 4;
	public static final int TAB_ENEMY = 5;
	public static final int TAB_ITEM = 6;
	public static final int TAB_QUEST = 7;
	public static final int TAB_CIN_MAP = 8;
	public static final int TAB_EDIT_MAP = 9;
	public static final int TAB_UNIFIED_VIEW = 10;
	
	private static Object saveLock = new Object();
	private static Color saveColor = Color.GREEN;
	private static JLabel saveLabel = new JLabel();
	private static SavingThread savingThread = null;
	
	private static class SavingThread implements Runnable {
		private AtomicBoolean cancel = new AtomicBoolean(false);
		
		public void cancel() {
			cancel.set(true);
		}
		
		@Override
		public void run() {
			while (!cancel.get()) {
				try {
					synchronized(saveLock) {
						
						saveColor = new Color(saveColor.getRed(), saveColor.getGreen(), saveColor.getBlue(), Math.max(0, saveColor.getAlpha() - 5));						
						saveLabel.setForeground(saveColor);
						saveLabel.repaint();
					}
					Thread.sleep(100);
				} catch (Exception e) {}
			}
		}
		
	}
	
	public static void updateSave(String text) {
		synchronized(saveLock) {
			if (savingThread != null)
				savingThread.cancel();
			saveColor = Color.BLACK;
			// savingThread = new SavingThread();
			// new Thread(savingThread).start();
			saveLabel.setText(text);		
			saveLabel.repaint();
		}
	}
	

	public static void main(String args[]) {
		PlannerFrame pf = new PlannerFrame(null);
		pf.setVisible(true);
		pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public PlannerFrame(MenuState menuState) {
		super("Planner: NO TRIGGERS LOADED " + version);

		this.menuState = menuState;
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}


		JMenuBar menuBar = new JMenuBar();
		
		createAndAddFileMenu(menuBar);
		createAndAddEditMenu(menuBar);
		createAndAddOptionsMenu(menuBar);
		createAndAddCinematicMenu(menuBar);
		
		this.setJMenuBar(menuBar);

		containersByName = new Hashtable<String, PlannerContainerDef>();

		referenceListByReferenceType = new ArrayList<ArrayList<PlannerReference>>();

		/********************/
		/* Set up referrers */
		/********************/
		PlannerDefinitions.setupRefererList(referenceListByReferenceType);

		/*******************/
		/* Set up triggers */
		/*******************/
		PlannerDefinitions.setupDefintions(referenceListByReferenceType, containersByName);			
		
		initUI();

		getSavedData();

		stateChanged(null);
	}
	
	public void getSavedData() {
		try {
			plannerIO.parseContainer(PlannerIO.PATH_ENEMIES, plannerTabs.get(TAB_ENEMY), "enemy", containersByName);
			plannerIO.parseContainer(PlannerIO.PATH_HEROES, plannerTabs.get(TAB_HERO), "hero", containersByName);
			plannerIO.parseContainer(PlannerIO.PATH_ITEMS, plannerTabs.get(TAB_ITEM), "item", containersByName);
			plannerIO.parseContainer(PlannerIO.PATH_QUESTS, plannerTabs.get(TAB_QUEST), "quest", containersByName);
			List<PlannerTab> tabsWithReferences = new ArrayList<>();
			for (int i = TAB_HERO; i <= TAB_QUEST; i++) {
				tabsWithReferences.add(plannerTabs.get(i));
			}
			
			updateErrorList(PlannerReference.establishReferences(tabsWithReferences, referenceListByReferenceType));
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred parsing the saved data, undo any changes made manually and try again:"
					+ e.getMessage(), "Error parsing data", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void createAndAddFileMenu(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		addMenuItem("New Triggers/Speech/Cinematic", "new", fileMenu);
		addMenuItem("Open Triggers/Speech/Cinematic", "open", fileMenu);
		changeAssociatedMapMenuItem = addMenuItem("Change Associated Map", "openmap", fileMenu);
		changeAssociatedMapMenuItem.setEnabled(false);
		exportMapItem = addMenuItem("Export Map", "exportmap", fileMenu);
		exportMapItem.setEnabled(false);
		addMenuItem("Reload Triggers/Speech/Cinematic", "reload", fileMenu);
		// addMenuItem("Create Triggers Based on Map", "generate", fileMenu);
		addMenuItem("Save Triggers/Speech/Cinematic", "save", fileMenu);
		addMenuItem("Save All", "saveall", fileMenu);
		addMenuItem("Export Enemy Cheat Sheet", "enemycheat", fileMenu);
		addMenuItem("Exit", "exit", fileMenu);
		menuBar.add(fileMenu);
	}
	
	private void createAndAddEditMenu(JMenuBar menuBar) {
		JMenu optionsMenu = new JMenu("Edit");
		JMenuItem findItem = new JMenuItem( "Find");
		findItem.addActionListener(this);
		findItem.setActionCommand("find");
		optionsMenu.add(findItem);
		
		JMenuItem findGlobalItem = new JMenuItem( "Global Find");
		findGlobalItem.addActionListener(this);
		findGlobalItem.setActionCommand("findglobal");
		optionsMenu.add(findGlobalItem);
		menuBar.add(optionsMenu);
	}
	
	private void createAndAddOptionsMenu(JMenuBar menuBar) {
		JMenu optionsMenu = new JMenu("Options");
		JCheckBoxMenuItem showLocationItem = new JCheckBoxMenuItem( "Show Map Locations", true);
		showLocationItem.addActionListener(this);
		showLocationItem.setActionCommand("showloc");
		optionsMenu.add(showLocationItem);
		menuBar.add(optionsMenu);
	}
	
	private void createAndAddCinematicMenu(JMenuBar menuBar) {
		JMenu cinematicMenu = new JMenu("Cinematic");
		playCinematicMenuItem = addMenuItem("Play Cinematic", "playcin", cinematicMenu);
		playCinematicMenuItem.setEnabled(false);
		menuBar.add(cinematicMenu);
	}
	
	private JMenuItem addMenuItem(String text, String actionCommand, JMenu parentMenu) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(this);
		menuItem.setActionCommand(actionCommand);
		parentMenu.add(menuItem);
		return menuItem;
	}

	

	private void initUI() {
		jtp = new JTabbedPane();

		// Add triggers
		PlannerTab tempPlannerTab = new PlannerTab("Triggers", containersByName,
				new String[] { "trigger" }, PlannerValueDef.REFERS_TRIGGER, this, TAB_TRIGGER);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Trigger Actions", tempPlannerTab.getUiAspect());

		// Add conditions
		tempPlannerTab = new PlannerTab("Conditions", containersByName,
				new String[] { "condition" }, PlannerValueDef.REFERS_CONDITIONS, this, TAB_CONDITIONS);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Condition", tempPlannerTab.getUiAspect());
		
		// Add cinematics
		tempPlannerTab = new PlannerTab("Cinematics", containersByName,
				new String[] { "cinematic" }, PlannerValueDef.REFERS_CINEMATIC,
				this, TAB_CIN);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Cinematic", tempPlannerTab.getUiAspect());

		// Add speech
		tempPlannerTab = new PlannerTab("Speeches", containersByName,
				new String[] { "text" }, PlannerValueDef.REFERS_TEXT, this, TAB_TEXT);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Speech", tempPlannerTab.getUiAspect());
		
		// Add heroes
		tempPlannerTab = new PlannerTab("Heroes", containersByName,
				new String[] { "hero" }, PlannerValueDef.REFERS_HERO, this, TAB_HERO);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Heroes", tempPlannerTab.getUiAspect());

		// Add enemies
		tempPlannerTab = new PlannerTab("Enemies", containersByName,
				new String[] { "enemy" }, PlannerValueDef.REFERS_ENEMY, this, TAB_ENEMY);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Enemies", tempPlannerTab.getUiAspect());

		// Add items
		tempPlannerTab = new PlannerTab("Items", containersByName,
				new String[] { "item" }, PlannerValueDef.REFERS_ITEM, this, TAB_ITEM);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Items", tempPlannerTab.getUiAspect());

		// Add quests
		tempPlannerTab = new PlannerTab("Quests", containersByName,
				new String[] { "quest" }, PlannerValueDef.REFERS_QUEST, this, TAB_QUEST);
		plannerTabs.add(tempPlannerTab);
		jtp.addTab("Quests", tempPlannerTab.getUiAspect());

		// Add maps
		cinematicMapPanel = new CinematicCreatorPanel(this);
		jtp.addTab("Cinematic Creator", cinematicMapPanel.getUiAspect());
		mapEditorPanel = new MapEditorPanel(this, referenceListByReferenceType);
		jtp.addTab("Map Editor", mapEditorPanel.getUIAspect());
		unifiedViewPanel = new UnifiedViewPanel(mapEditorPanel);
		jtp.addTab("Unified View" , unifiedViewPanel);
		// jtp.addTab("Battle Functions", new PlannerFunctionPanel());
		// jtp.addTab("Map Triggers", new MapTriggerPanel(containersByName));
		jtp.addChangeListener(this);
		jtp.setEnabledAt(TAB_TRIGGER, false);
		jtp.setEnabledAt(TAB_CIN, false);
		jtp.setEnabledAt(TAB_TEXT, false);
		jtp.setEnabledAt(TAB_CONDITIONS, false);
		jtp.setEnabledAt(TAB_CIN_MAP, false);
		jtp.setEnabledAt(TAB_EDIT_MAP, false);
		jtp.setEnabledAt(TAB_UNIFIED_VIEW, false);
		jtp.setSelectedIndex(TAB_HERO);
		JPanel backPanel = new JPanel(new BorderLayout());
		backPanel.add(jtp, BorderLayout.CENTER);
		errorScroll = new JScrollPane(errorList);
		errorScroll.setPreferredSize(new Dimension(errorScroll.getPreferredSize().width, 120));
		// backPanel.add(saveLabel, BorderLayout.PAGE_END);
		backPanel.add(errorScroll, BorderLayout.PAGE_END);
		this.setContentPane(backPanel);

		this.setPreferredSize(new Dimension(900, 600));
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.pack();
		this.setVisible(false);
	}

	public static JButton createActionButton(String text, String action,
			ActionListener listener) {
		JButton button = new JButton(text);
		button.setActionCommand(action);
		button.addActionListener(listener);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommand = arg0.getActionCommand();
		if (actionCommand.equalsIgnoreCase("new")) {
			JFileChooser fc = createFileChooser();
			int returnVal = fc.showSaveDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (triggerFile != null) {
					referenceListByReferenceType.get(PlannerValueDef.REFERS_TRIGGER - 1).clear();
					referenceListByReferenceType.get(PlannerValueDef.REFERS_TEXT - 1).clear();
					referenceListByReferenceType.get(PlannerValueDef.REFERS_CINEMATIC - 1)
							.clear();

					plannerTabs.get(TAB_TRIGGER).clearValues();
					plannerTabs.get(TAB_CIN).clearValues();
					plannerTabs.get(TAB_TEXT).clearValues();
					plannerTabs.get(TAB_CONDITIONS).clearValues();
				}
				
				jtp.setEnabledAt(TAB_CIN_MAP, false);
				jtp.setEnabledAt(TAB_EDIT_MAP, false);
				plannerMap = null;
				triggerFile = fc.getSelectedFile();

				this.setTitle("Planner: " + triggerFile.getName() + " " + version);

				try {
					triggerFile.createNewFile();
					openFile(triggerFile);
					saveTriggers(true);					
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "An error occurred while trying to save the trigger file:"
							+ e.getMessage(), "Error saving trigger file", JOptionPane.ERROR_MESSAGE);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "An error occurred while trying to save the trigger file:"
							+ e.getMessage(), "Error saving trigger file", JOptionPane.ERROR_MESSAGE);
					return;
				}

				jtp.setEnabledAt(TAB_TRIGGER, true);
				jtp.setEnabledAt(TAB_CIN, true);
				jtp.setEnabledAt(TAB_TEXT, true);
				jtp.setEnabledAt(TAB_CONDITIONS, true);
				jtp.setEnabledAt(TAB_UNIFIED_VIEW, true);
			}
		} else if (actionCommand.equalsIgnoreCase("open")) {
			JFileChooser fc = createFileChooser();
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				triggerFile = fc.getSelectedFile();

				openFile(triggerFile);
			}
		} else if (actionCommand.equalsIgnoreCase("openmap")) {
			promptAndLoadPlannerMap();
		} else if (actionCommand.equalsIgnoreCase("reload")) {
			if (triggerFile != null)
				openFile(triggerFile);
		} else if (actionCommand.equalsIgnoreCase("save")) {
			getDataInputTabs().stream().forEach(pt -> pt.setNewValues());

			saveTriggers(true);
		} else if (actionCommand.equalsIgnoreCase("saveall")) {
			plannerTabs.get(TAB_ENEMY).setNewValues();
			boolean success = true;
			if (!plannerIO.exportDataToFile(plannerTabs.get(TAB_ENEMY).getListPC(),
					PlannerIO.PATH_ENEMIES, false, "enemies"))
				success = false;

			plannerTabs.get(TAB_HERO).setNewValues();
			if (!plannerIO.exportDataToFile(plannerTabs.get(TAB_HERO).getListPC(),
					PlannerIO.PATH_HEROES, false, "heroes"))
				success = false;

			plannerTabs.get(TAB_ITEM).setNewValues();
			if (!plannerIO.exportDataToFile(plannerTabs.get(TAB_ITEM).getListPC(),
					PlannerIO.PATH_ITEMS, false, "items"))
				success = false;

			plannerTabs.get(TAB_QUEST).setNewValues();
			if (!plannerIO.exportDataToFile(plannerTabs.get(TAB_QUEST).getListPC(),
					PlannerIO.PATH_QUESTS, false, "quests"))
				success = false;

			saveTriggers(success);
		} else if (actionCommand.equalsIgnoreCase("exit")) {
			System.exit(0);
		}
		else if (actionCommand.equalsIgnoreCase("showloc")) {
			SHOW_CIN_LOCATION = !SHOW_CIN_LOCATION;
			((JCheckBoxMenuItem) arg0.getSource()).setSelected(SHOW_CIN_LOCATION);
			this.repaint();
		}
		else if (actionCommand.equalsIgnoreCase("exportmap"))
		{
			exportMap();
		}
		else if (actionCommand.equalsIgnoreCase("playcin")) {
			if (cinematicMapPanel.getSelectedCinematicId() != -1) {
				this.menuState.startCinematic(triggerFile.getName(), cinematicMapPanel.getSelectedCinematicId());
			} else {
				JOptionPane.showMessageDialog(this, "No cinematic has been selected yet");
			}
		} else if (actionCommand.equalsIgnoreCase("find") || actionCommand.equalsIgnoreCase("findglobal")) {
			String searchString = JOptionPane.showInputDialog(this, "Search text:", 
					(actionCommand.equalsIgnoreCase("find") ? "Find" : "Global Find"), JOptionPane.QUESTION_MESSAGE);
			
			if (searchString != null) {
				searchString = searchString.toLowerCase();
				List<SearchResult> results = null;
				if (actionCommand.equalsIgnoreCase("findglobal")) 
					results = searchGlobal(searchString);
				else
					results = searchLocal(searchString);
				
				
				if (results.size() > 0) {
					new PlannerSearchFrame(this, results);
				} else
					JOptionPane.showMessageDialog(this, "No matches were found");
			}
		}
		
	}

	private void exportMap() {
		JFileChooser fc = createFileChooser();
		fc.setSelectedFile(new File(triggerFile.getParentFile().getParentFile() + "/map/" + plannerMap.getMapName()));
		fc.setFileFilter(new FileNameExtensionFilter("Tiled Map File", "tmx"));
		int returnVal = fc.showSaveDialog(this);
		
		File newMapFile = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			newMapFile = fc.getSelectedFile();

			Path path = Paths.get(newMapFile.getAbsolutePath());
			try {
				Files.write(path, plannerMap.outputNewMap().getBytes());
				JOptionPane.showMessageDialog(this, "The map was exported successfully");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred while trying to save the map:"
						+ e.getMessage(), "Error saving map", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private List<SearchResult> searchLocal(String searchString) {
		List<SearchResult> results = new ArrayList<>();
		searchPlannerTabs(getDataInputTabs(), searchString, results, null);
		return results;
	}
	
	private List<SearchResult> searchGlobal(String searchString) {
		
		List<SearchResult> results = new ArrayList<>();
		
		// Get the non map related tab search results first and only once
		List<PlannerTab> nonMapTabs = new ArrayList<>();
		nonMapTabs.add(plannerTabs.get(TAB_ENEMY));
		nonMapTabs.add(plannerTabs.get(TAB_HERO));
		nonMapTabs.add(plannerTabs.get(TAB_ITEM));
		nonMapTabs.add(plannerTabs.get(TAB_QUEST));
		searchPlannerTabs(nonMapTabs, searchString, results, null);
		
		// Go through all of the map data files searching
		for (File file : new File(PlannerIO.PATH_MAPDATA).listFiles())
		{
			ArrayList<TagArea> tagAreas = null;
			try {
				tagAreas = XMLParser.process(Files.readAllLines(
					Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8), true);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, 
						"An error occurred while trying to parse file " + file.getPath() + " it will not be included in search results");
				e.printStackTrace();
				continue;
			}
			List<PlannerTab> tempTabs = new ArrayList<>();
			// Add triggers
			tempTabs.add(new PlannerTab("Triggers", containersByName,
					new String[] { "trigger" }, PlannerValueDef.REFERS_TRIGGER, this, TAB_TRIGGER));

			// Add conditions
			tempTabs.add(new PlannerTab("Conditions", containersByName,
					new String[] { "condition" }, PlannerValueDef.REFERS_CONDITIONS, this, TAB_CONDITIONS));
			
			// Add cinematics
			tempTabs.add(new PlannerTab("Cinematics", containersByName,
					new String[] { "cinematic" }, PlannerValueDef.REFERS_CINEMATIC,
					this, TAB_CIN));

			// Add speech
			tempTabs.add(new PlannerTab("Speeches", containersByName,
					new String[] { "text" }, PlannerValueDef.REFERS_TEXT, this, TAB_TEXT));
		
			plannerIO.parseContainer(tagAreas, tempTabs.get(0), "trigger", containersByName);
			plannerIO.parseContainer(tagAreas, tempTabs.get(3), "text", containersByName);
			plannerIO.parseContainer(tagAreas, tempTabs.get(2), "cinematic", containersByName);
			plannerIO.parseContainer(tagAreas, tempTabs.get(1), "condition", containersByName);
			
			searchPlannerTabs(tempTabs, searchString, results, file.getName());
		}
		
		return results;
	}
	
	private void searchPlannerTabs(List<PlannerTab> plannerTabs, String searchString, 
			List<SearchResult> results, String optionalFile) {
		for (PlannerTab pt : plannerTabs) {
			for (PlannerContainer pc : pt.getListPC()) {
				searchPlannerLine(searchString, pt, pc, pc.getDefLine(), results, optionalFile);
				for (PlannerLine pl : pc.getLines())
					searchPlannerLine(searchString, pt, pc, pl, results, optionalFile);
			}
		}
	}
	
	private void searchPlannerLine(String searchString, PlannerTab pt, 
			PlannerContainer pc, PlannerLine pl, List<SearchResult> results, String optionalFile) {
		for (Object o : pl.getValues()) {
			
			
			String searchMe = null;
			if (o != null) {
				if (o instanceof String && 
						((String) o).trim().length() > 0) {
					searchMe = ((String) o).trim();
				}
				else if (o instanceof PlannerReference && 
						((PlannerReference) o).getName() != null &&
						((PlannerReference) o).getName().trim().length() > 0)
					searchMe = ((PlannerReference) o).getName().trim();
			}
			
			if (searchMe != null && searchMe.toLowerCase().contains(searchString)) {
				results.add(new SearchResult(pt, pc, pl, searchMe, optionalFile));
			}
		}
	}
	
	class SearchResult {
		public final PlannerTab pt; 
		public final PlannerContainer pc; 
		public final PlannerLine pl;
		public final String value;
		public final String file;
		
		public SearchResult(PlannerTab pt, PlannerContainer pc, PlannerLine pl, String value, String file) {
			super();
			this.pt = pt;
			this.pc = pc;
			this.pl = pl;
			this.value = value;
			this.file = file;
		}
	}
	
	private boolean promptAndLoadPlannerMap() 
	{
		JFileChooser fc = createFileChooser();
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (triggerFile == null)
				JOptionPane.showMessageDialog(this, "You have opened a map file without first opening a trigger file.\n"
					+ "Keep in mind that map locations of the type 'trigger' and 'battletrigger' require\n"
					+ "the correct trigger file open so that they may be viewed correctly. As such triggers\n"
					+ "and battletriggers in the map view may not work correctly until the appropriate text file is loaded.");

			return loadPlannerMap(fc.getSelectedFile().getName());
		} else {
			return false;
		}
	}

	private boolean loadPlannerMap(String fileName) {
		plannerMap = new PlannerMap(fileName, referenceListByReferenceType.get(PlannerValueDef.REFERS_LOCATIONS - 1));

		try {
			MapParser.parseMap(new File(triggerFile.getParentFile().getParentFile() + "/map/" + fileName).getAbsolutePath(), plannerMap, new PlannerTilesetParser(), null);
			
			if (!plannerMap.validateLayers())
			{
				JOptionPane.showMessageDialog(null, 
						"The loaded map contains Object Layers that do not conform to the map naming schema."
						+ "\nOnly layers named 'Terrain', 'Meta', 'Trigger Region' and 'Battle' are allowed, \n"
						+ "all other layers will be removed upon map export", "Unknown Object Layers Found", JOptionPane.ERROR_MESSAGE);
			}
			
			if (plannerMap.getMapLayerAmount() < 4) {
				JOptionPane.showMessageDialog(this, "The selected map has too few layers to be compatible with the engine.\n "
						+ "Add at least 5 layers 'BG, BG Shadow, MG, MG Shadow, Walkable' to prevent errors.");
			}
			
			if (!plannerMap.hasMoveableLayer()) {
				JOptionPane.showMessageDialog(this, "The selected map contains no 'walkable' layer definition and is not compatible with the engine.\n "
						+ "Add a 'Walkable' layer to prevent errors");
			}
			
			Collections.sort(referenceListByReferenceType.get(PlannerValueDef.REFERS_LOCATIONS - 1), new Comparator<PlannerReference>() {
				@Override
				public int compare(PlannerReference o1, PlannerReference o2) { return o1.getName().compareTo(o2.getName()); }});
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(this, "An error occurred while loading the selected map: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		exportMapItem.setEnabled(true);
		ArrayList<PlannerTab> tabsWithMapRefs = getTabsWithMapReferences();
		plannerMap.setTabsWithMapReferences(tabsWithMapRefs);
		
		mapEditorPanel.loadMap(plannerMap, fileName, tabsWithMapRefs);
		cinematicMapPanel.loadMap(plannerMap);
		unifiedViewPanel.loadMap(plannerMap, tabsWithMapRefs);
		jtp.setEnabledAt(TAB_CIN_MAP, true);
		jtp.setEnabledAt(TAB_EDIT_MAP, true);
		jtp.setSelectedIndex(TAB_EDIT_MAP);
		return true;
	}

	public void openFile(File triggerFile) {
		this.triggerFile = triggerFile;
		openFile(triggerFile, true);
	}
	
	public void openFile(File triggerFile, boolean reportNoMap) {
		try {
			ArrayList<TagArea> tagAreas = XMLParser.process(Files.readAllLines(
					Paths.get(triggerFile.getAbsolutePath()), StandardCharsets.UTF_8), true);
			String mapFile = null;
			for (TagArea ta : tagAreas)
				if (ta.getTagType().equalsIgnoreCase("map")) {
					mapFile = ta.getAttribute("file");
					break;
				}
			
			referenceListByReferenceType.get(PlannerValueDef.REFERS_LOCATIONS - 1).clear();
			plannerMap = null;
			
			if (mapFile == null) {
				if (reportNoMap) {
					JOptionPane.showMessageDialog(this, "No map file has been associated with this map data, please choose a map now");
					if (!promptAndLoadPlannerMap())
						return;
				}
			} else {
				if (!loadPlannerMap(mapFile))
					return;
			}
			
			referenceListByReferenceType.get(PlannerValueDef.REFERS_TRIGGER - 1).clear();
			referenceListByReferenceType.get(PlannerValueDef.REFERS_TEXT - 1).clear();
			referenceListByReferenceType.get(PlannerValueDef.REFERS_CINEMATIC - 1).clear();

			plannerTabs.get(TAB_TRIGGER).clearValues();
			plannerTabs.get(TAB_CIN).clearValues();
			plannerTabs.get(TAB_TEXT).clearValues();
			plannerTabs.get(TAB_CONDITIONS).clearValues();
			
			plannerTabs.get(TAB_TRIGGER).updateAttributeList(-1);
			plannerTabs.get(TAB_CIN).updateAttributeList(-1);
			plannerTabs.get(TAB_TEXT).updateAttributeList(-1);
			plannerTabs.get(TAB_CONDITIONS).updateAttributeList(-1);

			this.setTitle("Planner: " + triggerFile.getName() + " " + version);
			
			this.changeAssociatedMapMenuItem.setEnabled(true);
			
			plannerIO.parseContainer(tagAreas, plannerTabs.get(TAB_TRIGGER), "trigger", containersByName);
			plannerIO.parseContainer(tagAreas, plannerTabs.get(TAB_TEXT), "text", containersByName);
			plannerIO.parseContainer(tagAreas, plannerTabs.get(TAB_CIN), "cinematic", containersByName);
			plannerIO.parseContainer(tagAreas, plannerTabs.get(TAB_CONDITIONS), "condition", containersByName);
			
			PlannerReference.establishReferences(getTabsWithMapReferences(), referenceListByReferenceType);
			updateErrorList(PlannerReference.getBadReferences(getDataInputTabs()));
			
			unifiedViewPanel.resetPanel();
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred while trying to open the file:"
					+ e.getMessage(), "Error opening file", JOptionPane.ERROR_MESSAGE);
			return;
		}


		jtp.setEnabledAt(TAB_TRIGGER, true);
		jtp.setEnabledAt(TAB_CIN, true);
		jtp.setEnabledAt(TAB_TEXT, true);
		jtp.setEnabledAt(TAB_CONDITIONS, true);
		jtp.setEnabledAt(TAB_UNIFIED_VIEW, true);
	}

	private ArrayList<PlannerTab> getTabsWithMapReferences() {
		ArrayList<PlannerTab> tabsWithReferences = new ArrayList<>();
		for (int i = TAB_TRIGGER; i <= TAB_TEXT; i++) {
			tabsWithReferences.add(plannerTabs.get(i));
		}
		return tabsWithReferences;
	}

	private void saveTriggers(boolean previousExportsSuccessful) {
		if (triggerFile != null) {
			LOGGER.fine("SAVE");
			Path path = Paths.get(triggerFile.getAbsolutePath());
			List<String> buffer = new ArrayList<>();
			buffer.add("<area>");
			buffer.add("<map file=\"" + plannerMap.getMapName() +"\"/>");
			
			try {
				plannerTabs.get(TAB_TRIGGER).setNewValues();
				buffer.addAll(PlannerIO.export(plannerTabs.get(TAB_TRIGGER).getListPC(), null));
	
				plannerTabs.get(TAB_TEXT).setNewValues();
				buffer.addAll(PlannerIO.export(plannerTabs.get(TAB_TEXT).getListPC(), null));
	
				plannerTabs.get(TAB_CIN).setNewValues();
				buffer.addAll(PlannerIO.export(plannerTabs.get(TAB_CIN).getListPC(), null));
				
				plannerTabs.get(TAB_CONDITIONS).setNewValues();
				buffer.addAll(PlannerIO.export(plannerTabs.get(TAB_CONDITIONS).getListPC(), null));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred while trying to format the trigger data:"
						+ e.getMessage(), "Error saving data", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			buffer.add("</area>");
			try {
				Files.write(path, buffer, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred while trying to save the data:"
						+ e.getMessage(), "Error saving data", JOptionPane.ERROR_MESSAGE);				
				return;
			}
		}

		if (previousExportsSuccessful) {
			int rc = JOptionPane.showConfirmDialog(this, 
					"The file was saved successfully.\nWould you also like to export the map at this time (Highly recommended)", 
					"Save Successful", JOptionPane.YES_NO_OPTION);
			if (rc == JOptionPane.YES_OPTION)
				exportMap();
		}
	}

	

	@Override
	public void stateChanged(ChangeEvent e) {
		// Commit changes for each tab that this isn't non-sensical for 
		getDataInputTabs().stream().forEach(pt -> pt.commitChanges());
		
		playCinematicMenuItem.setEnabled(false);

		if (jtp.getSelectedIndex() == TAB_CIN_MAP)
		{
			cinematicMapPanel.reloadCinematicItem();
			playCinematicMenuItem.setEnabled(true);
		} else if (jtp.getSelectedIndex() == TAB_UNIFIED_VIEW) {
			unifiedViewPanel.panelSelected();
		}
		
		if (jtp.getSelectedIndex() <= TAB_QUEST) {
			plannerTabs.get(jtp.getSelectedIndex()).setNewValues();
		}
	}

	public PlannerTab getPlannerTabAtIndex(int index)
	{
		return plannerTabs.get(index);
	}

	public void setSelectedTabIndex(int index)
	{
		jtp.setSelectedIndex(index);
	}
	
	public void setSelectedTab(PlannerTab pt) { 
		
	}

	public PlannerContainerDef getContainerDefByName(String name)
	{
		return this.containersByName.get(name);
	}

	public static JFileChooser createFileChooser()
	{
		JFileChooser jfc = new JFileChooser();
		DefaultBookmarksPanel panel = new DefaultBookmarksPanel();
		panel.setOwner(jfc);
		jfc.setAccessory(panel);
		jfc.setPreferredSize(new Dimension(800, 600));
		return jfc;
	}
	
	public List<PlannerTab> getDataInputTabs() {
		List<PlannerTab> referenceTabs = new ArrayList<>();
		for (int i = 0; i < jtp.getTabCount() - 3; i++) {
			referenceTabs.add(plannerTabs.get(i));
		}
		return referenceTabs;
	}
	
	public void updateErrorList(List<String> errors) {
		this.getContentPane().remove(errorScroll);
		if (errors.size() > 0) {
			String[] list = new String[errors.size()];
			errorList.setListData(errors.toArray(list));
			errorList.validate();
			errorList.repaint();
			this.getContentPane().add(errorScroll, BorderLayout.PAGE_END);
		}
		this.validate();
		this.repaint();
	}

	public boolean hasPlannerMap() {
		return plannerMap != null;
	}

	public PlannerMap getPlannerMap() {
		return plannerMap;
	}

	public UnifiedViewPanel getUnifiedViewPanel() {
		return unifiedViewPanel;
	}
}
