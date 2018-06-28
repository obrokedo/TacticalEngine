package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.battle.command.BattleCommand;
import tactical.game.manager.TurnManager;
import tactical.game.sprite.CombatSprite;

public class TargetSpriteAction extends TurnAction {
	private static final long serialVersionUID = 1L;

	private BattleCommand battleCommand;
	private int targetSprite;

	public TargetSpriteAction(BattleCommand battleCommand, CombatSprite targetSprite) {
		super(TurnAction.ACTION_TARGET_SPRITE);
		this.battleCommand = battleCommand;
		this.targetSprite = targetSprite.getId();
	}

	public CombatSprite getTargetSprite(Iterable<CombatSprite> combatSprites) {
		for (CombatSprite cs : combatSprites)
			if (cs.getId() == targetSprite)
				return cs;
		return null;
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		turnManager.setBattleCommand(battleCommand);
		turnManager.determineAttackableSpace(false);
		turnManager.getAttackableSpace().setTargetSprite(getTargetSprite(stateInfo.getCombatSprites()), stateInfo);
		turnManager.setDisplayAttackable(true);
		return true;
	}
}
