package tactical.game.sprite;

import org.newdawn.slick.Image;

public class Door extends StaticSprite
{
	private static final long serialVersionUID = 1L;

	public Door(int doorId, int locX, int locY, Image image) {
		super(locX, locY, "door" + doorId, image, null);
		this.id = doorId;
	}
}
