package tactical.game.menu.shop;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.item.Item.EquippableDifference;
import tactical.game.listener.MenuListener;
import tactical.game.menu.HeroesStatMenu;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class HeroesBuyMenu extends HeroesStatMenu
{
	protected Item selectedShopItem;
	protected ArrayList<String> differences = new ArrayList<>();
	protected boolean showDiffs = true;

	public HeroesBuyMenu(StateInfo stateInfo, MenuListener listener, Item item) {
		this(stateInfo, listener, item, true);
		selectedShopItem = item;
		if (selectedShopItem != null && selectedShopItem.isEquippable())
		{
			view = VIEW_DIFFS;
			determineDifferences(stateInfo);
		}
	}
	
	public HeroesBuyMenu(StateInfo stateInfo, MenuListener listener, Item item, boolean showDiffs) {
		super(stateInfo, listener);
		selectedShopItem = item;
		this.showDiffs = showDiffs;
		if (showDiffs && selectedShopItem.isEquippable())
		{
			view = VIEW_DIFFS;
			determineDifferences(stateInfo);
		}
	}

	public void determineDifferences(StateInfo stateInfo)
	{
		differences.clear();
		if (selectedShopItem.isEquippable())
		{
			int type = ((EquippableItem) selectedShopItem).getItemType();

			for (CombatSprite hero : heroes)
			{
				EquippableDifference ed = null;
				if (hero.isEquippable((EquippableItem) selectedShopItem))
				{
					if (type == EquippableItem.TYPE_WEAPON)
						ed = Item.getEquippableDifference(hero.getEquippedWeapon(), (EquippableItem) selectedShopItem);
					else if (type == EquippableItem.TYPE_ARMOR)
						ed = Item.getEquippableDifference(hero.getEquippedArmor(), (EquippableItem) selectedShopItem);
					else if (type == EquippableItem.TYPE_RING)
						ed = Item.getEquippableDifference(hero.getEquippedRing(), (EquippableItem) selectedShopItem);					
					// differences.add("ATT " + hero.getMaxAttack() + "-" + (hero.getMaxAttack() + ed.atk) +
					differences.add("ATT " + ed.atk +
						" DEF: " + ed.def +
						" AGI: " + ed.spd);
				}
				else
					differences.add("Can not equip");
			}
		}
	}

	@Override
	protected void renderMenuItem(Graphics graphics, int index, int drawY)
	{
		if (view == VIEW_DIFFS)
		{
			StringUtils.drawString(differences.get(index), 92, drawY, graphics);
		}
	}


	@Override
	protected MenuUpdate onLeft(StateInfo stateInfo) {
		if (view > 0)
			view--;
		else if (showDiffs && selectedShopItem.isEquippable())
			view = 2;
		else
			return super.onLeft(stateInfo);
		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	protected MenuUpdate onRight(StateInfo stateInfo) {
		if ((view == 2)
				|| (view == 1 && (!showDiffs || !selectedShopItem.isEquippable())))
			view = 0;
		else
			view++;
		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	protected MenuUpdate onConfirm(StateInfo stateInfo) {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public Object getExitValue() {
		return selectedHero;
	}
}
