package tactical.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import mb.tcp.network.Server;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.network.message.ClientIDMessage;
import tactical.network.message.ClientRegistrationMessage;
import tactical.network.message.PlayerListMessage;

public class TCPServer extends Server
{
	// TODO Base this on names or something
	private int id = 0;
	private Integer waitCount = 0;
	private Hashtable<Integer, ClientInfo> clientInfoById;

	public TCPServer() {
		super(5000);
		this.clientInfoById = new Hashtable<Integer, ClientInfo>();
	}

	@Override
	public void clientClosed(int clientNumber) {

	}

	@Override
	public void serverStarted() {
		System.out.println("STARTED");

	}

	@Override
	public void serverClosing() {
		

	}

	@Override
	public boolean messageRecieved(int clientNumber, Object message)
	{
		Message mess = (Message) message;
		switch (mess.getMessageType())
		{
			case PUBLIC_SPEECH:
				this.tellEveryone(message);
				break;
			/*
			 * When a wait message is received, the server will wait until all of the clients
			 * have issued a WAIT message and then the server will issue a CONTINUE message
			 */
			case WAIT:
				synchronized (waitCount)
				{
					waitCount++;
					if (waitCount == this.clientOutputStreams.size())
					{
						waitCount = 0;
						tellEveryone(new Message(MessageType.CONTINUE));
					}
				}
				break;
			case CLIENT_REGISTRATION:
				ClientRegistrationMessage crm = (ClientRegistrationMessage) mess;
				synchronized (clientInfoById)
				{
					clientInfoById.put(crm.getClientId(), new ClientInfo(crm.getName(), crm.getClientId()));
				}
				tellEveryone(new PlayerListMessage(getConnectedClientInfo()));
				break;
			case CLIENT_BROADCAST_HERO:
			case OVERLAND_MOVE_MESSAGE:
				tellEveryoneElse(message, clientNumber);
				break;
			default:
				this.tellEveryone(message);
				break;
		}

		return true;
	}

	@Override
	public void clientJoined(int clientNumber, ObjectOutputStream writer)
			throws IOException {
		int newId;
		synchronized (clientInfoById)
		{
			newId = id++;
		}
		this.tellSomeone(new ClientIDMessage(newId), writer);
		System.out.println("JOINED");
	}

	public ArrayList<ClientInfo> getConnectedClientInfo()
	{
		return new ArrayList<ClientInfo>(clientInfoById.values());
	}
}
