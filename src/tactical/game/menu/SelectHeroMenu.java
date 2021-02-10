package tactical.game.menu;

import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.MenuListener;
import tactical.game.sprite.CombatSprite;

public class SelectHeroMenu extends HeroesStatMenu {

	public SelectHeroMenu(Iterable<CombatSprite> chooseableSprites, StateInfo stateInfo,
			MenuListener listener) {
		super(PanelType.PANEL_SELECT_HERO, chooseableSprites, stateInfo, listener);
	}
	
	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		if (input.isKeyDown(KeyMapping.BUTTON_3)) {
			return MenuUpdate.MENU_CLOSE;
		}
		else
			return super.handleUserInput(input, stateInfo);
	}

	@Override
	public Object getExitValue() {
		return selectedHero;
	}
}
