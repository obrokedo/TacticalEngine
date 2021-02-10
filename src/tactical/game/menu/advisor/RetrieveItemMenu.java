package tactical.game.menu.advisor;

import java.util.ArrayList;

import tactical.engine.TacticalGame;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.menu.Portrait;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.shop.ShopBuyMenu;
import tactical.game.resource.ItemResource;
import tactical.game.trigger.Trigger;
import tactical.utils.StringUtils;

public class RetrieveItemMenu extends ShopBuyMenu {
	
	private Portrait portrait = null;
	
	public RetrieveItemMenu(StateInfo stateInfo) {
		super(stateInfo, null, false);
		
		this.portrait = Portrait.getPortrait(-1, -1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getAdvisorPortraitAnimFile(), stateInfo);
		
		ArrayList<Integer> stored = stateInfo.getClientProgress().getStoredItems();
		this.items = new Item[stored.size()];
		for (int i = 0; i < items.length; i++)
			items[i] = ItemResource.getItem(stored.get(i), stateInfo.getResourceManager());
		
		selectedItem = items[0];		
		itemName = StringUtils.splitItemString(selectedItem.getName());
		updateSelectedItem();
		showBuyPanel(stateInfo);
	}

	@Override
	protected void itemSelected(StateInfo stateInfo) {
		currentStep = ShopStepEnum.IS_COST_OK;
		this.valueSelected(stateInfo, Boolean.TRUE);
	}

	@Override
	protected void showBuyPanel(StateInfo stateInfo) {
		speechMenu = new SpeechMenu(menuConfig.getStorageWithdrawText(), stateInfo.getPaddedGameContainer(),
				Trigger.TRIGGER_NONE, null, null);
		currentStep = ShopStepEnum.SELECT_ITEM;
	}

	@Override
	protected Portrait getMenuPortrait(StateInfo stateInfo) {
		return portrait;
	}

	@Override
	protected void handleDealPurchase(StateInfo stateInfo) {
		stateInfo.getClientProgress().retrieveItem(selectedItemIndex);
		
		this.items = new Item[stateInfo.getClientProgress().getStoredItems().size()];
		for (int i = 0; i < stateInfo.getClientProgress().getStoredItems().size(); i++)
			this.items[i] = ItemResource.getItem(stateInfo.getClientProgress().getStoredItems().get(i), stateInfo.getResourceManager());
		if (selectedItemIndex > 0)
			selectedItemIndex--;
		if (items.length > 0) {
			this.selectedItem = items[selectedItemIndex];
			updateSelectedItem();
		}
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		switch (currentStep) {
			case SALE_COMPLETED:
				if (stateInfo.getClientProgress().getStoredItems().size() == 0) {
					stateInfo.sendMessage(new SpeechMessage(menuConfig.getStorageWithdrawNoItemsText(), 
							Trigger.TRIGGER_NONE, getMenuPortrait(stateInfo)));
					stateInfo.removeMenu(this);
				}
				showBuyPanel(stateInfo);
				break;
			default:
				super.valueSelected(stateInfo, value);
		}
	}

	@Override
	protected String getTransactionCompletedText() {
		return menuConfig.getStorageWithdrawnText();
	}
	
	
}
