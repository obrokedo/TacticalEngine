package tactical.game.item;

import tactical.engine.TacticalGame;
import tactical.game.Range;
import tactical.game.battle.BattleEffect;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class ItemUse
{
	public static final int DEFAULT_EXP = -1;

	private boolean targetsEnemy;
	private String effectId;
	private int effectLevel;
	private int damage;
	private int mpDamage;
	private transient BattleEffect effects = null;
	private Range range;
	private int area;
	private String battleText;
	private boolean singleUse;
	private boolean useOutsideBattle;

	public ItemUse(boolean targetsEnemy, int damage, int mpDamage,
			String effectId, int effectLevel, int range, int area, String battleText, boolean singleUse, boolean useOutsideBattle) {
		super();
		this.targetsEnemy = targetsEnemy;
		this.damage = damage;
		this.mpDamage = mpDamage;
		this.effectId = effectId;
		this.effectLevel = effectLevel;
		this.range = Range.convertIntToRange(range);
		this.area = area;
		this.battleText = battleText;
		this.singleUse = singleUse;
		this.useOutsideBattle = useOutsideBattle;
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
		// If there is an effectid specified then make sure the effect is loaded before returning
		if (StringUtils.isNotEmpty(effectId)) {
			// Effects are lazy loaded
			if (effects == null) {
				effects = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(effectId, effectLevel);
			}
		}
		
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

	public boolean isUseOutsideBattle() {
		return useOutsideBattle;
	}

	public int getExpGained()
	{
		return 1;
	}

	public String getBattleText(String targetName){
		return targetName + " " + battleText;
	}
}
