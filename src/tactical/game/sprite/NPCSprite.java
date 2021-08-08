package tactical.game.sprite;

import tactical.engine.TacticalGame;
import tactical.engine.message.SpriteMoveMessage;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.text.Speech;

public class NPCSprite extends AnimatedSprite
{
	private static final long serialVersionUID = 1L;

	private int speechId;
	private int uniqueNPCId;
	private int moveCounter = 0;
	private int initialTileX = -1;
	private int initialTileY = -1;
	private int maxWander = 0;
	private boolean throughWall = false;
	private boolean animate = true;
	private boolean turnOnTalk = true;
	
	private boolean waitingForSpeechToEnd = false;
	private Direction originalFacing = null;

	public NPCSprite(String imageName,
			int speechId, int id, String name, boolean throughWall,
			boolean animate, boolean turnOnTalk)
	{
		super(0, 0, imageName, id);
		this.name = name;
		this.speechId = speechId;
		this.spriteType = Sprite.TYPE_NPC;
		this.uniqueNPCId = 0;
		this.throughWall = throughWall;
		this.animate = animate;
		this.turnOnTalk = turnOnTalk;
	}
	
	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public void setInitialPosition(int xLoc, int yLoc,
			int tileWidth, int tileHeight, int maxWander, Direction direction)
	{
		this.setLocation(xLoc, yLoc, tileWidth, tileHeight);
		if (direction != null)
			this.setFacing(direction);
		this.initialTileX = this.getTileX();
		this.initialTileY = this.getTileY();
		this.maxWander = maxWander;
	}

	public boolean triggerButton1Event(StateInfo stateInfo)
	{
		if (Speech.showFirstSpeechMeetsReqs(speechId, stateInfo, false)) {
			originalFacing = this.getFacing();
			if (turnOnTalk) {
				turnTowardsHero(stateInfo);
			}
			waitingForSpeechToEnd = true;
			return true;
		}
		return false;
	}

	private void turnTowardsHero(StateInfo stateInfo) {
		if (stateInfo.getCurrentSprite().getLocX() > this.getLocX())
			this.setFacing(Direction.RIGHT);
		else if (stateInfo.getCurrentSprite().getLocX() < this.getLocX())
			this.setFacing(Direction.LEFT);
		else if (stateInfo.getCurrentSprite().getLocY() > this.getLocY())
			this.setFacing(Direction.DOWN);
		else if (stateInfo.getCurrentSprite().getLocY() < this.getLocY())
			this.setFacing(Direction.UP);
	}

	boolean moving = false;

	@Override
	public void update(StateInfo stateInfo) {
		if (animate)
			super.update(stateInfo);
		
		if (!waitingForSpeechToEnd) {
			if (stateInfo.getCamera().isVisible(this)) {
				if (maxWander > 0)
					wanderMove(stateInfo);
			}
		}
	}

	private void wanderMove(StateInfo stateInfo)
	{
		if (!moving)
		{
			if (moveCounter >= 30)
			{
				Direction nextDir = null;
				int count = 0;
				while (nextDir == null && count <= 4)
				{
					count++;
					nextDir = Direction.values()[TacticalGame.RANDOM.nextInt(4)];
					switch (nextDir)
					{
						case UP:
							if (Math.abs(this.getTileX() - this.initialTileX) + Math.abs(this.getTileY() - 1 - this.initialTileY) > maxWander)
								nextDir = null;
							break;
						case DOWN:
							if (Math.abs(this.getTileX() - this.initialTileX) + Math.abs(this.getTileY() + 1 - this.initialTileY) > maxWander)
								nextDir = null;
							break;
						case LEFT:
							if (Math.abs(this.getTileX() - 1 - this.initialTileX) + Math.abs(this.getTileY() - this.initialTileY) > maxWander)
								nextDir = null;
							break;
						case RIGHT:
							if (Math.abs(this.getTileX() + 1 - this.initialTileX) + Math.abs(this.getTileY() - this.initialTileY) > maxWander)
								nextDir = null;
							break;
					}
				}

				if (nextDir != null)
				{
					moveCounter = 0;
					moving = true;
					stateInfo.sendMessage(new SpriteMoveMessage(this, nextDir), true);
				}
			}
			else
				moveCounter++;
		}
	}
	
	

	@Override
	public void setFacing(Direction dir) {
		super.setFacing(dir);
	}

	public int getUniqueNPCId() {
		return uniqueNPCId;
	}

	public void setUniqueNPCId(int uniqueNPCId) {
		this.uniqueNPCId = uniqueNPCId;
	}

	@Override
	public void doneMoving(StateInfo stateInfo) {
		super.doneMoving(stateInfo);
		moving = false;
		if (waitingForSpeechToEnd && turnOnTalk) {
			turnTowardsHero(stateInfo);
		}
	}

	public boolean isThroughWall() {
		return throughWall;
	}

	public boolean isWaitingForSpeechToEnd() {
		return waitingForSpeechToEnd;
	}

	public void setWaitingForSpeechToEnd(boolean waitingForSpeechToEnd) {
		this.waitingForSpeechToEnd = waitingForSpeechToEnd;
	}

	public Direction getOriginalFacing() {
		return originalFacing;
	}

	public void setOriginalFacing(Direction originalFacing) {
		this.originalFacing = originalFacing;
	}
}
