package tactical.game.menu.battle;

import org.newdawn.slick.Image;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpeechMessage;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.menu.QuadMenu;
import tactical.game.menu.SpeechMenu;
import tactical.game.trigger.Trigger;
import tactical.game.trigger.Trigger.TriggerSave;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingScreenRenderer;
import tactical.loading.LoadingState;

public class BattleOptionMenu extends QuadMenu {
	public BattleOptionMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_BATTLE_OPTIONS, stateInfo);

		icons = new Image[8];

		for (int i = 0; i < icons.length; i++) {
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("battleoptions").getSprite(i % 4, i / 4);
		}

		text = new String[] {"Heroes", "Map", "TxtSpd", "Quit"};
		enabled = new boolean[] {true, true, true, true};
	}

	@Override
	public void initialize() {
		this.selected = Direction.UP;
	}

	@Override
	public MenuUpdate onBack() {
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public MenuUpdate onConfirm() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		switch (selected)
		{
			case UP:
				stateInfo.sendMessage(MessageType.SHOW_HEROES);
				break;
			case LEFT:
				stateInfo.sendMessage(MessageType.SHOW_MINI_MAP);
				break;
			case RIGHT:
				SpeechMenu.SPEECH_SPEED = (SpeechMenu.SPEECH_SPEED % 3) + 1;
				stateInfo.sendMessage(new SpeechMessage("Text speed has been set to " + SpeechMenu.SPEECH_SPEED + "x.<hardstop>"));
				break;
			case DOWN:
				StateBasedGame game = stateInfo.getPersistentStateInfo().getGame();
				((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo(null, false, false,
						stateInfo.getResourceManager(),
							(LoadableGameState) game.getState(TacticalGame.STATE_GAME_MENU),
								new LoadingScreenRenderer(stateInfo.getPaddedGameContainer()));
				TriggerSave triggerSave = new Trigger().new TriggerSave();
				triggerSave.perform(stateInfo);
				
				game.enterState(TacticalGame.STATE_GAME_LOADING);
				// stateInfo.sendMessage(new SpeechMessage("Are you sure you want to exit?", 
						// new int[] {Trigger.TRIGGER_ID_SAVE_AND_EXIT}, new int[] {Trigger.TRIGGER_NONE}, null));
		}

		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
}
