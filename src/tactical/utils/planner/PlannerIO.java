package tactical.utils.planner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import tactical.engine.log.LoggingUtils;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class PlannerIO {
	private static final Logger LOGGER = LoggingUtils.createLogger(PlannerIO.class);
	
	public static String PATH_ENEMIES = "definitions/Enemies";
	public static String PATH_HEROES = "definitions/Heroes";
	public static String PATH_ITEMS = "definitions/Items";
	public static String PATH_QUESTS = "Quests";
	public static String PATH_MAPS = "map";
	public static String PATH_MAPDATA = "mapdata";
	
	/********************************************/
	/* Export data methods 						*/
	/********************************************/
	public boolean exportDataToFile(ArrayList<PlannerContainer> containers,
			String pathToFile, boolean append, String rootXMLTag) {
		ArrayList<String> myBuffer = null;
		try {
			myBuffer = PlannerIO.export(containers, rootXMLTag);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred while trying to format the data for " + rootXMLTag + ":"
					+ e.getMessage(), "Error formatting data", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		Path path = Paths.get(pathToFile);
		try {
			if (append)
				Files.write(path, myBuffer, StandardCharsets.UTF_8,
						StandardOpenOption.APPEND);
			else
				Files.write(path, myBuffer, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred while trying to save the for " + rootXMLTag + ":"
					+ e.getMessage(), "Error saving data", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public static ArrayList<String> export(ArrayList<PlannerContainer> containers, String rootXMLTag)
	{		
		/*
		Map<String, Object> configs = new HashMap<>();
        configs.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonBuilderFactory factory = Json.createBuilderFactory(configs);
        
        JsonArrayBuilder headJAB = Json.createArrayBuilder();
        
        for (int i = 0; i < containers.size(); i++) {
			PlannerContainer pc = containers.get(i);
			
			JsonObjectBuilder job = factory.createObjectBuilder();
			job.add("header", exportLineAsJson(pc.getPcdef().getDefiningLine(),
					pc.getDefLine(), i));
			JsonArrayBuilder childJAB = Json.createArrayBuilder();
			for (PlannerLine pl : pc.getLines())
				childJAB.add(exportLineAsJson(pl.getPlDef(), pl, -1));
			job.add("values", childJAB);
		Json.createObjectBuilder().add("triggers", headJAB);
		*/
		
		
		ArrayList<String> buffer = new ArrayList<String>();
		if (rootXMLTag != null)
			buffer.add("<" + rootXMLTag + ">");
		for (int i = 0; i < containers.size(); i++) {
			PlannerContainer pc = containers.get(i);
			buffer.add(exportLine(pc.getPcdef().getDefiningLine(),
					pc.getDefLine(), i));

			for (PlannerLine pl : pc.getLines())
				buffer.add(exportLine(pl.getPlDef(), pl, -1));

			buffer.add("</" + pc.getPcdef().getDefiningLine().getTag() + ">");
		}
		if (rootXMLTag != null)
			buffer.add("</" + rootXMLTag + ">");
		return buffer;
	}
	
        /*
	private static JsonObject exportLineAsJson() {
		JsonObjectBuilder job = Json.createObjectBuilder();
		
		String stringBuffer = "";
		if (pl.isDefining())
			stringBuffer += "<" + pldef.getTag();
		else
			stringBuffer += "\t<" + pldef.getTag();

		if (id != -1)
			stringBuffer += " id=\"" + id + "\"";

		for (int i = 0; i < pldef.getPlannerValues().size(); i++) {
			PlannerValueDef pvd = pldef.getPlannerValues().get(i);
			stringBuffer += " " + pvd.getTag() + "=";
			if (pvd.getValueType() == PlannerValueDef.TYPE_BOOLEAN)
				stringBuffer += "\"" + pl.getValues().get(i) + "\"";
			else if (pvd.getValueType() == PlannerValueDef.TYPE_STRING ||
					pvd.getValueType() == PlannerValueDef.TYPE_LONG_STRING ||
					pvd.getValueType() == PlannerValueDef.TYPE_MULTI_LONG_STRING) {
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE)
					stringBuffer += "\"" + pl.getValues().get(i) + "\"";
				else
					stringBuffer += "\"" + ((PlannerReference) pl.getValues().get(i)).getName() + "\"";
			}
			else if (pvd.getValueType() == PlannerValueDef.TYPE_INT
					|| pvd.getValueType() == PlannerValueDef.TYPE_UNBOUNDED_INT) {
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE)
					stringBuffer += "\"" + pl.getValues().get(i) + "\"";
				else
					stringBuffer += "\"" + PlannerFrame.referenceListByReferenceType.get(pvd.getRefersTo() - 1).indexOf(pl.getValues().get(i)) + "\"";
					// stringBuffer += (int) pl.getValues().get(i) - 1;
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_INT) {
				String newVals = "";
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE) {
					String[] oldVals = ((String) pl.getValues().get(i)).split(",");
	
					for (int j = 0; j < oldVals.length; j++) {
						newVals = newVals + (Integer.parseInt(oldVals[j]) - 1);
						if (j + 1 <= oldVals.length)
							newVals += ",";
					}
				} else {
					@SuppressWarnings("unchecked")
					ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(i);
					for (int j = 0; j < refs.size(); j++) {
						newVals = newVals + PlannerFrame.referenceListByReferenceType.get(pvd.getRefersTo() - 1).indexOf(refs.get(j));
						if (j + 1 <= refs.size())
							newVals += ",";
					}
				}

				stringBuffer += "\"" + newVals + "\"";
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_STRING) {
				String newVals = "";
				ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(i);

				for (int j = 0; j < refs.size(); j++) {
					newVals = newVals + refs.get(j).getName();
					if (j + 1 <= refs.size())
						newVals += ",";
				}
				stringBuffer +=  "\"" + newVals + "\"";
			}
		}
		if (pl.isDefining())
			stringBuffer += ">";
		else
			stringBuffer += "/>";
		return stringBuffer;
	}
	
	*/

	private static String exportLine(PlannerLineDef pldef, PlannerLine pl, int id) {
		String stringBuffer = "";
		if (pl.isDefining())
			stringBuffer += "<" + pldef.getTag();
		else
			stringBuffer += "\t<" + pldef.getTag();

		if (id != -1)
			stringBuffer += " id=\"" + id + "\"";

		for (int i = 0; i < pldef.getPlannerValues().size(); i++) {
			PlannerValueDef pvd = pldef.getPlannerValues().get(i);
			stringBuffer += " " + pvd.getTag() + "=";
			if (pvd.getValueType() == PlannerValueDef.TYPE_BOOLEAN)
				stringBuffer += "\"" + pl.getValues().get(i) + "\"";
			else if (pvd.getValueType() == PlannerValueDef.TYPE_STRING ||
					pvd.getValueType() == PlannerValueDef.TYPE_LONG_STRING ||
					pvd.getValueType() == PlannerValueDef.TYPE_MULTI_LONG_STRING) {
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE)
					stringBuffer += "\"" + pl.getValues().get(i) + "\"";
				else
					stringBuffer += "\"" + ((PlannerReference) pl.getValues().get(i)).getName() + "\"";
			}
			else if (pvd.getValueType() == PlannerValueDef.TYPE_INT
					|| pvd.getValueType() == PlannerValueDef.TYPE_UNBOUNDED_INT) {
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE)
					stringBuffer += "\"" + pl.getValues().get(i) + "\"";
				else
					stringBuffer += "\"" + PlannerFrame.referenceListByReferenceType.get(pvd.getRefersTo() - 1).indexOf(pl.getValues().get(i)) + "\"";
					// stringBuffer += (int) pl.getValues().get(i) - 1;
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_INT) {
				String newVals = "";
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE) {
					String[] oldVals = ((String) pl.getValues().get(i)).split(",");
	
					for (int j = 0; j < oldVals.length; j++) {
						newVals = newVals + (Integer.parseInt(oldVals[j]) - 1);
						if (j + 1 <= oldVals.length)
							newVals += ",";
					}
				} else {
					@SuppressWarnings("unchecked")
					ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(i);
					for (int j = 0; j < refs.size(); j++) {
						newVals = newVals + PlannerFrame.referenceListByReferenceType.get(pvd.getRefersTo() - 1).indexOf(refs.get(j));
						if (j + 1 <= refs.size())
							newVals += ",";
					}
				}

				stringBuffer += "\"" + newVals + "\"";
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_STRING) {
				String newVals = "";
				ArrayList<PlannerReference> refs = (ArrayList<PlannerReference>) pl.getValues().get(i);

				for (int j = 0; j < refs.size(); j++) {
					newVals = newVals + refs.get(j).getName();
					if (j + 1 <= refs.size())
						newVals += ",";
				}
				stringBuffer +=  "\"" + newVals + "\"";
			}
		}
		if (pl.isDefining())
			stringBuffer += ">";
		else
			stringBuffer += "/>";
		return stringBuffer;
	}

	/********************************************/
	/* Import data methods 						*/
	/********************************************/
	public void parseContainer(String path, PlannerTab plannerTab, String itemXmlTag, 
			Hashtable<String, PlannerContainerDef> containersByName) throws IOException {
		parseContainer(
				XMLParser.process(Files.readAllLines(
						Paths.get(path), StandardCharsets.UTF_8), true),
				plannerTab, itemXmlTag, containersByName, true);
	}

	/**
	 * 
	 * @param tas
	 * @param plannerTab
	 * @param allowableValue
	 * @param containersByName
	 * @param addReferences Whether references should be established, whenever loading a "background" container
	 * this value should be false so as to not mess with the "real" container definitions.
	 */
	public void parseContainer(ArrayList<TagArea> tas, PlannerTab plannerTab,
			String allowableValue, Hashtable<String, PlannerContainerDef> containersByName, boolean addReferences) {
		for (TagArea ta : tas) {
			if (!ta.getTagType().equalsIgnoreCase(allowableValue))
				continue;
			// LOGGER.finest(ta.getTagType());
			PlannerContainerDef pcd = containersByName.get(ta.getTagType());
			PlannerContainer plannerContainer = new PlannerContainer(pcd, plannerTab, false);
			PlannerLine plannerLine = plannerContainer.getDefLine();
			parseLine(plannerLine, pcd.getDefiningLine(), ta);
			if (addReferences)
				pcd.getDataLines().add(new PlannerReference(plannerContainer.getDescription()));

			plannerTab.addPlannerContainer(plannerContainer);

			for (TagArea taChild : ta.getChildren()) {
				boolean found = false;
				for (PlannerLineDef allowable : pcd.getAllowableLines()) {
					if (taChild.getTagType().equalsIgnoreCase(
							allowable.getTag())) {
						found = true;
						PlannerLine childLine = new PlannerLine(allowable,
								false);
						parseLine(childLine, allowable, taChild);
						plannerContainer.addLine(childLine, false);
					}
				}

				if (!found)
					LOGGER.warning("Unable to find tag definition for "
							+ taChild.getTagType());
			}
		}

		plannerTab.updateAttributeList(-1);
	}

	private void parseLine(PlannerLine plannerLine, PlannerLineDef pld,
			TagArea ta) {
		LOGGER.finest("PARENT: " + pld.getTag());
		for (PlannerValueDef pvd : pld.getPlannerValues()) {
			// Handle string values
			if (pvd.getValueType() == PlannerValueDef.TYPE_STRING || pvd.getValueType() == PlannerValueDef.TYPE_LONG_STRING ||
					pvd.getValueType() == PlannerValueDef.TYPE_MULTI_LONG_STRING)
				plannerLine.getValues().add(ta.getAttribute(pvd.getTag()));
					
			// Handle integer values
			else if (pvd.getValueType() == PlannerValueDef.TYPE_INT
					|| pvd.getValueType() == PlannerValueDef.TYPE_UNBOUNDED_INT) {
				if (pvd.getRefersTo() == PlannerValueDef.REFERS_NONE) {
					LOGGER.finest("TAG: " + pvd.getTag() + " "
							+ ta.getAttribute(pvd.getTag()));
					int value = 0;
					try {
						value = Integer.parseInt(ta.getAttribute(
								pvd.getTag()));
					} catch (NumberFormatException ex) {
					}

					plannerLine.getValues().add(value);
				}
				else
				{
					if (ta.getAttribute(pvd.getTag()) != null)
						plannerLine.getValues().add(Integer.parseInt(ta.getAttribute(pvd.getTag())) + 1);
					else
						plannerLine.getValues().add(0);
				}
			// Handle multiple int values
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_INT) {
				String newVals = "";

				if (ta.getAttribute(pvd.getTag()) != null && ta.getAttribute(pvd.getTag()).trim().length() > 0) {
					String[] values = ta.getAttribute(pvd.getTag())
							.split(",");
					for (int j = 0; j < values.length; j++) {
						newVals = newVals + (Integer.parseInt(values[j]) + 1);
						if (j + 1 != values.length)
							newVals = newVals + ",";
					}
				} else
					newVals = "-1";

				plannerLine.getValues().add(newVals);
			// Handle multi strings
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_MULTI_STRING) {
				String newVals = "";

				if (ta.getAttribute(pvd.getTag()) != null)
					newVals = ta.getAttribute(pvd.getTag());
				else
					newVals = "";
				
				plannerLine.getValues().add(newVals);
			// Handle boolean valuess
			} else if (pvd.getValueType() == PlannerValueDef.TYPE_BOOLEAN)
				plannerLine.getValues().add(
						Boolean.parseBoolean(ta.getAttribute(pvd.getTag())));

		}
	}
}
