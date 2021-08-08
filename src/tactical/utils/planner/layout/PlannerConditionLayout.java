package tactical.utils.planner.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tactical.utils.planner.PlannerPanelLayout;
import tactical.utils.planner.PlannerValueDef;

public class PlannerConditionLayout implements PlannerPanelLayout {

	@Override
	public void layoutPanel(JPanel parentPanel, ArrayList<Component> panelComponents,
			ArrayList<PlannerValueDef> panelComponentDefinitions) {	
		JPanel backPanel = new JPanel();
		backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.PAGE_AXIS));
		
		addJustifiedComponent(backPanel, new JLabel("Instigating Action"));
		
		addBlock(backPanel, "Always {", "}");
		addJustifiedComponent(backPanel, new JLabel("  "));
		addBlock(backPanel, "If (", ")");
		addBlock(backPanel, "Then {", "}");
		
		parentPanel.setLayout(new BorderLayout());
		parentPanel.add(backPanel, BorderLayout.PAGE_START);
		parentPanel.add(new JPanel(), BorderLayout.CENTER);
	}
	
	private void addBlock(JPanel backPanel, String openTag, String closeTag) {
		JPanel blockPanel = new JPanel();
		blockPanel.setBackground(Color.GRAY);
		blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.PAGE_AXIS));
		addJustifiedComponent(blockPanel, new JLabel(openTag));
		addJustifiedRow(blockPanel, new JLabel("  Blap"));
		
		JLabel addAction = new JLabel("  + Add action");
		addAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(null, "Quit clicking me bro");
			}
		});
		
		addJustifiedRow(blockPanel, addAction);	
		addJustifiedComponent(blockPanel, new JLabel(closeTag));
		addJustifiedComponent(backPanel, blockPanel);
	}
	
	private void addJustifiedComponent(JPanel backPanel, JComponent comp) {
		comp.setAlignmentX(Component.LEFT_ALIGNMENT);
		backPanel.add(comp);
	}
	
	private void addJustifiedRow(JPanel backPanel, JComponent comp) {
		JPanel rowPanel = new JPanel(new BorderLayout());
		rowPanel.setBackground(Color.WHITE);
		rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rowPanel.add(comp, BorderLayout.LINE_START);
		backPanel.add(rowPanel);
	}
}
