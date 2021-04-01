package tactical.game.resource;

import java.util.Optional;

import tactical.cinematic.event.CinematicEvent;
import tactical.game.definition.EnemyDefinition;
import tactical.game.definition.HeroDefinition;
import tactical.game.definition.ItemDefinition;
import tactical.game.trigger.Trigger;
import tactical.utils.planner.PlannerLineDef;

public class GameResource {
	public Optional<Trigger> parseTrigger() {
		return Optional.empty();
	}
	
	public Optional<CinematicEvent> parseCinematicEvent() {
		return Optional.empty();
	}
	
	public Optional<HeroDefinition> parseHeroDefinition() {
		return Optional.empty();
	}
	
	public Optional<ItemDefinition> parseItemDefinition() {
		return Optional.empty();
	}
	
	public Optional<EnemyDefinition> parseEnemyDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getTriggerPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getCinematicPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getHeroPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getEnemyPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getConditiondPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getSpeechPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getItemPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getQuestPlannerDefinition() {
		return Optional.empty();
	}
	
	public Optional<PlannerLineDef> getMapPlannerDefinition() {
		return Optional.empty();
	}
}
