package tactical.utils.planner.layout;

public class PlannerHeroStatLayout extends ThreeColumnPanelLayout {
	@Override
	public String[][] getLayoutList() {
		String[][] layout = {{"attack", "attackstart", "attackend"},
				{"defense", "defensestart", "defenseend"},
				{"speed", "speedstart", "speedend"},
				{"hp", "hpstart", "hpend"},
				{"mp", "mpstart", "mpend"},
				{"fireAffin", "elecAffin", "coldAffin"},
				{"darkAffin", "waterAffin", "earthAffin"},
				{"windAffin", "lightAffin"},
				{"bodyStrength", "bodyProgress"},
				{"mindStrength", "mindProgress"},
				{"counterStrength", "evadeStrength"},
				{"doubleStrength", "critStrength"},
				{"usuableitems"},
				{"class"},
				{"evaluation"}};
		return layout;
	}
}
