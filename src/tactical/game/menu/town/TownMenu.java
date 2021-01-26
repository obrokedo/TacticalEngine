package tactical.game.menu.town;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.menu.QuadMenu;

public class TownMenu extends QuadMenu {
	public TownMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_TOWN, stateInfo);
		
		icons = new Image[8];

		// Search
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 0);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 1);
		
		// Item
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 0);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(2, 1);
		
		// Heroes
		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 0);
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 1);
		
		// Magic
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(1, 0);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(1, 1);
		
		
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Heroes", "Magic", "Item", "Search"};
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
		switch (selected) {
			case DOWN:
				stateInfo.sendMessage(MessageType.INVESTIGATE);
				stateInfo.setInputDelay(System.currentTimeMillis() + 200);
				stateInfo.checkSearchLocation();
				break;
			case UP:
				stateInfo.sendMessage(MessageType.SHOW_HEROES);
				break;
			case RIGHT:
				stateInfo.sendMessage(MessageType.SHOW_TOWN_ITEM_OPTION_MENU);
				break;
		}
		return MenuUpdate.MENU_ACTION_LONG;
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
