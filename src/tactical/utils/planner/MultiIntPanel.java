package tactical.utils.planner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

public class MultiIntPanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	
	private List<String> mitems;
	private PlannerLine parentLine;
	
	public MultiIntPanel(List<String> mitems, PlannerLine parentLine) {
		super();
		this.mitems = mitems;
		this.parentLine = parentLine;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("ADD"))
		{
			Vector<String> vs = new Vector<String>();
			vs.add("No value selected");
			vs.addAll(mitems);
			JComboBox<String> jc = new JComboBox<String>(vs);
			jc.addFocusListener(parentLine);
			this.add(jc);			
		}
		else if (e.getActionCommand().equalsIgnoreCase("REMOVE") && this.getComponentCount() > 3)
		{
			for (int i = this.getComponentCount() - 1; i >= 0; i++)
			{
				if (this.getComponent(i) instanceof JComboBox<?>)
				{
					this.remove(i);
					break;
				}
			}
		}
		parentLine.commitChanges();
		this.revalidate();
		this.repaint();
	}
}
