package tactical.utils.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tactical.engine.log.LoggingUtils;

public class PlannerReference {
	private static final Logger LOGGER = LoggingUtils.createLogger(PlannerReference.class);
	private String name;

	public PlannerReference(String name) {
		super();
		this.name = name;
	}

	/**
	 * This method is responsible reseting values of referencers when the referencee is deleted
	 * 
	 * @param referenceType the id (PlannerValueDef) that identifies the type of PlannerContainer that has been removed
	 * @param referenceIndex for int and multi-int fields the index of the item that was removed
	 */
	
	public static void removeReferences(int referenceType, int referenceIndex) {
		PlannerFrame.referenceListByReferenceType.get(referenceType - 1).remove(referenceIndex).setName("");
	}
	
	public static List<String> getBadReferences(List<PlannerTab> tabsWithReferences) {
		List<String> badReferences = new ArrayList<>();
		
		for (PlannerTab plannerTab : tabsWithReferences) {
			for (PlannerContainer plannerContainer : plannerTab.getListPC()) {
				getBadLineReferences(badReferences, plannerContainer, plannerContainer.getDefLine());
				for (PlannerLine pl : plannerContainer.getLines()) {
					getBadLineReferences(badReferences, plannerContainer, pl);
				}
			}
		}
		
		return badReferences;
	}
		
	private static void getBadLineReferences(
			List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl) {
		for (int j = 0; j < pl.getPlDef().getPlannerValues().size() && j < pl.getValues().size(); j++) {
			PlannerValueDef pvd = pl.getPlDef().getPlannerValues().get(j);
			
			if (pvd.getRefersTo() != PlannerValueDef.REFERS_NONE && !pvd.isOptional()) {
				if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_INT || pvd.getValueType() == PlannerValueDef.TYPE_MULTI_STRING) {
					ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(j);
					for (PlannerReference ref : refs) {
						if (ref.getName().equalsIgnoreCase("")) {
							addBadPlannerReference(badReferences, plannerContainer, pl, j, ref);
						}
					}
				} else {
					PlannerReference ref = (PlannerReference) pl.getValues().get(j);
					if (ref.getName().equalsIgnoreCase("")) {
						addBadPlannerReference(badReferences, plannerContainer, pl, j, ref);
					}
				}
			}
		}
	}
	
	public static List<String> establishReferences(List<PlannerTab> tabsWithReferences, ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType) {
		List<String> badReferences = new ArrayList<>();
		
		for (PlannerTab plannerTab : tabsWithReferences) {
			for (PlannerContainer plannerContainer : plannerTab.getListPC()) {
				establishLineReference(referenceListByReferenceType, badReferences, plannerContainer, plannerContainer.getDefLine());
				for (PlannerLine pl : plannerContainer.getLines()) {
					establishLineReference(referenceListByReferenceType, badReferences, plannerContainer, pl);
				}
			}
		}
		
		// displayBadReferences(badReferences);
		return badReferences;
	}

	public static void displayBadReferences(List<String> badReferences) {
		// Display errors
		if (badReferences.size() > 0) {
			JTextArea errors = new JTextArea();
			JScrollPane errorsScroll = new JScrollPane(errors);
			
			StringBuffer stringBuf = new StringBuffer();
			for (String badRef : badReferences) {
				stringBuf.append(badRef + System.lineSeparator());
				LOGGER.fine(badRef);
				System.out.println(badRef);
			}
			errors.setText(stringBuf.toString());
			JOptionPane.showMessageDialog(null, errorsScroll);
		}
	}

	public static void establishLineReference(ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType,
			List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl) {
		for (int j = 0; j < pl.getPlDef().getPlannerValues().size() && j < pl.getValues().size(); j++) {
			PlannerValueDef pvd = pl.getPlDef().getPlannerValues().get(j);
			
			if (pvd.getRefersTo() != PlannerValueDef.REFERS_NONE && 
					!(pl.getValues().get(j) instanceof PlannerReference) &&
					!(pl.getValues().get(j) instanceof ArrayList<?>)) {
				ArrayList<PlannerReference> references = referenceListByReferenceType.get(pvd.getRefersTo() - 1);
				if (pvd.getValueType() == PlannerValueDef.TYPE_INT) {
					// Unfortunately it's difficult to find errors here since we don't know whether the index they 
					// point to is reasonable or not, settle for indicating out of range errors
					int index = (int) pl.getValues().get(j) - 1;
					pl.getValues().set(j, establishIntReference(index, badReferences, plannerContainer, pl, j, references, pvd));
				} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_INT) {
					establishMultiIntReference(badReferences, plannerContainer, pl, j, pvd, references);
				} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_STRING) {
					String[] vals = null;
				
					try {
						String[] values = ((String) pl.getValues().get(j)).split(",");
						String newVals = "";
						for (int valIdx = 0; valIdx < values.length; valIdx++) {
							newVals = newVals + (Integer.parseInt(values[valIdx]) + 1);
							if (valIdx + 1 != values.length)
								newVals = newVals + ",";
						}
						pl.getValues().set(j, newVals);
						
						establishMultiIntReference(badReferences, plannerContainer, pl, j, pvd, references);
					} catch (Throwable t) {
						vals = ((String) pl.getValues().get(j)).split(",");
						List<PlannerReference> multiStringList = new ArrayList<PlannerReference>();
						for (String val : vals) {
							multiStringList.add(establishStringReference(val, badReferences, plannerContainer, pl, j, pvd, references));
						}
						pl.getValues().set(j, multiStringList);
					}
				} else if (pvd.getValueType() == PlannerValueDef.TYPE_STRING) {
					try {
						int intval =  Integer.parseInt((String) pl.getValues().get(j));
						pl.getValues().set(j, establishIntReference(intval, badReferences, plannerContainer, pl, j, references, pvd));
					} catch (Throwable e) {
						pl.getValues().set(j, establishStringReference((String) pl.getValues().get(j), 
								badReferences, plannerContainer, pl, j, pvd, references));
					}
				}
			}
		}
	}

	private static void establishMultiIntReference(List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl,
			int j, PlannerValueDef pvd, ArrayList<PlannerReference> references) {
		String[] vals = ((String) pl.getValues().get(j)).split(",");
		List<PlannerReference> multiIntList = new ArrayList<PlannerReference>();
		for (String val : vals) {
			int valParsed = Integer.parseInt(val) - 1;
			multiIntList.add(establishIntReference(valParsed, badReferences, plannerContainer, pl, j, references, pvd));
		}
		pl.getValues().set(j, multiIntList);
	}

	private static PlannerReference establishStringReference(String val, List<String> badReferences, PlannerContainer plannerContainer,
			PlannerLine pl, int j, PlannerValueDef pvd, ArrayList<PlannerReference> references) {
		PlannerReference refToAdd;
		int referenceIndex = references.indexOf(new PlannerReference(val));
		if (referenceIndex != -1) {
			refToAdd = references.get(referenceIndex);
		} else if (pvd.isOptional()) {
			refToAdd = new PlannerReference("");
		} else {
			if (plannerContainer != null) {
				addBadStringReference(badReferences, plannerContainer, pl, j);
			} else {
				badReferences.add("Attribute " + pl.getPlDef().getName() + 
					" has a bad reference to '" + (String) pl.getValues().get(j) + "' on it's " + 
					pl.getPlDef().getPlannerValues().get(j).getDisplayTag() + " value");
			}
			refToAdd = new PlannerReference("");
		}
		return refToAdd;
	}

	private static PlannerReference establishIntReference(int index, List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl, int j,
			ArrayList<PlannerReference> references, PlannerValueDef pvd) {
		if (index < 0 || index >= references.size()) {
			if (!pvd.isOptional()) {
				if (plannerContainer != null) {
					addBadIntReference(index, badReferences, plannerContainer, pl, pvd);
				} else {
					badReferences.add("Attribute " + pl.getPlDef().getName() + 
						" has a bad reference to index " + index + " on it's " + pvd.getDisplayTag() + " value");
				}
			}
			return new PlannerReference("");
		} else {
			return references.get(index);
		}
	}
	
	private static void addBadPlannerReference(List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl,
			int j, PlannerReference badRef) {
		badReferences.add(plannerContainer.getDefLine().getPlDef().getName() + " named: " + 
			plannerContainer.getDefLine().getValues().get(0) + " with attribute " + 
			pl.getPlDef().getName() + 
			" has a bad reference to '" + badRef.getName() + 
			"' on it's " + pl.getPlDef().getPlannerValues().get(j).getDisplayTag() + " value");
	}
	
	private static void addBadStringReference(List<String> badReferences, PlannerContainer plannerContainer, PlannerLine pl,
			int j) {
		badReferences.add(plannerContainer.getDefLine().getPlDef().getName() + " named: " + 
			plannerContainer.getDefLine().getValues().get(0) + " with attribute " + 
			pl.getPlDef().getName() + 
			" has a bad reference to '" + (String) pl.getValues().get(j) + 
			"' on it's " + pl.getPlDef().getPlannerValues().get(j).getDisplayTag() + " value");
	}

	private static void addBadIntReference(int index, List<String> badReferences, PlannerContainer plannerContainer,
			PlannerLine pl, PlannerValueDef pvd) {
		badReferences.add(plannerContainer.getDefLine().getPlDef().getName() + " named: " + 
			plannerContainer.getDefLine().getValues().get(0) + " with attribute " + 
			pl.getPlDef().getName() + 
			" has a bad reference to index " + index + " on it's " + pvd.getDisplayTag() + " value");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlannerReference other = (PlannerReference) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlannerReference [name=" + name + "]";
	}
}
