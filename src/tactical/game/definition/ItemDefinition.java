package tactical.game.definition;

import org.newdawn.slick.util.Log;

import tactical.game.exception.BadResourceException;
import tactical.game.exception.MissingCodeException;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.item.ItemUse;
import tactical.game.item.SpellItemUse;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;
import tactical.utils.XMLParser.TagArea;

public class ItemDefinition
{
	private Item item;
	private int imageX;
	private int imageY;
	private int id;

	private ItemDefinition() {}

	public static ItemDefinition parseItemDefinition(TagArea tagArea)
	{
		try
		{
			int attack = 0, defense = 0, speed = 0, style = 0, type = 0, range = 0,
					increasedMinDam = 0, increasedCrit = 0, increasedCounter = 0, increasedDouble = 0, 
					increasedEvade = 0, minHPRegen = 0, maxHPRegen = 0, minMPRegen = 0, maxMPRegen = 0, 
					effectLevel = 0, effectChance = 0, fireAffinity = 0, elecAffinity = 0, coldAffin = 0, 
					darkAffin = 0, waterAffin = 0, earthAffin = 0, windAffin = 0, lightAffin = 0, ohko = 0, ohkoOnCrit = 0;
			String weaponImage = null, effectName = null, damageAffinity = null;
			boolean equippable = false;
			boolean useDamagesItem = false;
			boolean isCustomEffect = false;
			ItemUse itemUse = null;
			SpellItemUse spellUse = null;
			//TODO Replace get attribute with remove attribute and then
			// check for any unused attributes that remain after we think we're done
			// this should be done in all definition files to ensure we don't miss anything
			for (TagArea childTagArea : tagArea.getChildren())
			{
				if (childTagArea.getTagType().equalsIgnoreCase("equippable"))
				{
					equippable = true;
					attack = Integer.parseInt(childTagArea.getAttribute("attack"));
					defense = Integer.parseInt(childTagArea.getAttribute("defense"));
					speed = Integer.parseInt(childTagArea.getAttribute("speed"));
					style = Integer.parseInt(childTagArea.getAttribute("style"));
					type = Integer.parseInt(childTagArea.getAttribute("type"));
					range = Integer.parseInt(childTagArea.getAttribute("range"));
					weaponImage = childTagArea.getAttribute("weaponimage");
					
					
					// Extended item statistics
					increasedMinDam = childTagArea.getIntAttribute("incmindam");
					increasedCrit = childTagArea.getIntAttribute("inccrit");
					increasedCounter = childTagArea.getIntAttribute("inccounter");
					increasedDouble = childTagArea.getIntAttribute("incdouble");
					increasedEvade = childTagArea.getIntAttribute("incevade");
					maxHPRegen = childTagArea.getIntAttribute("maxhpreg");
					minHPRegen = childTagArea.getIntAttribute("minhpreg");
					minMPRegen = childTagArea.getIntAttribute("minmpreg");
					maxMPRegen = childTagArea.getIntAttribute("maxmpreg");
					effectLevel = childTagArea.getIntAttribute("efflvl");
					effectChance = childTagArea.getIntAttribute("effchc");
					fireAffinity = childTagArea.getIntAttribute("fireAffin");
					elecAffinity = childTagArea.getIntAttribute("elecAffin");
					coldAffin = childTagArea.getIntAttribute("coldAffin");
					darkAffin = childTagArea.getIntAttribute("darkAffin");
					waterAffin = childTagArea.getIntAttribute("waterAffin");
					earthAffin = childTagArea.getIntAttribute("earthAffin");
					windAffin = childTagArea.getIntAttribute("windAffin");
					lightAffin = childTagArea.getIntAttribute("lightAffin");
					ohko = childTagArea.getIntAttribute("ohko");
					ohkoOnCrit = childTagArea.getIntAttribute("ohkooc");
					effectName = childTagArea.getAttribute("effect");
					if (StringUtils.isEmpty(effectName))
						effectName = null;
					// The damage affinity is a string that should not be null/empty
					damageAffinity = childTagArea.getAttribute("dmgaff");
					isCustomEffect = childTagArea.getBoolAttribute("csteff");
					
					if (StringUtils.isEmpty(weaponImage))
						weaponImage = null;
				}
				else if (childTagArea.getTagType().equalsIgnoreCase("use"))
				{
					itemUse = new ItemUse(
							Boolean.parseBoolean(
							childTagArea.getAttribute("targetsenemy")),
							Integer.parseInt(childTagArea.getAttribute("damage")),
							Integer.parseInt(childTagArea.getAttribute("mpdamage")),
							null,
							Integer.parseInt(childTagArea.getAttribute("range")),
							Integer.parseInt(childTagArea.getAttribute("area")),
							childTagArea.getAttribute("text"),
							Boolean.parseBoolean(childTagArea.getAttribute("singleuse")));
					useDamagesItem = Boolean.parseBoolean(childTagArea.getAttribute("damageitem"));
				}
				else if (childTagArea.getTagType().equalsIgnoreCase("usespell"))
				{
					spellUse = new SpellItemUse(
							childTagArea.getAttribute("spellid"),
							Integer.parseInt(childTagArea.getAttribute("level")),
							Boolean.parseBoolean(childTagArea.getAttribute("singleuse")));
					useDamagesItem = Boolean.parseBoolean(childTagArea.getAttribute("damageitem"));
				}
				else
					throw new MissingCodeException("Parsed an item with an unsupported child tag: " + childTagArea.getTagType());
			}

			ItemDefinition id = new ItemDefinition();

			id.id = Integer.parseInt(tagArea.getAttribute("id"));
			boolean isDeal = Boolean.parseBoolean(tagArea.getAttribute("isdeal"));
			boolean isDroppable = Boolean.parseBoolean(tagArea.getAttribute("droppable"));
			if (equippable)
				id.item = new EquippableItem(tagArea.getAttribute("name"), Integer.parseInt(tagArea.getAttribute("cost")), tagArea.getAttribute("description"), 
						itemUse, spellUse, useDamagesItem, isDeal, isDroppable, id.id, 
						attack, defense, speed, type, style, 
						increasedMinDam, increasedCrit, increasedCounter, 
						increasedDouble, increasedEvade, minHPRegen, maxHPRegen, 
						minMPRegen, maxMPRegen, effectLevel, effectChance, 
						fireAffinity, elecAffinity, coldAffin, darkAffin, waterAffin, 
						earthAffin, windAffin, lightAffin, ohko, ohkoOnCrit, range, 
						isCustomEffect, weaponImage, effectName, damageAffinity);
			else {
				id.item = new Item(tagArea.getAttribute("name"), 
						Integer.parseInt(tagArea.getAttribute("cost")), tagArea.getAttribute("description"),
							itemUse, spellUse, false, useDamagesItem, isDeal, isDroppable, id.id);
			}

			id.imageX = Integer.parseInt(tagArea.getAttribute("imageindexx"));
			id.imageY = Integer.parseInt(tagArea.getAttribute("imageindexy"));

			return id;
		}
		catch (BadResourceException bre)
		{
			Log.error("Parsing Item: " + tagArea.getAttribute("name") + " -> " + bre.getMessage());
			throw new BadResourceException("Parsing Item: " + tagArea.getAttribute("name") + " -> " + bre.getMessage());
		}
	}

	public Item getItem(ResourceManager fcrm)
	{
		initializeItem(item, fcrm);
		return item;
	}

	public Item getUnintializedItem()
	{
		return item;
	}

	public void initializeItem(Item i, ResourceManager fcrm)
	{
		i.setImage(fcrm.getSpriteSheet("items").getSprite(imageX, imageY));
	}

	public int getId() {
		return id;
	}
}
