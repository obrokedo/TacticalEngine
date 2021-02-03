package tactical.game.menu.town;

import java.awt.Point;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.state.StateInfo;
import tactical.game.battle.BattleEffect;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.ChooseItemMenu;
import tactical.game.menu.ItemOption;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.YesNoMenu;
import tactical.game.menu.shop.HeroesBuyMenu;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class TownChooseItemMenu extends ChooseItemMenu implements MenuListener {

	protected enum TownStepEnum
	{
		GIVE_SELECT_ITEM,
		GIVE_SHOW_SPEECH,
		GIVE_SELECT_HERO,
		DROP_SELECT_ITEM,
		DROP_CONFIRM,
		EQUIP_SELECT,
		EQUIP_SELECT_WEAPON,
		EQUIP_SELECT_ACC,
		USE_SELECT_ITEM,
		USE_SELECT_TARGET
	}
	
	private ItemOption option;
	private TownStepEnum step;
	private MenuConfiguration menuConfig = null;
	
	private CombatSprite givingHero = null;
	private int givingIndex = 0;
	
	public TownChooseItemMenu(StateInfo stateInfo, int option) {
		super(stateInfo, null);
		menuConfig = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration();
		this.option = ItemOption.values()[option];
		updateHeroItems();
		
		switch (this.option) {
			case GIVE:
				step = TownStepEnum.GIVE_SELECT_ITEM;
				break;
			case DROP:
				step = TownStepEnum.DROP_SELECT_ITEM;
				break;
			case EQUIP:
				step = TownStepEnum.EQUIP_SELECT;
				break;
			case USE:
				step = TownStepEnum.USE_SELECT_ITEM;
				break;
		}
	}
	
	// An item has been selected from this menu
	@Override
	protected boolean itemSelected(StateInfo stateInfo) {
		switch (option) {
			case GIVE:
				if (step == TownStepEnum.GIVE_SELECT_ITEM) {
					step = TownStepEnum.GIVE_SHOW_SPEECH;				
					stateInfo.addMenu(new SpeechMenu(menuConfig.getGiveToWhoText(selectedHero.getName(), items.get(selectingItemIndex).getName()), null, this, stateInfo));
					givingHero = selectedHero;
					givingIndex = selectingItemIndex;				
					this.selectingItemState = false;				
					return false;
				} else if (step == TownStepEnum.GIVE_SELECT_HERO) {
					Item tradeItem = selectedHero.getItem(selectingItemIndex);
					selectedHero.removeItem(tradeItem);
					
					giveItemImpl(stateInfo);
					
					givingHero.addItem(tradeItem);
					return true;
				}
			case DROP:
				step = TownStepEnum.DROP_CONFIRM;
				
				stateInfo.addMenu(new YesNoMenu(menuConfig.getDropConfirmText(items.get(selectingItemIndex).getName()), 
						stateInfo, this));
				return true;
			case USE:
				Item item = items.get(selectingItemIndex);
				if (item.isUsuable() && item.getItemUse().isUseOutsideBattle()) {
					step = TownStepEnum.USE_SELECT_ITEM;					
					stateInfo.addMenu(new YesNoMenu(menuConfig.getUseTargetText(items.get(selectingItemIndex).getName()), 
							stateInfo, this));
					return true;
				} else {					
					stateInfo.addMenu(new SpeechMenu(menuConfig.getUseFailedText(selectedHero.getName(), items.get(selectingItemIndex).getName()), 
							null, null, stateInfo));
					return true;
				}
			// Nothing to be done for equip
		}
		return false;
	}
	
	

	// An item has been selected from a menu we're listening to
	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		Item item = null;
		switch (step) {
			case GIVE_SHOW_SPEECH:
				step = TownStepEnum.GIVE_SELECT_HERO;
				break;
			case DROP_CONFIRM:
				boolean drop = (Boolean) value;
				if (drop) {
					item = items.get(selectingItemIndex);
					
					stateInfo.addMenu(new SpeechMenu(menuConfig.getDropSuccessText(item.getName())
							, stateInfo));
					selectedHero.removeItem(item);
				}
				break;
			case USE_SELECT_ITEM:
				step = TownStepEnum.USE_SELECT_TARGET;
				stateInfo.addMenu(new HeroesBuyMenu(stateInfo, 
						this, items.get(selectingItemIndex), false));
				break;
			case USE_SELECT_TARGET:
				if (value != null) {
					CombatSprite target = (CombatSprite) value;
					item = items.get(selectingItemIndex);
					String itemUseText = item.getItemUse().getBattleText(target.getName());					
					BattleEffect effects = item.getItemUse().getEffects();
					effects.effectStarted(selectedHero, target);
					String effectText = effects.effectStartedText(selectedHero, target);
					stateInfo.addMenu(new SpeechMenu(itemUseText + (effectText != null ? " " + effectText : ""), stateInfo));
					
					if (item.getItemUse().isSingleUse()) 
						selectedHero.removeItem(item);
				}
				break;
		}
	}
	
	
	
	@Override
	protected void selectedItemChanged() {		
		if (this.selectingItemState && this.option == ItemOption.EQUIP) {
			selectedHero.equipItem((EquippableItem) items.get(selectingItemIndex));
		}
	}

	// Override confirm when equipping items to jump to the next state
	@Override
	protected MenuUpdate onConfirm(StateInfo stateInfo) {
		if (this.selectingItemState && this.option == ItemOption.EQUIP) {
			items.clear();
			
			if (step == TownStepEnum.EQUIP_SELECT_WEAPON) {
				updateEquippableItems(EquippableItem.TYPE_RING);
				step = TownStepEnum.EQUIP_SELECT_ACC;
				if (items.size() == 0) {
					this.selectingItemState = false;
					updateHeroItems();
				} else 
					selectedItemChanged();
			} else {
				selectingItemState = false;
				updateHeroItems();
			}
			return MenuUpdate.MENU_ACTION_LONG;
		} else 
			return super.onConfirm(stateInfo);
	}

	@Override
	protected boolean selectingItemStateStarted(StateInfo stateInfo) {
		if (selectingItemState && this.option == ItemOption.EQUIP) {
			items.clear();
			updateEquippableItems(EquippableItem.TYPE_WEAPON);
			step = TownStepEnum.EQUIP_SELECT_WEAPON;
			if (items.size() == 0) {
				updateEquippableItems(EquippableItem.TYPE_RING);
				step = TownStepEnum.EQUIP_SELECT_ACC;
				if (items.size() == 0) {
					this.selectingItemState = false;
					updateHeroItems();
				} else 
					selectedItemChanged();
			} else 
				selectedItemChanged();			
		} else if (this.step == TownStepEnum.GIVE_SELECT_HERO 
				&& selectedHero.getItemsSize() < 4) {
			// If we select ourselves to give the item to then just ignore stuff
			if (selectedHero == givingHero)
				return true;
			
			giveItemImpl(stateInfo);
			return true;
		}
		
		return false;
	}

	protected void giveItemImpl(StateInfo stateInfo) {
		stateInfo.addMenu(new SpeechMenu(
				menuConfig.getGiveSuccessText(givingHero.getName(), givingHero.getItem(givingIndex).getName(), selectedHero.getName()), stateInfo));
		Item item = givingHero.getItem(givingIndex);
		givingHero.removeItem(item);
		selectedHero.addItem(item);
	}

	@Override
	protected void drawHeroSpecificsRight(Graphics graphics) {
		if (this.selectingItemState && this.option == ItemOption.EQUIP) {
			StringUtils.drawString(items.get(selectingItemIndex).getName(), 210,
					yOffsetTop + (35), graphics);
			StringUtils.drawString("ATT: " + selectedHero.getMaxAttack(), 210,
					yOffsetTop + (50), graphics);
			StringUtils.drawString("DEF: " + selectedHero.getMaxDefense(), 210,
					yOffsetTop + (50 + 1 * 18), graphics);
			StringUtils.drawString("AGI: " + selectedHero.getMaxSpeed(), 210,
					yOffsetTop + (50 + 2 * 18), graphics);
			StringUtils.drawString("MOV: " + selectedHero.getMaxMove(), 210,
					yOffsetTop + (50 + 3 * 18), graphics);
		} else
			super.drawHeroSpecificsRight(graphics);
	}
	
	

	@Override
	protected void drawHeroSpecificsLeft(Graphics graphics) {
		if (this.selectingItemState && this.option == ItemOption.EQUIP) {
			for (int i = 0; i < 4; i++) {
				Point loc = getSelectedItemIconPoint(i);
				
				if (i < items.size())
				{
					Item item = items.get(i);
					graphics.drawImage(item.getImage().getScaledCopy(1.3f), loc.x, loc.y);
				}
				else
				{
					graphics.drawImage(emptySpot.getScaledCopy(1.3f), loc.x, loc.y);
				}
			}
						
			graphics.setLineWidth(2);
			
			Point loc = getSelectedItemIconPoint(selectingItemIndex);
			
			graphics.drawRoundRect(loc.x, loc.y, 20, 32, 3);
		} else
			super.drawHeroSpecificsLeft(graphics);
	}

	/*
	@Override
	protected void updateHeroItems() {
		if (this.option == ItemOption.EQUIP) {
			items.clear();
			updateEquippableItems(EquippableItem.TYPE_WEAPON);
			updateEquippableItems(EquippableItem.TYPE_RING);
		} else
			super.updateHeroItems();
	}
	*/
	
	private void updateEquippableItems(int itemType) {
		int itemCnt = 0;
		for (int i = 0; i < selectedHero.getItemsSize(); i++) {
			Item item = selectedHero.getItem(i);
			if (item.isEquippable() && ((EquippableItem) item).getItemType() == itemType && selectedHero.isEquippable((EquippableItem) item)) {
				items.add(item);
				if (selectedHero.getEquippedWeapon() == item)
					selectingItemIndex = itemCnt;
				itemCnt++;
			}
		}
	}

	// A menu we're listening to has been selected
	@Override
	public void menuClosed() {
		
	}
}
