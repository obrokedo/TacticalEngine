package tactical.game.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import tactical.engine.TacticalGame;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.game.sprite.CombatSprite;

public class InitiativeManager extends Manager
{
	private List<CombatSprite> turnOrder = new ArrayList<>();
	/*
	private class InitiativeMenu extends Panel implements MouseListener
	{
		private boolean displayInit = true;
		// ArrayList<CombatSprite> initOrder;

		public InitiativeMenu(StateInfo stateInfo) {
			super(Panel.PANEL_INITIATIVE);
			stateInfo.registerMouseListener(this);

		}

		@Override
		public void render(FCGameContainer gc, Graphics graphics) {
			/*
			graphics.setColor(Color.white);
			graphics.drawRect(20, 20, 30, 14);
			if (displayInit && initOrder != null && initOrder.size() > 0)
			{
				graphics.drawString("^", 28, 22);
				for (int i = 0; i < 10; i++)
				{
					graphics.drawRect(20, 40 + i * 38, 30, 30);
					graphics.drawImage(initOrder.get(i % initOrder.size()).getCurrentImage(), 24, 42 + i * 38, TRANS);
				}
			}
			else
				graphics.drawString("v", 28, 18);
				*/
		/* }


		@Override
		public boolean mouseUpdate(int frameMX, int frameMY, int mapMX,
				int mapMY, boolean leftClicked, boolean rightClicked,
				StateInfo stateInfo)
		{
			if (leftClicked && Panel.contains(20, 50,
					frameMX, 20, 34, frameMY))
			{
				displayInit = !displayInit;
				return true;
			}

			return false;
		}

		@Override
		public int getZOrder() {
			return MouseListener.ORDER_INIT;
		}
	} */

	private class InitComparator implements Comparator<CombatSprite>
	{
		@Override
		public int compare(CombatSprite c1, CombatSprite c2) {
			return Math.round(c2.getCurrentInit() - c1.getCurrentInit());
		}
	}

	@Override
	public void initialize() {

	}

	private void initializeAfterSprites()
	{
		// initMenu = new InitiativeMenu(stateInfo);
		// The host will initialize the turn order, so check to see if you are the host
		turnOrder.clear();
		// stateInfo.addPanel(initMenu);
	}

	public void updateOnTurn()
	{
		getNextTurn();
	}
	
	private void getNextTurn()
	{
		CombatSprite nextTurn = null;
									
		if (stateInfo.getCombatSprites() != null) {
			// Remove all dead combatants from the turn order
			Iterator<CombatSprite> csIt = turnOrder.iterator();
			while (csIt.hasNext()) {
				CombatSprite cs = csIt.next();
				if (cs.getCurrentHP() <= 0) 
					csIt.remove();
			}
			
			while (nextTurn == null) {
				// Either turn order has not yet been initialized or all of the turns have already happened
				// So create the next "rounds" turn order
				if (turnOrder.size() == 0) {
					for (CombatSprite cs : stateInfo.getCombatSprites()) {			
						// Avoid weird float problems with 0
						if (cs.getCurrentHP() > 0) {
							cs.setCurrentInit(0.1f);
							turnOrder.add(cs);
						}
					}
				}
				
				turnOrder.sort(new InitComparator());
				
				if (turnOrder.get(0).getCurrentInit() >= 100) {
					nextTurn = turnOrder.remove(0);
					if (stateInfo.getCurrentSprite() == null) {								
						stateInfo.getCamera().centerOnSprite(nextTurn, stateInfo.getCurrentMap());
					}
					nextTurn.setCurrentInit(-1);
				} else {
					// +7/8, 1 times or 9/8 of agility, then a random modifier of -1, 0 or +1
					for (CombatSprite cs : turnOrder) {
						cs.setCurrentInit(cs.getCurrentInit() + (cs.getCurrentSpeed() * (TacticalGame.RANDOM.nextFloat() / 4 + .875f)) +
								TacticalGame.RANDOM.nextInt(3) - 1);
					}
				}
			}
		}

		
		/*
		while (nextTurn == null)
		{
			for (CombatSprite cs : stateInfo.getCombatSprites())
			{
				// Increase the sprites initiative by 7 and potentially an additional 1 based on speed
				cs.setCurrentInit(cs.getCurrentInit() + 7 + (TacticalGame.RANDOM.nextInt(100) < cs.getCurrentSpeed() ? 1 : 0));
				if (cs.getCurrentInit() >= 100 && cs.getCurrentHP() > 0)
				{
					if (nextTurn == null || cs.getCurrentInit() > nextTurn.getCurrentInit() ||
						(cs.getCurrentInit() == nextTurn.getCurrentInit() &&
							cs.getCurrentSpeed() > nextTurn.getCurrentInit()))
					{
							nextTurn = cs;
							nextTurn.setCurrentInit(-1);
							if (stateInfo.getCurrentSprite() == null) {								
								stateInfo.getCamera().centerOnSprite(cs, stateInfo.getCurrentMap());
							}
					}
				}
			}
		}
		*/

				
		stateInfo.sendMessage(new SpriteContextMessage(MessageType.COMBATANT_TURN, nextTurn), true);
	}
	
	public void initializeInitOrderFromLoad() {
		turnOrder.clear();
		for (CombatSprite cs : stateInfo.getCombatSprites()) {
			if (cs.getCurrentHP() > 0) {
				turnOrder.add(cs);
			}
		}
		
		turnOrder.sort(new InitComparator());
	}

	@Override
	public void recieveMessage(Message message) {
		switch (message.getMessageType())
		{
			case NEXT_TURN:
				updateOnTurn();
				break;
			case INITIALIZE_BATTLE:
				initializeAfterSprites();
				stateInfo.sendMessage(MessageType.NEXT_TURN);
				break;
			case INITIALIZE_BATTLE_FROM_LOAD:
				initializeInitOrderFromLoad();
				break;
			default:
				break;
		}
	}
}
