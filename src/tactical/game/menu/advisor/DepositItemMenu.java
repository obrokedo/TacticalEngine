package tactical.game.menu.advisor;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.Portrait;
import tactical.game.menu.SpeechMenu;
import tactical.game.trigger.Trigger;

public class DepositItemMenu extends ChooseItemMenu {

	private Portrait portrait = null;
	
	public DepositItemMenu(StateInfo stateInfo, MenuListener listener) {
		super(stateInfo, listener);
		this.portrait = Portrait.getPortrait(-1, -1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getAdvisorPortraitAnimFile(), stateInfo);
	}

	@Override
	protected boolean itemSelected(StateInfo stateInfo) {
		Item item = selectedHero.getItem(selectingItemIndex);
		stateInfo.addMenu(new SpeechMenu(menuConfig.getStorageDepositedText(item.getName()), Trigger.TRIGGER_LIST_NONE, portrait, stateInfo));
		stateInfo.getClientProgress().depositItem(item);
		selectedHero.removeItem(item);
		this.selectingItemState = false;
		this.updateHeroItems();
		return false;
	}
}
