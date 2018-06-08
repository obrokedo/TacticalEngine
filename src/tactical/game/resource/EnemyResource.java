package tactical.game.resource;

import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import tactical.game.definition.EnemyDefinition;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class EnemyResource
{
	private static int ID = -1;

	private static Hashtable<Integer, EnemyDefinition> enemyDefinitionsById = null;

	public static void initialize(Hashtable<Integer, EnemyDefinition> enemyDefinitionsById)
	{
		EnemyResource.enemyDefinitionsById = enemyDefinitionsById;
	}

	public static CombatSprite getEnemy(int enemyId)
	{
		return enemyDefinitionsById.get(enemyId).getEnemy(ID--);
	}

	public static String getAnimation(int enemyId)
	{
		return enemyDefinitionsById.get(enemyId).getAnimation();
	}
	
	public static int getEnemyIdByName(String enemyName) {
		if (StringUtils.isNotEmpty(enemyName))
			return enemyDefinitionsById.values().stream().filter(ed -> ed.getName().equals(enemyName)).findFirst().get().getId();
		return -1;
	}
	
	public static CombatSprite getEnemy(String enemyName) {
		return enemyDefinitionsById.values().stream().filter(
				ed -> ed.getName().equalsIgnoreCase(enemyName)).findFirst().get().getEnemy(-1);
	}
	
	public static List<String> getEnemyNames() {
		return enemyDefinitionsById.values().stream().map(EnemyDefinition::getName).collect(Collectors.toList());
	}
	
	public static int getGoldDroppedByName(String enemyName) {
		if (StringUtils.isNotEmpty(enemyName))
			return enemyDefinitionsById.values().stream().filter(ed -> ed.getName().equals(enemyName)).findFirst().get().getGoldDropped();
		return 0;
	}
}
