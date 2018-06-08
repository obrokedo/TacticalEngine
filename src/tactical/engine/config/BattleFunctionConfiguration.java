
package tactical.engine.config;

import java.util.Random;

import tactical.game.sprite.CombatSprite;

/**
 * Interface to call the BattleFunctions python methods that will determine
 * battle statistics and the text displayed in battle
 *
 * @see /scripts/BattleFunctions.py
 *
 * @author Broked
 *
 */
public interface BattleFunctionConfiguration {
	public int getDodgePercent(CombatSprite attacker, CombatSprite target);
	public int getCritPercent(CombatSprite attacker, CombatSprite target);
	public int getDoublePercent(CombatSprite attacker, CombatSprite target);
	public int getCounterPercent(CombatSprite attacker, CombatSprite target);
	public float getCounterDamageModifier(CombatSprite attacker, CombatSprite target);
	public float getCritDamageModifier(CombatSprite attacker, CombatSprite target);
	public int getDamageDealt(CombatSprite attacker, CombatSprite target, float landEffect, Random random);
	public String getDodgeText(CombatSprite attacker, CombatSprite target);
	public String getBlockText(CombatSprite attacker, CombatSprite target);
	public String getCriticalAttackText(CombatSprite attacker, CombatSprite target, int damage);
	public String getNormalAttackText(CombatSprite attacker, CombatSprite target, int damage);
	public String getCombatantDeathText(CombatSprite attacker, CombatSprite target);
	public int getExperienceGainedByDamage(int damage, int attackerLevel, CombatSprite target);
}
