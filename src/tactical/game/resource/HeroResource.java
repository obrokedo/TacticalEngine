package tactical.game.resource;

import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import tactical.game.definition.HeroDefinition;
import tactical.game.exception.BadResourceException;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class HeroResource
{
	private static Hashtable<Integer, HeroDefinition> heroDefinitionsById = new Hashtable<Integer, HeroDefinition>();

	public static void initialize(Hashtable<Integer, HeroDefinition> heroDefinitionsById)
	{
		HeroResource.heroDefinitionsById = heroDefinitionsById;
	}

	public static CombatSprite getHero(int heroId)
	{
		return heroDefinitionsById.get(heroId).getHero();
	}
	
	public static CombatSprite getHero(String heroName)
	{
		for (HeroDefinition hd : heroDefinitionsById.values())
		{
			if (hd.getName().equalsIgnoreCase(heroName))
				return hd.getHero();
		}
		
		throw new BadResourceException("Unable to find a hero with the name: " + heroName + " Did you misspell the name in DevParams?");
	}
	
	public static int getHeroIdByName(String heroName) {
		if (StringUtils.isNotEmpty(heroName))
			return heroDefinitionsById.values().stream().filter(hd -> hd.getName().equals(heroName)).findFirst().get().getId();
		return -1;
	}

	public static String getAnimation(int heroId)
	{
		return heroDefinitionsById.get(heroId).getAnimation();
	}
	
	public static List<String> getHeroNames() {
		return heroDefinitionsById.values().stream().map(HeroDefinition::getName).collect(Collectors.toList());
	}
}
