package tactical.game.menu;

import org.newdawn.slick.Image;

import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.Range;
import tactical.game.constants.Direction;
import tactical.game.move.AttackableSpace;
import tactical.game.sprite.CombatSprite;


public class BattleActionsMenu extends QuadMenu
{
	public BattleActionsMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_BATTLE, stateInfo);

		icons = new Image[8];

		for (int i = 0; i < icons.length; i++)
			icons[i] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(i % 4, i / 4);

		text = new String[] {"Attack", "Magic", "Items", "Stay"};
		enabled = new boolean[4];
		enabled[3] = true;
	}

	@Override
	public void initialize() {
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
				stateInfo.sendMessage(MessageType.ATTACK_PRESSED);
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
