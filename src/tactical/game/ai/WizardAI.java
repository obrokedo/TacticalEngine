package tactical.game.ai;

import java.awt.Point;
import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import tactical.engine.state.StateInfo;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.move.AttackableSpace;
import tactical.game.sprite.CombatSprite;

public class WizardAI extends CasterAI
{
	public WizardAI(int approachType) {
		super(approachType, false, 50f);
	}

	@Override
	protected void handleSpell(SpellDefinition spell,  KnownSpell knownSpell, int spellLevel, int tileWidth, int tileHeight, CombatSprite currentSprite,
			CombatSprite targetSprite, StateInfo stateInfo, int baseConfidence, int cost, Point attackPoint, int distance, AISpellConfidence aiSpellConf)
	{
		
	}

	@Override
	protected int determineBaseConfidence(CombatSprite currentSprite,
			CombatSprite targetSprite, int tileWidth, int tileHeight,
			Point attackPoint, StateInfo stateInfo, AIConfidence aiConf)
	{
		// Adding the targets counter attack causes us to flee to much
		// int damage = Math.max(1, targetSprite.getCurrentAttack() - currentSprite.getCurrentDefense());

		// Determine confidence, add 5 because the attacked sprite will probably always be in range
		int currentConfidence = 5;
		int nearbyAlly = getNearbySpriteAmount(stateInfo, currentSprite.isHero(), tileWidth, tileHeight, attackPoint, 2, currentSprite) * 5;
		int nearbyEnemy = getNearbySpriteAmount(stateInfo, !currentSprite.isHero(), tileWidth, tileHeight, attackPoint, 2, currentSprite) * 5;
		currentConfidence += nearbyAlly - nearbyEnemy;
		aiConf.allyInfluence = nearbyAlly;
		aiConf.enemyInfluence = nearbyEnemy;
		// Adding the attackers damage to this person causes us to flee way to much
		// - Math.min(20, (int)(20.0 * damage / currentSprite.getMaxHP()));
		return currentConfidence;
	}
}
