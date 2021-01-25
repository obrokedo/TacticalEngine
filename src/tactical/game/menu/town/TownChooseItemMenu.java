package tactical.game.menu.town;

import tactical.engine.state.StateInfo;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.ItemOption;

public class TownChooseItemMenu extends ChooseItemMenu {

	private ItemOption option;
	
	public TownChooseItemMenu(StateInfo stateInfo, int option) {
		super(stateInfo, null);
		this.option = ItemOption.values()[option];
	}
	
	@Override
	protected void itemSelected(StateInfo stateInfo) {
		
	}
}
