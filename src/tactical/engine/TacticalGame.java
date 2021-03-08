package tactical.engine;

import java.io.File;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import tactical.engine.config.EngineConfigurator;
import tactical.engine.config.provided.DefaultEngineConfiguration;
import tactical.engine.log.FileLogger;
import tactical.engine.log.LoggingUtils;
import tactical.engine.state.BattleState;
import tactical.engine.state.ChapterState;
import tactical.engine.state.CinematicState;
import tactical.engine.state.CreditsState;
import tactical.engine.state.IntroState;
import tactical.engine.state.MenuState;
import tactical.engine.state.PersistentStateInfo;
import tactical.engine.state.TownState;
import tactical.engine.state.devel.DevelAnimationViewState;
import tactical.engine.state.devel.DevelBattleAnimViewState;
import tactical.engine.state.devel.DevelMenuState;
import tactical.game.Camera;
import tactical.game.persist.ClientProfile;
import tactical.game.persist.ClientProgress;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingScreenRenderer;
import tactical.loading.LoadingState;
import tactical.loading.ResourceManager;
import tactical.loading.TextParser;
import tactical.utils.planner.PlannerFrame;

/**
 * Entry point to the Tactical Engine
 *
 * @author Broked
 *
 */
public abstract class TacticalGame extends StateBasedGame   {
	private static final Logger LOGGER = LoggingUtils.createLogger(TacticalGame.class);
	
	public static final int STATE_GAME_MENU_DEVEL = 1;

	/**
	 * State that displays and allows interaction with the "Start Menu"
	 */
	public static final int STATE_GAME_MENU = 2;

	/**
	 * State in which the game is actually being played
	 */
	public static final int STATE_GAME_BATTLE = 3;

	public static final int STATE_GAME_CINEMATIC= 4;

	public static final int STATE_GAME_LOADING = 5;

	/**
	 * State in which the game is actually being played
	 */
	public static final int STATE_GAME_BATTLE_ANIM = 6;

	public static final int STATE_GAME_TEST = 7;

	public static final int STATE_GAME_ANIM_VIEW = 8;
	public static final int STATE_GAME_MENU_MULTI = 9;
	/**
	 * State in which the game is actually being played
	 */
	public static final int STATE_GAME_TOWN = 10;
	
	public static final int STATE_GAME_BATTLE_ANIM_VIEW = 11;
	public static final int STATE_GAME_CREDITS = 12;
	public static final int STATE_GAME_PLANNER = 13;
	public static final int STATE_GAME_CHAPTER = 14;
	public static final int STATE_GAME_INTRO = 15;

	/**
	 * A global random number generator
	 */
	public static Random RANDOM = new Random();

	private LoadingState loadingState;

	public static String IP;

	public static String VERSION;
	public static String FILE_VERSION;

	public static String GAME_TITLE;

	public static boolean TEST_MODE_ENABLED = false; //true;

	public static boolean DEV_MODE_ENABLED = false;
	
	public static boolean BATTLE_MODE_OPTIMIZE = false;
	
	public static boolean MUTE_MUSIC = false;
	
	private PersistentStateInfo persistentStateInfo;
	
	public static TextParser TEXT_PARSER = new TextParser();
	
	public static EngineConfigurator ENGINE_CONFIGURATIOR = new DefaultEngineConfiguration();
	
	private boolean plannerMode = false;
	
	public TacticalGame(String gameTitle, String version, boolean devMode, String[] gameArgs)
	{
		super(gameTitle);
		GAME_TITLE = gameTitle;
		VERSION = version;
		DEV_MODE_ENABLED = devMode;
		
		TacticalGame.ENGINE_CONFIGURATIOR = getEngineConfigurator();
		
		if (gameArgs.length > 0) {
			if (gameArgs[0].equalsIgnoreCase("injar")) {
				LOGGER.fine("Running in jar");
				LoadingState.inJar = true;
				DEV_MODE_ENABLED = false;
			} else if (gameArgs[0].equalsIgnoreCase("planner")) {
				plannerMode = true;
				PlannerFrame pf = new PlannerFrame(null);
				pf.setVisible(true);
			}
		}
		
		Log.setLogSystem(new FileLogger());
	}
	
	public abstract EngineConfigurator getEngineConfigurator();
	
	public void setup() 
	{
		if (plannerMode)
			return;
		// Setup a game container: set it's display mode and target
		// frame rate
		Log.debug("Starting engine version " + VERSION) ;
		
		try
		{
			PaddedGameContainer container = new PaddedGameContainer(this);
							
			container.setIcons(new String[] {"image/engine/SomeIcon16.png", "image/engine/SomeIcon32.png"});
			container.determineScreenSize();	
			createOrLoadPersistantState(container);												
			setRenderSettings(container);
			container.start();
		}
		catch (Throwable ex)
		{
			JOptionPane.showMessageDialog(null, "An error has occurred: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void setRenderSettings(GameContainer container) {
		container.setShowFPS(true);
		container.setVSync(true);
		container.setAlwaysRender(true);
		container.setTargetFrameRate(60);
	}
	
	public void createOrLoadPersistantState(GameContainer container) {
		ClientProgress clientProgress = null;
		ClientProfile clientProfile = null;

		File file = new File(".");

		for (String s : file.list())
		{
			if (s.endsWith(ClientProfile.PROFILE_EXTENSION))
			{
				clientProfile = ClientProfile.deserializeFromFile(s);
			}
			else if (s.endsWith(ClientProgress.PROGRESS_EXTENSION))
			{
				clientProgress =  ClientProgress.deserializeFromFile(s);
			}
		}

		// Check to see if a client profile has been loaded.
		if (clientProfile == null)
		{
			clientProfile = new ClientProfile("Default");
			clientProfile.serializeToFile();

			// If Dev mode is enabled, check to see if Dev Params
			// were specified, if so then apply them to the client profile.
			// If this is "Test" mode then don't apply the dev params as
			// it may screw up the heroes in the party
			/* THIS IS NO LONGER NEEDED AS WE ONLY LOAD DURING BATTLE LOAD FROM DEVEL
			if (TacticalGame.DEV_MODE_ENABLED && !TacticalGame.TEST_MODE_ENABLED)
			{
				DevParams devParams = DevParams.parseDevParams();
				if (devParams != null)
					clientProfile.setDevParams(devParams);
			}
			*/

			Log.debug("Profile was created");
		}
		
		if (clientProgress == null)
		{
			Log.debug("Create Progress");
			clientProgress = new ClientProgress("Test");
			clientProgress.serializeToFile();
		}
		
		try {
			persistentStateInfo =
				new PersistentStateInfo(clientProfile, clientProgress,
						this,
						new Camera(PaddedGameContainer.GAME_SCREEN_SIZE.width, PaddedGameContainer.GAME_SCREEN_SIZE.height), container);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(0);
		}
	}	

	/**
	 * Initialize all of the game states. This is called automatically
	 * after the game container is initialized
	 */
	@Override
	public void initStatesList(GameContainer gameContainer) throws SlickException
	{		
		loadingState = new LoadingState(STATE_GAME_LOADING);
		this.addState(new MenuState(persistentStateInfo));
		this.addState(ENGINE_CONFIGURATIOR.getAttackCinematicState());
		this.addState(new DevelMenuState(persistentStateInfo));
		this.addState(new DevelAnimationViewState());
		this.addState(loadingState);
		this.addState(new DevelBattleAnimViewState());
		addState(new BattleState(persistentStateInfo));
		addState(new TownState(persistentStateInfo));
		addState(new CinematicState(persistentStateInfo));
		addState(new ChapterState(persistentStateInfo));
		addState(new CreditsState());
		addState(new IntroState(persistentStateInfo));

		// this.addState(new TestState());

		/*
		loadingState.setLoadingInfo("/loader/Test", null, false, true,
				new FCResourceManager(),
					(LoadableGameState) this.getState(STATE_GAME_TEST),
						new FCLoadingRenderSystem(gameContainer));
		*/



		// TODO Creating a "New Game" will automatically create a new save info with the current map set to the first level
		// Subsequent loads will be from wherever the user left off
		/*
		loadingState.setLoadingInfo("/level/Level1",
				new FCResourceManager(),
				(LoadableGameState) this.getState(STATE_GAME_TOWN),
				new FCLoadingRenderSystem(gameContainer));
				*/


		/*************************************/
		/* Uncomment for multiplayer support */
		/*************************************/
		/*
		this.addState(new MultiplayerMenuState(persistentStateInfo));
		loadingState.setLoadingInfo("/menu/MainMenu", false, true,
				new ResourceManager(),
					(LoadableGameState) this.getState(STATE_GAME_MENU_MULTI),
						TacticalGame.ENGINE_CONFIGURATIOR.getLogoLoadScreenRenderer(gameContainer));
		*/
		/******************************/
		/* Comment during multiplayer */
		/******************************/
		
		// DEVELOPMENT MODE
		
		if (DEV_MODE_ENABLED)
			loadingState.setLoadingInfo("/menu/MainMenu", false, true,
				new ResourceManager(),
					(LoadableGameState) this.getState(STATE_GAME_MENU_DEVEL),
						new LoadingScreenRenderer(gameContainer));
						
		// RELEASE MODE
		else {
			/*
			loadingState.setLoadingInfo("/menu/MainMenu", false, true,
				new ResourceManager(),
					(LoadableGameState) this.getState(STATE_GAME_MENU),
						TacticalGame.ENGINE_CONFIGURATIOR.getLogoLoadScreenRenderer(gameContainer));
						*/
			persistentStateInfo.loadCinematic(this.getEngineConfigurator().getConfigurationValues().getIntroCinematicMap(), 0, 
					TacticalGame.ENGINE_CONFIGURATIOR.getLogoLoadScreenRenderer(gameContainer));
		}
		
		// TESTER ONLY MODE
		/*
		EngineConfigurationValues jcv = CommRPG.engineConfiguratior.getConfigurationValues();
		persistentStateInfo.loadCinematic(jcv.getStartingMapData(), 0);
		*/
		
						
		// addState(new PlannerState());
		this.enterState(STATE_GAME_LOADING);
	}
	
	public static int getTestMultiplier()
	{
		return 1000;
	}
	
	public void setTextParser(TextParser parser) {
		this.TEXT_PARSER = parser;
	}
	
	public static boolean testD100(int percent, String roll) {
		int random = RANDOM.nextInt(100);
		return random <= percent;
	}
}
