package tactical.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.config.AnimationConfiguration;
import tactical.game.constants.Direction;
import tactical.game.exception.BadAnimationException;
import tactical.game.exception.BadResourceException;

public class SpriteAnims
{
	protected Hashtable<String, Animation> animations;
	private String spriteSheet;
	public ArrayList<Rectangle> imageLocs;
	public transient ArrayList<Image> images;

	public SpriteAnims(String spriteSheet, ArrayList<Rectangle> imageLocs)
	{
		animations = new Hashtable<String, Animation>();
		this.imageLocs = imageLocs;
		this.spriteSheet = spriteSheet;
	}

	public void addAnimation(String name, Animation anim)
	{
		animations.put(name, anim);
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

	public boolean hasAnimation(String name)
	{
		return animations.get(name) != null;
	}

	public boolean hasCharacterAnimation(String name, boolean isPromoted)
	{
		return animations.get((isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name) != null;
	}

	public Animation getCharacterAnimation(String name, boolean isPromoted)
	{	
		Animation a = animations.get((isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name);
		if (a == null)
			throw new BadResourceException("Unable to find animation: " +
					(isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name + " for animation using spritesheet: " + spriteSheet);
		return a;
	}
	
	public Animation getDirectionAnimation(Direction dir) {
		String animString = null;
		if (hasAnimation(animString = AnimationConfiguration.getUnpromotedDirection(dir)))
			return getAnimation(animString);
		else if (hasAnimation(animString = AnimationConfiguration.getPromotedDirection(dir)))
			return getAnimation(animString);
		else 
			throw new BadAnimationException("Unable to get character direction animation for dir " + dir + " on spritesheet " + 
					spriteSheet +". Neither a unpromoted or promoted animation has been defined");
	}
	
	public Animation getCharacterDirectionAnimation(Direction dir, boolean isHeroPromoted) {
		switch (dir)
		{
			case UP:
				return getCharacterAnimation(AnimationConfiguration.getUpAnimationName(), isHeroPromoted);
			case DOWN:
				return getCharacterAnimation(AnimationConfiguration.getDownAnimationName(), isHeroPromoted);
			case LEFT:
				return getCharacterAnimation(AnimationConfiguration.getLeftAnimationName(), isHeroPromoted);
			case RIGHT:
				return getCharacterAnimation(AnimationConfiguration.getRightAnimationName(), isHeroPromoted);
		}
		throw new BadAnimationException("Unable to get character direction animation for dir " + dir + " on spritesheet " + spriteSheet);
	}

	public Animation getAnimation(String name)
	{
		Animation a = animations.get(name);
		if (a == null)
			throw new BadResourceException("Unable to find animation: " +
					name + " for animation using spritesheet: " + spriteSheet);
		return a;
	}

	public Image getImageAtIndex(int idx)
	{
		return images.get(idx);
	}

	public String getSpriteSheet() {
		return spriteSheet;
	}

	public void printAnimations()
	{
		System.out.println("-- Print Animations --");
		for (String a : animations.keySet())
			System.out.println(a);
	}

	public Set<String> getAnimationKeys()
	{
		return animations.keySet();
	}
}
