package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import tactical.utils.planner.MapListRenderer.MapCell;

public class MapListRenderer implements ListCellRenderer<MapCell> {

	@Override
	public Component getListCellRendererComponent(
			JList<? extends MapCell> list, MapCell value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setMinimumSize(new Dimension(150, 100));

		if (value.button != null)
			panel.add(value.button);

		for (String v : value.values)
			panel.add(new JLabel(v));

		JPanel parentPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(value.name);
		label.setForeground(Color.white);

		if (isSelected)
			label.setBackground(Color.red);
		else
			label.setBackground(Color.darkGray);
		label.setOpaque(true);
		// label.setBackground(Color.black);
		parentPanel.add(label, BorderLayout.PAGE_START);

		parentPanel.add(panel, BorderLayout.CENTER);

		return parentPanel;
	}

	public static class MapCell
	{
		public String name;
		public ArrayList<String> values = new ArrayList<>();
		public JButton button = null;
	}
}
