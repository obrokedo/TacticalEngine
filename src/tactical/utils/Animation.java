package tactical.utils;

import java.io.Serializable;
import java.util.ArrayList;

import org.newdawn.slick.geom.Point;

public class Animation implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String name;
	public ArrayList<AnimFrame> frames;

	public Animation(String name) {
		super();
		this.name = name;
		this.frames = new ArrayList<AnimFrame>();
	}

	public int getAnimationLength()
	{
		int length = 0;
		for (AnimFrame af : frames)
			length += af.delay;

		return length;
	}

	public int getAnimationLengthMinusLast()
	{
		int length = 0;
		for (int i = 0; i < frames.size() - 1; i++)
			length += frames.get(i).delay;

		return length;
	}

	public Point getFirstFramePosition()
	{
		return new Point(frames.get(0).sprites.get(0).x, frames.get(0).sprites.get(0).y);
	}
}
