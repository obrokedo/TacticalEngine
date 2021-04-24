package tactical.utils.planner;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class PlannerLineDef
{
	@Getter private String tag;
	@Getter @Setter private String name;
	@Getter private String description;
	@Getter private ArrayList<PlannerValueDef> plannerValues;
	@Getter private PlannerPanelLayout panelLayout = null;

	public PlannerLineDef(String tag, String name, String description, 
			ArrayList<PlannerValueDef> plannerValues) {
		this(tag, name, description, plannerValues, null);
	}
	
	public PlannerLineDef(String tag, String name, String description, 
			ArrayList<PlannerValueDef> plannerValues, PlannerPanelLayout panelLayout) {
		this.tag = tag;
		this.name = name;
		this.description = description;
		this.plannerValues = plannerValues;
		this.panelLayout = panelLayout;
	}
}
