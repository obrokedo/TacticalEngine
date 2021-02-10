package tactical.game.definition;

import java.util.ArrayList;
import java.util.List;

import tactical.game.battle.LevelUpResult;
import tactical.game.exception.BadResourceException;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.HeroProgression;
import tactical.game.sprite.Progression;
import tactical.utils.StringUtils;
import tactical.utils.XMLParser.TagArea;

public abstract class HeroDefinition
{
	protected int id;
	protected String name;
	protected boolean leader;
	protected boolean startsPromoted;
	protected int level;
	protected String animations;

	protected String className[];
	protected String classDescription[];
	protected int move[];
	protected String movementType[];

	protected String attackGain[];
	protected int attackStart[];
	protected int attackEnd[];
	protected String defenseGain[];
	protected int defenseStart[];
	protected int defenseEnd[];
	protected String speedGain[];
	protected int speedStart[];
	protected int speedEnd[];
	protected String hpGain[];
	protected int hpStart[];
	protected int hpEnd[];
	protected String mpGain[];
	protected int mpStart[];
	protected int mpEnd[];
	
	protected boolean[] promotedProgression;
	protected boolean[] specialPromoted;
	protected int[] specialPromotionItemId;

	

	protected int[][] usuableWeapons;

	protected ArrayList<ArrayList<int[]>> spellsPerLevel;
	protected ArrayList<ArrayList<String>> spellIds;

	// These are starting items so we don't need one per progression
	protected ArrayList<Integer> items;
	protected ArrayList<Boolean> itemsEquipped;

	public HeroDefinition(TagArea tagArea) {
		parseHeroDefinition(tagArea);
	}

	private void parseHeroDefinition(TagArea tagArea)
	{
		try
		{
			// Get the starting attributes for this hero and the values that will not change
			name = tagArea.getAttribute("name");
			id = Integer.parseInt(tagArea.getAttribute("id"));
			startsPromoted = Boolean.parseBoolean(tagArea.getAttribute("promoted"));
			level = Integer.parseInt(tagArea.getAttribute("level"));
			animations = tagArea.getAttribute("animations");

			if (tagArea.getAttribute("leader") != null)
				leader = Boolean.parseBoolean(tagArea.getAttribute("leader"));

			// Arbitrarily choose 5 progression amounts and hope that there are never more then 3 special progressions...
			int listSize = 5;

			// Read base stats
			move = new int[listSize];
			movementType = new String[listSize];
			attackStart = new int[listSize];
			attackEnd = new int[listSize];
			defenseStart = new int[listSize];
			defenseEnd = new int[listSize];
			speedStart = new int[listSize];
			speedEnd = new int[listSize];
			hpStart = new int[listSize];
			hpEnd = new int[listSize];
			mpStart = new int[listSize];
			mpEnd = new int[listSize];
			attackGain = new String[listSize];
			defenseGain = new String[listSize];
			speedGain = new String[listSize];
			hpGain = new String[listSize];
			mpGain = new String[listSize];
			usuableWeapons = new int[listSize][];
			className = new String[listSize];
			classDescription = new String[listSize];

			parseCustomHeroDefinition(listSize);
			
			// Initialize spell stuff
			spellsPerLevel = new ArrayList<ArrayList<int[]>>();
			spellIds = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < listSize; i++) {
				spellsPerLevel.add(new ArrayList<>());
				spellIds.add(new ArrayList<>());
			}
			
			items = new ArrayList<Integer>();
			itemsEquipped = new ArrayList<Boolean>();

			
			promotedProgression = new boolean[listSize];
			specialPromoted = new boolean[listSize];
			specialPromotionItemId = new int[listSize];

			parseHeroProgressions(tagArea);
			
			parseProgressionReliantFields(tagArea);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			throw new BadResourceException("Unable to load hero statistics. Make sure that the Heroes\n"
					+ "is up to date by exporting heroes from the planner " + t.getMessage());
		}
	}
	
	protected abstract void parseCustomHeroDefinition(int maxProgressions);

	private void parseProgressionReliantFields(TagArea tagArea) {
		for (TagArea childTagArea : tagArea.getChildren()) {
			
			if (childTagArea.getTagType().equalsIgnoreCase("spellprogression"))
			{
				// Get the progression that is associated with these spells
				boolean spellPromoted = Boolean.parseBoolean(childTagArea.getAttribute("promotedprog"));
				boolean spellSpecialPromoted = Boolean.parseBoolean(childTagArea.getAttribute("specialpromoted"));
				if (spellSpecialPromoted)
					spellPromoted = false;
				boolean found = false;
				
				for (int associatedProgressionIndex = 0; associatedProgressionIndex < spellIds.size(); associatedProgressionIndex++) {
					if (className[associatedProgressionIndex] != null && 
						promotedProgression[associatedProgressionIndex] == spellPromoted &&
						specialPromoted[associatedProgressionIndex] == spellSpecialPromoted) {
						
						spellIds.get(associatedProgressionIndex).add(childTagArea.getAttribute("spellid"));
						
						String[] splitSpell = childTagArea.getAttribute("gained").split(",");
						int[] splitLevel = new int[splitSpell.length];
						for (int i = 0; i < splitSpell.length; i++)
							splitLevel[i] = Integer.parseInt(splitSpell[i].trim());
						spellsPerLevel.get(associatedProgressionIndex).add(splitLevel);
						
						found = true;
						break;
					}
				}
				
				if (!found)
					throw new BadResourceException("A spell progression was specified for " + name + " that is not associated with "
							+ "any specified progression due to the settings of it's promoted/special promotion attributes");
			}
			
			// These are starting items and should be associated with the starting progression
			else if (childTagArea.getTagType().equalsIgnoreCase("item"))
			{
				items.add(ItemResource.getItemIdByName(childTagArea.getAttribute("itemid")));
				if (childTagArea.getAttribute("equipped") != null)
					itemsEquipped.add(Boolean.parseBoolean(childTagArea.getAttribute("equipped")));
				else
					itemsEquipped.add(false);
			}
		}
	}

	private void parseHeroProgressions(TagArea tagArea) {
		int index = 0;
		for (TagArea childTagArea : tagArea.getChildren())
		{
			if (childTagArea.getTagType().equalsIgnoreCase("progression"))
			{
				promotedProgression[index] = Boolean.parseBoolean(childTagArea.getAttribute("promoted"));
				
				// Get special promotion indicators
				specialPromoted[index] = Boolean.parseBoolean(childTagArea.getAttribute("specialpromoted"));
				if (specialPromoted[index])
					promotedProgression[index] = false;
				if (StringUtils.isNotEmpty(childTagArea.getAttribute("specialpromoteitem")))
					specialPromotionItemId[index] = Integer.parseInt(childTagArea.getAttribute("specialpromoteitem"));
				
				move[index] = Integer.parseInt(childTagArea.getAttribute("move"));
				movementType[index] = childTagArea.getAttribute("movementtype");
				attackGain[index] = childTagArea.getAttribute("attack");
				attackStart[index] = Integer.parseInt(childTagArea.getAttribute("attackstart"));
				attackEnd[index] = Integer.parseInt(childTagArea.getAttribute("attackend"));
				defenseGain[index] = childTagArea.getAttribute("defense");
				defenseStart[index] = Integer.parseInt(childTagArea.getAttribute("defensestart"));
				defenseEnd[index] = Integer.parseInt(childTagArea.getAttribute("defenseend"));
				speedGain[index] = childTagArea.getAttribute("speed");
				speedStart[index] = Integer.parseInt(childTagArea.getAttribute("speedstart"));
				speedEnd[index] = Integer.parseInt(childTagArea.getAttribute("speedend"));
				hpGain[index] = childTagArea.getAttribute("hp");
				hpStart[index] = Integer.parseInt(childTagArea.getAttribute("hpstart"));
				hpEnd[index] = Integer.parseInt(childTagArea.getAttribute("hpend"));
				mpGain[index] = childTagArea.getAttribute("mp");
				mpStart[index] = Integer.parseInt(childTagArea.getAttribute("mpstart"));
				mpEnd[index] = Integer.parseInt(childTagArea.getAttribute("mpend"));
				className[index] = childTagArea.getAttribute("class");
				classDescription[index] = childTagArea.getAttribute("evaluation");

				parseCustomHeroProgression(index, childTagArea);


				String[] splitItems = childTagArea.getAttribute("usuableitems").split(",");
				int[] splitIds = new int[splitItems.length];
				for (int i = 0; i < splitItems.length; i++)
					splitIds[i] = Integer.parseInt(splitItems[i].trim());

				usuableWeapons[index] = splitIds;
				index++;
			}
		}
	}

	protected abstract void parseCustomHeroProgression(int index, TagArea childTagArea);

	public CombatSprite getHero()
	{
		Progression unpromotedProgression = null;
		Progression promotedProgression = null;
		List<Progression> specialProgressions = new ArrayList<>();
		
		for (int i = 0; i < this.promotedProgression.length; i++) {
			if (className[i] == null)
				continue;
			if (this.specialPromoted[i]) {
				specialProgressions.add(getProgression(i));
			} else if (this.promotedProgression[i]) {
				promotedProgression = this.getProgression(i);
			} else {
				unpromotedProgression = this.getProgression(i);
			}
		}

		// Create hero progression
		HeroProgression heroProgression = new HeroProgression(unpromotedProgression, promotedProgression, specialProgressions, id);
		
		// Create a CombatSprite from default stats, hero progression and spells known
		CombatSprite cs = createNewCombatSprite(heroProgression);
		
		cs.setLevel(1);
		for (int i = 1; i < level; i++) {
			LevelUpResult lur = cs.getHeroProgression().getLevelUpResults(cs);
			cs.getHeroProgression().levelUp(cs, lur);
		}
		cs.setExp(0);
		
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


	protected CombatSprite createNewCombatSprite(HeroProgression heroProgression) {
		return new CombatSprite(leader, name, animations, heroProgression,
				level, 0, startsPromoted, id);
	}

	protected abstract Progression getProgression(int index);

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public String getAnimation() {
		return animations;
	}
}