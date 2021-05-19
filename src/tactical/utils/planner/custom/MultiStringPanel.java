package tactical.utils.planner.custom;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tactical.utils.planner.PlannerLine;

public class MultiStringPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private List<JTextArea> textAreas;
	private JPanel textBoxPanel;
	private PlannerLine parentLine;
	private boolean longString = false;
	
	public MultiStringPanel(String[] strings, PlannerLine parentLine, boolean longString) {
		this.setLayout(new BorderLayout());
		this.longString = longString;
		textAreas = new ArrayList<>();
		JPanel buttonPanel = new JPanel();
		JButton addButton = new JButton("Add New Box");
		JButton removeButton = new JButton("Remove Last Box");
		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		addButton.setActionCommand("add");
		removeButton.setActionCommand("remove");
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		this.add(buttonPanel, BorderLayout.PAGE_START);
		
		textBoxPanel = new JPanel();
		textBoxPanel.setLayout(new BoxLayout(textBoxPanel, BoxLayout.PAGE_AXIS));
		this.parentLine = parentLine;
		for (String s : strings) 
			addTextArea(s);
		
		this.add(textBoxPanel, BorderLayout.CENTER);
	}
	
	private void addTextArea(String s) {
		JTextArea jta = new JTextArea((longString ? 5 : 1), 40);			
		jta.setWrapStyleWord(true);
		jta.setLineWrap(true);
		jta.setText(s);
		jta.addFocusListener(parentLine);
		textAreas.add(jta);
		textBoxPanel.add(jta);
		this.validate();
		this.repaint();
	}
	
	private void removeTextArea() {
		if (textAreas.size() == 0)
			return;
		JComponent jta = textAreas.remove(0);
		textBoxPanel.remove(jta);
		this.validate();
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if (a.getActionCommand().equalsIgnoreCase("add")) {
			addTextArea("");
		} else if (a.getActionCommand().equalsIgnoreCase("remove")) {
			removeTextArea();
		}
	}
	
	public List<String> getTextStrings() {
		List<String> texts = new ArrayList<>();
		for (JTextArea textArea : textAreas) {
			String textToCheck = textArea.getText();
			if (textToCheck.contains("\n")) {
				JOptionPane.showMessageDialog(null, "Newlines have been stripped in the text field, verify that it still looks correct.");
				textToCheck = textToCheck.replaceAll("\n", "");
				textArea.setText(textToCheck);	
			}
			texts.add(textToCheck);
		}
		return texts;
	}
}
