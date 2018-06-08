package tactical.game.turnaction;

import tactical.game.battle.command.BattleCommand;
import tactical.game.sprite.CombatSprite;

public class TargetSpriteAction extends TurnAction
{
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

	public BattleCommand getBattleCommand() {
		return battleCommand;
	}
}
