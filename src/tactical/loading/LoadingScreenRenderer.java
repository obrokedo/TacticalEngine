package tactical.loading;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class LoadingScreenRenderer
{
	protected Graphics graphics;
	protected GameContainer gc;

	public LoadingScreenRenderer(GameContainer container) {
		graphics = container.getGraphics();
		this.gc = container;
	}

	public void render(LoadingStatus loading) {
		graphics.setColor(Color.white);
		// graphics.drawString(CommRPG.GAME_TITLE, 15, gc.getHeight() - 30);

		/*
		if (loading.maxIndex > 0)
		{
			graphics.setColor(new Color(1 - 1 * (1.0f * loading.currentIndex / loading.maxIndex), (1.0f * loading.currentIndex / loading.maxIndex), 0));
			System.out.println(255 - 255 * (1.0f * loading.currentIndex / loading.maxIndex) + " " + 255 * (1.0f * loading.currentIndex / loading.maxIndex));
			graphics.fillRect(gc.getWidth() - 190, gc.getHeight() - 52, (float) (160 * (1.0 * loading.currentIndex / loading.maxIndex)), 20);
		}
		graphics.setColor(Color.white);
		graphics.drawRect(gc.getWidth() - 190, gc.getHeight() - 50, 160, 15);
		*/

		// graphics.drawString("LOADING: " + loading.currentIndex + " / " + loading.maxIndex, gc.getWidth() - 185, gc.getHeight() - 30);
	}
	
	public void doneLoading() {
		
	}
	
	public void initialize() throws SlickException {
		
	}
	
	public boolean canTransition(int delta) 
	{
		return true;
	}
}
