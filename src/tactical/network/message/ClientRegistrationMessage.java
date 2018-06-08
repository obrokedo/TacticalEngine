package tactical.network.message;

import tactical.engine.message.Message;
import tactical.engine.message.MessageType;

public class ClientRegistrationMessage extends Message {
	private static final long serialVersionUID = 1L;

	private String name;
	private int clientId;

	public ClientRegistrationMessage(String name,
			int clientId) {
		super(MessageType.CLIENT_REGISTRATION);
		this.name = name;
		this.clientId = clientId;
	}

	public String getName() {
		return name;
	}

	public int getClientId() {
		return clientId;
	}
}
