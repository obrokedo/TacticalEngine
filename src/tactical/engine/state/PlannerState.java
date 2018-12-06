package tactical.engine.state;

import java.awt.Font;
import java.io.File;

import javax.swing.JFileChooser;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerFrame;

public class PlannerState extends BasicGameState {

	private PlannerFrame plannerFrame = new PlannerFrame(null);
	private int lastX, lastY;
	private int listIndex = 0;
	
	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		JFileChooser fc = PlannerFrame.createFileChooser();
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File triggerFile = fc.getSelectedFile();

			plannerFrame.openFile(triggerFile);
		}
	}

	@Override
	public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
		int itemListx = 70;
		int itemListWidth = 200;
		int scrollBarWidth = 20;
		g.setColor(new Color(54, 57, 63));		
		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());	
		
		// Draw Item List BG
		g.setColor(new Color(47, 49, 54));
		g.fillRect(itemListx, 0, itemListWidth + scrollBarWidth, gc.getHeight());
		
		// Scrollbar
		g.setColor(new Color(66, 134, 244));
		g.fillRoundRect(itemListx + itemListWidth + 5, 60, 10, 30, 5);
		
		// Draw Button bar
		g.setColor(new Color(32, 34, 37));
		g.fillRect(0, 0, itemListx, gc.getHeight());
		g.fillRect(0, 55, gc.getWidth(), 2);
				
		g.setClip(itemListx, 0, 200, gc.getHeight());
		org.newdawn.slick.Font font = new TrueTypeFont(Font.decode("Times-New-Roman bold 16"), false);
		g.setFont(font);
		for (int containerIndex = listIndex; containerIndex < plannerFrame.getDataInputTabs().get(0).getListPC().size(); containerIndex++) {
			PlannerContainer pc = plannerFrame.getDataInputTabs().get(0).getListPC().get(containerIndex);						
			g.setColor(new Color(220, 220, 220));
			g.drawString(pc.getDescription(), itemListx + 10, 60 + 40 * (containerIndex - listIndex));			
		}
				
		g.clearClip();
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_PLANNER;
	}

	@Override
	public void mouseWheelMoved(int newValue) {
		super.mouseWheelMoved(newValue);
		if (newValue < 0)
			listIndex = Math.min(listIndex + 1, plannerFrame.getDataInputTabs().get(0).getListPC().size());
		else
			listIndex = Math.max(0, listIndex - 1);
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		super.mouseMoved(oldx, oldy, newx, newy);
		this.lastX = newx;
		this.lastY = newy;
	}

	
}
