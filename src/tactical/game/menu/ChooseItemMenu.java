package tactical.game.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.item.Item;
import tactical.game.listener.MenuListener;

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
			g.setColor(Color.white);
			g.drawRect(207, yOffsetTop + 50 + selectingItemIndex * 20, 80, 20);
		}
	}

	@Override
	protected MenuUpdate onUp(StateInfo stateInfo) {
		if (selectingItemState)
		{
			if (selectingItemIndex > 0)
				selectingItemIndex--;
			else
				selectingItemIndex = selectedHero.getItemsSize() - 1;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else
			return super.onUp(stateInfo);
	}



	@Override
	protected MenuUpdate onDown(StateInfo stateInfo) {
		if (selectingItemState)
		{
			if (selectingItemIndex < selectedHero.getItemsSize() - 1)
				selectingItemIndex++;
			else
				selectingItemIndex = 0;
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
			itemSelected(stateInfo);
		}

		return MenuUpdate.MENU_ACTION_LONG;
	}
	
	protected abstract void itemSelected(StateInfo stateInfo);

	@Override
	protected void drawHeroSpecificsLeft(Graphics graphics) {
		if (selectedHero != null) {
			for (int i = 0; i < 4; i++) {
				int x = 0;
				int y = 0;
				switch (i) {
					case 0:
						x = 130;
						y = 30;
						break;
					case 1:
						x = 100;
						y = 53;
						break;
					case 2:
						x = 160;
						y = 53;
						break;
					case 3:
						x = 130;
						y = 76;
						break;
				}
				
				if (i < selectedHero.getItemsSize())
				{
					Item item = selectedHero.getItem(i);
					graphics.drawImage(item.getImage().getScaledCopy(1.3f), x, y);
				}
				else
				{
					graphics.drawImage(emptySpot.getScaledCopy(1.3f), x, y);
				}
			}
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
