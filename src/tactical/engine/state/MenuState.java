package tactical.engine.state;

import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.state.transition.Transition;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingState;
import tactical.loading.ResourceManager;

/**
 * State that handles the main menu
 *
 * @author Broked
 *
 */
public class MenuState extends LoadableGameState
{
	public enum LoadTypeEnum
	{
		TOWN,
		CINEMATIC,
		BATTLE
	}

	protected StateBasedGame game;
	protected String version = TacticalGame.VERSION;
	protected Font font;
	protected boolean initialized = false;
	protected UserInput input;
	protected int stateIndex = 0;
	protected int menuIndex = 0;
	protected int updateDelta = 0;
	protected PersistentStateInfo persistentStateInfo;
	private Sound menuMove;
	private Sound menuSelect;
	private ResourceManager fcrm;
	private Music music;
	private Transition transition;
	
	public MenuState(PersistentStateInfo persistentStateInfo) {
		super();
		this.persistentStateInfo = persistentStateInfo;
	}



	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.init(container, game);
		this.game = game;
		input = new UserInput();
	}
	
	

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		super.enter(container, game);
		container.getInput().removeAllKeyListeners();
		container.getInput().addKeyListener(this);
	}



	/**
	 * Initializes this state, this only gets called when coming
	 * from a loading state
	 */
	@Override
	public void initAfterLoad() {
		
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g)
	{
		//TODO Break this out into it's own menu renderer
		if (initialized)
		{
			if (stateIndex == 0) {
				g.drawImage(fcrm.getImage("title2"), 32, 15);
				g.drawImage(fcrm.getImage("LowBanner1"), 26, 163);
				
				g.drawImage(fcrm.getImage("Selector"), 93, 182);
				g.drawImage(fcrm.getImage("PressStart"), 111, 186);
			} else {
				g.drawImage(fcrm.getImage("Groupshot"), 26, 17);
				g.drawImage(fcrm.getImage("LowBanner2"), 26, 168);
				
				if (menuIndex == 0)
					g.drawImage(fcrm.getImage("Selector"), 29, 172);
				g.drawImage(fcrm.getImage("NewGame"), 47, 176);
				
				if (menuIndex == 1)
					g.drawImage(fcrm.getImage("Selector"), 157, 172);
				g.drawImage(fcrm.getImage("Continue"), 175, 176);
				
				if (menuIndex == 2)
					g.drawImage(fcrm.getImage("Selector"), 93, 196);
				g.drawImage(fcrm.getImage("Quit"), 111, 200);
			}
			g.drawImage(fcrm.getImage("Frame"), 0, 0);		
			
			if (transition != null)
				try {
					transition.postRender(game, container, g);
				} catch (SlickException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void startCinematic( String mapData, int cinematicId)
	{
		persistentStateInfo.loadCinematic(mapData, cinematicId);
		
		if (container.isFullscreen())
			container.setMouseGrabbed(true);

		game.enterState(TacticalGame.STATE_GAME_LOADING);
	}

	public void start(LoadTypeEnum loadType, String mapData, String entrance)
	{			
		persistentStateInfo.isFirstLoad = true;
		switch (loadType)
		{
			case CINEMATIC:
				persistentStateInfo.loadCinematic(mapData, 0);
				break;
			case TOWN:
				persistentStateInfo.loadMap(mapData, entrance);
				break;
			case BATTLE:
				persistentStateInfo.loadBattle(mapData, entrance, 0);
			break;
		}
		
		LoadingState loadingState = ((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING));
		loadingState.setLoadingRenderer(TacticalGame.ENGINE_CONFIGURATIOR.getFirstLoadScreenRenderer(container, music));

		if (container.isFullscreen())
			container.setMouseGrabbed(true);

		game.enterState(TacticalGame.STATE_GAME_LOADING);
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if (initialized)
		{
			// This is a bit confusing, we use a transition between the "PRESS START" text 
			// and the rest of the options. This transition will be set to a value once the
			// user has pressed start and then this code will catch the end of that transition
			// and start the music and change the game state index
			if (transition != null) {
				transition.update(game, container, delta);
				if (transition.isComplete()) {
					if (transition instanceof FadeOutTransition) {
						transition = new FadeInTransition();
						//TODO Break this out into it's own menu renderer
						music = fcrm.getMusicByName("lovtheme");
						music.loop();
						menuIndex = 0;
						stateIndex = 1;			
					}
					else
						transition = null;
				}							
			}
			
			if (updateDelta != 0)
			{
				updateDelta = Math.max(0, updateDelta - delta);
				return;
			}
			
			if (TacticalGame.DEV_MODE_ENABLED && container.getInput().isKeyDown(Input.KEY_F1))
			{
				((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo("/menu/MainMenu", false, true,
						new ResourceManager(),
							(LoadableGameState) game.getState(TacticalGame.STATE_GAME_MENU_DEVEL),
								TacticalGame.ENGINE_CONFIGURATIOR.getLoadScreenRenderer(container));

				game.enterState(TacticalGame.STATE_GAME_LOADING);
			}

			handleInput(container);
		}
	}



	protected void handleInput(PaddedGameContainer container) {
		if (container.getInput().isKeyDown(Input.KEY_ENTER) || 
				container.getInput().isKeyDown(KeyMapping.BUTTON_1) || 
				container.getInput().isKeyDown(KeyMapping.BUTTON_3))
		{
			EngineConfigurationValues jcv = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
			menuSelect.play();
			if (stateIndex == 1) {
				// music.fade(500, 0f, true);
				if (menuIndex == 0) {
					// Clobber existing save data...
					persistentStateInfo.getClientProfile().initializeValues();
					persistentStateInfo.getClientProgress().initializeValues();
					// persistentStateInfo.getClientProfile().serializeToFile();
					// persistentStateInfo.getClientProgress().serializeToFile();
					start(LoadTypeEnum.valueOf(jcv.getStartingState()), 
							jcv.getStartingMapData(), jcv.getStartingLocation());
				}
				else if (menuIndex == 1)
				{
					LoadTypeEnum loadType = LoadTypeEnum.TOWN;
					if (persistentStateInfo.getClientProfile().getHeroes().size() > 0) {
						if (persistentStateInfo.getClientProgress().isBattle())
							loadType = LoadTypeEnum.BATTLE;
						start(loadType, persistentStateInfo.getClientProgress().getMapData(), null);
					} else {
						// Clobber existing save data...
						persistentStateInfo.getClientProfile().initializeValues();
						persistentStateInfo.getClientProgress().initializeValues();
						// persistentStateInfo.getClientProfile().serializeToFile();
						// persistentStateInfo.getClientProgress().serializeToFile();
						start(LoadTypeEnum.valueOf(jcv.getStartingState()), 
								jcv.getStartingMapData(), jcv.getStartingLocation());
					}
				}
				else if (menuIndex == 2)
				{
					System.exit(0);
				}
			}
			else if (stateIndex == 0) {
				transition = new FadeOutTransition();
				menuSelect.play();
				updateDelta = 1500;
				
			}
		}
		
		if (stateIndex == 1) { 
			if (menuIndex != 0 && (container.getInput().isKeyDown(Input.KEY_UP) || 
					container.getInput().isKeyDown(Input.KEY_LEFT)))
			{
				menuIndex = 0;
				updateDelta = 200;
				menuMove.play();
			}
			else if (menuIndex != 2 && container.getInput().isKeyDown(Input.KEY_DOWN))
			{
				menuIndex = 2;
				updateDelta = 200;
				menuMove.play();
			}
			else if (menuIndex != 1 && container.getInput().isKeyDown(Input.KEY_RIGHT))
			{
				menuIndex = 1;
				updateDelta = 200;
				menuMove.play();
			}
		}
	}
	
	

	// Override this so that the state doesn't try to pause
	@Override
	public boolean isPaused(GameContainer container) {
		return false;
	}



	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		fcrm = resourceManager;
		menuMove = fcrm.getSoundByName("menumove");
		menuSelect = fcrm.getSoundByName("menuselect");
		initialized = true;		
	}
	
	

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_MENU;
	}



	@Override
	protected Menu getPauseMenu() {
		
		return null;
	}



	@Override
	public void exceptionInState() {
		
		
	}

	
}
