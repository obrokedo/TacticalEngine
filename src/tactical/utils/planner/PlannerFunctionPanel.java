package tactical.utils.planner;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PlannerFunctionPanel extends JPanel
{
	private static final long serialVersionUID = 1L;	
	
	private ArrayList<JTextField> textFields = new ArrayList<JTextField>();
	
	public PlannerFunctionPanel()
	{
		super(new FlowLayout(FlowLayout.LEFT));
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		contentPanel.add(new JLabel("Use this panel to define the mathematical battle functions."));				
		contentPanel.add(new JLabel("Supported functions/operators:"));
		contentPanel.add(new JLabel("Addition: '2 + 2'   Subtraction: '2 - 2'   Multiplication: '2 * 2'   Division: '2 / 2'/n"));
		contentPanel.add(new JLabel("Exponential: '2 ^ 2'   Unary Minus,Plus (Sign Operators): '+2 - (-2)'   Modulo: '2 % 2'"));
		contentPanel.add(new JLabel("abs: absolute value   acos: arc cosine   asin: arc sine   atan: arc tangent"));
		contentPanel.add(new JLabel("cbrt: cubic root   ceil: nearest upper integer   cos: cosine   cosh: hyperbolic cosine"));
		contentPanel.add(new JLabel("exp: euler's number raised to the power (e^x)   floor: nearest lower integer"));
		contentPanel.add(new JLabel("log: logarithmus naturalis (base e)   sin: sine   sinh: hyperbolic sine"));
		contentPanel.add(new JLabel("sqrt: square root   tan: tangent   tanh: hyperbolic tangent"));
		
		contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		JTextField tf = new JTextField(50);
		textFields.add(tf);
		contentPanel.add(new JLabel("Dodge Chance: This should be a value between 0-100 that represents the percent chance of dodging."));		
		contentPanel.add(tf);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tf = new JTextField(50);
		textFields.add(tf);
		contentPanel.add(new JLabel("Crit Chance: This should be a value between 0-100 that represents the percent chance of critting."));
		contentPanel.add(tf);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tf = new JTextField(50);
		textFields.add(tf);
		contentPanel.add(new JLabel("Experience Gained Attacking: The amount of experience that is gained from an attack action"));
		contentPanel.add(tf);
		
		contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tf = new JTextField(50);
		textFields.add(tf);
		contentPanel.add(new JLabel("Experience Gained Healing Spell: The amount of experience that is gained from an attack action"));
		contentPanel.add(tf);
		
		add(contentPanel);
	}	
}
