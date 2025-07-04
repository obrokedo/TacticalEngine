package tactical.game.dev;

import java.awt.Point;

import tactical.engine.state.StateInfo;
import tactical.game.ai.AIConfidence;
import tactical.game.ai.ClericAI;
import tactical.game.sprite.CombatSprite;

public class DevHeroAI extends ClericAI {
	private static final long serialVersionUID = 1L;

	public DevHeroAI(int approachType) {
		super(approachType, Integer.MAX_VALUE);
	}
	
	@Override
	protected int determineBaseConfidence(CombatSprite currentSprite,
			CombatSprite targetSprite,
			Point attackPoint, StateInfo stateInfo, AIConfidence aiConf)
	{
		/*
		int damage = 0;
		if (targetSprite.isHero())
			damage = Math.max(1, targetSprite.getCurrentAttack() - currentSprite.getCurrentDefense());
			*/

		// Determine confidence, add 5 because the attacked sprite will probably always be in range
		int currentConfidence = 5 +
				getNearbySpriteAmount(stateInfo, currentSprite.isHero(), attackPoint, 3, currentSprite) * 50 -
				getNearbySpriteAmount(stateInfo, !currentSprite.isHero(), attackPoint, 2, currentSprite) * 5;
				// Adding the attackers damage to this person causes us to flee way to much
		 		// -Math.min(20, (int)(20.0 * damage / currentSprite.getMaxHP()));
		return currentConfidence;
	}
}
