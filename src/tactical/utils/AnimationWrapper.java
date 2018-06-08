package tactical.utils;

import java.util.Set;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import tactical.game.exception.BadAnimationException;

public class AnimationWrapper
{
	protected SpriteAnims spriteAnims;
	protected Animation animation;
	protected int animationIndex;
	protected int animationDelta;
	protected boolean loops;
	protected Image weapon = null;

	public AnimationWrapper(SpriteAnims spriteAnims)
	{
		this.spriteAnims = spriteAnims;
	}

	public AnimationWrapper(SpriteAnims spriteAnims, String animationName)
	{
		this.spriteAnims = spriteAnims;
		setAnimation(animationName, false);
	}

	public AnimationWrapper(SpriteAnims spriteAnims, String animationName, boolean loops)
	{
		this.spriteAnims = spriteAnims;
		setAnimation(animationName, loops);
	}

	public AnimationWrapper(SpriteAnims spriteAnims, String animationName, boolean loops, Image weapon)
	{
		this.spriteAnims = spriteAnims;
		this.weapon = weapon;
		setAnimation(animationName, loops);
	}

	/**
	 * Set the animation to be displayed by name starting from the first frame of the animation
	 *
	 * @param animationName the animation that should be displayed
	 * @param loops whether this animation should loop once it has finished
	 */
	public void setAnimation(String animationName, boolean loops)
	{
		Log.debug("Setting animation: " + animationName);

		this.animation = spriteAnims.getAnimation(animationName);
		if (animation == null)
			throw new BadAnimationException("No animation for the action: " + animationName + " could be found.");
		this.loops = loops;
		resetCurrentAnimation();

	}

	/**
	 * Sets this animation back to display the first animation frame with no time elapsed
	 */
	public void resetCurrentAnimation()
	{
		animationIndex = 0;
		animationDelta = 0;
	}

	public boolean update(long delta)
	{
		animationDelta += delta;
		while (animationDelta >= animation.frames.get(animationIndex).delay)
		{
			animationDelta -= animation.frames.get(animationIndex).delay;
			if (animationIndex + 1 >= animation.frames.size())
			{
				if (loops) {
					animationIndex = 0;
				}
				else
				{
					animationDelta -= animation.frames.get(animationIndex).delay;
					return true;
				}
			}
			else
			{
				animationIndex++;
			}
		}
		return false;
	}

	/**
	 * Renders the animation with the animation being drawn at a position relative to the specified coordinates.
	 *
	 * @param x the x location that the animation should be drawn relative to
	 * @param y the y location that the animation should be drawn relative to
	 * @param g the graphics to draw to
	 */
	public void drawAnimation(int x, int y, Graphics g)
	{
		drawAnimation(x, y, null, g);
	}

	/**
	 * Renders the given animation at the specified location, ignoring the animations locations
	 *
	 * @param x the x location to draw the animation at
	 * @param y the y location to draw the animation at
	 * @param g the graphics to draw to
	 */
	public void drawAnimationIgnoreOffset(int x, int y, Graphics g)
	{
		if (animation != null)
		{
			for (AnimSprite as : animation.frames.get(animationIndex).sprites)
			{
				if (as.imageIndex != -1)
				{
					g.drawImage(getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, null), x, y);
				}
				else
					drawWeapon(as, x, y, null, 1f, g);
			}
		}
	}

	/**
	 * Renders the idle animation of a portrait, that is it renders the first AnimSprite (should be the mouth) at
	 * location x, y + ySecond and the second AnimSprite (should be the head) at location x, y
	 *
	 * @param x the x location that the portrait should be drawn
	 * @param y the y location that the top of the head should be drawn at
	 * @param ySecond the offset from the given y position that the top of the mouth should be drawn at
	 * @param g the graphics to draw to
	 */
	public void drawAnimationPortrait(int x, int y, int ySecond, Graphics g)
	{
		boolean first = true;
		if (animation != null)
		{
			for (AnimSprite as : animation.frames.get(animationIndex).sprites)
			{
				if (as.imageIndex != -1)
				{
					if (first)
						g.drawImage(getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, null), x, y + ySecond);
					else
						g.drawImage(getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, null), x, y);
				}

				first = false;
			}
		}
	}

	/**
	 * Renders the animation with the animation being drawn at a position relative to the specified coordinates.
	 * A filter may be specified to render the animation in a different color. Drawn at native scale
	 *
	 * @param x the x location that the animation should be drawn relative to
	 * @param y the y location that the animation should be drawn relative to
	 * @param filter the color that that the animation should be filtered through
	 * @param g the graphics to draw to
	 */
	public void drawAnimation(int x, int y, Color filter, Graphics g)
	{
		drawAnimation(x, y, filter, null, g);
	}

	/**
	 * Renders the animation with the animation being drawn at a position relative to the specified coordinates and
	 * scaled at the specified amount. A filter may be specified to render the animation in a different color.
	 *
	 * @param x the x location that the animation should be drawn relative to
	 * @param y the y location that the animation should be drawn relative to
	 * @param filter the color that that the animation should be filtered through
	 * @param scale the scale that the animation should be rendered to relative to it's normal size
	 * @param g the graphics to draw to
	 */
	public void drawAnimation(int x, int y, Color filter, Float scale, Graphics g)
	{
		if (animation != null)
		{
			for (AnimSprite as : animation.frames.get(animationIndex).sprites)
			{
				if (as.imageIndex != -1)
				{
					if (filter == null)
						g.drawImage(getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, scale),
								x + as.x * (scale == null ? 1 : scale), y + as.y * (scale == null ? 1 : scale));
					else
						g.drawImage(getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, scale),
								x + as.x * (scale == null ? 1 : scale), y + as.y * (scale == null ? 1 : scale), filter);
				}
				else
					drawWeapon(as, x, y, filter, scale, g);
			}
		}
	}
	
	public void drawAnimationDirect(int x, int y, Float scale, boolean scaleCoords, boolean flip)
	{
		if (animation != null)
		{
			for (AnimSprite as : animation.frames.get(animationIndex).sprites)
			{
				if (as.imageIndex != -1)
				{
					if (!flip)
						getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, scale).draw(
								x + as.x * (scale == null || ! scaleCoords? 1 : scale), y + as.y * (scale == null || ! scaleCoords ? 1 : scale));
					else
						getRotatedImageIfNeeded(spriteAnims.getImageAtIndex(as.imageIndex), as, scale).getFlippedCopy(true, false).draw(
								x + as.x * (scale == null || ! scaleCoords ? 1 : scale), y + as.y * (scale == null || ! scaleCoords ? 1 : scale));
				}
			}
		}
	}
	
	public void drawAnimationDirect(int x, int y, Float scale, boolean flip)
	{
		drawAnimationDirect(x, y, scale, true, flip);
	}

	protected void drawWeapon(AnimSprite as, int x, int y, Color filter, Float scale, Graphics g)
	{
		if (weapon != null)
		{
			if (filter == null)
				g.drawImage(getRotatedImageIfNeeded(weapon, as, scale),
						x + as.x * (scale == null ? 1 : scale), y + as.y * (scale == null ? 1 : scale));
			else
				g.drawImage(getRotatedImageIfNeeded(weapon, as, scale),
						x + as.x * (scale == null ? 1 : scale), y + as.y * (scale == null ? 1 : scale), filter);
		}
	}

	/**
	 * Returns an image that is rotated and flipped to respect the orientation described by the AnimSprite
	 *
	 * @param image the image to flip/rotate as needed
	 * @param as the AnimSprite that describes what the images orientation is
	 * @param scale the size that this image should be scaled to relative to it's normal size. Value of
	 * null should be specified if the image should not be scaled
	 * @return an image that is rotated and flipped to respect the orientation described by the AnimSprite
	 */
	protected Image getRotatedImageIfNeeded(Image image, AnimSprite as, Float scale)
	{
		Image im = image;

		if (as.angle != 0 || as.flipH || as.flipV || scale != null)
		{
			if (scale == null || scale == 1.0f) {
				im = image.copy();
			}
			else
				im = image.getScaledCopy(scale);

			if (as.flipH || as.flipV)
				im = im.getFlippedCopy(as.flipH, as.flipV);

			if (as.angle != 0)
				im.rotate(as.angle);
		}
		
		return im;
	}

	public int getAnimationLength()
	{
		if (animation != null)
		{
			return animation.getAnimationLength();
		}
		else
			return -1;
	}

	public boolean hasAnimation(String animationName)
	{
		return this.spriteAnims.hasAnimation(animationName);
	}

	public Animation getCurrentAnimation()
	{
		return animation;
	}

	public Set<String> getAnimations()
	{
		return spriteAnims.getAnimationKeys();
	}

	public void setWeapon(Image weapon) {
		this.weapon = weapon;
	}

	public void copyAnimationLocation(AnimationWrapper wrapper) {
		this.animationDelta = wrapper.animationDelta;
		this.animationIndex = wrapper.animationIndex;
	}
	
	public void setLoops(boolean loops) {
		this.loops = loops;
	}
}
