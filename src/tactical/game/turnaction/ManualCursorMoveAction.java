package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;

public class ManualCursorMoveAction extends TurnAction {

	public ManualCursorMoveAction() {
		super(TurnAction.ACTION_MANUAL_MOVE_CURSOR);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		// Get any combat sprite at the cursors location
		if (stateInfo.getCombatSpriteAtMapLocation(turnManager.getCursorTargetX(), turnManager.getCursorTargetY(), null) != null)
		{
			turnManager.setDisplayOverCursor(false);
		}


		if (turnManager.getCursor().getX() < turnManager.getCursorTargetX())
			turnManager.getCursor().setX(turnManager.getCursor().getX() + stateInfo.getTileWidth() / 6);
		else if (turnManager.getCursor().getX() > turnManager.getCursorTargetX())
			turnManager.getCursor().setX(turnManager.getCursor().getX() - stateInfo.getTileWidth() / 6);

		if (turnManager.getCursor().getY() < turnManager.getCursorTargetY())
			turnManager.getCursor().setY(turnManager.getCursor().getY() + stateInfo.getTileHeight() / 6);
		else if (turnManager.getCursor().getY() > turnManager.getCursorTargetY())
			turnManager.getCursor().setY(turnManager.getCursor().getY() - stateInfo.getTileHeight() / 6);

		stateInfo.getCamera().centerOnPoint((int) turnManager.getCursor().getX(), (int) turnManager.getCursor().getY(), stateInfo.getCurrentMap());

		if (turnManager.getCursorTargetX() == turnManager.getCursor().getX() && turnManager.getCursorTargetY() == turnManager.getCursor().getY())
		{
			// Get any combat sprite at the cursors location
			CombatSprite cs = stateInfo.getCombatSpriteAtMapLocation((int) turnManager.getCursor().getX(), (int) turnManager.getCursor().getY(), null);

			// if there is a combat sprite here display it's health panel
			if (cs != null)
			{
				stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
				cs.triggerOverEvent(stateInfo);
				turnManager.getLandEffectPanel().setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(cs.getMovementType(),
						cs.getTileX(), cs.getTileY()));
				stateInfo.removePanel(turnManager.getLandEffectPanel());
				stateInfo.addSingleInstancePanel(turnManager.getLandEffectPanel());
			}
			else
			{
				turnManager.setDisplayOverCursor(true);
				stateInfo.removePanel(turnManager.getLandEffectPanel());
				// Remove any health bar panels that may have been displayed from a sprite that we were previously over
				stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
			}
			return true;
		}
		return false;
	}
}
