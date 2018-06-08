package tactical.game.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

public class SelectRectUI extends Rectangle
{
	private static final long serialVersionUID = 1L;

	public SelectRectUI(float x, float y, float width, float height) {
		super(x,
				y,
				width,
				height);
	}

	public void draw(Graphics g, Color color)
	{
		g.setColor(color);
		for (int i = 0; i < 3; i++)
			g.drawRect(x + i, y + i, width - i * 2, height - i * 2);
	}

	@Override
	public void setX(float x) {
		super.setX(x);
	}
}
