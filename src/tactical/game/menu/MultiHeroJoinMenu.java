package tactical.game.menu;

import java.util.List;

import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.constants.TextSpecialCharacters;
import tactical.game.listener.MenuListener;
import tactical.game.sprite.CombatSprite;

public class MultiHeroJoinMenu extends HeroesStatMenu implements MenuListener {
	
	public MultiHeroJoinMenu(List<CombatSprite> combatSpriteHeroOptions, StateInfo stateInfo) {
		super(PanelType.PANEL_MULTI_JOIN_CHOOSE, combatSpriteHeroOptions, null);
	}
	
	@Override
	protected MenuUpdate onBack(StateInfo stateInfo) {
		confirmHeroSelection(stateInfo);
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	protected MenuUpdate onConfirm(StateInfo stateInfo) {
		confirmHeroSelection(stateInfo);
		return MenuUpdate.MENU_NO_ACTION;
	}

	private void confirmHeroSelection(StateInfo stateInfo) {
		stateInfo.addMenu(new YesNoMenu("Are you sure you want to add " + selectedHero.getName() + " to the party?" , stateInfo, this));		
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {		
		if ((boolean) value) {
			stateInfo.removeMenu(this);
			stateInfo.getClientProfile().addHero(this.selectedHero);
			stateInfo.sendMessage(new SpeechMessage(selectedHero.getName() + " has joined your party!" + TextSpecialCharacters.CHAR_HARD_STOP));
		}
	}

	@Override
	public void menuClosed() {
		
	}
	
	@Override
	public boolean displayWhenNotTop() {
		return true;
	}
}
