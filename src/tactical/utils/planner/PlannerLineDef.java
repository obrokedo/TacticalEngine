package tactical.utils.planner;

import java.util.ArrayList;

public class PlannerLineDef
{
	private String tag;
	private String name;
	private String description;
	private ArrayList<PlannerValueDef> plannerValues;

	public PlannerLineDef(String tag, String name, String description, ArrayList<PlannerValueDef> plannerValues) {
		this.tag = tag;
		this.name = name;
		this.description = description;
		this.plannerValues = plannerValues;
	}

	public String getTag() {
		return tag;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ArrayList<PlannerValueDef> getPlannerValues() {
		return plannerValues;
	}

	public void setName(String name) {
		this.name = name;
	}
}
