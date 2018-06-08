package tactical.game.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tactical.engine.state.StateInfo;
import tactical.game.input.UserInput;
import tactical.game.listener.StringListener;
import tactical.game.ui.Button;
import tactical.game.ui.PaddedGameContainer;

public class UninitializedStringMenu extends StringMenu
{
	protected Button cinButton;

	public UninitializedStringMenu(GameContainer gc, String text,
			StringListener listener) {
		super(gc, text, listener);
		okButton = new Button(width / 2 - 60 + x, 235, 50, 20, "Town");
		cinButton = new Button(width / 2 + 20  + x, 235, 50, 20, "Cin");
	}
	
	public boolean handleMouseInput(int mouseX, int mouseY, boolean leftClick, int delta)
	{		
		if (okButton.handleUserInput(mouseX, mouseY, leftClick))
		{
			listener.stringEntered(textField.getText(), "OK");
			return true;
		}
		
		if (cinButton.handleUserInput(mouseX, mouseY, leftClick))
		{
			listener.stringEntered(textField.getText(), "CIN");
			return true;
		}
		textField.setFocus(true);
		return false;
	}
	
	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) 
	{
		/*
		if (okButton.handleUserInput(mouseX, mouseY, leftClick))
		{
			listener.stringEntered(textField.getText());
			return true;
		}
		textField.setFocus(true);
		*/
		return MenuUpdate.MENU_NO_ACTION;
	}


	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		graphics.setColor(Color.blue);
		graphics.fillRect(x, 150, width, 120);
		graphics.setColor(COLOR_FOREFRONT);
		graphics.drawString(text, x + 15, 165);
		okButton.render(graphics);
		cinButton.render(graphics);
		textField.render(gc, graphics);
	}	
}
