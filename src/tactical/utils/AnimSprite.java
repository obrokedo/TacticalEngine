package tactical.utils;

import java.io.Serializable;

public class AnimSprite implements Serializable
{
	private static final long serialVersionUID = 1L;

	public int x, y, imageIndex, angle;
	public boolean flipH, flipV;

	public AnimSprite(int x, int y, int imageIndex, int angle, boolean flipH, boolean flipV) {
		super();
		this.x = x;
		this.y = y;
		this.imageIndex = imageIndex;
		this.angle = angle;
		this.flipH = flipH;
		this.flipV = flipV;
	}
}
