package tactical.game.ui;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Game;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.loading.LoadableGameState;

public class PaddedGameContainer extends AppGameContainer
{

	public static int GAME_SCREEN_SCALE = 1;
	public static int GAME_SCREEN_PADDING = 0;
	public static final Dimension GAME_SCREEN_SIZE = new Dimension(320, 240);
	
	private int displayPaddingX;
	private static int fullScreenWidth, fullScreenHeight;

	public PaddedGameContainer(Game game) throws SlickException
	{
		super(game);
	}

	public int getDisplayPaddingX() {
		return displayPaddingX;
	}

	public void setDisplayPaddingX(int displayPaddingX) {
		this.displayPaddingX = displayPaddingX;
	}

	/**
	 * Strategy for overloading game loop context handling
	 *
	 * @throws SlickException Indicates a game failure
	 */
	@Override
	protected void gameLoop() throws SlickException {
		int delta = getDelta();
		if (!Display.isVisible() && updateOnlyOnVisible) {
			try { Thread.sleep(100); } catch (Exception e) {}
		} else {
			try {
				updateAndRender(delta);
			} catch (Throwable e) {
				if (((TacticalGame) game).getCurrentState() instanceof LoadableGameState)
					((LoadableGameState) ((TacticalGame) game).getCurrentState()).exceptionInState();
				((TacticalGame) game).enterState(TacticalGame.STATE_GAME_MENU_DEVEL);
				JOptionPane.showMessageDialog(null, "An error occurred during execution " + e.getMessage());
				return;
			}
		}

		updateFPS();

		Display.update();

		if (Display.isCloseRequested()) {
			if (game.closeRequested()) {
				running = false;
			}
		}
	}

	public int getPaddedWidth() {
		return super.getWidth() - displayPaddingX * 2;
	}
	
	public int getTargetFrameRate() {
		return super.targetFPS;
	}
	
	public void determineScreenSize() throws SlickException, LWJGLException {
		// TODO We want to keep the same screen resolution ratio but then just expand the vertical black bars. Potentially put menus in the bars
		fullScreenWidth = 0;
		fullScreenHeight = Integer.MAX_VALUE;


		try {
			double ratio =  getScreenWidth() * 1.0 / getScreenHeight();
			Log.debug("Screen Ratio: " + ratio);
			DisplayMode[] modes = Display.getAvailableDisplayModes();

			for (DisplayMode dm : modes)
			{
				double sRatio = 1.0 * dm.getWidth() / dm.getHeight();
				if (sRatio == ratio && fullScreenHeight > dm.getHeight()
						&& dm.getHeight() % 240 == 0 && dm.getHeight() > 240)
				{
					fullScreenWidth = dm.getWidth();
					fullScreenHeight = dm.getHeight();
				}
			}

			Log.debug("Fullscreen dimensions: " + fullScreenWidth + " " + fullScreenHeight);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		if (fullScreenWidth == 0)
		{
			Log.debug("Unable to enter full screen");
		}
		
		setDisplayPaddingX(0);
		
		try
		{
			PaddedGameContainer.GAME_SCREEN_SCALE = 3;
			// container.setDisplayMode(GAME_SCREEN_SIZE.width * PaddedGameContainer.GAME_SCREEN_SCALE, 
				//	GAME_SCREEN_SIZE.height * PaddedGameContainer.GAME_SCREEN_SCALE, false);
			setDisplayMode(GAME_SCREEN_SIZE.width * PaddedGameContainer.GAME_SCREEN_SCALE, 
					GAME_SCREEN_SIZE.height * PaddedGameContainer.GAME_SCREEN_SCALE, false);
			PaddedGameContainer.GAME_SCREEN_PADDING = 0;
		}
		catch (SlickException se)
		{
			setDisplayMode(GAME_SCREEN_SIZE.width, GAME_SCREEN_SIZE.height, false);
		}					
	}	
	
	public void toggleFullScreen() throws SlickException
	{
		if (isFullscreen())
		{
			 setDisplayMode(320 * 3, 240 * 3, false);
			 setDisplayPaddingX(0);
			 setMouseGrabbed(false);
			 PaddedGameContainer.GAME_SCREEN_PADDING = 0;
			 PaddedGameContainer.GAME_SCREEN_SCALE = 3;
		}
		else
		{
			if (fullScreenWidth != 0)
			{
				PaddedGameContainer.GAME_SCREEN_SCALE = fullScreenHeight / 240;
				setDisplayPaddingX((fullScreenWidth - GAME_SCREEN_SIZE.width * PaddedGameContainer.GAME_SCREEN_SCALE) / 2);
				PaddedGameContainer.GAME_SCREEN_PADDING = getDisplayPaddingX();
				 setDisplayMode(fullScreenWidth, fullScreenHeight, true);
				 setMouseGrabbed(true);
			}
		}
	}
}
