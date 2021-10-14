package tactical.game.item;

import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.game.Range;
import tactical.game.battle.BattleEffect;


public class EquippableItem extends Item
{
	private static final long serialVersionUID = 1L;

	public static final int TYPE_WEAPON = 0;
	public static final int TYPE_RING = 1;
	public static final int TYPE_ARMOR = 2;

	public static final int RANGE_BOW_2_NO_1 = 4;
	public static final int RANGE_BOW_3_NO_1 = 5;
	public static final int RANGE_BOW_3_NO_1_OR_2 = 6;

	private int attack, defense, speed, itemType, itemStyle, increasedMinDam, increasedCrit, increasedCounter, increasedDouble, 
			increasedEvade, minHPRegen, maxHPRegen, minMPRegen, maxMPRegen, effectLevel, effectChance, 
			fireAffinity, elecAffinity, coldAffin, darkAffin, waterAffin, earthAffin, 
			windAffin, lightAffin, ohko, ohkoOnCrit;
	private Range range;
	private boolean promotedOnly;
	private String weaponImage, weaponAnim, effectName, damageAffinity;

	/* Old "non-extended" equipped items
	public EquippableItem(String name, int cost, String description,
			 ItemUse itemUse, SpellItemUse spell, int itemId, int attack, int defense, int speed,
			 int range, int itemType, int itemStyle, String weaponImage, boolean useDamagesItem) {
		super(name, cost, description, itemUse, spell, true, useDamagesItem, itemId);
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.range = Range.convertIntToRange(range);
		this.itemType = itemType;
		this.itemStyle = itemStyle;
		this.equipped = false;
		this.weaponImage = weaponImage;
	}
	*/
	
	/*
	 *TODO NEW ATTRIBUTES
	 * The minimum damage increase can only max out at 15%, is that intentional
	 * 
	 * incmindam=7 maxhpreg=0 minhpreg=0 maxmpreg=0 minhpreg=0 
	 * effect="" efflvl=-1 effchc=0 csteff=false dmgaff="NORMAL" ohko=0 ohkooc=0
	 */
	
	public EquippableItem(String name, int cost, String description, ItemUse itemUse, SpellItemUse spellUse,
			boolean useDamagesItem, boolean isDeal, boolean isDroppable, int itemId, int attack, int defense, int speed, int itemType,
			int itemStyle, int increasedMinDam, int increasedCrit, int increasedCounter, int increasedDouble,
			int increasedEvade, int minHPRegen, int maxHPRegen, int minMPRegen, int maxMPRegen, int effectLevel,
			int effectChance, int fireAffinity, int elecAffinity, int coldAffin, int darkAffin, int waterAffin,
			int earthAffin, int windAffin, int lightAffin, int ohko, int ohkoOnCrit, int range,
			boolean promotedOnly, String weaponImage, String weaponAnim, String effectName, String damageAffinity) {
		super(name, cost, description, itemUse, spellUse, true, useDamagesItem, isDeal, isDroppable, itemId);
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.itemType = itemType;
		this.itemStyle = itemStyle;
		this.increasedMinDam = increasedMinDam;
		this.increasedCrit = increasedCrit;
		this.increasedCounter = increasedCounter;
		this.increasedDouble = increasedDouble;
		this.increasedEvade = increasedEvade;
		this.minHPRegen = minHPRegen;
		this.maxHPRegen = maxHPRegen;
		this.minMPRegen = minMPRegen;
		this.maxMPRegen = maxMPRegen;
		this.effectLevel = effectLevel;
		this.effectChance = effectChance;
		this.fireAffinity = fireAffinity;
		this.elecAffinity = elecAffinity;
		this.coldAffin = coldAffin;
		this.darkAffin = darkAffin;
		this.waterAffin = waterAffin;
		this.earthAffin = earthAffin;
		this.windAffin = windAffin;
		this.lightAffin = lightAffin;
		this.ohko = ohko;
		this.ohkoOnCrit = ohkoOnCrit;
		this.range = Range.convertIntToRange(range);
		this.promotedOnly = promotedOnly;
		this.weaponImage = weaponImage;
		this.weaponAnim = weaponAnim;
		this.effectName = effectName;
		this.damageAffinity = damageAffinity;
	}

	public Range getRange() {
		return range;
	}

	public int getAttack() {
		return attack;
	}

	public int getDefense() {
		return defense;
	}

	public int getSpeed() {
		return speed;
	}

	public int getItemType() {
		return itemType;
	}

	public int getItemStyle() {
		return itemStyle;
	}
	
	public String getWeaponImage() {
		return weaponImage;
	}
	
	public String getWeaponAnim() {
		return weaponAnim;
	}

	public int getIncreasedMinDam() {
		return increasedMinDam;
	}

	public int getIncreasedCrit() {
		return increasedCrit;
	}

	public int getIncreasedCounter() {
		return increasedCounter;
	}

	public int getIncreasedDouble() {
		return increasedDouble;
	}

	public int getIncreasedEvade() {
		return increasedEvade;
	}

	public int getMinHPRegen() {
		return minHPRegen;
	}

	public int getMaxHPRegen() {
		return maxHPRegen;
	}

	public int getMinMPRegen() {
		return minMPRegen;
	}

	public int getMaxMPRegen() {
		return maxMPRegen;
	}

	public int getEffectLevel() {
		return effectLevel;
	}

	public int getEffectChance() {
		return effectChance;
	}

	public int getFireAffinity() {
		return fireAffinity;
	}

	public int getElecAffinity() {
		return elecAffinity;
	}

	public int getColdAffin() {
		return coldAffin;
	}

	public int getDarkAffin() {
		return darkAffin;
	}

	public int getWaterAffin() {
		return waterAffin;
	}

	public int getEarthAffin() {
		return earthAffin;
	}

	public int getWindAffin() {
		return windAffin;
	}

	public int getLightAffin() {
		return lightAffin;
	}

	public int getOhko() {
		return ohko;
	}

	public int getOhkoOnCrit() {
		return ohkoOnCrit;
	}

	public String getEffectName() {
		return effectName;
	}
	
	public BattleEffect getAttackEffect()
	{
		BattleEffect eff = null;
		if (effectName != null)
		{
			eff = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(effectName, effectLevel);
			eff.setEffectChance(effectChance);
		}
		return eff;
	}

	public String getDamageAffinity() {
		return damageAffinity;
	}
	
	public boolean isPromotedOnly() {
		return promotedOnly;
	}

	@Override
	public Item copyItem() {
		return new EquippableItem(name, cost, description, itemUse, spellUse, 
				isEquippable, useDamagesItem, isDeal, isDroppable, itemId, attack, defense, 
				speed, itemType, itemStyle, increasedMinDam, increasedCrit, 
				increasedCounter, increasedDouble, increasedEvade, minHPRegen,
				maxHPRegen, minMPRegen, maxMPRegen, effectLevel, effectChance, 
				fireAffinity, elecAffinity, coldAffin, darkAffin, waterAffin, 
				earthAffin, windAffin, lightAffin, ohko, ohkoOnCrit, range, 
				promotedOnly, weaponImage, weaponAnim, effectName, damageAffinity, image);
	}

	private EquippableItem(String name, int cost, String description, ItemUse itemUse, SpellItemUse spellUse,
			boolean isEquippable, boolean useDamagesItem, boolean isDeal, boolean isDroppable, int itemId, int attack, int defense,
			int speed, int itemType, int itemStyle, int increasedMinDam, int increasedCrit, int increasedCounter,
			int increasedDouble, int increasedEvade, int minHPRegen, int maxHPRegen, int minMPRegen, int maxMPRegen,
			int effectLevel, int effectChance, int fireAffinity, int elecAffinity, int coldAffin, int darkAffin,
			int waterAffin, int earthAffin, int windAffin, int lightAffin, int ohko, int ohkoOnCrit, Range range,
			boolean promotedOnly, String weaponImage, String weaponAnim, String effectName, String damageAffinity, Image image) {
		super(name, cost, description, itemUse, spellUse, isEquippable, useDamagesItem, isDeal, isDroppable, itemId);
		this.attack = attack;
		this.defense = defense;
		this.speed = speed;
		this.itemType = itemType;
		this.itemStyle = itemStyle;
		this.increasedMinDam = increasedMinDam;
		this.increasedCrit = increasedCrit;
		this.increasedCounter = increasedCounter;
		this.increasedDouble = increasedDouble;
		this.increasedEvade = increasedEvade;
		this.minHPRegen = minHPRegen;
		this.maxHPRegen = maxHPRegen;
		this.minMPRegen = minMPRegen;
		this.maxMPRegen = maxMPRegen;
		this.effectLevel = effectLevel;
		this.effectChance = effectChance;
		this.fireAffinity = fireAffinity;
		this.elecAffinity = elecAffinity;
		this.coldAffin = coldAffin;
		this.darkAffin = darkAffin;
		this.waterAffin = waterAffin;
		this.earthAffin = earthAffin;
		this.windAffin = windAffin;
		this.lightAffin = lightAffin;
		this.ohko = ohko;
		this.ohkoOnCrit = ohkoOnCrit;
		this.range = range;
		this.weaponImage = weaponImage;
		this.weaponAnim = weaponAnim;
		this.effectName = effectName;
		this.damageAffinity = damageAffinity;
		this.image = image;
	}
}
