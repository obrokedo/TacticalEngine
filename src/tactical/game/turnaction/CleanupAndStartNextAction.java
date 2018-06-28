package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.manager.TurnManager;

public class CleanupAndStartNextAction extends TurnAction {

	public CleanupAndStartNextAction() {
		super(TurnAction.ACTION_CHECK_SPEECH_END_TURN);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		/*
		 * This path is ALWAYS taken at the end of the of a turn
		 * so although it seems to be strange that we remove panels here
		 * it makes sense as long as we want them on the screen while
		 * text is displayed
		 */
		stateInfo.removePanel(turnManager.getLandEffectPanel());
		stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
		stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
		stateInfo.removeKeyboardListeners();
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, 
				TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration().getMenuRemovedSoundEffect(), 1f, false));
		turnManager.setDisplayMoveable(false);
		turnManager.setDisplayAttackable(false);
		
		if (!turnManager.isCinWasDisplayed())
			turnManager.getCursor().setLocation(turnManager.getCurrentSprite().getLocX(), turnManager.getCurrentSprite().getLocY());
					
		stateInfo.sendMessage(MessageType.NEXT_TURN, true);
		return true;
	}
}
