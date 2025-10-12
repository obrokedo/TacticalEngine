package tactical.engine.message;

public class StringMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private String string;

	public StringMessage(MessageType messageType, String string) {
		super(messageType);
		this.string = string;
	}
	
	public StringMessage(MessageType messageType, String string, boolean immediate) {
		super(messageType, immediate, false);
		this.string = string;
	}

	public String getString() {
		return string;
	}
}
