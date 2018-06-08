package tactical.engine.message;

import java.util.ArrayList;

import tactical.game.turnaction.TurnAction;

public class TurnActionsMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private boolean forHero = false;
	private ArrayList<TurnAction> turnActions;

	public TurnActionsMessage(boolean forHero,
			ArrayList<TurnAction> turnActions) {
		super(MessageType.TURN_ACTIONS);
		this.forHero = forHero;
		this.turnActions = turnActions;
	}

	public boolean isForHero() {
		return forHero;
	}

	public ArrayList<TurnAction> getTurnActions() {
		return turnActions;
	}
}
