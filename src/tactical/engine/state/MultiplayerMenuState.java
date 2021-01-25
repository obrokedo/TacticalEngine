package tactical.engine.state;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.StateBasedGame;

import mb.tcp.network.Client;
import mb.tcp.network.PacketHandler;
import tactical.engine.TacticalGame;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.game.listener.StringListener;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.network.ClientInfo;
import tactical.network.TCPClient;
import tactical.network.TCPServer;
import tactical.network.message.BackedSpriteMessage;
import tactical.network.message.ClientIDMessage;
import tactical.network.message.ClientRegistrationMessage;
import tactical.network.message.PlayerListMessage;

public class MultiplayerMenuState extends MenuState implements PacketHandler, StringListener {

	private int init = -1;
	private TCPClient client;
	private TCPServer server = null;
	private boolean ready = false;
	private TextField clientNameField;
	private TextField ipField;
	private String[] menuItems = new String[] {"Name", "IP", "Start Server", "Connect", "Prepare Server", "Start Game"};
	private int selectedIndex = 0;
	private int inputDelay;
	private ArrayList<Integer> availableMenuItems;
	private ArrayList<String> players;
	private int yStart = 100;

	public MultiplayerMenuState(PersistentStateInfo psi)
	{
		super(psi);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.game = game;
		this.gc = container;
		this.players = new ArrayList<>();
		availableMenuItems = new ArrayList<>();
		availableMenuItems.add(0);
		availableMenuItems.add(1);
		availableMenuItems.add(2);
		availableMenuItems.add(3);
		clientNameField = new TextField(container, container.getDefaultFont(), 100, yStart + 50, 200, 30);
		clientNameField.setBackgroundColor(Color.blue);
		clientNameField.setFocus(true);
		ipField = new TextField(container, container.getDefaultFont(), 100, yStart + 100, 200, 30);
		ipField.setBackgroundColor(Color.blue);
		init++;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.setColor(Color.white);
		g.drawRect(40, 135, 580, 300);
		g.drawRect(39, 134, 582, 302);
		clientNameField.render(container, g);
		ipField.render(container, g);

		for (int i = 0; i <menuItems.length; i++)
		{
			if (i != selectedIndex)
			{
				if (availableMenuItems.contains(i))
					g.setColor(Color.white);
				else
					g.setColor(Color.gray);

			}
			else
				g.setColor(Color.yellow);
			g.drawString(menuItems[i], 50, yStart + 55 + i * 50);
		}

		g.setColor(Color.darkGray);
		g.fillRect(400, yStart + 80, 200, 200);

		g.setColor(Color.white);
		g.drawRect(400, yStart + 80, 200, 200);
		// g.drawRect(25, 30, 600, 310);
		g.drawString("Players", 400, yStart + 55);

		for (int i = 0; i < players.size(); i++)
		{
			g.drawString(players.get(i), 405, yStart + 85 + i * 30);
		}
	}



	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {


		if (inputDelay == 0)
		{
			if (container.getInput().isKeyDown(Input.KEY_DOWN))
			{
				int index = availableMenuItems.indexOf(selectedIndex);
				index = (index + 1) % availableMenuItems.size();

				selectedIndex = availableMenuItems.get(index);
				if (selectedIndex == 0) {
					clientNameField.setFocus(true);
					clientNameField.setAcceptingInput(true);
					clientNameField.setInput(container.getInput());
				}
				else if (selectedIndex == 1)
					ipField.setFocus(true);
				else
					ipField.setFocus(false);
				inputDelay = 200;
			}
			else if (container.getInput().isKeyDown(Input.KEY_UP))
			{
				int index = availableMenuItems.indexOf(selectedIndex);
				if (index == 0)
					index = availableMenuItems.size() - 1;
				else
					index--;

				selectedIndex = availableMenuItems.get(index);
				if (selectedIndex == 0)
					clientNameField.setFocus(true);
				else if (selectedIndex == 1)
					ipField.setFocus(true);

				inputDelay = 200;
			}
			else if(container.getInput().isKeyDown(Input.KEY_ENTER))
			{
				if (selectedIndex == 2)
				{
					server = new TCPServer();
					server.startServer();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { }


					System.out.println("TRY AND CONNECT");

					// Add starting heroes if they haven't been added yet
					if (persistentStateInfo.getClientProfile().getHeroes().size() == 0)
					{
						for (String heroName : TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getStartingHeroIds())
							persistentStateInfo.getClientProfile().addHero(HeroResource.getHero(heroName));
					}

					client = new TCPClient("127.0.0.1");
					client.registerPacketHandler(this);
					try {
						client.startAndConnect();
					} catch (Exception e) {
						e.printStackTrace();
					}

					availableMenuItems.clear();
					availableMenuItems.add(0);
					availableMenuItems.add(4);

					selectedIndex = 4;
				}
				else if (selectedIndex == 3)
				{
					selectedIndex = 0;

					System.out.println("TRY AND CONNECT");

					// Add starting heroes if they haven't been added yet
					if (persistentStateInfo.getClientProfile().getHeroes().size() == 0)
					{
						for (String heroName : TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getStartingHeroIds())
							persistentStateInfo.getClientProfile().addHero(HeroResource.getHero(heroName));
					}

					client = new TCPClient(ipField.getText());
					client.registerPacketHandler(this);
					try {
						client.startAndConnect();
					} catch (Exception e) {
						e.printStackTrace();
					}

					availableMenuItems.clear();
					availableMenuItems.add(0);
					availableMenuItems.add(1);
				}
				else if (selectedIndex == 4)
				{
					selectedIndex = 5;
					client.sendMessage(new Message(MessageType.GAME_READY));
					availableMenuItems.clear();
					availableMenuItems.add(0);
					availableMenuItems.add(5);
				}
				else if (selectedIndex == 5)
				{
					client.sendMessage(new Message(MessageType.START_GAME));
					availableMenuItems.clear();
					availableMenuItems.add(0);
				}

				inputDelay = 200;
			}
		}
		else
			inputDelay = Math.max(0, inputDelay - delta);

		if (init == 1)
		{
			/*
			System.out.println("TRY AND CONNECT");

			// Add starting heroes if they haven't been added yet
			if (persistentStateInfo.getClientProfile().getStartingHeroIds() != null)
			{
				for (Integer heroId : persistentStateInfo.getClientProfile().getStartingHeroIds())
					persistentStateInfo.getClientProfile().addHero(HeroResource.getHero(heroId));
				persistentStateInfo.getClientProfile().setStartingHeroIds(null);
			}

			client = new TCPClient("127.0.0.1");
			client.registerPacketHandler(this);
			try {
				client.startAndConnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/

			init = 0;
		}

		if (!ready && container.getInput().isKeyDown(Input.KEY_R))
		{
			ready = true;
			client.sendMessage(new Message(MessageType.GAME_READY));
		}
		else if (ready && container.getInput().isKeyDown(Input.KEY_S))
		{
			client.sendMessage(new Message(MessageType.START_GAME));
		}
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_MENU_MULTI;
	}

	@Override
	public void handleIncomingPacket(Client client, Object packet) {
		switch (((Message) packet).getMessageType())
		{
			case CLIENT_ID:
				int clientId = ((ClientIDMessage) packet).getClientId();
				// client.sendMessage(new ClientRegistrationMessage(persistentStateInfo.getClientProfile().getName(), clientId, persistentStateInfo.getClientProfile().getHeroes().size()));
				client.sendMessage(new ClientRegistrationMessage(clientNameField.getText(), clientId));
				persistentStateInfo.setClientId(clientId);
				System.out.println("CLIENT ID " + clientId);
				for (CombatSprite cs : persistentStateInfo.getClientProfile().getHeroes())
					cs.setId(persistentStateInfo.getClientId() * 256 + cs.getId());
				break;

			case CLIENT_BROADCAST_HERO:
				persistentStateInfo.getClientProfile().addNetworkHeroes(((BackedSpriteMessage) packet).getSprites());
				System.out.println("ADDED HEROES " + persistentStateInfo.getClientProfile().getHeroes().size());
				break;
			case GAME_READY:
				persistentStateInfo.getClientProfile().initializeValues();
				persistentStateInfo.getClientProgress().initializeValues();
				persistentStateInfo.getClientProfile().initialize();
				System.out.println("Send game ready " + persistentStateInfo.getClientProfile().getHeroes().size());
				client.sendMessage(new BackedSpriteMessage(persistentStateInfo.getClientProfile().getHeroes()));
				break;
			case START_GAME:
				persistentStateInfo.setClient(this.client);
				persistentStateInfo.setServer(server);
				this.start(LoadTypeEnum.TOWN, "eriumcastle", "SouthWestHallStairsEntrance");
				break;
			case PLAYER_LIST:
				this.players.clear();
				PlayerListMessage plm = (PlayerListMessage) packet;
				for (ClientInfo ci : plm.getClientInfos())
				{
					players.add(ci.getName());
				}

			default:
				break;
		}
	}

	@Override
	public void handlerRegistered(Client client) {

	}

	@Override
	public void stringEntered(String string, String action) {
		

	}
}
