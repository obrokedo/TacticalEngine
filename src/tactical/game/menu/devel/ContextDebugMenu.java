package tactical.game.menu.devel;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.menu.Menu;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class ContextDebugMenu extends Menu {
	
	protected int selectedIndex = 0;
	protected String title;
	protected ArrayList<String> options = new ArrayList<>();

	public ContextDebugMenu() {
		super(PanelType.PANEL_CONTEXT_DEBUG);
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		if (input.isKeyDown(KeyMapping.BUTTON_DOWN)) {
			selectedIndex = (selectedIndex + 1) % options.size();
			return MenuUpdate.MENU_ACTION_LONG;
		} else if (input.isKeyDown(KeyMapping.BUTTON_UP)) {
			selectedIndex = (selectedIndex == 0 ? options.size() - 1 : selectedIndex - 1);
			return MenuUpdate.MENU_ACTION_LONG;
		} else if (input.isKeyDown(KeyMapping.BUTTON_2))
			return MenuUpdate.MENU_CLOSE;
		
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
			int stepSize = 15;
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(10, 10, 100, 26 + (Math.min(options.size(), stepSize) * 13), graphics, null);
			graphics.setColor(Panel.COLOR_FOREFRONT);
			StringUtils.drawString(title, 18, 9, graphics);
			int offset = 0;
	
			if (selectedIndex / stepSize > 0)
				offset = (selectedIndex / stepSize) * stepSize;
			for (int i = 0; i < Math.min(options.size() - offset, stepSize); i++)
				StringUtils.drawString(options.get(i + offset), 18, 22 + ((i) * 13), graphics);
			graphics.setColor(Color.red);
			graphics.drawRect(15, 30 + (selectedIndex - offset) * 13, 80, 10);
	}
}
