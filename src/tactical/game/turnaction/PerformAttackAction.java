package tactical.game.turnaction;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import tactical.engine.TacticalGame;
import tactical.engine.message.MessageType;
import tactical.engine.state.AttackCinematicState;
import tactical.engine.state.StateInfo;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.BattleResults;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;

public class PerformAttackAction extends TurnAction
{
	private static final long serialVersionUID = 1L;

	private BattleResults battleResults;

	public PerformAttackAction(BattleResults battleResults) {
		super (TurnAction.ACTION_PERFORM_ATTACK);
		this.battleResults = battleResults;
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		turnManager.setDisplayMoveable(false);
		stateInfo.removePanel(turnManager.getLandEffectPanel());
		stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
		stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
		AttackCinematicState acs = TacticalGame.ENGINE_CONFIGURATIOR.getAttackCinematicState();
		if (TacticalGame.BATTLE_MODE_OPTIMIZE || acs == null)
		{
			for (int i = 0; i < turnManager.getBattleResults().targets.size(); i++)
			{
				CombatSprite t = turnManager.getBattleResults().targets.get(i);
				t.modifyCurrentHP(turnManager.getBattleResults().hpDamage.get(i));
				t.modifyCurrentMP(turnManager.getBattleResults().mpDamage.get(i));
				turnManager.getCurrentSprite().modifyCurrentHP(turnManager.getBattleResults().attackerHPDamage.get(i));
				turnManager.getCurrentSprite().modifyCurrentMP(turnManager.getBattleResults().attackerMPDamage.get(i));
				if (turnManager.getBattleResults().targetEffects.get(i) != null)
					for (BattleEffect be : turnManager.getBattleResults().targetEffects.get(i))
					{
						// If the effect is already done then it is instantaneous so don't bother adding it to the target
						if (!be.isDone())
							t.addBattleEffect(be);
						be.effectStarted(turnManager.getCurrentSprite(), t);
					}
			}
			stateInfo.sendMessage(MessageType.RETURN_FROM_ATTACK_CIN);
			return true;
		}
		
		stateInfo.setShowAttackCinematic(true);
		acs.setBattleInfo(turnManager.getCurrentSprite(), stateInfo.getResourceManager(), turnManager.getBattleResults(), stateInfo.getPaddedGameContainer(), TacticalGame.STATE_GAME_BATTLE);
		stateInfo.getPersistentStateInfo().getGame().enterState(TacticalGame.STATE_GAME_BATTLE_ANIM, new FadeOutTransition(Color.black, 250), new EmptyTransition());
		return true;
	}

}
