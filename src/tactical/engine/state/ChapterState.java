package tactical.engine.state;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.game.hudmenu.Panel;
import tactical.game.input.KeyMapping;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class ChapterState extends BasicGameState {

	private Image chapterImage = null;
	private Trigger exitTrigger = null;
	private String header = null;
	private StateInfo stateInfo;
	private ArrayList<String> description;
	private Font headerFont;
	
	public ChapterState(PersistentStateInfo psi)
	{
		this.stateInfo = new StateInfo(psi, false, false);
	}
	
	public void setChapterInfo(String header, String description, Trigger exitTrigger) {
		stateInfo.initState();
		this.header = header;
		this.description = new ArrayList<>();
				
		int idx = 0;
		String[] splitLine = description.split(" ");
		String line = "";
		while (idx < splitLine.length) {
			if (Panel.SPEECH_FONT.getWidth(line + " " + splitLine[idx]) < 700) {
				line += " " + splitLine[idx];				
			}
			else { 				
				this.description.add(line);
				line = "";
				idx--;
			}
			idx++;			
		}
		
		if (line.length() > 0)
			this.description.add(line);
		
		this.exitTrigger = exitTrigger;
		chapterImage = stateInfo.getResourceManager().getImage("chapter");		
		headerFont = stateInfo.getResourceManager().getFontByName("chaptermenufont");
	}
	
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.clearClip();
		g.setColor(Color.black);
		g.fillRect(0, 0, container.getWidth(), container.getHeight());
		g.translate(((PaddedGameContainer) container).getDisplayPaddingX(), 0);
		g.scale(PaddedGameContainer.GAME_SCREEN_SCALE, PaddedGameContainer.GAME_SCREEN_SCALE);
		g.setClip(((PaddedGameContainer) container).getDisplayPaddingX(), 0, 
				((PaddedGameContainer) container).getPaddedWidth(), container.getHeight());
		g.drawImage(chapterImage, 0, 0);
		g.setFont(Panel.PANEL_FONT);
		g.setColor(Color.white);
		header = "Chapter One - Slugman Cometh";
		StringUtils.drawString(header, 50, 27, g);
		g.setFont(Panel.SPEECH_FONT);
				
		
		
		for (int i = 0; i < this.description.size(); i++) {
			StringUtils.drawString(this.description.get(i), 41, 105 + i * 15, g);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		if (stateInfo.getInput().isKeyDown(KeyMapping.BUTTON_1) || stateInfo.getInput().isKeyDown(KeyMapping.BUTTON_3)) {
			exitTrigger.perform(stateInfo);
			stateInfo.processMessages();
			
			stateInfo.getResourceManager().reinitialize();
			stateInfo.getInput().clear();
			stateInfo.setInitialized(false);
			
		}
	}
	
	
	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		stateInfo.getInput().clear();
		super.leave(container, game);
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return TacticalGame.STATE_GAME_CHAPTER;
	}

}
