package tactical.engine.state;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.game.menu.Menu;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;

public class CreditsState extends LoadableGameState{
	private float scrollY = 750;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initAfterLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException {
		scrollY = (float) Math.max(scrollY - .5, -1250);
		if (container.getInput().isKeyDown(Input.KEY_ENTER)) {
			System.exit(0);
		}
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		drawCredits(container, g);
		
		if (scrollY == -1250) {
			g.drawString("(Press enter key to exit)", 120, container.getHeight() - 40);
		}
		
	}

	@Override
	protected Menu getPauseMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_CREDITS;
	}
	
	private void drawMusicContributions(float y, Graphics g) {
		y = y + 105 + scrollY;
		Color accentColor = new Color(66, 134, 244);
		g.setColor(accentColor);
		g.drawString("Musical Contributions", 40, y);
		g.setColor(Color.white);
		g.drawLine(40, y += 20, 400, y);
		g.drawString("'The Tense Battle' ~ Sephirot24", 40, y += 5);
		g.drawString("'Rise of the Titans' ~ Computer112/Dem0lecule", 40, y += 30);
		g.drawString("'Remote Attack' ~ Computer112/Dem0lecule", 40, y += 30);
		g.drawString("'Surrounded' ~ Devastus", 40, y += 30);
		g.drawString("'Shark Patrol' ~ BenjaminTibbetts", 40, y += 30);
		g.drawString("'King's Banquet' ~ dfiechter2", 40, y += 30);
		g.drawString("'No Quarter' ~ Jeremiah 'McTricky' George", 40, y += 30);
		g.drawString("'Unveiling for Brass Ensemble' ~ BrianSadler", 40, y += 30);
		g.drawString("'Triumphant' ~ DavidGrossmanMusic", 40, y += 30);
		g.drawString("'Hero Music' ~ Benmode", 40, y += 30);	
		g.drawString("'Triumph' ~ nmarnson", 40, y += 30);
		
		g.drawString("In memory of Patrick Parent 'ZeXr0'", 65, y += 200);
		
		g.drawString("Legacies of Veridocia is an open-source project developed by a dedicated ", -60, y += 250);
		g.drawString("team of volunteers and we are always looking for more help. If you're ", -60, y += 30);
		g.drawString("interested in joining us in any capacity please contact us at ", -60, y += 30);
		g.drawString("legaciesofveridocia@gmail.com", -60, y += 30);
		g.drawString("Thank you for playing!", 120, y += 120);	
			
	}

	private void drawCredits(GameContainer gc, Graphics g) {
		g.resetTransform();
		float y = 0 + scrollY;
		Color accentColor = new Color(66, 134, 244);
		g.setColor(accentColor);
		g.translate(gc.getWidth() / 2 - 240, 0);
		g.drawString("Credits", -90, y);
		
		g.drawString("Project Leads", -70, y += 20);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Keegan McCarthy 'MXC'", -70, y += 5);		
		g.drawString("Peter Dale 'Stordarth'", 230, y);
		g.drawString("Brian Amell 'Corsair'", -70, y += 20);
		g.drawString("Broked", 230, y);
		g.drawString("Giuseppe T. 'SirIsaacLemon'", -70, y += 20);
		
		
		y -= 45;
		g.setColor(accentColor);
		g.drawString("- Project", 125, y += 5);		
		g.drawString("- Project", 435, y);
		g.drawString("- Artist", 125, y += 20);
		g.drawString("- Programming", 295, y);
		g.drawString("- Engineering", 180, y += 20);
				
		g.setColor(accentColor);
		g.drawString("Map Creation", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Kevin Redenz 'zexxar'", -70, y += 5);
		g.drawString("Johannes Husing 'Drakonis'", 220, y);
		
		g.setColor(accentColor);
		g.drawString("Story and Script", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Richard Page 'Rick'", -70, y += 5);
		g.drawString("Ethan Rowe 'Antman 537'", 220, y);
		g.drawString("Jon Chown 'Nuburan'", -70, y += 20);
		g.drawString("Hirsute", 220, y);
		
		g.setColor(accentColor);
		g.drawString("Artists", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Joshua Greiner 'Dark Link'", -70, y += 5);
		g.drawString("L. Porteous 'Wyndigo'", 220, y);
		g.drawString("Mystic Shadow", -70, y += 20);
		g.drawString("Alones", 220, y);
		g.drawString("whiterose", -70, y += 20);
		g.drawString("Dani Hunt 'Omega Entity'", 220, y);
		g.drawString("Googrifflon", -70, y += 20);
		g.drawString("Red Archer", 220, y);		
		
		g.setColor(accentColor);
		g.drawString("Character Creation", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Frank Gritzmacher 'Balbaroy'", -70, y += 5);
		g.drawString("xenometal", 220, y);
		g.drawString("Chris Geddis 'Aldur'", -70, y += 20);
		g.drawString("RagnarokkerAJ", 220, y);
		g.drawString("Al Gritzmacher", -70, y += 20);
		
		g.setColor(accentColor);
		g.drawString("Project Inspiration", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("BigNailCow", -70, y += 5);
		g.drawString("Space King", 220, y);
		g.drawString("Patrick Parent 'ZeXr0'", -70, y += 20);		
		g.drawString("aanderse", 220, y);
		g.drawString("SFC Community", -70, y += 20);
		
				
		g.setColor(accentColor);
		g.drawString("Support", -70, y += 30);
		g.setColor(Color.white);
		g.drawLine(-70, y += 20, 520, y);
		g.drawString("Loftus", -70, y += 5);
		g.drawString("nightshade00123", 220, y);		
		g.drawString("Wandering Dezorian", -70, y += 20);
		g.drawString("NekoNova", 220, y);
		g.drawString("SirHedge", -70, y += 20);
		g.drawString("Amelie", 220, y);
		
		drawMusicContributions(y - scrollY, g);
	}
}
