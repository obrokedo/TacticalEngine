package tactical.utils.planner;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class AttributeTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private final DataFlavor localObjectFlavor;
	private String transferedObjects = null;
	private int index;
	protected int newIndex;
	private PlannerContainer currentPC;

	public AttributeTransferHandler(PlannerContainer currentPC) {
		super();
		localObjectFlavor = new ActivationDataFlavor(
			      String.class, DataFlavor.javaJVMLocalObjectMimeType, "Item");
		this.currentPC = currentPC;
	}

	@Override
	public boolean canImport(TransferSupport arg0) {
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JList<?> list = (JList<?>) c;
	    index = list.getSelectedIndex();
	    transferedObjects = (String) list.getSelectedValue();
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

		JList.DropLocation dl = (JList.DropLocation)ts.getDropLocation();
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

		return false;
	}
}
