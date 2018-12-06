package tactical.utils.planner.components;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.GUIContext;

public abstract class SlickComponent extends AbstractComponent {
	protected int x, y;
	protected int width, height;
	protected boolean isMouseOver;
	
	public SlickComponent(GUIContext container) {
		super(container);
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void update() {
		
	}
	
	public final void notifyMouseEntered() {
		isMouseOver = true;
		mouseEntered();		
	}
	
	public final void notifyMouseExited() {
		isMouseOver = false;
		mouseExited();		
	}
	
	public void mouseEntered() {
		
	}
	
	public void mouseExited() {
		
	}
}
