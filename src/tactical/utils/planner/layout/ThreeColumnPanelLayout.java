package tactical.utils.planner.layout;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tactical.utils.planner.PlannerPanelLayout;
import tactical.utils.planner.PlannerValueDef;

public abstract class ThreeColumnPanelLayout implements PlannerPanelLayout {
	
	public abstract String[][] getLayoutList();
	
	private void addComponents(JPanel parentPanel, Hashtable<String, ComponentDefinition> definitionsByTag,
			GridBagConstraints gbc, int row, String ...tags) {	
		int count = 0;
		for (String tag : tags) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = count++;
			gbc.gridy = row;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.ipady = 8;
			if (tags.length == 1)
				gbc.gridwidth = GridBagConstraints.REMAINDER;
			
			ComponentDefinition def = definitionsByTag.get(tag);
			JLabel label = new JLabel(String.format("%1$" + 20 + "s", def.plannerValueDef.getDisplayTag()));
			parentPanel.add(label, gbc);
			gbc.gridx = count++;
			parentPanel.add(def.component, gbc);
		}
	}
	
	private class ComponentDefinition {
		public Component component;
		public PlannerValueDef plannerValueDef;
		
		public ComponentDefinition(Component component, PlannerValueDef plannerValueDef) {
			super();
			this.component = component;
			this.plannerValueDef = plannerValueDef;
		}
	}

	@Override
	public void layoutPanel(JPanel parentPanel, ArrayList<Component> panelComponents,
			ArrayList<PlannerValueDef> panelComponentDefinitions) {
		parentPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		Hashtable<String, ComponentDefinition> definitionsByTag = new Hashtable<>();
		for (int i = 0; i < panelComponents.size(); i++) {
			definitionsByTag.put(panelComponentDefinitions.get(i).getTag(), 
					new ComponentDefinition(panelComponents.get(i), panelComponentDefinitions.get(i)));
		}
		
		String[][] layout = getLayoutList();
		
		for (int i = 0; i < layout.length; i++) {
			addComponents(parentPanel, definitionsByTag, gbc, i, layout[i]);
		}
	}
}
