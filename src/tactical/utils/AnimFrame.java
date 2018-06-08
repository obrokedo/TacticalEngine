package tactical.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class AnimFrame implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int delay;
	public ArrayList<AnimSprite> sprites;
	
	public AnimFrame(int delay) {
		super();
		this.delay = delay;
		sprites = new ArrayList<AnimSprite>();
	}
}
