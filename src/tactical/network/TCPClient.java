package tactical.network;

import mb.tcp.network.Client;

public class TCPClient extends Client 
{

	public TCPClient() {
		super("127.0.0.1", 5000);
	}
	
	public TCPClient(String ip) {
		super(ip, 5000);
	}

	@Override
	public void clientOpening() {
	
	}

	@Override
	public void clientClosing() {
		
	}
}
