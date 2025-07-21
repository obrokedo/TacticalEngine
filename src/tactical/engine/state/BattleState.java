package tactical.engine.state;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.BooleanMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.devel.SaveState;
import tactical.game.manager.CinematicManager;
import tactical.game.manager.InitiativeManager;
import tactical.game.manager.KeyboardManager;
import tactical.game.manager.MenuManager;
import tactical.game.manager.SoundManager;
import tactical.game.manager.SpriteManager;
import tactical.game.manager.TurnManager;
import tactical.game.menu.Menu;
import tactical.game.menu.PauseMenu;
import tactical.game.menu.devel.DebugMenu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;
import tactical.renderer.MenuRenderer;
import tactical.renderer.PanelRendererManager;
import tactical.renderer.SpriteRenderer;
import tactical.renderer.TileMapRenderer;
import tactical.utils.StringUtils;

/**
 * State that drives and renders battle movement and menus. This does not
 * handle attack cinematics
 *
 * @author Broked
 *
 */
public class BattleState extends LoadableGameState
{
	private TileMapRenderer tileMapRenderer;
	private SpriteRenderer spriteRenderer;
	private PanelRendererManager panelRenderer;
	private MenuRenderer menuRenderer;
	private SpriteManager spriteManager;
	private MenuManager menuManager;
	private KeyboardManager keyboardManager;
	private SoundManager soundManager;
	private CinematicManager cinematicManager;

	private InitiativeManager initManager;
	private TurnManager turnManager;

	private StateInfo stateInfo;
	
	private SaveState manualSaveState = null;

	/*
	private float musicVolume = 0;
	private String music = null;
	*/

	public BattleState(PersistentStateInfo psi)
	{
		this.stateInfo = new StateInfo(psi, true, false);
		this.tileMapRenderer = new TileMapRenderer();
		stateInfo.registerManager(tileMapRenderer);
		this.spriteRenderer = new SpriteRenderer();
		stateInfo.registerManager(spriteRenderer);
		this.panelRenderer = new PanelRendererManager();
		stateInfo.registerManager(panelRenderer);
		this.menuRenderer = new MenuRenderer();
		stateInfo.registerManager(menuRenderer);
		this.initManager = new InitiativeManager();
		stateInfo.registerManager(initManager);
		this.spriteManager = new SpriteManager();
		stateInfo.registerManager(spriteManager);
		this.menuManager = new MenuManager();
		stateInfo.registerManager(menuManager);
		this.keyboardManager = new KeyboardManager();
		stateInfo.registerManager(keyboardManager);
		this.turnManager = new TurnManager();
		stateInfo.registerManager(turnManager);
		this.soundManager = new SoundManager();
		stateInfo.registerManager(soundManager);
		this.cinematicManager = new CinematicManager(false);
		stateInfo.registerManager(cinematicManager);
	}

	/**
	 * Initializes this state, this only gets called when coming
	 * from a loading state
	 */
	@Override
	public void initAfterLoad() {
		stateInfo.initState();
		manualSaveState = null;
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);

		if (stateInfo.isShowAttackCinematic())
		{
			stateInfo.getInput().clear();
			container.getInput().addKeyListener(stateInfo.getInput());
			this.stateInfo.setInputDelay(System.currentTimeMillis() + 200);
			stateInfo.setShowAttackCinematic(false);
			stateInfo.sendMessage(new BooleanMessage(MessageType.RESUME_MUSIC, true));
			stateInfo.sendMessage(MessageType.RETURN_FROM_ATTACK_CIN);
		}
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		if (!stateInfo.isShowAttackCinematic())
		{
			cleanupState();
		}

		super.leave(container, game);
	}

	protected void cleanupState() {
		stateInfo.getResourceManager().reinitialize();
		stateInfo.setInitialized(false);
		stateInfo.getInput().clear();
		stateInfo.clearSaveStates();
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		if (stateInfo.isInitialized())
		{
			float xOffset = stateInfo.getCamera().getLocationX() % stateInfo.getCurrentMap().getTileRenderWidth();
			float yOffset = stateInfo.getCamera().getLocationY() % stateInfo.getCurrentMap().getTileRenderHeight();

			tileMapRenderer.render(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
			turnManager.render(g);
			spriteRenderer.render(g);
			if (stateInfo.getCurrentSprite() != null && stateInfo.getCurrentSprite().isVisible())
				stateInfo.getCurrentSprite().render(stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer(), stateInfo.getTileHeight());
			cinematicManager.render(g);
			tileMapRenderer.renderForeground(xOffset, yOffset, stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer());
			
			// Render a "shadow" sprite on top of foreground
			if (stateInfo.getCurrentSprite() != null && stateInfo.getCurrentSprite().isVisible()) {
				int currAlpha = stateInfo.getCurrentSprite().getAlpha();
				stateInfo.getCurrentSprite().setAlpha(Math.min(100, stateInfo.getCurrentSprite().getAlpha()));
				stateInfo.getCurrentSprite().render(stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer(), stateInfo.getTileHeight());
				stateInfo.getCurrentSprite().setAlpha(currAlpha);
			}
			
			turnManager.renderCursor(g);
			cinematicManager.renderPostEffects(g);
			panelRenderer.render(g);
			menuRenderer.render(g);
			
			if (manualSaveState != null) {
				StringUtils.drawString("Manual Save State", 0, -10, g);
			}
		}
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException
	{
		try {
			if (TacticalGame.TEST_MODE_ENABLED)
				delta *= TacticalGame.getTestMultiplier();
	
			if (stateInfo.getTopMenu() == null || !(stateInfo.getTopMenu() instanceof DebugMenu)) {
				stateInfo.processMessages();
			}
			
			if (stateInfo.isInitialized())
			{
				
				menuManager.update(delta);
				cinematicManager.update(delta);
				
				if (!menuManager.isBlocking() && !cinematicManager.isBlocking())
				{
					//hudMenuManager.update();
					keyboardManager.update();
					turnManager.update(game, delta);
				}
				
				if (TacticalGame.DEV_MODE_ENABLED) {					
					if (container.getInput().isKeyDown(Input.KEY_F5) && inputTimer <= 0) {
						manualSaveState = stateInfo.saveBattleForDebug();
						stateInfo.dumpSaveStatesToFile();
						inputTimer = 200;
					}
					else if (container.getInput().isKeyDown(Input.KEY_F6) && manualSaveState != null && inputTimer <= 0) {						
						stateInfo.loadBattleFromState(manualSaveState);
						inputTimer = 200;
					}
				}
	
				stateInfo.getCurrentMap().update(delta);
				spriteManager.update(delta);
				soundManager.update(delta);
				stateInfo.getInput().update(delta, container.getInput());
			}
		} catch (Exception e) {
			if (TacticalGame.DEV_MODE_ENABLED) {
				e.printStackTrace();
				try {
					stateInfo.dumpSaveStatesToFile();
				// Nothing bad ever happens
				} catch (Exception e2) {e2.printStackTrace();}
				
				JFrame jf = new JFrame();
				jf.setAlwaysOnTop(true);
				JButton back1 = new JButton("Undo 1 turn");
				back1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						stateInfo.loadBattleFromPreviousState();					
					}				
				});
				JButton back5 = new JButton("Undo 5 turns");
				back5.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						stateInfo.loadBattleFromPreviousState(5);					
					}				
				});
				JButton back10 = new JButton("Undo 10 turns");
				back10.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						stateInfo.loadBattleFromPreviousState(10);					
					}				
				});
				Log.error(e);
				
				// RELOAD GRAPHIC ASSETS
				JButton returnToMenu = new JButton("Return to Menu");
				returnToMenu.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						((TacticalGame) game).enterState(TacticalGame.STATE_GAME_MENU_DEVEL);		
					}				
				});
				
				JPanel bPanel = new JPanel();
				bPanel.add(back1);			
				bPanel.add(back5);
				bPanel.add(back10);
				bPanel.add(returnToMenu);			
				JOptionPane.showMessageDialog(jf, bPanel);
			}
		}
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		this.stateInfo.setResourceManager(resourceManager);
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_BATTLE;
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
		stateInfo.getCamera().centerOnSprite(stateInfo.getCurrentSprite(), stateInfo.getCurrentMap());
		stateInfo.sendMessage(MessageType.RESUME_MUSIC);
	}

	@Override
	public void exceptionInState() {
		cleanupState();
	}	
}
