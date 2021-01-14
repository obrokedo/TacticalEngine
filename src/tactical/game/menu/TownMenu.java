package tactical.game.menu;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;

public class TownMenu extends QuadMenu {
	public TownMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_TOWN, stateInfo);
		
		icons = new Image[8];

		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 0);
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 0);
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 0);
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(1, 0);
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 1);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 1);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 1);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(1, 1);
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Search", "Item", "Heroes", "Magic"};
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	protected MenuUpdate onConfirm() {
		switch (selected) {
			case UP:
				stateInfo.sendMessage(MessageType.INVESTIGATE);
				stateInfo.setInputDelay(System.currentTimeMillis() + 200);
				stateInfo.checkSearchLocation();
				break;
			case RIGHT:
				stateInfo.sendMessage(new Message(MessageType.SHOW_HEROES));
				break;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	protected int getTextboxWidth() {
		/*
		switch (selected) {
			case UP:
				return 63;
			case DOWN:
			case RIGHT:
				return 72;
		}
		*/
	
		return super.getTextboxWidth();
	}
}
