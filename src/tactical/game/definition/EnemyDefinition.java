package tactical.game.definition;

import java.util.ArrayList;

import tactical.game.battle.spell.KnownSpell;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.utils.XMLParser.TagArea;

public class EnemyDefinition
{
	private static int ENEMY_COUNT = -1;

	private int id;
	private String name;
	private boolean leader = false;
	private int hp;
	private int mp;
	private int attack;
	private int defense;
	private int speed;
	private int move;
	private int maxFireAffin, maxElecAffin,
		maxColdAffin, maxDarkAffin, maxWaterAffin, maxEarthAffin, maxWindAffin,
		maxLightAffin, maxBody, maxMind, maxCounter, maxEvade,
		maxDouble, maxCrit;
	private String movementType;
	private int level;
	private String animations;
	private String effectId = null;
	private String paletteName;
	private int effectChance = -1, effectLevel = -1;
	private int goldDropped;

	private ArrayList<Integer> spellsPerLevel;
	private ArrayList<String> spellIds;

	private ArrayList<Integer> items;
	private ArrayList<Boolean> itemsEquipped;

	private EnemyDefinition() {}

	public static EnemyDefinition parseEnemyDefinition(TagArea tagArea)
	{
		EnemyDefinition hd = new EnemyDefinition();

		hd.name = tagArea.getAttribute("name");
		hd.id = Integer.parseInt(tagArea.getAttribute("id"));
		hd.hp = Integer.parseInt(tagArea.getAttribute("hp"));
		hd.mp = Integer.parseInt(tagArea.getAttribute("mp"));
		hd.attack = Integer.parseInt(tagArea.getAttribute("attack"));
		hd.defense = Integer.parseInt(tagArea.getAttribute("defense"));
		hd.speed = Integer.parseInt(tagArea.getAttribute("speed"));
		hd.level = Integer.parseInt(tagArea.getAttribute("level"));
		hd.move = Integer.parseInt(tagArea.getAttribute("move"));
		hd.movementType = tagArea.getAttribute("movementtype");
		hd.animations = tagArea.getAttribute("animations");

		if (hd.paletteName == null || hd.paletteName.trim().length() == 0)
			hd.paletteName = null;
		else
			hd.animations = hd.animations + "-" + hd.paletteName;

		// Load affinities
		hd.maxFireAffin = Integer.parseInt(tagArea.getAttribute("fireAffin"));
		hd.maxElecAffin = Integer.parseInt(tagArea.getAttribute("elecAffin"));
		hd.maxColdAffin = Integer.parseInt(tagArea.getAttribute("coldAffin"));
		hd.maxDarkAffin = Integer.parseInt(tagArea.getAttribute("darkAffin"));
		hd.maxWaterAffin = Integer.parseInt(tagArea.getAttribute("waterAffin"));
		hd.maxEarthAffin = Integer.parseInt(tagArea.getAttribute("earthAffin"));
		hd.maxWindAffin = Integer.parseInt(tagArea.getAttribute("windAffin"));
		hd.maxLightAffin = Integer.parseInt(tagArea.getAttribute("lightAffin"));

		// Load body/mind
		hd.maxBody = Integer.parseInt(tagArea.getAttribute("bodyStrength"));
		hd.maxMind = Integer.parseInt(tagArea.getAttribute("mindStrength"));

		// Load battle stats
		hd.maxCounter = Integer.parseInt(tagArea.getAttribute("counterStrength"));
		hd.maxEvade = Integer.parseInt(tagArea.getAttribute("evadeStrength"));
		hd.maxDouble = Integer.parseInt(tagArea.getAttribute("doubleStrength"));
		hd.maxCrit = Integer.parseInt(tagArea.getAttribute("critStrength"));

		if (tagArea.getAttribute("leader") != null)
			hd.leader = Boolean.parseBoolean(tagArea.getAttribute("leader"));

		hd.spellsPerLevel = new ArrayList<Integer>();
		hd.spellIds = new ArrayList<String>();
		hd.items = new ArrayList<Integer>();
		hd.itemsEquipped = new ArrayList<Boolean>();
		hd.effectId = null;
		hd.goldDropped = Integer.parseInt(tagArea.getAttribute("gold"));

		for (TagArea childTagArea : tagArea.getChildren())
		{
			if (childTagArea.getTagType().equalsIgnoreCase("spell"))
			{
				hd.spellIds.add(childTagArea.getAttribute("spellid"));
				hd.spellsPerLevel.add(Integer.parseInt(childTagArea.getAttribute("level")));
			}
			else if (childTagArea.getTagType().equalsIgnoreCase("item"))
			{
				hd.items.add(ItemResource.getItemIdByName(childTagArea.getAttribute("itemid")));
				if (childTagArea.getAttribute("equipped") != null)
					hd.itemsEquipped.add(Boolean.parseBoolean(childTagArea.getAttribute("equipped")));
				else
					hd.itemsEquipped.add(false);
			}
			else if (childTagArea.getTagType().equalsIgnoreCase("attackeffect"))
			{
				hd.effectId = childTagArea.getAttribute("effectid");
				hd.effectChance = Integer.parseInt(childTagArea.getAttribute("effectchance"));
				hd.effectLevel = Integer.parseInt(childTagArea.getAttribute("effectlevel"));
			}
		}

		return hd;
	}

	public CombatSprite getEnemy(int myId)
	{
		// Set up known spells
		ArrayList<KnownSpell> knownSpells = new ArrayList<KnownSpell>();
		for (int i = 0; i < spellsPerLevel.size(); i++)
		{
			knownSpells.add(new KnownSpell(spellIds.get(i), (byte) spellsPerLevel.get(i).intValue()));
		}

		// Create a CombatSprite from default stats, hero progression and spells known
		CombatSprite cs = new CombatSprite(leader, name, animations, hp, mp, attack, defense,
				speed, move, movementType, maxFireAffin, maxElecAffin,
				maxColdAffin, maxDarkAffin, maxWaterAffin, maxEarthAffin, maxWindAffin,
				maxLightAffin, maxBody, maxMind, maxCounter, maxEvade,
				maxDouble, maxCrit, level, myId, knownSpells, ENEMY_COUNT--,
				effectId, effectChance, effectLevel);

		// Add items to the combat sprite
		for (int i = 0; i < items.size(); i++)
		{
			Item item = ItemResource.getUninitializedItem(items.get(i));
			cs.addItem(item);
			if (itemsEquipped.get(i))
				cs.equipItem((EquippableItem) item);
		}

		return cs;
	}

	public int getId() {
		return id;
	}

	public String getAnimation() {
		return animations;
	}

	public static void resetEnemyIds()
	{
		ENEMY_COUNT = -1;
	}
	
	public static void setNextEnemyId(int nextId)
	{
		ENEMY_COUNT = nextId;
	}

	public String getName() {
		return name;
	}

	public int getGoldDropped() {
		return goldDropped;
	}
}
