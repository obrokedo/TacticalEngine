package tactical.utils.planner;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ComboBoxRenderer extends JLabel implements ListCellRenderer<String> {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list,
			String value, int index, boolean isSelected, boolean cellHasFocus)
	{
		this.setText(value);
		this.setForeground(Color.RED);
		return this;
	}
}
