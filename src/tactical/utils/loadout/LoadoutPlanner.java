package tactical.utils.loadout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class LoadoutPlanner extends JFrame {
	// Heroes
	// Levels
	// Items
	// Quests Completed
	
	public static void main(String args[]) {
		LoadoutPlanner lp = new LoadoutPlanner();
		lp.setupUI();
		lp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lp.setVisible(true);
	}
	
	public void setupUI() {
		JPanel backPanel = new JPanel(new BorderLayout());
		this.setContentPane(backPanel);
		
		JPanel battlePropertyHolderPanel = new JPanel(new BorderLayout());
		JPanel battlePropertyPanel = new JPanel();
		JPanel questPanel = new JPanel();		
		questPanel.setPreferredSize(new Dimension(questPanel.getPreferredSize().width, 200));
		battlePropertyHolderPanel.add(battlePropertyPanel);
		battlePropertyHolderPanel.add(questPanel, BorderLayout.PAGE_END);
		
		backPanel.add(battlePropertyHolderPanel);
		
		this.setMinimumSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.pack();
	}
}
