package tactical.game.move;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.Range;
import tactical.game.constants.Direction;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.hudmenu.SpriteContextPanel;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.KeyboardListener;
import tactical.game.listener.MouseListener;
import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;

public class AttackableSpace implements KeyboardListener, MouseListener
{
	private boolean targetsHero = false;
	private CombatSprite currentSprite;
	private int[][] range;
	private int[][] area;
	private int rangeOffset;
	private int areaOffset;
	private float targetSelectX, targetSelectY;
	private float selectX, selectY;
	private int selectedTarget = 0;
	private int spriteTileX, spriteTileY;
	private int tileWidth, tileHeight;
	private ArrayList<CombatSprite> targetsInRange = new ArrayList<CombatSprite>();
	private Image cursorImage;
	private final Color ATTACKABLE_COLOR = new Color(200, 200, 200, 60);
	private boolean targetsAll = false;
	private boolean canTargetSelf = true;
	
	private boolean showHealthBar = false;

	public static final int[][] AREA_0 = {{1}};

	public static final int[][] AREA_1 = {{-1, 1, -1},
											{4, 5, 2},
											{-1, 3, -1}};

	public static final int[][] AREA_2 = {	{-1,  -1, 5, -1, -1},
											{-1,  12, 1,  6, -1},
											{ 11,  4, 13,  2,  7},
											{-1,  10, 3,  8, -1},
											{-1,  -1, 9, -1, -1}};

	public static final int[][] AREA_2_NO_1 = {   {-1, -1, 1, -1, -1},
												{-1,  8, -1,  2, -1},
												{ 7,  -1, 9,  -1,  3},
												{-1,  6, -1,  4, -1},
												{-1, -1, 5, -1, -1}};

	public static final int[][] AREA_3 = {	{-1,  -1, - 1, 13, -1,  -1, -1},
											{-1,  -1,  24, 5,  14,  -1, -1},
											{-1,  23,  12, 1,   6,  15, -1},
											{ 22, 11,   4, 25,   2,   7,  16},
											{-1,  21,  10, 3,   8,  17, -1},
											{-1,  -1,  20, 9,  18,  -1, -1},
											{-1,  -1,  -1, 19, -1,  -1, -1}};

	public static final int[][] AREA_3_NO_1 =
										{	{-1,  -1,  -1,   9,  -1,  -1, -1},
											{-1,  -1,  20,   1,  10,  -1, -1},
											{-1,  19,   8,  -1,   2,  11, -1},
											{ 18,  7,  -1,  21,  -1,   3,  12},
											{-1,  17,   6,  -1,   4,  13, -1},
											{-1,  -1,  16,   5,  14,  -1, -1},
											{-1,  -1,  -1,  15,  -1,  -1, -1}};
	public static final int[][] AREA_3_NO_1_2 =
										{	{-1, -1, -1,  1, -1, -1, -1},
											{-1, -1,  12, -1,  2, -1, -1},
											{-1,  11, -1, -1, -1,  3, -1},
											{ 10, -1, -1,  13, -1, -1,  4},
											{-1,  9, -1, -1, -1,  5, -1},
											{-1, -1,  8, -1,  6, -1, -1},
											{-1, -1, -1,  7, -1, -1, -1}};

	public static final int[][] AREA_ALL = {{}};

	public static final int AREA_ALL_INDICATOR = 0;

	public AttackableSpace(StateInfo stateInfo, CombatSprite currentSprite,
			int[][] range, int[][] area)
	{
		this(stateInfo, currentSprite, true, range, area, true);
		this.showHealthBar = false;
		this.setTargetSprite(currentSprite, stateInfo);
		this.targetsInRange.clear();
		this.targetsInRange.add(currentSprite);
		targetSelectX = -1;
		targetSelectY = -1;
	}

	public AttackableSpace(StateInfo stateInfo, CombatSprite currentSprite, boolean targetsHero,
			int[][] range, int[][] area, boolean canTargetSelf)
	{
		this.currentSprite = currentSprite;
		this.range = range;
		this.area = area;
		this.targetsHero = targetsHero;
		this.tileWidth = stateInfo.getTileWidth();
		this.tileHeight = stateInfo.getTileHeight();
		this.canTargetSelf = canTargetSelf;
		this.showHealthBar = true;
		spriteTileX = currentSprite.getTileX();
		spriteTileY = currentSprite.getTileY();
		Log.debug("Finding attackables for " + currentSprite.getName());
		if (area == AttackableSpace.AREA_ALL) {
			targetsAll = true;
			this.area = AttackableSpace.AREA_0;
			this.range = AttackableSpace.AREA_0;
		}

		this.areaOffset = (this.area.length - 1) / 2;
		this.rangeOffset = (this.range.length - 1) / 2;
		
		ORDER: for (int order = 1; order < Integer.MAX_VALUE; order++) {
			for (int i = 0; i < this.range.length; i++)
			{
				for (int j = 0; j < this.range[0].length; j++)
				{
					if (this.range[i][j] == order)
					{
						Log.debug("\tChecking space for targetables " + (currentSprite.getTileX() - rangeOffset + i) + ", " +
								(currentSprite.getTileY() - rangeOffset + j));
						CombatSprite targetable = stateInfo.getCombatSpriteAtTile(currentSprite.getTileX() - rangeOffset + i,
								currentSprite.getTileY() - rangeOffset + j, targetsHero);
						if (targetable != null && (targetable != currentSprite || canTargetSelf))
						{
							targetsInRange.add(targetable);
	
							Log.debug("\tAttackable Space: Add Targetable " + targetable.getName());
						}
						continue ORDER;
					}
				}
			}
			break;
		}

		selectX = currentSprite.getLocX();
		selectY = currentSprite.getLocY();

		if (targetsInRange.size() > 0)
		{
			targetSelectX = targetsInRange.get(0).getLocX();
			targetSelectY = targetsInRange.get(0).getLocY();
			this.setTargetSprite(targetsInRange.get(0), stateInfo);
			Log.debug("Default target " + targetsInRange.get(0).getName());
		}
		else
		{
			Log.debug("\tNo targets found in range");
		}

		if (currentSprite.isHero())
			stateInfo.registerMouseListener(this);

		cursorImage = stateInfo.getResourceManager().getImage("battlecursor");
	}

	/**
	 * This displays the white "targeting" rectangle during a characters attack phase
	 */
	public void render(PaddedGameContainer gc, Camera camera, Graphics graphics)
	{
		graphics.setColor(ATTACKABLE_COLOR);
		for (int i = 0; i < range.length; i++)
		{
			for (int j = 0; j < range[0].length; j++)
			{
				if (range[j][i] != -1 && (i != rangeOffset || 
						j != rangeOffset || (targetsHero == currentSprite.isHero() && canTargetSelf)))
				{
					graphics.fillRect((spriteTileX - rangeOffset + i) * tileWidth - camera.getLocationX(),
							(spriteTileY - rangeOffset + j) * tileHeight - camera.getLocationY(), tileWidth, tileHeight);
				}
			}
		}

		if (targetSelectX != -1)
		{
			graphics.setColor(Color.white);

			for (int i = 0; i < area.length; i++)
			{
				for (int j = 0; j < area[0].length; j++)
				{
					if (area[i][j] == 1)
						cursorImage.draw(selectX + (tileWidth * (i - areaOffset)) - camera.getLocationX(),
								selectY + (tileHeight * (j - areaOffset)) - camera.getLocationY());
				}
			}
		}
	}

	@Override
	public boolean mouseUpdate(int frameMX, int frameMY, int mapMX, int mapMY,
			boolean leftClicked, boolean rightClicked, StateInfo stateInfo)
	{
		return false;
	}

	public void setTargetSprite(CombatSprite targetSprite, StateInfo stateInfo)
	{
		targetSelectX = targetSprite.getLocX();
		targetSelectY = targetSprite.getLocY();

		if (targetSelectX > currentSprite.getLocX())
			currentSprite.setFacing(Direction.RIGHT);
		else if (targetSelectX < currentSprite.getLocX())
			currentSprite.setFacing(Direction.LEFT);
		else if (targetSelectY > currentSprite.getLocY())
			currentSprite.setFacing(Direction.DOWN);
		else if (targetSelectY < currentSprite.getLocY())
			currentSprite.setFacing(Direction.UP);

		stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
		if (showHealthBar) {
			stateInfo.addPanel(new SpriteContextPanel(PanelType.PANEL_ENEMY_HEALTH_BAR, targetsInRange.get(selectedTarget), 
				TacticalGame.ENGINE_CONFIGURATIOR.getHealthPanelRenderer(), 
				stateInfo.getResourceManager(), stateInfo.getPaddedGameContainer()));

			// Including this in here is a bit of kludge, but because this method is going to be called
			// twice when creating an AttackableSpace for showing Spell range, this prevents the sound effect from
			// playing twice as well
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
		}
	}

	@Override
	public int getZOrder() {
		return MouseListener.ORDER_ATTACKABLE_SPACE;
	}

	public void update(StateInfo stateInfo)
	{
		if (selectX > targetSelectX)
			selectX -= stateInfo.getTileWidth() / 4;
		else if (selectX < targetSelectX)
			selectX += stateInfo.getTileWidth() / 4;

		if (selectY > targetSelectY)
			selectY -= stateInfo.getTileHeight() / 4;
		else if (selectY < targetSelectY)
			selectY += stateInfo.getTileHeight() / 4;
	}

	@Override
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo)
	{
		update(stateInfo);

		if (input.isKeyDown(KeyMapping.BUTTON_1))
		{

		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			// Remove yourself as the active keyboard listener
			stateInfo.removeKeyboardListener();
			stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
			stateInfo.sendMessage(MessageType.HIDE_ATTACK_AREA);
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
			return true;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_3))
		{
			if (targetsInRange.size() == 0)
				return false;

			stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));

			ArrayList<AnimatedSprite> sprites = new ArrayList<>();

			if (!targetsAll) {
				for (int i = 0; i < area.length; i++)
				{
					for (int j = 0; j < area[0].length; j++)
					{
						if (area[i][j] == 1)
						{
							CombatSprite cs = stateInfo.getCombatSpriteAtTile(targetsInRange.get(selectedTarget).getTileX() + i - areaOffset,
									targetsInRange.get(selectedTarget).getTileY() + j - areaOffset, targetsHero);
							if (cs != null)
								sprites.add(cs);
						}
					}
				}
			} else {
				for (CombatSprite cs : stateInfo.getCombatSprites()) {
					if (cs.isHero() == targetsHero && cs.getCurrentHP() > 0)
						sprites.add(cs);
				}
			}

			if (sprites.size() > 0)
			{
				// If the current sprite is a target, move it to the end of the list
				if (sprites.remove(currentSprite))
				{
					sprites.add(currentSprite);
				}
				stateInfo.sendMessage(new SpriteContextMessage(MessageType.TARGET_SPRITE, sprites));

				Log.debug("Target Amount -> " + sprites.size());

				return true;
			}
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_UP) || input.isKeyDown(KeyMapping.BUTTON_LEFT))
		{
			if (targetsInRange.size() <= 1 ||
					selectX != targetSelectX || selectY != targetSelectY)
				return false;

			selectedTarget = (selectedTarget + 1) % targetsInRange.size();
			stateInfo.sendMessage(new SpriteContextMessage(MessageType.SET_SELECTED_SPRITE, targetsInRange.get(selectedTarget)));
			// setTargetSprite(targetsInRange.get(selectedTarget), stateInfo);
			return true;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_DOWN) || input.isKeyDown(KeyMapping.BUTTON_RIGHT))
		{
			if (targetsInRange.size() <= 1 ||
					selectX != targetSelectX || selectY != targetSelectY)
				return false;

			if (selectedTarget > 0)
				selectedTarget--;
			else
				selectedTarget = targetsInRange.size() - 1;
			stateInfo.sendMessage(new SpriteContextMessage(MessageType.SET_SELECTED_SPRITE, targetsInRange.get(selectedTarget)));
			// setTargetSprite(targetsInRange.get(selectedTarget), stateInfo);
			return true;
		}
		return false;
	}

	public int getTargetAmount()
	{
		return targetsInRange.size();
	}

	public static int[][] getAttackableArea(Range range)
	{
		int area[][] = null;

		switch (range)
		{
			case ONE_ONLY:
				area = AttackableSpace.AREA_1;
				break;
			case TWO_AND_LESS:
				area = AttackableSpace.AREA_2;
				break;
			case THREE_AND_LESS:
				area = AttackableSpace.AREA_3;
				break;
			case TWO_NO_ONE:
				area = AttackableSpace.AREA_2_NO_1;
				break;
			case THREE_NO_ONE_OR_TWO:
				area = AttackableSpace.AREA_3_NO_1_2;
			case SELF_ONLY:
				break;
			case THREE_NO_ONE:
				area = AttackableSpace.AREA_3_NO_1;
				break;
			default:
				break;
		}

		return area;
	}
}
