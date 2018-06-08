package tactical.utils.planner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

public class PlannerLine
{
	private PlannerLineDef plDef;
	private ArrayList<Component> components;
	private ArrayList<Object> values;
	private boolean isDefining;
	private JComboBox<String> box;
	private JPanel uiAspect;

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

	public void setupUI(ArrayList<PlannerLineDef> allowableValues, ActionListener aListener,
			int index, ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType, PlannerTab parentTab)
	{
		setupUI(allowableValues, aListener,
				index, referenceListByReferenceType, true, true, parentTab);
	}

	public void setupUI(ArrayList<PlannerLineDef> allowableValues, ActionListener aListener,
			int index, ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType, 
			boolean displayButtons, boolean commitChanges, PlannerTab parentTab)
	{
		if (commitChanges)
			this.commitChanges();
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

			/**
			 * I believe that this is dead code I removed the "defining panel"
			 * 7-7-17... so if it continues to work this can be removed
			if (definingPanel == null)
			{
				System.out.println("CREATE NEW DEFIINING PANEL");
				Vector<String> allowableStrings = new Vector<String>();
				for (PlannerLineDef pld : allowableValues)
					allowableStrings.add(pld.getName());
				box = new JComboBox<String>(allowableStrings);
				box.setMaximumRowCount(30);
				headerPanel.add(box);

				JButton addLineButton = new JButton("Add line");
				addLineButton.setActionCommand("addline");
				addLineButton.addActionListener(aListener);
				headerPanel.add(addLineButton);

				if (plDef.getTag().equalsIgnoreCase("Cinematic"))
				{
					JButton refreshButton = new JButton("Refresh");
					refreshButton.setActionCommand("refresh");
					refreshButton.addActionListener(aListener);
					headerPanel.add(refreshButton);
				}
				definingPanel = headerPanel;
			}
			*/
		}
		else
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

			JTextArea descriptionArea = new JTextArea(this.plDef.getDescription());
			descriptionArea.setOpaque(false);
			descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.BOLD));
			descriptionArea.setForeground(Color.black);
			//descriptionArea.setLineWrap(true);
			//descriptionArea.setWrapStyleWord(true);
			descriptionArea.setEditable(false);
			descriptionArea.setBackground(Color.LIGHT_GRAY);
			descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
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
					if (pv.getRefersTo() == PlannerValueDef.REFERS_NONE)
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

						((NumberFormatter) ((JSpinner.NumberEditor) ((JSpinner) c).getEditor()).getTextField().getFormatter()).setAllowsInvalid(false);
					}
					else
					{
						Vector<String> items = new Vector<String>();
						items.add("No value selected");
						items.addAll(getReferenceStringList(referenceListByReferenceType, pv));
						c = new JComboBox<String>(items);
						if (values.size() > i)
							((JComboBox<?>) c).setSelectedItem(((PlannerReference) values.get(i)).getName());
					}
					break;
				case PlannerValueDef.TYPE_MULTI_STRING:
				case PlannerValueDef.TYPE_MULTI_INT:
					Vector<String> mitems = new Vector<String>();
					mitems.add("No value selected");
					mitems.addAll(getReferenceStringList(referenceListByReferenceType, pv));

					c = new MultiIntPanel(getReferenceStringList(referenceListByReferenceType, pv));
					JButton ab = new JButton("Add Item");
					ab.addActionListener((MultiIntPanel) c);
					ab.setActionCommand("ADD");
					c.add(ab);

					JButton rb = new JButton("Remove Last Item");
					rb.addActionListener((MultiIntPanel) c);
					rb.setActionCommand("REMOVE");
					c.add(rb);
					if (values.size() > i)
					{
						@SuppressWarnings("unchecked")
						ArrayList<PlannerReference> vals = ((ArrayList<PlannerReference>) values.get(i));

						for (PlannerReference plannerRef : vals)
						{
							JComboBox<String> jcb = new JComboBox<String>(mitems);
							if (plannerRef.getName().length() > 0)
								jcb.setSelectedItem(plannerRef.getName());
							c.add(jcb);
						}
					}
					else
						c.add(new JComboBox<String>(mitems));

					break;
				case PlannerValueDef.TYPE_BOOLEAN:
					c = new JCheckBox();
					if (values.size() > i && values.get(i) != null) {
						((JCheckBox) c).setSelected(Boolean.parseBoolean(values.get(i).toString()));
					}
					break;
				case PlannerValueDef.TYPE_STRING:
					if (pv.getRefersTo() == PlannerValueDef.REFERS_NONE)
					{
						c = new JTextField(30);
						if (values.size() > i)
						{
							try
							{
								((JTextField) c).setText((String) values.get(i));
							}
							catch (Throwable t)
							{
								System.out.println(t);
							}
						}
					}
					else
					{
						Vector<String> items = new Vector<String>(getReferenceStringList(referenceListByReferenceType, pv));
						// if (pv.isOptional())
						// We're going to leave the "" in so bad references don't default to something
							items.add(0, "");
						c = new JComboBox<String>(items);
						if (values.size() > i)
							((JComboBox<?>) c).setSelectedItem(((PlannerReference) values.get(i)).getName());
					}
					break;
				case PlannerValueDef.TYPE_LONG_STRING:
					JTextArea ta = new JTextArea(5, 40);

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
						c = new MultiStringPanel(((String) values.get(i)).split("<split>"));
					else
						c = new MultiStringPanel(new String[0]);
					break;

			}

			label.setToolTipText(pv.getDisplayDescription());
			c.setToolTipText(pv.getDisplayDescription());
			
			/*
			JLabel descriptionLabel = new JLabel(pv.getDisplayDescription());
			descriptionLabel.setOpaque(true);
			descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
			*/
			/*
			JTextArea descriptionArea = new JTextArea(pv.getDisplayDescription());
			descriptionArea.setOpaque(false);
			descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.BOLD));
			descriptionArea.setForeground(Color.black);
			descriptionArea.setLineWrap(true);
			descriptionArea.setWrapStyleWord(true);
			descriptionArea.setEditable(false);
			descriptionArea.setBackground(Color.GRAY);
			descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			*/
			// descriptionArea.setEnabled(false);
			
			// descriptionLabel.setBackground(Color.DARK_GRAY);
			JLabel descriptionLabel = new JLabel(convertToConstantWidth(pv.getDisplayDescription()));
			descriptionLabel.setOpaque(true);
			descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
			
			panel.add(descriptionLabel, BorderLayout.PAGE_START);
			panel.add(c);
			components.add(c);
			valuePanel.add(panel);
			valuePanel.add(Box.createRigidArea(new Dimension(5, 15)));
		}
		uiAspect.add(valuePanel);
	}
	
	private List<String> getReferenceStringList(ArrayList<ArrayList<PlannerReference>> referenceListByReferenceType,
			PlannerValueDef pv) {
		return referenceListByReferenceType.get(pv.getRefersTo() - 1).stream().map(referTo -> referTo.getName()).collect(Collectors.toList());
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

	public void commitChanges()
	{
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
						if (pv.getRefersTo() == PlannerValueDef.REFERS_NONE)
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
							if (i >= values.size())
								values.add(((JComboBox<?>) components.get(i)).getSelectedItem());
							else
								values.set(i, ((JComboBox<?>) components.get(i)).getSelectedItem());
						}
						break;
					case PlannerValueDef.TYPE_LONG_STRING:
						if (((JTextArea) components.get(i)).getText().trim().length() < 0 && !pv.isOptional())
						{
							throw new IllegalArgumentException();
						}

						if (i >= values.size())
							values.add(((JTextArea) components.get(i)).getText());
						else
							values.set(i, ((JTextArea) components.get(i)).getText());
						break;
					case PlannerValueDef.TYPE_UNBOUNDED_INT:
					case PlannerValueDef.TYPE_INT:
						if (pv.getRefersTo() == PlannerValueDef.REFERS_NONE)
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

						break;
					case PlannerValueDef.TYPE_MULTI_INT:
						multi = "";
						mip = (MultiIntPanel) components.get(i);
						for (int j = 2; j < mip.getComponentCount(); j++)
						{
							multi = multi + ((JComboBox<?>)mip.getComponent(j)).getSelectedIndex();
							if (j + 1 != mip.getComponentCount())
								multi = multi + ",";
						}

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
		
		ArrayList<String> badReferences = new ArrayList<>();
		PlannerReference.establishLineReference(PlannerFrame.referenceListByReferenceType, badReferences, null, this);
	}

	public ArrayList<Component> getPlannerLineComponents() {
		return components;
	}

	public void setComponents(ArrayList<Component> components) {
		this.components = components;
	}

	public int getSelectedItem()
	{
		System.out.println(box.getSelectedIndex());
		System.out.println(box);
		return box.getSelectedIndex();
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
				+ isDefining + ", box=" + box + ", uiAspect=" + uiAspect + "]";
	}
}
