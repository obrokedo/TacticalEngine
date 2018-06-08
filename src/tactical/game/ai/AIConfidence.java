package tactical.game.ai;

import java.awt.Point;

import tactical.game.sprite.CombatSprite;
import tactical.game.turnaction.AttackSpriteAction;

public class AIConfidence {
	public int confidence;
	public boolean willKill = false;
	public boolean willHeal = false;
	public boolean foundHero = false;
	public CombatSprite target = null;
	public Point attackPoint = null;
	public AttackSpriteAction potentialAttackSpriteAction = null;

	public AIConfidence(int confidence) {
		super();
		this.confidence = confidence;
	}

	@Override
	public String toString()
	{
		return confidence + " " + willKill + " " + willHeal + " " + foundHero + " " + attackPoint + " " + target + " " + (potentialAttackSpriteAction == null ? "NO ACTION" : potentialAttackSpriteAction.action);
	}
}
