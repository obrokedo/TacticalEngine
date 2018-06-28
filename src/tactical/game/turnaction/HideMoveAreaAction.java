package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.manager.TurnManager;

public class HideMoveAreaAction extends TurnAction {

	public HideMoveAreaAction() {
		super(TurnAction.ACTION_HIDE_MOVE_AREA);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		turnManager.setDisplayMoveable(false);
		return true;
	}
}
