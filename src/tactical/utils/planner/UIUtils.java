package tactical.utils.planner;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class UIUtils {
	public static void addWindowKeyListener(JComponent component, KeyStroke keyStroke, Supplier<?> toCall, Object text) {
		component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, text);
		component.getActionMap().put(text, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toCall.get();
			}		
		});
	}
}
