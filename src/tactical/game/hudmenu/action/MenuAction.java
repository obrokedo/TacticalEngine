package tactical.game.hudmenu.action;

import org.newdawn.slick.geom.Rectangle;

public class MenuAction 
{
	public static final int MA_CLOSE_WINDOW = 1;
	public static final int MA_CUSTOM = 2;
	
	protected int menuAction;
	protected Rectangle bounds;
	protected long lastClick;
	private boolean onMouseDown = false;
	
	public MenuAction(int menuAction, Rectangle bounds, boolean onMouseDown)
	{
		this.menuAction = menuAction;
		this.bounds = bounds;
		this.onMouseDown = onMouseDown;
	}

	public int getMenuAction() {
		return menuAction;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public boolean isOnMouseDown() {
		return onMouseDown;
	}
	
	public boolean allowClick()
	{
		return System.currentTimeMillis() >= lastClick;			
	}
	
	public void clicked()
	{
		if (onMouseDown)
			lastClick = System.currentTimeMillis() + 500;
	}
}
