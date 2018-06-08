package tactical.engine.message;

/**
 * A reusable message that indicates that a battle menu action was selected
 *
 * @author Broked
 *
 */
public class BattleSelectionMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private int selectionIndex, level;

	public BattleSelectionMessage(MessageType messageType, int selectionIndex)
	{
		super(messageType);
		this.selectionIndex = selectionIndex;
		this.level = 0;
	}

	public BattleSelectionMessage(MessageType messageType, int selectionIndex, int level)
	{
		super(messageType);
		this.selectionIndex = selectionIndex;
		this.level = level;
	}

	public int getSelectionIndex() {
		return selectionIndex;
	}

	public int getLevel() {
		return level;
	}
}
