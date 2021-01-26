package tactical.game.menu.town;

import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.ItemOption;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.YesNoMenu;
import tactical.game.menu.shop.HeroesBuyMenu;
import tactical.game.sprite.CombatSprite;

public class TownChooseItemMenu extends ChooseItemMenu implements MenuListener {

	protected enum TownStepEnum
	{
		GIVE_SELECT_ITEM,
		GIVE_SHOW_SPEECH,
		GIVE_SELECT_HERO,
		DROP_SELECT_ITEM,
		DROP_CONFIRM,
		EQUIP_SELECT
	}
	
	private ItemOption option;
	private TownStepEnum step;
	
	public TownChooseItemMenu(StateInfo stateInfo, int option) {
		super(stateInfo, null);
		this.option = ItemOption.values()[option];
		
		switch (this.option) {
			case GIVE:
				step = TownStepEnum.GIVE_SELECT_ITEM;
				break;
			case DROP:
				step = TownStepEnum.DROP_SELECT_ITEM;
				break;
			case EQUIP:
				step = TownStepEnum.EQUIP_SELECT;
		}
	}
	
	// An item has been selected from this menu
	@Override
	protected boolean itemSelected(StateInfo stateInfo) {
		switch (option) {
			case GIVE:
				step = TownStepEnum.GIVE_SHOW_SPEECH;
				stateInfo.addMenu(new SpeechMenu("Who will you give the " + selectedHero.getItem(selectingItemIndex).getName() + " to?<hardstop>", null, this, stateInfo));
				return true;
			case DROP:
				step = TownStepEnum.DROP_CONFIRM;
				stateInfo.addMenu(new YesNoMenu("Are you sure you want to drop the " + selectedHero.getItem(selectingItemIndex).getName() + "?<hardstop>", 
						stateInfo, this));
				return true;
			case EQUIP:
				
				break;
			case USE:
				break;
		}
		return false;
	}
	
	

	// An item has been selected from a menu we're listening to
	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		Item item = null;
		switch (step) {
			case GIVE_SHOW_SPEECH:
				step = TownStepEnum.GIVE_SELECT_HERO;
				stateInfo.addMenu(new HeroesBuyMenu(stateInfo, 
						this, selectedHero.getItem(selectingItemIndex)));
				break;
			case GIVE_SELECT_HERO:
				if (value != null) {
					CombatSprite target = (CombatSprite) value;
					stateInfo.addMenu(new SpeechMenu(
							"The " + selectedHero.getItem(selectingItemIndex).getName() + " now belongs to " + target.getName() + "<hardstop>", stateInfo));
					item = selectedHero.getItem(selectingItemIndex);
					selectedHero.removeItem(item);
					target.addItem(item);
				}
				break;
			case DROP_CONFIRM:
				boolean drop = (Boolean) value;
				if (drop) {
					item = selectedHero.getItem(selectingItemIndex);
					stateInfo.addMenu(new SpeechMenu(
							"The " + item.getName() + " has been discarded<hardstop>", stateInfo));
					selectedHero.removeItem(item);
				}
				break;
		}
	}

	// A menu we're listening to has been selected
	@Override
	public void menuClosed() {
		
	}
}
