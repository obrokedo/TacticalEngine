package tactical.utils.planner.components;

import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.GUIContext;

public class SlickList<T extends SlickComponent> extends SlickComponent {
	private int listPosition;
	private List<T> listItems;
	
	public SlickList(GUIContext container, int x, int y) {
		super(container);
		setLocation(x, y);
	}
	
	@Override
	public void render(GUIContext gui, Graphics g) throws SlickException {
		g.setClip(x, y, width, height);
		int renderAt = -listPosition;
		
		for (T item : listItems) {
			int itemHeight = item.getHeight();
			item.setLocation(x, renderAt);
			item.render(gui, g);
			renderAt += itemHeight;
		}
	}	
	
	public void addListItem(T item) {
		this.listItems.add(item);
	}

	protected int getFullHeight() {
		int fh = 0;
		for (T item : listItems)
			fh += item.getHeight();
		return fh;
	}
	
	
}
