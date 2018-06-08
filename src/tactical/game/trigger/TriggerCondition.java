package tactical.game.trigger;

import java.util.ArrayList;
import java.util.List;

import tactical.engine.state.StateInfo;
import tactical.game.sprite.CombatSprite;
import tactical.map.MapObject;

public class TriggerCondition {
	private int triggerId;
	private String description;
	private List<Conditional> conditions = new ArrayList<>();
	
	public TriggerCondition(int triggerId, String description) {
		super();
		this.triggerId = triggerId;
		this.description = description;
	}

	public void addCondition(Conditional c)
	{
		this.conditions.add(c);
	}
	
	public boolean executeCondtions(String location, boolean locationEntered, boolean immediate,
			boolean onMapLoad, boolean searching, StateInfo stateInfo)
	{
		for (Conditional c : conditions)
			if (!c.conditionIsMet(location, locationEntered, immediate, onMapLoad, searching, stateInfo))
				return false;
		stateInfo.getResourceManager().getTriggerEventById(triggerId).perform(stateInfo, immediate);
		return true;
	}
	
	public static class EnemiesRemaining implements Conditional {
		private int amount;
		private String operator;
		
		public EnemiesRemaining(int amount, String operator) {
			super();
			this.amount = amount;
			this.operator = operator;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			int count = 0;
			for (CombatSprite cs : stateInfo.getCombatSprites())
			{
				if (!cs.isHero())
				{
					count++;
				}
			}
			
			if (operator.equalsIgnoreCase("Greater Than"))
			{
				return count > amount;
					
			}
			else if (operator.equalsIgnoreCase("Less Than"))
			{
				return count < amount;
			}
			else if (operator.equalsIgnoreCase("Equals"))
			{
				return count == amount;
			}
			return false;
		}
	}
	
	public static class MapLoaded implements Conditional {
		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			return onMapLoad;
		}
	}
	
	public static class QuestComplete implements Conditional {
		private String quest;
		
		public QuestComplete(String quest) {
			super();
			this.quest = quest;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			return stateInfo.isQuestComplete(quest);
		}
		
	}
	
	public static class UnitDeath implements Conditional
	{
		private int unitId;
		private boolean enemy = false;
		
		public UnitDeath(int unitId, boolean enemy) {
			super();
			this.unitId = unitId;
			this.enemy = enemy;
		}

		// Check to see if a unit with a unit id equal to the 
		// specified unit id exists anymore, if not then this condition
		// is met
		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			if (enemy)
			{
				for (CombatSprite cs : stateInfo.getCombatSprites()) {
					if (cs.getUniqueEnemyId() == unitId)
						return false;
				}
			} else {
				for (CombatSprite cs : stateInfo.getAllHeroes()) {
					if (cs.getId() == unitId && cs.getCurrentHP() > 0)
						return false;
				}
			}
			return true;
		}
	}
	
	public static class LocationSearched implements Conditional 
	{
		private String location;
		
		public LocationSearched(String location) {
			super();
			this.location = location;
		}

		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			return searching && location != null && location.equalsIgnoreCase(this.location);
		}
	}
	
	public static class HeroEntersLocation implements Conditional
	{
		private String location;
		private boolean immediate;
		
		public HeroEntersLocation(String location, boolean immediate) {
			super();
			this.location = location;
			this.immediate = immediate;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			if (locationEntered && location != null && this.immediate == immediate && location.equalsIgnoreCase(this.location))
			{
				return true;
			}
			
			return false;
		}
		
	}
	
	public static class LocationContainsUnits implements Conditional
	{
		private String location;
		private boolean enemy;
		private int amount;
		private String operator;
		
		public LocationContainsUnits(String location, boolean enemy, int amount, String operator) {
			super();
			this.location = location;
			this.enemy = enemy;
			this.amount = amount;
			this.operator = operator;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			int count = 0;
			for (MapObject mo : stateInfo.getCurrentMap().getMapObjects())
			{
				if (mo.getName() != null && mo.getName().equalsIgnoreCase(this.location))
				{
					for (CombatSprite cs : stateInfo.getCombatSprites())
					{	
						if (cs.isHero() && !enemy && mo.contains(cs))
						{
							count++;
						} else if (!cs.isHero() && enemy && mo.contains(cs)) {
							count++;
						}
					}
					
					if (operator.equalsIgnoreCase("Greater Than"))
					{
						return count > amount;
							
					}
					else if (operator.equalsIgnoreCase("Less Than"))
					{
						return count < amount;
					}
					else if (operator.equalsIgnoreCase("Equals"))
					{
						return count == amount;
					}
					
				}
			}
			return false;
		}
		
	}
	
	public static class HeroInBattle implements Conditional
	{
		private int id;
		
		public HeroInBattle(int id) {
			super();
			this.id = id;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			return stateInfo.getCombatantById(id) != null;
		}
	}
	
	public static class EnemyInBattle implements Conditional
	{
		private int unitId;
		
		public EnemyInBattle(int id) {
			super();
			this.unitId = id;
		}

		@Override
		public boolean conditionIsMet(String location, boolean locationEntered, 
				boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo) {
			return stateInfo.getEnemyCombatSpriteByUnitId(unitId) != null;
		}
	}
}