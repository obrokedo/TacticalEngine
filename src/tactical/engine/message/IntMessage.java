package tactical.engine.message;

/**
 * A reusable message that can be used to issue a message with a single integer as a value
 *
 * @author Broked
 *
 */
public class IntMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private int value;

	public IntMessage(MessageType type, int value)
	{
		super(type);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
