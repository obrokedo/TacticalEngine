package tactical.utils.planner;

import java.awt.Dimension;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class PlannerAttributeList extends JScrollPane
{
	private JList<String> attributeList;
	private DefaultListModel<String> attributeListModel;

	private static final long serialVersionUID = 1L;

	public PlannerAttributeList(ListSelectionListener lsl, MouseListener ml)
	{
		attributeListModel = new DefaultListModel<String>();
		attributeList = new JList<String>(attributeListModel);
		this.setViewportView(attributeList);
		attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeList.addListSelectionListener(lsl);
		attributeList.addMouseListener(ml);
		attributeList.setDropMode(DropMode.INSERT);
		attributeList.setDragEnabled(true);
	}

	public PlannerAttributeList(PlannerContainer currentPC,
			ListSelectionListener lsl, AttributeTransferHandler transferHandler)
	{
		attributeListModel = new DefaultListModel<String>();
		for (PlannerLine pl : currentPC.getLines())
		{
			attributeListModel.addElement(pl.getPlDef().getName());
		}
		attributeList = new JList<String>(attributeListModel);
		this.setViewportView(attributeList);
		attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeList.addListSelectionListener(lsl);
		attributeList.setTransferHandler(transferHandler);
		attributeList.setDropMode(DropMode.INSERT);
		attributeList.setDragEnabled(true);
		this.setPreferredSize(new Dimension(200, 400));
	}

	public void updateAttributeList(PlannerContainer currentPC, int index, AttributeTransferHandler transferHandler)
	{
		attributeListModel.removeAllElements();
		if (currentPC != null)
		{
			String actorName = "";
			for (PlannerLine pl : currentPC.getLines())
			{
				for (int i = 0; i < pl.getPlDef().getPlannerValues().size(); i++) {
					if (pl.getPlDef().getPlannerValues().get(i).getTag().equalsIgnoreCase("name")) {
						actorName = (String) pl.getValues().get(i);
						break;
					}
				}
				attributeListModel.addElement(pl.getPlDef().getName() + " (" + actorName + ")");
			}
		}

		attributeList.setTransferHandler(transferHandler);

		if (attributeListModel.size() > index && index >= 0)
			attributeList.setSelectedIndex(index);

		attributeList.revalidate();
		this.revalidate();
		attributeList.repaint();
		this.repaint();
	}

	public int getSelectedIndex()
	{
		return attributeList.getSelectedIndex();
	}

	public void setSelectedIndex(int index)
	{
		attributeList.setSelectedIndex(index);
	}

	public int getListLength()
	{
		return attributeListModel.getSize();
	}
}
