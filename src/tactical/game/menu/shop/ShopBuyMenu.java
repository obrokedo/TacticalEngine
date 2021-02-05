package tactical.game.menu.shop;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Polygon;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.Menu;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.YesNoMenu;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.game.ui.RectUI;
import tactical.game.ui.TextUI;
import tactical.utils.StringUtils;

public class ShopBuyMenu extends Menu implements MenuListener
{
	protected enum ShopStepEnum
	{
		SELECT_ITEM,
		IS_COST_OK,
		WHO_WILL_USE,
		SELECT_HERO,
		SALE_COMPLETED,
		SELL_OLD_WEAPON,
		EQUIP_NOW
	}

	protected double sellPercent;
	protected double buyPercent;
	protected Item[] items;
	protected int selectedItemIndex = 0;
	protected Item selectedItem;
	protected String[] itemName;
	protected Font smallFont;
	protected int gold;
	protected SpeechMenu speechMenu;
	protected boolean hasFocus = true;
	protected ShopStepEnum currentStep;
	protected CombatSprite selectedHero;
	protected ShopMessage shopMessage;
	protected StateInfo stateInfo;
	protected MenuConfiguration menuConfig;

	// Base UI Shapes
	protected Polygon leftArrow, rightArrow;
	protected RectUI itemPanel, itemNamePanel, goldPanel, selectedItemRect;
	protected TextUI itemNameText1, itemNameText2, itemCostText, goldTitleText, goldAmountText;

	public ShopBuyMenu(StateInfo stateInfo, ShopMessage shopMessage) {
		super(PanelType.PANEL_SHOP);

		this.menuConfig = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration();
		this.sellPercent = shopMessage.getSellPercent();
		this.buyPercent = shopMessage.getBuyPercent();
		if (shopMessage.getMessageType() == MessageType.SHOW_SHOP_DEALS) {
			this.items = new Item[stateInfo.getClientProgress().getDealItems().size()];
			for (int i = 0; i < stateInfo.getClientProgress().getDealItems().size(); i++)
				this.items[i] = ItemResource.getItem(stateInfo.getClientProgress().getDealItems().get(i), stateInfo.getResourceManager());
		}
		else {
			this.items = new Item[shopMessage.getItemIds().length];
			for (int i = 0; i < items.length; i++)
				items[i] = ItemResource.getItem(shopMessage.getItemIds()[i], stateInfo.getResourceManager());
		}
		this.smallFont = stateInfo.getResourceManager().getFontByName("smallmenufont");
		this.gold = stateInfo.getClientProfile().getGold();

		selectedItem = items[0];		
		itemName = StringUtils.splitItemString(selectedItem.getName());

		rightArrow = new Polygon(new float[] {
				285, 13,
				289, 17,
				285, 21});


		leftArrow = new Polygon(new float[] {
				35, 13,
				31, 17,
				35, 21});

		itemPanel = new RectUI(25, 2, 270, 32);
		itemNamePanel = new RectUI(27, 34, 90, 37);
		selectedItemRect = new RectUI(36,  6,  18,  23);

		// Setup gold
		goldPanel = new RectUI(241, 148, 62, 32);
		goldTitleText = new TextUI("Gold", 248, 144);
		goldAmountText = new TextUI(gold + "", 248, 156);
		this.stateInfo = stateInfo;
		this.shopMessage = shopMessage;
		updateSelectedItem();

		showBuyPanel(stateInfo);
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		if (speechMenu != null)
			speechMenu.update(delta, stateInfo);
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		if (!hasFocus)
		{
			MenuUpdate mu = speechMenu.handleUserInput(input, stateInfo);
			if (input.isKeyDown(KeyMapping.BUTTON_3) || input.isKeyDown(KeyMapping.BUTTON_1))
				return MenuUpdate.MENU_ACTION_LONG;
			else
				return MenuUpdate.MENU_NO_ACTION;
		}

		if (input.isKeyDown(Input.KEY_RIGHT))
		{
			selectedItemIndex = Math.min(selectedItemIndex + 1, items.length - 1);
			selectedItem = items[selectedItemIndex];
			itemName = StringUtils.splitItemString(selectedItem.getName());
			updateSelectedItem();
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(Input.KEY_LEFT))
		{
			selectedItemIndex = Math.max(0, selectedItemIndex - 1);
			selectedItem = items[selectedItemIndex];
			itemName = StringUtils.splitItemString(selectedItem.getName());
			updateSelectedItem();
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3))
		{
			showCostPanel(stateInfo);
			// stateInfo.sendMessage(new IntMessage(MessageType.SHOW_SHOP_HERO_SELECT, selectedItem.getItemId()));
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			stateInfo.getClientProfile().setGold(gold);
			return MenuUpdate.MENU_CLOSE;
		}

		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		if (currentStep != ShopStepEnum.IS_COST_OK && currentStep != ShopStepEnum.SELECT_ITEM)
			return;

		// Draw items box
		itemPanel.drawPanel(graphics);

		graphics.setColor(Color.white);
		graphics.setFont(smallFont);
		// Draw items and price
		for (int i = (selectedItemIndex < 9 ? 0 : selectedItemIndex - 8); i < Math.min(items.length,  (selectedItemIndex < 9 ? 9 : selectedItemIndex + 1)); i++)
		{
			items[i].getImage().draw(36 +
				(28 * (i - (selectedItemIndex < 9 ? 0 : selectedItemIndex - 8))),
					6);

			graphics.rotate(gc.getPaddedWidth() / 2, gc.getHeight() / 2, 90);
			graphics.drawString((int) (items[i].getCost() * buyPercent) + "", 49, 216 -
					(28 * (i - (selectedItemIndex < 9 ? 0 : selectedItemIndex - 8))));
			graphics.rotate(gc.getPaddedWidth() / 2, gc.getHeight() / 2, -90);
		}

		// Draw item name

		itemNamePanel.drawPanel(graphics);
		graphics.setColor(Color.white);
		itemNameText1.drawText(graphics);
		if (itemName.length > 1)
			itemNameText2.drawText(graphics);
		itemCostText.drawText(graphics);

		// Draw gold box
		goldPanel.drawPanel(graphics);
		graphics.setColor(Color.white);
		goldTitleText.drawText(graphics);
		goldAmountText.drawText(graphics);

		// Draw selection box
		graphics.setLineWidth(2);
		selectedItemRect.drawRoundRect(graphics, Color.white, 3);
		// selectedItemRect.drawRect(graphics, Color.white);

		if (speechMenu != null)
			speechMenu.render(gc, graphics);

		graphics.setColor(Color.white);
		if (items.length > 9 && selectedItemIndex != items.length - 1)
			graphics.fill(rightArrow);

		if (selectedItemIndex > 8)
			graphics.fill(leftArrow);
	}

	private void updateSelectedItem()
	{	
		if (selectedItemIndex < 7) {
			itemNamePanel.setX(27 + 28 * selectedItemIndex);
			itemNameText1 = new TextUI(itemName[0], 33 + 28  *selectedItemIndex, 29);
			if (itemName.length > 1)
				itemNameText2 = new TextUI(itemName[1], 33 + 28 * selectedItemIndex, 39);
			String itemCost = "" + ((int) (selectedItem.getCost() * shopMessage.getBuyPercent()));
			itemCostText = new TextUI(itemCost+ "", 35 + 28 * selectedItemIndex, 49);
		}
		else {
			itemNamePanel.setX(205);
			itemNameText1 = new TextUI(itemName[0], 213, 29);
			if (itemName.length > 1)
				itemNameText2 = new TextUI(itemName[1], 213, 39);
			String itemCost = "" + ((int) (selectedItem.getCost() * shopMessage.getBuyPercent()));
			itemCostText = new TextUI(itemCost+ "", 213, 49);
		}
		selectedItemRect.setX(36 + 28 * Math.min(8, selectedItemIndex));
	}

	private void showBuyPanel(StateInfo stateInfo)
	{
		if (shopMessage.getMessageType() == MessageType.SHOW_SHOP_BUY)
			speechMenu = new SpeechMenu(menuConfig.getShopLookAtNormalText(), stateInfo.getPaddedGameContainer(),
				Trigger.TRIGGER_NONE, null, null);
		else
			speechMenu = new SpeechMenu(menuConfig.getShopLookAtDealsText(), stateInfo.getPaddedGameContainer(),
					Trigger.TRIGGER_NONE, null, null);
		currentStep = ShopStepEnum.SELECT_ITEM;
	}

	private void showCostPanel(StateInfo stateInfo)
	{
		hasFocus = false;
		speechMenu = null;
		stateInfo.addMenu(new YesNoMenu(menuConfig.getShopPromptPurchaseCostText(selectedItem.getName(), 
				(int) ( selectedItem.getCost() * shopMessage.getBuyPercent()) + ""), 
				stateInfo, this));
		currentStep = ShopStepEnum.IS_COST_OK;
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		hasFocus = true;
		EquippableItem equipped;
		switch (currentStep)
		{
			case IS_COST_OK:
				if (!(boolean) value)
					showBuyPanel(stateInfo);
				else
				{
					if (gold >= (int) (selectedItem.getCost() * buyPercent)) {
						currentStep = ShopStepEnum.WHO_WILL_USE;
						speechMenu = null;
						stateInfo.addMenu(new SpeechMenu(menuConfig.getShopPromptWhoGetsItemText(), stateInfo.getPaddedGameContainer(),
								Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
					} else {
						stateInfo.addMenu(new SpeechMenu(menuConfig.getShopNotEnoughGoldText(), stateInfo.getPaddedGameContainer(),
								Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
						currentStep = ShopStepEnum.SALE_COMPLETED;
					}
					//
				}
				break;
			case WHO_WILL_USE:
				currentStep = ShopStepEnum.SELECT_HERO;
				stateInfo.addMenu(new HeroesBuyMenu(stateInfo, this, selectedItem));
				break;
			case SELECT_HERO:
				if (value == null)
				{
					currentStep = ShopStepEnum.SALE_COMPLETED;
					stateInfo.addMenu(new SpeechMenu(menuConfig.getShopTransactionCancelledText(), stateInfo.getPaddedGameContainer(),
							Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
				}
				else
				{
					selectedHero = (CombatSprite) value;

					if (selectedHero.getItemsSize() < CombatSprite.MAXIMUM_ITEM_AMOUNT)
					{
						// If this is an equippable item that the hero can use
						if (selectedItem.isEquippable() && selectedHero.isEquippable((EquippableItem) selectedItem))
						{
							currentStep = ShopStepEnum.EQUIP_NOW;
							stateInfo.addMenu(new YesNoMenu(menuConfig.getShopPromptEquipItNowText(),
									Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), stateInfo, this));
						}
						// Otherwise it's not equippable or this hero can't equip it so just
						// put it in their inventor
						else
						{
							selectedHero.addItem(selectedItem);
							gold -= (int) (selectedItem.getCost() * buyPercent);
							goldAmountText.setText(gold + "");
							
							handleDealPurchase(stateInfo);
							
							currentStep = ShopStepEnum.SALE_COMPLETED;
							stateInfo.addMenu(new SpeechMenu(menuConfig.getShopTransactionSuccessfulText(), stateInfo.getPaddedGameContainer(),
									Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
						}
					}
					// No room for items
					else
					{
						currentStep = ShopStepEnum.WHO_WILL_USE;
						stateInfo.addMenu(new SpeechMenu(menuConfig.getShopCantCarryMoreText(selectedHero.getName()), stateInfo.getPaddedGameContainer(),
								Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
					}
				}
				break;

			case SALE_COMPLETED:
				if (shopMessage.getMessageType() == MessageType.SHOW_SHOP_DEALS) {
					if (stateInfo.getClientProgress().getDealItems().size() == 0) {
						stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopNoMoreDealsText(), Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo)));
						stateInfo.removeMenu(this);
					}
				}
				showBuyPanel(stateInfo);
				break;
			case SELL_OLD_WEAPON:
				selectedHero.addItem(selectedItem);
				equipped = selectedHero.equipItem((EquippableItem) selectedItem);
				if ((boolean) value) {
					selectedHero.removeItem(equipped);
					gold += (int) (equipped.getCost() * sellPercent);
					goldAmountText.setText(gold + "");
					// If we just sold a deal item then add it to the deals menu
					if (equipped.isDeal()) {
						stateInfo.getClientProgress().getDealItems().add(equipped.getItemId());
					}
				}
				currentStep = ShopStepEnum.SALE_COMPLETED;
				stateInfo.addMenu(new SpeechMenu(menuConfig.getShopTransactionSuccessfulText(), stateInfo.getPaddedGameContainer(),
						Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
				break;
			case EQUIP_NOW:
				// Equip the item now
				if ((boolean) value)
				{
					equipped = selectedHero.getEquippedWeapon();
					gold -= (int) (selectedItem.getCost() * buyPercent);
					goldAmountText.setText(gold + "");
					
					// Check if we purchased a "deal" if so remove it from the deals list
					handleDealPurchase(stateInfo);
					
					// If they aren't equipped with anything then just equip this item
					// and be done
					if (equipped == null)
					{
						selectedHero.addItem(selectedItem);
						selectedHero.equipItem((EquippableItem) selectedItem);
						currentStep = ShopStepEnum.SALE_COMPLETED;
						stateInfo.addMenu(new SpeechMenu(menuConfig.getShopTransactionSuccessfulText(), stateInfo.getPaddedGameContainer(),
								Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
					}
					else
					{
						currentStep = ShopStepEnum.SELL_OLD_WEAPON;
						stateInfo.addMenu(new YesNoMenu(menuConfig.getShopPromptSellOldText(equipped.getName(), (int) (equipped.getCost() * sellPercent) + ""), 
								Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), stateInfo, this));
					}
				}
				// Don't equip the item, just put it in the inventory and complete
				else
				{
					selectedHero.addItem(selectedItem);
					gold -= (int) (selectedItem.getCost() * buyPercent);
					goldAmountText.setText(gold + "");
					currentStep = ShopStepEnum.SALE_COMPLETED;
					stateInfo.addMenu(new SpeechMenu(menuConfig.getShopTransactionSuccessfulText(), stateInfo.getPaddedGameContainer(),
							Trigger.TRIGGER_NONE, shopMessage.getPortrait(stateInfo), this));
				}
				break;
		}
	}

	private void handleDealPurchase(StateInfo stateInfo) {
		// Check if we purchased a "deal" if so remove it from the deals list
		if (shopMessage.getMessageType() == MessageType.SHOW_SHOP_DEALS && selectedItem.isDeal()) {
			stateInfo.getClientProgress().getDealItems().remove(stateInfo.getClientProgress().getDealItems().indexOf(selectedItem.getItemId()));
			
			this.items = new Item[stateInfo.getClientProgress().getDealItems().size()];
			for (int i = 0; i < stateInfo.getClientProgress().getDealItems().size(); i++)
				this.items[i] = ItemResource.getItem(stateInfo.getClientProgress().getDealItems().get(i), stateInfo.getResourceManager());
			if (selectedItemIndex > 0)
				selectedItemIndex--;
			updateSelectedItem();
		}
	}

	@Override
	public void menuClosed() {

	}
}
