package tactical.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.ai.AI;
import tactical.game.ai.ClericAI;
import tactical.game.ai.WarriorAI;
import tactical.game.ai.WizardAI;
import tactical.game.constants.Direction;
import tactical.game.item.Item;
import tactical.game.resource.EnemyResource;
import tactical.game.resource.ItemResource;
import tactical.game.resource.NPCResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.Door;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.sprite.StaticSprite;
import tactical.game.trigger.Trigger;
import tactical.game.trigger.TriggerCondition;
import tactical.loading.ResourceManager;

public class MapObject
{
	private int width, height, x, y;
	private String name = null;
	private String key;
	private ArrayList<Point> polyPoints = null;
	private Hashtable<String, String> params;
	private Shape shape;

	public MapObject()
	{
		params = new Hashtable<String, String>();
	}

	public void determineShape()
	{
		if (polyPoints == null)
			shape = new Rectangle(x, y, width, height);
		else
		{
			float[] points = new float[polyPoints.size() * 2];
			for (int i = 0; i < polyPoints.size(); i++)
			{
				points[2 * i] = polyPoints.get(i).x + x;
				points[2 * i + 1] = polyPoints.get(i).y + y;
			}
			shape = new Polygon(points);
			x = (int) shape.getX();
			y = (int) shape.getY();
			width = (int) shape.getWidth();
			height = (int) shape.getHeight();
		}
	}

	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setValue(String value)
	{
		if (value != null && value.length() > 0)
		{
			String[] splitParams = value.split(" ");
			for (int i = splitParams.length - 1; i >= 0; i--)
			{
				if (splitParams[i].endsWith("="))
					continue;
				String[] attributes = splitParams[i].split("=");
				
				// Handle the case where we've parsed something with a space in it
				if (attributes.length == 1) {
					splitParams[i - 1] = splitParams[i - 1] + " " + splitParams[i];
					continue;
				}
				params.put(attributes[0], attributes[1]);
			}
		}
	}

	public void setPolyPoints(ArrayList<Point> polyPoints) {
		this.polyPoints = polyPoints;
	}

	public Shape getShape()
	{
		return shape;
	}

	public String getParam(String param)
	{
		return params.get(param);
	}

	public Hashtable<String, String> getParams() {
		return params;
	}

	public void placeSpritesAtStartLocation(StateInfo stateInfo)
	{
		int startX = 0;
		int startY = 0;

		Iterator<Sprite> spritesItr = stateInfo.getSpriteIterator();
		boolean getOnNext = true;
		Sprite sprite = null;

		while (spritesItr.hasNext())
		{
			if (getOnNext)
				sprite = spritesItr.next();
			getOnNext = true;

			if (sprite.getSpriteType() == Sprite.TYPE_COMBAT && ((CombatSprite) sprite).isHero() && sprite.getLocX() == -1)
			{
				if (shape.contains(x + startX + 1, y + startY + 1))
				{
					CombatSprite cs = ((CombatSprite) sprite);
					Direction facing = cs.getFacing();
					cs.setLocation((x + startX), (y + startY), stateInfo.getTileWidth(), stateInfo.getTileHeight());
					if (stateInfo.isCombat() || facing == null)
						cs.setFacing(Direction.DOWN);
					else
						cs.setFacing(facing);
					
				}
				else
					getOnNext = false;

				startX += stateInfo.getTileWidth();
				if (startX == shape.getWidth())
				{
					startX = 0;
					startY += stateInfo.getTileHeight();
					if (startY == shape.getHeight())
						break;
				}
			}
		}
	}

	public NPCSprite getNPC(ResourceManager fcrm)
	{
		String animation = params.get("animation");
		Integer wander = null;
		if (params.containsKey("wander"))
			wander = Integer.parseInt(params.get("wander"));
		
		Integer uniqueId = null;
		if (params.get("npcid") != null)
			uniqueId = Integer.parseInt(params.get("npcid"));
		Integer facing = null;
		if (params.containsKey("facing")) {
			facing = Integer.parseInt(params.get("facing"));
		}
		
		boolean throughWall = false;
		if (params.containsKey("throughwall")) {
			throughWall = Boolean.parseBoolean(params.get("throughwall"));
		}
		
		boolean turnOnTalk = true;
		boolean animate = true;
		
		if (params.containsKey("noanimate")) {
			animate = !Boolean.parseBoolean(params.get("noanimate"));
		}
		
		if (params.containsKey("noturn")) {
			turnOnTalk = !Boolean.parseBoolean(params.get("noturn"));
		}
		
		return getNPC(Integer.parseInt(params.get("textid")), params.get("name"), 
				animation, facing, wander, uniqueId, throughWall, animate, turnOnTalk, fcrm);
	}
	
	public NPCSprite getNPC(int textId, String name, String animation, Integer facing, 
			Integer wander, Integer npcId, boolean throughWall,
			boolean animate, boolean turnOnTalk, ResourceManager fcrm) {
		NPCSprite npc = NPCResource.getNPC(animation, textId, name, throughWall, animate, turnOnTalk);
		npc.initializeSprite(fcrm);
		
		int wanderVal = 0;
		Direction facingVal = Direction.DOWN;
		if (wander != null)
			wanderVal = wander;
		if (facing != null && facing != -1)
			facingVal = Direction.values()[facing];
		
		npc.setInitialPosition(x, y, fcrm.getMap().getTileEffectiveWidth(), 
				fcrm.getMap().getTileEffectiveHeight(), wanderVal, facingVal);
		
		if (npcId != null)
			npc.setUniqueNPCId(npcId);
		return npc;
	}

	public CombatSprite getEnemy(ResourceManager fcrm)
	{
		CombatSprite enemy = EnemyResource.getEnemy(params.get("enemyid"));
		if (params.containsKey("ai"))
		{
			String type = params.get("ai");
			String approach = params.get("aiapproach");
			String music = params.get("music");

			int id = 0;
			if (params.containsKey("unit"))
				id = Integer.parseInt(params.get("unit"));

			int approachIndex = 0;
			if (approach.equalsIgnoreCase("fast"))
				approachIndex = AI.APPROACH_KAMIKAZEE;
			else if (approach.equalsIgnoreCase("slow"))
				approachIndex = AI.APPROACH_HESITANT;
			else if (approach.equalsIgnoreCase("wait"))
				approachIndex = AI.APPROACH_REACTIVE;
			else if (approach.equalsIgnoreCase("wander"))
				approachIndex = AI.APPROACH_WANDER;

			int vision = Integer.MAX_VALUE;
			if (params.containsKey("vision") && params.get("vision") != null &&
					params.get("vision").length() > 0 && Integer.parseInt(params.get("vision")) != 0)					
				vision = Integer.parseInt(params.get("vision"));
			
			if (type.equalsIgnoreCase("wizard"))
				enemy.setAi(new WizardAI(approachIndex, vision));
			else if (type.equalsIgnoreCase("cleric"))
				enemy.setAi(new ClericAI(approachIndex, vision));
			else if (type.equalsIgnoreCase("fighter"))
				enemy.setAi(new WarriorAI(approachIndex, vision));

			if (id != -1)
				enemy.setUniqueEnemyId(id);
			
			if (music != null)
				enemy.setCustomMusic(music);
		}

		enemy.initializeSprite(fcrm);
		enemy.getAi().initialize(enemy);
		enemy.setLocX(x, fcrm.getMap().getTileEffectiveWidth());
		enemy.setLocY(y, fcrm.getMap().getTileEffectiveHeight());

		return enemy;
	}

	public Sprite getSprite(ResourceManager fcrm)
	{
		String name = params.get("name");
		String image = params.get("image");

		int[] trigger = null;
		if (params.containsKey("searchtrigger"))
		{
			String[] split = params.get("searchtrigger").split(",");
			trigger = new int[split.length];
			for (int i = 0; i < split.length; i++)
				trigger[i] = Integer.parseInt(split[i]);
		}

		return getSprite(name, image, trigger, fcrm);
	}
	
	public Sprite getSprite(String name, String imageName, int[] trigger, ResourceManager fcrm)
	{
		Image image = fcrm.getImage(imageName);
		
		Sprite s = new StaticSprite(x, y, name, image, trigger);
		s.initializeSprite(fcrm);
		s.setLocX(x, fcrm.getMap().getTileEffectiveWidth());
		s.setLocY(y, fcrm.getMap().getTileEffectiveHeight());
		return s;
	}

	public void establishSearchArea(ResourceManager fcrm)
	{
		int[] trigger = null;
		if (params.containsKey("searchtrigger"))
		{
			String[] split = params.get("searchtrigger").split(",");
			trigger = new int[split.length];
			for (int i = 0; i < split.length; i++)
				trigger[i] = Integer.parseInt(split[i]);
		}
		
		if (trigger == null || trigger.length == 0) {
			JOptionPane.showMessageDialog(null, "A searcharea (" + name + ")with no search trigger is defined was found at location " + x + " " + y);
		} else {		
			TriggerCondition tc = new TriggerCondition(trigger[0], "");
			tc.addCondition(new TriggerCondition.LocationSearched(name));
			fcrm.addTriggerCondition(tc);
		}
	}
	
	public Sprite establishChest(int triggerId1, int triggerId2, ResourceManager fcrm) {
		// parse sprite image
		// setup chest sprite
		// create trigger
		// 1. give item
		// 2. remove sprite
		// add search vondition
		
		String spriteImage = params.get("spriteimage");
		String itemString = params.get("itemid");
		Item item = null;
		if (itemString != null && itemString.trim().length() > 0 && !itemString.equalsIgnoreCase("-1")) {
			item = ItemResource.getItem(Integer.parseInt(itemString), fcrm);
		}
		
		Trigger searchTrigger1 = new Trigger("SearchChest" + triggerId1, triggerId1, true, false, true, false,
				 null, null);
		Trigger searchTrigger2 = new Trigger("SearchChest" + triggerId2, triggerId2, false, 
				true, true, false, null, null);
		
		StaticSprite chestSprite = new StaticSprite(x, y, name, fcrm.getImage(spriteImage), new int[] {triggerId2} );
		chestSprite.setLocX(x, fcrm.getMap().getTileEffectiveWidth());
		chestSprite.setLocY(y, fcrm.getMap().getTileEffectiveHeight());
		chestSprite.setOffsetUp(false);
		searchTrigger1.addTriggerable(searchTrigger1.new TriggerRemoveSprite(name));
		searchTrigger1.addTriggerable(searchTrigger1.new TriggerAddSearchArea(this, Trigger.TRIGGER_CHEST_NO_ITEM));
		if (item != null) {
			Trigger t = new Trigger();
			
			searchTrigger2.addTriggerable(searchTrigger2.new TriggerAddItem(item.getItemId(), 
					t.new TriggerShowText(TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getItemInChestTextNoRoom(item.getName()))));
			searchTrigger2.addTriggerable(searchTrigger2.new TriggerShowText(
					TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getItemInChestText(item.getName())));
			searchTrigger2.addTriggerable(searchTrigger2.new TriggerRunTriggers(new int[] {triggerId1}));
		} else {
			searchTrigger2.addTriggerable(searchTrigger2.new TriggerShowText(TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getNoItemInChestText()));
			searchTrigger2.addTriggerable(searchTrigger2.new TriggerRunTriggers(new int[] {triggerId1}));
		}
		fcrm.addTriggerEvent(triggerId1, searchTrigger1);
		fcrm.addTriggerEvent(triggerId2, searchTrigger2);
		
		return chestSprite;
	}

	public Sprite getDoor(ResourceManager fcrm, int doorId)
	{
		Image image = fcrm.getImage(params.get("image"));

		Sprite s = new Door(doorId, x, y, image);
		s.initializeSprite(fcrm);
		s.setLocX(x, fcrm.getMap().getTileEffectiveWidth());
		s.setLocY(y, fcrm.getMap().getTileEffectiveHeight());
		return s;
	}
	
	public boolean contains(int mapX, int mapY)
	{
		return shape.contains(mapX + .1f, mapY + .1f);
	}
	
	public boolean contains(CombatSprite cs)
	{
		return shape.contains(cs.getLocX() + .1f, cs.getLocY() + .1f);
	}
}
