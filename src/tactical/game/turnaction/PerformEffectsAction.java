package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.battle.BattleEffect;
import tactical.game.manager.TurnManager;

public class PerformEffectsAction extends TurnAction {

	public PerformEffectsAction() {
		super(TurnAction.ACTION_PERFORM_EFFECTS);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		for (int i = 0; i < turnManager.getCurrentSprite().getBattleEffects().size(); i++)
		{
			BattleEffect be = turnManager.getCurrentSprite().getBattleEffects().get(i);
			be.performEffect(turnManager.getCurrentSprite());
			//TODO Add a new message saying the hero has died?
			if (turnManager.getCurrentSprite().getCurrentHP() <= 0) {
				// Wait for the "death spin"
				turnActions.add(new WaitAction(TurnManager.SPIN_TIME / TurnManager.UPDATE_TIME));
				turnActions.add(new CurrentSpriteDeathAction());
				break;
			}
			be.incrementTurn();
		}
		
		turnActions.add(new WaitForSpeechAction());
		turnActions.add(new CleanupAndStartNextAction());
		return true;
	}
	
	
	
}
