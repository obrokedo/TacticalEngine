package tactical.utils.planner;


public class PlannerValueDef
{
	public static final int TYPE_STRING = 0;
	public static final int TYPE_INT = 1;
	public static final int TYPE_BOOLEAN = 2;
	public static final int TYPE_MULTI_INT = 3;
	public static final int TYPE_LONG_STRING = 4;
	public static final int TYPE_UNBOUNDED_INT = 5;
	public static final int TYPE_MULTI_LONG_STRING = 6;
	public static final int TYPE_MULTI_STRING = 7;

	// Refers to
	private int refersTo;

	// Value type
	private int valueType;

	// Variable output
	private String tag;

	private boolean optional = false;

	private String displayTag;
	private String displayDescription;

	public PlannerValueDef(int refersTo, int valueType, String tag,
			boolean optional, String displayTag, String displayDescription) {
		super();
		this.refersTo = refersTo;
		this.valueType = valueType;
		this.tag = tag;
		this.optional = optional;
		this.displayTag = displayTag;
		this.displayDescription = displayDescription;
		
		if (refersTo != ReferenceStore.REFERS_NONE && (valueType == TYPE_LONG_STRING || valueType == TYPE_MULTI_LONG_STRING || valueType == TYPE_BOOLEAN))
		{
			throw new RuntimeException("Can not create a PlannerValueDef with value type: " + valueType + " that is a reference");
		}
	}

	public PlannerValueDef copy()
	{
		return new PlannerValueDef(refersTo, valueType, tag, optional, displayTag, displayDescription);
	}

	public int getRefersTo() {
		return refersTo;
	}

	public int getValueType() {
		return valueType;
	}

	public String getTag() {
		return tag;
	}

	public boolean isOptional() {
		return optional;
	}

	public String getDisplayTag() {
		return displayTag;
	}

	public String getDisplayDescription() {
		return displayDescription;
	}

	@Override
	public String toString() {
		return "PlannerValueDef [refersTo=" + refersTo + ", valueType=" + valueType + ", tag=" + tag + ", optional="
				+ optional + ", displayTag=" + displayTag + ", displayDescription=" + displayDescription + "]";
	}
}
