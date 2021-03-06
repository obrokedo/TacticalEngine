package tactical.game.menu;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.item.Item;
import tactical.game.item.Item.ItemDurability;
import tactical.game.listener.MenuListener;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class HeroesStatMenu extends Menu
{
	protected static final int VIEW_LEVEL = 0;
	protected static final int VIEW_STATS = 1;
	protected static final int VIEW_DIFFS = 2;
	protected static final Color COLOR_NONE = new Color(204, 0, 70);

	protected Image emptySpot;
	protected int yOffsetTop = 1, yOffsetBot = 10;
	protected int selectedIndex = 0;
	protected int listPosition = 0;
	protected ArrayList<CombatSprite> heroes;
	protected String[][] itemNames;
	protected ArrayList<Item> items;
	protected CombatSprite selectedHero;
	protected int view = VIEW_LEVEL;

	protected Portrait selectedHeroPortrait;

	public HeroesStatMenu(StateInfo stateInfo)
	{
		this(stateInfo, null);
	}

	/**
	 * Constructor to create a HeroesStatMenu with all of the heroes in the party
	 * 
	 * @param stateInfo
	 * @param listener
	 */
	public HeroesStatMenu(StateInfo stateInfo, MenuListener listener)
	{
		super(PanelType.PANEL_HEROS_OVERVIEW);
		emptySpot = stateInfo.getResourceManager().getSpriteSheet("items").getSprite(TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getEmptyItemIndexX(), 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getEmptyItemIndexY());
		heroes = stateInfo.getClientProfile().getHeroesInParty();
		
		for (CombatSprite cs : stateInfo.getAllHeroes()) {
			if (!heroes.stream().anyMatch(h -> h.getId() == cs.getId()))
				heroes.add(cs);
		}
		updateCurrentHero(stateInfo);
		this.listener = listener;
		this.listPosition = 0;
	}
	
	protected HeroesStatMenu(PanelType panelType, Iterable<CombatSprite> chooseableSprites, StateInfo stateInfo, MenuListener listener) {
		super(panelType);
		heroes = new ArrayList<>();
		for (CombatSprite cs : chooseableSprites)
			heroes.add(cs);
		updateCurrentHero(stateInfo);
		this.listener = listener;
		this.listPosition = 0;
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		if (selectedHeroPortrait != null)
			this.selectedHeroPortrait.update(delta);
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		int sizeMax = 6;
		drawHeroSpecifics(graphics);
		
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(20,
				yOffsetBot + 117,
				PaddedGameContainer.GAME_SCREEN_SIZE.width - 40,
			112, graphics, null);


		graphics.setColor(Color.white);
		graphics.setLineWidth(2);
		graphics.drawRoundRect(25,
				yOffsetBot + (134 + 15 * (selectedIndex - listPosition)),
				269, 15, 3);
		StringUtils.drawString("NAME", 27,
				yOffsetBot + 113, graphics);

		if (view == VIEW_LEVEL)
		{
			StringUtils.drawString("LEVEL", 127,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("EXP", 227,
					yOffsetBot + 113, graphics);
		}
		else if (view == VIEW_STATS)
		{
			StringUtils.drawString("HP", 92,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("MP", 127,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("ATT", 162,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("DEF", 197,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("AGI", 232,
					yOffsetBot + 113, graphics);
			StringUtils.drawString("MOV", 267,
					yOffsetBot + 113, graphics);
		}

		
		for (int count = listPosition; count < Math.min(heroes.size(),  listPosition + sizeMax); count++)
		{				
			if (heroes.get(count).getCurrentHP() <= 0) {
				graphics.setColor(Color.red);
			} else {
				graphics.setColor(Color.white);
			}
			int drawY = yOffsetBot + (128 + 15 * (count - (listPosition)));
			StringUtils.drawString(heroes.get(count).getName(),
					27,
					drawY, graphics);

			if (view == VIEW_LEVEL)
			{
				StringUtils.drawString(heroes.get(count).getLevel() + "", 127,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getExp() + "", 227,
						drawY, graphics);
			}
			else if (view == VIEW_STATS)
			{
				StringUtils.drawString(heroes.get(count).getMaxHP() + "", 92,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getMaxMP() + "", 127,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getMaxAttack() + "", 162,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getMaxDefense() + "", 197,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getMaxSpeed() + "", 232,
						drawY, graphics);
				StringUtils.drawString(heroes.get(count).getMaxMove() + "", 267,
						drawY, graphics);
			}
			else
			{
				renderMenuItem(graphics, count, drawY);
			}
		}

		postRender(graphics);
	}

	private void drawHeroSpecifics(Graphics graphics) {
		// Draw hero stat box
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(82,
				yOffsetTop + 5,
			218,
			115, graphics, null);
		graphics.setColor(Color.white);

		/****************************/
		/* Draw the portrait window	*/
		/****************************/
		if (selectedHero != null)
		{
			int x = (PaddedGameContainer.GAME_SCREEN_SIZE.width - 280) / 2;
			int y = (PaddedGameContainer.GAME_SCREEN_SIZE.height - 226) / 2 -1;
			if (selectedHeroPortrait != null)
				selectedHeroPortrait.render(x, y, graphics);
		}

		graphics.setColor(Color.white);
		StringUtils.drawString(selectedHero.getName() + " " + selectedHero.getCurrentProgression().getClassName() +
				" L" + selectedHero.getLevel(),
				88,
				yOffsetTop, graphics);

		

		

		drawHeroSpecificsLeft(graphics);

		drawHeroSpecificsRight(graphics);
	}

	protected void drawHeroSpecificsRight(Graphics graphics) {
		StringUtils.drawString("ITEMS", 200,
				yOffsetTop + 17, graphics);
		
		// Draw hero items
		if (itemNames != null && items.size() > 0)
			for (int i = 0; i < items.size(); i++)
			{
				if (selectedHero.getEquipped().get(i))
				{
					graphics.setColor(Color.yellow);
					StringUtils.drawString("EQ", 190,
							yOffsetTop + (27 + i * 20), graphics);
					graphics.setColor(Color.red);
					if (selectedHero.getItem(i).getDurability() == ItemDurability.DAMAGED)
						StringUtils.drawString("DM", 190,
							yOffsetTop + (37 + i * 20), graphics);
					else if (selectedHero.getItem(i).getDurability() == ItemDurability.BROKEN)
						StringUtils.drawString("BR", 190,
								yOffsetTop + (37 + i * 20), graphics);
					graphics.setColor(Color.white);
				}

				StringUtils.drawString(itemNames[i][0], 210,
					yOffsetTop + (27 + i * 20), graphics);
				if (itemNames[i].length > 1)
					StringUtils.drawString(itemNames[i][1], 225,
							yOffsetTop + (37 + i * 20), graphics);
			}
		else
		{
			graphics.setColor(COLOR_NONE);
			StringUtils.drawString("NONE", 210,
				yOffsetTop + 27, graphics);
			graphics.setColor(Color.white);
		}
	}

	protected void drawHeroSpecificsLeft(Graphics graphics) {
		StringUtils.drawString("SPELLS", 90,
				yOffsetTop + 17, graphics);
		
		// Draw Hero Spells
		if (selectedHero.getSpellsDescriptors() != null && selectedHero.getSpellsDescriptors().size() > 0)
			for (int i = 0; i < selectedHero.getSpellsDescriptors().size(); i++)
			{
				StringUtils.drawString(selectedHero.getSpellsDescriptors().get(i).getSpell().getName(),
						100,
					yOffsetTop + (27 + i * 20), graphics);
				StringUtils.drawString("Level " + selectedHero.getSpellsDescriptors().get(i).getMaxLevel(), 115,
						yOffsetTop + (37 + i * 20), graphics);
			}
		else
		{
			graphics.setColor(COLOR_NONE);
			StringUtils.drawString("NONE", 100,
				yOffsetTop + 27, graphics);
			graphics.setColor(Color.white);
		}
	}

	protected void postRender(Graphics g)
	{
		
	}

	protected void renderMenuItem(Graphics graphics, int index, int drawY)
	{
		
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo)
	{
		if (input.isKeyDown(KeyMapping.BUTTON_UP))
		{			
			return onUp(stateInfo);
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_DOWN))
		{
			return onDown(stateInfo);
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_LEFT))
		{
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
			return onLeft(stateInfo);
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_RIGHT))
		{
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
			return onRight(stateInfo);
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{			
			return onBack(stateInfo);
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3))
		{			
			return onConfirm(stateInfo);
		}

		return MenuUpdate.MENU_NO_ACTION;
	}

	protected MenuUpdate onLeft(StateInfo stateInfo)
	{
		if (view > 0)
			view--;
		else
			view = 1;
		return MenuUpdate.MENU_ACTION_LONG;
	}

	protected MenuUpdate onRight (StateInfo stateInfo)
	{
		if (view == 1)
			view = 0;
		else
			view++;
		return MenuUpdate.MENU_ACTION_LONG;
	}

	protected MenuUpdate onBack(StateInfo stateInfo)
	{
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		selectedHero = null;
		return MenuUpdate.MENU_CLOSE;
	}

	protected MenuUpdate onConfirm(StateInfo stateInfo)
	{
		stateInfo.sendMessage(new SpriteContextMessage(MessageType.SHOW_HERO, selectedHero));
		return MenuUpdate.MENU_ACTION_LONG;
	}

	protected MenuUpdate onUp(StateInfo stateInfo)
	{
		if (selectedIndex > 0)
		{
			if (listPosition > (selectedIndex - 1)) 
				listPosition--;
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
			selectedIndex--;
			updateCurrentHero(stateInfo);
			return MenuUpdate.MENU_ACTION_LONG;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	protected MenuUpdate onDown(StateInfo stateInfo)
	{
		int MAX_HEROES_SHOWN = 6;
		if (selectedIndex < heroes.size() - 1)
		{
			if (listPosition + MAX_HEROES_SHOWN <= (selectedIndex + 1)) 
				listPosition++;
			
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
			selectedIndex++;
			updateCurrentHero(stateInfo);
			return MenuUpdate.MENU_ACTION_LONG;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	protected void updateCurrentHero(StateInfo stateInfo)
	{
		selectedHero = heroes.get(selectedIndex);
		selectedHeroPortrait = Portrait.getPortrait(selectedHero, stateInfo);
		updateHeroItems();
	}
	
	protected void updateHeroItems() {
		itemNames = new String[selectedHero.getItemsSize()][];
		items = new ArrayList<>();
		for (int i = 0; i < selectedHero.getItemsSize(); i++) {
			itemNames[i] = StringUtils.splitItemString(selectedHero.getItem(i).getName());
			items.add(selectedHero.getItem(i));
		}
	}

	@Override
	public Object getExitValue() {
		return null;
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
	


	@Override
	public boolean makeAddSounds() {
		
		return true;
	}

	@Override
	public boolean makeRemoveSounds() {
		
		return true;
	}
}
