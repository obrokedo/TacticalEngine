package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.manager.TurnManager;

public class MoveCursorToActorAction extends TurnAction {

	public MoveCursorToActorAction() {
		super(TurnAction.ACTION_MOVE_CURSOR_TO_ACTOR);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		turnManager.setDisplayOverCursor(true);

		if (turnManager.getCursor().getX() == turnManager.getCurrentSprite().getLocX() &&
			turnManager.getCursor().getY() == turnManager.getCurrentSprite().getLocY())
		{
			if (turnManager.isOwnsSprite())
			{
				stateInfo.addKeyboardListener(turnManager.getMoveableSpace());
			}
			turnManager.getLandEffectPanel().setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(turnManager.getCurrentSprite().getMovementType(),
					turnManager.getCurrentSprite().getTileX(), turnManager.getCurrentSprite().getTileY()));
			stateInfo.addSingleInstancePanel(turnManager.getLandEffectPanel());
			turnManager.setDisplayMoveable(true);
			// The display turnManager.getCursor() will toggled via the wait			

			stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
			turnManager.getCurrentSprite().triggerOverEvent(stateInfo);

			if (turnActions.size() == 1)
				turnManager.setDisplayCursor(false);
			return true;
		}

		// Move the turnManager.getCursor() back to the target sprite
		if (turnManager.getCursor().getX() < turnManager.getCurrentSprite().getLocX())
		{
			if (turnManager.getCurrentSprite().getLocX() - turnManager.getCursor().getX() > stateInfo.getTileWidth() / 4)
				turnManager.getCursor().setX(turnManager.getCursor().getX() + stateInfo.getTileWidth() / 4);
			else
				turnManager.getCursor().setX(turnManager.getCurrentSprite().getLocX());
		}
		else if (turnManager.getCursor().getX() > turnManager.getCurrentSprite().getLocX())
		{
			if (turnManager.getCursor().getX() - turnManager.getCurrentSprite().getLocX() > stateInfo.getTileWidth() / 4)
				turnManager.getCursor().setX(turnManager.getCursor().getX() - stateInfo.getTileWidth() / 4);
			else
				turnManager.getCursor().setX(turnManager.getCurrentSprite().getLocX());
		}
		if (turnManager.getCursor().getY() < turnManager.getCurrentSprite().getLocY())
		{
			if (turnManager.getCurrentSprite().getLocY() - turnManager.getCursor().getY() > stateInfo.getTileWidth() / 4)
				turnManager.getCursor().setY(turnManager.getCursor().getY() + stateInfo.getTileHeight() / 4);
			else
				turnManager.getCursor().setY(turnManager.getCurrentSprite().getLocY());
		}
		else if (turnManager.getCursor().getY() > turnManager.getCurrentSprite().getLocY())
		{
			if (turnManager.getCursor().getY() - turnManager.getCurrentSprite().getLocY() > stateInfo.getTileWidth() / 4)
				turnManager.getCursor().setY(turnManager.getCursor().getY() - stateInfo.getTileHeight() / 4);
			else
				turnManager.getCursor().setY(turnManager.getCurrentSprite().getLocY());
		}

		stateInfo.getCamera().centerOnPoint((int) turnManager.getCursor().getX(), (int) turnManager.getCursor().getY(), stateInfo.getCurrentMap());
		return false;
	}
}
