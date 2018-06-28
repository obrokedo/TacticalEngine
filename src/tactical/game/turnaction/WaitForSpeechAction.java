package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.manager.TurnManager;

public class WaitForSpeechAction extends TurnAction {

	public WaitForSpeechAction() {
		super(TurnAction.ACTION_WAIT_FOR_SPEECH);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		return !stateInfo.isMenuDisplayed(PanelType.PANEL_SPEECH);
	}
}
