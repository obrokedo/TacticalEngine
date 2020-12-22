package tactical.game.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import tactical.game.definition.ItemDefinition;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;

public class ItemResource
{
	private static Hashtable<Integer, ItemDefinition> itemDefinitionsById = null;

	public static void initialize(Hashtable<Integer, ItemDefinition> itemDefinitionsById)
	{
		ItemResource.itemDefinitionsById = itemDefinitionsById;
	}

	public static Item getItem(int itemId, ResourceManager fcrm)
	{
		return itemDefinitionsById.get(itemId).getItem(fcrm).copyItem();
	}

	public static Item getUninitializedItem(int itemId)
	{
		return itemDefinitionsById.get(itemId).getUnintializedItem().copyItem();
	}
	
	public static int getItemIdByName(String itemName) {
		if (StringUtils.isNotEmpty(itemName)) {
			return itemDefinitionsById.values().stream().filter(
				id -> id.getUnintializedItem().getName().equalsIgnoreCase(itemName)).findFirst().get().getId();
		}
		return -1;
	}

	public static void initializeItem(Item item, ResourceManager fcrm)
	{
		itemDefinitionsById.get(item.getItemId()).initializeItem(item, fcrm);
		// Check to see if the name is null then, if so then this item has been unserialized and
		// needs it's transient fields back into it
		if (item.getName() == null) {
			item.initializeTransientFieldsFromItem(getItem(item.getItemId(), fcrm));
		}

		// If there is a spell use defined, initialize it so the
		// spell object can be loaded
		if (item.getSpellUse() != null)
			item.getSpellUse().initialize(fcrm);
	}
	
	public static ArrayList<Item> getAllWeapons() {
		ArrayList<Item> itemDefs = new ArrayList<>();
		for (ItemDefinition id : itemDefinitionsById.values()) {
			Item item = id.getUnintializedItem();
			if (item.isEquippable() && 
					((EquippableItem) item).getItemType() == EquippableItem.TYPE_WEAPON) {
				itemDefs.add(item);
			}
		}
		return itemDefs;
	}
	
	public static ArrayList<Item> getAllItems() {
		ArrayList<Item> itemDefs = new ArrayList<>();
		for (ItemDefinition id : itemDefinitionsById.values()) {
			Item item = id.getUnintializedItem();
			itemDefs.add(item);
		}
		return itemDefs;
	}
}
