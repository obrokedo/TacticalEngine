package tactical.cinematic;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import tactical.engine.TacticalGame;
import tactical.engine.config.AnimationConfiguration;
import tactical.engine.config.CinematicActorConfiguration;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.constants.Direction;
import tactical.game.exception.BadAnimationException;
import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.AnimSprite;
import tactical.utils.Animation;
import tactical.utils.SpriteAnims;

/**
 * Defines an "Actor" in a cinematic and contains the state information
 * to render the given actor and progress its' state as engine time passes
 *
 * @author Broked
 *
 */
public class CinematicActor implements Comparable<CinematicActor>
{
	private static final int INDEFINITE_TIME = -1;

	public static final int SE_NONE = 0;
	public static final int SE_SHRINK = 1;
	public static final int SE_GROW = 2;
	public static final int SE_QUIVER = 3;
	public static final int SE_FALL_ON_FACE = 4;
	public static final int SE_LAY_ON_SIDE_LEFT = 5;
	public static final int SE_FLASH = 6;
	public static final int SE_NOD = 7;
	public static final int SE_HEAD_SHAKE = 8;
	public static final int SE_LAY_ON_BACK = 9;
	public static final int SE_TREMBLE = 10;
	public static final int SE_LAY_ON_SIDE_RIGHT = 11;

	private SpriteAnims spriteAnims;
	private Animation currentAnim;
	private int imageIndex;
	private boolean animationLooping;
	private Direction facing;
	private long animUpdate = Long.MAX_VALUE;
	private long animDelta = 0;
	private boolean animHalting;
	private AnimatedSprite sprite;
	private boolean visible = true;

	// Moving location
	private float locX;
	private float locY;
	private int moveToLocX;
	private int moveToLocY;
	private Path movePath;
	private int movePathIndex;
	private boolean haltingMove;
	private float moveSpeed;
	private boolean loopMoving = false;
	private float startLoopX;
	private float startLoopY;
	private boolean forceFacingMove = false;
	private boolean moveHorFirst;
	private boolean moveDiag;

	// Rotate Params
	private int spinSpeed;
	private int spinDuration;
	private int spinDelta;
	private boolean spinning = false;

	private int specialEffectType;
	private int specialEffectUpdate;
	private int specialEffectDelta;
	private int specialEffectDuration;
	private float specialEffectCounter;
	private Direction specialEffectDirection;

	boolean isHeroBacked = false;
	boolean isHeroPromoted = false;

	private Color flashColor = new Color(255, 255, 255);

	private boolean moving;

	public CinematicActorConfiguration jCinematicActor;

	public CinematicActor(SpriteAnims spriteAnims, String initialAnimation, int x, int y, boolean visible,
			boolean isHeroBacked, boolean isHeroPromoted)
	{
		jCinematicActor = TacticalGame.ENGINE_CONFIGURATIOR.getCinematicActorConfiguration();
		this.isHeroBacked = isHeroBacked;
		this.isHeroPromoted = isHeroPromoted;
		this.facing = Direction.DOWN;
		this.spriteAnims = spriteAnims;

		if (isHeroBacked)
			currentAnim = this.spriteAnims.getCharacterAnimation(initialAnimation, isHeroPromoted);
		else
			currentAnim = this.spriteAnims.getAnimation(initialAnimation);

		this.setAnimation(initialAnimation, 1000, false, true);
		this.locX = x;
		this.locY = y;
		this.visible = visible;
	}

	public CinematicActor(AnimatedSprite sprite, StateInfo stateInfo)
	{
		jCinematicActor = TacticalGame.ENGINE_CONFIGURATIOR.getCinematicActorConfiguration();
		// Set the sprite that is in the town or battle to invisible
		sprite.setVisible(false);
		this.isHeroBacked = true;
		this.isHeroPromoted = false;
		if (sprite instanceof CombatSprite)
			this.isHeroPromoted = ((CombatSprite) sprite).isPromoted();
		this.sprite = sprite;
		this.spriteAnims = sprite.getSpriteAnims();
		currentAnim = sprite.getCurrentAnim();
		this.facing = sprite.getFacing();
		// For sprite backed actors, compensate for their usual offset
		this.locX = sprite.getLocX();
		this.locY = sprite.getLocY() - stateInfo.getResourceManager().getMap().getTileRenderHeight();
		
	}
	
	public void render(Graphics graphics, Camera camera, PaddedGameContainer cont, StateInfo stateInfo)
	{
		if (!visible)
			return;
		if (specialEffectType == SE_FALL_ON_FACE)
			renderOnDirection(Direction.UP, graphics, camera, cont);
		else if (specialEffectType == SE_LAY_ON_SIDE_RIGHT)
			renderOnDirection(Direction.RIGHT, graphics, camera, cont);
		else if (specialEffectType == SE_LAY_ON_SIDE_LEFT)
			renderOnDirection(Direction.LEFT, graphics, camera, cont);
		else if (specialEffectType == SE_LAY_ON_BACK)
			renderOnDirection(Direction.DOWN, graphics, camera, cont);
		else
		{
			for (AnimSprite as : currentAnim.frames.get(imageIndex).sprites)
			{
				Image im = spriteAnims.getImageAtIndex(as.imageIndex);
				if (as.flipH) {
					im = im.getFlippedCopy(true, false);
				}
				if (as.flipV) {
					im = im.getFlippedCopy(false, true);
				}
				float xPos = locX - camera.getLocationX();
				float yPos = locY + stateInfo.getTileHeight() / 6 - camera.getLocationY();
				switch (specialEffectType)
				{
					case SE_NONE:
						AnimatedSprite.drawShadow(im, xPos, yPos, 
								camera, false, stateInfo.getTileHeight());
						graphics.drawImage(im, xPos, yPos);
						break;
					case SE_GROW:
					case SE_SHRINK:
						Image scaled = im.getScaledCopy(specialEffectCounter);

						AnimatedSprite.drawShadow(scaled, (xPos  + (im.getWidth() - scaled.getWidth()) / 2),
								 (yPos + im.getHeight() - scaled.getHeight()), camera, false, stateInfo.getTileHeight());

						scaled.draw(xPos + (im.getWidth() - scaled.getWidth()) / 2,
								yPos + im.getHeight() - scaled.getHeight());
						break;
					case SE_QUIVER:

						AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos + (specialEffectCounter % 2 == 0 ? 0 : (-2 + specialEffectCounter)),
								yPos, camera, false, stateInfo.getTileHeight());

						graphics.drawImage(im, xPos  + (specialEffectCounter % 2 == 0 ? 0 : (-2 + specialEffectCounter)),
								yPos);
						break;
					case SE_FLASH:
						AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos, camera, false, stateInfo.getTileHeight());

						Image whiteIm = im;

						// 1. bind the sprite sheet
						whiteIm.bind();

						// 2. change texture environment
						GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
								GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);

						// 3. start rendering the sprite sheet
						whiteIm.startUse();

						// 4. bind any colors, draw any sprites
						flashColor.bind();
						whiteIm.drawEmbedded(xPos ,
								yPos, whiteIm.getWidth(), whiteIm.getHeight());

						// 5. stop rendering the sprite sheet
						whiteIm.endUse();

						// 6. reset the texture environment
						GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
								GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

						/*
						graphics.drawImage(im, locX - camera.getLocationX() ,
								locY - camera.getLocationY());
						graphics.drawImage(whiteIm, locX - camera.getLocationX() ,
								locY - camera.getLocationY(), flashColor);
								*/
						break;
					case SE_NOD:
						// Only draw the nod if we are on SE counter 1
						if (specialEffectCounter == 1)
						{
							AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos, camera, false, stateInfo.getTileHeight());

							// This is the lower portion of the sprite
							im.getSubImage(0, 10, im.getWidth(), 14).draw(xPos,
									yPos + 10);
							// This is the head of the sprite
							im.getSubImage(0, 0, im.getWidth(), 10).draw(xPos,
									yPos + 1);
						}
						else
						{
							AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos, camera, false, stateInfo.getTileHeight());

							graphics.drawImage(im, xPos ,
									yPos);
						}
						break;
					case SE_HEAD_SHAKE:
						AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos, camera, false, stateInfo.getTileHeight());

						im.getSubImage(0, 10,
								im.getWidth(), 14).draw(xPos,
								yPos + 10);

						switch ((int) specialEffectCounter % 4)
						{
							case 0:
							case 2:
								im.getSubImage(0, 0, im.getWidth(), 10).draw(xPos,
									yPos);
								break;
							case 1:
								im.getSubImage(0, 0, im.getWidth(), 10).draw(xPos + 1,
										yPos);
								break;
							case 3:
								im.getSubImage(0, 0, im.getWidth(), 10).draw(xPos - 1,
										yPos);
								break;
						}

						break;
					case SE_TREMBLE:
						int trembleVal = 4 - (int) Math.abs(specialEffectCounter - 4);
						if (trembleVal > 0)
						{
							im = spriteAnims.getImageAtIndex(as.imageIndex).getScaledCopy(
									spriteAnims.getImageAtIndex(as.imageIndex).getWidth() - trembleVal,
									spriteAnims.getImageAtIndex(as.imageIndex).getHeight() - trembleVal);
						}
						else
						{
							im = spriteAnims.getImageAtIndex(as.imageIndex);
							trembleVal = 0;
						}

						AnimatedSprite.drawShadow(im, xPos + (trembleVal) / 2,
								yPos + trembleVal, camera, false, stateInfo.getTileHeight());

						graphics.drawImage(im, xPos  + (trembleVal) / 2,
								yPos + trembleVal);
						break;
				}
			}
		}
	}

	private void renderOnDirection(Direction dir, Graphics graphics, Camera camera, PaddedGameContainer cont)
	{
		Animation anim = null;
		if (isHeroBacked)
			anim = spriteAnims.getCharacterDirectionAnimation(dir, isHeroPromoted);
		else
			anim = spriteAnims.getDirectionAnimation(dir);
			
		for (AnimSprite as : anim.frames.get(0).sprites)
		{
			Image im = spriteAnims.getImageAtIndex(as.imageIndex).copy();
			if (as.flipH) {
				im = im.getFlippedCopy(true, false);
			}
			if (as.flipV) {
				im = im.getFlippedCopy(false, true);
			}
			switch (specialEffectDirection)
			{
				case UP:
					break;
				case RIGHT:
					im.rotate(90f);
					break;
				case DOWN:
					im.rotate(180f);
					break;
				case LEFT:
					im.rotate(270f);
					break;

			}

			graphics.drawImage(im, locX - camera.getLocationX(),
					locY - camera.getLocationY());
		}
	}

	private boolean moveHorz(float movePercent)
	{
		if (locX != moveToLocX)
		{
			if (locX > moveToLocX)
				setLocX(getLocX() - Math.min(moveSpeed * movePercent, locX - moveToLocX));
			else
				setLocX(getLocX() + Math.min(moveSpeed * movePercent, moveToLocX - locX));
			return true;
		}
		else
			return false;
	}

	private boolean moveVert(float movePercent)
	{
		if (locY != moveToLocY)
		{
			if (locY > moveToLocY)
				setLocY(getLocY() - Math.min(moveSpeed * movePercent, locY - moveToLocY));
			else
				setLocY(getLocY() + Math.min(moveSpeed * movePercent, moveToLocY - locY));
			return true;
		}
		else
			return false;
	}

	public void update(int delta, Cinematic cinematic, StateInfo stateInfo)
	{
		animDelta += delta;
		while (animDelta > animUpdate)
		{
			animDelta -= animUpdate;
			if (imageIndex % currentAnim.frames.size() == currentAnim.frames.size() - 1)
			{
				imageIndex = 0;

				// If this isn't a looping animation then
				if (!animationLooping)
				{
					setFacing(facing);
					if (animHalting)
					{
						animHalting = false;
						cinematic.decreaseAnims();
						this.animUpdate = Long.MAX_VALUE;
					}
				}
			}
			else
				imageIndex++;
		}

		if (spinning)
		{
			spinDelta += delta;
			while (spinDelta > spinSpeed)
			{
				spinDelta -= spinSpeed;

				switch (facing)
				{
					case UP:
						setFacing(Direction.LEFT);
						break;
					case LEFT:
						setFacing(Direction.DOWN);
						break;
					case DOWN:
						setFacing(Direction.RIGHT);
						break;
					case RIGHT:
						setFacing(Direction.UP);
						break;
				}

				if (spinDuration != INDEFINITE_TIME)
				{
					spinDuration = Math.max(0, spinDuration - spinSpeed);
					if (spinDuration == 0)
						spinning = false;
				}
			}
		}

		if (moving)
		{
			/*
			movingDelta += delta;
			while (movingDelta > jCinematicActor.getMoveUpdate())
			{
				movingDelta -= jCinematicActor.getMoveUpdate();
				*/
				boolean moved = false;
				float movePercent = (1.0f * delta) / jCinematicActor.getMoveUpdate();

				if (moveDiag)
				{
					if (moveVert(movePercent))
						moved = true;
					if (moveHorz(movePercent))
						moved = true;
				}
				else
				{
					if (moveHorFirst)
						moved = moveHorz(movePercent);

					if (!moved)
						moved = moveVert(movePercent);

					if (!moveHorFirst && !moved)
						moved = moveHorz(movePercent);
				}


				if (!moved)
				{
					if (!loopMoving)
					{
						moving = false;
						this.forceFacingMove = false;

						if (haltingMove)
						{
							haltingMove = false;
							cinematic.decreaseMoves();
						}
						this.animUpdate = jCinematicActor.getAnimUpdateAfterSE();
						// this.animUpdate = Long.MAX_VALUE;
					}
					else
					{
						// Set the directions directly so the facing does not change
						this.locX = startLoopX;
						this.locY = startLoopY;
					}
				}
				// Check to see if we are moving along a path,
				// if so then we may need to set the next move location
				else if (movePath != null)
				{
					if (this.locX == this.moveToLocX &&
							this.locY == this.moveToLocY)
					{
						movePathIndex++;
						if (movePathIndex < movePath.getLength())
						{
							Step step = this.movePath.getStep(movePathIndex);
							this.moveToLocX = step.getX();
							this.moveToLocY = step.getY() - (this.sprite != null ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0);
						}
					}
				}
			// }
		}

		if (specialEffectType != SE_NONE)
		{
			specialEffectDelta += delta;
			if (specialEffectDuration != INDEFINITE_TIME)
			{
				specialEffectDuration = Math.max(0, specialEffectDuration - delta);
				// if (specialEffectDuration == IN)
			}

			while (specialEffectDelta > specialEffectUpdate && specialEffectType != SE_NONE)
			{
				specialEffectDelta -= specialEffectUpdate;

				switch (specialEffectType)
				{
					case SE_SHRINK:
						specialEffectCounter -= .01f;
						if (specialEffectDuration == 0 && specialEffectCounter < .01f)
							specialEffectType = SE_NONE;
						break;
					case SE_GROW:
						specialEffectCounter += .01f;
						if (specialEffectDuration == 0 && specialEffectCounter > .99f)
							specialEffectType = SE_NONE;
						break;
					case SE_QUIVER:
						specialEffectCounter = (specialEffectCounter + 1) % 4;
						break;
					case SE_TREMBLE:
						specialEffectCounter = (specialEffectCounter + 1) % 19;
						break;
					case SE_FLASH:
						if (specialEffectDuration == INDEFINITE_TIME || specialEffectDuration > 0)
						{
							specialEffectCounter = (specialEffectCounter + 5) % 510;

							if (specialEffectCounter <= 255)
								flashColor.r = flashColor.g =  flashColor.b = specialEffectCounter / 255;
							else
								flashColor.r = flashColor.g =  flashColor.b = (510 - specialEffectCounter) / 255;
							break;
						}
						else
							specialEffectType = SE_NONE;
						break;
					case SE_NOD:
						specialEffectCounter++;
						if (specialEffectCounter == 3)
						{
							specialEffectType = SE_NONE;
							animUpdate = jCinematicActor.getAnimUpdateAfterSE();
						}
						break;
					case SE_HEAD_SHAKE:
						if (specialEffectCounter + 1 == 10)
						{
							specialEffectType = SE_NONE;
							animUpdate = jCinematicActor.getAnimUpdateAfterSE();
						}
						else
							specialEffectCounter++;
						break;
				}
			}
		}
	}

	public void shakeHead(int speed)
	{
		specialEffectType = SE_HEAD_SHAKE;
		specialEffectDelta = 0;
		specialEffectUpdate = speed / 10;
		specialEffectCounter = 0;
		animUpdate = Long.MAX_VALUE;
	}

	public void nodHead()
	{
		specialEffectType = SE_NOD;
		specialEffectCounter = 0;
		specialEffectDelta = 0;
		specialEffectUpdate = jCinematicActor.getNodHeadDuration();
		specialEffectCounter = 0;
		animUpdate = Long.MAX_VALUE;
	}

	public void fallOnFace(Direction dir)
	{
		specialEffectType = SE_FALL_ON_FACE;
		specialEffectDuration = -1;
		specialEffectUpdate = 500;
		specialEffectDirection = dir;
		animUpdate = Long.MAX_VALUE;
	}

	public void layOnBack(Direction dir)
	{
		specialEffectType = SE_LAY_ON_BACK;
		specialEffectDuration = -1;
		specialEffectUpdate = 500;
		specialEffectDirection = dir;
		animUpdate = Long.MAX_VALUE;
	}

	public void layOnSideRight(Direction dir)
	{
		specialEffectType = SE_LAY_ON_SIDE_RIGHT;
		specialEffectDuration = -1;
		specialEffectUpdate = 500;
		specialEffectDirection = dir;
		animUpdate = Long.MAX_VALUE;
	}

	public void layOnSideLeft(Direction dir)
	{
		specialEffectType = SE_LAY_ON_SIDE_LEFT;
		specialEffectDuration = -1;
		specialEffectUpdate = 500;
		specialEffectDirection = dir;
		animUpdate = Long.MAX_VALUE;
	}

	public void flash(int speed, int duration)
	{
		flashColor.r = 0;
		flashColor.g = 0;
		flashColor.b = 0;
		specialEffectType = SE_FLASH;
		specialEffectDuration = duration;
		specialEffectDelta = 0;
		specialEffectUpdate = Math.max(1, speed / 102);
		specialEffectCounter = 0;
		animUpdate = Long.MAX_VALUE;
	}

	public void quiver()
	{
		specialEffectType = SE_QUIVER;
		specialEffectDuration = INDEFINITE_TIME;
		specialEffectDelta = 0;
		specialEffectUpdate = jCinematicActor.getQuiverUpdate();
		specialEffectCounter = 0;
		animUpdate = Long.MAX_VALUE;
	}

	public void tremble()
	{
		specialEffectType = SE_TREMBLE;
		specialEffectDuration = INDEFINITE_TIME;
		specialEffectDelta = 0;
		specialEffectUpdate = jCinematicActor.getTrembleUpdate();
		specialEffectCounter = 0;
		animUpdate = Long.MAX_VALUE;
	}

	public void shrink(int duration)
	{
		specialEffectType = SE_SHRINK;
		specialEffectDuration = duration;
		specialEffectDelta = 0;
		specialEffectUpdate = duration / 100;
		specialEffectCounter = 1f;
		animUpdate = Long.MAX_VALUE;
	}

	public void grow(int duration)
	{
		specialEffectType = SE_GROW;
		specialEffectDuration = duration;
		specialEffectDelta = 0;
		specialEffectUpdate = duration / 100;
		specialEffectCounter = 0f;
		animUpdate = Long.MAX_VALUE;
	}

	public void stopAnimation()
	{
		animUpdate = Long.MAX_VALUE;
	}

	public void stopSpecialEffect()
	{
		specialEffectType = SE_NONE;
		animUpdate = jCinematicActor.getAnimUpdateAfterSE();
	}

	private void setLocX(float locX) {
		if (!this.forceFacingMove)
		{
			// Moving right
			if (locX > this.locX)
				setFacing(Direction.RIGHT);
			// Moving left
			else if (locX < this.locX)
				setFacing(Direction.LEFT);
		}
		this.locX = locX;
	}

	private void setLocY(float locY) {
		if (!this.forceFacingMove)
		{
			// Moving down
			if (locY > this.locY)
				setFacing(Direction.DOWN);
			// Moving up
			else if (locY < this.locY)
				setFacing(Direction.UP);
		}
		this.locY = locY ;
	}

	public void setSpinning(int spinSpeed, int spinDuration)
	{
		this.spinSpeed = spinSpeed;
		this.spinDuration = spinDuration;
		this.animUpdate = Long.MAX_VALUE;
		this.spinDelta = 0;
		spinning = true;
	}

	public void stopSpinning()
	{
		this.spinning = false;
	}

	public void setFacing(Direction dir)
	{
		if (isHeroBacked) 
			currentAnim = spriteAnims.getCharacterDirectionAnimation(dir, isHeroPromoted);
		else
			currentAnim = spriteAnims.getDirectionAnimation(dir);
		facing = dir;
	}

	public void setFacing(int dir)
	{
		if (dir == 0)
			setFacing(Direction.UP);
		else if (dir == 1)
			setFacing(Direction.DOWN);
		else if (dir == 2)
			setFacing(Direction.LEFT);
		else if (dir == 3)
			setFacing(Direction.RIGHT);
	}

	public void setAnimation(String animation, int time, boolean halting, boolean looping)
	{
		this.animHalting = halting;
		this.animationLooping = looping;
		if (isHeroBacked) {
			if (animation.startsWith(AnimationConfiguration.getUnpromotedPrefix()))
				animation.replaceFirst(AnimationConfiguration.getUnpromotedPrefix(), "");
			else if (animation.startsWith(AnimationConfiguration.getPromotedPrefix()))
				animation.replaceFirst(AnimationConfiguration.getPromotedPrefix(), "");
			this.currentAnim = spriteAnims.getCharacterAnimation(animation, isHeroPromoted);
		}
		else
			this.currentAnim = spriteAnims.getAnimation(animation);

		if (currentAnim == null)
			throw new BadAnimationException("Unable to find animation: " + (isHeroBacked ? 
					(isHeroPromoted ? AnimationConfiguration.getPromotedPrefix() : AnimationConfiguration.getUnpromotedPrefix()) : "" ) + animation + " for cinematic actor");
		this.animDelta = 0;
		this.animUpdate = time / currentAnim.frames.size();
	}

	public void moveToLocation(int moveToLocX, int moveToLocY, float speed, boolean haltingMove, 
			int direction, boolean moveHorFirst, boolean moveDiag)
	{
		this.loopMoving = false;
		this.moveToLocX = moveToLocX;
		this.moveToLocY = moveToLocY;
		this.haltingMove = haltingMove;
		this.moveSpeed = speed;
		this.animDelta = 0;
		this.animUpdate = (long) jCinematicActor.getAnimSpeedForMoveSpeed(speed);
		this.moveHorFirst = moveHorFirst;
		this.moveDiag = moveDiag;
		if (direction != -1)
		{
			this.setFacing(direction);
			this.forceFacingMove = true;
		}
		else
			this.forceFacingMove = false;
		moving = true;
		movePath = null;
	}
	
	public void moveAlongPath(Path path, float speed, boolean haltingMove, StateInfo stateInfo)
	{
		if (path.getLength() > 1)
		{
			this.moving = true;
			this.haltingMove = haltingMove;
			this.movePathIndex = 1;
			this.movePath = path;
			this.animUpdate = (long) jCinematicActor.getAnimSpeedForMoveSpeed(speed);
			Step step = this.movePath.getStep(movePathIndex);
			this.moveToLocX = step.getX();
			this.moveToLocY = step.getY() - (this.sprite != null ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0);
			this.moveSpeed = speed;
		}
	}

	public void loopMoveToLocation(int moveToLocX, int moveToLocY, float speed)
	{
		this.startLoopX = this.locX;
		this.startLoopY = this.locY;
		moveToLocation(moveToLocX, moveToLocY, speed, haltingMove, -1, false, false);
		this.loopMoving = true;
		this.movePath = null;
	}

	public void stopLoopMove()
	{
		this.loopMoving = false;
	}

	public boolean isHaltingMove() {
		return haltingMove;
	}

	public boolean isAnimHalting() {
		return animHalting;
	}

	public float getLocX() {
		return locX;
	}

	public float getLocY() {
		return locY;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void spriteRemoved(StateInfo stateInfo)
	{
		if (sprite != null)
		{
			sprite.setLocX(locX, stateInfo.getTileWidth());
			sprite.setLocY(locY, stateInfo.getTileHeight());
			stateInfo.removeCombatSprite((CombatSprite) sprite);
		}
	}

	public AnimatedSprite resetSprite(StateInfo stateInfo)
	{
		if (sprite != null)
		{
			sprite.setLocX(locX, stateInfo.getTileWidth());
			sprite.setLocY(locY + stateInfo.getResourceManager().getMap().getTileRenderHeight(), stateInfo.getTileHeight());
			sprite.setFacing(facing);
			sprite.setVisible(true);
			if (sprite.getSpriteType() == Sprite.TYPE_NPC) {
				NPCSprite npc = (NPCSprite) sprite;
				npc.setMoving(false);
			}
		}
		return sprite;
	}

	@Override
	public int compareTo(CinematicActor otherActor) {
		return (int) (this.getLocY() - otherActor.getLocY());
	}
}
