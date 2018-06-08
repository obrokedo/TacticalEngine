package tactical.game.combat;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import tactical.game.ui.PaddedGameContainer;

public class TransBGCombatAnimation extends CombatAnimation
{
	private Image backgroundImage;
	private int bgXLoc, bgYLoc;
	private int endLocX;
	private int offsetLocX;
	private CombatAnimation childAnimation;
	private boolean transIn;

	public TransBGCombatAnimation(Image backgroundImage, int bgXLoc,
			int bgYLoc, int screenWidth, CombatAnimation childAnimation,
			boolean transIn, boolean isHero) {
		super();
		this.backgroundImage = backgroundImage;
		this.bgXLoc = bgXLoc;
		this.bgYLoc = bgYLoc;
		this.childAnimation = childAnimation;
		this.minimumTimePassed = 250;
		this.transIn = transIn;

		if (isHero)
			endLocX = screenWidth;
		else
			endLocX = -screenWidth;

		update(0);
	}

	@Override
	public void initialize() {
		update(0);
	}

	@Override
	public boolean update(int delta) {
		this.totalTimePassed += delta;

		if (transIn) {
			if (endLocX > 0)
				offsetLocX = Math.max(0, Math.min(endLocX, (int) (1.0f * endLocX * (minimumTimePassed - totalTimePassed) / minimumTimePassed)));
			else
				offsetLocX = Math.min(0, Math.max(endLocX, (int) (1.0f * endLocX * (minimumTimePassed - totalTimePassed) / minimumTimePassed)));
		}
		else {
			if (endLocX > 0)
				offsetLocX =Math.max(0, Math.min(endLocX, (int) (1.0f * endLocX * totalTimePassed / minimumTimePassed)));
			else
				offsetLocX = Math.min(0, Math.max(endLocX, (int) (1.0f * endLocX * totalTimePassed / minimumTimePassed)));
		}

		if (childAnimation != null)
			childAnimation.xOffset = offsetLocX;

		return totalTimePassed >= minimumTimePassed;
	}

	@Override
	public void render(PaddedGameContainer fcCont, Graphics g, int yDrawPos, float scale) {
		g.setColor(Color.black);
		g.fillRect(0, 0, PaddedGameContainer.GAME_SCREEN_SIZE.width, PaddedGameContainer.GAME_SCREEN_SIZE.height);

		g.drawImage(backgroundImage, bgXLoc + offsetLocX, bgYLoc);

		if (childAnimation != null)
		{
			if (childAnimation.getParentSprite().getCurrentHP() > 0)
				childAnimation.render(fcCont, g, yDrawPos, scale);
		}

		g.setColor(Color.black);
		g.fillRect(0, 0, 0, fcCont.getHeight());
		g.fillRect(backgroundImage.getWidth(), 0, 0, fcCont.getHeight());
	}


}
