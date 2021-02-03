package tactical.game.menu;

public class ShopMenu { /* extends Menu implements YesNoListener
{
	private int x;

	private boolean buy = true;
	private Button switchViewButton;

	private ArrayList<Item> items;
	// private Rectangle itemSelectRect;
	private Rectangle itemUpRect;
	private Rectangle itemDownRect;
	private int mouseOverItems = -1;
	private int itemOffset = 0;
	private Item selectedItem = null;

	private ArrayList<CombatSprite> heroes;
	private int heroOffset = 0;
	private int mouseOverHeroes = -1;
	private Rectangle heroUpRect;
	private Rectangle heroDownRect;
	// private Rectangle charSelectRect;
	private Button buyButton;
	private CombatSprite selectedHero = null;


	private ArrayList<String> differences;
	private int gold;
	private double sellPercent = .75;
	private double buyPercent = 1;
	private int waitingYesNo = 0;
	private EquippableItem oldItem;

	public ShopMenu(GameContainer gc, StateInfo stateInfo, double sellPercent, double buyPercent, int[] itemIds) {
		super(Panel.PANEL_SHOP);
		x = (CommRPG.GAME_SCREEN_SIZE.width - 700) / 2;
		switchViewButton = new Button(x + 100, 35, 140, 20, "Switch to sell");
		// itemSelectRect = new Rectangle(x + 15, 90, 655, 360);
		itemUpRect = new Rectangle(x + 670, 91, 15, 15);
		itemDownRect = new Rectangle(x + 670, 435, 15, 15);
		heroUpRect = new Rectangle(x + 670, 476, 15, 15);
		heroDownRect = new Rectangle(x + 670, 650, 15, 15);
		buyButton = new Button(x + 570, 680, 100, 20, "Buy");
		// charSelectRect = new Rectangle(x + 15, 475, 655, 180);

		this.heroes = stateInfo.getClientProfile().getHeroes();
		this.gold = stateInfo.getClientProfile().getGold();
		items = new ArrayList<Item>();
		differences = new ArrayList<String>();

		for (Integer i : itemIds)
			items.add(ItemResource.getItem(i, stateInfo));

		selectedHero = heroes.get(0);
		this.sellPercent = sellPercent;
		this.buyPercent = buyPercent;
	}

	@Override
	public MenuUpdate handleUserInput(FCInput input, StateInfo stateInfo) {
		/*
		if (rightClick)
		{
			stateInfo.getClientProfile().setGold(gold);
			return true;
		}

		// Handle switching the view from buy/sell
		if (switchViewButton.handleUserInput(mouseX, mouseY, leftClick))
		{
			if (buy)
			{
				switchViewButton.setText("Switch to buy");
				buyButton.setText("Sell");
			}
			else
			{
				switchViewButton.setText("Switch to sell");
				buyButton.setText("Buy");
			}

			differences.clear();
			buy = !buy;
			mouseOverItems = -1;
			itemOffset = 0;
			selectedItem = null;
		}

		// Handle heroes
		if (leftClick && heroUpRect.contains(mouseX, mouseY))
			heroOffset = Math.max(heroOffset - 1, 0);
		else if (leftClick && heroes.size() > 6 && heroDownRect.contains(mouseX, mouseY))
			heroOffset = Math.min(heroOffset + 1, heroes.size() - 6);

		if (buyButton.handleUserInput(mouseX, mouseY, leftClick) && selectedItem != null)
		{
			if (buy)
			{
				if (selectedHero.getItemsSize() < 4)
				{
					selectedHero.addItem(selectedItem);
					gold -= (int) (selectedItem.getCost() * buyPercent);
					stateInfo.getClientProfile().setGold(gold);

					if (selectedItem.isEquippable() && selectedHero.isEquippable((EquippableItem) selectedItem))
					{
						waitingYesNo = 1;
						stateInfo.addMenu(new YesNoMenu(stateInfo.getGc(), "Would you like to equip it now?", this));
					}
				}
				else
					stateInfo.sendMessage(new ChatMessage(MessageType.SEND_INTERNAL_MESSAGE, "SYSTEM", "Selected character already has 4 items."));
			}
			else
			{
				selectedHero.removeItem(selectedItem);
				gold += (int) (selectedItem.getCost() * sellPercent);
				selectedItem = null;
			}
		}

		// Select Heroes
		if (charSelectRect.contains(mouseX, mouseY))
		{
			int over = ((mouseY - 475) / 30);

			if (over < heroes.size())
			{
				mouseOverHeroes = over;
				if (leftClick)
				{
					selectedHero = heroes.get(mouseOverHeroes + heroOffset);
					if (!buy)
					{
						selectedItem = null;
					}
				}
			}
		}

		else
			mouseOverHeroes = -1;

		// Handle items
		if (itemSelectRect.contains(mouseX, mouseY))
		{
			int over = ((mouseY - 90) / 30);

			if (buy)
			{
				if (over < items.size())
				{
					mouseOverItems = over;
					if (leftClick)
					{
						selectedItem = items.get(mouseOverItems + itemOffset);
						determineDifferences();
					}
				}
			}
			else
			{
				if (over < selectedHero.getItemsSize())
				{
					mouseOverItems = over;
					if (leftClick)
					{
						selectedItem = selectedHero.getItem(mouseOverItems + itemOffset);
						determineDifferences();
					}
				}
			}
		}
		else
			mouseOverItems = -1;

		if (leftClick && itemUpRect.contains(mouseX, mouseY))
			itemOffset = Math.max(itemOffset - 1, 0);
		else if (buy && leftClick && items.size() > 12 && itemDownRect.contains(mouseX, mouseY))
			itemOffset = Math.min(itemOffset + 1, items.size() - 12);
		*/

	/*
		return MenuUpdate.MENU_CLOSE;
	}

	public void determineDifferences()
	{
		differences.clear();
		if (selectedItem.isEquippable())
		{
			int type = ((EquippableItem) selectedItem).getItemType();

			for (CombatSprite hero : heroes)
			{
				EquippableDifference ed = null;
				if (hero.isEquippable((EquippableItem) selectedItem))
				{

					if (type == EquippableItem.TYPE_WEAPON)
						ed = Item.getEquippableDifference(hero.getEquippedWeapon(), (EquippableItem) selectedItem);
					else if (type == EquippableItem.TYPE_ARMOR)
						ed = Item.getEquippableDifference(hero.getEquippedArmor(), (EquippableItem) selectedItem);
					else if (type == EquippableItem.TYPE_RING)
						ed = Item.getEquippableDifference(hero.getEquippedRing(), (EquippableItem) selectedItem);
					differences.add("ATT: " + ed.atk +
						" DEF: " + ed.def +
						" AGI: " + ed.spd);
				}
				else
					differences.add("Can not equip");
			}
		}
	}

	@Override
	public void render(FCGameContainer gc, Graphics graphics) {
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, 25, 700, 700, graphics);


		graphics.setColor(Panel.COLOR_FOREFRONT);

		// Draw Shop Type
		graphics.drawString("Shop", x + 15, 35);

		switchViewButton.render(gc, graphics);

		// Draw items box
		if (mouseOverItems != -1)
		{
			graphics.setColor(Panel.COLOR_MOUSE_OVER);
			graphics.fillRect(x + 15, 90 + (mouseOverItems * 30), 655, 30);
		}

		if (buy)
			renderBuy(graphics);
		else
			renderSell(graphics);

		graphics.setColor(Panel.COLOR_FOREFRONT);
		graphics.drawRect(x + 200, 60, 370, 390);
		graphics.drawRect(x + 15, 60, 670, 390);

		graphics.drawLine(x + 15, 90, x + 685, 90);
		graphics.drawString("Name", x + 25, 65);
		graphics.drawString("Description", x + 210, 65);
		graphics.drawString("Cost", x + 580, 65);


		// Draw buy buttons
		buyButton.render(gc, graphics);

		graphics.drawString("Gold: " + gold, x + 15, 680);

		// Draw usuable by box
		if (mouseOverHeroes != -1)
		{
			graphics.setColor(Panel.COLOR_MOUSE_OVER);
			graphics.fillRect(x + 15, 475 + (mouseOverHeroes * 30), 655, 30);
		}

		graphics.setColor(Panel.COLOR_FOREFRONT);

		for (int i = 0; i < Math.min(heroes.size(), 6); i++)
		{
			if (heroes.get(i + heroOffset) == selectedHero)
			{
				graphics.setColor(Panel.COLOR_MOUSE_OVER);
				graphics.fillRect(x + 15, 475 + (i * 30), 655, 30);
				graphics.setColor(Panel.COLOR_FOREFRONT);
			}

			graphics.drawString(heroes.get(i + heroOffset).getName(), x + 25, 485 + i * 30);

			if (differences.size() > 0)
				graphics.drawString(differences.get(i),
										x + 210, 485 + i * 30);
		}


		graphics.drawRect(x + 15, 475, 670, 190);
		graphics.drawLine(x + 200, 475, x + 200, 665);

		graphics.setColor(Color.darkGray);
		graphics.fillRect(x + 670, 91, 15, 359);
		graphics.fillRect(x + 670, 476, 15, 189);

		graphics.setColor(Color.lightGray);
		Panel.fillRect(itemUpRect, graphics);
		Panel.fillRect(itemDownRect, graphics);
		Panel.fillRect(heroUpRect, graphics);
		Panel.fillRect(heroDownRect, graphics);

		graphics.setColor(Panel.COLOR_FOREFRONT);
		graphics.drawString("^", itemUpRect.getX() + 2, itemUpRect.getY() + 2);
		graphics.drawString("^", heroUpRect.getX() + 2, heroUpRect.getY() + 2);

		graphics.drawString("v", itemDownRect.getX() + 2, itemDownRect.getY() - 1);
		graphics.drawString("v", heroDownRect.getX() + 2, heroDownRect.getY() - 1);


		/*
		graphics.setColor(Color.red);
		Menu.drawRect(charSelectRect, graphics);
		*/
	/*
	}

	private void renderBuy(Graphics graphics)
	{
		graphics.setColor(Panel.COLOR_FOREFRONT);
		for (int i = 0; i < Math.min(items.size(), 12); i++)
		{
			if (items.get(i + itemOffset) == selectedItem)
			{
				graphics.setColor(Panel.COLOR_MOUSE_OVER);
				graphics.fillRect(x + 15, 90 + (i * 30), 655, 30);
				graphics.setColor(Panel.COLOR_FOREFRONT);
			}

			graphics.drawString(items.get(i + itemOffset).getName(), x + 25, 95 + i * 30);
			graphics.drawString(items.get(i + itemOffset).getDescription(), x + 210, 95 + i * 30);
			graphics.drawString((int)(items.get(i + itemOffset).getCost() * buyPercent)+ "" , x + 580, 95 + i * 30);
		}
	}

	private void renderSell(Graphics graphics)
	{
		graphics.setColor(Panel.COLOR_FOREFRONT);
		for (int i = 0; i < Math.min(selectedHero.getItemsSize(), 4); i++)
		{
			if (selectedHero.getItem(i + itemOffset) == selectedItem)
			{
				graphics.setColor(Panel.COLOR_MOUSE_OVER);
				graphics.fillRect(x + 15, 90 + (i * 30), 655, 30);
				graphics.setColor(Panel.COLOR_FOREFRONT);
			}

			if (selectedHero.getEquipped().get(i))
				graphics.drawString(selectedHero.getItem(i + itemOffset).getName() + " (EQ)", x + 25, 95 + i * 30);
			else
				graphics.drawString(selectedHero.getItem(i + itemOffset).getName(), x + 25, 95 + i * 30);
			graphics.drawString(selectedHero.getItem(i + itemOffset).getDescription(), x + 210, 95 + i * 30);
			graphics.drawString((int)(selectedHero.getItem(i + itemOffset).getCost() * sellPercent) + "" , x + 580, 95 + i * 30);
		}
	}

	@Override
	public boolean valueSelected(StateInfo stateInfo, boolean value) {
		// Equip the item based on value
		if (waitingYesNo == 1)
		{
			if (value)
			{
				oldItem = selectedHero.equipItem((EquippableItem) selectedItem);
				determineDifferences();

				if (oldItem != null)
				{
					waitingYesNo = 2;
					//stateInfo.addMenu(new YesNoMenu(stateInfo.getGc(),
						//	"Would you like to sell your previously equipped " + oldItem.getName() + "?", this));
				}
			}
		}
		else if (waitingYesNo == 2)
		{
			if (value)
			{
				selectedHero.removeItem(oldItem);
				gold += (int) (oldItem.getCost() * sellPercent);
				stateInfo.getClientProfile().setGold(gold);
				oldItem = null;
			}
		}
		return false;
	} */
}
