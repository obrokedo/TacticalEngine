package tactical.utils.progression;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tactical.game.battle.LevelUpResult;
import tactical.game.exception.BadResourceException;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.utils.planner.AutoCompletion;

public class ProgressionFrame extends JFrame implements ActionListener {
	private JComboBox<String> heroNameBox;
	private JTextArea outputArea = new JTextArea();
	private JButton generate = new JButton("Generate");
	public ProgressionFrame() {
		setVisible(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setMinimumSize(new Dimension(200, 200));
		generate.addActionListener(this);
	}
	
	public void init() {
		JPanel backPanel = new JPanel(new BorderLayout());
		List<String> h = HeroResource.getHeroNames();
		heroNameBox = new JComboBox<String>(new Vector<String>(
				HeroResource.getHeroNames()));
		AutoCompletion.enable(heroNameBox);
		backPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
		JPanel topPanel = new JPanel();
		topPanel.add(heroNameBox);
		topPanel.add(generate);
		backPanel.add(topPanel, BorderLayout.PAGE_START);
		this.setContentPane(backPanel);
		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == generate) {
			String sel = (String) heroNameBox.getSelectedItem();
			CombatSprite cs = HeroResource.getHero(sel);
			if (cs.getEquippedWeapon() != null)
				cs.unequipItem(cs.getEquippedWeapon());
			if (cs.getEquippedRing() != null)
				cs.unequipItem(cs.getEquippedRing());
			List<String> output = new ArrayList<>();
			try {
				if (!cs.isPromoted()) {
					while (cs.getLevel() < 10) {
						LevelUpResult lur = cs.getHeroProgression().getLevelUpResults(cs, output);
						cs.getHeroProgression().levelUp(cs, lur, output);
					}
					cs.setPromoted(true, 0);
					output.add(System.lineSeparator() + "--------- PROMOTED ----------" + System.lineSeparator());
				}
				
				while (cs.getLevel() < 30) {
					LevelUpResult lur = cs.getHeroProgression().getLevelUpResults(cs, output);
					cs.getHeroProgression().levelUp(cs, lur, output);
				}
			} catch (BadResourceException bre) {
				JOptionPane.showMessageDialog(null, bre.getMessage() + " Progression generation will be stopped.");
			}
			
			StringBuffer sb = new StringBuffer();
			for (String s : output)
				sb.append(s + System.lineSeparator());
			this.outputArea.setText(sb.toString());
			this.outputArea.validate();
		}
	}
}
