package tactical.engine.state.devel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.JOptionPane;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.particles.ParticleSystem;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import mb.fc.utils.gif.GifFrame;
import tactical.engine.TacticalGame;
import tactical.engine.load.BulkLoader;
import tactical.engine.state.MenuState;
import tactical.engine.state.PersistentStateInfo;
import tactical.game.dev.DevParams;
import tactical.game.exception.BadResourceException;
import tactical.game.hudmenu.Panel;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.resource.SpellResource;
import tactical.game.ui.Button;
import tactical.game.ui.ListUI;
import tactical.game.ui.ListUI.ResourceSelectorListener;
import tactical.game.ui.PaddedGameContainer;
import tactical.game.ui.ResourceSelector;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingScreenRenderer;
import tactical.loading.LoadingState;
import tactical.loading.MapParser;
import tactical.loading.ResourceManager;
import tactical.loading.TilesetParser;
import tactical.map.Map;
import tactical.map.MapObject;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.progression.ProgressionFrame;

/**
 * Development menu state that allows loading maps programatically
 *
 * @author Broked
 *
 */
public class DevelMenuState extends MenuState implements ResourceSelectorListener
{
	private ResourceSelector textSelector;
	private ResourceSelector loadoutSelector;
	private ListUI entranceSelector;
	private PlannerFrame plannerFrame = null;
	private GifFrame quickAnimate = new GifFrame(true);
	private ProgressionFrame progressionFrame = new ProgressionFrame();
	public static ParticleSystem ps;
	private String currentMap;
	private Button loadTownButton = new Button(0, 550, 150, 25, "Load Town");
	private Button loadCinButton = new Button(0, 580, 150, 25, "Load Cin");
	private Button loadBattleButton = new Button(0, 610, 150, 25, "Load Battle");
	
	protected int totalResources = 0;
	private ResourceManager mainGameFCRM = null;
	protected BulkLoader mainGameBulkLoader = null;
	protected AlertPanel alertPanel = null;
	protected Image backgroundIm = null;


	public DevelMenuState(PersistentStateInfo persistentStateInfo) {
		super(persistentStateInfo);
	}
	
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		super.init(container, game);
		this.game = game;

		textSelector = new ResourceSelector("Select Text", 0, true, "mapdata", "", container);
		textSelector.setListener(this);
		textSelector.setIgnoreClicksInUpdate(true);
		
		loadoutSelector = new ResourceSelector("Select Loadout", 20, false, "loadouts", "", container);
		
		loadTownButton.setEnabled(false);
		loadBattleButton.setEnabled(false);
		loadCinButton.setEnabled(false);
		
		if (!LoadingState.inJar)
			 plannerFrame = new PlannerFrame(this);
		
		backgroundIm = new Image("image/engine/BlackGeo.jpg");
		
		// Checks to see if any mapdata has errors
		/*
		for (File f : new File("mapdata").listFiles()) {
			if (f.isFile()) {
				System.out.println("Checking file: " + f.getName());
				plannerFrame.openFile(f, false);
				if (!plannerFrame.hasPlannerMap())
					System.out.println("Has no planner map assigned");
				PlannerReference.getBadReferences(plannerFrame.getDataInputTabs()).forEach(s -> System.out.println(s));
			}
		}*/
		/*
		for (String spell : GlobalPythonFactory.createJSpell().getSpellList()) {
			JSpell jspell = GlobalPythonFactory.createJSpell().init(spell);
			System.out.println(spell);
			System.out.println(jspell.getId());
			System.out.println(jspell.getMaxLevel());
			System.out.println(jspell.getName());
			for (int i = 0; i < jspell.getMaxLevel(); i++) {
				System.out.println(jspell.getSpellAnimationFile(i));
				System.out.println(jspell.getSpellRainAnimationFile(i));
				System.out.println(jspell.getSpellRainAnimationName(i));
				System.out.println(jspell.getSpellRainFrequency(i));
				System.out.println(jspell.getSpellOverlayColor(i));
				System.out.println(jspell.getArea()[i]);
				System.out.println(jspell.getCosts()[i]);
				System.out.println(jspell.getDamage()[i]);
				System.out.println(jspell.getMpDamage()[i]);
				System.out.println(jspell.getRange()[i]);
			}
			System.out.println(jspell.getSpellIconId());
			System.out.println(jspell.isLoops());
			System.out.println(jspell.isTargetsEnemy());
		}
		*/
		
		
		//ps = new ParticleSystem(new Image("image/RainBig.png"));
		//RainEmitter rainEmitter = new RainEmitter(500, 100, true);
		//ps.addEmitter(rainEmitter);
	}
	
	

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		Log.debug("Entered DevelMenuState");
		SpellResource.initSpells(null);
		this.progressionFrame.init();
		initializeBulkLoader();
		container.getInput().removeAllListeners();
		container.getInput().addKeyListener(this);
		container.getInput().addMouseListener(this);
		
		
		textSelector.registerListeners(container);
		loadoutSelector.registerListeners(container);		
	}

	protected void initializeBulkLoader() {
		if (mainGameBulkLoader == null) {
			mainGameFCRM = new ResourceManager();
			mainGameBulkLoader = new BulkLoader(mainGameFCRM);
			
			try {
				mainGameBulkLoader.start("/loader/Default");
			} catch (IOException e) {
				throw new BadResourceException("No default resource loader could be found at: ./loader/Default");
			}
			
			totalResources = mainGameBulkLoader.getResourceAmount();
		}
	}


	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException
	{
		g.clearClip();
		g.drawImage(backgroundIm.getScaledCopy(.5f), 0, 0);
		g.setColor(Color.red);
		g.drawString("DEVELOPMENT MODE", 5, 695);

		g.setColor(Color.white);
		
		
		g.fillRect(container.getWidth() / 2, container.getHeight() - 60, 
				170 * (totalResources - mainGameBulkLoader.getResourceAmount()) / totalResources, 15) ;

		g.setColor(Color.blue);
		g.drawRect(container.getWidth() / 2, container.getHeight() - 60, 
				170, 15);
		g.setColor(Color.white);
		
		textSelector.render(container, g);

		if (entranceSelector != null)
			entranceSelector.render(container, g);
		
		loadoutSelector.render(container, g);

		loadTownButton.render(g);
		loadBattleButton.render(g);
		loadCinButton.render(g);

		g.drawString(version, container.getWidth() / 2, container.getHeight() - 30);
		
		int amount = 8;
		g.drawString("F1 - Toggle Main/Dev Menu", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F2 - Open Planner", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F3 - Open Quick Animator", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F4 - Open Animation Viewer", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		// g.drawString("F5 - Run Test", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F6 - Open Battle Viewer", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F8 - Load Saved Game", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F10 - Open Progression Viewer", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		g.drawString("F11 - Show Credits", container.getWidth() - 250, container.getHeight() - amount-- * 30);
		

		if (initialized && ps != null)
		{
			g.scale(3, 3);
			ps.render();
			g.resetTransform();
		}
		
		if (alertPanel != null) {			
			alertPanel.render(g);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException
	{
		if (alertPanel != null) {
			if (alertPanel.update(container.getInput()) == MenuUpdate.MENU_CLOSE)
				alertPanel = null;
			return;
		}
		
		int x = container.getInput().getMouseX();
		int y = container.getInput().getMouseY();

		loadTownButton.handleUserInput(x, y, false);
		loadCinButton.handleUserInput(x, y, false);
		loadBattleButton.handleUserInput(x, y, false);
		
		if (updateDelta > 0)
			updateDelta = Math.max(0, updateDelta - delta);
		
		textSelector.update(container, delta);
		loadoutSelector.update(container, delta);

		if (entranceSelector != null) {
			entranceSelector.update(container, delta);
			loadTownButton.setEnabled(entranceSelector.getSelectedResource() != null);
			loadBattleButton.setEnabled(entranceSelector.getSelectedResource() != null);
		}

		if (initialized && ps != null)
		{
			ps.update(delta);
		}
		
		if (initialized)
			mainGameBulkLoader.update();		
	}
	
	@Override
	public void keyPressed(int key, char c) {
		if (updateDelta <= 0) {
			updateDelta += 50;
			if (key == Input.KEY_F1)
			{
				((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo("/menu/MainMenu", false, true,
						new ResourceManager(),
							(LoadableGameState) game.getState(TacticalGame.STATE_GAME_MENU),
								new LoadingScreenRenderer(this.container));
	
				game.enterState(TacticalGame.STATE_GAME_LOADING);
			}
	
			if (key == Input.KEY_F2 || key == Input.KEY_P)
			{
				if (!plannerFrame.isVisible()) {
					plannerFrame.setVisible(true);
					plannerFrame.requestFocus();
					plannerFrame.toFront();
				}
			}
	
			if (key == Input.KEY_F3)
			{
				if (!quickAnimate.isVisible())
					quickAnimate.setVisible(true);
			}
	
			if (key == Input.KEY_F4)
			{
				game.enterState(TacticalGame.STATE_GAME_ANIM_VIEW);
			}
	
			/*
			if (container.getInput().isKeyPressed(Input.KEY_F5))
			{
				CommRPG.TEST_MODE_ENABLED = true;
				this.gameSetup(game, container);
				start(LoadTypeEnum.CINEMATIC, "neweriumcastle", null);
			}
			*/
			
			if (key == Input.KEY_F6)
			{
				((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo("eriumjail", true, true,
						new ResourceManager(),
							(LoadableGameState) game.getState(TacticalGame.STATE_GAME_BATTLE_ANIM_VIEW),
								new LoadingScreenRenderer(container));
	
				game.enterState(TacticalGame.STATE_GAME_LOADING);
			}
	
			if (key == Input.KEY_F7)
			{
				try {
					
					((PaddedGameContainer) this.container).toggleFullScreen();
				} catch (SlickException e) {
					Log.error("Unable to toggle fullscreen mode: " + e.getMessage());
					alertPanel = new AlertPanel("Unable to toggle fullscreen mode: " + e.getMessage());
				}
				updateDelta = 200;
			}
			
			if (key == Input.KEY_F8)
			{
				TacticalGame.TEST_MODE_ENABLED = false;
				LoadTypeEnum loadType = LoadTypeEnum.TOWN;
				if (persistentStateInfo.getClientProgress().isBattle())
					loadType = LoadTypeEnum.BATTLE;
				start(loadType, persistentStateInfo.getClientProgress().getMapData(), null);
			}
			
			if (key == Input.KEY_F9)
			{
				TacticalGame.TEST_MODE_ENABLED = true;
				TacticalGame.BATTLE_MODE_OPTIMIZE = true;
				if (textSelector.getSelectedResource() != null && 
						entranceSelector.getSelectedResource() != null)
					start(LoadTypeEnum.BATTLE, textSelector.getSelectedResource(), 
							entranceSelector.getSelectedResource());
			}
			if (key == Input.KEY_F10)
			{
				progressionFrame.setVisible(true);
			}
			
			if (key == Input.KEY_F11)
			{	
				game.enterState(TacticalGame.STATE_GAME_CREDITS);
			}
		}
	}
	
	

	@Override
	public void startCinematic(String mapData, int cinematicId) {
		// This whole line of logic is somewhat terrifying... We set the resource manager
		// of the psi to the one that the bulkloader is using it is NOT set in the
		// state info at this point. It will be set in the state info and PSI (again)
		// once the town/cin/battle state loads. What's more concerning is that we manually
		// set the loading states bulk loader here and it we will use the same bulk loader
		// for the rest of the game after we get past the menu state. Ideally it would be nice
		// to pass the bulkloader along on these load* calls (below), but there currently isn't
		// a use case for that now
		persistentStateInfo.setResourceManager(mainGameFCRM);
		((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setBulkLoader(mainGameBulkLoader);
		
		super.startCinematic(mapData, cinematicId);
	}



	@Override
	public void mousePressed(int button, int x, int y) {
		if (updateDelta <= 0) {
			if (button == Input.MOUSE_LEFT_BUTTON)
			{
				this.updateDelta = 200;
				
				if (loadTownButton.handleUserInput(x, y, true)) {
					// This whole line of logic is somewhat terrifying... We set the resource manager
					// of the psi to the one that the bulkloader is using it is NOT set in the
					// state info at this point. It will be set in the state info and PSI (again)
					// once the town/cin/battle state loads. What's more concerning is that we manually
					// set the loading states bulk loader here and it we will use the same bulk loader
					// for the rest of the game after we get past the menu state. Ideally it would be nice
					// to pass the bulkloader along on these load* calls (below), but there currently isn't
					// a use case for that now
					persistentStateInfo.setResourceManager(mainGameFCRM);
					((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setBulkLoader(mainGameBulkLoader);
					applyDevParams();
					start(LoadTypeEnum.TOWN, textSelector.getSelectedResource(), entranceSelector.getSelectedResource());
				}
				if (loadCinButton.handleUserInput(x, y, true)) {
					String id = JOptionPane.showInputDialog("Enter the cinematic id (a number) to run");
					try {
						int iId = Integer.parseInt(id);	
						applyDevParams();
						startCinematic(textSelector.getSelectedResource(), iId);
					} catch (NumberFormatException e) {
						alertPanel = new AlertPanel("The value must be a number: " + e.getMessage());
					}
					
				}
				if (loadBattleButton.handleUserInput(x, y, true)) {					
					applyDevParams();
					startBattle();
				}
				
				this.textSelector.handleInput(x, y, true);
				if (entranceSelector != null)
					entranceSelector.handleInput(x, y, true);
			}
		}
	}
	
	private void applyDevParams() {
		if (loadoutSelector.getSelectedResource() != null)	
			DevParams.parseDevParams(loadoutSelector.getSelectedResource(), persistentStateInfo.getClientProfile(), 
				((TacticalGame) game).getEngineConfigurator().getConfigurationValues().getStartingHeroIds());
	}
	
	private boolean startBattle() {
		// This whole line of logic is somewhat terrifying... We set the resource manager
		// of the psi to the one that the bulkloader is using it is NOT set in the
		// state info at this point. It will be set in the state info and PSI (again)
		// once the town/cin/battle state loads. What's more concerning is that we manually
		// set the loading states bulk loader here and it we will use the same bulk loader
		// for the rest of the game after we get past the menu state. Ideally it would be nice
		// to pass the bulkloader along on these load* calls (below), but there currently isn't
		// a use case for that now
		persistentStateInfo.setResourceManager(mainGameFCRM);
		((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setBulkLoader(mainGameBulkLoader);
		start(LoadTypeEnum.BATTLE, textSelector.getSelectedResource(), entranceSelector.getSelectedResource());
		return false;
	}


	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		this.initialized = true;
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_MENU_DEVEL;
	}

	@Override
	public boolean resourceSelected(String selectedItem,
			ListUI parentSelector) {
		
		try {			
			String firstLine = ResourceManager.readAllLines("/mapdata/" + selectedItem).get(1);			
			
			if (firstLine.startsWith("<map")) {
				ArrayList<TagArea> tagArea = XMLParser.process(Collections.singletonList(firstLine), false);
				currentMap = tagArea.get(0).getAttribute("file");
			} else {
				alertPanel = new AlertPanel("The selected map data has not had a map associated with it yet.\\n Load the mapdata in the planner to assign a map");
				return false;
			}
		} catch (FileNotFoundException e) {
			alertPanel = new AlertPanel("The selected file could not be found: " + e.getMessage());
			return false;
		} catch (IOException e) {
			alertPanel = new AlertPanel("The selected file could not be read or is improperly formatted: " + e.getMessage());
			return false;
		}
		
		
		Map map = new Map();
		ArrayList<String> entrances = new ArrayList<>();
		try {
			MapParser.parseMap("/map/" + currentMap, map, new TilesetParser(), null);

			for (MapObject mo : map.getMapObjects())
				if (mo.getKey().equalsIgnoreCase("start"))
					entrances.add(mo.getParam("exit"));

			if (entranceSelector != null) {
				entranceSelector.unregisterListeners(container);
			}
			
			entranceSelector = new ListUI(container, "Select Entrance", (((PaddedGameContainer) container).getPaddedWidth() - 150) / 2, entrances);
			entranceSelector.setIgnoreClicksInUpdate(true);
			loadCinButton.setEnabled(true);
			return true;

		} catch (Throwable t) {
			t.printStackTrace();
			alertPanel = new AlertPanel("The selected map " + currentMap + " contains errors \\n and may not be loaded: " + t.getMessage());
			entranceSelector = null;
		}
		
		loadCinButton.setEnabled(false);
		return false;
	}	
	
	private class AlertPanel {

		private List<Button> buttons = new ArrayList<Button>();
		private List<BooleanSupplier> reactions = new ArrayList<>();
		private String[] text;
		
		public AlertPanel(String text) {
			this.text = text.split("\\\\n");
			Button b1 = new Button(300, 270, 80, 20, "Ok");
			buttons.add(b1);
			reactions.add(null);
			// b1 = new Button(400, 270, 80, 20, "Ok");
			// buttons.add(b1);			
		}
		
		public AlertPanel(String text, BooleanSupplier bs1, BooleanSupplier bs2) {
			this.text = text.split("\\\\n");
			Button b1 = new Button(300, 270, 80, 20, "Yes");
			buttons.add(b1);
			reactions.add(bs1);
			b1 = new Button(400, 270, 80, 20, "No");
			buttons.add(b1);
			reactions.add(bs2);
		}

		public void render(Graphics graphics) {
			graphics.setColor(Color.white);
			Panel.fillRect(new Rectangle(270, 200, 400, 100), graphics);
			graphics.setColor(Color.blue);
			Panel.drawRect(new Rectangle(270, 200, 400, 100), graphics);
			graphics.setColor(Color.black);
			for (int i = 0; i < text.length; i++) {
				graphics.drawString(text[i], 280, 210 + 25 * i);
			}			
			for (Button b : buttons)
				b.render(graphics);
		}

		public MenuUpdate update(Input inp) {
			boolean pressed = inp.isMousePressed(Input.MOUSE_LEFT_BUTTON);
			for (int i = 0; i < buttons.size(); i++) {
				Button b = buttons.get(i);
				if (b.handleUserInput(inp.getMouseX(), inp.getMouseY(), pressed))
				{
					if (reactions.get(i) != null) {
						reactions.get(i).getAsBoolean();
					}
					
					return MenuUpdate.MENU_CLOSE;
				}
			}
			return MenuUpdate.MENU_NO_ACTION;
		}		
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		container.getInput().removeAllListeners();
	}
	
	
}
