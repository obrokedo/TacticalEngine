package tactical.engine.config;

import tactical.game.battle.spell.KnownSpell;
import tactical.game.constants.AttributeStrength;
import tactical.game.sprite.CombatSprite;

public interface LevelProgressionConfiguration {
	public float[] getProgressArray(String progressionType, boolean promoted);

	public abstract String[] getStandardStatProgressionTypeList();
	public abstract String[] getBodyMindProgressionTypeList();
	public abstract int getBaseBattleStat(AttributeStrength attributeStrength, CombatSprite combatSprite);
	public abstract int getBaseBodyMindStat(AttributeStrength attributeStrength, CombatSprite combatSprite);
	public abstract int getLevelUpBattleStat(AttributeStrength unpromotedAttributeStrength, AttributeStrength promotedAttributeStrength, 
			CombatSprite heroSprite, int newLevel, boolean promoted, int currentValue);
	public abstract int getLevelUpBodyMindStat(String progressionType, CombatSprite heroSprite,
			int newLevel, boolean promoted);
	public abstract String levelUpHero(CombatSprite heroSprite);
	public abstract boolean spellLevelIncreased(CombatSprite heroSprite, KnownSpell knownSpell);
}
