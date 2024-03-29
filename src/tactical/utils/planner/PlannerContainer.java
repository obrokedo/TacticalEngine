package tactical.utils.planner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PlannerContainer implements ActionListener, LineCommitListener
{
	private PlannerContainerDef pcdef;
	private ArrayList<PlannerLine> lines;
	private PlannerLine defLine;
	private PlannerTimeBarViewer plannerGraph = null;
	private PlannerTab parentTab;
	private JPanel uiAspect = new JPanel();

	public PlannerContainer(PlannerContainerDef pcdef, PlannerTab parentTab, boolean initializeValues) {
		super();
		uiAspect.setLayout(new BoxLayout(uiAspect, BoxLayout.PAGE_AXIS));
		this.pcdef = pcdef;

		this.defLine = new PlannerLine(pcdef.getDefiningLine(), true);		
		this.lines = new ArrayList<PlannerLine>();
		this.parentTab = parentTab;			
		
		if (initializeValues) {
			this.setupUI();
			this.commitChanges();
		}
		
		this.defLine.commitChanges(pcdef.getReferenceStore());
		this.defLine.setListener(this);
	}

	public PlannerContainer(String newName, PlannerContainer copyContainer)
	{
		super();
		uiAspect.setLayout(new BoxLayout(uiAspect, BoxLayout.PAGE_AXIS));
		this.pcdef = copyContainer.pcdef;

		this.defLine = new PlannerLine(copyContainer.defLine);
		this.defLine.getValues().set(0, newName);
		this.lines = new ArrayList<PlannerLine>();
		this.parentTab = copyContainer.parentTab;
		for (PlannerLine l : copyContainer.lines)
			this.lines.add(new PlannerLine(l));
	}

	public void setupUI()
	{
		setupUI(0);
	}

	public void setupUI(int index)
	{
		uiAspect.removeAll();
		uiAspect.validate();

		defLine.setupUI(this, 0, pcdef.getReferenceStore(), parentTab.getPlannerFrame());
		uiAspect.add(defLine.getUiAspect());

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		if (index != -1 && index < lines.size())
		{
			lines.get(index).setupUI(this, index + 1, pcdef.getReferenceStore(), parentTab.getPlannerFrame());
			lines.get(index).setListener(this);
			listPanel.add(lines.get(index).getUiAspect());
		}

		uiAspect.add(listPanel);
		uiAspect.validate();
		uiAspect.repaint();
		// this.add(new JScrollPane(listPanel));
	}

	public void addLine(PlannerLine line, boolean initializeValues)
	{
		this.lines.add(line);
		if (initializeValues) {
			this.setupUI(this.lines.size() - 1);
			this.commitChanges();
		}
		parentTab.refreshItem(this);
	}
	
	public void addLine(PlannerLine line)
	{
		this.addLine(line, true);
	}

	public void addLine(PlannerLine line, int addIndex)
	{
		this.lines.add(addIndex, line);
		this.setupUI(addIndex);
		this.commitChanges();
		parentTab.refreshItem(this);
	}

	public PlannerLine removeLine(int index)
	{
		PlannerLine pl = lines.remove(index);
		parentTab.refreshItem(this);
		parentTab.checkForErrorsAndRename(this);
		return pl;
	}

	public void duplicateLine(int index)
	{
		PlannerLine pl = lines.get(index);
		lines.add(index + 1, new PlannerLine(pl));
		parentTab.refreshItem(this);
	}

	public PlannerContainer duplicateContainer(String newName)
	{
		return new PlannerContainer(newName, this);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		String action = a.getActionCommand();
		if (action.startsWith("addline"))
		{
			int index = Integer.parseInt(action.substring(8));
			this.lines.add(new PlannerLine(pcdef.getAllowableLines().get(index), false));
			parentTab.addAttribute(pcdef.getAllowableLines().get(index).getName(), lines.size() - 1);
			// setupUI();
			uiAspect.revalidate();
			uiAspect.repaint();
		}
		else if (action.startsWith("refresh"))
		{
			setupUI();
			uiAspect.revalidate();
			uiAspect.repaint();
		}
		else if (action.startsWith("remove"))
		{
			int index = Integer.parseInt(action.split(" ")[1]) - 1;
			lines.remove(index);
			parentTab.refreshItem(this);
		}
		else if (action.startsWith("moveup"))
		{
			int index = Integer.parseInt(action.split(" ")[1]) - 1;
			if (index != 0)
			{
				PlannerLine pl = lines.remove(index);
				lines.add(index - 1, pl);
				parentTab.refreshItem(this);
			}
		}
		else if (action.startsWith("movedown"))
		{
			int index = Integer.parseInt(action.split(" ")[1]) - 1;
			if (index != lines.size() - 1)
			{
				PlannerLine pl = lines.remove(index);
				lines.add(index + 1, pl);
				pl.getUiAspect().repaint();
				parentTab.refreshItem(this);
			}
		}
		else if (action.startsWith("duplicate"))
		{
			int index = Integer.parseInt(action.split(" ")[1]) - 1;
			PlannerLine pl = lines.get(index);
			lines.add(index + 1, new PlannerLine(pl));
			parentTab.refreshItem(this);
		}
		else if (action.startsWith("save")) {
			int index = Integer.parseInt(action.split(" ")[1]) - 1;
			PlannerLine pl = lines.get(index);
			pl.commitChanges(pcdef.getReferenceStore());
			parentTab.refreshItem(this);
			uiAspect.revalidate();
			uiAspect.repaint();
		}
	}

	public void commitChanges()
	{
		for (PlannerLine pl : lines)
			pl.commitChanges(pcdef.getReferenceStore());
	}

	public PlannerContainerDef getPcdef() {
		return pcdef;
	}

	public String getDescription()
	{
		return (String) defLine.getValues().get(0);
	}

	public PlannerLine getDefLine() {
		return defLine;
	}

	public ArrayList<PlannerLine> getLines() {
		return lines;
	}

	public PlannerTimeBarViewer getPlannerGraph() {
		return plannerGraph;
	}

	public PlannerTab getParentTab() {
		return parentTab;
	}

	public JPanel getUiAspect() {
		return uiAspect;
	}

	@Override
	public void lineCommitted() {
		parentTab.checkForErrorsAndRename(this);
	}
}
