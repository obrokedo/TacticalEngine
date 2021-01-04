package tactical.game.ai;

import java.awt.Point;
import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import tactical.engine.state.StateInfo;
import tactical.game.Range;
import tactical.game.battle.command.BattleCommand;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.item.EquippableItem;
import tactical.game.move.AttackableSpace;
import tactical.game.sprite.CombatSprite;
import tactical.game.turnaction.AttackSpriteAction;

public abstract class CasterAI extends AI
{
	protected boolean willKill;
	protected boolean willHeal;
	protected int mostConfident = 0;
	protected SpellDefinition bestSpell;
	protected KnownSpell bestKnownSpell;
	protected int spellLevel;
	protected ArrayList<CombatSprite> targets;
	protected float maxKillConfidenceAmt;

	public CasterAI(int approachType, boolean canHeal, float maxKillConfidenceAmt, int vision) {
		super(approachType, canHeal, vision);
		this.maxKillConfidenceAmt = maxKillConfidenceAmt;
	}

	public void initialize(CombatSprite puppet)
	{
		super.initialize(puppet);
		mostConfident = Integer.MIN_VALUE;
		bestSpell = null;
		spellLevel = 0;
		targets = null;
		willKill = false;
		willHeal = false;
	}

	@Override
	protected AIConfidence getConfidence(CombatSprite currentSprite,
			CombatSprite targetSprite, int tileWidth, int tileHeight,
			Point attackPoint, int distance, StateInfo stateInfo)
	{
		bestSpell = null;
		mostConfident = 0;
		bestKnownSpell = null;
		
		boolean couldAttackTarget = false;
		AIConfidence aiC = new AIConfidence(mostConfident);
		int baseConfidence = determineBaseConfidence(currentSprite, targetSprite, 
				tileWidth, tileHeight, attackPoint, stateInfo, aiC);
		Log.debug("Base Caster confidence " + baseConfidence + " name " + targetSprite.getName());
		int currentConfidence = baseConfidence;

		Range attackRange = currentSprite.getAttackRange();

		// Check to make sure that if we're using a ranged weapon that has spots that it cannot target in the range that the enemy is not in one of those spaces
		// Get the wizards basic attack confidence, but make sure that the current sprite is in basic attack range
		if (attackRange.isInDistance(distance) && targetSprite.isHero() != currentSprite.isHero())
		{
			couldAttackTarget = true;
			int damage = Math.max(1, currentSprite.getCurrentAttack() - targetSprite.getCurrentDefense());
			int damageInfluence = Math.min(30, (int)(30.0 * damage / targetSprite.getMaxHP()));
			currentConfidence += damageInfluence;
			aiC.damageInfluence = damageInfluence;

			// If this attack would kill the target then add 50 confidence
			if (targetSprite.getCurrentHP() <= damage)
			{
				currentConfidence += 50;
				willKill = true;
			}

			Log.debug("Caster Attack confidence " + currentConfidence + " name " + targetSprite.getName());

			// This will always be most confident provided that the confidence is greater then 0
			mostConfident = checkForMaxConfidence(mostConfident, currentConfidence, null, null, 0, null, willKill, false);
		}


		this.checkSpells(currentSprite, targetSprite, tileWidth, tileHeight, attackPoint, distance, stateInfo, baseConfidence, aiC);
		aiC.confidence = mostConfident;
		aiC.willKill = willKill;
		aiC.willHeal = willHeal;

		// If we're not healing (good effecting) the target and it's the same
		// as you hero/enemy-wise then just return negative
		if (!willHeal && targetSprite.isHero() == currentSprite.isHero()) {
			AIConfidence minConf = new AIConfidence(Integer.MIN_VALUE);
			minConf.aiSpellConfs = aiC.aiSpellConfs;
		}

		// If we're not casting a spell and couldn't reach the target then
		// we can't actually attack this person. In this case just
		// return the minimum value
		if (this.bestSpell == null && !couldAttackTarget) {
			AIConfidence minConf = new AIConfidence(Integer.MIN_VALUE);
			minConf.aiSpellConfs = aiC.aiSpellConfs;
			return minConf;
		}

		return aiC;
	}

	protected void checkSpells(CombatSprite currentSprite,
			CombatSprite targetSprite, int tileWidth, int tileHeight,
			Point attackPoint, int distance, StateInfo stateInfo, int baseConfidence, AIConfidence aiConf)
	{
		/**********************************************************/
		/* Check each of the spells to see if they should be cast */
		/**********************************************************/
		if (currentSprite.getSpellsDescriptors() != null)
		{
			ArrayList<AISpellConfidence> spellConfs = new ArrayList<>();
			for (KnownSpell sd : currentSprite.getSpellsDescriptors())
			{
				SpellDefinition spell = sd.getSpell();

				for (int i = 1; i <= sd.getMaxLevel(); i++)
				{
					int cost = spell.getCosts()[i - 1];

					// If we don't have enough MP to cast the spell then don't consider this spell
					if (cost > currentSprite.getCurrentMP())
						continue;

					// Make sure the target is the correct type for this spell
					if ((targetSprite.isHero() == currentSprite.isHero()) == spell.isTargetsEnemy())
						continue;

					// Check to see if the target is in range of this spell
					if (!spell.getRange()[i - 1].isInDistance(distance) &&
							(spell.isTargetsEnemy() || currentSprite != targetSprite))
						continue;					

					Log.debug(sd.getSpellId() + " level " + i + " can be cast, checking it now.");
					AISpellConfidence aisc = new AISpellConfidence(spell.getName(), i);
					if (spell.getDamage() != null && spell.getDamage().length >= spellLevel && spell.getDamage()[0] < 0)
					{
						handleDamagingSpell(spell, sd, i, tileWidth, tileHeight, currentSprite,
								targetSprite, stateInfo, baseConfidence, cost, attackPoint, distance, aisc);
					} else {
						handleSpell(spell, sd, i, tileWidth, tileHeight, currentSprite,
							targetSprite, stateInfo, baseConfidence, cost, attackPoint, distance, aisc);
					}
					spellConfs.add(aisc);
				}
			}
			aiConf.aiSpellConfs = spellConfs;
		}
	}
	
	protected void handleDamagingSpell(SpellDefinition spell,  KnownSpell knownSpell, int spellLevel, 
			int tileWidth, int tileHeight, CombatSprite currentSprite,
			CombatSprite targetSprite, StateInfo stateInfo, int baseConfidence, int cost, 
			Point attackPoint, int distance, AISpellConfidence aiSpellConf)
	{
		boolean willKill = false;
		int currentConfidence = 0;
		int area = spell.getArea()[spellLevel - 1];
		ArrayList<CombatSprite> targetsInArea;
		if (area > 1 || area == AttackableSpace.AREA_ALL_INDICATOR)
		{
			int killed = 0;

			// If there are multiple targets then get the total percent damage done and then divide it by the area amount
			// this will hopefully prevent wizards from casting higher level spells then they need to
			if (area != AttackableSpace.AREA_ALL_INDICATOR) {
			targetsInArea = getNearbySprites(stateInfo, (currentSprite.isHero() ? !spell.isTargetsEnemy() : spell.isTargetsEnemy()),
					tileWidth, tileHeight,
					new Point(targetSprite.getTileX(), targetSprite.getTileY()), spell.getArea()[spellLevel - 1] - 1,
						currentSprite);
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

			for (CombatSprite ts : targetsInArea)
			{
				if (ts.getCurrentHP() + spell.getEffectiveDamage(currentSprite, ts, spellLevel - 1) <= 0)
				{
					killed++;
					willKill = true;
				}
				else
				{
					// TODO WHY ARE WE USING THEIR MAX HEALTH HERE? PERCENTAGE OF CURRENT HEALTH IS SUFFICIENT WITH
					// A MAX PERCENT OF -1. OTHERWISE WE WILL ALMOST ALWAYS USE HIGHER LEVEL SPELLS
					// ALSO, WHY DO WE HAVE CURRENT CONFIDENCE BE MAXED TO -50? IT SHOULD BE MINNED TO 0
					currentConfidence += Math.min(maxKillConfidenceAmt, (int)(-maxKillConfidenceAmt * spell.getEffectiveDamage(currentSprite, ts, spellLevel - 1) / ts.getMaxHP()));
				}
			}

			if (area != AttackableSpace.AREA_ALL_INDICATOR) {
				// currentConfidence /= area;
				aiSpellConf.targetAmtDivisor = area;
			}
			else {
				// currentConfidence /= targetsInArea.size();
				aiSpellConf.targetAmtDivisor = targetsInArea.size();
			}

			// Add a confidence equal to the amount killed + 50
			currentConfidence += killed * maxKillConfidenceAmt;
		}
		// Only a single target
		else
		{
			if (targetSprite.getCurrentHP() + spell.getEffectiveDamage(currentSprite, targetSprite, spellLevel - 1) <= 0)
			{
				currentConfidence += maxKillConfidenceAmt;
				willKill = true;
			}
			else
				currentConfidence += Math.min(maxKillConfidenceAmt, (int)(-maxKillConfidenceAmt * spell.getEffectiveDamage(currentSprite, targetSprite, spellLevel - 1) / targetSprite.getMaxHP()));
			targetsInArea = null;
			aiSpellConf.targetAmtDivisor = 1;
		}

		aiSpellConf.damageInfluence = currentConfidence;
		
		currentConfidence += baseConfidence;
		
		// Maximize distance from target
		currentConfidence += distance - 1;
		aiSpellConf.distanceFromTarget = distance - 1;

		// Subtract the mp cost of the spell
		currentConfidence -= cost;
		aiSpellConf.mpCost = -cost;

		Log.debug("Caster Spell confidence " + currentConfidence + " name " + targetSprite.getName() + " spell " + spell.getName() + " level " + spellLevel);

		// Check to see if this is the most confident
		mostConfident = checkForMaxConfidence(mostConfident, currentConfidence, spell, knownSpell, spellLevel, targetsInArea, willKill, false);
	}

	/**
	 * 
	 * @param mostConfident
	 * @param confidence
	 * @param currentSpell
	 * @param currentKnownSpell
	 * @param level
	 * @param targets any additional targets that would be included in the area
	 * @param willKill
	 * @param willHeal
	 * @return
	 */
	protected int checkForMaxConfidence(int mostConfident, int confidence, SpellDefinition currentSpell, KnownSpell currentKnownSpell,
			int level, ArrayList<CombatSprite> targets, boolean willKill, boolean willHeal)
	{
		if (confidence > mostConfident)
		{
			Log.debug("Spell " + (currentSpell == null ? "NONE" : currentSpell.getName() + " level " + level) + " has a higher confidence then " + 
					(bestSpell == null ? "NONE" : bestSpell.getName() + " level " + this.spellLevel) + 
						confidence + " vs " + mostConfident);
			bestSpell = currentSpell;
			bestKnownSpell = currentKnownSpell;
			this.spellLevel = level;
			this.targets = targets;
			this.willKill = willKill;
			this.willHeal = willHeal;
			return confidence;
		}
		return mostConfident;
	}

	@Override
	protected int getMaxRange(CombatSprite currentSprite) {
		// Check for weapons that have a range, but areas in the range they cannot hit
		int range = currentSprite.getAttackRange().getMaxRange();
		if (range == EquippableItem.RANGE_BOW_2_NO_1)
			range = 2;
		else if (range == EquippableItem.RANGE_BOW_3_NO_1 || range == EquippableItem.RANGE_BOW_3_NO_1_OR_2)
			range = 3;

		// Get the largest spell range
		if (currentSprite.getSpellsDescriptors() != null)
		{
			for (KnownSpell sd : currentSprite.getSpellsDescriptors())
			{
				SpellDefinition spell = sd.getSpell();

				for (int i = 1; i <= sd.getMaxLevel(); i++)
				{
					range = Math.max(spell.getRange()[i - 1].getMaxRange(), range);
				}
			}
		}

		return range;
	}

	@Override
	protected AttackSpriteAction getPerformedTurnAction(CombatSprite target) {
		if (bestSpell == null)
		{
			Log.debug("Attack is the preferred action");
			return new AttackSpriteAction(target,
				new BattleCommand(BattleCommand.COMMAND_ATTACK));
		}
		else
		{
			Log.debug("Cast spell " + bestSpell.getName() + " " + spellLevel + " is the preferred action");
			if (targets != null)
				return new AttackSpriteAction(targets, new BattleCommand(BattleCommand.COMMAND_SPELL, bestSpell, bestKnownSpell, spellLevel));
			else
				return new AttackSpriteAction(target, new BattleCommand(BattleCommand.COMMAND_SPELL, bestSpell, bestKnownSpell, spellLevel));
		}
	}

	protected abstract void handleSpell(SpellDefinition spell,  KnownSpell knownSpell, int i, int tileWidth, int tileHeight, CombatSprite currentSprite,
			CombatSprite targetSprite, StateInfo stateInfo, int baseConfidence, int cost, Point attackPoint, int distance, AISpellConfidence aiSpellConf);

	protected abstract int determineBaseConfidence(CombatSprite currentSprite,
			CombatSprite targetSprite, int tileWidth, int tileHeight,
			Point attackPoint, StateInfo stateInfo, AIConfidence aiConf);

	@Override
	protected int getLandEffectWeight(int landEffect) {
		return landEffect / 6;
	}


}
