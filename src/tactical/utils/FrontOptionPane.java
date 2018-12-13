package tactical.utils;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class FrontOptionPane {
	public static int showConfirmDialog(String text, String title) {
		JOptionPane pane = new JOptionPane(text, 
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog diag = pane.createDialog(JOptionPane.getRootFrame(), title);
		diag.requestFocus();
		diag.setVisible(true);
		diag.dispose();
		Object ret = pane.getValue();
		if (ret != null && ((Integer) ret) == JOptionPane.YES_OPTION)
			return JOptionPane.YES_OPTION;
		return JOptionPane.NO_OPTION;
	}
}
