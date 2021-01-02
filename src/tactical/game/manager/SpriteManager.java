package tactical.game.manager;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import tactical.engine.TacticalGame;
import tactical.engine.config.MusicConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.BooleanMessage;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.game.constants.Direction;
import tactical.game.definition.EnemyDefinition;
import tactical.game.dev.BattleOptimizer;
import tactical.game.exception.BadMapException;
import tactical.game.exception.BadResourceException;
import tactical.game.item.Item;
import tactical.game.resource.NPCResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.sprite.StaticSprite;
import tactical.map.MapObject;

public class SpriteManager extends Manager
{
	private int updateDelta = 0;
	private static final int UPDATE_TIME = 50;
	private static BattleOptimizer battleOptimizer = new BattleOptimizer();

	@Override
	public void initialize()
	{
		
	}

	private void initializeAfterSprites(boolean fromLoad)
	{
		EnemyDefinition.resetEnemyIds();
		NPCResource.resetNPCIds();

		List<CombatSprite> heroesInState = stateInfo.getClientProfile().getHeroesInParty();
		
		// If we are not in combat then get this clients main character and set them as the current sprite
		if (!stateInfo.isCombat())
		{
			for (CombatSprite cs : heroesInState)
			{
				if (cs.isLeader())
				{
					if (cs.getClientId() == stateInfo.getPersistentStateInfo().getClientId())
					{
						stateInfo.setCurrentSprite(cs);
					}
					stateInfo.addSprite(cs);
				}
			}

			// Even though this is not the leader, the sprites need to be initialized so we can
			// view items, spells and pictures
			for (CombatSprite cs : stateInfo.getAllHeroes())
			{
				Direction facing = cs.getFacing();
				cs.initializeSprite(stateInfo.getResourceManager());
				cs.initializeStats();
				if (facing != null)
					cs.setFacing(facing);
				else
					cs.setFacing(Direction.DOWN);
			}

			boolean foundStart = false;
			String entranceLocation = stateInfo.getEntranceLocation();

			// Get any npcs from the map
			for (MapObject mo : stateInfo.getResourceManager().getMap().getMapObjects())
			{
				if (mo.getKey().equalsIgnoreCase("npc"))
				{
					stateInfo.addSprite(mo.getNPC(stateInfo.getResourceManager()));
				}
				// Depending on where we are initializing this map from the entrance location could be null.
				// If we are loading a map for the first time or via transition from another map then this
				// value will be set. If we are loading from save then we will just use the absolute sprite
				// absolute sprite location
				else if (entranceLocation != null && mo.getKey().equalsIgnoreCase("start")
						&& mo.getParam("exit").equalsIgnoreCase(entranceLocation))
				{
					mo.placeSpritesAtStartLocation(stateInfo);
					foundStart = true;
				}
			}
			
			if (entranceLocation == null) {
				Point savedPoint = stateInfo.getClientProgress().getInTownLocation();
				if (savedPoint == null)
					throw new BadMapException("The selected map does not contain a start location or a start point. Your save file may be bad");
				
				// Use integer division to place the hero at the nearest tile
				stateInfo.getCurrentSprite().setLocation((savedPoint.x / stateInfo.getTileWidth()) * stateInfo.getTileWidth(), 
						(savedPoint.y / stateInfo.getTileHeight()) * stateInfo.getTileHeight(), stateInfo.getTileWidth(), stateInfo.getTileHeight());
			}
			else if (!foundStart)
			{
				throw new BadMapException("The selected map does not contain a start location with the name " + entranceLocation);
			}

			stateInfo.getCamera().centerOnSprite(stateInfo.getCurrentSprite(), stateInfo.getCurrentMap());
		}
		// Otherwise just add all of the heroes
		else
		{
			// Initialize all of the heroes, this just sets images and initializes effects.
			// Stats are initialized above for all heroes
			for (CombatSprite cs : stateInfo.getAllHeroes())
				cs.initializeSprite(stateInfo.getResourceManager());
			
			
			// Regardless of whether or not this is a loaded mid-way battle
			// or a new battle, initialize the hero list, do NOT initialize
			// the stats
			for (CombatSprite cs : heroesInState)
			{
				// Do not allow dead sprites to appear in the battle
				if (cs.getCurrentHP() <= 0)
					continue;

				// Only initialize stats if this is a "new" battle
				if (!fromLoad)
					cs.initializeStats();
			}

			// Since this battle is not loaded we need to add all of the heroes and enemies
			// to the state and place them in their starting locations
			if (!fromLoad) 
			{
				stateInfo.addAllCombatSprites(heroesInState);

				// Get any npcs from the map
				for (MapObject mo : stateInfo.getResourceManager().getMap().getMapObjects())
				{
					// TODO This should automatically start the "BATTLE START"
					if (mo.getKey().equalsIgnoreCase("start") && mo.getParam("exit").equalsIgnoreCase(stateInfo.getEntranceLocation()))
					{
						mo.placeSpritesAtStartLocation(stateInfo);
					}
					
					if (mo.getKey().equalsIgnoreCase("enemy"))
					{
						CombatSprite cs = mo.getEnemy(stateInfo.getResourceManager());
						
						if (TacticalGame.BATTLE_MODE_OPTIMIZE)
							battleOptimizer.modifyStats(cs);
							
						// Enemies do not need to be initialized as it was 
						// already done via the .getEnemy() method above
						cs.initializeStats();
						stateInfo.addSprite(cs);
					}
					
					// We purposely don't remove the combat sprites until this point so that their
					// spots are still taken up in the start location (as empty spaces). Otherwise
					// it could completely fuck up split battles... but so could less people in the party...
					Iterator<CombatSprite> csIt = heroesInState.iterator();
					while (csIt.hasNext())
					{
						CombatSprite cs = csIt.next();
						if (cs.getCurrentHP() <= 0)
							csIt.remove();
					}
				}
				
				if (heroesInState.stream().anyMatch(hs -> hs.getLocX() == -1))
					throw new BadResourceException("The current battle does not have enough start locations to place all of the heroes currently in the party."
							+ " Ensure that you have the expected amount of members in the party and that the start trigger refers to the correct start location");
				
				if (TacticalGame.BATTLE_MODE_OPTIMIZE)
					battleOptimizer.startBattle();
			// Since the enemies already exist in the combat sprite list
			// we don't need to add them, but they do need to be "initialized"s
			} else {
				// Initialize all of the heroes, this just sets images and initializes effects.
				// Stats are initialized above for all heroes
				for (CombatSprite cs : stateInfo.getAllHeroes())
					cs.initializeSprite(stateInfo.getResourceManager());
				
				int nextEnemyId = 0;
				for (CombatSprite cs : stateInfo.getCombatSprites())
				{
					if (!cs.isHero())
					{
						cs.initializeSprite(stateInfo.getResourceManager());
						// The target location of AI may be incorrect, fix that here
						if (cs.getAi() != null)
							cs.getAi().reinitialize(stateInfo);
						nextEnemyId = Math.min(nextEnemyId, cs.getId());
					}
				}
				EnemyDefinition.setNextEnemyId(nextEnemyId - 1);
			}
				

			/*
			AIController aic = new AIController();
			aic.initialize(stateInfo.getCombatSprites());
			*/
		}
	}

	public void update(int delta)
	{
		updateDelta += delta;
		while (updateDelta >= UPDATE_TIME)
		{
			updateDelta -= UPDATE_TIME;
			boolean isEnemyAlive = false;

			// TODO Is this too cumbersome, could move it when people move around the map?
			stateInfo.sortSprites();
			Iterator<Sprite> spriteItr = stateInfo.getSpriteIterator();
			while (spriteItr.hasNext())
			{
				Sprite s = spriteItr.next();
				s.update(stateInfo);
				if (s.getSpriteType() == Sprite.TYPE_COMBAT)
				{
					CombatSprite cs = (CombatSprite) s;
					if (cs.getCurrentHP() <= 0) {
						if (cs.getCurrentHP() != Integer.MIN_VALUE) {
							cs.setCurrentHP(Integer.MIN_VALUE);
							cs.setAlpha(255);
						} else
							cs.setAlpha(cs.getAlpha() - delta);
					}
					
					isEnemyAlive = handleSpriteDying(isEnemyAlive, spriteItr, s, cs);
				}
			}

			// If we are in battle and there are no enemies alive then the
			// battle has been won! Trigger the default "battle over" trigger
			if (TacticalGame.BATTLE_MODE_OPTIMIZE && !isEnemyAlive && stateInfo.isCombat())
			{
				loadBattleOptmize(true);
			}
		}
	}

	private boolean handleSpriteDying(boolean isEnemyAlive, Iterator<Sprite> spriteItr, Sprite s, CombatSprite cs) {
		if (cs.getAlpha() <= 0)
		{
			// Add dropped items to deals
			if (!cs.isHero()) {
				for (int itemInd = 0; itemInd < cs.getItemsSize(); itemInd++) {
					Item item = cs.getItem(itemInd);
					if (item.isDeal()) {
						stateInfo.getClientProgress().getDealItems().add(item.getItemId());
					}
				}
			}
			
			cs.setCurrentHP(0);
			spriteItr.remove();
			stateInfo.removeCombatSprite(cs);
			MusicConfiguration musicSelector = TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration();
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, 
					musicSelector.getSpriteDeathOnMapSoundEffect(cs.getName()), 
					1.0f, false));
			
			s.destroy(stateInfo);

			if (TacticalGame.BATTLE_MODE_OPTIMIZE && cs.isLeader())
			{
				// If it's a hero then we've lost
				loadBattleOptmize(!cs.isHero());
			}
			
			if (cs.isHero())
				stateInfo.checkTriggersHeroDeath(cs);
			else
				stateInfo.checkTriggersEnemyDeath(cs);
		}
		// If the sprite did not die, then check to see if it is an enemy, if so the battle is not over
		else if (!((CombatSprite) s).isHero())
			isEnemyAlive = true;
		return isEnemyAlive;
	}
	
	private void loadBattleOptmize(boolean won)
	{
		// Set everyones hit points to full so that no one starts dead
		for (CombatSprite cs : stateInfo.getAllHeroes())
			cs.setCurrentHP(cs.getMaxHP());
		if (won)
			this.battleOptimizer.wonBattle();
		else
			this.battleOptimizer.lostBattle();
		stateInfo.getPersistentStateInfo().loadBattle(stateInfo.getClientProgress().getMapData(), 
				stateInfo.getPersistentStateInfo().getEntranceLocation(), 0);
	}

	@Override
	public void recieveMessage(Message message)
	{
		switch (message.getMessageType())
		{
			case INTIIALIZE_MANAGERS:
				initializeAfterSprites(((BooleanMessage) message).isBool());
				break;
			case INVESTIGATE:				
				handleInvestigate();
				break;
			case MENU_CLOSED:
				for (Sprite s : stateInfo.getSprites()) {
					if (s.getSpriteType() == Sprite.TYPE_NPC) {
						NPCSprite npc = (NPCSprite) s;
						if (npc.isWaitingForSpeechToEnd()) {
							npc.setWaitingForSpeechToEnd(false);
							npc.setFacing(npc.getOriginalFacing());
						}
					}
				}
				break;
			default:
				break;
		}
	}

	private void handleInvestigate() {
		int checkX = stateInfo.getCurrentSprite().getTileX();
		int checkY = stateInfo.getCurrentSprite().getTileY();
		int checkX2 = checkX;
		int checkY2 = checkY;
		
		switch (stateInfo.getCurrentSprite().getFacing())
		{
			case UP:
				checkY--;
				checkY2 -= 2;
				break;
			case DOWN:
				checkY++;
				checkY2 += 2;
				break;
			case LEFT:
				checkX--;
				checkX2 -= 2;
				break;
			case RIGHT:
				checkX++;
				checkX2 += 2;
				break;
		}

		boolean checkThroughWall = false;
		if (!stateInfo.getCurrentMap().isMarkedMoveable(checkX, checkY))
			checkThroughWall = true;
		for (Sprite s : stateInfo.getSprites())
		{
			if (s.getSpriteType() == Sprite.TYPE_NPC)
			{
				NPCSprite npc = (NPCSprite) s;
				if (npc.getTileX() == checkX &&
						npc.getTileY() == checkY || (checkThroughWall && 
								npc.isThroughWall() &&
								npc.getTileX() == checkX2 &&
								npc.getTileY() == checkY2))
				{
					npc.triggerButton1Event(stateInfo);
					break;
				}
			}
			else if (s.getSpriteType() == Sprite.TYPE_STATIC_SPRITE)
			{
				StaticSprite ss = (StaticSprite) s;
				if (s.getTileX() == checkX &&
						ss.getTileY() == checkY)
				{
					ss.triggerButton1Event(stateInfo);
					break;
				}
			}
		}
	}
}
