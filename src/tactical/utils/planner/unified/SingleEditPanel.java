package tactical.utils.planner.unified;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerContainerDef;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerLineDef;

public class SingleEditPanel extends JPanel implements ActionListener {
	
	private PlannerContainer pc;
	private JComboBox<String> jcb;
	
	public SingleEditPanel(PlannerContainer pc) {
		super(new BorderLayout());
		this.pc = pc;
		
		setupUI();
		JPanel jp = this;
		
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				Window window = SwingUtilities.getWindowAncestor(jp);
				if (window instanceof Dialog) {
					Dialog dialog = (Dialog) window;
					if (!dialog.isResizable()) {
						dialog.setResizable(true);
					}
				}
			}
		});
		this.setMaximumSize(new Dimension(this.getPreferredSize().width, Integer.MAX_VALUE));
	}
	
	private void setupUI() {
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.PAGE_AXIS));
		PlannerContainerDef pcdef = pc.getPcdef();		
		PlannerLine plDef = pc.getDefLine();
		plDef.setupUI(pcdef.getAllowableLines(), this, 0, pcdef.getListOfLists(), true, null);
		boxPanel.add(plDef.getUiAspect());
		int cnt = 1;
		for (PlannerLine pl : pc.getLines()) {
			pl.setupUI(pcdef.getAllowableLines(), this, cnt++, pcdef.getListOfLists(), true, null);
			boxPanel.add(pl.getUiAspect());
		}						
		jcb = new JComboBox<>();
		for (PlannerLineDef pld : pc.getPcdef().getAllowableLines())
			jcb.addItem(pld.getName());
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JLabel("Add new entry:"));
		buttonPanel.add(jcb);
		JButton button = new JButton("Add");
		button.setActionCommand("addbutton");
		button.addActionListener(this);
		buttonPanel.add(button);
		this.add(buttonPanel, BorderLayout.PAGE_START);
		this.add(boxPanel,BorderLayout.CENTER);
	}
	
	private JMenuItem newMenuItem(String item)
	{
		JMenuItem jmi = new JMenuItem(item);
		jmi.setActionCommand(item);
		jmi.addActionListener(this);
		return jmi;
	}

	

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("addbutton".equalsIgnoreCase(e.getActionCommand()))
			pc.addLine(new PlannerLine(pc.getPcdef().getAllowableLines().get(jcb.getSelectedIndex()), false));		 
		else
			pc.actionPerformed(e);
		
		this.removeAll();
		setupUI();
	}
}
