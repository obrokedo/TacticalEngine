package tactical.engine.message;

/**
 * A message that indicates that a piece of information should be emitted by the engine
 *
 * @author Broked
 *
 */
public class InfoMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private String sender;
	private String text;

	public InfoMessage(String sender, String text) {
		super(MessageType.SEND_INTERNAL_MESSAGE);
		this.sender = sender;
		this.text = text;
	}

	public String getSender() {
		return sender;
	}

	public String getText() {
		return text;
	}
}
