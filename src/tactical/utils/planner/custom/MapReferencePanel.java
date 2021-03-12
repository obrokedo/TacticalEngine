package tactical.utils.planner.custom;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tactical.loading.MapParser;
import tactical.map.MapObject;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.AutoCompletion;
import tactical.utils.planner.PlannerIO;
import tactical.utils.planner.PlannerLine;

public class MapReferencePanel extends JPanel implements ItemListener{
	private JComboBox<String> combo;
	
	public MapReferencePanel(PlannerLine parentLine, Vector<String> items, String selected) {
		super (new BorderLayout());
		combo = new JComboBox<String>(items);
		if (selected != null)
			combo.setSelectedItem(selected);
		combo.addItemListener(parentLine);
		combo.addItemListener(this);		
		AutoCompletion.enable(combo);
		this.add(combo, BorderLayout.PAGE_START);
		this.showEntrances();
	}
	
	public Object getSelectedItem() {
		return combo.getSelectedItem();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		showEntrances();
	}

	private void showEntrances() {
		JPanel entPanel = new JPanel();
		entPanel.setLayout(new BoxLayout(entPanel, BoxLayout.PAGE_AXIS));
		entPanel.add(new JLabel("--- Entrances ---"));
		if (combo.getSelectedIndex() != 0) {
			try {
				File file = new File(PlannerIO.PATH_MAPDATA + "/" + combo.getSelectedItem());
				ArrayList<TagArea> tagAreas = XMLParser.process(Files.readAllLines(
						Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8), true);
				
				HashSet<TagArea> mapAreas = new HashSet<TagArea>();
				for (TagArea ta : tagAreas)
					if (ta.getTagType().equalsIgnoreCase("objectgroup")) {
						for (TagArea mapObj : ta.getChildren()) {
							MapObject mo = MapParser.parseMapObject(mapObj);
							if (mo.getKey().equalsIgnoreCase("start"))
								entPanel.add(new JLabel(mo.getParam("exit")));
						}
					}
			} catch (Exception ex) {
				
			}
		}
		this.removeAll();
		this.add(combo, BorderLayout.PAGE_START);
		this.add(entPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
}
