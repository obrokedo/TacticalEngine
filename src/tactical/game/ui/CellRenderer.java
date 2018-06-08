package tactical.game.ui;

import org.newdawn.slick.Graphics;


public interface CellRenderer<T>
{
	public abstract String getColumnValue(T item, int column, Graphics g);
}
