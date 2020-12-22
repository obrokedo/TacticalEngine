package tactical.engine.state;

import java.util.Stack;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.particles.ParticleSystem;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.game.input.KeyMapping;
import tactical.game.manager.CinematicManager;
import tactical.game.manager.MenuManager;
import tactical.game.manager.PanelManager;
import tactical.game.manager.SoundManager;
import tactical.game.manager.SpriteManager;
import tactical.game.manager.TownMoveManager;
import tactical.game.menu.HeroStatMenu;
import tactical.game.menu.Menu;
import tactical.game.menu.PauseMenu;
import tactical.game.menu.devel.DebugMenu;
import tactical.game.menu.devel.HeroContextDebugMenu;
import tactical.game.menu.devel.HeroesContextDebugMenu;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;
import tactical.map.MapObject;
import tactical.renderer.MenuRenderer;
import tactical.renderer.PanelRendererManager;
import tactical.renderer.SpriteRenderer;
import tactical.renderer.TileMapRenderer;

/**
 * State that handles movement in town/overland, NPCs and
 * town menus
 *
 * @author Broked
 *
 */
public class TownState extends LoadableGameState
{
	private TileMapRenderer tileMapRenderer;
	private SpriteRenderer spriteRenderer;
	private PanelRendererManager panelRenderer;
	private MenuRenderer menuRenderer;
	private SpriteManager spriteManager;
	private PanelManager panelManager;
	private MenuManager menuManager;
	private TownMoveManager townMoveManager;
	private CinematicManager cinematicManager;
	private SoundManager soundManager;
	
	private Stack<NPCSprite> TEST_NPCS_STACK = null;

	private StateInfo stateInfo;
	public static ParticleSystem ps = null;
	
	// TODO THIS IS A DEBUG TOOL
	public static float updateSpeed = 1;

	public TownState(PersistentStateInfo psi) {
		this.stateInfo = new StateInfo(psi, false, false);
		this.tileMapRenderer = new TileMapRenderer();
		stateInfo.registerManager(tileMapRenderer);
		this.spriteRenderer = new SpriteRenderer();
		stateInfo.registerManager(spriteRenderer);
		this.panelRenderer = new PanelRendererManager();
		stateInfo.registerManager(panelRenderer);
		this.menuRenderer = new MenuRenderer();
		stateInfo.registerManager(menuRenderer);
		this.spriteManager = new SpriteManager();
		stateInfo.registerManager(spriteManager);
		this.panelManager = new PanelManager();
		stateInfo.registerManager(panelManager);
		this.menuManager = new MenuManager();
		stateInfo.registerManager(menuManager);
		this.townMoveManager = new TownMoveManager();
		stateInfo.registerManager(townMoveManager);
		this.cinematicManager = new CinematicManager(false);
		stateInfo.registerManager(cinematicManager);
		this.soundManager = new SoundManager();
		stateInfo.registerManager(soundManager);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException
	{

	}

	/**
	 * Initializes this state, this only gets called when coming
	 * from a loading state
	 */
	@Override
	public void initAfterLoad() {
		stateInfo.initState();
		
		if (TacticalGame.TEST_MODE_ENABLED) {
			TEST_NPCS_STACK = new Stack<>();
			for (Sprite s : stateInfo.getSprites()) {
				if (s.getSpriteType() == Sprite.TYPE_NPC) {
					TEST_NPCS_STACK.add((NPCSprite) s);
				}
			}
		}
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);
		// To allow the hero to continue moving between maps, initialize the input
		// with any movement keys that are already pressed
		stateInfo.getInput().setInitialMovementInput(container.getInput());
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {

		cleanupState();

		super.leave(container, game);
	}

	protected void cleanupState() {
		stateInfo.getResourceManager().reinitialize();
		stateInfo.setInitialized(false);
		stateInfo.getInput().clear();
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) 
	{
		if (stateInfo.isInitialized())
		{
			float xOffset = stateInfo.getCamera().getLocationX() % stateInfo.getCurrentMap().getTileRenderWidth();
			float yOffset = stateInfo.getCamera().getLocationY() % stateInfo.getCurrentMap().getTileRenderHeight();

			tileMapRenderer.render(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
			spriteRenderer.render(g);
			cinematicManager.render(g);
			tileMapRenderer.renderForeground(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
			panelRenderer.render(g);
			cinematicManager.renderPostEffects(g);
			
			if (ps != null)
				ps.render();
			
			menuRenderer.render(g);
		}
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException
	{
		stateInfo.processMessages();

		if (stateInfo.isInitialized() && !stateInfo.isWaiting())
		{
			menuManager.update(delta);
			cinematicManager.update(delta);
			if (!cinematicManager.isBlocking()) {
				if (!menuManager.isBlocking())
				{
					panelManager.update(delta);
					
					
					if (TacticalGame.TEST_MODE_ENABLED) {
						if (TEST_NPCS_STACK.size() > 0) {
							TEST_NPCS_STACK.remove(0).triggerButton1Event(stateInfo);
						}
					}
				}
				townMoveManager.update(delta);
			}
			stateInfo.getCurrentMap().update(delta);
			spriteManager.update(delta);
			soundManager.update(delta);
			
			if (ps != null)
				ps.update(delta);

			if (System.currentTimeMillis() > stateInfo.getInputDelay())
			{
				if (stateInfo.getInput().isKeyDown(KeyMapping.BUTTON_3) && !stateInfo.areMenusDisplayed())
				{
					if (!menuManager.isBlocking() && !cinematicManager.isBlocking())
					{
						stateInfo.sendMessage(MessageType.INVESTIGATE);
						stateInfo.setInputDelay(System.currentTimeMillis() + 200);
						
						checkSearchLocation();
					}
				}
				else if (stateInfo.getInput().isKeyDown(KeyMapping.BUTTON_1) && !stateInfo.areMenusDisplayed())
				{
					stateInfo.sendMessage(new Message(MessageType.SHOW_HEROES));
				}
				// Key for debugging menus
				else if (TacticalGame.DEV_MODE_ENABLED && container.getInput().isKeyDown(Input.KEY_Q))
				{
					Menu top = stateInfo.getTopMenu();
					if (top != null) {
						switch(top.getPanelType()) {
							case PANEL_HEROS_STATS:
								stateInfo.addSingleInstanceMenu(new HeroContextDebugMenu(((HeroStatMenu) top).getSelectedSprite()));
								stateInfo.setInputDelay(System.currentTimeMillis() + 200);
								break;
							case PANEL_HEROS_OVERVIEW:
								stateInfo.addSingleInstanceMenu(new HeroesContextDebugMenu());
								stateInfo.setInputDelay(System.currentTimeMillis() + 200);
								break;
							default:
								break;								
						}
					}
					// image = null;
					
					// container.getGraphics().copyArea(image, 0, 0);
					// image.flushPixelData();
					// 	stateInfo.sendMessage(new Message(MessageType.SHOW_HEROES));
					// stateInfo.sendMessage(new ShopMessage(1.2, .8, new int[] {1, 1, 2, 2, 0, 0, 1, 1, 2, 2, 0, 0}, "Noah"));
					
					/*
					ArrayList<CombatSprite> multiJoinSprites = new ArrayList<>();
					multiJoinSprites.add(stateInfo.getHeroById(0));
					multiJoinSprites.add(stateInfo.getHeroById(1));
					stateInfo.sendMessage(new SpriteContextMessage(
							MessageType.SHOW_PANEL_MULTI_JOIN_CHOOSE, multiJoinSprites));
							*/
					
					// stateInfo.sendMessage(MessageType.SHOW_PRIEST);
				}
			}

			stateInfo.getInput().update(delta, container.getInput());
		}
	}

	private void checkSearchLocation() {
		int checkX = stateInfo.getCurrentSprite().getTileX();
		int checkY = stateInfo.getCurrentSprite().getTileY();

		switch (stateInfo.getCurrentSprite().getFacing())
		{
			case UP:
				checkY--;
				break;
			case DOWN:
				checkY++;
				break;
			case LEFT:
				checkX--;
				break;
			case RIGHT:
				checkX++;
				break;
		}
		
		for (MapObject mo : stateInfo.getCurrentMap().getMapObjects())
		{
			if (mo.contains(checkX * stateInfo.getTileWidth() + 1, 
					checkY * stateInfo.getTileHeight() + 1))
			{
				stateInfo.getResourceManager().checkTriggerCondtions(
						mo.getName(), false, false, false, true, stateInfo);
			}
		}
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_TOWN;
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		this.stateInfo.setResourceManager(resourceManager);
	}

	public StateInfo getStateInfo() {
		return stateInfo;
	}
	
	public Image getStateImageScreenshot(boolean showHero) throws SlickException {
		PaddedGameContainer container = stateInfo.getPaddedGameContainer();
		Image image = new Image(container.getPaddedWidth(), container.getHeight());
		if (!showHero)
			stateInfo.getCurrentSprite().setVisible(false);
		render(container, null, container.getGraphics());
		container.getGraphics().copyArea(image, 0, 0);
		if (!showHero)
			stateInfo.getCurrentSprite().setVisible(true);
		container.getGraphics().resetTransform();
		return image;
	}

	@Override
	protected Menu getPauseMenu() {
		stateInfo.sendMessage(MessageType.PAUSE_MUSIC);
		if (TacticalGame.DEV_MODE_ENABLED)
			return new DebugMenu(stateInfo);
		return new PauseMenu(stateInfo);
	}

	@Override
	protected void pauseMenuClosed() {
		super.pauseMenuClosed();
		stateInfo.sendMessage(MessageType.RESUME_MUSIC);
	}

	@Override
	public void exceptionInState() {
		cleanupState();
	}
}
