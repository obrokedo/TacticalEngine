package tactical.engine.state;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.ResourceLoader;

import tactical.engine.TacticalGame;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;

public class IntroState extends LoadableGameState {

	private Image tabletImage;
	private PersistentStateInfo psi;
	private UnicodeFont font;
	
	public IntroState(PersistentStateInfo psi) {
		this.psi = psi;
	}
	
	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		
	}

	@Override
	public void initAfterLoad() {
		tabletImage = psi.getResourceManager().getImage("tablet");
		
		InputStream inputStream	= ResourceLoader.getResourceAsStream("font/LoVDialogueFont.ttf");
		try
		{
			Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			boolean italic = false;
			boolean bold = true;
			
			UnicodeFont ufont = new UnicodeFont(awtFont, 40, bold, italic);
			ufont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
			ufont.addAsciiGlyphs();
			ufont.addGlyphs(400, 600);
			ufont.loadGlyphs();
			font = ufont;
		} catch (FontFormatException e) {
			e.printStackTrace();			
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		g.drawImage(tabletImage, 0, 0);			
		g.setFont(font);
		
		g.setColor(Color.black);
		StringUtils.drawString("and once he was satisfied of this, departed this world to follow his brethren to war once again.", 41, 121, g);
		
		g.setColor(Color.white);
		StringUtils.drawString("and once he was satisfied of this, departed this world to follow his brethren to war once again.", 40, 120, g);
		
	}

	@Override
	protected Menu getPauseMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exceptionInState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return TacticalGame.STATE_GAME_INTRO;
	}
	
}
