package tactical.utils.planner;

import java.util.ArrayList;
import java.util.Hashtable;

public class PlannerContainerDef
{
	private PlannerLineDef definingLine;
	private ArrayList<PlannerLineDef> allowableLines;
	private ArrayList<ArrayList<PlannerReference>> listOfLists;
	private int writeToIndex;
	private Hashtable<String, ArrayList<String>> groupingsForAllowableLine;

	public PlannerContainerDef(PlannerLineDef definingLine,
			ArrayList<PlannerLineDef> allowableLines, ArrayList<ArrayList<PlannerReference>> listOfLists,
			int writeToIndex)
	{
		this(definingLine, allowableLines, listOfLists, writeToIndex, null);
	}

	public PlannerContainerDef(PlannerLineDef definingLine,
			ArrayList<PlannerLineDef> allowableLines, ArrayList<ArrayList<PlannerReference>> listOfLists,
			int writeToIndex, Hashtable<String, ArrayList<String>> groupingsForAllowableLine)
	{
		this.definingLine = definingLine;
		this.allowableLines = allowableLines;
		this.listOfLists = listOfLists;
		this.writeToIndex = writeToIndex;
		this.groupingsForAllowableLine = groupingsForAllowableLine;
	}

	public PlannerLineDef getDefiningLine() {
		return definingLine;
	}

	public ArrayList<PlannerLineDef> getAllowableLines() {
		return allowableLines;
	}

	/**
	 * Get the "list of lists" that contain the name of every item definied so that they
	 * may be refered to by REFER tags
	 *
	 * @return the "list of lists" that contain the name of every item definied so that they
	 * may be refered to by REFER tags.
	 */
	public ArrayList<PlannerReference> getDataLines()
	{
		return listOfLists.get(writeToIndex);
	}

	public ArrayList<ArrayList<PlannerReference>> getListOfLists() {
		return listOfLists;
	}

	public Hashtable<String, ArrayList<String>> getGroupingsForAllowableLine() {
		return groupingsForAllowableLine;
	}
}
