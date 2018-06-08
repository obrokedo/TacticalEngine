package tactical.game.menu;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Graphics;

import tactical.engine.state.StateInfo;
import tactical.game.constants.TextSpecialCharacters;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.MenuListener;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;

public class MultiHeroJoinMenu extends Menu implements MenuListener {
	private List<HeroStatMenu> heroMenus = new ArrayList<>();
	private int activeMenu = 0;
	
	public MultiHeroJoinMenu(List<CombatSprite> combatSpriteHeroOptions, StateInfo stateInfo) {
		super(PanelType.PANEL_MULTI_JOIN_CHOOSE);
		heroMenus = new ArrayList<>();
		combatSpriteHeroOptions.forEach(cs -> heroMenus.add(new HeroStatMenu(stateInfo.getPaddedGameContainer(), cs, stateInfo)));
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		if (activeMenu + 1 < heroMenus.size() && input.isKeyDown(KeyMapping.BUTTON_RIGHT)) {
			activeMenu++;
			return MenuUpdate.MENU_ACTION_SHORT;
		} else if (activeMenu > 0 && input.isKeyDown(KeyMapping.BUTTON_LEFT)) {
			activeMenu--;
			return MenuUpdate.MENU_ACTION_SHORT;
		} else if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3)) {
			stateInfo.addMenu(new YesNoMenu("Would you like to have " + "THE CLOWN" + " join the force?" + TextSpecialCharacters.CHAR_HARD_STOP, stateInfo, this));
			return MenuUpdate.MENU_ACTION_LONG;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		heroMenus.get(activeMenu).render(gc, graphics);
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		
	}

	@Override
	public void menuClosed() {
		// TODO Auto-generated method stub
		
	}
}
