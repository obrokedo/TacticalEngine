package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.manager.TurnManager;
import tactical.game.menu.SpeechMenu;

public class CurrentSpriteDeathAction extends TurnAction {

	public CurrentSpriteDeathAction() {
		super(TurnAction.ACTION_CURRENT_SPRITE_DEATH);
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		stateInfo.addMenu(new SpeechMenu(TacticalGame.ENGINE_CONFIGURATIOR.getBattleFunctionConfiguration().getCombatantDeathText(null, turnManager.getCurrentSprite()), stateInfo));
		return true;
	}
}
