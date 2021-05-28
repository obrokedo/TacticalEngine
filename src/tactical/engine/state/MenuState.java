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
import tactical.engine.message.LoadMapMessage;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.manager.SoundManager;
import tactical.game.menu.Menu;
import tactical.game.trigger.Trigger;
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
	protected Music music;
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
		fcrm = persistentStateInfo.getResourceManager();
		menuMove = fcrm.getSoundByName("menumove");
		menuSelect = fcrm.getSoundByName("menuselect");
		initialized = true;		
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
		start(loadType, mapData, entrance, 0);
	}

	public void start(LoadTypeEnum loadType, String mapData, String entrance, int resourceId)
	{		
		Trigger trigger = new Trigger();
		switch (loadType)
		{
			case CINEMATIC:
				trigger.addTriggerable(trigger.new TriggerLoadCinematic(mapData, 0));				
				break;
			case TOWN:
				trigger.addTriggerable(trigger.new TriggerLoadMap(mapData, entrance, null));
				break;
			case BATTLE:
				trigger.addTriggerable(trigger.new TriggerStartBattle(mapData, entrance, 0));
				break;
		}
		
		persistentStateInfo.loadChapter(TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getFirstChapterHeaderText(), 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getFirstChapterDescriptionText(), trigger, true);
			
		if (container.isFullscreen())
			container.setMouseGrabbed(true);
	}
	
	public void load(LoadTypeEnum loadType, String mapData, String entrance, int resourceId) {
		persistentStateInfo.isFirstLoad = true;
		switch (loadType)
		{
			case CINEMATIC:
				persistentStateInfo.loadCinematic(mapData, resourceId);
				break;
			case TOWN:
				persistentStateInfo.loadMap(mapData, entrance);
				break;
			case BATTLE:
				persistentStateInfo.loadBattle(mapData, entrance, resourceId);
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

	protected void startNewGame(EngineConfigurationValues jcv) {
		// Clobber existing save data...
		persistentStateInfo.getClientProfile().initializeStartingValues();
		persistentStateInfo.getClientProgress().initializeValues();
		persistentStateInfo.getClientProfile().initializeStartingHeroes();
		// persistentStateInfo.getClientProfile().serializeToFile();
		// persistentStateInfo.getClientProgress().serializeToFile();
		start(LoadTypeEnum.valueOf(jcv.getStartingState()), 
				jcv.getStartingMapData(), jcv.getStartingLocation());
	}

	protected void handleInput(PaddedGameContainer container) {
		if (container.getInput().isKeyDown(Input.KEY_ENTER) || 
				container.getInput().isKeyDown(KeyMapping.BUTTON_1) || 
				container.getInput().isKeyDown(KeyMapping.BUTTON_3))
		{
			EngineConfigurationValues jcv = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
			menuSelect.play(1f, SoundManager.GLOBAL_VOLUME);
			if (stateIndex == 1) {
				// music.fade(500, 0f, true);
				if (menuIndex == 0) {
					startNewGame(jcv);
				}
				else if (menuIndex == 1)
				{
					LoadTypeEnum loadType = LoadTypeEnum.TOWN;
					// This is the first time through the engine
					if (persistentStateInfo.getClientProfile().getHeroes().size() > 0) {
						LoadMapMessage lmm = null;
						if ((lmm = persistentStateInfo.getClientProgress().getAndClearChapterSaveMessage()) != null) 
						{
							switch (lmm.getMessageType()) {
								case LOAD_CINEMATIC:
									persistentStateInfo.loadCinematic(lmm);
									break;
								case LOAD_MAP:
									persistentStateInfo.loadMap(lmm);
									break;
								case START_BATTLE:
									persistentStateInfo.loadBattle(lmm);
									break;
							}
							return;
							
						}
						else if (persistentStateInfo.getClientProgress().isBattle())
							loadType = LoadTypeEnum.BATTLE;
						load(loadType, persistentStateInfo.getClientProgress().getLastSaveLocation().getLastSaveMapData(), 
								null, 0);
					} else {
						startNewGame(jcv);
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
				menuMove.play(1f, SoundManager.GLOBAL_VOLUME);
			}
			else if (menuIndex != 2 && container.getInput().isKeyDown(Input.KEY_DOWN))
			{
				menuIndex = 2;
				updateDelta = 200;
				menuMove.play(1f, SoundManager.GLOBAL_VOLUME);
			}
			else if (menuIndex != 1 && container.getInput().isKeyDown(Input.KEY_RIGHT))
			{
				menuIndex = 1;
				updateDelta = 200;
				menuMove.play(1f, SoundManager.GLOBAL_VOLUME);
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
