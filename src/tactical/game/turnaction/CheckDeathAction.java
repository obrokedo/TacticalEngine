package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;

public class CheckDeathAction extends TurnAction {

	public CheckDeathAction() {
		super(TurnAction.ACTION_CHECK_DEATH);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		if (turnManager.getBattleResults().death)
		{
			for (CombatSprite cs : stateInfo.getCombatSprites()) {
				if (cs.getCurrentHP() <= 0) {							
					turnActions.add(0, new WaitAction(0));
					return false;
				}
			}
		}
		turnActions.add(new EndTurnAction());
		return true;
	}
}
