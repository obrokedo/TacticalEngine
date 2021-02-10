package tactical.game.menu.advisor;

import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.Portrait;
import tactical.game.menu.QuadMenu;
import tactical.game.menu.SpeechMenu;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class StorageMenu extends QuadMenu implements MenuListener {

	public StorageMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_STORAGE, stateInfo);		
		
		this.portrait = Portrait.getPortrait(-1, -1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getAdvisorPortraitAnimFile(), stateInfo);
		
		icons = new Image[8];

		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(23, 0);
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 0);
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(5, 0);
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(24, 0);
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(23, 1);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 1);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(5, 1);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(24, 1);
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Deposit", "Item", "Evaluate", "Withdraw"};
	}

	@Override
	public void initialize() {
		
		
	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}
	
	public boolean showDepositMenu() {
		stateInfo.sendMessage(MessageType.SHOW_DEPOSIT_MENU);
		return true;
	}
	
	public boolean showChooseItemMenu() {
		stateInfo.addMenu(new ChooseItemMenu(stateInfo, this));
		return true;
	}
	
	public boolean showWithdrawMenu() {
		stateInfo.sendMessage(MessageType.SHOW_WITHDRAW_MENU);
		return true;
	}

	@Override
	protected MenuUpdate onConfirm() {
		switch (selected) {
			case UP:
				stateInfo.addMenu(new SpeechMenu(menuConfig.getStorageDepositText(), portrait, stateInfo, this::showDepositMenu));				
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case LEFT:
				stateInfo.sendMessage(MessageType.SHOW_TOWN_ITEM_OPTION_MENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));		
				break;		
			case RIGHT:
				stateInfo.addMenu(new SpeechMenu(menuConfig.getStorageEvaluateText(), 
						portrait, stateInfo, this::showChooseItemMenu));		
				break;
			case DOWN:
				if (stateInfo.getClientProgress().getStoredItems().size() > 0)
					showWithdrawMenu();
				else
					stateInfo.addMenu(new SpeechMenu(menuConfig.getStorageWithdrawNoItemsText(), -1, this.portrait, stateInfo));
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
		}
		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	protected int getTextboxWidth() {
		switch (selected) {
			case UP:
				return 63;
			case DOWN:
			case RIGHT:
				return 72;
		}
	
		return super.getTextboxWidth();
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		if (value != null) {
			Object[] obs = (Object[]) value;
			Item i = ((CombatSprite) obs[0]).getItem((int) obs[1]);
			String desc = i.getDescription();
			if (StringUtils.isEmpty(desc))
				desc = "Only slightly more damaging then a stern glare.<hardstop>";
			stateInfo.addMenu(new SpeechMenu(desc, -1, this.portrait, stateInfo));
		}		
	}

	@Override
	public void menuClosed() {
		// TODO Auto-generated method stub
		
	}
}
