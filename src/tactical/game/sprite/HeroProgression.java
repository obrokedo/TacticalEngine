package tactical.game.sprite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.config.LevelProgressionConfiguration;
import tactical.game.battle.LevelUpResult;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.exception.BadResourceException;
import tactical.game.item.EquippableItem;
import tactical.game.resource.SpellResource;

public class HeroProgression implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int STAT_NONE = 0;
	public static final int STAT_WEAK = 1;
	public static final int STAT_AVERAGE = 2;
	public static final int STAT_STRONG = 3;
	public static final int STAT_VERY_STRONG = 4;

	private Progression unpromotedProgression;
	private Progression promotedProgression;
	private List<Progression> specialProgressions;
	private int heroID;

	public HeroProgression(Progression unpromotedProgression, Progression promotedProgression, List<Progression> specialProgressions,
			int heroID) {
		super();
		this.unpromotedProgression = unpromotedProgression;
		this.promotedProgression = promotedProgression;
		this.specialProgressions = specialProgressions;
		this.heroID = heroID;
	}

	public void promote(CombatSprite cs, Progression progression)
	{
		if (progression == promotedProgression)
			cs.setPromoted(true, 0);
		else 
			cs.setPromoted(true, specialProgressions.indexOf(progression) + 1);
		
		// Remove equipped items because we are going to be setting new stats
		// explicitly and don't want to lose the bonuses from the equipped items
		EquippableItem weapon = cs.getEquippedWeapon();
		EquippableItem ring = cs.getEquippedRing();
		
		if (weapon != null)
			cs.unequipItem(weapon);
		
		if (ring != null)
			cs.unequipItem(ring);
		
		// Explicitly set values for the promoted class
		cs.setMaxMove(cs.getCurrentProgression().getMove());
		cs.setMovementType(cs.getCurrentProgression().getMovementType());
		cs.setNonRandomStats();
		
		// Re-equip items
		if (weapon != null && cs.isEquippable(weapon))
			cs.equipItem(weapon);
		if (ring != null && cs.isEquippable(ring))
			cs.equipItem(ring);
	}

	public LevelUpResult getLevelUpResults(CombatSprite cs) {
		return getLevelUpResults(cs, null);
	}
	
	//TODO This needs to make sure it uses all values from the LevelProgression script
	public LevelUpResult getLevelUpResults(CombatSprite cs, List<String> statChanges)
	{
		Progression p = cs.getCurrentProgression();
		String text = cs.getName() + " has reached level " + (cs.getLevel() + 1) + "!}[";
		LevelUpResult level = new LevelUpResult();

		addStatChangeLog(System.lineSeparator() + cs.getName() + " has reached " + (cs.isPromoted() ? "Promoted" : "Unpromoted") + " level " + (cs.getLevel() + 1), statChanges);
		addStatChangeLog(System.lineSeparator() + "Updating HP", statChanges);
		level.hitpointGain = getStatIncrease(p.getHp(), cs.isPromoted(), 
				cs.getMaxHP(), cs.getLevel() + 1, statChanges);
		if (level.hitpointGain > 0)
			text += " HP increased by " + level.hitpointGain + ".}[";

		addStatChangeLog(System.lineSeparator() + "Updating MP", statChanges);
		level.magicpointGain = getStatIncrease(p.getMp(), cs.isPromoted(), 
				cs.getMaxMP(), cs.getLevel() + 1, statChanges);
		if (level.magicpointGain > 0)
			text += " MP increased by " + level.magicpointGain + ".}[";

		addStatChangeLog(System.lineSeparator() + "Updating Attack", statChanges);
		level.attackGain = getStatIncrease(p.getAttack(), cs.isPromoted(), 
				cs.getMaxAttack(), cs.getLevel() + 1, statChanges);
		if (level.attackGain > 0)
			text += " Attack increased by " + level.attackGain + ".}[";

		addStatChangeLog(System.lineSeparator() + "Updating Defense", statChanges);
		level.defenseGain = getStatIncrease(p.getDefense(), cs.isPromoted(), 
				cs.getMaxDefense(), cs.getLevel() + 1, statChanges);
		if (level.defenseGain > 0)
			text += " Defense increased by " + level.defenseGain + ".}[";

		addStatChangeLog(System.lineSeparator() + "Updating Speed", statChanges);
		level.speedGain = getStatIncrease(p.getSpeed(), cs.isPromoted(), 
				cs.getMaxSpeed(), cs.getLevel() + 1, statChanges);
		if (level.speedGain > 0)
			text += " Speed increased by " + level.speedGain + ".}[";
		
		addStatChangeLog("HP: " + (cs.getMaxHP() + level.hitpointGain), statChanges);
		addStatChangeLog("MP: " + (cs.getMaxMP() + level.magicpointGain), statChanges);
		addStatChangeLog("Attack: " + (cs.getMaxAttack() + level.attackGain), statChanges);
		addStatChangeLog("Defense: " + (cs.getMaxDefense() + level.defenseGain), statChanges);
		addStatChangeLog("Speed: " + (cs.getMaxSpeed() + level.speedGain), statChanges);
		

		ArrayList<int[]> spellLevels = p.getSpellLevelLearned();
		ArrayList<String> spellIds = p.getSpellIds();
		for (int i = 0; i < spellLevels.size(); i++)
		{
			for (int j = 0; j < spellLevels.get(i).length; j++)
			{
				if (spellLevels.get(i)[j] == (cs.getLevel() + 1))
				{
					Log.debug(cs.getName() + " will learn " + spellIds.get(i));
					addStatChangeLog("Will learn spell: " + spellIds.get(i), statChanges);
					
					SpellDefinition spell = SpellResource.getSpell(spellIds.get(i));
					text += " " + cs.getName() + " learned " + spell.getName() + " " + (j + 1) + "}[";
					
				}
			}
		}

		level.text = text;

		return level;
	}
	
	private static void addStatChangeLog(String log, List<String> statChange) {
		if (statChange != null)
			statChange.add(log);
	}
	
	public void levelUp(CombatSprite cs, LevelUpResult level) {
		levelUp(cs, level, null);
	}

	public void levelUp(CombatSprite cs, LevelUpResult level, List<String> statChanges)
	{
		Log.debug("Applying level-up for " + cs.getName() + " Level: " + (cs.getLevel() + 1));
		
		cs.setLevel(cs.getLevel() + 1);
		cs.setExp(cs.getExp() - 100);

		cs.setCurrentAttack(cs.getCurrentAttack() + level.attackGain);
		cs.setMaxAttack(cs.getMaxAttack() + level.attackGain);

		cs.setCurrentDefense(cs.getCurrentDefense() + level.defenseGain);
		cs.setMaxDefense(cs.getMaxDefense() + level.defenseGain);

		cs.setCurrentSpeed(cs.getCurrentSpeed() + level.speedGain);
		cs.setMaxSpeed(cs.getMaxSpeed() + level.speedGain);

		cs.setMaxHP(cs.getMaxHP() + level.hitpointGain);

		cs.setMaxMP(cs.getMaxMP() + level.magicpointGain);
		
		ArrayList<int[]> spellLevels = cs.getCurrentProgression().getSpellLevelLearned();
		ArrayList<String> spellIds = cs.getCurrentProgression().getSpellIds();
		for (int i = 0; i < spellLevels.size(); i++)
		{
			for (int j = 0; j < spellLevels.get(i).length; j++)
			{
				if (spellLevels.get(i)[j] == cs.getLevel())
				{
					SpellDefinition spell = SpellResource.getSpell(spellIds.get(i));					

					boolean found = false;
					if (cs.getSpellsDescriptors() != null)
					{
						for (KnownSpell sd : cs.getSpellsDescriptors())
						{
							if (sd.getSpellId().equalsIgnoreCase(spell.getId()))
							{								
								sd.setMaxLevel((byte) (j + 1));
								cs.newSpellLearned(sd);
								addStatChangeLog(spell.getId() + " has been updated to level " + sd.getMaxLevel(), statChanges);								
								found = true;
								break;
							}
						}

						if (!found)
						{
							cs.getSpellsDescriptors().add(new KnownSpell((byte) (j + 1), SpellResource.getSpell(spell.getId())));
						}
					}
					else
					{
						cs.setSpells(new ArrayList<KnownSpell>());
						cs.getSpellsDescriptors().add(new KnownSpell((byte) (j + 1), SpellResource.getSpell(spell.getId())));
					}
				}
			}
		}
		
		addStatChangeLog(cs.dumpAffinitiesToString(), statChanges);
		
		// Level up non-displayed stats
		cs.levelUpCustomStatistics();
	}

	public static void main(String args[])
	{
		TacticalGame.ENGINE_CONFIGURATIOR.initialize();

		int val = 30;
		int max = 0;

		for (int j = 0; j < 10; j++)
		{
			val = 30;
			for (int i = 2; i < 30; i++)
			{
				// System.out.println("New Level ---- " + i);
				float gain = getStatIncrease(new Object[]{"4", 30, 90}, true, val, i, null);
				val += (int) gain;
			}
		}

		if (val > max)
		{
			max = val;
		}

	}

	private static int getStatIncrease(Object[] stat, boolean isPromoted, int currentStat,
			int newLevel, List<String> statChanges)
	{
		LevelProgressionConfiguration jlp = TacticalGame.ENGINE_CONFIGURATIOR.getLevelProgression();
		float[] values = jlp.getProgressArray((String) stat[0], isPromoted);

		if (values == null)
			throw new BadResourceException("No value was found for the progression type " + ((String) stat[0]) +
					" check LevelProgression.py to make sure an array is returned for the given value");

		float percentDone = 0;
		for (int i = 1; i < newLevel; i++)
			percentDone += values[i];

		percentDone /= 100;
		addStatChangeLog("Percent through progression: " + percentDone, statChanges);

		int amountToGainTotal = (((int) stat[2]) - ((int) stat[1]));
		addStatChangeLog("Amount to gain during whole progression: " + amountToGainTotal, statChanges);
		int amountToGainNowInt = 0;
		
		// If the hero is not supposed to ever gain any of the stat then 
		// the amount they should gain now is always 0
		if (amountToGainTotal != 0)
		{
			amountToGainNowInt = calculateStatIncrease(stat, isPromoted, currentStat, newLevel, statChanges, values,
					percentDone, amountToGainTotal);
		}

		addStatChangeLog("Final value to gain: " + amountToGainNowInt, statChanges);
		
		return amountToGainNowInt;
	}

	private static int calculateStatIncrease(Object[] stat, boolean isPromoted, int currentStat, int newLevel,
			List<String> statChanges, float[] values, float percentDone, int amountToGainTotal) {
		int amountToGainNowInt;
		int averageValue = (int) ((amountToGainTotal * percentDone) + // The amount that should have been gained at minimum
				((int) stat[1]) + // The base stat
				newLevel / 2); // Assume 50% bonus hp

		// If promoted then assume 1.5 bonus for each of the "big" levels
		if (isPromoted)
			averageValue += (int) ((1 + (newLevel - 1) / 6) * 1.5);

		addStatChangeLog("Average stat value for this level: " + averageValue, statChanges);

		float amountToGainNow = (values[newLevel - 1] / 100) * amountToGainTotal;
		
		addStatChangeLog("Base amount to gain: " + amountToGainNow, statChanges);

		if (!isPromoted || newLevel % 6 != 0)
		{
			// Add a value 0 - 1
			// amountToGainNow += CommRPG.RANDOM.nextInt(2);
			amountToGainNow += TacticalGame.RANDOM.nextFloat();
			addStatChangeLog("Amount to gain after adding a value 0 to 1: " + amountToGainNow, statChanges);
		}
		// BIG LEVEL!
		else
		{
			// Add a value 0 - 3
			amountToGainNow += TacticalGame.RANDOM.nextFloat() * 3;
			addStatChangeLog("Amount to gain after adding a BIG LEVEL value 0-3: " + amountToGainNow, statChanges);
		}

		// amountToGainNowInt = Math.round(amountToGainNow);
		amountToGainNowInt = (int) Math.floor(amountToGainNow);

		// Only give pity upgrades if you are unpromoted and under level 11 or
		// if you are promoted and under level 25
		if ((!isPromoted && newLevel <= 10) || (isPromoted && newLevel <= 25))
		{
			// Check for pity upgrades
			if (currentStat + amountToGainNowInt < averageValue)
			{
				amountToGainNowInt++;
				addStatChangeLog("Added a pity value because we are below average: " + (amountToGainNow + 1), statChanges);
				// System.out.println("Pity @ " + newLevel);
			}
		}
		return amountToGainNowInt;
	}

	public Progression getUnpromotedProgression() {
		return unpromotedProgression;
	}

	public Progression getPromotedProgression() {
		return promotedProgression;
	}

	public List<Progression> getSpecialProgressions() {
		return specialProgressions;
	}

	public int getHeroID() {
		return heroID;
	}	
}
