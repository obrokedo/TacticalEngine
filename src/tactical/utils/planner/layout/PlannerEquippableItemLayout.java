package tactical.utils.planner.layout;

public class PlannerEquippableItemLayout extends ThreeColumnPanelLayout {

	@Override
	public String[][] getLayoutList() {
		String[][] layout = 
				{{"attack", "defense", "speed"},
				{"type", "style", "range"},
				{"weaponimage", "weaponanim"},
				{"incmindam", "inccrit", "inccounter"},
				{"incdouble", "incevade"},
				{"maxhpreg", "minhpreg"},
				{"maxmpreg", "minmpreg"},
				{"effect", "efflvl", "effchc"},
				{"dmgaff", "ohko"},
				{"fireAffin", "elecAffin", "coldAffin"},
				{"darkAffin", "waterAffin", "earthAffin"}, 
				{"windAffin", "lightAffin"}, 
				{"promoteonly"}
		};
		return layout;
	}
	
}
