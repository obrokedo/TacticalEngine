package tactical.game.menu;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.Timer;
import tactical.game.constants.Direction;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public abstract class QuadMenu extends Menu
{
	protected StateInfo stateInfo;
	protected Direction selected = Direction.UP;
	protected static final Color disabledColor = new Color(111, 111, 111);
	protected int blinkDelta = 0;
	protected boolean blink = false;

	protected Image[] icons;
	protected String[] text;
	protected boolean[] enabled;
	protected boolean paintSelectionCursor;
	protected boolean largeFlor = true;
	protected Image flourish;
	protected Image selectSide;
	protected Image selectTop;
	protected int selectCount = 3;
	protected int flashCount = -5;
	protected Color flashColor = new Color(100, 100, 100);
	protected Timer timer;
	protected Portrait portrait;
	protected boolean visible = true;

	protected QuadMenu(PanelType menuType, StateInfo stateInfo) {
		this(menuType, null, true, stateInfo);
	}
	
	protected QuadMenu(PanelType menuType, Portrait portrait, StateInfo stateInfo) {
		this(menuType, portrait, true, stateInfo);
	}

	/**
	 * Creates a QuadMenu
	 *
	 * @param menuType the menu-type of this menu (used as an ID)
	 * @param largeFlor a boolean indicating whether the large-flourish should be used. This is determined
	 * 					by the width of the items that will be in the menu. For "narrow" images this should
	 * 					by set to false, for "wide" items this should be true
	 * @param stateInfo the relavant stateinfo that this menu will resuide in
	 */
	protected QuadMenu(PanelType menuType, Portrait portrait, boolean largeFlor, StateInfo stateInfo) {
		super(menuType);
		this.portrait = portrait;
		if (portrait != null)
			this.portrait.setTalking(false);
		this.largeFlor = largeFlor;
		if (largeFlor)
			this.flourish = stateInfo.getResourceManager().getImage("largeflor");
		else
			this.flourish = stateInfo.getResourceManager().getImage("smallflor");
		this.selectSide = stateInfo.getResourceManager().getImage("selectside");
		this.selectTop = stateInfo.getResourceManager().getImage("selecttop");
		this.stateInfo = stateInfo;
		this.paintSelectionCursor = false;
		this.timer = new Timer(16);
	}

	public abstract void initialize();

	protected Image getIconImage(Direction dir, boolean blink) {
		switch (dir)
		{
			case UP:
				return icons[0 + (blink ? 4 : 0)];
			case LEFT:
				return icons[1 + (blink ? 4 : 0)];
			case RIGHT:
				return icons[2 + (blink ? 4 : 0)];
			case DOWN:
				return icons[3 + (blink ? 4 : 0)];
		}
		return null;
	}

	protected boolean isOptionEnabled(Direction dir) {
		switch (dir)
		{
			case UP:
				return enabled[0];
			case LEFT:
				return enabled[1];
			case RIGHT:
				return enabled[2];
			case DOWN:
				return enabled[3];
		}
		return true;
	}

	protected String getText(Direction dir) {
		switch (dir)
		{
			case UP:
				return text[0];
			case LEFT:
				return text[1];
			case RIGHT:
				return text[2];
			case DOWN:
				return text[3];
		}
		return null;
	}

	protected int getSelectedInt()
	{
		switch (selected)
		{
			case UP:
				return 0;
			case LEFT:
				return 1;
			case RIGHT:
				return 2;
			case DOWN:
				return 3;
		}
		return 0;
	}
	
	protected void setSelectedInt(int dir)
	{
		switch (dir)
		{
			case 0:
				selected = Direction.UP;
				return;
			case 1:
				selected = Direction.LEFT;
				return;
			case 2:
				selected = Direction.RIGHT;
				return;
			case 3:
				selected = Direction.DOWN;
				return;
		}
	}

	protected void renderTextBox(PaddedGameContainer gc, Graphics graphics)
	{
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(205,
				195,
				getTextboxWidth(),
				23, graphics, null);

		graphics.setColor(COLOR_FOREFRONT);

		StringUtils.drawString(getText(selected), 212, 193, graphics);
	}

	@Override
	public final void render(PaddedGameContainer gc, Graphics graphics)
	{
		if (!visible)
			return;
		
		renderTextBox(gc, graphics);

		int iconWidth = getIconImage(Direction.UP, false).getWidth();
		int iconHeight = getIconImage(Direction.UP, false).getHeight();

		int x = (PaddedGameContainer.GAME_SCREEN_SIZE.width - iconWidth) / 2;
		int y = PaddedGameContainer.GAME_SCREEN_SIZE.height - iconHeight * 2 - 25;

		if (isOptionEnabled(Direction.UP))
		{
			if (selected == Direction.UP)
				graphics.drawImage(selectTop, (PaddedGameContainer.GAME_SCREEN_SIZE.width - selectTop.getWidth()) / 2, y - selectTop.getHeight() - selectCount);
			drawImage(getIconImage(Direction.UP, (selected == Direction.UP ? blink : false)), x, y - (selected == Direction.UP ? selectCount : 0), graphics, Direction.UP);
		}
		else
			graphics.drawImage(getIconImage(Direction.UP, false), x, y, disabledColor);

		if (isOptionEnabled(Direction.LEFT))
		{
			if (selected == Direction.LEFT)
				graphics.drawImage(selectSide, x - iconWidth - selectSide.getWidth() + 3 - selectCount, (float) (y + iconHeight - .5 * selectSide.getHeight()));
			drawImage(getIconImage(Direction.LEFT, (selected == Direction.LEFT ? blink : false)), x - iconWidth - (selected == Direction.LEFT ? selectCount : 0), (float) (y + iconHeight * .5), graphics, Direction.LEFT);
		}
		else
			graphics.drawImage(getIconImage(Direction.LEFT, false), x - iconWidth, (float) (y + iconHeight * .5), disabledColor);


		if (isOptionEnabled(Direction.RIGHT))
		{
			if (selected == Direction.RIGHT)
				graphics.drawImage(selectSide.getFlippedCopy(true, false), x + iconWidth * 2 - 3 + selectCount, (float) (y + iconHeight - .5 * selectSide.getHeight()));
			drawImage(getIconImage(Direction.RIGHT, (selected == Direction.RIGHT ? blink : false)), x + iconWidth + (selected == Direction.RIGHT ? selectCount : 0), (float) (y + iconHeight * .5), graphics, Direction.RIGHT);
		}
		else
			graphics.drawImage(getIconImage(Direction.RIGHT, false), x + iconWidth, (float) (y + iconHeight * .5), disabledColor);
		graphics.drawImage(flourish.getFlippedCopy(false, true), x - flourish.getWidth(), y + iconHeight + flourish.getHeight());

		if (isOptionEnabled(Direction.DOWN))
		{
			if (selected == Direction.DOWN)
				graphics.drawImage(selectTop.getFlippedCopy(false, true), (PaddedGameContainer.GAME_SCREEN_SIZE.width - selectTop.getWidth()) / 2, y + iconHeight * 2 + selectCount);
			drawImage(getIconImage(Direction.DOWN, (selected == Direction.DOWN ? blink : false)), x, y + iconHeight + (selected == Direction.DOWN ? selectCount : 0), graphics, Direction.DOWN);
		}
		else
			graphics.drawImage(getIconImage(Direction.DOWN, false), x, y + iconHeight, disabledColor);

		graphics.drawImage(flourish, x - flourish.getWidth(), (float) (y + iconHeight * .5) - flourish.getHeight());
		graphics.drawImage(flourish.getFlippedCopy(true, false), x + iconWidth, (float) (y + iconHeight * .5) - flourish.getHeight());
		graphics.drawImage(flourish.getFlippedCopy(false, true), x - flourish.getWidth(), y + iconHeight + flourish.getHeight());
		graphics.drawImage(flourish.getFlippedCopy(true, true), x + iconWidth, y + iconHeight + flourish.getHeight());

		if (portrait != null) {
			portrait.render(15, 12, graphics);
		}
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		super.update(delta, stateInfo);
		timer.update(delta);

		while (timer.perform())
		{
			blinkDelta++;
			flashCount += 1;

			if (flashCount >= 50)
				flashCount = -50;

			flashColor.r = (50 - Math.abs(flashCount)) / 255.0f;
			flashColor.g = (50 - Math.abs(flashCount)) / 255.0f;
			flashColor.b = (50 - Math.abs(flashCount)) / 255.0f;

			if (selectCount < 3)
				selectCount++;


			if (blinkDelta == 20)
			{
				if (!paintSelectionCursor)
					blink = !blink;
				blinkDelta = 0;
			}
		}
		
		if (portrait != null) {
			this.portrait.setTalking(false);
			portrait.update(delta);
		}

		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo)
	{
		if (stateInfo.getCurrentSprite().getClientId() != stateInfo.getPersistentStateInfo().getClientId())
			return MenuUpdate.MENU_NO_ACTION;

		if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			return onBack();
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_3) || input.isKeyDown(KeyMapping.BUTTON_1))
		{
			return onConfirm();
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_UP))
		{
			return onUp();
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_DOWN))
		{
			return onDown();
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_LEFT))
		{
			return onLeft();
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_RIGHT))
		{
			return onRight();
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	protected MenuUpdate onUp()
	{
		return onDirection(Direction.UP);
	}

	protected MenuUpdate onDown()
	{
		return onDirection(Direction.DOWN);
	}

	protected MenuUpdate onLeft()
	{
		return onDirection(Direction.LEFT);
	}

	protected MenuUpdate onRight()
	{
		return onDirection(Direction.RIGHT);
	}

	protected abstract MenuUpdate onBack();

	protected abstract MenuUpdate onConfirm();

	protected MenuUpdate onDirection(Direction dir)
	{
		if (selected != dir && isOptionEnabled(dir))
		{
			selectCount = 2;
			flashCount = -5;
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
			selected = dir;
			if (!paintSelectionCursor)
				blink = true;
			blinkDelta = 0;
		}
		return MenuUpdate.MENU_ACTION_SHORT;
	}

	protected int getTextboxWidth()
	{
		return 57;
	}

	protected void drawImage(Image image, float x, float y, Graphics g, Direction dir)
	{
		if (paintSelectionCursor && dir == selected)
		{
			Image whiteIm = image;

			// 1. bind the sprite sheet
			whiteIm.bind();

			// 2. change texture environment
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
					GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);

			// 3. start rendering the sprite sheet
			whiteIm.startUse();

			// 4. bind any colors, draw any sprites
			flashColor.bind();
			whiteIm.drawEmbedded(x, y, image.getWidth(), image.getHeight());

			// 5. stop rendering the sprite sheet
			whiteIm.endUse();

			// 6. reset the texture environment
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
					GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);


			/*
			g.setColor(Color.red);
			g.drawRect(x + 1, y + 1, image.getWidth() - 2, image.getHeight() - 2);
			g.drawRect(x + 2, y + 2, image.getWidth() - 4, image.getHeight() - 4);
			*/
		}
		else
			g.drawImage(image, x, y);

	}

	@Override
	public boolean displayWhenNotTop() {
		return false;
	}
}
