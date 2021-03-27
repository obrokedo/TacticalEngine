package tactical.game.turnaction;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import tactical.engine.state.StateInfo;
import tactical.game.battle.BattleEffect;
import tactical.game.constants.Direction;
import tactical.game.manager.TurnManager;
import tactical.game.menu.SpeechMenu;

public class EndTurnAction extends TurnAction {

	public EndTurnAction() {
		super(TurnAction.ACTION_END_TURN);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		stateInfo.removeKeyboardListeners();
		// This moveable space is no longer needed to destroy it		
		if (turnManager.getCurrentSprite().getCurrentHP() > 0)
		{
			turnManager.getCurrentSprite().setFacing(Direction.DOWN);

			if (turnManager.getCurrentSprite().isHero())
			{
				stateInfo.checkTriggersMovement((int) turnManager.getCurrentSprite().getLocX(), (int) turnManager.getCurrentSprite().getLocY(), false);
			}

			// Make sure that the current sprite is still in the battle
			if (stateInfo.getCombatantById(turnManager.getCurrentSprite().getId()) != null && turnManager.getCurrentSprite().getBattleEffects().size() > 0)
			{
				String text = "";
				for (int i = 0; i < turnManager.getCurrentSprite().getBattleEffects().size(); i++)
				{
					String effectText = null;
					BattleEffect be = turnManager.getCurrentSprite().getBattleEffects().get(i);
					be.incrementTurn();
					 
					Log.debug("The battle effect: " + be.getBattleEffectId() + " was performed on " + turnManager.getCurrentSprite());
					effectText = be.getPerformEffectText(turnManager.getCurrentSprite());
					
					// If the sprite is still alive and the effect is done
					// then remove the effect and indicate such
					if (turnManager.getCurrentSprite().getCurrentHP() > 0 && be.isDone())
					{
						Log.debug("The battle effect: " + be.getBattleEffectId() + " has ended on " + turnManager.getCurrentSprite());
						turnManager.getCurrentSprite().getBattleEffects().remove(i--);
						be.effectEnded(turnManager.getCurrentSprite());
						if (effectText == null)
							effectText = be.effectEndedText(turnManager.getCurrentSprite());
						else
							effectText = effectText + "} " + be.effectEndedText(turnManager.getCurrentSprite());
						turnManager.getCurrentSprite().removeBattleEffect(be);
					}
					
					// If the current sprite is dead, there will be no more text added after this
					// so just end here
					if (turnManager.getCurrentSprite().getCurrentHP() <= 0) {
						if (effectText != null)
							text = text + effectText + "]";
						break;
					}
					
					if (effectText != null) {
						text = text + effectText;
						if (i + 1 == turnManager.getCurrentSprite().getBattleEffects().size())
							text += "]";
						else
							text += "} ";
					}
				}

				stateInfo.addMenu(new SpeechMenu(text, stateInfo));
				turnActions.add(new WaitForSpeechAction());
				turnActions.add(new PerformEffectsAction());
				return true;
			}
		}

		turnActions.add(new WaitForSpeechAction());
		turnActions.add(new CleanupAndStartNextAction());
		return true;
	}
}
