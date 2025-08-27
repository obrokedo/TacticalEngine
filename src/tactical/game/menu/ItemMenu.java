package tactical.game.menu;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.config.SpellMenuRenderer;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.BattleSelectionMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpellMessage;
import tactical.engine.state.StateInfo;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.constants.Direction;
import tactical.game.hudmenu.Panel;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.item.Item.EquippableDifference;
import tactical.game.item.Item.ItemDurability;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class ItemMenu extends QuadMenu
{
	protected Image emptySpot;
	protected ItemOption itemOption;
	protected String[] itemDiffs;
	protected boolean choseItem = false;
	protected SpellMenuRenderer spellMenuRenderer;
	protected KnownSpell itemSpellUse = null;
	protected int selectedLevel = 0;
	
	public ItemMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_ITEM, null, false, stateInfo);
		emptySpot = stateInfo.getResourceManager().getSpriteSheet("items").getSprite(
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getEmptyItemIndexX(), 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getEmptyItemIndexY());

		this.enabled = new boolean[4];
		this.icons = new Image[4];
		this.text = new String[4];
		this.paintSelectionCursor = true;		
	}
	
	public void initialize(ItemOption itemOption) {
		this.itemOption = itemOption;
		this.selectedLevel = 0;
		this.initialize();
	}

	@Override
	public void initialize() {
		this.choseItem = false;
		this.spellMenuRenderer = null;
		for (int i = 0; i < 4; i++)
		{
			if (i < stateInfo.getCurrentSprite().getItemsSize())
			{
				Item item = stateInfo.getCurrentSprite().getItem(i);
				
				if (itemOption == ItemOption.EQUIP) {
					if(item.isEquippable() && 
							stateInfo.getCurrentSprite().isEquippable((EquippableItem) item) &&
							item.getDurability() != ItemDurability.BROKEN) {
						enabled[i] = true;
					} else
						enabled[i] = false;
				} else if (itemOption == ItemOption.USE) {
					if (item.isUsuable() && item.getDurability() != ItemDurability.BROKEN) {
						enabled[i] = true;
					} else
						enabled[i] = false;
				} else 
					enabled[i] = true;
				
				if (stateInfo.getCurrentSprite().getEquipped().get(i))
					text[i] = item.getName() + "(EQ)";
				else
					text[i] = item.getName();

				icons[i] = item.getImage();
			}
			else
			{
				enabled[i] = false;
				icons[i] = emptySpot;
			}
		}
		
		findFirstCorrectItem();
		
		for (int i = 0; i < enabled.length; i++) {
			if (enabled[i]) {
				setItemDifferences();
				break;
			}
		}
		
		//if (itemOption == ItemOption.USE)
		//	onDirection(Direction.UP);
	}

	@Override
	protected void renderTextBox(PaddedGameContainer gc, Graphics graphics)
	{
		String[] split = getText(selected).split(" ", 2);
		int locY = 195;
		if (itemOption == ItemOption.USE)
			locY = 160;
		if (itemOption != ItemOption.EQUIP) {
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(195,
				locY - 7 * (split.length == 1 ? 0 : 1),
				getTextboxWidth(),
				15 * (split.length == 1 ? 1 : 2) + 12, graphics, null);
	
			graphics.setColor(COLOR_FOREFRONT);
	
			StringUtils.drawString(split[0], 202,
					locY - 2 - 7 * (split.length == 1 ? 0 : 1), graphics);
			if (split.length > 1)
				StringUtils.drawString(split[1], 202,
					locY + 6, graphics);
		} else {
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(195,
					locY - 30 - 7 * (split.length == 1 ? 0 : 1),
					100,
					15 * (split.length == 1 ? 1 : 2) + 42, graphics, null);
		
			graphics.setColor(COLOR_FOREFRONT);
			StringUtils.drawString(split[0], 202,
					locY - 34 - 7 * (split.length == 1 ? 0 : 1), graphics);
			if (split.length > 1)
				StringUtils.drawString(split[1], 202,
					locY - 30, graphics);
			StringUtils.drawString(itemDiffs[0], 202,
					locY - 20 + 7 * (split.length == 1 ? 0 : 1), graphics);
			StringUtils.drawString(itemDiffs[1], 202,
					locY - 8 + 7 * (split.length == 1 ? 0 : 1), graphics);
			StringUtils.drawString(itemDiffs[2], 202,
					locY + 4 + 7 * (split.length == 1 ? 0 : 1), graphics);
		}
		
		if (spellMenuRenderer != null) {
			spellMenuRenderer.render("", stateInfo.getCurrentSprite(), stateInfo.getResourceManager(), 
					choseItem, selectedLevel, 
					itemSpellUse, 
					stateInfo, graphics, Panel.COLOR_FOREFRONT);
		}
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		if (spellMenuRenderer != null) {
			spellMenuRenderer.update(delta);
		}
		return super.update(delta, stateInfo);
	}

	@Override
	public MenuUpdate onBack() {
		if (choseItem) {
			choseItem = false;
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
			spellMenuRenderer.spellLevelChanged(0);
			stateInfo.sendMessage(MessageType.HIDE_ATTACK_AREA);
			return MenuUpdate.MENU_ACTION_LONG;
		} else {
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
			stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
			return MenuUpdate.MENU_CLOSE;
		}
	}
	
	

	@Override
	protected MenuUpdate onDirection(Direction dir) {
		// If we haven't chosen an item yet then just move the cursor
		if (!this.choseItem) {
			MenuUpdate mu = super.onDirection(dir);
			setItemDifferences();
			setItemSpellRenderer();
			return mu;
		}
		// If an item has been chosen then we are changing spell level
		else {
			if (dir == Direction.LEFT) {
				if (selectedLevel > 0)
				{
					// It's kind of ugly but this menu will not play the sound effect
					// for this menu move, instead it is played by AttackableSpace
					selectedLevel--;			
				}
				else
					selectedLevel = itemSpellUse.getMaxLevel() - 1;
				
				stateInfo.sendMessage(new SpellMessage(MessageType.SHOW_SPELL_LEVEL, itemSpellUse, selectedLevel));
				if (spellMenuRenderer != null)
					spellMenuRenderer.spellLevelChanged(selectedLevel);
			} else if (dir == Direction.RIGHT) {
				if (selectedLevel + 1 < itemSpellUse.getMaxLevel())
				{
					// It's kind of ugly but this menu will not play the sound effect
					// for this menu move, instead it is played by AttackableSpace
					selectedLevel++;			
				} else {
					selectedLevel = 0;
				}
				
				stateInfo.sendMessage(new SpellMessage(MessageType.SHOW_SPELL_LEVEL, itemSpellUse, selectedLevel));
				if (spellMenuRenderer != null)
					spellMenuRenderer.spellLevelChanged(selectedLevel);							
			}
			return MenuUpdate.MENU_ACTION_LONG;
		}
	}
	
	protected void setItemSpellRenderer() {
		if (itemOption == ItemOption.USE) {
			Item item = stateInfo.getCurrentSprite().getItem(this.getSelectedInt());
			if (item.getSpellUse() != null) {
				selectedLevel = 0;
				SpellDefinition sd = item.getSpellUse().getSpell();
				// TODO This breaks generic use-cases
				KnownSpell ks = new KnownSpell(
						(byte) TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getItemMaxLevel(stateInfo.getCurrentSprite(), 
								item, sd), sd);
				this.itemSpellUse = ks;
				this.spellMenuRenderer = TacticalGame.ENGINE_CONFIGURATIOR.getItemSpellUseMenuRenderer(item);
				this.spellMenuRenderer.spellLevelChanged(0);
			} else {
				this.spellMenuRenderer = null;
				this.itemSpellUse = null;
			}
			
		}
	}

	private void findFirstCorrectItem()
	{
		//make sure the item selected is of type equippable to avoid errors when setting item differences
		for (int i = 0; i < 4; i++)
		{
			if (i < stateInfo.getCurrentSprite().getItemsSize())
			{
				Item item = stateInfo.getCurrentSprite().getItem(i);
				
				if (itemOption == ItemOption.EQUIP) {
					if(item.isEquippable())
					{
						onDirection(ConvertInventoryIndexToDirection(i));
						break;
					}
				}
				else if(itemOption == ItemOption.USE)
				{
					if(item.isUsuable())
					{
						onDirection(ConvertInventoryIndexToDirection(i));
						break;
					}
				}
			}
		}
	}
	
	private Direction ConvertInventoryIndexToDirection(int index)
	{
		switch(index)
		{
		case 0:
			return Direction.UP;
		case 1:
			return Direction.LEFT;
		case 2:
			return Direction.RIGHT;
		case 3:
			return Direction.DOWN;
		}
		return Direction.UP;
	}
	
	private void setItemDifferences() {
		if (itemOption == ItemOption.EQUIP) {
			Item item = stateInfo.getCurrentSprite().getItem(this.getSelectedInt());
			Item equippedItem = null;
			switch (((EquippableItem) item).getItemType()) {
			case EquippableItem.TYPE_WEAPON:
				equippedItem = stateInfo.getCurrentSprite().getEquippedWeapon();
				break;
			case EquippableItem.TYPE_RING:
				equippedItem = stateInfo.getCurrentSprite().getEquippedRing();
				break;
			}
			
			EquippableDifference ed = Item.getEquippableDifference((EquippableItem) equippedItem, (EquippableItem) item);
			itemDiffs = new String[] {
					"ATT: " + (stateInfo.getCurrentSprite().getCurrentAttack() + ed.atk) + " (" + ed.atk +")",
					"DEF: " + (stateInfo.getCurrentSprite().getCurrentDefense() + ed.def) + " (" + ed.def +")",
					"AGI: " + (stateInfo.getCurrentSprite().getCurrentSpeed() + ed.spd) + " (" + ed.spd +")"
			};
		}
	}

	@Override
	public MenuUpdate onConfirm() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		Item item = stateInfo.getCurrentSprite().getItem(this.getSelectedInt());
		
		// stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
			
		switch (itemOption)
		{
			case USE:
				// If this is not a spell then just use it now
				if (itemSpellUse == null)
					stateInfo.sendMessage(new BattleSelectionMessage(MessageType.USE_ITEM, this.getSelectedInt()));
				else {
					if (choseItem) {
						// Make sure we have enough charges to cast the spell
						if (item.getSpellUse().getCharges() > selectedLevel)
							stateInfo.sendMessage(new BattleSelectionMessage(MessageType.USE_ITEM, this.getSelectedInt(), selectedLevel));
						else {
							stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, MUSIC_SELECTOR.getInvalidActionSoundEffect(), 1f, false));
							return MenuUpdate.MENU_ACTION_LONG;
						}
					} else {
						choseItem = true;
						selectedLevel = 0;
						stateInfo.sendMessage(new SpellMessage(MessageType.SHOW_SPELL_LEVEL, itemSpellUse, selectedLevel));
						return MenuUpdate.MENU_ACTION_LONG;
					}
				}
				return MenuUpdate.MENU_CLOSE;
			case GIVE:
				stateInfo.sendMessage(new BattleSelectionMessage(MessageType.GIVE_ITEM, this.getSelectedInt()));
				return MenuUpdate.MENU_CLOSE;
			case EQUIP:
				if (!stateInfo.getCurrentSprite().getEquipped().get(this.getSelectedInt()))
					stateInfo.getCurrentSprite().equipItem((EquippableItem) item);
				else
					stateInfo.getCurrentSprite().unequipItem((EquippableItem) item);
				
				int i = getSelectedInt();
				this.initialize();
				setSelectedInt(i);
				setItemDifferences();
				return MenuUpdate.MENU_ACTION_LONG;
			case DROP:
				stateInfo.getCurrentSprite().removeItem(item);
				if (item.isDeal())
					stateInfo.getClientProgress().getDealItems().add(item.getItemId());
				this.initialize();
				boolean moreOptions = false;
				for (boolean e : enabled) {
					if (e) {
						moreOptions = true;
						break;
					}
				}
				
				// If there are more things we can drop just stay here
				if (moreOptions) {
					return MenuUpdate.MENU_ACTION_LONG;
				} else {
					// If there is nothing left to drop, but the unit still has items, go back to
					// the item menu
					if (stateInfo.getCurrentSprite().getItemsSize() > 0)
						stateInfo.sendMessage(MessageType.SHOW_ITEM_OPTION_MENU);
					// Sprite has no items left, go to the battle menu
					else
						stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
				}
					
				return MenuUpdate.MENU_CLOSE;
		}
		
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public int getTextboxWidth()
	{
		return 95;
	}
}

