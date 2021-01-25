package tactical.engine.config.provided;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.config.YesNoMenuRenderer;
import tactical.engine.state.StateInfo;
import tactical.game.ui.PaddedGameContainer;
import tactical.game.ui.RectUI;
import tactical.game.ui.SelectRectUI;
import tactical.game.ui.TextUI;

public class DefaultYesNoRenderer implements YesNoMenuRenderer {

	private RectUI yesPanel, noPanel;
	private TextUI yesText, noText;
	private SelectRectUI selectRect;
	
	@Override
	public void initialize(StateInfo stateInfo) {
		yesPanel = new RectUI(120, 146, 32, 32);
		noPanel = new RectUI(170, 146, 32, 32);
		yesText = new TextUI("Yes", 125, 148);
		noText = new TextUI("No", 179, 148);
		selectRect = new SelectRectUI(120, 146, 32, 32);
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		// Draw background
		yesPanel.drawPanel(graphics);
		noPanel.drawPanel(graphics);
		// Draw temporary YES - NO
		graphics.setColor(Color.white);
		yesText.drawText(graphics);
		noText.drawText(graphics);

		// Draw selection square
		selectRect.draw(graphics, Color.red);
	}

	@Override
	public void update(long delta, StateInfo stateInfo) {
				
	}

	@Override
	public void yesPressed() {
		selectRect.setX(120);
	}

	@Override
	public void noPressed() {
		selectRect.setX(170);
	}
	
}
