package tactical.game.menu;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.BattleSelectionMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.constants.Direction;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.item.Item.EquippableDifference;
import tactical.game.item.Item.ItemDurability;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class ItemMenu extends QuadMenu
{
	private Image emptySpot;
	private ItemOption itemOption;
	private String[] itemDiffs;
	
	public enum ItemOption { 
		USE,
		GIVE,
		EQUIP,
		DROP,
		
	}

	public ItemMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_ITEM, null, false, stateInfo);
		emptySpot = stateInfo.getResourceManager().getSpriteSheet("items").getSprite(17, 1);

		this.enabled = new boolean[4];
		this.icons = new Image[4];
		this.text = new String[4];
		this.paintSelectionCursor = true;
	}
	
	public void initialize(ItemOption itemOption) {
		this.itemOption = itemOption;
		this.initialize();
	}

	@Override
	public void initialize() {
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
		
		for (int i = 0; i < enabled.length; i++) {
			if (enabled[i]) {
				setSelectedInt(i);
				setItemDifferences();
				break;
			}
		}
	}

	@Override
	protected void renderTextBox(PaddedGameContainer gc, Graphics graphics)
	{
		String[] split = getText(selected).split(" ", 2);

		if (itemOption != ItemOption.EQUIP) {
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(195,
				195 - 7 * (split.length == 1 ? 0 : 1),
				getTextboxWidth(),
				15 * (split.length == 1 ? 1 : 2) + 12, graphics, null);
	
			graphics.setColor(COLOR_FOREFRONT);
	
			StringUtils.drawString(split[0], 202,
					193 - 7 * (split.length == 1 ? 0 : 1), graphics);
			if (split.length > 1)
				StringUtils.drawString(split[1], 202,
					201, graphics);
		} else {
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(195,
					165 - 7 * (split.length == 1 ? 0 : 1),
					100,
					15 * (split.length == 1 ? 1 : 2) + 42, graphics, null);
		
			graphics.setColor(COLOR_FOREFRONT);
			StringUtils.drawString(split[0], 202,
					161 - 7 * (split.length == 1 ? 0 : 1), graphics);
			if (split.length > 1)
				StringUtils.drawString(split[1], 202,
					165, graphics);
			StringUtils.drawString(itemDiffs[0], 202,
					175 + 7 * (split.length == 1 ? 0 : 1), graphics);
			StringUtils.drawString(itemDiffs[1], 202,
					187 + 7 * (split.length == 1 ? 0 : 1), graphics);
			StringUtils.drawString(itemDiffs[2], 202,
					199 + 7 * (split.length == 1 ? 0 : 1), graphics);
		}
	}

	@Override
	public MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
		return MenuUpdate.MENU_CLOSE;
	}
	
	

	@Override
	protected MenuUpdate onDirection(Direction dir) {
		MenuUpdate mu = super.onDirection(dir);
		setItemDifferences();
		return mu;
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
					"ATK: " + (stateInfo.getCurrentSprite().getCurrentAttack() + ed.atk) + " (" + ed.atk +")",
					"DEF: " + (stateInfo.getCurrentSprite().getCurrentDefense() + ed.def) + " (" + ed.def +")",
					"SPD: " + (stateInfo.getCurrentSprite().getCurrentSpeed() + ed.spd) + " (" + ed.spd +")"
			};
		}
	}

	@Override
	public MenuUpdate onConfirm() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		Item item = stateInfo.getCurrentSprite().getItem(this.getSelectedInt());
		
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
		
		switch (itemOption)
		{
			case USE:
				stateInfo.sendMessage(new BattleSelectionMessage(MessageType.USE_ITEM, this.getSelectedInt()));
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
