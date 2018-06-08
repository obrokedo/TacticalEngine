package tactical.utils.planner;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeAttributeTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private final DataFlavor localObjectFlavor;
	private DefaultMutableTreeNode transferedObjects = null;
	private JTree selectedTree;
	protected int newIndex;
	private ArrayList<PlannerContainer> plannerPCs;

	public TreeAttributeTransferHandler(ArrayList<PlannerContainer> plannerPCs) {
		super();
		localObjectFlavor = new ActivationDataFlavor(
				DefaultMutableTreeNode.class, DataFlavor.javaJVMLocalObjectMimeType, "Item");
		this.plannerPCs = plannerPCs;
	}

	@Override
	public boolean canImport(TransferSupport arg0) {
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		selectedTree = (JTree) c;
	    transferedObjects =  (DefaultMutableTreeNode) selectedTree.getLastSelectedPathComponent();
	    // Don't allow items (as opposed to attributes) to be transferred
	    if (((DefaultMutableTreeNode) selectedTree.getModel().getRoot()).getIndex(transferedObjects) != -1)
	    {
	    	return null;
	    }

	    return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
	}

	@Override
	protected void exportDone(JComponent arg0, Transferable arg1, int arg2) {
		super.exportDone(arg0, arg1, arg2);
	}

	@Override
	public int getSourceActions(JComponent arg0) {
		return TransferHandler.MOVE;
	}

	@Override
	public boolean importData(TransferSupport ts) {

		JTree.DropLocation dl = (JTree.DropLocation) ts.getDropLocation();
		DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
		DefaultMutableTreeNode rootNode = ((DefaultMutableTreeNode) selectedTree.getModel().getRoot());
		DefaultTreeModel treeModel = ((DefaultTreeModel) selectedTree.getModel());

		int indexInRoot = rootNode.getIndex(dropNode);
		// We've dropped the node higher then it needs to be
		if (indexInRoot == -1)
		{
			System.out.println("DROP IN PARENT");

			if (dl.getChildIndex() <= 0)
			{
				System.out.println("Can't drop in non-item");
				return false;
			}
			// Really we want to drop this at the end of item at the previous index
			else
			{
				PlannerContainer oldPC = plannerPCs.get(rootNode.getIndex(transferedObjects.getParent()));
				PlannerContainer newPC = plannerPCs.get(dl.getChildIndex() - 1);
				PlannerLine lineToMove = oldPC.removeLine(transferedObjects.getParent().getIndex(transferedObjects));
				newPC.addLine(lineToMove);
			}
		}
		// We've actually got a valid location to insert at
		else
		{
			PlannerContainer oldPC = plannerPCs.get(rootNode.getIndex(transferedObjects.getParent()));
			PlannerContainer newPC = plannerPCs.get(indexInRoot);
			PlannerLine lineToMove = oldPC.removeLine(transferedObjects.getParent().getIndex(transferedObjects));
			newPC.addLine(lineToMove, dl.getChildIndex());
		}


		/*
		if (index != dl.getIndex())
		{
			if (currentPC == null)
				return false;

			ArrayList<PlannerLine> pls = currentPC.getLines();
			newIndex = dl.getIndex();
			if (dl.getIndex() < index)
			{
				pls.add(dl.getIndex(), pls.remove(index));
			}
			else
			{
				pls.add(--newIndex, pls.remove(index));
			}

			return true;
		}

		System.out.println(dl.getIndex() + " " + index);
		*/

		return false;
	}
}