package tactical.network.message;

import java.util.ArrayList;

import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.network.ClientInfo;

public class PlayerListMessage extends Message{
	private static final long serialVersionUID = 1L;

	private ArrayList<ClientInfo> clientInfos;

	public PlayerListMessage(ArrayList<ClientInfo> clientInfos)
	{
		super(MessageType.PLAYER_LIST);
		this.clientInfos = clientInfos;
	}

	public ArrayList<ClientInfo> getClientInfos() {
		return clientInfos;
	}
}
