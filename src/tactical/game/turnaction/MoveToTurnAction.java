package tactical.game.turnaction;


public class MoveToTurnAction extends TurnAction
{
	private static final long serialVersionUID = 1L;
	
	public int locX;
	public int locY;
	
	public MoveToTurnAction(int locX, int locY) {
		super(TurnAction.ACTION_MOVE_TO);
		this.locX = locX;
		this.locY = locY;
	}
}
