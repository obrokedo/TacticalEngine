package tactical.game.menu;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.StringMessage;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.menu.Menu.MenuUpdate;

public class AdvisorMenu extends QuadMenu {

	public AdvisorMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_ADVISOR, stateInfo);

		icons = new Image[8];

		for (int i = 0; i < icons.length; i++)
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(i % 4 + 19, i / 4);
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Heroes", "Storage", "Advice", "Change Party"};
	}

	@Override
	public void initialize() {
		this.selected = Direction.UP;
	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	protected MenuUpdate onConfirm() {
		switch (selected) {
			case LEFT:
				stateInfo.sendMessage(MessageType.SHOW_STORAGE_MENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case UP:
				stateInfo.sendMessage(MessageType.SHOW_HEROES);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case DOWN:
				stateInfo.sendMessage(MessageType.SHOW_CHANGE_PARTY_MENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case RIGHT:
				stateInfo.sendMessage(new StringMessage(MessageType.SPEECH, "Win some battles!<hardstop>"));
				break;				
		}
		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	protected int getTextboxWidth() {
		switch (selected) {
			case DOWN:
				return 100;
			case LEFT:
				return 63;
		}
		
		return 60;
		
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
	
	
}
