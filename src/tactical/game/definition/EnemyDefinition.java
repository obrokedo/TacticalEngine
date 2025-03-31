package tactical.game.definition;

import java.util.ArrayList;

import tactical.game.battle.special.SpecialAbility;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.utils.XMLParser.TagArea;

public abstract class EnemyDefinition
{
	protected static int ENEMY_COUNT = -1;

	protected int id;
	protected String name;
	protected boolean leader = false;
	protected int hp;
	protected int mp;
	protected int attack;
	protected int defense;
	protected int speed;
	protected int move;	
	protected String movementType;
	protected int level;
	protected String animations;
	protected String effectId = null;
	protected String paletteName;
	protected int effectChance = -1, effectLevel = -1;
	protected int goldDropped;

	protected ArrayList<Integer> spellsPerLevel;
	protected ArrayList<String> spellIds;

	protected ArrayList<Integer> items;
	protected ArrayList<Boolean> itemsEquipped;
	
	protected ArrayList<String> specialAttackIds;
	protected ArrayList<Integer> specialAttackChances;

	public EnemyDefinition(TagArea tagArea) {
		parseEnemyDefinition(tagArea);
	}

	public void parseEnemyDefinition(TagArea tagArea)
	{
		name = tagArea.getAttribute("name");
		id = Integer.parseInt(tagArea.getAttribute("id"));
		hp = Integer.parseInt(tagArea.getAttribute("hp"));
		mp = Integer.parseInt(tagArea.getAttribute("mp"));
		attack = Integer.parseInt(tagArea.getAttribute("attack"));
		defense = Integer.parseInt(tagArea.getAttribute("defense"));
		speed = Integer.parseInt(tagArea.getAttribute("speed"));
		level = Integer.parseInt(tagArea.getAttribute("level"));
		move = Integer.parseInt(tagArea.getAttribute("move"));
		movementType = tagArea.getAttribute("movementtype");
		animations = tagArea.getAttribute("animations");

		if (paletteName == null || paletteName.trim().length() == 0)
			paletteName = null;
		else
			animations = animations + "-" + paletteName;

		parseCustomEnemyDefinition(tagArea);
		
		if (tagArea.getAttribute("leader") != null)
			leader = Boolean.parseBoolean(tagArea.getAttribute("leader"));

		spellsPerLevel = new ArrayList<Integer>();
		spellIds = new ArrayList<String>();
		
		specialAttackIds = new ArrayList<String>();
		specialAttackChances = new ArrayList<Integer>();
		
		items = new ArrayList<Integer>();
		itemsEquipped = new ArrayList<Boolean>();
		effectId = null;
		goldDropped = Integer.parseInt(tagArea.getAttribute("gold"));

		for (TagArea childTagArea : tagArea.getChildren())
		{
			if (childTagArea.getTagType().equalsIgnoreCase("spell"))
			{
				spellIds.add(childTagArea.getAttribute("spellid"));
				spellsPerLevel.add(Integer.parseInt(childTagArea.getAttribute("level")));
			}
			else if (childTagArea.getTagType().equalsIgnoreCase("specialattack"))
			{
				specialAttackIds.add(childTagArea.getAttribute("spellid"));
				specialAttackChances.add(Integer.parseInt(childTagArea.getAttribute("specialchance")));
			}
			else if (childTagArea.getTagType().equalsIgnoreCase("item"))
			{
				items.add(ItemResource.getItemIdByName(childTagArea.getAttribute("itemid")));
				if (childTagArea.getAttribute("equipped") != null)
					itemsEquipped.add(Boolean.parseBoolean(childTagArea.getAttribute("equipped")));
				else
					itemsEquipped.add(false);
			}
			else if (childTagArea.getTagType().equalsIgnoreCase("attackeffect"))
			{
				effectId = childTagArea.getAttribute("effectid");
				effectChance = Integer.parseInt(childTagArea.getAttribute("effectchance"));
				effectLevel = Integer.parseInt(childTagArea.getAttribute("effectlevel"));
			}
			else {
				System.out.println("UNHANDLED");
			}
		}
	}

	

	public CombatSprite getEnemy(int myId)
	{
		// Set up known spells
		ArrayList<KnownSpell> knownSpells = new ArrayList<KnownSpell>();
		for (int i = 0; i < spellsPerLevel.size(); i++)
		{
			knownSpells.add(new KnownSpell(spellIds.get(i), (byte) spellsPerLevel.get(i).intValue()));
		}
		
		ArrayList<SpecialAbility> specialAbilities = new ArrayList<SpecialAbility>();
		for (int i = 0; i < specialAttackIds.size(); i++) {
			specialAbilities.add(new SpecialAbility(specialAttackIds.get(i), specialAttackChances.get(i)));
		}

		// Create a CombatSprite from default stats, hero progression and spells known
		CombatSprite cs = createNewCombatSprite(myId, knownSpells, specialAbilities);

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

	protected abstract void parseCustomEnemyDefinition(TagArea tagArea);
	
	protected abstract CombatSprite createNewCombatSprite(int myId, ArrayList<KnownSpell> knownSpells, 
			ArrayList<SpecialAbility> specialAbilitites);

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
