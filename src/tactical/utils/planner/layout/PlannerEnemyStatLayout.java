package tactical.utils.planner.layout;

public class PlannerEnemyStatLayout extends ThreeColumnPanelLayout {

	@Override
	public String[][] getLayoutList() {
		String[][] layout = 
			{{"name", "level", "gold"},
				{"move", "movementtype"},
				{"hp", "mp", "attack"},
				{"defense", "speed"},
				{"fireAffin", "elecAffin", "coldAffin"},
				{"darkAffin", "waterAffin", "earthAffin"}, 
				{"windAffin", "lightAffin"}, 
				{"bodyStrength", "mindStrength"},
				{"counterStrength", "evadeStrength"},
				{"doubleStrength", "critStrength"},
				{"animations"}};
		return layout;
	}
}
