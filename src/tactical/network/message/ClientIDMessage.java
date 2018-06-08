package tactical.network.message;

import tactical.engine.message.Message;
import tactical.engine.message.MessageType;

public class ClientIDMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private int clientId;

	public ClientIDMessage(int clientId) {
		super(MessageType.CLIENT_ID);
		this.clientId = clientId;
	}

	public int getClientId() {
		return clientId;
	}
}
