package tactical.game.sprite;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.constants.Direction;
import tactical.game.exception.BadResourceException;
import tactical.game.move.MovingSprite;
import tactical.loading.ResourceManager;
import tactical.utils.AnimSprite;
import tactical.utils.Animation;
import tactical.utils.SpriteAnims;

public class AnimatedSprite extends Sprite
{
	private static final long serialVersionUID = 1L;

	public final transient static Color DEFAULT_SHADOW_COLOR = new Color(0, 0, 0, 120);
	public static Color SHADOW_COLOR = new Color(0, 0, 0, 120);
	public static int SHADOW_OFFSET = 13;
	public static final int DEFAULT_SHADOW_OFFSET = 13;

	protected transient int imageIndex;
	protected transient int animationDelay = 0;
	protected transient SpriteAnims spriteAnims;

	protected transient Animation currentAnim;
	protected String imageName;
	protected transient Direction facing;
	private int animationUpdate = 10;
	protected transient int alpha = 255;

	public AnimatedSprite(int locX, int locY, String imageName, int id) {
		super(locX, locY, id);
		this.imageName = imageName;
	}

	public Image getCurrentImage()
	{
		return spriteAnims.getImageAtIndex(currentAnim.frames.get(imageIndex).sprites.get(0).imageIndex);
	}

	@Override
	public void render(Camera camera, Graphics graphics, GameContainer cont, int tileHeight) {
		float xPos = this.getLocX() - camera.getLocationX();
		float yPos = this.getLocY() - camera.getLocationY() - tileHeight / 3;
		
		for (AnimSprite as : currentAnim.frames.get(imageIndex).sprites)
		{
			AnimatedSprite.drawShadow(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos, camera, true, tileHeight);

			if (as.flipH)
				graphics.drawImage(spriteAnims.getImageAtIndex(as.imageIndex).getFlippedCopy(true, false), xPos, yPos);
			else
				graphics.drawImage(spriteAnims.getImageAtIndex(as.imageIndex), xPos, yPos);
		}
	}

	public static void drawShadow(Image originalIm, float locX, float locY, Camera camera, boolean tileOffset, int tileHeight)
	{
		Image i = (originalIm).getScaledCopy(originalIm.getWidth(), (int) (originalIm.getHeight() * .65));
		i.drawSheared((float) (locX - SHADOW_OFFSET * (1.0 * originalIm.getHeight() / tileHeight)),
				locY + originalIm.getHeight() - i.getHeight(),
				(int) (SHADOW_OFFSET * (1.0 * originalIm.getHeight() / tileHeight)),
				0, SHADOW_COLOR);
	}

	@Override
	public void initializeSprite(ResourceManager fcrm) {
		super.initializeSprite(fcrm);

		imageIndex = 0;
		spriteAnims = fcrm.getSpriteAnimation(imageName);
		if (spriteAnims == null)
			throw new BadResourceException("Unable to initialize sprite " + this.name +".\n"
					+ "Associated animation file " + imageName + ".anim could not be found.\n"
							+ "Check the animationsheets folder to make sure an animation by that name exists.\n"
							+ "Keep in mind that animation file names ARE case sensitive.");
		if (!(this instanceof CombatSprite) && !(this instanceof NPCSprite))
				currentAnim = spriteAnims.getAnimation("Down");
		facing = Direction.DOWN;
		animationUpdate = MovingSprite.STAND_ANIMATION_SPEED;
	}

	@Override
	public void update(StateInfo stateInfo)
	{
		animationDelay++;
		if (animationDelay >= animationUpdate)
		{

			if (imageIndex % 2 == 1)
				imageIndex--;
			else
				imageIndex++;

			animationDelay = 0;
		}
	}

	@Override
	public void setLocX(float locX, int tileWidth) {
		// Moving right
		if (locX > this.getLocX())
			setFacing(Direction.RIGHT);
		// Moving left
		else if (locX < this.getLocX())
			setFacing(Direction.LEFT);
		super.setLocX(locX, tileWidth);
	}

	@Override
	public void setLocY(float locY, int tileHeight) {
		// Moving down
		if (locY > this.getLocY())
			setFacing(Direction.DOWN);
		// Moving up
		else if (locY < this.getLocY())
			setFacing(Direction.UP);
		super.setLocY(locY, tileHeight);
	}

	public void setFacing(Direction dir)
	{
		if (facing != dir) {
			switch (dir)
			{
				case UP:
					currentAnim = spriteAnims.getCharacterAnimation("Up", false);
					break;
				case DOWN:
					currentAnim = spriteAnims.getCharacterAnimation("Down", false);
					break;
				case LEFT:
					currentAnim = spriteAnims.getCharacterAnimation("Left", false);
					break;
				case RIGHT:
					currentAnim = spriteAnims.getCharacterAnimation("Right", false);
					break;
			}
		}
		facing = dir;
	}

	public void setSpriteAnims(SpriteAnims spriteAnims) {
		this.spriteAnims = spriteAnims;
	}

	/**
	 * Sets the location of the sprite
	 *
	 * @param locX
	 * @param locY
	 */
	public void setLocation(float locX, float locY, int tileWidth, int tileHeight)
	{
		setLocX(locX, tileWidth);
		setLocY(locY, tileHeight);
	}

	public Direction getFacing() {
		return facing;
	}

	public void setAnimationUpdate(int animationUpdate) {
		this.animationUpdate = animationUpdate;
	}

	public SpriteAnims getSpriteAnims() {
		return spriteAnims;
	}

	public Animation getCurrentAnim() {
		return currentAnim;
	}

	public void doneMoving(StateInfo stateInfo) {

	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
}
