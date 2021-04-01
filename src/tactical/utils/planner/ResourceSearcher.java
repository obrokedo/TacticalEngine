package tactical.utils.planner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;

import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class ResourceSearcher {
	
	private Hashtable<String, PlannerContainerDef> containersByName;
	private String pathPrefix;
	
	public ResourceSearcher(String pathPrefix) {
		containersByName = new Hashtable<>();
		ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType = new ArrayList<ArrayList<PlannerReference>>();

		/*******************/
		/* Set up triggers */
		/*******************/
		PlannerDefinitions.setupDefintions(referenceListByReferenceType, containersByName);		
		this.pathPrefix = pathPrefix;
	}
	
	public ResourceSearcher(Hashtable<String, PlannerContainerDef> containersByName) {
		this.containersByName = containersByName;
		this.pathPrefix = "";
	}
	public List<SearchResult> searchGlobal(String searchString) {
		List<SearchResult> results = new ArrayList<>();
		
		PlannerIO plannerIO = new PlannerIO();
		// Go through all of the map data files searching
		for (File file : new File(pathPrefix + PlannerIO.PATH_MAPDATA).listFiles()) {
			ArrayList<TagArea> tagAreas = null;
			try {
				tagAreas = XMLParser
						.process(Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8), true);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "An error occurred while trying to parse file " + file.getPath()
						+ " it will not be included in search results");
				e.printStackTrace();
				continue;
			}
			List<PlannerTab> tempTabs = new ArrayList<>();
			// Add triggers
			tempTabs.add(new PlannerTab("Triggers", containersByName, new String[] { "trigger" },
					PlannerValueDef.REFERS_TRIGGER, null, PlannerFrame.TAB_TRIGGER));

			// Add conditions
			tempTabs.add(new PlannerTab("Conditions", containersByName, new String[] { "condition" },
					PlannerValueDef.REFERS_CONDITIONS, null, PlannerFrame.TAB_CONDITIONS));

			// Add cinematics
			tempTabs.add(new PlannerTab("Cinematics", containersByName, new String[] { "cinematic" },
					PlannerValueDef.REFERS_CINEMATIC, null, PlannerFrame.TAB_CIN));

			// Add speech
			tempTabs.add(new PlannerTab("Speeches", containersByName, new String[] { "text" },
					PlannerValueDef.REFERS_TEXT, null, PlannerFrame.TAB_TEXT));

			plannerIO.parseContainer(tagAreas, tempTabs.get(0), "trigger", containersByName, false);
			plannerIO.parseContainer(tagAreas, tempTabs.get(3), "text", containersByName, false);
			plannerIO.parseContainer(tagAreas, tempTabs.get(2), "cinematic", containersByName, false);
			plannerIO.parseContainer(tagAreas, tempTabs.get(1), "condition", containersByName, false);

			searchPlannerTabs(tempTabs, searchString, results, file.getName());
		}

		return results;
	}

	public void searchPlannerTabs(List<PlannerTab> plannerTabs, String searchString, List<SearchResult> results,
			String optionalFile) {
		for (PlannerTab pt : plannerTabs) {
			for (PlannerContainer pc : pt.getListPC()) {
				searchPlannerLine(searchString, pt, pc, pc.getDefLine(), results, optionalFile);
				for (PlannerLine pl : pc.getLines())
					searchPlannerLine(searchString, pt, pc, pl, results, optionalFile);
			}
		}
	}

	protected void searchPlannerLine(String searchString, PlannerTab pt, PlannerContainer pc, PlannerLine pl,
			List<SearchResult> results, String optionalFile) {
		for (Object o : pl.getValues()) {

			String searchMe = null;
			if (o != null) {
				if (o instanceof String && ((String) o).trim().length() > 0) {
					searchMe = ((String) o).trim();
				} else if (o instanceof PlannerReference && ((PlannerReference) o).getName() != null
						&& ((PlannerReference) o).getName().trim().length() > 0)
					searchMe = ((PlannerReference) o).getName().trim();
			}

			if (searchMe != null && searchMe.toLowerCase().contains(searchString)) {
				results.add(new SearchResult(pt, pc, pl, searchMe, optionalFile));
			}
		}
	}
	
	public class SearchResult {
		public final PlannerTab pt; 
		public final PlannerContainer pc; 
		public final PlannerLine pl;
		public final String value;
		public final String file;
		
		public SearchResult(PlannerTab pt, PlannerContainer pc, PlannerLine pl, String value, String file) {
			super();
			this.pt = pt;
			this.pc = pc;
			this.pl = pl;
			this.value = value;
			this.file = file;
		}
	}
}
