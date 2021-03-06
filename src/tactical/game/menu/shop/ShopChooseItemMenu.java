package tactical.game.menu.shop;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.item.Item.ItemDurability;
import tactical.game.listener.MenuListener;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.YesNoMenu;
import tactical.game.trigger.Trigger;
import tactical.game.ui.RectUI;
import tactical.game.ui.TextUI;

public class ShopChooseItemMenu extends ChooseItemMenu implements MenuListener
{
	private ShopMessage shopMessage;
	private boolean isSellMenu = false;
	
	protected RectUI goldPanel;
	protected TextUI goldTitleText, goldAmountText;

	public ShopChooseItemMenu(StateInfo stateInfo, MenuListener listener, ShopMessage shopMessage) {
		super(stateInfo, listener);
		this.shopMessage = shopMessage;
		
		if (shopMessage.getMessageType() == MessageType.SHOW_SHOP_SELL)
			isSellMenu = true;
		else
			isSellMenu = false;
		
		goldPanel = new RectUI(20, 89, 62, 32);
		goldTitleText = new TextUI("Gold", 27, 85);
		goldAmountText = new TextUI(stateInfo.getClientProfile().getGold() + "", 27, 97);
	}

	@Override
	protected void postRender(Graphics g) {
		super.postRender(g);
		
		// Draw gold box
		goldPanel.drawPanel(g);
		g.setColor(Color.white);
		goldTitleText.drawText(g);
		goldAmountText.drawText(g);
	}

	@Override
	protected boolean itemSelected(StateInfo stateInfo) {
		if (isSellMenu)
			promptSellItem(stateInfo);	
		else
			promptRepairItem(stateInfo);
		return false;
	}

	private void promptSellItem(StateInfo stateInfo) {
		Item item = items.get(selectingItemIndex);
		String sellText = null;
		if (item.isDeal()) {
			sellText = menuConfig.getShopPromptSellDealText(item.getName(), (int) (item.getCost() * shopMessage.getSellPercent()) + "");
		} else {
			sellText = menuConfig.getShopPromptSellNormalText(item.getName(), (int) (item.getCost() * shopMessage.getSellPercent()) + "");
		}
		stateInfo.addMenu(new YesNoMenu(sellText, Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo), stateInfo, this));
	}
	
	private void promptRepairItem(StateInfo stateInfo) {
		Item item = items.get(selectingItemIndex);
		switch (item.getDurability()) {
		case BROKEN:
			
			stateInfo.addMenu(new YesNoMenu(menuConfig.getShopPromptRepairBrokenText(item.getName(), (int) (item.getCost() * .5) + ""),
					Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo), stateInfo, this));
			break;
		case DAMAGED:
			stateInfo.addMenu(new YesNoMenu(menuConfig.getShopPromptRepairDamagedText(item.getName(), (int) (item.getCost() * .2) + ""), 
					Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo),stateInfo, this));
			break;
		case PERFECT:
			stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopItemNotDamagedText(item.getName()), 
					Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
			break;
		default:
			break;
		
		}
	}

	@Override
	public Object getExitValue() {
		if (selectedHero != null)
			return new Object[] {selectedHero, selectingItemIndex};
		else
			return null;
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		if ((boolean) value) {
			if (isSellMenu)
				sellItem(stateInfo);
			else
				repairItem(stateInfo);
		} else {
			if (isSellMenu)
				stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopTransactionCancelledText(), 
						Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
			else
				stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopRepairCancelledText(), 
						Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
		}
		selectingItemState = false;
	}

	private void sellItem(StateInfo stateInfo) {
		Item item = items.get(selectingItemIndex);
		if (selectedHero.getEquipped().get(selectingItemIndex)) {
			selectedHero.unequipItem((EquippableItem) item);
		}
		selectedHero.removeItem(item);
		this.updateCurrentHero(stateInfo);
		if (item.isDeal()) {
			stateInfo.getClientProgress().getDealItems().add(item.getItemId());
		}
		stateInfo.getClientProfile().setGold(stateInfo.getClientProfile().getGold() + (int) (item.getCost() * shopMessage.getSellPercent()));
		goldAmountText.setText(stateInfo.getClientProfile().getGold() + "");
		stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopTransactionSuccessfulText(), 
				Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
	}
	
	private void repairItem(StateInfo stateInfo) {
		
		Item item = selectedHero.getItem(selectingItemIndex);
		int cost = 0;
		if (item.getDurability() == ItemDurability.BROKEN)
			cost = (int) (item.getCost() * .5);
		else if (item.getDurability() == ItemDurability.DAMAGED)
			cost = (int) (item.getCost() * .2);
		
		if (cost > stateInfo.getClientProfile().getGold()) {
			stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopNotEnoughGoldText(), 
					Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
		} else {
			stateInfo.getClientProfile().setGold(stateInfo.getClientProfile().getGold() - cost);
			item.setDurability(ItemDurability.PERFECT);
			goldAmountText.setText(stateInfo.getClientProfile().getGold() + "");
			
			stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopItemRepairedText(), 
					Trigger.TRIGGER_LIST_NONE, shopMessage.getPortrait(stateInfo)));
		}
	}

	@Override
	public void menuClosed() {

	}
}
