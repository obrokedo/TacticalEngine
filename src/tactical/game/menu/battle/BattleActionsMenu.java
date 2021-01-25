package tactical.game.menu.battle;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.Range;
import tactical.game.constants.Direction;
import tactical.game.menu.QuadMenu;
import tactical.game.move.AttackableSpace;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.Sprite;


public class BattleActionsMenu extends QuadMenu
{
	protected Sprite searchSprite = null;
	
	public BattleActionsMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_BATTLE, stateInfo);

		icons = new Image[8];

		enabled = new boolean[4];
		enabled[3] = true;
	}

	@Override
	public void initialize() {
		for (int i = 0; i < icons.length; i++)
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(i % 4, i / 4);

		text = new String[] {"Attack", "Magic", "Items", "Stay"};
		
		this.searchSprite = null;
		this.selected = Direction.DOWN;
		CombatSprite currentSprite = stateInfo.getCurrentSprite();

		if (currentSprite.getSpellsDescriptors() != null && stateInfo.getCurrentSprite().getSpellsDescriptors().size() > 0)
			enabled[1] = true;
		else
			enabled[1] = false;

		if (currentSprite.getItemsSize() > 0)
			enabled[2] = true;
		else
			enabled[2] = false;

		/**************************************************/
		/* Determine if there are enemies in attack range */
		/**************************************************/
		Range declaredRange = currentSprite.getAttackRange();
		int range[][] = AttackableSpace.getAttackableArea(declaredRange);

		int rangeOffset = (range.length - 1) / 2;

		enabled[0] = false;

		OUTER: for (int i = 0; i < range.length; i++)
		{
			for (int j = 0; j < range[0].length; j++)
			{
				if (range[i][j] == 1)
				{
					CombatSprite targetable = stateInfo.getCombatSpriteAtTile(currentSprite.getTileX() - rangeOffset + i,
							currentSprite.getTileY() - rangeOffset + j, false);
					if (targetable != null)
					{
						enabled[0] = true;
						this.selected = Direction.UP;
						break OUTER;
					}
				}
			}
		}
		
		// If there is nothing to attack then check search areas
		if (!enabled[0]) {
			range = AttackableSpace.getAttackableArea(Range.ONE_ONLY);
			rangeOffset = (range.length - 1) / 2;
			
			OUTER: for (int i = 0; i < range.length; i++)
			{
				for (int j = 0; j < range[0].length; j++)
				{
					if (range[i][j] == 1)
					{
						Sprite targetable = stateInfo.getSearchableAtTile(currentSprite.getTileX() - rangeOffset + i,
								currentSprite.getTileY() - rangeOffset + j);
						if (targetable != null)
						{
							enabled[0] = true;
							this.selected = Direction.UP;
							searchSprite = targetable;
							text[0] = "Search";
							icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 0);
							icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(4, 1);
							break OUTER;
						}
					}
				}
			}
		}
	}

	@Override
	public MenuUpdate onBack() {
		stateInfo.sendMessage(MessageType.SHOW_MOVEABLE);
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public MenuUpdate onConfirm() {
		switch (selected)
		{
			case UP:
				if (searchSprite == null)
					stateInfo.sendMessage(MessageType.ATTACK_PRESSED);
				else
					stateInfo.sendMessage(MessageType.SEARCH_IN_BATTLE);
				break;
			case LEFT:
				stateInfo.sendMessage(MessageType.SHOW_SPELLMENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case RIGHT:
				stateInfo.sendMessage(MessageType.SHOW_ITEM_OPTION_MENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				break;
			case DOWN:
				stateInfo.sendMessage(MessageType.PLAYER_END_TURN);
				break;
		}

		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
}
