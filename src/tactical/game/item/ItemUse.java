package tactical.game.item;

import tactical.game.Range;
import tactical.game.battle.BattleEffect;

public class ItemUse
{
	public static final int DEFAULT_EXP = -1;

	private boolean targetsEnemy;
	private int damage;
	private int mpDamage;
	private BattleEffect effects;
	private Range range;
	private int area;
	private String battleText;
	private boolean singleUse;

	// TODO EFFECTS ARE NOT SUPPORTED FOR ITEM USE YET
	public ItemUse(boolean targetsEnemy, int damage, int mpDamage,
			BattleEffect effects, int range, int area, String battleText, boolean singleUse) {
		super();
		this.targetsEnemy = targetsEnemy;
		this.damage = damage;
		this.mpDamage = mpDamage;
		this.effects = effects;
		this.range = Range.convertIntToRange(range);
		this.area = area;
		this.battleText = battleText;
		this.singleUse = singleUse;
	}

	public boolean isTargetsEnemy() {
		return targetsEnemy;
	}

	public int getDamage() {
		return damage;
	}

	public int getMpDamage() {
		return mpDamage;
	}

	public BattleEffect getEffects() {
		return effects;
	}

	public Range getRange() {
		return range;
	}

	public int getArea() {
		return area;
	}

	public String getBattleText() {
		return battleText;
	}

	public boolean isSingleUse() {
		return singleUse;
	}

	public int getExpGained()
	{
		return 1;
	}

	public String getBattleText(String targetName){
		return targetName + " " + battleText;
	}
}
