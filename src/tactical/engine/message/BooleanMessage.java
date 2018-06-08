package tactical.engine.message;

public class BooleanMessage extends Message 
{
	private static final long serialVersionUID = 1L;
	
	private boolean bool;

	public BooleanMessage(MessageType messageType, boolean bool) {
		super(messageType);
		this.bool = bool;
	}

	public boolean isBool() {
		return bool;
	}
}
