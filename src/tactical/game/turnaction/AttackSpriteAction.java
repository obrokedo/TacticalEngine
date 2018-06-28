package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.BattleResultsMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.battle.BattleResults;
import tactical.game.battle.command.BattleCommand;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;

public class AttackSpriteAction extends TurnAction
{
	private static final long serialVersionUID = 1L;

	private ArrayList<Integer> targets;
	private BattleCommand battleCommand;

	public AttackSpriteAction(CombatSprite target, BattleCommand battleCommand) {
		super(TurnAction.ACTION_ATTACK_SPRITE);
		this.battleCommand = battleCommand;
		targets = new ArrayList<Integer>();
		targets.add(target.getId());
	}

	public AttackSpriteAction(ArrayList<CombatSprite> targets, BattleCommand battleCommand) {
		super (TurnAction.ACTION_ATTACK_SPRITE);
		this.battleCommand = battleCommand;
		this.targets = new ArrayList<Integer>();
		for (CombatSprite cs : targets)
			this.targets.add(cs.getId());
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		if (!turnManager.getCurrentSprite().isHero())
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		ArrayList<CombatSprite> targArray = new ArrayList<>();
		for (CombatSprite cs : stateInfo.getCombatSprites())
		{
			for (Integer targ : targets)
			{
				if (cs.getId() == targ)
				{
					targArray.add(cs);
					break;
				}
			}
		}
		BattleResults br = BattleResults.determineBattleResults(turnManager.getCurrentSprite(),
				targArray, battleCommand, stateInfo.getResourceManager());
		stateInfo.getClientProfile().modifyGold(br.goldGained);
		stateInfo.sendMessage(new BattleResultsMessage(br), true);
		turnManager.setDisplayAttackable(false);
		return true;
	}

	public BattleCommand getBattleCommand() {
		return battleCommand;
	}
}
