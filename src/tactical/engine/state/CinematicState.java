package tactical.engine.state;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.engine.message.IntMessage;
import tactical.engine.message.MessageType;
import tactical.game.manager.CinematicManager;
import tactical.game.manager.MenuManager;
import tactical.game.manager.SoundManager;
import tactical.game.menu.Menu;
import tactical.game.menu.PauseMenu;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;
import tactical.renderer.MenuRenderer;
import tactical.renderer.TileMapRenderer;

/**
 * Dedicated state that renders Cinematics
 *
 * @author Broked
 *
 */
public class CinematicState extends LoadableGameState
{
	private TileMapRenderer tileMapRenderer;
	private SoundManager soundManager;
	private MenuManager menuManager;
	private MenuRenderer menuRenderer;
	private CinematicManager cinematicManager;
	// private Cinematic cinematic;

	// TODO THIS IS A DEBUG TOOL
	public static float cinematicSpeed = 1;

	private StateInfo stateInfo;

	public CinematicState(PersistentStateInfo psi) {
		this.stateInfo = new StateInfo(psi, false, true);
		this.tileMapRenderer = new TileMapRenderer();
		stateInfo.registerManager(tileMapRenderer);
		this.soundManager = new SoundManager();
		stateInfo.registerManager(soundManager);
		this.menuManager = new MenuManager();
		stateInfo.registerManager(menuManager);
		this.menuRenderer = new MenuRenderer();
		stateInfo.registerManager(menuRenderer);
		this.cinematicManager = new CinematicManager(true);
		stateInfo.registerManager(cinematicManager);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {

	}

	/**
	 * Initializes this state, this only gets called when coming
	 * from a loading state
	 */
	@Override
	public void initAfterLoad() {
		stateInfo.initState();
		for (CombatSprite cs : stateInfo.getAllHeroes())
			cs.initializeSprite(stateInfo.getResourceManager());
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);

		// Get the first cinematic
		stateInfo.sendMessage(new IntMessage(MessageType.SHOW_CINEMATIC, stateInfo.getPersistentStateInfo().getCinematicID()));
		// cinematic = stateInfo.getResourceManager().getCinematicById(stateInfo.getPsi().getCinematicID());
		// cinematic.initialize(stateInfo);
		stateInfo.setInitialized(true);
		stateInfo.getInput().clear();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.leave(container, game);
		stateInfo.setInitialized(false);
		stateInfo.getResourceManager().reinitialize();
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g)
	{
		if (stateInfo.isInitialized())
		{
			// stateInfo.getCamera().realSetLocation();
			
			if (!cinematicManager.isBlocking()) {
				g.setColor(Color.black);
				g.fillRect(0, 0, container.getWidth(), container.getHeight());
			} else {

				float xOffset = stateInfo.getCamera().getLocationX() % stateInfo.getCurrentMap().getTileRenderWidth();
				float yOffset = stateInfo.getCamera().getLocationY() % stateInfo.getCurrentMap().getTileRenderHeight();
	
				tileMapRenderer.render(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
				cinematicManager.render(g);
				// cinematic.render(g, stateInfo.getCamera(), stateInfo.getGc(), stateInfo);
				tileMapRenderer.renderForeground(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
				cinematicManager.renderPostEffects(g);
				// cinematic.renderPostEffects(g, stateInfo.getCamera(), stateInfo.getGc(), stateInfo);
				menuRenderer.render(g);
			}
		}

	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		// If this is test mode then we want to speed
		// up the game
		if (TacticalGame.TEST_MODE_ENABLED)
			delta *= TacticalGame.getTestMultiplier();

		if (stateInfo.isInitialized())
		{
			stateInfo.processMessages();
			menuManager.update(delta);
			// cinematic.update((int) (delta * cinematicSpeed), stateInfo.getCamera(), stateInfo.getInput(), stateInfo.getResourceManager().getMap(), stateInfo);
			cinematicManager.update(delta);
			soundManager.update(delta);
			stateInfo.getCurrentMap().update(delta);
		}
		stateInfo.getInput().update(delta, container.getInput());
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		stateInfo.setResourceManager(resourceManager);
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_CINEMATIC;
	}
	
	@Override
	protected Menu getPauseMenu() {
		stateInfo.sendMessage(MessageType.PAUSE_MUSIC);
		return new PauseMenu(stateInfo);
	}

	@Override
	protected void pauseMenuClosed() {
		super.pauseMenuClosed();
		stateInfo.sendMessage(MessageType.RESUME_MUSIC);
	}
}
