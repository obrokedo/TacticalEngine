package tactical.engine.config;

import tactical.game.definition.EnemyDefinition;
import tactical.game.definition.HeroDefinition;
import tactical.utils.XMLParser.TagArea;

public interface BattleStatisticConfigration {
	public HeroDefinition parseHeroDefinition(TagArea tagArea);
	public EnemyDefinition parseEnemyDefinition(TagArea tagArea);
}
