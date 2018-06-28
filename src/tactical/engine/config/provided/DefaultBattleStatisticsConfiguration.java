package tactical.engine.config.provided;

import tactical.engine.config.BattleStatisticConfigration;
import tactical.game.definition.DefaultEnemyDefinition;
import tactical.game.definition.DefaultHeroDefinition;
import tactical.game.definition.EnemyDefinition;
import tactical.game.definition.HeroDefinition;
import tactical.utils.XMLParser.TagArea;

public class DefaultBattleStatisticsConfiguration implements BattleStatisticConfigration {

	@Override
	public HeroDefinition parseHeroDefinition(TagArea tagArea) {
		return new DefaultHeroDefinition(tagArea);
	}

	@Override
	public EnemyDefinition parseEnemyDefinition(TagArea tagArea) {
		return new DefaultEnemyDefinition(tagArea);
	}

}
