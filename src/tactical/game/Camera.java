package tactical.game;


import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.state.StateInfo;
import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.Sprite;
import tactical.map.Map;

/**
 * Contains information that indicates which portion of a map should be displayed at any given time.
 * Also contains helper methods to move the camera around.
 *
 * @author Broked
 *
 */
public class Camera
{
	private Rectangle viewport;

	public Camera(int width, int height) {
		super();
		viewport = new Rectangle(0, 0, width, height);
	}

	private Camera(Camera camera)
	{
		this.viewport = new Rectangle(camera.viewport.getX(), camera.viewport.getY(),
				camera.viewport.getWidth(), camera.viewport.getHeight());
	}

	public float getLocationX()
	{
		return viewport.getX();
	}

	public float getLocationY()
	{
		return viewport.getY();
	}

	public void setLocation(float x, float y, StateInfo stateInfo)
	{
		if (x + getViewportWidth() > stateInfo.getCurrentMap().getMapWidthInPixels())
			x = stateInfo.getCurrentMap().getMapWidthInPixels() - getViewportWidth();
		if (y + getViewportHeight() > stateInfo.getCurrentMap().getMapHeightInPixels())
			y = stateInfo.getCurrentMap().getMapHeightInPixels() - getViewportHeight();
		if (x <= 0)
			x = 0;
		if (y <= 0)
			y = 0;
		viewport.setLocation(fixXFloatPrecision(x), y);
		// nextX = x;
		// nextY = y;
	}
	
	public boolean canMoveTowards(float x, float y, StateInfo stateInfo)
	{
		
		boolean cantMoveHor = x == this.getLocationX();
		if (!cantMoveHor)
			cantMoveHor = (x < 0 && this.getLocationX() == 0) || x == this.getLocationX();
		if (!cantMoveHor)
			cantMoveHor = x + getViewportWidth() > stateInfo.getCurrentMap().getMapWidthInPixels() &&  // Cant move right
				this.getLocationX() == stateInfo.getCurrentMap().getMapWidthInPixels() - getViewportWidth();
		boolean cantMoveVert = y == this.getLocationY();
		if (!cantMoveVert)
				cantMoveVert = y < 0 && this.getLocationY() == 0;
		if (!cantMoveVert)
			cantMoveVert = y + getViewportHeight() > stateInfo.getCurrentMap().getMapHeightInPixels() &&  // Cant move down
				this.getLocationY() == stateInfo.getCurrentMap().getMapHeightInPixels() - getViewportHeight();
			
		boolean b = !(cantMoveVert && cantMoveHor);
		return b;
	}
	
	private float fixXFloatPrecision(float x) {
		float fixFloatPrecision = x - (int) x - .5f;
		if (Math.abs(fixFloatPrecision) < .002) {
			x = x + (fixFloatPrecision > .5 ? .02f : -.02f);
		}
		return x;
	}

	private void setX(float x)
	{
		viewport.setX(fixXFloatPrecision(x));
	}

	private void setY(float y)
	{
		viewport.setY(y);
	}

	/*
	float nextX, nextY;

	public void realSetLocation()
	{
		viewport.setLocation(nextX, nextY);
	}
	*/

	public int getViewportWidth()
	{
		return (int) viewport.getWidth();
	}

	public int getViewportHeight()
	{
		return (int) viewport.getHeight();
	}

	public void centerOnSprite(Sprite sprite, Map map)
	{
		if (sprite.getLocX() < getViewportWidth() / 2)
			setX(0);
		else if (sprite.getLocX() > map.getMapWidthInPixels() - getViewportWidth() / 2)
		{
			setX(Math.max(0, map.getMapWidthInPixels() - getViewportWidth()));
		}
		else
			setX(Math.max(0, sprite.getLocX() - getViewportWidth() / 2));

		if (sprite.getLocY() < getViewportHeight() / 2)
			setY(0);
		else if (sprite.getLocY() > map.getMapHeightInPixels() - getViewportHeight() / 2)
		{
			setY(Math.max(0, map.getMapHeightInPixels() - getViewportHeight()));
		}
		else
			setY(Math.max(0, sprite.getLocY() - getViewportHeight() / 2));
	}

	public void centerOnPoint(float locX, float locY, Map map)
	{
		if (locX < getViewportWidth() / 2)
			setX(0);
		else if (locX > map.getMapWidthInPixels() - getViewportWidth() / 2)
		{
			setX(Math.max(0, map.getMapWidthInPixels() - getViewportWidth()));
		}
		else
			setX(Math.max(0, locX - getViewportWidth() / 2));

		if (locY < getViewportHeight() / 2)
			setY(0);
		else if (locY >  map.getMapHeightInPixels() - getViewportHeight() / 2)
		{
			setY(Math.max(0, map.getMapHeightInPixels() - getViewportHeight()));
		}
		else
			setY(Math.max(0, locY - getViewportHeight() / 2));
	}
	
	public Point getCenterOfCamera()
	{
		return new Point(this.getLocationX() + getViewportWidth() / 2,
				this.getLocationY() + getViewportHeight() / 2);
	}
	
	public boolean isVisible(AnimatedSprite animatedSprite) {
		return this.viewport.contains(animatedSprite.getSpriteBounds().getCenterX(),
				animatedSprite.getSpriteBounds().getCenterY());
	}
	
	public Point getSpriteScreenPosition(AnimatedSprite animatedSprite, int tileHeight) {
		return new Point((int) (animatedSprite.getLocX() - getLocationX()),
					(int) (animatedSprite.getLocY() - getLocationY() - tileHeight / 2));
	}

	public Camera duplicate()
	{
		return new Camera(this);
	}
}
