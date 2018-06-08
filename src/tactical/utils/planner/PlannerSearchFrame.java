package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import tactical.utils.planner.PlannerFrame.SearchResult;

public class PlannerSearchFrame extends JFrame implements ActionListener {
	
	private List<SearchResult> searchResults;
	private JList<String> resultList;
	private PlannerFrame plannerFrame;
	
	public PlannerSearchFrame(PlannerFrame plannerFrame, List<SearchResult> searchResults) {
		this.searchResults = searchResults;
		this.setLayout(new BorderLayout());
		
		resultList = new JList<>(new SearchListModel());
		this.plannerFrame = plannerFrame;
		JPanel backPanel = new JPanel(new BorderLayout());
		backPanel.add(new JScrollPane(resultList));
		JButton goToButton =  new JButton("Go to item");
		goToButton.addActionListener(this);
		backPanel.add(goToButton, BorderLayout.PAGE_END);
		this.setContentPane(backPanel);
		this.setPreferredSize(new Dimension(600, 400));
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
	
	private class SearchListModel implements ListModel<String> {

		@Override
		public void addListDataListener(ListDataListener l) {}

		@Override
		public String getElementAt(int index) {
			SearchResult sr = searchResults.get(index);
			return (sr.file != null ? sr.file + " -> " : "") + sr.pt.getName() + " -> " + sr.pc.getDefLine().getValues().get(0) + " -> " + sr.pl.getPlDef().getName() + " -> " + sr.value;
		}

		@Override
		public int getSize() {
			// TODO Auto-generated method stub
			return searchResults.size();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {}
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		int idx = resultList.getSelectedIndex();
		if (idx > -1) {
			SearchResult sr = searchResults.get(idx);
			if (sr.file != null)
			{
				int result = JOptionPane.showConfirmDialog(this, "This value is in the file " + sr.file 
						+ " would you like to open that file now (Unsaved data will be lost)?", "Load File", JOptionPane.OK_CANCEL_OPTION);
				if (result != JOptionPane.OK_OPTION)
					return;
				plannerFrame.openFile(new File(PlannerIO.PATH_MAPDATA + "/" + sr.file).getAbsoluteFile());
				
				plannerFrame.setSelectedTabIndex(sr.pt.getTabIndex());
				plannerFrame.getPlannerTabAtIndex(sr.pt.getTabIndex()).setSelectedListItem(sr.pt.getListPC().indexOf(sr.pc), sr.pc.getLines().indexOf(sr.pl));
			} else {
				plannerFrame.setSelectedTabIndex(sr.pt.getTabIndex());
				sr.pt.setSelectedListItem(sr.pc, sr.pl);
			}
		}
	}
}
