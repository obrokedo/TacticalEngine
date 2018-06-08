package tactical.game.sprite;

import java.io.Serializable;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.loading.ResourceManager;

public class Sprite implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final byte TYPE_COMBAT = 1;
	public static final byte TYPE_NPC = 2;
	public static final byte TYPE_STATIC_SPRITE = 3;
	protected Rectangle spriteBounds;
	private int tileX, tileY;
	protected byte spriteType;
	protected String name;
	protected boolean visible = true;
	protected int id;

	// protected static StateInfo stateInfo;

	public Sprite(int locX, int locY, int id)
	{
		spriteBounds = new Rectangle(locX, locY, 24, 24);
	}

	public void initializeSprite(ResourceManager fcrm)
	{ }

	public void update(StateInfo stateInfo)
	{

	}

	public void render(Camera camera, Graphics graphics, GameContainer cont, int tileHeight)
	{

	}

	public void destroy(StateInfo stateInfo)
	{

	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public float getLocX() {
		return spriteBounds.getX();
	}

	public void setLocX(float locX, int tileWidth) {
		spriteBounds.setX(locX);
		tileX = (int) Math.ceil(locX / tileWidth);
	}

	public float getLocY() {
		return spriteBounds.getY();
	}
	
	public void setLocY(float locY, int tileHeight) {
		spriteBounds.setY(locY);
		tileY = (int) Math.ceil(locY / tileHeight);
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileY() {
		return tileY;
	}
	
	public Rectangle getSpriteBounds() {
		return spriteBounds;
	}

	public byte getSpriteType() {
		return spriteType;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
