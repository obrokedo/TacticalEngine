package tactical.utils;

import java.io.Serializable;
import java.util.ArrayList;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import lombok.Getter;

public class Animation implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String name;
	public ArrayList<AnimFrame> frames;
	@Getter private String spriteSheet;
	public ArrayList<Rectangle> imageLocs;
	public transient ArrayList<Image> images;

	public Animation(String name, ArrayList<Rectangle> imageLocs, String spriteSheet) {
		super();
		this.name = name;
		this.frames = new ArrayList<AnimFrame>();
		this.imageLocs = imageLocs;
		this.spriteSheet = spriteSheet;
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
	
	public void initialize(Image image)
	{
		images = new ArrayList<Image>();

		for (int i = 0; i < imageLocs.size(); i++)
		{
			Rectangle r = imageLocs.get(i);
			Image subImage = image.getSubImage((int) r.getX(), (int) r.getY(), (int) r.getWidth(),
					(int) r.getHeight());
			subImage.setFilter(Image.FILTER_NEAREST);
			images.add(subImage);
		}
	}
	
	public Image getImageAtIndex(int idx)
	{
		return images.get(idx);
	}
}
