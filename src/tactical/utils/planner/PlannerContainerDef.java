package tactical.utils.planner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PlannerContainerDef
{
	private PlannerLineDef definingLine;
	private ArrayList<PlannerLineDef> allowableLines;
	private ReferenceStore referenceStore;
	private int writeToIndex;
	private Hashtable<String, ArrayList<String>> groupingsForAllowableLine;

	public PlannerContainerDef(PlannerLineDef definingLine,
			ArrayList<PlannerLineDef> allowableLines, ReferenceStore referenceStore,
			int writeToIndex)
	{
		this(definingLine, allowableLines, referenceStore, writeToIndex, null);
	}

	public PlannerContainerDef(PlannerLineDef definingLine,
			ArrayList<PlannerLineDef> allowableLines, ReferenceStore referenceStore,
			int writeToIndex, Hashtable<String, ArrayList<String>> groupingsForAllowableLine)
	{
		this.definingLine = definingLine;
		this.allowableLines = allowableLines;
		this.referenceStore = referenceStore;
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
	public List<PlannerReference> getDataLines()
	{
		return referenceStore.getReferencesForType(writeToIndex);
	}

	public ReferenceStore getReferenceStore() {
		return referenceStore;
	}

	public Hashtable<String, ArrayList<String>> getGroupingsForAllowableLine() {
		return groupingsForAllowableLine;
	}
}
