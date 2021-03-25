package tactical.engine.state;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.ResourceLoader;

import tactical.engine.TacticalGame;
import tactical.game.input.KeyMapping;
import tactical.game.manager.SoundManager;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.LoadingScreenRenderer;
import tactical.loading.LoadingState;
import tactical.loading.ResourceManager;
import tactical.utils.AnimationWrapper;
import tactical.utils.SpriteAnims;
import tactical.utils.StringUtils;

public class IntroState extends LoadableGameState {

	private Image tabletImage;
	private PersistentStateInfo psi;
	private UnicodeFont font;
	private ArrayList<String> textToDisplay;
	private float time = 400f;
	private AnimationWrapper anim;
	private int maxLength;
	private int exitTime;
	
	public IntroState(PersistentStateInfo psi) {
		this.psi = psi;		
	}
	
	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		
	}

	@Override
	public void initAfterLoad() {
		time = 240f + TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroTextEnterDelayTime();
		exitTime = (int) (time - TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroExitTime());
		SpriteAnims sa = psi.getResourceManager().getSpriteAnimation(
				TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroAnimationFileName());
		anim = new AnimationWrapper(sa);
		anim.setAnimation("1", false);
		
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
			
			String text = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroText().replaceAll("\n", " ");
					
			
			String[] textSplit = text.split(" ");
			String tempText = "";
			maxLength = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroTextMaxWidth();
			textToDisplay = new ArrayList<>();
			for (int i = 0; i < textSplit.length; i++) {
				if (textSplit[i].trim().equalsIgnoreCase("<linebreak>")) 
				{
					textToDisplay.add(tempText.trim());
					textToDisplay.add("");
					tempText = "";
				}
				else if (font.getWidth(tempText + " " + textSplit[i]) > maxLength) {
					textToDisplay.add(tempText.trim());
					tempText = textSplit[i];
				} else {
					tempText += " " + textSplit[i];
				}
			}
			textToDisplay.add(tempText.trim());
			
			//TODO Break this out into it's own menu renderer
			Music music = psi.getResourceManager().getMusicByName(TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroMusicName());
			music.loop(1f, SoundManager.GLOBAL_VOLUME);
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
		time -= delta / (float) TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getIntroScrollSpeed();		
		if (time <= exitTime|| container.getInput().isKeyDown(KeyMapping.BUTTON_1) || container.getInput().isKeyDown(KeyMapping.BUTTON_3)) {
			((LoadingState) game.getState(TacticalGame.STATE_GAME_LOADING)).setLoadingInfo(null, false, false,
					psi.getResourceManager(),
						(LoadableGameState) game.getState(TacticalGame.STATE_GAME_MENU),
							new LoadingScreenRenderer(container));

			game.enterState(TacticalGame.STATE_GAME_LOADING);
		}
		
		anim.update(delta);
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		anim.drawAnimationIgnoreOffset(0, 0, g);
		g.setFont(font);
	
		
		for (int i = 0; i < textToDisplay.size(); i++) {
			g.setColor(Color.black);
			StringUtils.drawString(textToDisplay.get(i), (800 - maxLength) / 2, i * 20 + time, g);			
			g.setColor(Color.white);
			StringUtils.drawString(textToDisplay.get(i), (800 - maxLength) / 2 - 1, i * 20 - 1 + time, g);
		}
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
