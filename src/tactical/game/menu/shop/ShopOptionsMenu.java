package tactical.game.menu.shop;

import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.menu.QuadMenu;
import tactical.game.trigger.Trigger;

public class ShopOptionsMenu extends QuadMenu
{
	private ShopMessage shopMessage;
	protected MenuConfiguration menuConfig;

	public ShopOptionsMenu(ShopMessage shopMessage, StateInfo stateInfo) {
		super(PanelType.PANEL_SHOP_OPTIONS, shopMessage.getPortrait(stateInfo), true, stateInfo);
		this.menuConfig = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration();
		this.shopMessage = shopMessage;
		
		icons = new Image[8];

		for (int i = 0; i < icons.length; i++)
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(i % 4 + 11, i / 4);
		enabled = new boolean[4];
		for (int i = 0; i < enabled.length; i++)
			enabled[i] = true;
		text = new String[] {"Buy", "Deals", "Repair", "Sell"};
	}

	@Override
	public void initialize() {

	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopMenuClosedText(), Trigger.TRIGGER_NONE, portrait));
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	protected MenuUpdate onConfirm() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		switch (selected)
		{
			case UP:
				shopMessage.setMenuTypeShopBuy();
				stateInfo.sendMessage(shopMessage);
				break;
			case LEFT:
				if (stateInfo.getClientProgress().getDealItems().size() > 0) {
					shopMessage.setMenuTypeShopDeals();
					stateInfo.sendMessage(shopMessage);
				} else {
					stateInfo.sendMessage(new SpeechMessage(menuConfig.getShopNoDealsText(), Trigger.TRIGGER_NONE, portrait));
				}
				break;
			case RIGHT:
				shopMessage.setMenuTypeShopRepair();
				stateInfo.sendMessage(shopMessage);
				break;
			case DOWN:
				shopMessage.setMenuTypeShopSell();
				stateInfo.sendMessage(shopMessage);
				break;
		}
		return MenuUpdate.MENU_ACTION_LONG;
	}
	
	

	@Override
	public MenuUpdate update(int delta) {
		// TODO Auto-generated method stub
		return super.update(delta);
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
}
