package tactical.game.menu.town;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.IntMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.menu.QuadMenu;

public class TownItemOptionMenu extends QuadMenu {
	public TownItemOptionMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_ITEM_OPTIONS, stateInfo);

		icons = new Image[8];

		for (int i = 0; i < icons.length; i++)
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(i % 4 + 7, i / 4);
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Use", "Give", "Equip", "Drop"};
	}

	@Override
	public void initialize() {
		selected = Direction.UP;
	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	protected MenuUpdate onConfirm() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		stateInfo.sendMessage(new IntMessage(MessageType.SHOW_TOWN_SELECT_ITEM, getSelectedInt()));
		return MenuUpdate.MENU_ACTION_LONG;
	}
}
