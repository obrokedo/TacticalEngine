package tactical.game.definition;

import java.util.ArrayList;
import java.util.List;

import tactical.game.constants.AttributeStrength;
import tactical.game.exception.BadResourceException;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.HeroProgression;
import tactical.game.sprite.Progression;
import tactical.utils.StringUtils;
import tactical.utils.XMLParser.TagArea;

public class HeroDefinition
{
	private int id;
	private String name;
	private boolean leader;
	private boolean startsPromoted;
	private int level;
	private String animations;

	private String className[];
	private int move[];
	private String movementType[];

	private String attackGain[];
	private int attackStart[];
	private int attackEnd[];
	private String defenseGain[];
	private int defenseStart[];
	private int defenseEnd[];
	private String speedGain[];
	private int speedStart[];
	private int speedEnd[];
	private String hpGain[];
	private int hpStart[];
	private int hpEnd[];
	private String mpGain[];
	private int mpStart[];
	private int mpEnd[];
	
	private boolean[] promotedProgression;
	private boolean[] specialPromoted;
	private int[] specialPromotionItemId;

	private String[] bodyProgression, mindProgression;

	private int[] maxFireAffin, maxElecAffin,
		maxColdAffin, maxDarkAffin, maxWaterAffin, maxEarthAffin, maxWindAffin,
		maxLightAffin;
	private AttributeStrength[] maxBody, maxMind, maxCounter, maxEvade,
		maxDouble, maxCrit;

	private int[][] usuableWeapons;

	private ArrayList<ArrayList<int[]>> spellsPerLevel;
	private ArrayList<ArrayList<String>> spellIds;

	// These are starting items so we don't need one per progression
	private ArrayList<Integer> items;
	private ArrayList<Boolean> itemsEquipped;

	private HeroDefinition() {}

	public static HeroDefinition parseHeroDefinition(TagArea tagArea)
	{
		/*
		<hero name="Noah" id=0 leader=true hp=12 mp=0 attack=9 defense=5 speed=4 promoted=false level=1 portrait=0 animations="Noah">
			<progression promoted=false attack=1 defense=1 speed=1 hp=1 mp=1 move=5 movementtype=0 usuableitems=2/>
			<progression promoted=true attack=1 defense=1 speed=1 hp=1 mp=1 move=7 movementtype=0 usuableitems=2/>
			<spellprogression spellid=0 gained=2,6,9/>
			<item itemid=0 equipped=true>
		</hero>
		*/

		HeroDefinition hd = new HeroDefinition();

		try
		{
			// Get the starting attributes for this hero and the values that will not change
			hd.name = tagArea.getAttribute("name");
			hd.id = Integer.parseInt(tagArea.getAttribute("id"));
			hd.startsPromoted = Boolean.parseBoolean(tagArea.getAttribute("promoted"));
			hd.level = Integer.parseInt(tagArea.getAttribute("level"));
			hd.animations = tagArea.getAttribute("animations");

			if (tagArea.getAttribute("leader") != null)
				hd.leader = Boolean.parseBoolean(tagArea.getAttribute("leader"));

			// Arbitrarily choose 5 progression amounts and hope that there are never more then 3 special progressions...
			int listSize = 5;

			// Read base stats
			hd.move = new int[listSize];
			hd.movementType = new String[listSize];
			hd.attackStart = new int[listSize];
			hd.attackEnd = new int[listSize];
			hd.defenseStart = new int[listSize];
			hd.defenseEnd = new int[listSize];
			hd.speedStart = new int[listSize];
			hd.speedEnd = new int[listSize];
			hd.hpStart = new int[listSize];
			hd.hpEnd = new int[listSize];
			hd.mpStart = new int[listSize];
			hd.mpEnd = new int[listSize];
			hd.attackGain = new String[listSize];
			hd.defenseGain = new String[listSize];
			hd.speedGain = new String[listSize];
			hd.hpGain = new String[listSize];
			hd.mpGain = new String[listSize];
			hd.usuableWeapons = new int[listSize][];
			hd.className = new String[listSize];

			// Read non-standard stats
			hd.maxFireAffin = new int[listSize];
			hd.maxElecAffin = new int[listSize];
			hd.maxColdAffin = new int[listSize];
			hd.maxDarkAffin = new int[listSize];
			hd.maxWaterAffin = new int[listSize];
			hd.maxEarthAffin = new int[listSize];
			hd.maxWindAffin = new int[listSize];
			hd.maxLightAffin = new int[listSize];

			hd.maxBody = new AttributeStrength[listSize];
			hd.maxMind = new AttributeStrength[listSize];
			hd.maxCounter = new AttributeStrength[listSize];
			hd.maxEvade = new AttributeStrength[listSize];
			hd.maxDouble = new AttributeStrength[listSize];
			hd.maxCrit = new AttributeStrength[listSize];

			// Initialize spell stuff
			hd.spellsPerLevel = new ArrayList<ArrayList<int[]>>();
			hd.spellIds = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < listSize; i++) {
				hd.spellsPerLevel.add(new ArrayList<>());
				hd.spellIds.add(new ArrayList<>());
			}
			
			hd.items = new ArrayList<Integer>();
			hd.itemsEquipped = new ArrayList<Boolean>();

			hd.bodyProgression = new String[listSize];
			hd.mindProgression = new String[listSize];
			
			hd.promotedProgression = new boolean[listSize];
			hd.specialPromoted = new boolean[listSize];
			hd.specialPromotionItemId = new int[listSize];

			parseHeroProgressions(tagArea, hd);
			
			parseProgressionReliantFields(tagArea, hd);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			throw new BadResourceException("Unable to load hero statistics. Make sure that the Heroes\n"
					+ "is up to date by exporting heroes from the planner " + t.getMessage());
		}

		return hd;
	}

	private static void parseProgressionReliantFields(TagArea tagArea, HeroDefinition hd) {
		for (TagArea childTagArea : tagArea.getChildren()) {
			if (childTagArea.getTagType().equalsIgnoreCase("spellprogression"))
			{
				// Get the progression that is associated with these spells
				boolean spellPromoted = Boolean.parseBoolean(childTagArea.getAttribute("promotedprog"));
				boolean spellSpecialPromoted = Boolean.parseBoolean(childTagArea.getAttribute("specialpromoted"));
				if (spellSpecialPromoted)
					spellPromoted = false;
				boolean found = false;
				
				for (int associatedProgressionIndex = 0; associatedProgressionIndex < hd.spellIds.size(); associatedProgressionIndex++) {
					if (hd.className[associatedProgressionIndex] != null && 
						hd.promotedProgression[associatedProgressionIndex] == spellPromoted &&
						hd.specialPromoted[associatedProgressionIndex] == spellSpecialPromoted) {
						
						hd.spellIds.get(associatedProgressionIndex).add(childTagArea.getAttribute("spellid"));
						
						String[] splitSpell = childTagArea.getAttribute("gained").split(",");
						int[] splitLevel = new int[splitSpell.length];
						for (int i = 0; i < splitSpell.length; i++)
							splitLevel[i] = Integer.parseInt(splitSpell[i].trim());
						hd.spellsPerLevel.get(associatedProgressionIndex).add(splitLevel);
						
						found = true;
						break;
					}
				}
				
				if (!found)
					throw new BadResourceException("A spell progression was specified for " + hd.name + " that is not associated with "
							+ "any specified progression due to the settings of it's promoted/special promotion attributes");
			}
			// These are starting items and should be associated with the starting progression
			else if (childTagArea.getTagType().equalsIgnoreCase("item"))
			{
				hd.items.add(ItemResource.getItemIdByName(childTagArea.getAttribute("itemid")));
				if (childTagArea.getAttribute("equipped") != null)
					hd.itemsEquipped.add(Boolean.parseBoolean(childTagArea.getAttribute("equipped")));
				else
					hd.itemsEquipped.add(false);
			}
		}
	}

	private static void parseHeroProgressions(TagArea tagArea, HeroDefinition hd) {
		int index = 0;
		for (TagArea childTagArea : tagArea.getChildren())
		{
			if (childTagArea.getTagType().equalsIgnoreCase("progression"))
			{
				hd.promotedProgression[index] = Boolean.parseBoolean(childTagArea.getAttribute("promoted"));
				
				// Get special promotion indicators
				hd.specialPromoted[index] = Boolean.parseBoolean(childTagArea.getAttribute("specialpromoted"));
				if (hd.specialPromoted[index])
					hd.promotedProgression[index] = false;
				if (StringUtils.isNotEmpty(childTagArea.getAttribute("specialpromoteitem")))
					hd.specialPromotionItemId[index] = Integer.parseInt(childTagArea.getAttribute("specialpromoteitem"));
				
				hd.move[index] = Integer.parseInt(childTagArea.getAttribute("move"));
				hd.movementType[index] = childTagArea.getAttribute("movementtype");
				hd.attackGain[index] = childTagArea.getAttribute("attack");
				hd.attackStart[index] = Integer.parseInt(childTagArea.getAttribute("attackstart"));
				hd.attackEnd[index] = Integer.parseInt(childTagArea.getAttribute("attackend"));
				hd.defenseGain[index] = childTagArea.getAttribute("defense");
				hd.defenseStart[index] = Integer.parseInt(childTagArea.getAttribute("defensestart"));
				hd.defenseEnd[index] = Integer.parseInt(childTagArea.getAttribute("defenseend"));
				hd.speedGain[index] = childTagArea.getAttribute("speed");
				hd.speedStart[index] = Integer.parseInt(childTagArea.getAttribute("speedstart"));
				hd.speedEnd[index] = Integer.parseInt(childTagArea.getAttribute("speedend"));
				hd.hpGain[index] = childTagArea.getAttribute("hp");
				hd.hpStart[index] = Integer.parseInt(childTagArea.getAttribute("hpstart"));
				hd.hpEnd[index] = Integer.parseInt(childTagArea.getAttribute("hpend"));
				hd.mpGain[index] = childTagArea.getAttribute("mp");
				hd.mpStart[index] = Integer.parseInt(childTagArea.getAttribute("mpstart"));
				hd.mpEnd[index] = Integer.parseInt(childTagArea.getAttribute("mpend"));
				hd.className[index] = childTagArea.getAttribute("class");

				// Load affinities
				hd.maxFireAffin[index] = Integer.parseInt(childTagArea.getAttribute("fireAffin"));
				hd.maxElecAffin[index] = Integer.parseInt(childTagArea.getAttribute("elecAffin"));
				hd.maxColdAffin[index] = Integer.parseInt(childTagArea.getAttribute("coldAffin"));
				hd.maxDarkAffin[index] = Integer.parseInt(childTagArea.getAttribute("darkAffin"));
				hd.maxWaterAffin[index] = Integer.parseInt(childTagArea.getAttribute("waterAffin"));
				hd.maxEarthAffin[index] = Integer.parseInt(childTagArea.getAttribute("earthAffin"));
				hd.maxWindAffin[index] = Integer.parseInt(childTagArea.getAttribute("windAffin"));
				hd.maxLightAffin[index] = Integer.parseInt(childTagArea.getAttribute("lightAffin"));

				// Load body/mind
				hd.maxBody[index] = AttributeStrength.valueOf(childTagArea.getAttribute("bodyStrength"));
				hd.maxMind[index] = AttributeStrength.valueOf(childTagArea.getAttribute("mindStrength"));

				// Load body/mind progress
				hd.bodyProgression[index] = childTagArea.getAttribute("bodyProgress");
				hd.mindProgression[index] = childTagArea.getAttribute("mindProgress");

				// Load battle stats
				hd.maxCounter[index] = AttributeStrength.valueOf(childTagArea.getAttribute("counterStrength"));
				hd.maxEvade[index] = AttributeStrength.valueOf(childTagArea.getAttribute("evadeStrength"));
				hd.maxDouble[index] = AttributeStrength.valueOf(childTagArea.getAttribute("doubleStrength"));
				hd.maxCrit[index] = AttributeStrength.valueOf(childTagArea.getAttribute("critStrength"));


				String[] splitItems = childTagArea.getAttribute("usuableitems").split(",");
				int[] splitIds = new int[splitItems.length];
				for (int i = 0; i < splitItems.length; i++)
					splitIds[i] = Integer.parseInt(splitItems[i].trim());

				hd.usuableWeapons[index] = splitIds;
				index++;
			}
		}
	}

	public CombatSprite getHero()
	{
		/*
		leaderProgression = new HeroProgression(new int[][] {{KnownSpell.ID_BLAZE, 2, 6, 16, 27}},
				new Progression(new int[] {EquippableItem.STYLE_SWORD}, new int[] {EquippableItem.STYLE_LIGHT,
						EquippableItem.STYLE_MEDIUM, EquippableItem.STYLE_HEAVY}, 5, HeroProgression.STAT_AVERAGE,
						HeroProgression.STAT_AVERAGE, HeroProgression.STAT_AVERAGE, HeroProgression.STAT_AVERAGE,
						HeroProgression.STAT_AVERAGE),
				new Progression(new int[] {EquippableItem.STYLE_SWORD}, new int[] {EquippableItem.STYLE_LIGHT,
						EquippableItem.STYLE_MEDIUM, EquippableItem.STYLE_HEAVY}, 5, HeroProgression.STAT_AVERAGE,
						HeroProgression.STAT_AVERAGE, HeroProgression.STAT_AVERAGE, HeroProgression.STAT_AVERAGE,
						HeroProgression.STAT_AVERAGE));

		ArrayList<KnownSpell> spellDs;
		spellDs = new ArrayList<KnownSpell>();
		spellDs.add(new KnownSpell(KnownSpell.ID_HEAL, (byte) 1));
		spellDs.add(new KnownSpell(KnownSpell.ID_AURA, (byte) 1));


		heroes.add(new CombatSprite(true, "Kiwi1", "kiwi", leaderProgression, 12, 50, 12, 7, 8,
				leaderProgression.getUnpromotedProgression().getMove(), CombatSprite.MOVEMENT_ANIMALS_BEASTMEN, 1, 0, 0, spellDs));
		*/

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
		CombatSprite cs = new CombatSprite(leader, name, animations, heroProgression,
				level, 0, startsPromoted, id);

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

	private Progression getProgression(int index) {
		return new Progression(usuableWeapons[index], null, move[index], movementType[index],
				new Object[] {attackGain[index], attackStart[index], attackEnd[index]},
				new Object[] {defenseGain[index], defenseStart[index], defenseEnd[index]},
				new Object[] {speedGain[index], speedStart[index], speedEnd[index]},
				new Object[] {hpGain[index], hpStart[index], hpEnd[index]},
				new Object[] {mpGain[index], mpStart[index], mpEnd[index]},
				maxFireAffin[index], maxElecAffin[index], maxColdAffin[index], maxDarkAffin[index],
				maxWaterAffin[index], maxEarthAffin[index], maxWindAffin[index], maxLightAffin[index],
				maxCounter[index], maxEvade[index], maxDouble[index], maxCrit[index], maxBody[index], maxMind[index],
				bodyProgression[index], mindProgression[index], spellIds.get(index), spellsPerLevel.get(index),
				specialPromotionItemId[index], className[index]);
	}

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