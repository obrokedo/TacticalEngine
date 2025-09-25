package tactical.engine.state;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;

public class CreditsState extends LoadableGameState{
	private float timeElapsed = 0;
	private EngineConfigurationValues engineConfig;

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {		
		super.enter(container, game);
		timeElapsed = -31000;
		engineConfig = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
	}



	@Override
	public void stateLoaded(ResourceManager resourceManager) {	
	}

	@Override
	public void initAfterLoad() {
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException {
		timeElapsed += delta;
		if (container.getInput().isKeyDown(Input.KEY_ENTER)) {
			System.exit(0);
		}
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		drawCredits(container, g);		
	}

	@Override
	protected Menu getPauseMenu() {
		
		return null;
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_CREDITS;
	}
	
	
	private void drawCredits(GameContainer gc, Graphics graphics) {	
		Color accentColor = new Color(66, 134, 244);
		engineConfig.renderCredits(gc, graphics, timeElapsed, Color.white, accentColor);
		
		// MOVE OVER MUSICAL CONTRIBUTIONS
		/*
		timeElapsed = 0;
		
		graphics.resetTransform();        
        graphics.translate(gc.getWidth() / 2 - 240, timeElapsed / -50);
        
		int left = -70;
		int right = 220;
		int fromTop = 100;
		int spacing = 20;
		int spacingAfterHorizontalRule = 5;
		int spacingBetweenSections = 60;
		
		graphics.setColor(accentColor);
		graphics.drawString("Credits", -90, fromTop);
		fromTop = fromTop + spacing;
		// Project Leads
		graphics.drawString("Project Leads", -70, fromTop);
		fromTop = fromTop + spacing;
		graphics.setColor(Color.white);
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		
		graphics.drawString("Keegan McCarthy 'MXC'", left, fromTop);		
		graphics.drawString("Peter Dale 'Stordarth'", 230, fromTop);
		graphics.setColor(accentColor);		
		graphics.drawString("- Project", 125, fromTop);		
		graphics.drawString("- Project", 435, fromTop);		
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);
		graphics.drawString("Brian Amell 'Corsair'", left, fromTop);
		graphics.drawString("'Broked'", 230, fromTop);
		graphics.setColor(accentColor);		
		graphics.drawString("- Artist", 125, fromTop);
		graphics.drawString("- Programming", 310, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);
		graphics.drawString("'SirIsaacLemon'", left, fromTop);
		graphics.setColor(accentColor);					
		graphics.drawString("- Engineering", 70, fromTop);		
		graphics.setColor(Color.white);
		graphics.drawString("L. Porteous 'Wyndigo'", 230, fromTop);
		graphics.setColor(accentColor);					
		graphics.drawString("- Mapping", 425, fromTop);								
		
		// Story and Script
		fromTop = fromTop + spacingBetweenSections;		
		graphics.setColor(accentColor);
		graphics.drawString("Story and Script", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.setColor(Color.white);		
		graphics.drawLine(left, fromTop, 520, fromTop);
		
		graphics.drawString("Richard Page 'Rick'", left, fromTop);
		graphics.drawString("Keegan McCarthy 'MXC'", right, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.drawString("Peter Dale 'Stordarth'", left, fromTop);
		graphics.drawString("Ethan Rowe 'Antman 537'", right, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.drawString("Jon Chown 'Nuburan'", left, fromTop);
		graphics.drawString("'Hirsute'", right, fromTop);
		
		// Artists
		fromTop = fromTop + spacingBetweenSections;
		graphics.setColor(accentColor);
		graphics.drawString("Artists", left, fromTop);
		graphics.setColor(Color.white);
		fromTop = fromTop + spacing;
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		graphics.drawString("Brian Amell 'Corsair'", left, fromTop);
		graphics.drawString("L. Porteous 'Wyndigo'", right, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("Joshua Greiner 'Dark Link'", left, fromTop);
		graphics.drawString("'Mystic Shadow'", right, fromTop);
				
		fromTop = fromTop + spacing;
		graphics.drawString("'whiterose'", right, fromTop);
		graphics.drawString("'Alones'", left, fromTop);
		
		fromTop = fromTop + spacing;		
		graphics.drawString("Dani Hunt 'Omega Entity'", left, fromTop);
		graphics.drawString("'Googrifflon'", right, fromTop);
		
		fromTop = fromTop + spacing;		
		graphics.drawString("'Red Archer'", left, fromTop);
		
		// Game Engine
		fromTop = fromTop + spacingBetweenSections;
		
		graphics.setColor(accentColor);
		graphics.drawString("Game Engine", left, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);		
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		
		graphics.drawString("'Broked'", left, fromTop);		
		graphics.drawString("'SirIsaacLemon'", right, fromTop);
		
		// Map Creation
		fromTop = fromTop + spacingBetweenSections;
				
		graphics.setColor(accentColor);
		graphics.drawString("Map Creation", left, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);		
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		
		graphics.drawString("L. Porteous 'Wyndigo'", left, fromTop);
		graphics.drawString("Kevin Redenz 'zexxar'", right, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("Johannes Hüsing 'Drakonis'", left, fromTop);
		graphics.drawString("Keegan McCarthy 'MXC'", right, fromTop);	
		
		fromTop = fromTop + spacing;
		graphics.drawString("'SirIsaacLemon'", left, fromTop);
		
		// Battle and Weapon Systems
		fromTop = fromTop + spacingBetweenSections;
		
		graphics.setColor(accentColor);
		graphics.drawString("Battle and Weapon Systems", left, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);		
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		
		graphics.drawString("Keegan McCarthy 'MXC'", right, fromTop);		
		graphics.drawString("Peter Dale 'Stordarth'", left, fromTop);
		
		// Character Creation
		fromTop = fromTop + spacingBetweenSections;
		graphics.setColor(accentColor);
		graphics.drawString("Character Creation", left, fromTop);
		fromTop = fromTop + spacing;
		
		graphics.setColor(Color.white);
		graphics.drawLine(left, fromTop, 520, fromTop);
		fromTop = fromTop + spacingAfterHorizontalRule;
		
		graphics.drawString("Richard Page 'Rick'", left, fromTop);
		graphics.drawString("Keegan McCarthy 'MXC'", right, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("xenometal", left, fromTop);
		graphics.drawString("Frank Gritzmacher 'Balbaroy'", right, fromTop);		
		fromTop = fromTop + spacing;
		graphics.drawString("Peter Dale 'Stordarth'", left, fromTop);
		graphics.drawString("'RagnarokkerAJ'", right, fromTop);
		fromTop = fromTop + spacing;		
		graphics.drawString("Chris Geddis 'Aldur'", left, fromTop);
		
		// Project Inspiration and Support
		fromTop = fromTop + spacingBetweenSections;
		graphics.setColor(accentColor);
		graphics.drawString("Project Inspiration and Support", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.setColor(Color.white);
		graphics.drawLine(left, fromTop, 520, fromTop);
		graphics.drawString("'BigNailCow'", left, fromTop += 5);
		graphics.drawString("'Space King'", right, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("Patrick Parent 'ZeXr0'", left, fromTop);				
		graphics.drawString("'Loftus'", right, fromTop);
		
		fromTop = fromTop + spacing;		
		graphics.drawString("'nightshade00123'", left, fromTop);
		graphics.drawString("'Wandering Dezorian'", right, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'NekoNova'", left, fromTop);
		graphics.drawString("'SirHedge'", right, fromTop);
		
		fromTop = fromTop + spacing;		
		graphics.drawString("'Amelie'", left, fromTop);
		
		fromTop = fromTop + spacingBetweenSections;
		graphics.setColor(accentColor);
		graphics.drawString("Musical Contributions", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.setColor(Color.white);
		graphics.drawLine(left, fromTop, 520, fromTop);
		
		fromTop = fromTop + spacingAfterHorizontalRule;
		graphics.drawString("'The Tense Battle' ~ Sephirot24", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Rise of the Titans' ~ Computer112/Dem0lecule", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Remote Attack' ~ Computer112/Dem0lecule", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Surrounded' ~ Devastus", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Shark Patrol' ~ BenjaminTibbetts", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'King's Banquet' ~ dfiechter2", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'No Quarter' ~ Jeremiah 'McTricky' George", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Unveiling for Brass Ensemble' ~ BrianSadler", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Triumphant' ~ DavidGrossmanMusic", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Hero Music' ~ Benmode", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Triumph' ~ nmarnson", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'It Feeds on Sanity' ~ SantaBro", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Battle Of The Brave' ~ Mattashi", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Darque Horizons' ~ satsumaudio", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Rising Warrior' ~ SteveSyz", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Contemplative Bliss' ~ TheBenjerman", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'He Made It' ~ AzureWorm", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'The American Child' ~ Bosa", left, fromTop);
		
		fromTop = fromTop + spacing;
		graphics.drawString("'Icy Memories' ~ jakejosepheternity", left, fromTop);
		
		fromTop = fromTop + 200;
		graphics.drawString("In memory of Frank Girtzmacher and Patrick Parent 'ZeXr0'", -20, fromTop);
		
		fromTop = fromTop + 200;
		graphics.drawString("Legacies of Veridocia is an open-source project developed by a dedicated ", -60, fromTop);
		fromTop = fromTop + spacing;
		graphics.drawString("team of volunteers and we are always looking for more help. If you're ", -60, fromTop);
		fromTop = fromTop + spacing;
		graphics.drawString("interested in joining us in any capacity please contact us at ", -60, fromTop);
		fromTop = fromTop + spacing;
		graphics.drawString("legaciesofveridocia@gmail.com", -60, fromTop);
		fromTop = fromTop + spacingBetweenSections;
		graphics.drawString("Thank you for playing!", 120, fromTop);			
		*/
		
	}

	@Override
	public void exceptionInState() {
		
		
	}
}
