package tactical.game.battle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.config.BattleFunctionConfiguration;
import tactical.game.battle.command.BattleCommand;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.constants.TextSpecialCharacters;
import tactical.game.item.Item;
import tactical.game.item.ItemUse;
import tactical.game.resource.EnemyResource;
import tactical.game.sprite.CombatSprite;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;

public class BattleResults implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int ITEM_CHANCE_TO_BREAK = 15;
	public boolean countered, doubleAttack;
	public ArrayList<Boolean> dodged;
	public ArrayList<Boolean> critted;
	public ArrayList<Integer> hpDamage;
	public ArrayList<Integer> mpDamage;
	public ArrayList<Integer> remainingHP;
	public ArrayList<String> text;
	public List<CombatSprite> targets;
	public ArrayList<ArrayList<BattleEffect>> targetEffects;
	public BattleCommand battleCommand;
	public ArrayList<Integer> attackerHPDamage;
	public ArrayList<Integer> attackerMPDamage;
	public boolean death = false;
	public boolean attackerDeath = false;
	public String attackOverText = "";
	public LevelUpResult levelUpResult = null;
	public boolean itemDamaged = false;
	public Item itemUsed = null;
	public int goldGained;
	
	// TODO Retrieve dropped enemy items

	// TODO Effects

	public static BattleResults determineBattleResults(CombatSprite attacker,
			List<CombatSprite> targets, BattleCommand battleCommand, ResourceManager fcrm)
	{
        BattleFunctionConfiguration jBattleFunctions = TacticalGame.ENGINE_CONFIGURATIOR.getBattleFunctionConfiguration();

		BattleResults br = new BattleResults();
		br.battleCommand = battleCommand;
		br.targets = targets;
		br.hpDamage = new ArrayList<Integer>();
		br.mpDamage = new ArrayList<Integer>();
		br.text = new ArrayList<String>();
		br.targetEffects = new ArrayList<ArrayList<BattleEffect>>();
		br.attackerHPDamage = new ArrayList<Integer>();
		br.attackerMPDamage = new ArrayList<Integer>();
		br.remainingHP = new ArrayList<>();
		br.countered = false;
		br.dodged = new ArrayList<Boolean>();
		br.critted = new ArrayList<Boolean>();
		br.doubleAttack = false;

		SpellDefinition spell = null;
		ItemUse itemUse = null;
		int spellLevel = 0;
		String preventEffectName = null;

		for (BattleEffect effect : attacker.getBattleEffects()) {
			if ((battleCommand.getCommand() == BattleCommand.COMMAND_ITEM && effect.preventsItems()) ||
					(battleCommand.getCommand() == BattleCommand.COMMAND_ATTACK && effect.preventsAttack()) ||
					(battleCommand.getCommand() == BattleCommand.COMMAND_SPELL && effect.preventsSpells()))
			{
					battleCommand = new BattleCommand(BattleCommand.COMMAND_TURN_PREVENTED);
					preventEffectName = effect.getBattleEffectId();
					break;
			}
		}

		if (battleCommand.getCommand() == BattleCommand.COMMAND_TURN_PREVENTED) {
			br.text.add(attacker.getName() + " was unable to act due to the " + preventEffectName);
			// Remove all but one target as that's the most that will
			// be seen in the animation
			while (br.targets.size() > 1) {
				br.targets.remove(1);
			}
		}

		// Check to see if we're using an item
		if (battleCommand.getCommand() == BattleCommand.COMMAND_ITEM) {
			br.itemUsed = battleCommand.getItem();

			// If the item has a spell use get the spell
			// and check for single use
			if (br.itemUsed.getSpellUse() != null) {
				spell = br.itemUsed.getSpellUse().getSpell();
				spellLevel = br.itemUsed.getSpellUse().getLevel() - 1;
				battleCommand.setjSpell(spell);
				battleCommand.setLevel(spellLevel + 1);
				// Check to see if the item is single use, if so
				// then remove the item from the attacker
				if (br.itemUsed.getSpellUse().isSingleUse())
					attacker.removeItem(br.itemUsed);
			}
			// We're just using the 'item use', retrieve that and see if
			// this item was single use
			else {
				itemUse = br.itemUsed.getItemUse();

				if (itemUse.isSingleUse())
					attacker.removeItem(br.itemUsed);
			}

			// Check durability
			if (br.itemUsed.useDamagesItem() && TacticalGame.RANDOM.nextInt(100) <= ITEM_CHANCE_TO_BREAK) {
				br.itemUsed.damageItem();
				br.itemDamaged = true;
			}

		// Check to see if we're using a spell
		} else if (battleCommand.getCommand() == BattleCommand.COMMAND_SPELL) {
			spell = battleCommand.getSpell();
			spellLevel = battleCommand.getLevel() - 1;
		}

		int expGained = 0;
		br.goldGained = 0;

		int index = 0;
		for (int targetIndex = 0; targetIndex < targets.size(); targetIndex++)
		{
			CombatSprite target = targets.get(targetIndex);

			CommandResult commandResult = new CommandResult();

			// If we are doing a simple attack command then we need to get the dodge chance and calculate damage dealt
			if (battleCommand.getCommand() == BattleCommand.COMMAND_ATTACK)
			{
				handleAttackAction(attacker, fcrm, jBattleFunctions, br, target, commandResult);
			}
			// Check to see if the battle command indicates a spell is being used
			else if (spell != null)
			{
				handleSpellAction(attacker, br, spell, spellLevel, index, target, commandResult);
			}
			else if (itemUse != null)
			{
				handleItemAction(attacker, br, itemUse, target, commandResult);
			}
			
			expGained += commandResult.expGained;
			String text = commandResult.text;
			
			// Check to see if the target will die, if so peel off the special characters at the end
			// and add the combatant death text
			if (target.getCurrentHP() + br.hpDamage.get(index) <= 0 ||
					(br.battleCommand.getCommand() == BattleCommand.COMMAND_ATTACK && br.death && !br.attackerDeath))
			{
				text = addCombatantDeathText(attacker, target, text, br, jBattleFunctions);
				if (attacker.isHero() && !target.isHero())
					br.goldGained += EnemyResource.getGoldDroppedByName(target.getName());
			}
			// Check to see if the target will die, if so peel off the special characters at the end
			// and add the combatant death text
			else if (br.attackerDeath)
			{
				text = addCombatantDeathText(target, attacker, text, br, jBattleFunctions);
				if (!attacker.isHero() && target.isHero()) {
					br.goldGained += EnemyResource.getGoldDroppedByName(attacker.getName());
				}
			}
			br.text.add(text);
			index++;
		}
	
		// The maximum exp you can ever get is 49
		expGained = Math.min(49, expGained);

		// In optimize mode no exp should be gained
		if (TacticalGame.BATTLE_MODE_OPTIMIZE)
			expGained = 0;
		
		indicateGainedExp(attacker, targets, expGained, br, fcrm);

		if (br.goldGained > 0) {
			CombatSprite goldGainer;
			if (!attacker.isHero())		
				goldGainer = targets.get(0);
			else
				goldGainer = attacker;
			br.attackOverText += (br.attackOverText.length() > 0 ? " " : "") + goldGainer.getName() + " recieved " + 
				br.goldGained + " gold." + TextSpecialCharacters.CHAR_SOFT_STOP;
		}
		
		// If there is no attack over text, set it to null so nothing
		// is displayed in battle (null text isn't displayed)
		if (StringUtils.isEmpty(br.attackOverText))
			br.attackOverText = null;
	
		return br;
	}

	private static void indicateGainedExp(CombatSprite attacker, List<CombatSprite> targets, int expGained,
			BattleResults br, ResourceManager fcrm) {
		if (attacker.isHero())
		{
			if (!br.attackerDeath)
			{
				attacker.setExp(attacker.getExp() + expGained);
				br.attackOverText += attacker.getName() + " gained " + expGained +  " experience.}";
				// If the hero has leveled up then set the level up results and the correct text
				if (attacker.getExp() >= 100)
				{
					br.attackOverText += " " + TextSpecialCharacters.CHAR_NEXT_CIN + TextSpecialCharacters.CHAR_LINE_BREAK + " ";
					br.levelUpResult = attacker.getHeroProgression().getLevelUpResults(attacker);
					br.attackOverText += br.levelUpResult.text;
				}
			}
		}
		else if (expGained != 0)
		{
			targets.get(0).setExp(targets.get(0).getExp() + expGained);
			br.attackOverText += targets.get(0).getName() + " gained " + expGained +  " experience.}";
			if (targets.get(0).getExp() >= 100)
			{
				br.attackOverText += " " + TextSpecialCharacters.CHAR_NEXT_CIN + TextSpecialCharacters.CHAR_LINE_BREAK + " ";
				br.levelUpResult = targets.get(0).getHeroProgression().getLevelUpResults(targets.get(0));
				br.attackOverText += br.levelUpResult.text;
			}
		}
	}

	private static String addCombatantDeathText(CombatSprite killer, CombatSprite target,
			String text, BattleResults br, BattleFunctionConfiguration jBattleFunctions) {
		br.death = true;
		int idx = text.lastIndexOf(TextSpecialCharacters.CHAR_SOFT_STOP);
		if (idx != -1)
			text = text.substring(0, idx);

		idx = text.lastIndexOf(TextSpecialCharacters.CHAR_HARD_STOP);
		if (idx != -1)
			text = text.substring(0, idx);

		text = text.replaceAll(TextSpecialCharacters.CHAR_SOFT_STOP, "");
		text = text + " " +jBattleFunctions.getCombatantDeathText(killer, target);
		return text;
	}

	private static void handleItemAction(CombatSprite attacker, BattleResults br, ItemUse itemUse, CombatSprite target,
			CommandResult commandResult) {
		String text;
		int expGained = 0;
		text = itemUse.getBattleText(target.getName());

		int damage = 0;
		if (itemUse.getDamage() != 0)
		{
			damage = itemUse.getDamage();
			br.hpDamage.add(damage);
			br.remainingHP.add(target.getCurrentHP() + damage);
			
			int amountActuallyDone = Math.min(Math.max(0, target.getCurrentHP() + damage), target.getMaxHP()) - target.getCurrentHP();
			text = text.replaceAll(TextSpecialCharacters.REPLACE_VALUE, "" + amountActuallyDone);
		}
		else
		{
			br.hpDamage.add(0);
			br.remainingHP.add(target.getCurrentHP());
		}

		if (itemUse.getMpDamage() != 0)
			br.mpDamage.add(itemUse.getMpDamage());
		else
			br.mpDamage.add(0);

		ArrayList<BattleEffect> appliedEffects = new ArrayList<>();

		BattleEffect eff = null;
		if ((eff = itemUse.getEffects()) != null && eff.isEffected(target))
		{
			appliedEffects.add(eff);
			Log.debug(target.getName() + " was affected by " + eff.getBattleEffectId());
		}

		br.targetEffects.add(appliedEffects);

		for (BattleEffect effect : appliedEffects)
		{
			String effectText = effect.effectStartedText(attacker, target);
			if (effectText != null)
				text = text + " " + effectText;
		}

		text += TextSpecialCharacters.CHAR_SOFT_STOP;

		br.attackerHPDamage.add(0);
		br.attackerMPDamage.add(0);

		int exp = itemUse.getExpGained();

		if (attacker.isHero())
			expGained += exp;

		br.critted.add(false);
		br.dodged.add(false);
		commandResult.expGained = expGained;
		commandResult.text = text;
	}

	private static void handleSpellAction(CombatSprite attacker, BattleResults br, SpellDefinition spell, int spellLevel,
			int index, CombatSprite target, CommandResult commandResult) {
		int damage = 0;
		String text;
		int expGained = 0;

		if (spell.getDamage() != null)
		{
			damage = spell.getEffectiveDamage(attacker, target, spellLevel);
			br.hpDamage.add(damage);
			br.remainingHP.add(target.getCurrentHP() + damage);
		}
		else
		{
			br.hpDamage.add(0);
			br.remainingHP.add(target.getCurrentHP());
		}

		if (spell.getMpDamage() != null)
			br.mpDamage.add(spell.getMpDamage()[spellLevel]);
		else
			br.mpDamage.add(0);

		ArrayList<BattleEffect> appliedEffects = new ArrayList<>();
		
		// This spell will NOT kill the target so effects should still be applied
		if (target.getCurrentHP() + damage > 0)
		{
			// Check to see if a battle effect should be applied via this spell
			BattleEffect[] effs = null;
			if ((effs = spell.getEffects(attacker, spellLevel)) != null)
			{
				for (BattleEffect eff : effs)
				{
					if (eff.isEffected(target))
					{
						Log.debug(target.getName() + " was affected by " + eff.getBattleEffectId());
						appliedEffects.add(eff);
					}
				}
			}
		}

		br.targetEffects.add(appliedEffects);

		br.attackerHPDamage.add(0);
		if (index == 0)
			br.attackerMPDamage.add(-1 * spell.getCosts()[spellLevel]);
		else
			br.attackerMPDamage.add(0);

		text = spell.getBattleText(target, damage, br.mpDamage.get(br.mpDamage.size() - 1),
				br.attackerHPDamage.get(br.attackerHPDamage.size() - 1),
				br.attackerMPDamage.get(br.attackerMPDamage.size() - 1));

		// br.targetEffects.get(br.targetEffects.size() - 1)

		// If a battle effect was applied then append that to the battle text
		for (BattleEffect eff : appliedEffects) {
			String effectText = eff.effectStartedText(attacker, target);
			if (effectText != null)
			{
				if (text.length() > 0)
					text = text + "} " + effectText;
				else
					text = text + " " + effectText;
			}

		}
		int exp = spell.getExpGained(spellLevel, attacker, target);

		if (attacker.isHero())
			expGained += exp;
		br.critted.add(false);
		br.dodged.add(false);
		text = text + TextSpecialCharacters.CHAR_SOFT_STOP;
		commandResult.expGained = expGained;
		commandResult.text = text;
	}

	private static void handleAttackAction(CombatSprite attacker, ResourceManager fcrm,
			BattleFunctionConfiguration jBattleFunctions, BattleResults br, CombatSprite target, CommandResult commandResult) {
		int damage = 0;
		int sumDamage = 0;
		String text;
		int expGained = 0;

		// Normal Attack
		text = addAttack(attacker, target, br, fcrm, jBattleFunctions, false);
		damage = br.hpDamage.get(0);
		sumDamage = damage;
		br.remainingHP.add(target.getCurrentHP() + damage);

		if (attacker.isHero())
		{
			if (damage == 0)
				expGained += 1;
		}

		// Check to see if the target is dead, if so then there is nothing additional to do
		if (br.remainingHP.get(0) > 0)
		{
			int distanceApart = Math.abs(attacker.getTileX() - target.getTileX()) + Math.abs(attacker.getTileY() - target.getTileY());
			// Counter Attack
			if (distanceApart == 1 && target.getAttackRange().isInDistance(1) && 
					TacticalGame.testD100(jBattleFunctions.getCounterPercent(attacker, target), "counter"))
			{
				br.text.add(text);
				text = addAttack(target, attacker, br, fcrm, jBattleFunctions, true);
				damage = br.hpDamage.get(1);

				// Add the attackers remaining HP
				br.remainingHP.add(attacker.getCurrentHP() + damage);
				if (br.remainingHP.get(br.remainingHP.size() - 1) <= 0)
				{
					br.death = true;
					br.attackerDeath = true;
				}

				if (target.isHero())
				{
					if (damage != 0)
						expGained += getExperienceByDamage(damage, target, attacker);
					else
						expGained += 1;
				}
				else if (br.attackerDeath)
				{
					expGained = 0;
				}

				br.countered = true;
			}

			// Check to make sure the attacker is still alive
			if (br.remainingHP.size() == 1 || br.remainingHP.get(1) > 0)
			{
				// Double Attack
				if (TacticalGame.testD100(jBattleFunctions.getDoublePercent(attacker, target), "double"))
				{
					br.text.add(text);
					text = addAttack(attacker, target, br, fcrm, jBattleFunctions, false);
					damage = br.hpDamage.get(br.hpDamage.size() - 1);
					sumDamage += damage;

					if (damage == 0)
						expGained += 1;
					
					// Add the targets remaining HP
					br.remainingHP.add(br.remainingHP.get(0) + damage);
					if (br.remainingHP.get(br.remainingHP.size() - 1) <= 0)
						br.death = true;

					br.doubleAttack = true;
				}
			}
		}
		
		if (attacker.isHero() && sumDamage != 0)
		{
			expGained += getExperienceByDamage(sumDamage, attacker, target);
		}
		else if (br.attackerDeath)
			expGained = 0;
		
		commandResult.expGained = expGained;
		commandResult.text = text;
	}

	private static String addAttack(CombatSprite attacker, CombatSprite target, BattleResults br,
			ResourceManager fcrm, BattleFunctionConfiguration jBattleFunctions, boolean counter)
	{
		String text;

		// TODO This needs to take into effect other hitting modifiers.
		int dodgeChance = jBattleFunctions.getDodgePercent(attacker, target);

		if (TacticalGame.testD100(dodgeChance, "dodge"))
		{
			br.hpDamage.add(0);
			br.mpDamage.add(0);
			if (target.isDodges())
				text = jBattleFunctions.getDodgeText(attacker, target);
			else
				text = jBattleFunctions.getBlockText(attacker, target);
			br.targetEffects.add(new ArrayList<BattleEffect>());
			br.attackerHPDamage.add(0);
			br.attackerMPDamage.add(0);
			br.dodged.add(true);
			br.critted.add(false);
		}
		else
		{
			br.dodged.add(false);
			float landEffect = (100 + fcrm.getMap().getLandEffectByTile(target.getMovementType(),
					target.getTileX(), target.getTileY())) / 100.0f;

			boolean critted = false;
			if (TacticalGame.testD100(jBattleFunctions.getCritPercent(attacker, target), "crit"))
				critted = true;

			br.critted.add(critted);

			// Multiply the attackers attack by .8 - 1.2 and the targets defense by .8 - 1.2 and then the difference
			// between the two values is the damage dealt or 1 if result is less then 1.
			int damage = jBattleFunctions.getDamageDealt(attacker, target, landEffect, TacticalGame.RANDOM);

			if (counter)
				damage = Math.min(-1, (int) (damage * jBattleFunctions.getCounterDamageModifier(attacker, target)));

			if (critted)
			{
				int critDamage = Math.min(-1, (int) (damage * jBattleFunctions.getCritDamageModifier(attacker, target)));
				br.hpDamage.add(critDamage);
				text = jBattleFunctions.getCriticalAttackText(attacker, target, critDamage * -1);
			}
			else
			{
				br.hpDamage.add(damage);
				text = jBattleFunctions.getNormalAttackText(attacker, target, damage * -1);
			}

			br.mpDamage.add(0);

			ArrayList<BattleEffect> appliedEffects = new ArrayList<>();

			// This spell will NOT kill the target so effects should still be applied
			if (target.getCurrentHP() + damage > 0)
			{
				BattleEffect eff = null;
				if ((eff = attacker.getAttackEffect()) != null && eff.isEffected(target))
				{
					appliedEffects.add(eff);
					Log.debug(target.getName() + " was affected by " + eff.getBattleEffectId());
					String effectText = eff.effectStartedText(attacker, target);
					if (effectText != null)
						text = text + " " + effectText;
				}
			}

			br.targetEffects.add(appliedEffects);

			br.attackerHPDamage.add(0);
			br.attackerMPDamage.add(0);
		}

		text = text + TextSpecialCharacters.CHAR_SOFT_STOP;

		return text;
	}

	/*
	public static void main(String args[]) throws UnknownFunctionException, UnparsableExpressionException
	{

		System.out.println("% Dam  10 20 30 40 50 60 70 80 90");
		for (int i = 1; i < 9; i++)
		{
			System.out.print("LVL " + i + ": ");
			for (int j = 1; j < 10; j++)
			{
				System.out.print(getExperienceByDamage(j, i, 10, 10, 3) + " ");
			}
			System.out.println();
		}

	}

	private static String getExperienceByDamage(int damage, int attackerLevel, int targetHP, int targetMaxHP, int targetLevel)
	{
		int maxExp = Math.max(1, Math.min(49, (targetLevel - attackerLevel) * 7 + 35));
		// Check to see if we've killed the target
		if (targetHP + damage <= 0)
		{
			return getString(maxExp);
		}
		// Otherwise give experience based on damage dealt
		else
		{
			// Calculate the percent experience gained, this gives full "kill" experience any time you do 75% damage or more
			// and smaller amounts based on the percent of 75% health that you've done. This number can never exceed 1.
			double percentExperienceGained = Math.min(Math.abs(1.0 * damage / targetMaxHP) / .75, 1);
			return getString((int) Math.max(Math.max(1, 5 + targetLevel - attackerLevel), maxExp * percentExperienceGained));
		}
	}


	private static String getString(int i)
	{
		if (i < 10)
			return "0" + i;
		else
			return "" + i;
	}
	*/

	private static int getExperienceByDamage(int damage, CombatSprite attacker, CombatSprite target)
	{
		int attackerLevel = attacker.getLevel();
		if (attacker.isPromoted())
		{
			attackerLevel += 10;
		}
		return TacticalGame.ENGINE_CONFIGURATIOR.getBattleFunctionConfiguration().getExperienceGainedByDamage(damage, attackerLevel, target);
		/*
		int maxExp = Math.max(1, Math.min(49, (target.getLevel() - attackerLevel) * 7 + 35));
		// Check to see if we've killed the target
		if (target.getCurrentHP() + damage <= 0)
			return maxExp;
		// Otherwise give experience based on damage dealt
		else
		{
			// Calculate the percent experience gained, this gives full "kill" experience any time you do 75% damage or more
			// and smaller amounts based on the percent of 75% health that you've done. This number can never exceed 1.
			double percentExperienceGained = Math.min(Math.abs(1.0 * damage / target.getMaxHP()) / .75, 1);
			return (int) Math.max(Math.max(1, 5 + target.getLevel() - attackerLevel), maxExp * percentExperienceGained);
		}
		*/
	}

	public void initialize(ResourceManager fcrm)
	{
		battleCommand.initializeSpell(fcrm);
	}
	
	protected static class CommandResult {
		String text;
		int expGained;
	}
}