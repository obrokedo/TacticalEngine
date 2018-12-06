package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.manager.TurnManager;
import tactical.game.move.MovingSprite;

public class MoveToTurnAction extends TurnAction
{
	private static final long serialVersionUID = 1L;
	
	public int locX;
	public int locY;
	
	public MoveToTurnAction(int locX, int locY) {
		super(TurnAction.ACTION_MOVE_TO);
		this.locX = locX;
		this.locY = locY;
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		if (turnManager.getMovingSprite() == null)
		{
			Direction dir = Direction.UP;

			if (locX > turnManager.getCurrentSprite().getLocX())
				dir = Direction.RIGHT;
			else if (locX < turnManager.getCurrentSprite().getLocX())
				dir = Direction.LEFT;
			else if (locY > turnManager.getCurrentSprite().getLocY())
				dir = Direction.DOWN;
			else if (locY < turnManager.getCurrentSprite().getLocY())
				dir = Direction.UP;

			// This check catches enemies movement being ended and heroes location being "reset"
			if (locX == turnManager.getCurrentSprite().getLocX() && locY == turnManager.getCurrentSprite().getLocY())
			{
				turnActions.remove(0);
				if (!turnManager.isResetSpriteLoc())
					turnManager.getMoveableSpace().setCheckEvents(true);
				if (turnActions.size() == 0)
				{
					// turnManager.getMoveableSpace().setCheckEvents(true);
					if (turnManager.isResetSpriteLoc())
					{
						turnManager.setResetSpriteLoc(false);
						turnManager.getCurrentSprite().setFacing(Direction.DOWN);
						// If we are already reset then switch to turnManager.getCursor() mode
						turnManager.setToCursorMode();
					}
				}
				turnManager.getLandEffectPanel().setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(turnManager.getCurrentSprite().getMovementType(),
						turnManager.getCurrentSprite().getTileX(), turnManager.getCurrentSprite().getTileY()));
				turnManager.setMovingSprite(null);
				return false;
			}


			turnManager.setMovingSprite(new MovingSprite(turnManager.getCurrentSprite(), dir, stateInfo));
		}

		// Check to see if we have arrived at our destination, if so
		// then we just remove this action and allow input for the moveablespace
		if (turnManager.getMovingSprite().update(delta, turnManager.isResetSpriteLoc()))
		{			
			// If this a sprite resetting location then we don't end our movement
			// until they are actually at the start
			if (!turnManager.isResetSpriteLoc() || (turnManager.getSpriteStartPoint().getX() == turnManager.getCurrentSprite().getTileX() &&
				turnManager.getSpriteStartPoint().getY() == turnManager.getCurrentSprite().getTileY()))
			{
				turnManager.getMoveableSpace().setCheckEvents(true);
				// turnManager.getMoveableSpace().handleKeyboardInput(stateInfo.getInput(), stateInfo);
				if (turnManager.isResetSpriteLoc())
					stateInfo.getInput().clear();
			}

			int movingRemainder = turnManager.getMovingSprite().getMoveRemainder();
			turnManager.setMovingSprite(null);

			if (turnActions.size() == 1)
			{

				if (turnManager.isResetSpriteLoc())
				{
					turnManager.setResetSpriteLoc(false);
					turnManager.getCurrentSprite().setFacing(Direction.DOWN);
					// If we are already reset then switch to turnManager.getCursor() mode
					turnManager.setToCursorMode();
				}
			}
			else if (turnActions.get(1).action == TurnAction.ACTION_MOVE_TO) {
				turnActions.remove(0);
				turnActions.get(0).perform(movingRemainder, turnManager, stateInfo, turnActions);
				return false;				
			}

			turnManager.getLandEffectPanel().setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(turnManager.getCurrentSprite().getMovementType(),
					turnManager.getCurrentSprite().getTileX(), turnManager.getCurrentSprite().getTileY()));
			return true;
		}
		return false;
	}
}
