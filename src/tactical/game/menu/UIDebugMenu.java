package tactical.game.menu;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import tactical.game.Timer;
import tactical.game.ui.Button;
import tactical.game.ui.ListUI;
import tactical.game.ui.PaddedGameContainer;
import tactical.game.ui.ResourceSelector;

public class UIDebugMenu
{
	private enum UIDebugState
	{
		MAIN_MENU,
		MAP,
		MAP_TEXT,
		MAP_ENTER,
		CIN,
		CIN_TEXT,
		CIN_ID,
		BAT,
		BAT_TEXT,
	}
	
	private static final Color MENU_COLOR = new Color(0, 0, 0, 120);
	private List<Button> rootButtons = new ArrayList<>();
	private Button backButton = new Button(10, 10, 20, 10, "Back", true);
	private UIDebugState currentState;
	private Timer clickCooldown = new Timer(200);
	private ListUI listUI = null;
	
	public UIDebugMenu()
	{
		this.currentState = UIDebugState.MAIN_MENU;
		
		backButton.setVisible(false);
		
		rootButtons.add(new Button(10, 10, 80, 10, "Load Map", true));
		
		rootButtons.add(new Button(10, 30, 80, 10, "Load Cinematic", true));
		
		rootButtons.add(new Button(10, 50, 80, 10, "Load Battle", true));
		
		rootButtons.add(new Button(10, 70, 80, 10, "Heal", true));
		
		rootButtons.add(new Button(10, 90, 80, 10, "Set Heroes to 1 HP", true));
		
		rootButtons.add(new Button(10, 110, 80, 10, "Gain Level", true));
		
		rootButtons.add(new Button(10, 130, 80, 10, "Mute", true));
		
		rootButtons.add(new Button(10, 150, 80, 10, "Reload Scripts", true));
	}
	
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		int mouseX = (container.getInput().getMouseX() - PaddedGameContainer.GAME_SCREEN_PADDING) / PaddedGameContainer.GAME_SCREEN_SCALE;
		int mouseY = container.getInput().getMouseY() / PaddedGameContainer.GAME_SCREEN_SCALE;
		
		this.clickCooldown.update(delta);
		
		for (Button button : rootButtons) {
			if (button.handleUserInput(mouseX, mouseY, container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) && clickCooldown.perform())
			{
				this.handleButtonPush(button.getText(), container);
			}
		}
		
		if (backButton.handleUserInput(mouseX, mouseY, container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) && clickCooldown.perform())
			this.handleButtonPush(backButton.getText(), container);
		
		if (listUI != null)
			listUI.update(container, delta);
	}
	
	public void render(Graphics g) {
		g.setColor(MENU_COLOR);
		g.fillRect(0, 0, 100, PaddedGameContainer.GAME_SCREEN_SIZE.height);
		
		if (currentState == UIDebugState.MAIN_MENU)
			for (Button button : rootButtons)
				button.render(g);
		else	
			backButton.render(g);
		
		if (listUI != null)
			listUI.render(g);
	}
	
	private void handleButtonPush(String buttonPushed, GameContainer container) {
		if (buttonPushed.equalsIgnoreCase("Load Map")) {
			this.currentState = UIDebugState.MAP;
			toggleMainButtons(false);
			listUI = new ResourceSelector("Select Map", 0, true, "map", ".tmx", container);
		} else if (buttonPushed.equalsIgnoreCase("Load Cinematic")) {
			this.currentState = UIDebugState.CIN;
			toggleMainButtons(false);
		} else if (buttonPushed.equalsIgnoreCase("Load Battle")) {
			this.currentState = UIDebugState.BAT;
			toggleMainButtons(false);
		} else if (buttonPushed.equalsIgnoreCase("Back")) {
			if (currentState == UIDebugState.MAP || 
				currentState == UIDebugState.CIN ||
				currentState == UIDebugState.BAT)
			{
				this.currentState = UIDebugState.MAIN_MENU;
				this.listUI = null;
				toggleMainButtons(true);
			}
		}
	}
	
	private void toggleMainButtons(boolean showMain)
	{
		this.backButton.setVisible(!showMain);
		for (Button button : rootButtons)
			button.setVisible(showMain);
	}
	
}