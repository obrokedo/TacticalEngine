package tactical.game.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.TacticalGame;

public class RectUI extends Rectangle
{
	private static final long serialVersionUID = 1L;

	public RectUI(float x, float y, float width, float height,
			int nonScaleX, int nonScaleY, int nonScaleWidth, int nonScaleHeight) {
		super(x + nonScaleX,
				y + nonScaleY,
					width + nonScaleWidth,
						height + nonScaleHeight);
	}

	public RectUI(float x, float y, float width, float height)
	{
		this(x, y, width, height, 0, 0, 0, 0);
	}

	public void fillRect(Graphics g, Color color)
	{
		g.setColor(color);
		g.fill(this);
	}

	public void drawRect(Graphics g, Color color)
	{
		g.setColor(color);
		g.draw(this);
	}

	public void drawPanel(Graphics g)
	{
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render((int) x, (int) y, (int) width, (int) height, g, null);
	}

	public void drawPanel(Graphics g, Color color)
	{
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render((int) x, (int) y, (int) width, (int) height, g, color);
	}

	@Override
	public void setX(float x) {
		super.setX(x);
	}

	@Override
	public void setY(float y) {
		super.setY(y);
	}


}
