package tactical.game.battle.spell;

import java.io.Serializable;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.config.ParticleEmitterConfiguration;
import tactical.game.Range;
import tactical.game.battle.BattleEffect;
import tactical.game.sprite.CombatSprite;

/**
 * Interface to call the Spells python methods that will determine
 * spell statistics, experience gained by using a spell and the text
 * to display when using a spell
 *
 * @see /scripts/Spells.py
 *
 * @author Broked
 *
 */
public abstract class SpellDefinition implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Spell attributes
	 */
	protected String name;
	protected int[] costs;
	protected boolean targetsEnemy, targetsAllies;
	protected int maxLevel;
	protected int[] damage;
	protected int[] mpDamage;
	protected String[][] effects;
	protected int[][] effectLevel;
	protected Range[] range;
	protected int[] area;
	protected String id;
	protected transient Image spellIcon;
	protected boolean loops = false;
	protected int spellIconIndex = 0;

	public SpellDefinition() {
		
	}
	
	/**
	 * Gets the spell's name
	 *
	 * @return a string representing the spell's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets an array of integers where a value at a given index
	 * represents the cost for the spell of level = index + 1
	 *
	 * @return an array of integers where a value at a given index
	 * represents the cost for the spell of level = index + 1
	 */
	public int[] getCosts() {
		return costs;
	}

	/**
	 * Gets a boolean indicating whether this spell is used to target
	 * "enemies". The term "enemy" is relative to the person casting the spell.
	 * A value of true indicates that the spell does target "enemies"
	 *
	 * @return a boolean indicating whether this spell is used to target
	 * "enemies"
	 */
	public boolean isTargetsEnemy() {
		return targetsEnemy;
	}

	/**
	 * Gets an integer representing the max level of this spell. This should NOT
	 * be confused with the current spell level of the caster
	 *
	 * @return an integer representing the max level of this spell
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * Gets an array of integers where a value at a given index
	 * represents the damage for the spell of level = index + 1
	 *
	 * @return an array of integers where a value at a given index
	 * represents the damage for the spell of level = index + 1
	 */
	public int[] getDamage() {
		return damage;
	}

	public abstract int getEffectiveDamage(CombatSprite attacker, CombatSprite target, int spellLevel);

	/**
	 * Gets the BattleEffects for the spell at a given level
	 * A value of null will be returned for a given index if there is no
	 * BattleEffect for that level
	 *
	 * @return a BattleEffect for the spell at a given level
	 * A value of null will be returned for a given index if there is no
	 * BattleEffect for that level
	 */
	public BattleEffect[] getEffects(CombatSprite caster, int spellLevel) {
		if (effects == null || effects.length <= spellLevel || 
				effects[spellLevel] == null || effects[spellLevel].length == 0)
			return null;
		BattleEffect[] instantiatedEffects = new BattleEffect[effects[spellLevel].length];
		for (int i = 0; i < instantiatedEffects.length; i++)
		{

			instantiatedEffects[i] = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(effects[spellLevel][i], effectLevel[spellLevel][i]);
			instantiatedEffects[i].setEffectChance(getEffectChance(caster, spellLevel));
		}

		return instantiatedEffects;
	}

	/**
	 * Gets an array of integers where a value at a given index
	 * represents the range for the spell of level = index + 1
	 *
	 * @return an array of integers where a value at a given index
	 * represents the range for the spell of level = index + 1
	 */
	public Range[] getRange() {
		return range;
	}

	/**
	 * Gets an array of integers where a value at a given index
	 * represents the area for the spell of level = index + 1
	 *
	 * @return an array of integers where a value at a given index
	 * represents the area for the spell of level = index + 1
	 */
	public int[] getArea() {
		return area;
	}

	/**
	 * Gets an array of integers where a value at a given index
	 * represents the mp damage for the spell of level = index + 1
	 *
	 * @return an array of integers where a value at a given index
	 * represents the mp damage for the spell of level = index + 1
	 */
	public int[] getMpDamage() {
		return mpDamage;
	}

	/**
	 * Gets an integer that represents the spell Id for this spell
	 *
	 * @return an integer that represents the spell Id for this spell
	 * @see KnownSpell
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the image that should be displayed as this spells
	 * icon in menus
	 *
	 * @return the image that should be displayed as this spells
	 * icon in menus
	 */
	public Image getSpellIcon() {
		return spellIcon;
	}

	/**
	 * Returns the index of the image that should be used for the
	 * spell icon from the Spells image
	 *
	 * @return the index of the image that should be used for the
	 * spell icon from the Spells image
	 */
	public int getSpellIconId() {
		return spellIconIndex;
	}


	/**
	 * Gets a flag indicating whether spell animation should loop
	 *
	 * @return a flag indicating whether spell animation should loop
	 */
	public boolean isLoops() {
		return loops;
	}

	/**
	 * Gets the amount of experience that should be gained when using this spell as determined
	 * by Spells.py
	 *
	 * @param level an integer representing the "level" of the spell - 1
	 * @param attacker the CombatSprite that cast this spell
	 * @param target the CombatSprite that is the target of this spell
	 * @return an integer representing the amount of experience that should be gained when using this spell
	 * @see /scripts/Spells.py
	 */
	public abstract int getExpGained(int level, CombatSprite attacker, CombatSprite target);

	/**
	 * Gets a string containing the text that should be shown when casting the given spell as determined
	 * by Spells.py
	 *
	 * @param target the CombatSprite that is the target of this spell
	 * @return a string containing the text that should be shown when casting the given spell
	 * @see /scripts/Spells.py
	 */
	public abstract String getBattleText(CombatSprite target, int damage, int mpDamage, int attackerHPDamage,
			int attackerMPDamage);

	/**
	 * Gets the color that the spell overlay should appear as for the given spell.
	 * This occurs after the spell flash and the color fades in gradually
	 *
	 * @param the level of the spell that is being case
	 * @return the color that the spell overlay should as
	 * @see /scripts/Spells.py
	 */
	public abstract Color getSpellOverlayColor(int spellLevel);
	
	public abstract String getSpellAnimationFile(int spellLevel);
	
	public abstract String getSpellRainAnimationFile(int spellLevel);
	
	public abstract String getSpellRainAnimationName(int spellLevel);
	
	public abstract int getSpellRainFrequency(int spellLevel);
	
	public abstract ParticleEmitterConfiguration getEmitter(int spellLevel);

	/*****************************************************************************/
	/* Protected methods used by the Python scripts to set values for this spell */
	/*****************************************************************************/
	protected void setName(String name) {
		this.name = name;
	}
	protected void setCosts(int[] costs) {
		this.costs = costs;
	}
	protected void setTargetsEnemy(boolean targetsEnemy) {
		this.targetsEnemy = targetsEnemy;
	}
	protected void setTargetsAllies(boolean targetsAllies) {
		this.targetsAllies = targetsAllies;
	}
	protected void setMaxLevel(int maxLevel) {
		this.effects = new String[maxLevel][];
		this.effectLevel = new int[maxLevel][];
		this.maxLevel = maxLevel;
	}
	protected void setDamage(int[] damage) {
		this.damage = damage;
	}
	protected void setMpDamage(int[] mpDamage) {
		this.mpDamage = mpDamage;
	}
	protected void setEffects(String[] effects, int spellLevel) {
		this.effects[spellLevel - 1] = effects;
	}
	protected void setEffectLevel(int[] effectLevel, int spellLevel) {
		this.effectLevel[spellLevel - 1] = effectLevel;
	}
	public abstract int getEffectChance(CombatSprite caster, int spellLevel); 
	
	protected void setRange(Range[] range) {
		this.range = range;
	}
	protected void setArea(int[] area) {
		this.area = area;
	}
	protected void setId(String id) {
		this.id = id;
	}
	public void setSpellIcon(Image spellIcon) {
		this.spellIcon = spellIcon;
	}
	protected void setLoops(boolean loops) {
		this.loops = loops;
	}
	protected void setSpellIconIndex(int spellIconIndex) {
		this.spellIconIndex = spellIconIndex;
	}
}
