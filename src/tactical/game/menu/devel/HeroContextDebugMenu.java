package tactical.game.menu.devel;

import java.util.Arrays;

import tactical.engine.message.ShopMessage;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;

public class HeroContextDebugMenu extends ContextDebugMenu {
	private CombatSprite selectedSprite;
	
	public HeroContextDebugMenu(CombatSprite selectedSprite) {
		this.title = ("HERO DEBUG");
		this.selectedSprite = selectedSprite;
		this.options.addAll(Arrays.asList("Level Up", "Level Down", "+10 Exp", "Reset"));
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		MenuUpdate res = super.handleUserInput(input, stateInfo);
		if (res != MenuUpdate.MENU_NO_ACTION)
			return res;
		if (input.isKeyDown(KeyMapping.BUTTON_2))
			return MenuUpdate.MENU_CLOSE;
		else if (input.isKeyDown(KeyMapping.BUTTON_1) ||  input.isKeyDown(KeyMapping.BUTTON_3)) {
			switch (selectedIndex) {
				case 0: // LEVEL UP
					selectedSprite.levelUp();
					selectedSprite.initializeSprite(stateInfo.getResourceManager());
					break;
				case 1: // LEVEL DOWN
					selectedSprite.levelDown();
					selectedSprite.initializeSprite(stateInfo.getResourceManager());
					break;
				case 2: // +10 EXP
					selectedSprite.setExp((selectedSprite.getExp() + 10) % 100);
					break;
				case 3: // RESET
					selectedSprite.resetHero();
					selectedSprite.initializeSprite(stateInfo.getResourceManager());
					break;
			}		
			return MenuUpdate.MENU_ACTION_LONG;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}	
}