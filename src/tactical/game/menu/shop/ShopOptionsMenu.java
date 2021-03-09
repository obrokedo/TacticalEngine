package tactical.game.menu.shop;

import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
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
		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(11, 0);
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(12, 0);
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(13, 0);
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(14, 0);
		
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(11, 1);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(12, 1);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(13, 1);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(14, 1);
		enabled = new boolean[4];
		for (int i = 0; i < enabled.length; i++)
			enabled[i] = true;
		text = new String[] {"Buy", "Sell", "Recharge","Deals"};
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
			case DOWN:
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
			case LEFT:
				shopMessage.setMenuTypeShopSell();
				stateInfo.sendMessage(shopMessage);
				break;
		}
		return MenuUpdate.MENU_ACTION_LONG;
	}
	
	

	@Override
	protected int getTextboxWidth() {
		if ((selected == Direction.RIGHT))
			return 74;
		return super.getTextboxWidth();
	}

	@Override
	public MenuUpdate update(int delta) {
		
		return super.update(delta);
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
}
