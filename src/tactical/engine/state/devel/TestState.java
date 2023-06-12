package tactical.engine.state.devel;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;
import tactical.utils.AnimSprite;
import tactical.utils.Animation;
import tactical.utils.SpriteAnims;

public class TestState extends LoadableGameState
{
	private ResourceManager frm;
	private Image backgroundImage = null;
	private PaddedGameContainer gc;
	private int bgXPos;
	private int bgYPos;
	private int rangedAttackCounter = 0;
	private Color trans = new Color(255, 255, 255, 62);
	private int rangedDelta = 0;
	private Animation targetAnim;
	private SpriteAnims sas;

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g)
	{
		g.setColor(Color.black);
		g.fillRect(0, 0, gc.getPaddedWidth(), gc.getHeight());
		if (backgroundImage != null)
		{
			// Assume that the target is a hero and determine locations for it
			int xLoc = 0;
			int yLoc = bgYPos + backgroundImage.getHeight();

			if (rangedAttackCounter < 22)
			{
				backgroundImage.draw(bgXPos, bgYPos, .5f);
				for (AnimSprite as : targetAnim.frames.get(0).sprites)
					targetAnim.getImageAtIndex(as.imageIndex).draw(xLoc + (as.x) * .5f, yLoc + (as.y) * 1.5f, .5f);
				for (int i = Math.max(0, rangedAttackCounter - 11); i < rangedAttackCounter; i++)
				{
					if (i < 11)
						backgroundImage.draw(bgXPos, bgYPos, Math.min(.5f + .05f * (rangedAttackCounter - i), 1), trans);
					else
						backgroundImage.draw(bgXPos, bgYPos, 1, trans);
				}

				for (int i = Math.max(0, rangedAttackCounter - 11); i < rangedAttackCounter; i++)
				{

					for (AnimSprite as : targetAnim.frames.get(0).sprites)
						targetAnim.getImageAtIndex(as.imageIndex).draw(
								xLoc + (as.x)  * Math.min(.5f + .05f * i, 1),
								yLoc + (as.y) * Math.max(1.5f - .05f * i, 1),
								Math.min(.5f + .05f * i, 1), trans);
				}
			}
			else if (rangedAttackCounter < 100)
			{
				backgroundImage.draw(bgXPos, bgYPos);
				for (AnimSprite as : targetAnim.frames.get(0).sprites)
					targetAnim.getImageAtIndex(as.imageIndex).draw(xLoc + (as.x), yLoc + (as.y));
			}
			else if (rangedAttackCounter < 200)
			{
				backgroundImage.draw(bgXPos, bgYPos, .5f);
				for (AnimSprite as : targetAnim.frames.get(0).sprites)
					targetAnim.getImageAtIndex(as.imageIndex).draw(xLoc + (as.x) * .5f, yLoc + (as.y) * 1.5f, .5f);
			}
		}
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		rangedDelta += delta;
		while (rangedDelta > 30)
		{
			rangedDelta -= 30;
			if (rangedAttackCounter < 11)
				rangedAttackCounter = (rangedAttackCounter + 1) % 200;
			else
				rangedAttackCounter = (rangedAttackCounter + 2) % 200;
		}
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		frm = resourceManager;
		Image bgIm = frm.getSpriteSheet("battlebg").getSprite(0,  0);

		backgroundImage = bgIm.getScaledCopy(gc.getPaddedWidth() / (float) bgIm.getWidth());

		bgXPos = 0;
		bgYPos = (gc.getHeight() - backgroundImage.getHeight()) / 2;

		sas = frm.getSpriteAnimation("Darkling Ooze");
		targetAnim = sas.getAnimation("UnStand");
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_TEST;
	}

	@Override
	public void initAfterLoad() {

	}

	@Override
	protected Menu getPauseMenu() {
		
		return null;
	}

	@Override
	public void exceptionInState() {
		
		
	}

}
