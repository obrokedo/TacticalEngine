package tactical.game.dev;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.persist.ClientProfile;
import tactical.game.resource.HeroResource;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class DevParams {
	private static final String DEV_PARAMS_FILE = "DevParams";

	public static void parseDevParams(String fileName, ClientProfile cp, String[] startingHeroes)
	{
		try {
			ArrayList<TagArea> tags = XMLParser.process("loadouts/" + fileName, true);
			cp.removeAllHeroes();
			
			for (String hero : startingHeroes)
				cp.addHero(HeroResource.getHero(hero));
			
			for (TagArea t : tags) {
				if (t.getTagType().equalsIgnoreCase("hero")) {
					int id = HeroResource.getHeroIdByName(t.getAttribute("name"));					
					CombatSprite hero = null;
					boolean found = false;
					
					for (CombatSprite h : cp.getHeroes()) {
						if (h.getId() == id) {
							found = true;
							hero = h;
							break;
						}
					}
					
					if (!found) {
						hero = HeroResource.getHero(id);
						cp.addHero(hero);
					}
					
					hero.upgradeHeroToLevel(t.getIntAttribute("level"));
					while (hero.getItemsSize() > 0) {
						if (hero.getEquipped().get(0)) {
							hero.unequipItem((EquippableItem) hero.getItem(0));
						}
						hero.removeItem(hero.getItem(0));
					}
					
					String[] items = t.getAttribute("item").split(",");
					String[] eqp = t.getAttribute("eqp").split(",");
					for (int i = 0; i < items.length; i++) {
						Item item = ItemResource.getUninitializedItem(Integer.parseInt(items[i]));
						hero.addItem(item);
						if (Boolean.parseBoolean(eqp[i])) 
							hero.equipItem((EquippableItem) item);
					}
						
				} else if (t.getTagType().equalsIgnoreCase("gold")) {
					cp.setGold(t.getIntAttribute("amt"));
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Log.debug("An error occurred while trying to load the dev params from: " + DEV_PARAMS_FILE);
		}
	}
}
