package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class PlannerTab implements ActionListener, TreeSelectionListener
{
	protected static final long serialVersionUID = 1L;

	protected Hashtable<String, PlannerContainerDef> containersByName;
	protected String[] containers;
	protected ArrayList<PlannerContainer> listPC;
	protected PlannerContainer currentPC;
	protected int selectedPC;
	protected JScrollPane currentPCScroll;
	protected JComboBox<String> typeComboBox;
	protected int refersTo;
	protected PlannerFrame plannerFrame;
	protected PlannerTree plannerTree;
	private JPanel listPanel;
	private String name;
	private JPanel uiAspect;
	private boolean renamingItem = false;
	private int tabIndex;

	public PlannerTab(String name, Hashtable<String, PlannerContainerDef> containersByName,
			String[] containers, int refersTo, PlannerFrame plannerFrame, int tabIndex)
	{
		uiAspect = new JPanel(new BorderLayout());

		this.name = name;
		this.tabIndex = tabIndex;
		listPanel = new JPanel();

		listPC = new ArrayList<PlannerContainer>();

		this.containersByName = containersByName;
		this.containers = containers;

		listPanel.setBackground(Color.DARK_GRAY);
		listPanel.add(PlannerFrame.createActionButton("Add", "add", this));
		listPanel.add(PlannerFrame.createActionButton("Remove", "remove", this));

		// this.add(listPanel, BorderLayout.LINE_START);
		// uiAspect.add(listPanel, BorderLayout.PAGE_START);

		typeComboBox = new JComboBox<String>(containers);

		this.refersTo = refersTo;
		this.plannerFrame = plannerFrame;

		plannerTree = new PlannerTree(name, listPC, this, new TabAttributeTransferHandler(listPC), this);
		uiAspect.add(plannerTree.getUiAspect(), BorderLayout.LINE_START);
	}

	@Override
	public void actionPerformed(ActionEvent al)
	{
		String command = al.getActionCommand();
		if (command.equalsIgnoreCase("add"))
		{
			addNewContainer();
		}
		else if (command.equalsIgnoreCase("remove"))
		{
			removeContainer(plannerTree.getSelectedIndex());
		} else if (command.equalsIgnoreCase("save")) {
			commitChanges();
			plannerFrame.updateErrorList(PlannerReference.getBadReferences(plannerFrame.getDataInputTabs()));
		}
	}

	public PlannerContainer addNewContainer()
	{
		String newName = JOptionPane.showInputDialog("Enter the new objects name");
		if (newName == null)
			return null;

		String type = containers[typeComboBox.getSelectedIndex()];
		PlannerContainer newPC = new PlannerContainer(containersByName.get(type), this, true);
		listPC.add(newPC);
		PlannerContainerDef pcd = containersByName.get(type);
		pcd.getDataLines().add(new PlannerReference(newName));
		newPC.getDefLine().getValues().add(newName);
		plannerTree.addItem(newName, listPC.size() - 1);
		return newPC;
	}

	public void duplicateContainer(int index)
	{
		String newName = JOptionPane.showInputDialog("Enter the new objects name");
		if (newName == null)
			return;

		PlannerContainer pc = listPC.get(index);
		PlannerContainer dupPC = pc.duplicateContainer(newName);
		listPC.add(dupPC);
		String type = containers[typeComboBox.getSelectedIndex()];
		PlannerContainerDef pcd = containersByName.get(type);
		// Update the "list of lists" that contain the name of every item definied so that they
		// may be refered to by REFER tags
		pcd.getDataLines().add(new PlannerReference(newName));
		plannerTree.addItem(newName, listPC.size() - 1);
		plannerTree.refreshItem(dupPC, listPC.size() - 1);
	}

	public void removeContainer(int index)
	{
		int rc = JOptionPane.showConfirmDialog(uiAspect,
				"Are you sure you'd like to delete the entire " + typeComboBox.getSelectedItem(),
				"Confirm " + typeComboBox.getSelectedItem() + " deletion?", JOptionPane.YES_NO_OPTION);
		if (rc != JOptionPane.OK_OPTION)
			return;
		plannerTree.removeItem(index);
		PlannerReference.removeReferences(refersTo, index);
		if (refersTo == PlannerValueDef.REFERS_TRIGGER)
			plannerFrame.getPlannerMap().removeReferences(true, index);
		else if (refersTo == PlannerValueDef.REFERS_TEXT)
			plannerFrame.getPlannerMap().removeReferences(false, index);
		plannerFrame.updateErrorList(PlannerReference.getBadReferences(plannerFrame.getDataInputTabs()));
		uiAspect.remove(currentPCScroll);
		uiAspect.repaint();

		currentPC = null;
		listPC.remove(index);
	}

	public void commitChanges()
	{
		/*
		System.out.println("PLANNER TAB COMMIT CHANGES");
		for (PlannerContainer pcs : listPC)
			pcs.commitChanges();
			*/
	}

	public void setNewValues()
	{

		if (currentPCScroll != null)
		{
			uiAspect.remove(currentPCScroll);
		}

		if (currentPC != null)
		{
			System.out.println("SET NEW VALUES COMMIT CHANGES");
			currentPC.commitChanges();
			plannerFrame.updateErrorList(PlannerReference.getBadReferences(plannerFrame.getDataInputTabs()));
			// Check to see if the description (name) has been renamed
			if (!currentPC.getDescription().equalsIgnoreCase(plannerTree.getTreeLabel(selectedPC)))
			{
				renamingItem = true;
				plannerTree.updateTreeLabel(selectedPC, currentPC.getDescription());
				renamingItem = false;
				currentPC.getPcdef().getDataLines().get(selectedPC).setName(currentPC.getDescription());
				uiAspect.validate();
			}
		}

		if (plannerTree.getSelectedIndex() != -1)
		{
			currentPC = listPC.get(plannerTree.getSelectedIndex());
			selectedPC = plannerTree.getSelectedIndex();
			if (plannerTree.getSelectedAttributeIndex() != -1)
				currentPC.setupUI(plannerTree.getSelectedAttributeIndex());
			else
				currentPC.setupUI();

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(currentPC.getUiAspect());
			currentPCScroll = new JScrollPane(panel);
			currentPCScroll.getVerticalScrollBar().setUnitIncrement(75);

			uiAspect.add(currentPCScroll, BorderLayout.CENTER);

			currentPCScroll.revalidate();
			uiAspect.validate();
			uiAspect.repaint();
		}
		else
		{
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			currentPCScroll = new JScrollPane(panel);
			uiAspect.add(currentPCScroll, BorderLayout.CENTER);
			uiAspect.revalidate();
		}
	}

	public void updateAttributeList(int index)
	{
		plannerTree.updateTreeValues(name, listPC);
		uiAspect.validate();
	}

	public void addPlannerContainer(PlannerContainer pc)
	{
		this.listPC.add(pc);
	}

	public ArrayList<PlannerContainer> getListPC() {
		return listPC;
	}

	public void clearValues()
	{
		this.currentPC = null;
		this.listPC.clear();
		// plannerTree.updateTreeValues(name, listPC);
		/*
		if (currentPCScroll != null)
		{
			this.remove(currentPCScroll);
		}

		System.out.println("SET NEW VALUES");

		if (currentPC != null && currentPC.getDefLine() != null)
		{
			System.out.println("REMOVE DEF LINE");
			this.remove(currentPC.getDefLine().getDefiningPanel());
		}

		for (int i = 0; i < this.getComponentCount(); i++)
		{
			if (this.getComponent(i) instanceof PlannerTimeBarViewer)
			{
				this.remove(i);
				i--;
			}
		}
		*/
		uiAspect.revalidate();
		uiAspect.repaint();
	}

	public boolean setSelectedListItem(int index, Integer leafIndex)
	{
		if (index < listPC.size())
		{
			if (leafIndex != null && leafIndex != -1) {
				this.plannerTree.setSelectedIndex(index, leafIndex);		
			} else {
				this.plannerTree.setSelectedIndex(index);
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean setSelectedListItem(PlannerContainer pc, PlannerLine pl)
	{
		int lineIndex = pc.getLines().indexOf(pl);
		if (lineIndex != -1)
			this.plannerTree.setSelectedIndex(listPC.indexOf(pc), pc.getLines().indexOf(pl));
		else
			this.plannerTree.setSelectedIndex(listPC.indexOf(pc));
		return true;
	}

	public class TabAttributeTransferHandler extends TreeAttributeTransferHandler
	{
		private static final long serialVersionUID = 1L;

		public TabAttributeTransferHandler(ArrayList<PlannerContainer> plannerPCs) {
			super(plannerPCs);
		}

		@Override
		public boolean importData(TransferSupport ts) {
			boolean rc = super.importData(ts);
			if (rc)
				updateAttributeList(newIndex);
			return rc;
		}

	}

	public PlannerContainerDef getPlannerContainerDef()
	{
		return containersByName.get(containers[typeComboBox.getSelectedIndex()]);
	}

	public PlannerContainer getCurrentPC() {
		return currentPC;
	}

	public Vector<String> getItemList() {
		return plannerTree.getItemList();
	}

	public void refreshItem(PlannerContainer plannerContainer)
	{
		if (plannerTree != null && listPC != null)
			plannerTree.refreshItem(plannerContainer, listPC.indexOf(plannerContainer));
	}

	public void addAttribute(String name, int index)
	{
		plannerTree.addAttribute(name, selectedPC, index);
	}

	public void addLineToContainerAtIndex(PlannerLine lineToAdd, int indexOfLine, int indexOfContainer)
	{
		listPC.get(indexOfContainer).addLine(lineToAdd, indexOfLine);
	}

	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		if (tse.getNewLeadSelectionPath() != null && 
				plannerTree.getSelectedIndex() != -1 && 
				!renamingItem)
		{
			setNewValues();
		}
	}

	public JPanel getUiAspect() {
		return uiAspect;
	}

	public String getName() {
		return name;
	}

	public int getTabIndex() {
		return tabIndex;
	}
	
	public PlannerContainer getPlannerContainerByReference(PlannerReference ref) {
		for (PlannerContainer pc : listPC) {
			if (((String) (pc.getDefLine().getValues().get(0))).equalsIgnoreCase(ref.getName()))
					return pc;
		}
		return null;
	}
}
