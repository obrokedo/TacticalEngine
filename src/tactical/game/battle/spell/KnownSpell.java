package tactical.game.battle.spell;

import java.io.Serializable;

import tactical.game.resource.SpellResource;
import tactical.loading.ResourceManager;

public class KnownSpell implements Serializable
{
	private static final long serialVersionUID = 1L;

	// These ids are directly linked to the icons index in the spell images
	public static final int ID_HEAL = 0;
	public static final int ID_AURA = 1;
	public static final int ID_DETOX = 2;
	public static final int ID_BOOST = 3;
	public static final int ID_SLOW = 4;
	public static final int ID_STRENGTH = 5;
	public static final int ID_DISPEL = 6;
	public static final int ID_MUDDLE = 7;
	public static final int ID_DESOUL = 8;
	public static final int ID_SLEEP = 9;
	public static final int ID_EGRESS = 10;
	public static final int ID_BLAZE = 11;
	public static final int ID_FREEZE = 12;
	public static final int ID_BOLT = 13;
	public static final int ID_BLAST = 14;
	public static final int ID_DAO = 21;
	public static final int ID_APOLLO = 22;
	public static final int ID_NEPTUNE = 23;
	public static final int ID_ATLAS = 24;
	

	private String spellId;
	private byte maxLevel;
	private transient SpellDefinition spell;

	/**
	 * A constructor to add spell descriptors to add spells to a CombatSprite before resources have been loaded.
	 * The spell field of this object will be null until the spell has been intitialized on resource load. This can
	 * be used when creating initial heroes before the game has started or giving spells to enemies as they will be
	 * intialized after the spells have been added
	 *
	 * @param spellId The spell id of the spell to create
	 * @param maxLevel The max level that the caster knows of this spell
	 */
	public KnownSpell(String spellId, byte maxLevel) {
		super();
		this.spellId = spellId;
		this.maxLevel = maxLevel;
	}

	public KnownSpell(byte maxLevel, SpellDefinition spell) {
		super();
		this.spellId = spell.id;
		this.maxLevel = maxLevel;
		this.spell = spell;
	}

	public String getSpellId() {
		return spellId;
	}

	public byte getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(byte maxLevel) {
		this.maxLevel = maxLevel;
	}

	public SpellDefinition getSpell() {
		return spell;
	}

	public void initializeFromLoad(ResourceManager fcrm)
	{
		this.spell = SpellResource.getSpell(spellId);
	}
}
