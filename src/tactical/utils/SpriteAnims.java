package tactical.utils;

import java.util.Hashtable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tactical.engine.config.AnimationConfiguration;
import tactical.game.constants.Direction;
import tactical.game.exception.BadAnimationException;
import tactical.game.exception.BadResourceException;

@AllArgsConstructor
public class SpriteAnims
{
	@Getter private String parentDir;
	protected Hashtable<String, Animation> animations;		

	public SpriteAnims()
	{
		animations = new Hashtable<String, Animation>();
	}

	public void addAnimation(String name, Animation anim)
	{
		animations.put(name, anim);
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
					(isPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) + name + " for animations loaded from: " + parentDir);
		return a;
	}
	
	public Animation getDirectionAnimation(Direction dir) {
		String animString = null;
		if (hasAnimation(animString = AnimationConfiguration.getUnpromotedDirection(dir)))
			return getAnimation(animString);
		else if (hasAnimation(animString = AnimationConfiguration.getPromotedDirection(dir)))
			return getAnimation(animString);
		else 
			throw new BadAnimationException("Unable to get character direction animation for dir " + dir + " on spritesheet for animations loaded from: " + parentDir
					+ "Neither a unpromoted or promoted animation has been defined");
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
		throw new BadAnimationException("Unable to get character direction animation for dir " + dir  + " for animations loaded from: " + parentDir);
	}

	public Animation getAnimation(String name)
	{
		Animation a = animations.get(name);
		if (a == null)
			throw new BadResourceException("Unable to find animation: " +
					name  + " for animations loaded from: " + parentDir);
		return a;
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
