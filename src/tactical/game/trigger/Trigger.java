package tactical.game.trigger;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.BattleCondMessage;
import tactical.engine.message.LoadMapMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.ShowCinMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.message.StringMessage;
import tactical.engine.state.StateInfo;
import tactical.game.ai.AI;
import tactical.game.constants.Direction;
import tactical.game.item.Item;
import tactical.game.resource.HeroResource;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.sprite.StaticSprite;
import tactical.game.text.Speech;
import tactical.map.MapObject;
import tactical.utils.StringUtils;

public class Trigger
{
	public static final int TRIGGER_NONE = -1;
	public static final int TRIGGER_ID_EXIT = -2;
	public static final int TRIGGER_ID_SAVE_AND_EXIT = -3;
	
	public static final int TRIGGER_CHEST_NO_ITEM = 50000;

	private ArrayList<Triggerable> triggerables = new ArrayList<Triggerable>();

	private String name;
	private boolean retrigOnEnter;
	private boolean nonRetrig;
	private boolean triggerOnce;
	private boolean triggerImmediately;
	private boolean triggered = false;
	private String[] requires;
	private String[] excludes;
	private int id;
	
	public enum TriggerStatus {
		TRIGGERED,
		REQUIRED_QUEST_NOT_DONE,
		EXCLUDED_QUEST_DONE,
		NON_RETRIG,
		TRIGGER_ONCE,
		IS_IMMEDIATE,
		COULD_NOT_TRIGGER
	}
	
	public Trigger() {}

	public Trigger(String name, int id, boolean retrigOnEnter, boolean nonRetrig,
			boolean triggerOnce, boolean triggerImmediately, String[] requires, String[] excludes) {
		super();
		this.name = name;
		this.retrigOnEnter = retrigOnEnter;
		this.nonRetrig = nonRetrig;
		this.triggerOnce = triggerOnce;
		this.triggerImmediately = triggerImmediately;
		this.id = id;
		this.requires = requires;
		this.excludes = excludes;
	}

	public void addTriggerable(Triggerable tt)
	{
		triggerables.add(tt);
	}

	public TriggerStatus perform(StateInfo stateInfo)
	{
		return perform(stateInfo, false);
	}

	public TriggerStatus perform(StateInfo stateInfo, boolean immediate)
	{
		Log.debug("Beginning Trigger Perform: " + this.id);
		if (triggerImmediately != immediate) {
			Log.debug("Trigger will not be executed, movement is immediate " + immediate + " trigger is immediate " + triggerImmediately);
			return TriggerStatus.IS_IMMEDIATE;
		}

		/* WHY IS THIS HERE?!??!?!?!
		if (!stateInfo.isInitialized() && this.id != 0) {
			Log.debug("Trigger will not be performed because the state has been changed");
			return;
		}
		*/

		// Check to see if this trigger meets all required quests
		if (requires != null)
		{
			for (String quest : requires)
			{
				if (StringUtils.isNotEmpty(quest) && !stateInfo.isQuestComplete(quest)) {
					Log.debug("Trigger will not be executed due to a failed requires " + quest);
					return TriggerStatus.REQUIRED_QUEST_NOT_DONE;
				}
			}
		}

		// Check to see if the excludes quests have been completed, if so
		// then we can't use this trigger
		if (excludes != null)
		{
			for (String quest : excludes)
			{
				if (StringUtils.isNotEmpty(quest) && stateInfo.isQuestComplete(quest)) {
					Log.debug("Trigger will not be executed due to a failed excludes " + quest);
					return TriggerStatus.EXCLUDED_QUEST_DONE;
				}
			}
		}

		if (nonRetrig)
		{
			if (stateInfo.getClientProgress().isNonretriggableTrigger(id))
			{
				// Check to see if this is a "on enter" perform for a retriggerable trigger that has already been triggered
				// If so we want this to be retriggered
				if (!stateInfo.isInitialized()) // && retrigOnEnter && stateInfo.getClientProgress().isPreviouslyTriggered(id))
				{
					Log.debug("Trigger will be performed on strange path");
					performTriggerImpl(stateInfo);
				}
				// The state has been changed and triggers should not be executed
				else {
					Log.debug("Trigger will not be performed because the state has been changed on strange path");
					return TriggerStatus.NON_RETRIG;
				}
			}
		}

		// Check if this is trigger once per map
		if (triggerOnce && triggered)
		{
			Log.debug("Trigger will not be triggered as it has already been triggered once");
			return TriggerStatus.TRIGGER_ONCE;
		}

		Log.debug("Trigger will be performed");
		if (!performTriggerImpl(stateInfo))
			return TriggerStatus.COULD_NOT_TRIGGER;
		
		if (triggerOnce)
			triggered = true;
		
		if (nonRetrig)
			stateInfo.getClientProgress().addNonretriggerableByMap(id);
		
		if (retrigOnEnter && !stateInfo.getClientProgress().isPreviouslyTriggered(id))
		{
			stateInfo.getClientProgress().addRetriggerableByMap(id);
		}
		
		return TriggerStatus.TRIGGERED;
	}

	private boolean performTriggerImpl(StateInfo stateInfo)
	{
		Iterator<Triggerable> tt = triggerables.iterator();

		while (tt.hasNext())
			// I wonder why we remove certain triggerables... (Currently just AI changes?)
			if (!tt.next().perform(stateInfo))
				return false;
		return true;
				
	}

	public class TriggerToggleQuest implements Triggerable
	{
		private String questId;
		private boolean complete = false;

		public TriggerToggleQuest(String questId, boolean complete) {
			super();
			this.questId = questId;
			this.complete = complete;
		}
		
		@Override
		public boolean perform(StateInfo stateInfo) {
			Log.debug("Completing Quest: " + questId);
			if (complete)
				stateInfo.sendMessage(new StringMessage(MessageType.COMPLETE_QUEST, questId));
			else
				stateInfo.sendMessage(new StringMessage(MessageType.UNCOMPLETE_QUEST, questId));
			return true;
		}
	}

	public class TriggerLoadMap implements Triggerable
	{
		private String mapData;
		private String location;
		private Direction transDir;

		public TriggerLoadMap(String mapData, String location, Direction transDir) {
			super();
			this.location = location;
			this.mapData = mapData;
			this.transDir = transDir;
		}

		@Override
		public boolean perform(StateInfo stateInfo)
		{
			stateInfo.sendMessage(new LoadMapMessage(MessageType.LOAD_MAP, mapData, location, transDir), true);
			return true;
		}
	}

	public class TriggerStartBattle implements Triggerable
	{
		private String battle;
		private String entrance;
		private int battleBG;

		public TriggerStartBattle(String battle, String entrance, int battleBG) {
			super();
			this.battle = battle;
			this.entrance = entrance;
			this.battleBG = battleBG;
		}

		@Override
		public boolean perform(StateInfo stateInfo)
		{
			stateInfo.sendMessage(new LoadMapMessage(MessageType.START_BATTLE, battle, entrance, battleBG), true);
			return true;
		}
	}

	public class TriggerBattleCond implements Triggerable
	{
		private int[] leaderIds;
		private int[] enemyLeaderIds;
		private boolean killAllLeaders;

		public TriggerBattleCond(int[] leaderIds, int[] enemyLeaderIds, boolean killAllLeaders) {
			super();
			this.leaderIds = leaderIds;
			this.killAllLeaders = killAllLeaders;
			this.enemyLeaderIds = enemyLeaderIds;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new BattleCondMessage(leaderIds, enemyLeaderIds, killAllLeaders), true);
			return true;
		}
	}

	public class TriggerShowShop implements Triggerable
	{
		private double buyPercent;
		private double sellPercent;
		private int[] itemIds;
		private String anims;

		public TriggerShowShop(String buyPercent, String sellPercent, int[] itemIds, String anim)
		{
			this.sellPercent = Double.parseDouble(sellPercent);
			this.buyPercent = Double.parseDouble(buyPercent);
			this.itemIds = itemIds;
			this.anims = anim;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new ShopMessage(buyPercent, sellPercent, itemIds, anims));
			return true;
		}
	}

	public class TriggerShowPriest implements Triggerable
	{
		private String anim;
		
		
		
		public TriggerShowPriest(String anim) {
			super();
			this.anim = anim;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new StringMessage(MessageType.SHOW_PRIEST, anim));
			return true;
		}
	}

	public class TriggerAddHero implements Triggerable
	{
		private int heroId;

		public TriggerAddHero(int heroId)
		{
			this.heroId = heroId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.getPersistentStateInfo().getClientProfile().addHero(HeroResource.getHero(heroId));
			return true;
		}
	}
	
	public class TriggerAddMultiHero implements Triggerable
	{
		private int[] heroIds;

		public TriggerAddMultiHero(int[] heroIds)
		{
			this.heroIds = heroIds;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new SpriteContextMessage(MessageType.SHOW_PANEL_MULTI_JOIN_CHOOSE, heroIds, null));
			return true;
		}
	}
	
	public class TriggerRemoveHero implements Triggerable
	{
		private int heroId;

		public TriggerRemoveHero(int heroId)
		{
			this.heroId = heroId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.getPersistentStateInfo().getClientProfile().removeHeroById(heroId);
			return true;
		}
	}

	public class TriggerById implements Triggerable
	{
		private int triggerId;

		public TriggerById(int triggerId)
		{
			this.triggerId = triggerId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.getResourceManager().getTriggerEventById(triggerId).perform(stateInfo);
			return true;
		}
	}

	public class TriggerPlayMusic implements Triggerable
	{
		private String song;
		private int volume;

		public TriggerPlayMusic(String song, int volume)
		{
			this.song = song;
			this.volume = volume;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new AudioMessage(MessageType.PLAY_MUSIC, song, volume / 100.f, true));
			return true;
		}

		public String getSong() {
			return song;
		}
	}
	
	public class TriggerPauseMusic implements Triggerable
	{
		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(MessageType.PAUSE_MUSIC);
			return true;
		}
	}
	
	public class TriggerResumeMusic implements Triggerable
	{
		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(MessageType.RESUME_MUSIC);
			return true;
		}
	}

	public class TriggerPlaySound implements Triggerable
	{
		private String song;
		private int volume;

		public TriggerPlaySound(String song, int volume)
		{
			this.song = song;
			this.volume = volume;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, song, volume / 100.0f, false));
			return true;
		}
	}

	public class TriggerShowCinematic implements Triggerable
	{
		private int cinematicId;
		private int exitTriggerId;

		public TriggerShowCinematic(int id, int exitTriggerId)
		{
			cinematicId = id;
			this.exitTriggerId = exitTriggerId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new ShowCinMessage(cinematicId, exitTriggerId), true);
			return true;
		}
	}

	public class TriggerLoadCinematic implements Triggerable
	{
		private String mapData;
		private int cinematicId;
		public TriggerLoadCinematic(String mapData, int id)
		{
			this.mapData = mapData;
			cinematicId = id;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.sendMessage(new LoadMapMessage(MessageType.LOAD_CINEMATIC, mapData, cinematicId), true);
			return true;
		}
	}

	public class TriggerShowText implements Triggerable
	{
		private int textId;
		private String message;
		private boolean immediate = false;

		public TriggerShowText(int textId)
		{
			this.textId = textId;
			message = null;
		}
		
		public TriggerShowText(int textId, boolean immediate)
		{
			this.textId = textId;
			message = null;
			this.immediate = immediate;
		}
		
		public TriggerShowText(String message) {
			this.message = message;
		}

		@Override
		public boolean perform(StateInfo stateInfo)
		{
			if (message == null)
				Speech.showFirstSpeechMeetsReqs(textId, stateInfo, immediate);
			else
				stateInfo.sendMessage(new SpeechMessage(message));
			return true;
		}
	}

	public class TriggerChangeAI implements Triggerable
	{
		private int speed;
		private int id;
		private int targetId;
		private int heroTargetId;
		private Point p = null;
		private int priority = 0;
		
		public TriggerChangeAI(String speed, String id, String targetId, 
				String heroTargetId, String x, String y, String priority)
		{
			this.id = Integer.parseInt(id);
			this.priority = Integer.parseInt(priority);
			if (targetId != null)
				this.targetId = Integer.parseInt(targetId);
			if (heroTargetId != null)
				this.heroTargetId = Integer.parseInt(heroTargetId);

			if (x != null && y != null)
			{
				p = new Point(Integer.parseInt(x), Integer.parseInt(y));
			}

			try
			{
				this.speed = Integer.parseInt(speed);
			}
			catch (NumberFormatException ex)
			{
				if (speed.equalsIgnoreCase("fast"))
					this.speed = AI.APPROACH_KAMIKAZEE;
				else if (speed.equalsIgnoreCase("slow"))
					this.speed = AI.APPROACH_HESITANT;
				else if (speed.equalsIgnoreCase("wait"))
					this.speed = AI.APPROACH_REACTIVE;
				else if (speed.equalsIgnoreCase("follow"))
					this.speed = AI.APPROACH_FOLLOW;
				else if (speed.equalsIgnoreCase("moveto"))
					this.speed = AI.APPROACH_MOVE_TO_POINT;
				else if (speed.equalsIgnoreCase("target"))
					this.speed = AI.APPROACH_TARGET;
			}
		}

		@Override
		public boolean perform(StateInfo stateInfo)
		{
			for (CombatSprite s : stateInfo.getCombatSprites())
			{
				if (s.getUniqueEnemyId() == id)
				{
					if (s.getAi().getPriority() > priority)
						continue;
					
					switch (speed)
					{
						case AI.APPROACH_FOLLOW:
							CombatSprite targetSprite = null;
							for (CombatSprite ts : stateInfo.getCombatSprites())
							{
								if (ts.getUniqueEnemyId() == targetId && ts.getCurrentHP() > 0)
								{
									targetSprite = ts;
									break;
								}
							}

							if (targetSprite != null)
							{
								s.getAi().setPriority(priority);
								s.getAi().setApproachType(speed, targetSprite);
								Log.debug("Follow sprite " + targetSprite.getName());
							}

							break;
						case AI.APPROACH_TARGET:
							CombatSprite target = stateInfo.getHeroById(heroTargetId);
							if (target.getCurrentHP() > 0)
							{
								s.getAi().setPriority(priority);
								s.getAi().setApproachType(speed, target);
								Log.debug("Target sprite " + target.getName());
							}
							break;
						case AI.APPROACH_MOVE_TO_POINT:
							Log.debug("Move to point " + p);
							s.getAi().setPriority(priority);
							s.getAi().setApproachType(speed, new Point(p.x * stateInfo.getTileWidth(), p.y * stateInfo.getTileHeight()));
							break;
						default:
							s.getAi().setPriority(priority);
							s.getAi().setApproachType(speed);
							break;
					}
				}
			}

			return true;
		}
	}

	public class TriggerToggleRoof implements Triggerable
	{
		private int roofId;
		private boolean show;

		public TriggerToggleRoof(int id, boolean showRoof)
		{
			roofId = id;
			this.show = showRoof;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			stateInfo.getResourceManager().getMap().getRoofById(roofId).setVisible(show);
			return true;
		}
	}
	
	public class TriggerRunTriggers implements Triggerable {
		private int[] triggers;

		public TriggerRunTriggers(int[] triggers) {
			super();
			this.triggers = triggers;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			for (int trig : triggers) {
				stateInfo.getResourceManager().getTriggerEventById(trig).perform(stateInfo);
			}
			return true;
		}
	}
	
	public class TriggerAddSprite implements Triggerable
	{
		private String spriteName;
		private String image;
		private int[] searchTriggers;
		private String locationName;
		private Sprite sprite = null;

		public TriggerAddSprite(String spriteName, String image, int[] searchTriggers, String locationName) {
			super();
			this.spriteName = spriteName;
			this.image = image;
			this.searchTriggers = searchTriggers;
			this.locationName = locationName;
		}
		
		public TriggerAddSprite(Sprite sprite) {
			this.sprite = sprite;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			if (sprite != null) {
					stateInfo.addSprite(sprite);
				} else {
					for (MapObject mo : stateInfo.getCurrentMap().getMapObjects()) {
						if (locationName.equalsIgnoreCase(mo.getName())) {
							stateInfo.addSprite(mo.getSprite(spriteName, image, searchTriggers, stateInfo.getResourceManager()));
							break;
						}
					}
				}
			return true;
		}
	}
	
	public class TriggerChangeNPCAnimation implements Triggerable {
		private String animation;
		private String npcName;
		
		public TriggerChangeNPCAnimation(String animation, String npcName) {
			super();
			this.animation = animation;
			this.npcName = npcName;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			for (Sprite sprite : stateInfo.getSprites()) {
				if (sprite.getSpriteType() == Sprite.TYPE_NPC &&
						npcName.equalsIgnoreCase(sprite.getName())) {
					NPCSprite npc = (NPCSprite) sprite;
					npc.setSpriteAnims(stateInfo.getResourceManager().getSpriteAnimation(animation));
					npc.setFacing(npc.getFacing());
					break;
				}
			}
				
			return true;
		}
		
	}
	
	public class TriggerAddNpc implements Triggerable
	{
		private int textId;
		private String name;
		private String animation;
		private Integer facing;
		private Integer wander;
		private Integer npcId;
		private String locationName;

		public TriggerAddNpc(int textId, String name, String animation, Integer facing, Integer wander, Integer npcId,
				String locationName) {
			super();
			this.textId = textId;
			this.name = name;
			this.animation = animation;
			this.facing = facing;
			this.wander = wander;
			this.npcId = npcId;
			this.locationName = locationName;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			for (MapObject mo : stateInfo.getCurrentMap().getMapObjects()) {
				if (locationName.equalsIgnoreCase(mo.getName())) {
					stateInfo.addSprite(mo.getNPC(textId, name, animation, 
							facing, wander, npcId, false, true, true, stateInfo.getResourceManager()));
					break;
				}
			}
			return true;
		}
	}

	public class TriggerRemoveSprite implements Triggerable
	{
		private String spriteName;

		public TriggerRemoveSprite(String spriteName) {
			super();
			this.spriteName = spriteName;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			Iterator<Sprite> spriteIt = stateInfo.getSpriteIterator();
			while (spriteIt.hasNext())
			{
				Sprite s = spriteIt.next();

				if (s.getName() != null && s.getName().equalsIgnoreCase(spriteName))
				{
					spriteIt.remove();
					return true;
				}
			}
			return true;
		}
	}

	public class TriggerChangeSprite implements Triggerable
	{
		private String spriteName;
		private String newImage;

		public TriggerChangeSprite(String spriteName, String newImage) {
			super();
			this.spriteName = spriteName;
			this.newImage = newImage;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			Iterator<Sprite> spriteIt = stateInfo.getSpriteIterator();
			while (spriteIt.hasNext())
			{
				Sprite s = spriteIt.next();

				if (s.getName() != null && s.getName().equalsIgnoreCase(spriteName))
				{
					StaticSprite ss = (StaticSprite) s;
					ss.setImage(stateInfo.getResourceManager().getImage(newImage));
					return true;
				}
			}
			return true;
		}
	}

	public class TriggerAddItem implements Triggerable
	{
		private int itemId;
		private Triggerable onFailureTriggerable = null;

		public TriggerAddItem(int itemId) {
			super();
			this.itemId = itemId;
		}
		
		public TriggerAddItem(int itemId, Triggerable onFailureTriggerable) {
			super();
			this.itemId = itemId;
			this.onFailureTriggerable = onFailureTriggerable;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			boolean given = false;
			for (CombatSprite hero : stateInfo.getAllHeroes())
			{
				if (hero.getItemsSize() != 4)
				{
					Item item = ItemResource.getItem(itemId, stateInfo.getResourceManager());
					hero.addItem(item);
					stateInfo.sendMessage(new SpeechMessage(
							TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getItemRecievedText(hero.getName(), item.getName())));
					given = true;
					break;
				}
			}
			
			if (!given && onFailureTriggerable != null) {
				onFailureTriggerable.perform(stateInfo);
			}

			return given;
		}
	}

	public class TriggerExit implements Triggerable
	{
		@Override
		public boolean perform(StateInfo stateInfo) {
			System.exit(0);
			return true;
		}

	}
	
	public class TriggerReviveHeroes implements Triggerable
	{
		private String spriteToRev = null;
		
		// Create a trigger to revive a specific hero or all
		// heroes if the specified sprite name is null
		public TriggerReviveHeroes(String spriteToRev) {
			super();
			this.spriteToRev = spriteToRev;
		}
		
		@Override
		public boolean perform(StateInfo stateInfo) {
			for (CombatSprite cs : stateInfo.getAllHeroes())
				if (spriteToRev == null || cs.getName().equalsIgnoreCase(spriteToRev))
					cs.setCurrentHP(cs.getMaxHP());
			return true;
		}
	}
	
	public class TriggerKillEnemies implements Triggerable
	{
		private int unitId;
		
		public TriggerKillEnemies(int unitId) {
			this.unitId = unitId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			for (CombatSprite cs : stateInfo.getCombatSprites()) {
				if (!cs.isHero() && cs.getUniqueEnemyId() == unitId) {
					cs.setCurrentHP(-1);
				}
			}
			
			return true;
		}	
	}
	
	public class TriggerNPCSpeech implements Triggerable
	{
		private String npcName;
		
		public TriggerNPCSpeech(String npcName) {
			super();
			this.npcName = npcName;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			for (Sprite s : stateInfo.getSprites()) {
				if (s.getSpriteType() == Sprite.TYPE_NPC && npcName.equalsIgnoreCase(s.getName())) {
					((NPCSprite) s).triggerButton1Event(stateInfo);
				}
			}
			return true;
		}
		
		
	}
	
	public class TriggerAddSearchArea implements Triggerable
	{
		private MapObject mapObject;
		private int searchTriggerId;
		
		public TriggerAddSearchArea(MapObject mapObject, int searchTriggerId) {
			super();
			this.mapObject = mapObject;
			this.searchTriggerId = searchTriggerId;
		}

		@Override
		public boolean perform(StateInfo stateInfo) {
			mapObject.getParams().put("searchtrigger", "" + searchTriggerId);
			mapObject.establishSearchArea(stateInfo.getResourceManager());
			return true;
		}
		
		
	}
	
	public class TriggerSave implements Triggerable {

		@Override
		public boolean perform(StateInfo stateInfo) {
			if (stateInfo.isCombat())
				stateInfo.saveBattle();
			else
				stateInfo.save();;
			return true;
		}
		
	}

	public ArrayList<Triggerable> getTriggerables() {
		return triggerables;
	}

	public String getName() {
		return name;
	}
}
