package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import tactical.utils.planner.custom.MapReferencePanel;
import tactical.utils.planner.custom.MultiIntPanel;
import tactical.utils.planner.custom.MultiStringPanel;

public class PlannerLine implements FocusListener, ChangeListener, ItemListener
{
	private PlannerLineDef plDef;
	private ArrayList<Component> components;
	private ArrayList<Object> values;
	private boolean isDefining;
	private JPanel uiAspect;
	private LineCommitListener listener;
	private ReferenceStore referenceStore;

	public PlannerLine(PlannerLineDef plDef, boolean isDefining)
	{
		uiAspect = new JPanel();
		uiAspect.setLayout(new BoxLayout(uiAspect, BoxLayout.PAGE_AXIS));
		this.plDef = plDef;
		this.components = new ArrayList<Component>();
		this.values = new ArrayList<Object>();
		this.isDefining = isDefining;
	}

	public PlannerLine(PlannerLine plannerLine)
	{
		uiAspect = new JPanel();
		uiAspect.setLayout(new BoxLayout(uiAspect, BoxLayout.PAGE_AXIS));
		this.plDef = plannerLine.plDef;
		this.components = new ArrayList<Component>();
		this.values = new ArrayList<Object>();
		for (Object o : plannerLine.values)
			this.values.add(o);
		this.isDefining = plannerLine.isDefining;
	}

	public void setupUI(ActionListener aListener,
			int index, ReferenceStore referenceStore, PlannerTab parentTab)
	{
		setupUI(aListener,
				index, referenceStore, false, parentTab, true);
	}
	
	public void setupUI(ActionListener aListener,
			int index, ReferenceStore referenceStore, 
			boolean displayButtons, PlannerTab parentTab)
	{
		setupUI(aListener, index, referenceStore, displayButtons, parentTab, true);
	}
	
	public void setupUI(ActionListener aListener,
			int index, ReferenceStore referenceStore, 
			boolean displayButtons, PlannerTab parentTab, boolean showHeader)
	{
		this.referenceStore = referenceStore;
		ArrayList<String> badReferences = new ArrayList<>();
		PlannerReference.establishLineReference(
				referenceStore, badReferences, null, this);
		// this.commitChanges();
		components.clear();
		uiAspect.removeAll();
		JPanel headDescPanel = new JPanel(new BorderLayout());
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (isDefining)
		{
			headDescPanel.setBackground(Color.DARK_GRAY);
			headerPanel.setBackground(Color.DARK_GRAY);
		}
		else
		{
			headerPanel.setBackground(Color.LIGHT_GRAY);
			headDescPanel.setBackground(Color.LIGHT_GRAY);
		}
		JLabel headerLabel = new JLabel(plDef.getName().toUpperCase());
		headerPanel.add(headerLabel);
		if (isDefining)
		{
			headerLabel.setForeground(Color.WHITE);
		}
		else if (showHeader)
		{
			if (displayButtons)
			{
				JButton removeLineButton = new JButton("Remove " + plDef.getName());
				removeLineButton.setActionCommand("remove " + index);
				removeLineButton.addActionListener(aListener);
				headerPanel.add(removeLineButton);
				JButton moveupButton = new JButton("Move Up");
				moveupButton.setActionCommand("moveup " + index);
				moveupButton.addActionListener(aListener);
				headerPanel.add(moveupButton);
				JButton movedownButton = new JButton("Move Down");
				movedownButton.setActionCommand("movedown " + index);
				movedownButton.addActionListener(aListener);
				headerPanel.add(movedownButton);
				JButton copyButton = new JButton("Duplicate");
				copyButton.setActionCommand("duplicate " + index);
				copyButton.addActionListener(aListener);
				headerPanel.add(copyButton);
				JButton saveButton = new JButton("Save Values");
				saveButton.setActionCommand("save " + index);
				saveButton.addActionListener(aListener);
				headerPanel.add(saveButton);
			}
			
			JLabel descriptionLabel = new JLabel(convertToConstantWidth(this.plDef.getDescription()));
			
			headDescPanel.add(headerPanel.add(descriptionLabel), BorderLayout.PAGE_END);
			headDescPanel.add(headerPanel, BorderLayout.CENTER);
			uiAspect.add(headDescPanel);
		}

		JPanel valuePanel = new JPanel();
		valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.PAGE_AXIS));

		for (int i = 0; i < plDef.getPlannerValues().size(); i++)
		{

			PlannerValueDef pv = plDef.getPlannerValues().get(i);
			// JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel panel = new JPanel(new BorderLayout());
			JLabel label = new JLabel(pv.getDisplayTag() + (pv.isOptional() ? "(Optional)" : ""));
			label.setPreferredSize(new Dimension(150, 25));
			JComponent c = null;
			panel.add(label, BorderLayout.LINE_START);
			switch (pv.getValueType())
			{
				case PlannerValueDef.TYPE_UNBOUNDED_INT:
				case PlannerValueDef.TYPE_INT:
					if (pv.getRefersTo() == ReferenceStore.REFERS_NONE)
					{
						SpinnerNumberModel snm = null;

						if (pv.getValueType() == PlannerValueDef.TYPE_INT)
							snm = new SpinnerNumberModel(0, -1, Integer.MAX_VALUE, 1);
						else
							snm = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
												

						if (values.size() > i)
							snm.setValue(values.get(i));
						c = new JSpinner(snm);
						((JSpinner.NumberEditor) ((JSpinner) c).getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);

						((NumberFormatter) ((JSpinner.NumberEditor) ((JSpinner) c).getEditor()).getTextField().getFormatter()).setAllowsInvalid(true);
						snm.addChangeListener(this);
					}
					else
					{
						Vector<String> items = new Vector<String>();
						items.add("No value selected");
						
						items.addAll(referenceStore.getReferencesAsStrings(pv.getRefersTo() - 1));
						c = new JComboBox<String>(items);						
						if (values.size() > i)
							((JComboBox<?>) c).setSelectedItem(((PlannerReference) values.get(i)).getName());
						((JComboBox<?>) c).addItemListener(this);
						AutoCompletion.enable((JComboBox) c);
						((JComboBox<?>) c).setMaximumRowCount(20);
						
					}
					break;
				case PlannerValueDef.TYPE_MULTI_STRING:
					if (pv.getRefersTo() == ReferenceStore.REFERS_NONE) {
						if (values.size() > i && values.get(i) != null)
							c = new MultiStringPanel(((String) values.get(i)).split("<split>"), this, false);
						else
							c = new MultiStringPanel(new String[0], this, false);
						break;
					}
				case PlannerValueDef.TYPE_MULTI_INT:
					Vector<String> mitems = new Vector<String>();
					mitems.add("No value selected");
					mitems.addAll(referenceStore.getReferencesAsStrings(pv.getRefersTo() - 1));

					c = new MultiIntPanel(referenceStore.getReferencesAsStrings(pv.getRefersTo() - 1), this);
					JButton ab = new JButton("Add Item");
					ab.addActionListener((MultiIntPanel) c);
					ab.setActionCommand("ADD");
					c.add(ab);

					JButton rb = new JButton("Remove Last Item");
					rb.addActionListener((MultiIntPanel) c);
					rb.setActionCommand("REMOVE");
					c.add(rb);
					boolean hadAValue = false;
					if (values.size() > i)
					{
						@SuppressWarnings("unchecked")
						Iterator<PlannerReference> vals = ((ArrayList<PlannerReference>) values.get(i)).iterator();

						while (vals.hasNext())
						{
							PlannerReference plannerRef = vals.next();
							JComboBox<String> jcb = new JComboBox<String>(mitems);							;							
							jcb.setMaximumRowCount(20);
							if (plannerRef.getName().length() > 0) {
								jcb.setSelectedItem(plannerRef.getName());
								c.add(jcb);
								hadAValue = true;
							} else {
								vals.remove();
							}
							jcb.addItemListener(this);
							AutoCompletion.enable(jcb);
						}
						
						// Make sure at least one box is displayed even if it's empty
						if (!hadAValue) {
							JComboBox<String> jcb = new JComboBox<String>(mitems);
							jcb.addItemListener(this);
							AutoCompletion.enable(jcb);							
							c.add(jcb);
						}
					}
					else {
						JComboBox<String> jcb = new JComboBox<String>(mitems);
						jcb.addItemListener(this);
						AutoCompletion.enable(jcb);						
						c.add(jcb);
					}
					
					break;
				case PlannerValueDef.TYPE_BOOLEAN:					
					c = new JCheckBox();
					c.addFocusListener(this);
					if (values.size() > i && values.get(i) != null) {
						((JCheckBox) c).setSelected(Boolean.parseBoolean(values.get(i).toString()));
					}
					break;
				case PlannerValueDef.TYPE_STRING:
					if (pv.getRefersTo() == ReferenceStore.REFERS_NONE)
					{
						c = new JTextField(30);
						c.addFocusListener(this);
						if (values.size() > i)
						{
							((JTextField) c).setText((String) values.get(i));
						}
					}
					else
					{
						Vector<String> items = new Vector<String>(referenceStore.getReferencesAsStrings(pv.getRefersTo() - 1));
						// if (pv.isOptional())
						// We're going to leave the "" in so bad references don't default to something
						items.add(0, "");
						
						String selected = null;
						if (values.size() > i)
							selected = ((PlannerReference) values.get(i)).getName();
						
						if (pv.getRefersTo() != ReferenceStore.REFERS_MAPDATA) {
							c = new JComboBox<String>(items);											
							((JComboBox<?>) c).setMaximumRowCount(20);
							
							if (selected != null)
								((JComboBox<?>) c).setSelectedItem(selected);
							
							((JComboBox) c).addItemListener(this);
							AutoCompletion.enable((JComboBox) c);
						} else {
							c = new MapReferencePanel(this, items, selected);
						}
						
					}
					break;
				case PlannerValueDef.TYPE_LONG_STRING:
					JTextArea ta = new JTextArea(5, 40);
					ta.addFocusListener(this);
					ta.setWrapStyleWord(true);
					ta.setLineWrap(true);
					if (values.size() > i)
					{
						try
						{
							ta.setText((String) values.get(i));
						}
						catch (Throwable t)
						{
							System.out.println(t);
						}
					}

					c = ta;
					break;
				case PlannerValueDef.TYPE_MULTI_LONG_STRING:
					if (values.size() > i && values.get(i) != null)
						c = new MultiStringPanel(((String) values.get(i)).split("<split>"), this, true);
					else
						c = new MultiStringPanel(new String[0], this, true);
					break;

			}

			label.setToolTipText(pv.getDisplayDescription());
			c.setToolTipText(pv.getDisplayDescription());
			c.addFocusListener(this);
			
			
			
			//JLabel descriptionLabel = new JLabel(convertToConstantWidth(pv.getDisplayDescription()));
			//descriptionLabel.setOpaque(true);
			//descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
			
			// panel.add(descriptionLabel, BorderLayout.PAGE_START);
			components.add(c);
			
			if (plDef.getPanelLayout() == null) {
				panel.add(c);
				if (pv.getRefersTo() == ReferenceStore.REFERS_TRIGGER) {
					JButton createTrigger = new JButton("Create a new trigger");
					panel.add(createTrigger, BorderLayout.PAGE_END);
					createTrigger.addActionListener(new ActionListener() {						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (parentTab.getPlannerFrame().getPlannerTabAtIndex(
									PlannerFrame.TAB_TRIGGER).addNewContainer() != null)
								setupUI(aListener, index, referenceStore, displayButtons, parentTab, showHeader);
						}
					});
				} else if (pv.getRefersTo() == ReferenceStore.REFERS_TEXT) {
					JButton createSpeech = new JButton("Create new text");
					panel.add(createSpeech, BorderLayout.PAGE_END);
					createSpeech.addActionListener(new ActionListener() {						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (parentTab.getPlannerFrame().getPlannerTabAtIndex(
									PlannerFrame.TAB_TEXT).addNewContainer() != null)
								setupUI(aListener, index, referenceStore, displayButtons, parentTab, showHeader);
						}
					});
				}
				valuePanel.add(panel);
				valuePanel.add(Box.createRigidArea(new Dimension(5, 10)));
			}
		}
		if (plDef.getPanelLayout() != null)
			plDef.getPanelLayout().layoutPanel(valuePanel, components, plDef.getPlannerValues());
		uiAspect.add(valuePanel);
	}
	
	private String convertToConstantWidth(String str)
	{
		String[] splitDesc = str.split(" ");
		String newDesc = "<html>";
		
		String line = "";
		for (int i = 0; i < splitDesc.length; i++)
		{
			if (line.length() > 140)
			{
				line = line + "<br>";
				newDesc = newDesc + line;
				line = "";
			}
			
			line = line + " " + splitDesc[i];
		}
		newDesc = newDesc + line;
		newDesc = newDesc + "</html>";
		return newDesc;
	}
	
	public void commitChanges() {
		commitChanges(referenceStore);
	}

	public void commitChanges(ReferenceStore referenceStore)
	{
		//System.out.println("----------------- COMMIT");
		//for (Component c : components)
		//	System.out.println("   " + c);
		PlannerFrame.updateSave("Saved...");
		
		if (components.size() > 0)
		{
			for (int i = 0; i < plDef.getPlannerValues().size(); i++)
			{
				PlannerValueDef pv = plDef.getPlannerValues().get(i);

				switch (pv.getValueType())
				{
					case PlannerValueDef.TYPE_BOOLEAN:
						if (i >= values.size())
							values.add(((JCheckBox) components.get(i)).isSelected());
						else
							values.set(i, ((JCheckBox) components.get(i)).isSelected());
						break;
					case PlannerValueDef.TYPE_STRING:
						if (pv.getRefersTo() == ReferenceStore.REFERS_NONE)
						{
							if (((JTextField) components.get(i)).getText().trim().length() < 0 && !pv.isOptional())
							{
								throw new IllegalArgumentException();
							}

							if (i >= values.size())
								values.add(((JTextField) components.get(i)).getText());
							else
								values.set(i, ((JTextField) components.get(i)).getText());
						}
						else
						{
							Object val = null; 
							if (pv.getRefersTo() != ReferenceStore.REFERS_MAPDATA) 
								val = ((JComboBox<?>) components.get(i)).getSelectedItem();
							else
								val = ((MapReferencePanel) components.get(i)).getSelectedItem();
								
							if (i >= values.size())
								values.add(val);
							else
								values.set(i, val);
						}
						break;
					case PlannerValueDef.TYPE_LONG_STRING:
						if (((JTextArea) components.get(i)).getText().trim().length() < 0 && !pv.isOptional())
						{
							throw new IllegalArgumentException();
						}
						
						String textToCheck = ((JTextArea) components.get(i)).getText();
						if (textToCheck.contains("\n")) {
							JOptionPane.showMessageDialog(null, "Newlines have been stripped in the text field, verify that it still looks correct.");
							textToCheck = textToCheck.replaceAll("\n", "");
							((JTextArea) components.get(i)).setText(textToCheck);
						}
						
						
						if (i >= values.size())
							values.add(textToCheck);
						else
							values.set(i, textToCheck);
						break;
					case PlannerValueDef.TYPE_UNBOUNDED_INT:
					case PlannerValueDef.TYPE_INT:
						if (pv.getRefersTo() == ReferenceStore.REFERS_NONE)
						{
							if (i >= values.size())
								values.add((int) ((JSpinner) components.get(i)).getValue());
							else
								values.set(i, (int) ((JSpinner) components.get(i)).getValue());
						}
						else
						{
							if (i >= values.size())
								values.add(((JComboBox<?>) components.get(i)).getSelectedIndex());
							else
								values.set(i, ((JComboBox<?>) components.get(i)).getSelectedIndex());
						}
						break;
					case PlannerValueDef.TYPE_MULTI_STRING:
						if (pv.getRefersTo() == ReferenceStore.REFERS_NONE) {
							String multi = "";
							for (String text : ((MultiStringPanel) components.get(i)).getTextStrings()) {
								multi = multi + "<split>" + text;
							}
							multi = multi.replaceFirst("<split>", "");
							if (i >= values.size())
								values.add(multi);
							else
								values.set(i, multi);
						} else {
							String multi = "";
							MultiIntPanel mip = (MultiIntPanel) components.get(i);
							for (int j = 2; j < mip.getComponentCount(); j++)
							{
								multi = multi + ((JComboBox<?>)mip.getComponent(j)).getSelectedItem();
								if (j + 1 != mip.getComponentCount())
									multi = multi + ",";
							}
	
							if (i >= values.size())
								values.add(multi);
							else
								values.set(i, multi);
						}

						break;
					case PlannerValueDef.TYPE_MULTI_INT:
						String multi = "";
						MultiIntPanel mip = (MultiIntPanel) components.get(i);
						
						for (int j = 2; j < mip.getComponentCount(); j++)
						{														
							multi = multi + ((JComboBox<?>)mip.getComponent(j)).getSelectedIndex();
							if (j + 1 != mip.getComponentCount())
								multi = multi + ",";
						}
						
						if (multi.length() == 0)
							multi = "0";

						if (i >= values.size())
							values.add(multi);
						else							
							values.set(i, multi);

						break;
					case PlannerValueDef.TYPE_MULTI_LONG_STRING:
						multi = "";
						for (String text : ((MultiStringPanel) components.get(i)).getTextStrings()) {
							multi = multi + "<split>" + text;
						}
						multi = multi.replaceFirst("<split>", "");
						if (i >= values.size())
							values.add(multi);
						else
							values.set(i, multi);
						break;
				}
			}
		}
		
		// Clear out my components so I don't 're-commit'
		// this.components.clear();
		ArrayList<String> badReferences = new ArrayList<>();
		PlannerReference.establishLineReference(referenceStore, badReferences, null, this);
		if (listener != null)
			listener.lineCommitted();
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public boolean isDefining() {
		return isDefining;
	}

	public PlannerLineDef getPlDef() {
		return plDef;
	}

	public JPanel getUiAspect() {
		return uiAspect;
	}

	@Override
	public String toString() {
		return "PlannerLine [plDef=" + plDef + ", components=" + components + ", values=" + values + ", isDefining="
				+ isDefining + ", uiAspect=" + uiAspect + "]";
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		commitChanges(referenceStore);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		commitChanges(referenceStore);
	}

	public void setListener(LineCommitListener listener) {
		this.listener = listener;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		commitChanges(referenceStore);
	}
}
