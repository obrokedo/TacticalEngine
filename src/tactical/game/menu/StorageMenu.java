package tactical.game.menu;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.menu.Menu.MenuUpdate;

public class StorageMenu extends QuadMenu {

	public StorageMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_STORAGE, stateInfo);
		
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

	@Override
	protected MenuUpdate onConfirm() {
		
		return null;
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
}
