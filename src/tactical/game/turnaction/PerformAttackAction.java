package tactical.game.turnaction;

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
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo) {
		stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
		stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
		if (TacticalGame.BATTLE_MODE_OPTIMIZE)
		{
			for (int i = 0; i < battleResults.targets.size(); i++)
			{
				CombatSprite t = battleResults.targets.get(i);
				t.modifyCurrentHP(battleResults.hpDamage.get(i));
				t.modifyCurrentMP(battleResults.mpDamage.get(i));
				turnManager.getCurrentSprite().modifyCurrentHP(battleResults.attackerHPDamage.get(i));
				turnManager.getCurrentSprite().modifyCurrentMP(battleResults.attackerMPDamage.get(i));
				if (battleResults.targetEffects.get(i) != null)
					for (BattleEffect be : battleResults.targetEffects.get(i))
					{
						be.effectStarted(turnManager.getCurrentSprite(), t);
					}
			}
			stateInfo.sendMessage(MessageType.RETURN_FROM_ATTACK_CIN);
			return true;
		}
		
		stateInfo.setShowAttackCinematic(true);
		AttackCinematicState acs = TacticalGame.ENGINE_CONFIGURATIOR.getAttackCinematicState();
		acs.setBattleInfo(turnManager.getCurrentSprite(), stateInfo.getResourceManager(), battleResults, stateInfo.getPaddedGameContainer(), TacticalGame.STATE_GAME_BATTLE);
		stateInfo.getPersistentStateInfo().getGame().enterState(TacticalGame.STATE_GAME_BATTLE_ANIM, new FadeOutTransition(Color.black, 250), new EmptyTransition());
		return true;
	}

}
