package tactical.game.sprite;

import java.io.Serializable;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.loading.ResourceManager;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sprite implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final byte TYPE_COMBAT = 1;
	public static final byte TYPE_NPC = 2;
	public static final byte TYPE_STATIC_SPRITE = 3;
	@Getter protected Rectangle spriteBounds;
	@Getter @Setter private int tileX, tileY;
	@Getter protected byte spriteType;
	@Getter protected String name;
	@Getter @Setter protected boolean visible = true;
	@Getter protected int id;

	// protected static StateInfo stateInfo;

	public Sprite(int locX, int locY, int id)
	{
		spriteBounds = new Rectangle(locX, locY, 24, 24);
		this.id = id;
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
}
