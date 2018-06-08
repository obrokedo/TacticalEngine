package tactical.game.move;

import java.util.LinkedList;
import java.util.Queue;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.CombatSprite;
import tactical.map.Map.Stairs;

public class MovingSprite
{
	private AnimatedSprite animatedSprite;
	private int moveIndex;
	private Direction direction;
	private float endX, endY;
	private StateInfo stateInfo;
	private boolean isFirstMove = true;
	private int moveRemainder = 0;
	private Queue<Direction> nextMoveQueue = null;
	private Stairs stairs = null;
	private float yMovedForStairs;
	// public static int MOVE_SPEED = 11;
	public static int MOVE_SPEED = 220;
	public static int STAND_ANIMATION_SPEED = 10;
	public static int WALK_ANIMATION_SPEED = 4;

	public MovingSprite(AnimatedSprite combatSprite, Direction dir, Stairs stairs, StateInfo stateInfo) {
		super();
		this.animatedSprite = combatSprite;
		this.stateInfo = stateInfo;
		combatSprite.setAnimationUpdate(WALK_ANIMATION_SPEED);
		this.stairs = stairs;
		this.moveIndex = 0;
		this.direction = dir;
		switch (direction)
		{
			case LEFT:
				endX = animatedSprite.getLocX() - stateInfo.getTileWidth();
				endY = stairs.getYCoordByTileX(combatSprite.getTileX() - 1);
				break;
			case RIGHT:
				endX = animatedSprite.getLocX() + stateInfo.getTileWidth();
				endY = stairs.getYCoordByTileX(combatSprite.getTileX() + 1);
				break;
		}
		
		yMovedForStairs = endY - combatSprite.getLocY();
	}
	
	public MovingSprite(AnimatedSprite combatSprite, Direction dir, StateInfo stateInfo) {
		super();
		this.animatedSprite = combatSprite;
		this.stateInfo = stateInfo;
		combatSprite.setAnimationUpdate(WALK_ANIMATION_SPEED);
		initializeDirection(dir);
	}

	private void initializeDirection(Direction dir) {
		this.direction = dir;
		this.moveIndex = 0;
		switch (direction)
		{
			case UP:
				endX = animatedSprite.getLocX();
				endY = animatedSprite.getLocY() - stateInfo.getTileHeight();
				break;
			case DOWN:
				endX = animatedSprite.getLocX();
				endY = animatedSprite.getLocY() + stateInfo.getTileHeight();
				break;
			case LEFT:
				endX = animatedSprite.getLocX() - stateInfo.getTileWidth();
				endY = animatedSprite.getLocY();
				break;
			case RIGHT:
				endX = animatedSprite.getLocX() + stateInfo.getTileWidth();
				endY = animatedSprite.getLocY();
				break;
		}
	}

	public boolean update(int delta, boolean fastMove)
	{		
		int moveSpeed = MOVE_SPEED;
		if (stateInfo.isCombat() && (!((CombatSprite) animatedSprite).isHero() || fastMove))
			moveSpeed = (int) (moveSpeed / 1.5f);

		if (stateInfo.isCombat() && isFirstMove)
		{
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "step", 1f, false));
		}
		
		isFirstMove = false;

		moveIndex += delta;

		if (moveIndex >= moveSpeed)
		{
			moveRemainder = moveIndex - moveSpeed;
			moveIndex = moveSpeed;
			animatedSprite.setLocation(endX, endY, stateInfo.getTileWidth(), stateInfo.getTileHeight());
			animatedSprite.setFacing(direction);

			if (animatedSprite == stateInfo.getCurrentSprite())
				stateInfo.getCamera().centerOnSprite(animatedSprite, stateInfo.getCurrentMap());

			// Check to see if we have queued moves, if so start the next move
			if (nextMoveQueue != null && nextMoveQueue.size() > 0)
			{
				initializeDirection(nextMoveQueue.remove());
				this.updateWithRemainder(moveRemainder);
				return false;
			}

			animatedSprite.setAnimationUpdate(STAND_ANIMATION_SPEED);
			return true;
		}
		
		// When we're on the stairs then we may move some y direction even when pressing right or left
		
		float amountMoved = ((moveSpeed - moveIndex) / (moveSpeed * 1.0f) * stateInfo.getTileHeight());
		float yMovedOnStairs = ((moveSpeed - moveIndex) / (moveSpeed * 1.0f) * yMovedForStairs);
		
		switch (direction)
		{
			case UP:
				animatedSprite.setLocY(endY + amountMoved, stateInfo.getTileHeight());
						// 2 * CommRPG.GLOBAL_WORLD_SCALE[CommRPG.getGameInstance()]));
				break;
			case DOWN:
				animatedSprite.setLocY(endY - amountMoved, stateInfo.getTileHeight());
						// 2 * CommRPG.GLOBAL_WORLD_SCALE[CommRPG.getGameInstance()]));
				break;
			case LEFT:
				if (stairs != null)
					animatedSprite.setLocY(endY - yMovedOnStairs, stateInfo.getTileHeight());
				animatedSprite.setLocX(endX + amountMoved, stateInfo.getTileWidth());
						// 2 * CommRPG.GLOBAL_WORLD_SCALE[CommRPG.getGameInstance()]));
				break;
			case RIGHT:
				if (stairs != null)
					animatedSprite.setLocY(endY - yMovedOnStairs, stateInfo.getTileHeight());
				animatedSprite.setLocX(endX - amountMoved, stateInfo.getTileWidth());
						// 2 * CommRPG.GLOBAL_WORLD_SCALE[CommRPG.getGameInstance()]));
				break;
		}

		if (animatedSprite == stateInfo.getCurrentSprite()) {
			stateInfo.getCamera().centerOnSprite(animatedSprite, stateInfo.getCurrentMap());
		}

		return false;
	}

	public boolean isFirstMove() {
		return isFirstMove;
	}

	public boolean update(int delta)
	{
		return update(delta, false);
	}

	public boolean updateWithRemainder(int delta)
	{
		boolean retVal = update(delta, false);
		this.isFirstMove = true;
		this.moveRemainder = 0;
		return retVal;
	}

	public float getEndX() {
		return endX;
	}

	public float getEndY() {
		return endY;
	}

	public AnimatedSprite getAnimatedSprite() {
		return animatedSprite;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getMoveRemainder() {
		return moveRemainder;
	}

	public void addNextMovement(Direction dir) {
		if (nextMoveQueue == null)
			nextMoveQueue = new LinkedList<>();
		nextMoveQueue.add(dir);
	}
}
