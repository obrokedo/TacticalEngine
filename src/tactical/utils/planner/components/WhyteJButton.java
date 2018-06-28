package tactical.utils.planner.components;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public class WhyteJButton extends JButton implements MouseListener {
	private int overDuration = 0;

	public WhyteJButton(String text) {
		super(text);
		setCustomUI();
		this.addMouseListener(this);
	}
	
	private void setCustomUI() {
		setBackground(Color.white);
		setForeground(Color.BLACK);
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(5,  5,  5 , 5)));
		this.setFocusPainted(false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {	
		this.setBackground(new Color(0, 153, 255));		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.setBackground(Color.WHITE);
	}
}
