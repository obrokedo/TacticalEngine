package tactical.engine.state;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.state.transition.Transition;

import lombok.Getter;
import lombok.Setter;
import tactical.engine.TacticalGame;
import tactical.engine.message.LoadMapMessage;
import tactical.engine.transition.MoveMapTransition;
import tactical.game.Camera;
import tactical.game.constants.Direction;
import tactical.game.persist.ClientProfile;
import tactical.game.persist.ClientProgress;
import tactical.game.sprite.CombatSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingScreenRenderer;
import tactical.loading.LoadingState;
import tactical.loading.ResourceManager;

/**
 * Contains information that should be shared across all game states
 *
 * @author Broked
 *
 */
public class PersistentStateInfo
{
	private Camera camera;
	private StateBasedGame game;
	private PaddedGameContainer gc;
	
	@Setter
	private ClientProfile clientProfile;
	
	@Setter
	private ClientProgress clientProgress;
	private ResourceManager resourceManager;
	private String entranceLocation = null;
	private int cinematicID = 0;
	private int clientId;
	@Getter @Setter
	private StateInfo currentStateInfo;
	public transient boolean isFirstLoad = true;

	public PersistentStateInfo(ClientProfile clientProfile, ClientProgress clientProgress, 
			StateBasedGame game, Camera camera, GameContainer gc)
	{
		this.game = game;
		this.camera = camera;
		this.gc = (PaddedGameContainer) gc;
		this.clientProfile = clientProfile;
		this.clientProgress = clientProgress;
	}

	/********************/
	/* Map Management	*/
	/********************/
	public void loadMap(String mapData, String entrance)
	{	
		loadMap(mapData, entrance, null);
	}
	
	public void loadMap(LoadMapMessage lmm) {		
		loadMap(lmm.getMapData(), lmm.getLocation(), lmm.getTransDir());
	}
	
	private void loadMap(String mapData, String entrance, Direction transitionDir)
	{	
		this.entranceLocation = entrance;

		cleanupStateAndLoadNext(mapData, TacticalGame.STATE_GAME_TOWN, transitionDir, null);
	}

	private void cleanupStateAndLoadNext(String mapData, int nextState, Direction transitionDir, LoadingScreenRenderer lsr) {
		if (lsr == null)
			lsr = new LoadingScreenRenderer(gc);
		gc.getInput().removeAllKeyListeners();

		getClientProgress().setMapData(mapData, false);

		if (transitionDir != null) {
			TownState townState = (TownState) getGame().getCurrentState();
			try {
				Image image = townState.getStateImageScreenshot(true);
				setLoadingInfo(mapData,
					(LoadableGameState) getGame().getState(nextState), getResourceManager(),
					lsr,
					image,
					new MoveMapTransition(townState, transitionDir));
			} catch (SlickException e) {
				e.printStackTrace();
				setLoadingInfo(mapData,
						(LoadableGameState) getGame().getState(nextState), getResourceManager(), lsr, null, null);
			}
		} else {
			setLoadingInfo(mapData, (LoadableGameState) getGame().getState(nextState), 
					getResourceManager(), lsr, null, null);
		}
		
		// Do not fade out when coming from a cinematic or if the map is going to 'slide' out
		if (getGame().getCurrentStateID() == TacticalGame.STATE_GAME_CINEMATIC ||
				transitionDir != null)
			getGame().enterState(TacticalGame.STATE_GAME_LOADING);
		else
			getGame().enterState(TacticalGame.STATE_GAME_LOADING, 
					// Do not fade out when coming from a cinematic
					new FadeOutTransition(Color.black, 250), new EmptyTransition());
		/*
		getGame().enterState(CommRPG.STATE_GAME_LOADING, 
				// Do not fade out when coming from a cinematic
				(getGame().getCurrentStateID() != CommRPG.STATE_GAME_CINEMATIC) ? new FadeOutTransition(Color.black, 250) :
					new EmptyTransition(), new EmptyTransition());
					*/
	}
	
	/**
	 * Sets the loading state to use existing resources that are already contained in the resource manager
	 * and to just load the specified text and map. It then transitions into the specified next state.
	 *
	 * @param text The text file to load
	 * @param nextState The next state that should be entered once the loading is done
	 * @param fcResourceManager Existing resource manager that contains all resources already loaded
	 * @param intermediateImage An image to show for the loading screen background
	 * @param transition the Transition that should be used to load in to the next state. A value of 
	 * 			null will use the default transition
	 */
	private void setLoadingInfo(String text, LoadableGameState nextState,
			ResourceManager fcResourceManager, LoadingScreenRenderer lsr, Image intermediateImage, Transition transition)
	{
		// If it's not the first load then we don't want to reload the resources
		((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo(text, true, isFirstLoad,
			(fcResourceManager == null ? new ResourceManager() : fcResourceManager),
				nextState,
					lsr, intermediateImage, transition);
		isFirstLoad = false;
	}

	
	public void loadMapFromSave()
	{
		loadMap(clientProgress.getMapData(), null);
	}

	public void loadBattle(LoadMapMessage lmm) {		
		loadBattle(lmm.getMapData(), lmm.getLocation(), lmm.getBattleBG());
	}
	
	public void loadBattle(String mapData, String entrance, int battleBGIndex)
	{
		this.entranceLocation = entrance;

		cleanupStateAndLoadNext(mapData, TacticalGame.STATE_GAME_BATTLE, null, null);
	}

	public void loadCinematic(LoadMapMessage lmm) {		
		loadCinematic(lmm.getMapData(), lmm.getCinematicID());
	}
	
	public void loadCinematic(String mapData, int cinematicID)
	{
		this.cinematicID = cinematicID;		
		
		cleanupStateAndLoadNext(mapData, TacticalGame.STATE_GAME_CINEMATIC, null, null);
	}
	
	public void loadCinematic(String mapData, int cinematicID, LoadingScreenRenderer lsr)
	{
		this.cinematicID = cinematicID;		
		
		cleanupStateAndLoadNext(mapData, TacticalGame.STATE_GAME_CINEMATIC, null, lsr);
	}
	
	public void loadChapter(String header, String description, Trigger exitTrigger, boolean isFirst) {
		((ChapterState) game.getState(TacticalGame.STATE_GAME_CHAPTER)).setChapterInfo(header, description, exitTrigger, isFirst);
		getGame().enterState(TacticalGame.STATE_GAME_CHAPTER, 
				// Do not fade out when coming from a cinematic
				new FadeOutTransition(Color.black, 250), new EmptyTransition());
	}

	public Camera getCamera() {
		return camera;
	}

	public PaddedGameContainer getGc() {
		return gc;
	}

	public void setQuestStatus(String id, boolean completed)
	{
		this.clientProgress.setQuestStatus(id, completed);
	}

	public boolean isQuestComplete(String questId)
	{
		return this.clientProgress.isQuestComplete(questId);
	}

	public StateBasedGame getGame() {
		return game;
	}

	public ClientProfile getClientProfile() {
		return clientProfile;
	}

	public ClientProgress getClientProgress() {
		return clientProgress;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public String getEntranceLocation() {
		return entranceLocation;
	}

	public int getCinematicID() {
		return cinematicID;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		for (CombatSprite cs : clientProfile.getHeroes())
			cs.setClientId(clientId);
		this.clientId = clientId;
	}
}
