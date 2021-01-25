package tactical.game.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import tactical.engine.state.StateInfo;
import tactical.game.input.UserInput;
import tactical.game.manager.SoundManager;
import tactical.game.ui.PaddedGameContainer;

public class PauseMenu extends Menu {

	private static final Color MENU_COLOR = new Color(0, 0, 0, 120);
	private UnicodeFont menuFont;
	private UnicodeFont subMenuFont;
	private StateInfo stateInfo;
	public PauseMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_PAUSE);
		subMenuFont = new UnicodeFont(PANEL_FONT.getFont().deriveFont(40f));
		subMenuFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));		
		subMenuFont.addAsciiGlyphs();
		subMenuFont.addGlyphs(400, 600);
		menuFont = new UnicodeFont(PANEL_FONT.getFont().deriveFont(80f));
		menuFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		menuFont.addAsciiGlyphs();
		menuFont.addGlyphs(400, 600);
		this.stateInfo = stateInfo;
		try {
			subMenuFont.loadGlyphs();
			menuFont.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		};
	}

	@Override
	public MenuUpdate update(long delta, StateInfo si) {
		if (stateInfo != null) {
			if (SoundManager.GLOBAL_VOLUME > 0 && stateInfo.getInput().isKeyDown(Input.KEY_LEFT)) {
				SoundManager.GLOBAL_VOLUME -= .01f;
			} else if (SoundManager.GLOBAL_VOLUME < 1 && stateInfo.getInput().isKeyDown(Input.KEY_RIGHT)) {
				SoundManager.GLOBAL_VOLUME += .01f;
			}
			if (stateInfo.getInput().isKeyDown(Input.KEY_ESCAPE))
				System.exit(0);
		}
		return super.update(delta, stateInfo);
	}



	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		
		return null;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		graphics.resetTransform();
		graphics.setColor(MENU_COLOR);
		graphics.fillRect(0, 0, gc.getWidth(), gc.getHeight());
		graphics.setColor(Color.white);
		graphics.setFont(menuFont);		
		
		graphics.drawString("Paused", (gc.getWidth() 
				- menuFont.getWidth("PAUSED")) / 2, gc.getHeight() / 3);
		
		if (stateInfo != null) {
			graphics.setFont(subMenuFont);
			int volume = (int) (SoundManager.GLOBAL_VOLUME * 100);
			graphics.drawString("- Volume " + volume +" -", (gc.getWidth() 
					- subMenuFont.getWidth("- Volume " + volume +" -")) / 2, gc.getHeight() / 3 + 70);
			graphics.drawString("Press Escape to Exit Game",
					(gc.getWidth() - subMenuFont.getWidth("Press Escape to Exit Game")) / 2,
					gc.getHeight() / 3 + 100);
		}
	}

}
