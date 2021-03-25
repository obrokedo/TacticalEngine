package tactical.utils.planner;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;

public interface PlannerPanelLayout {
	public void layoutPanel(JPanel parentPanel, ArrayList<Component> panelComponents, ArrayList<PlannerValueDef> panelComponentDefinitions);
}
