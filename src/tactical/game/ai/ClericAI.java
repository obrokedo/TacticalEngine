package tactical.game.ai;

import java.awt.Point;
import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import tactical.engine.state.StateInfo;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.move.AttackableSpace;
import tactical.game.sprite.CombatSprite;

public class ClericAI extends CasterAI
{
	public ClericAI(int approachType, int vision) {
		super(approachType, true, 30f, vision);
	}

	@Override
	protected void handleSpell(SpellDefinition spell, KnownSpell knownSpell, int i, int tileWidth, int tileHeight, CombatSprite currentSprite,
			CombatSprite targetSprite, StateInfo stateInfo, int baseConfidence, int cost, Point attackPoint, int distance, AISpellConfidence aiSpellConf)
	{
		handleHealingSpell(spell, knownSpell, i, tileWidth, tileHeight, currentSprite, 
				targetSprite, stateInfo, baseConfidence, cost, attackPoint, aiSpellConf);
	}

	// TODO Lots to do here, they aren't really smart enough to move and target them self, kind of need it's own AI for that?
	// Somehow there is never a time when aura can get 2 people in it
	private void handleHealingSpell(SpellDefinition spell, KnownSpell knownSpell, int spellLevel, int tileWidth, int tileHeight, CombatSprite currentSprite,
			CombatSprite targetSprite, StateInfo stateInfo, int baseConfidence, int cost, Point attackPoint, AISpellConfidence aiSpellConf)
	{
		boolean healSelf = false;
		if (1.0 * currentSprite.getCurrentHP() / currentSprite.getMaxHP() < .65)
			healSelf = true;


		int currentConfidence = 0;
		int area = spell.getArea()[spellLevel - 1];
		ArrayList<CombatSprite> targetsInArea;
		if (area > 1 || area == AttackableSpace.AREA_ALL_INDICATOR)
		{
			boolean healedSelf = false;

			// If there are multiple targets then get the total percent damage done and then divide it by the area amount
			// this will hopefully prevent casters from casting higher level spells then they need to
			Point castPoint = new Point(targetSprite.getTileX(), targetSprite.getTileY());
			if (area != AttackableSpace.AREA_ALL_INDICATOR) {
				targetsInArea = getNearbySprites(stateInfo, (currentSprite.isHero() ? !spell.isTargetsEnemy() : spell.isTargetsEnemy()),
						tileWidth, tileHeight,
						castPoint, spell.getArea()[spellLevel - 1] - 1, currentSprite);
			// If this is area all then just add all of the correct targets
			} else {
				targetsInArea = new ArrayList<>();
				boolean targetHero = false;
				if (spell.isTargetsEnemy())
					targetHero = !currentSprite.isHero();
				for (CombatSprite cs : stateInfo.getCombatSprites())
				{
					if (targetHero == cs.isHero())
					{
						targetsInArea.add(cs);
					}
				}
			}

			// Check to see if the point that the healer would move to would be in the radius of the spell. Make sure
			// we don't add the hero multiple times though. If this is a spell that hits everyone
			// then don't bother checking
			if (targetSprite != currentSprite && spell.getArea()[spellLevel - 1] != AttackableSpace.AREA_ALL_INDICATOR)
			{
				if (Math.abs(castPoint.x - attackPoint.x) + Math.abs(castPoint.y - attackPoint.y) <= spell.getArea()[spellLevel - 1] - 1)
				{
					targetsInArea.add(currentSprite);
					healedSelf = true;
				}
			}
			else
				healedSelf = true;

			if (targetsInArea.size() > 1)
				Log.debug("There are multiple targets in cleric range " + targetsInArea.size());

			int incidentalHealed = 0;

			// We only want to consider enemies that will be healed for 75% of the healing (prevents healing for 1)
			// the healing would provide or are at less then 50% health
			for (CombatSprite ts : targetsInArea)
			{
				int effectiveDamage = spell.getEffectiveDamage(currentSprite, ts, spellLevel - 1);
				int maxDamage = spell.getDamage()[spellLevel - 1];
				// Check to see if the character is at less then 50% health or if the spell would use at least 75% of it's healing power
				if (effectiveDamage != 0 && (ts.getCurrentHP() * 1.0 / ts.getMaxHP() < .5
						|| (ts.getMaxHP() - ts.getCurrentHP()) / (1.0 * maxDamage) > .75))
				{
					// Get the percent of the max health that the spell can heal for and the percent of damage that
					// the target is hurt for and the take the smaller of the two numbers. This prevents spells that
					// can technically heal for a higher percent of max health getting a higher value (causing low
					// level heal spells never to be used)
					float healing = Math.min(ts.getMaxHP() - ts.getCurrentHP(), effectiveDamage) * 1.0f;
					int healingConf = Math.min(50, (int)(50.0 *					
							healing / targetSprite.getMaxHP()));					
					currentConfidence += healingConf;
				}
				//TODO ADD A CHECK TO SEE IF IT WILL CURE A CONDITION
				// If this target isn't hurt but will be in the spell area we just add a small amount of confidence,
				// this won't be added unless at least one person in the radius needs healing
				else
					incidentalHealed++;
			}
			
			aiSpellConf.damageInfluence = currentConfidence;

			// Only add the base confidence if we have found someone to heal
			if (currentConfidence > 0)
			{
				currentConfidence += incidentalHealed * 2;
				// TODO Should this be divided by the area?
				// currentConfidence /= area;
				// If this action will end up healing the cleric as well,
				// then add an additional 20 points
				if (healedSelf && healSelf) {
					currentConfidence += 20;
					aiSpellConf.selfHealBonus = 20;
				}
				currentConfidence += baseConfidence;
			}
		}
		// Single target healing
		else
		{
			int effectiveDamage = spell.getEffectiveDamage(currentSprite, targetSprite, spellLevel - 1);
			int maxDamage = spell.getDamage()[spellLevel - 1];
			Log.debug("Check healing: Effective Healing: " + effectiveDamage + " Percent left " + (targetSprite.getCurrentHP() * 1.0 / targetSprite.getMaxHP()) +
					" Amount of heal power: " + (targetSprite.getMaxHP() - targetSprite.getCurrentHP()) / (1.0 * maxDamage));
			if (targetSprite.getCurrentHP() * 1.0 / targetSprite.getMaxHP() < .5 ||
					(targetSprite.getMaxHP() - targetSprite.getCurrentHP()) /
						(1.0 * maxDamage) > .75)
			{
				// Get the actual amount that can be healed (can't heal more then max)
				// Get the percent of the max health that the spell can heal for and the percent of damage that
				// the target is hurt for and the take the smaller of the two numbers. This prevents spells that
				// can technically heal for a higher percent of max health getting a higher value (causing low
				// level heal spells never to be used)
				float healing = Math.min(targetSprite.getMaxHP() - targetSprite.getCurrentHP(), effectiveDamage) * 1.0f;
				int healingConf = Math.min(50, (int)(50.0 *					
						healing / targetSprite.getMaxHP()));
				aiSpellConf.damageInfluence = healingConf;
				currentConfidence += healingConf;

				if (targetSprite == currentSprite && healSelf) {
					currentConfidence += 20;
					aiSpellConf.selfHealBonus = 20;
				}

				// Only add the base confidence if we have found someone to heal
				currentConfidence += baseConfidence;
			}
			else
				currentConfidence = 0;
			targetsInArea = null;
		}

		// Subtract the mp cost of the spell
		currentConfidence -= cost;
		
		aiSpellConf.mpCost = cost;

		Log.debug(" Cleric Spell confidence " + currentConfidence + " name " +
		targetSprite.getName() + " " + targetSprite.getUniqueEnemyId() + " spell " + spell.getName() + " level " + spellLevel);

		// Check to see if this is the most confident
		mostConfident = checkForMaxConfidence(mostConfident, currentConfidence, spell, knownSpell, spellLevel, targetsInArea, false, true);
	}

	@Override
	protected int determineBaseConfidence(CombatSprite currentSprite,
			CombatSprite targetSprite, int tileWidth, int tileHeight,
			Point attackPoint, StateInfo stateInfo, AIConfidence aiConf)
	{
		/*
		int damage = 0;
		if (targetSprite.isHero())
			damage = Math.max(1, targetSprite.getCurrentAttack() - currentSprite.getCurrentDefense());
			*/

		// Determine confidence, add 5 because the attacked sprite will probably always be in range
		int currentConfidence = 5;
		int nearbyAlly = getNearbySpriteAmount(stateInfo, currentSprite.isHero(), tileWidth, tileHeight, attackPoint, 2, currentSprite) * 5;
		int nearbyEnemy = getNearbySpriteAmount(stateInfo, !currentSprite.isHero(), tileWidth, tileHeight, attackPoint, 2, currentSprite) * 5;
		currentConfidence += nearbyAlly - nearbyEnemy;
		aiConf.allyInfluence = nearbyAlly;
		aiConf.enemyInfluence = nearbyEnemy;
		// Adding the attackers damage to this person causes us to flee way to much
 		// -Math.min(20, (int)(20.0 * damage / currentSprite.getMaxHP()));
		return currentConfidence;
	}
}
