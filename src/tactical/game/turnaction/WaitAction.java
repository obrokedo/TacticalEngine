package tactical.game.turnaction;

import java.util.ArrayList;

import tactical.engine.state.StateInfo;
import tactical.game.manager.TurnManager;

public class WaitAction extends TurnAction
{
	private static final long serialVersionUID = 1L;
	
	public int waitAmt = 20;
	public WaitAction() {
		super(TurnAction.ACTION_WAIT);			
	}		
	
	public WaitAction(int amt) {
		super(TurnAction.ACTION_WAIT);
		waitAmt = amt;
		
		
	}

	@Override
	public boolean perform(int delta, TurnManager turnManager, StateInfo stateInfo, ArrayList<TurnAction> turnActions) {
		if (waitAmt > 0)
		{
			waitAmt -= delta;
		}
		else
		{
			turnManager.setDisplayCursor(false);
			return true;
		}
		return false;
	}		
}