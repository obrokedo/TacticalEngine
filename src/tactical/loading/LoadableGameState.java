package tactical.loading;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.engine.state.devel.DevelMenuState;
import tactical.game.menu.Menu;
import tactical.game.menu.UIDebugMenu;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public abstract class LoadableGameState extends BasicGameState
{
	protected boolean loading = false;
	
	protected boolean paused = false;
	
	protected UIDebugMenu uiDebugMenu = null;
	
	protected Menu defaultPauseMenu;
	protected Menu pauseMenu = null;
	
	protected float updateSpeed = 1.0f;
	protected GameContainer container;

	public abstract void stateLoaded(ResourceManager resourceManager);

	public abstract void initAfterLoad();
	
	public abstract void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException;
	
	public abstract void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g);
	
	protected int inputTimer = 0;
	
	
	
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		 uiDebugMenu = new UIDebugMenu(container);
		 this.container = container;
	}

	public boolean isPaused(GameContainer gc) {
		if (gc.getInput().isKeyDown(Input.KEY_ENTER))
		{			
			if (paused) {
				pauseMenu.dispose();
				pauseMenuClosed();
			}
			else {
				pauseMenu = getPauseMenu();
				if (pauseMenu == null)
					return false;
			}
			paused = !paused;
			inputTimer = 500;
		}
		return paused;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.clearClip();
		g.setColor(Color.black);
		g.fillRect(0, 0, container.getWidth(), container.getHeight());
		g.translate(((PaddedGameContainer) container).getDisplayPaddingX(), 0);
		g.scale(PaddedGameContainer.GAME_SCREEN_SCALE, PaddedGameContainer.GAME_SCREEN_SCALE);
		g.setClip(((PaddedGameContainer) container).getDisplayPaddingX(), 0, 
				((PaddedGameContainer) container).getPaddedWidth(), container.getHeight());
		doRender((PaddedGameContainer) container, game, g);
		
		if (updateSpeed != 1)
		{
			g.setColor(Color.red);
			StringUtils.drawString("Update speed: " + updateSpeed, 15, 15, g);
		}
		
		if (paused) {
			pauseMenu.render((PaddedGameContainer) container, g);
			// uiDebugMenu.render(g);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		if (inputTimer > 0)
			inputTimer -= delta;
		
		if (inputTimer <= 0)
			isPaused(container);
		
		if (!paused) {
			if (inputTimer <= 0) {				
				if (TacticalGame.DEV_MODE_ENABLED && container.getInput().isKeyDown(Input.KEY_F11))
				{
					updateSpeed /= 2;
					inputTimer = 200;
				}
				else if (TacticalGame.DEV_MODE_ENABLED && container.getInput().isKeyDown(Input.KEY_F12))
				{
					updateSpeed *= 2;
					inputTimer = 200;
				}
				else if (container.getInput().isKeyDown(Input.KEY_F7))
				{
					((PaddedGameContainer) container).toggleFullScreen();
				}
				else if (TacticalGame.DEV_MODE_ENABLED && container.getInput().isKeyPressed(Input.KEY_ESCAPE))
				{
					TacticalGame.MUTE_MUSIC = true;
					game.enterState(TacticalGame.STATE_GAME_MENU_DEVEL);
				}
			}
			
			doUpdate((PaddedGameContainer) container, game, (int) (delta * updateSpeed));
		} else {
			pauseMenu.update(delta, null);
			// uiDebugMenu.update(container, game, delta);
		}
		
		SoundStore.get().poll(delta);
	}
	
	protected abstract Menu getPauseMenu();
	
	public abstract void exceptionInState();
	
	protected void pauseMenuClosed() {
		
	}
}
