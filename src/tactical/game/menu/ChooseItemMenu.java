package tactical.game.menu;

import java.awt.Point;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;
import tactical.game.menu.Menu.MenuUpdate;

public abstract class ChooseItemMenu extends HeroesStatMenu
{
	protected boolean selectingItemState = false;
	protected int selectingItemIndex = 0;
	protected MenuConfiguration menuConfig;

	public ChooseItemMenu(StateInfo stateInfo, MenuListener listener) {
		super(stateInfo, listener);
		this.menuConfig = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration();
	}

	@Override
	protected void postRender(Graphics g) {
		if (selectingItemState)
		{
			g.setLineWidth(2);
			g.setColor(Color.white);
			g.drawRoundRect(207, yOffsetTop + 50 + selectingItemIndex * 20, 80, 20, 3);
			
			Point loc = getSelectedItemIconPoint(selectingItemIndex);
			
			g.drawRoundRect(loc.x, loc.y, 20, 32, 3);
		}
	}

	@Override
	protected MenuUpdate onUp(StateInfo stateInfo) {
		if (selectingItemState)
		{
			selectingItemIndex = 0;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else
			return super.onUp(stateInfo);
	}



	@Override
	protected MenuUpdate onDown(StateInfo stateInfo) {
		if (selectingItemState)
		{
			if (selectedHero.getItemsSize() > 3)
				selectingItemIndex = 3;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else
			return super.onDown(stateInfo);
	}

	

	@Override
	protected MenuUpdate onBack(StateInfo stateInfo) {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		if (selectingItemState)
		{
			selectingItemState = false;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else
		{
			selectedHero = null;
			return MenuUpdate.MENU_CLOSE;
		}
	}

	@Override
	protected MenuUpdate onConfirm(StateInfo stateInfo) {
		// Show the item selection cursor
		if (!selectingItemState)
		{
			// Check if the hero has items to sell
			if (selectedHero.getItemsSize() > 0 ) {
				selectingItemState = true;
				selectingItemIndex = 0;
			}
		}
		// Otherwise we are done, prompt to sell the selected item
		else
		{
			if (itemSelected(stateInfo))
				return MenuUpdate.MENU_CLOSE;
		}

		return MenuUpdate.MENU_ACTION_LONG;
	}
	
	protected abstract boolean itemSelected(StateInfo stateInfo);

	@Override
	protected void drawHeroSpecificsLeft(Graphics graphics) {
		if (selectedHero != null) {
			for (int i = 0; i < 4; i++) {
				Point loc = getSelectedItemIconPoint(i);
				
				if (i < selectedHero.getItemsSize())
				{
					Item item = selectedHero.getItem(i);
					graphics.drawImage(item.getImage().getScaledCopy(1.3f), loc.x, loc.y);
				}
				else
				{
					graphics.drawImage(emptySpot.getScaledCopy(1.3f), loc.x, loc.y);
				}
			}
		}
	}	
	
	@Override
	protected MenuUpdate onLeft(StateInfo stateInfo) {
		if (selectingItemState) {
			if (selectedHero.getItemsSize() > 1)
				selectingItemIndex = 1;
			return MenuUpdate.MENU_ACTION_LONG;
		} else
			return super.onLeft(stateInfo);
	}

	@Override
	protected MenuUpdate onRight(StateInfo stateInfo) {
		if (selectingItemState) {
			if (selectedHero.getItemsSize() > 2)
				selectingItemIndex = 2;
			return MenuUpdate.MENU_ACTION_LONG;
		} else
			return super.onRight(stateInfo);
	}

	private Point getSelectedItemIconPoint(int index) {
		switch (index) {
			case 0:
				return new Point(130, 30);
			case 1:
				return new Point(100, 53);
			case 2:
				return new Point(160, 53);
			case 3:
				return new Point(130, 76);
			default:
				return null;
		}
		
	}

	@Override
	public Object getExitValue() {
		if (selectedHero != null)
			return new Object[] {selectedHero, selectingItemIndex};
		else
			return null;
	}
}
