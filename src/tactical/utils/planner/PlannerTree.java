package tactical.utils.planner;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DropMode;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class PlannerTree
{
	private JTree attributeTree;
	private DefaultTreeModel attributeTreeModel;
	private DefaultMutableTreeNode rootNode;
	private JScrollPane uiAspect;
	private PlannerContainerDef containerDef;
	private ArrayList<PlannerContainer> plannerContainers;
	private PlannerTab parentTab;

	private static final long serialVersionUID = 1L;

	public PlannerTree(String rootName, ArrayList<PlannerContainer> listPC,
			MouseListener ml, TreeAttributeTransferHandler transferHandler,
			PlannerTab parentTab)
	{
		rootNode = new DefaultMutableTreeNode(rootName);
		attributeTreeModel = new DefaultTreeModel(rootNode);
		attributeTree = new JTree(attributeTreeModel);

		uiAspect = new JScrollPane();
		uiAspect.setViewportView(attributeTree);
		attributeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		attributeTree.addMouseListener(ml);
		attributeTree.setTransferHandler(transferHandler);
		attributeTree.setDropMode(DropMode.INSERT);
		attributeTree.setDragEnabled(true);
		attributeTree.addMouseListener(new TreeContextMenu());
		this.parentTab = parentTab;
		this.plannerContainers = listPC;

		uiAspect.setPreferredSize(new Dimension(150, 400));
		uiAspect.setMaximumSize(new Dimension(150, 400));
	}

	private class TreeContextMenu extends MouseAdapter implements ActionListener
	{
		private JPopupMenu contextMenu;
		private DefaultMutableTreeNode selectedNode;

		@Override
		public void mousePressed(MouseEvent me) {
			super.mousePressed(me);

			if (SwingUtilities.isRightMouseButton(me))
			{
				TreePath path = attributeTree.getPathForLocation(me.getX(), me.getY());
				if (path == null)
					return;
				
				attributeTree.setSelectionPath(path);

				selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

				if (rootNode == selectedNode)
				{
					contextMenu = new JPopupMenu();
					contextMenu.add(newMenuItem("Add Planner Item"));
				}
				else if (rootNode.getIndex(selectedNode) == -1)
				{
					contextMenu = new JPopupMenu();
					contextMenu.add(newMenuItem("Edit Planner Attribute"));
					contextMenu.add(newMenuItem("Remove Planner Attribute"));
					contextMenu.add(newMenuItem("Duplicate Planner Attribute"));
					contextMenu.add(new JSeparator());
					addAllowableLines();

				}
				else
				{
					contextMenu = new JPopupMenu();
					contextMenu.add(newMenuItem("Add Planner Item"));
					contextMenu.add(newMenuItem("Remove Planner Item"));
					contextMenu.add(newMenuItem("Duplicate Planner Item"));
					contextMenu.add(new JSeparator());
					addAllowableLines();
				}


				contextMenu.show(attributeTree, me.getX(), me.getY());
			}
		}

		private void addAllowableLines() {
			if (containerDef.getGroupingsForAllowableLine() == null) {
				for (PlannerLineDef pld : containerDef.getAllowableLines())
					contextMenu.add(newMenuItem(pld.getName()));
			} else {
				Vector<String> keys = new Vector<String>(containerDef.getGroupingsForAllowableLine().keySet());
				Collections.sort(keys);
				for (String key : keys) {
					JMenu subMenu = new JMenu(key);
					for (String val : containerDef.getGroupingsForAllowableLine().get(key)) {
						subMenu.add(newMenuItem(val));
					}
					contextMenu.add(subMenu);
				}
			}
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
			if (rootNode == selectedNode)
			{
				parentTab.addNewContainer();
			}
			else if (rootNode.getIndex(selectedNode) == -1)
			{
				int parentIndex = rootNode.getIndex(selectedNode.getParent());
				int nodeIndex = selectedNode.getParent().getIndex(selectedNode);
				if (e.getActionCommand().equalsIgnoreCase("Remove Planner Attribute"))
					plannerContainers.get(parentIndex).removeLine(nodeIndex);
				else if (e.getActionCommand().equalsIgnoreCase("Duplicate Planner Attribute"))
					plannerContainers.get(parentIndex).duplicateLine(nodeIndex);
				else if (e.getActionCommand().equalsIgnoreCase("Edit Planner Attribute")) {
					parentTab.editSelectedPlannerLine();
				}
				else
				{
					for (int i = 0; i < containerDef.getAllowableLines().size(); i++)
					{
						if (containerDef.getAllowableLines().get(i).getName().equalsIgnoreCase(e.getActionCommand()))
						{
							PlannerLine pl = new PlannerLine(containerDef.getAllowableLines().get(i), false);
							plannerContainers.get(parentIndex).addLine(pl, nodeIndex);
						}
					}
				}
			}
			else
			{
				if (e.getActionCommand().equalsIgnoreCase("Add Planner Item"))
				{
					parentTab.addNewContainer();
				}
				else if (e.getActionCommand().equalsIgnoreCase("Remove Planner Item"))
				{
					parentTab.removeContainer(rootNode.getIndex(selectedNode));
				}
				else if (e.getActionCommand().equalsIgnoreCase("Duplicate Planner Item"))
				{
					parentTab.duplicateContainer(rootNode.getIndex(selectedNode));
				}
				else
				{
					for (int i = 0; i < containerDef.getAllowableLines().size(); i++)
					{
						if (containerDef.getAllowableLines().get(i).getName().equalsIgnoreCase(e.getActionCommand()))
						{
							PlannerLine pl = new PlannerLine(containerDef.getAllowableLines().get(i), false);
							plannerContainers.get(rootNode.getIndex(selectedNode)).addLine(pl);
						}
					}
				}
			}
		}

	}
	
	public void updateTreeLabel(int index, String newVal)
	{
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) attributeTreeModel.getChild(attributeTreeModel.getRoot(), index);
		dmtn.setUserObject(newVal);
		attributeTreeModel.nodeChanged(dmtn);
	}
	
	public String getTreeLabel(int index) {
		return (String) ((DefaultMutableTreeNode) attributeTreeModel.getChild(attributeTreeModel.getRoot(), index)).getUserObject();
	}

	public void updateTreeValues(String rootName, ArrayList<PlannerContainer> listPC)
	{
		containerDef = parentTab.containersByName.get(parentTab.containers[0]);

		TreePath currentlySelected = attributeTree.getSelectionPath();
		DefaultMutableTreeNode item = null;
		
		int itemIndex = 0;
		while (rootNode.getChildCount() > 0)
			attributeTreeModel.removeNodeFromParent((MutableTreeNode) rootNode.getChildAt(0));
		for (PlannerContainer pc : listPC)
		{
			item = new DefaultMutableTreeNode(pc.getDefLine().getValues().get(0));
			attributeTreeModel.insertNodeInto(item, (MutableTreeNode) attributeTreeModel.getRoot(), itemIndex);
			int attIndex = 0;
			for (PlannerLine pl : pc.getLines())
			{
				attributeTreeModel.insertNodeInto(new DefaultMutableTreeNode(pl.getPlDef().getName()), item, attIndex);
				attIndex++;
			}
			itemIndex++;
		}

		this.plannerContainers = listPC;
		attributeTree.setSelectionPath(new TreePath(attributeTree.getModel().getRoot()));
		// attributeTree.setSelectionPath(currentlySelected);
	}

	public void refreshItem(PlannerContainer pc, int containerIndex)
	{
		if (((DefaultMutableTreeNode) attributeTreeModel.getRoot()).getChildCount() <= 0)
			return;
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) attributeTreeModel.getChild(rootNode, containerIndex);
		while (childNode.getChildCount() > 0)
			attributeTreeModel.removeNodeFromParent((MutableTreeNode) childNode.getChildAt(0));

		int attIndex = 0;
		DefaultMutableTreeNode firstNode = null;
		for (PlannerLine pl : pc.getLines())
		{
			if (firstNode == null)
			{
				firstNode = new DefaultMutableTreeNode(pl.getPlDef().getName());
				attributeTreeModel.insertNodeInto(firstNode, childNode, attIndex);
			}
			else
				attributeTreeModel.insertNodeInto(new DefaultMutableTreeNode(pl.getPlDef().getName()), childNode, attIndex);
			attIndex++;

		}


		attributeTree.expandPath(new TreePath(childNode.getPath()));
	}

	public int getSelectedIndex()
	{

		if (attributeTree.getSelectionPath() == null ||
				attributeTree.getSelectionPath().getLastPathComponent() == null)
			return -1;

		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) attributeTree.getSelectionPath().getLastPathComponent();
		if (dmtn.getParent() == null)
			return -1;
		else if (dmtn.getParent() == rootNode)
			return ((DefaultMutableTreeNode) attributeTreeModel.getRoot()).getIndex(dmtn);
		else
			return ((DefaultMutableTreeNode) attributeTreeModel.getRoot()).getIndex(dmtn.getParent());
	}

	public int getSelectedAttributeIndex()
	{
		if (attributeTree.getSelectionPath() == null ||
				attributeTree.getSelectionPath().getLastPathComponent() == null)
			return -1;

		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) attributeTree.getSelectionPath().getLastPathComponent();
		if (dmtn.getParent() == null)
			return -1;
		else if (dmtn.getParent() == rootNode)
			return -1;
		else
			return ((DefaultMutableTreeNode) dmtn.getParent()).getIndex(dmtn);
	}

	public void setSelectedIndex(int index)
	{
		Object root = attributeTree.getModel().getRoot();
		Object child = attributeTree.getModel().getChild(root, index);
		TreePath newPath = new TreePath(new Object[] {root, child});
		attributeTree.setSelectionPath(newPath);
		attributeTree.scrollPathToVisible(newPath);
	}
	
	public void setSelectedIndex(int index, int leafIndex)
	{
		Object root = attributeTree.getModel().getRoot();
		Object child = attributeTree.getModel().getChild(root, index);
		Object leaf = attributeTree.getModel().getChild(child, leafIndex);
		TreePath newPath = new TreePath(new Object[] {root, child, leaf});
		attributeTree.setSelectionPath(newPath);
		attributeTree.scrollPathToVisible(newPath);
	}

	public Vector<String> getItemList()
	{
		Vector<String> items = new Vector<>();
		for (int i = 0; i < attributeTree.getModel().getChildCount(rootNode); i++)
		{
			items.add(attributeTree.getModel().getChild(rootNode, i).toString());
		}
		return items;
	}

	public void addItem(String name, int itemIndex)
	{
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(name);
		attributeTreeModel.insertNodeInto(dmtn,
				rootNode, itemIndex);
		attributeTree.scrollPathToVisible(new TreePath(dmtn));
	}

	public void removeItem(int itemIndex)
	{
		attributeTreeModel.removeNodeFromParent((MutableTreeNode) attributeTreeModel.getChild(rootNode, itemIndex));
	}

	public void addAttribute(String name, int itemIndex, int attributeIndex)
	{
		DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(itemIndex);
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(name);
		attributeTreeModel.insertNodeInto(dmtn,
				child, attributeIndex);
		attributeTree.scrollPathToVisible(new TreePath(dmtn));
		

	}

	public JScrollPane getUiAspect() {
		return uiAspect;
	}
}
