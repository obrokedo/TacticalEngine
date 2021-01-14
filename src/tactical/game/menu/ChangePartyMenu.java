package tactical.game.menu;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.menu.Menu.MenuUpdate;

public class ChangePartyMenu extends QuadMenu {

	public ChangePartyMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_CHANGE_PARTY, stateInfo);
				
		icons = new Image[8];

		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(25, 0);
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(26, 0);
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 0);
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(6, 0);
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(25, 1);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(26, 1);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 1);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(6, 1);
		enabled = new boolean[] {true, true, true, true};
		text = new String[] {"Join", "Rest", "Inspect", "Talk"};
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getTextboxWidth() {
		switch (selected) {
			case RIGHT:
				return 70;
		}
	
		return super.getTextboxWidth();
	}
}
