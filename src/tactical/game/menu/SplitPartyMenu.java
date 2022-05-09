package tactical.game.menu;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.input.UserInput;
import tactical.game.sprite.CombatSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;

public class SplitPartyMenu extends Menu {

	protected ArrayList<CombatSprite> heroesLeft;
	protected ArrayList<CombatSprite> heroesRight;
	protected int heroesLeftAmt = 0;
	protected int totalHeroes = 0;
	
	protected SpeechMenu speechMenu;
	
	public SplitPartyMenu(int heroesLeftAmt, StateInfo stateInfo) {
		super(PanelType.PANEL_SPLIT_PARTY);
		heroesLeft = stateInfo.getClientProfile().getHeroesInParty();
		heroesRight = new ArrayList<>();
		this.heroesLeftAmt = heroesLeftAmt;
		this.totalHeroes = heroesLeft.size();
		speechMenu = new SpeechMenu(menuConfig.getSplitPartyText(heroesLeftAmt), stateInfo.getPaddedGameContainer(),
				Trigger.TRIGGER_LIST_NONE, null, null);
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		// TODO Auto-generated method stub
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(10, 10, 
				100, 160, graphics, null);
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(PaddedGameContainer.GAME_SCREEN_SIZE.width - 110, 10, 
				100, 160, graphics, null);
		speechMenu.render(gc, graphics);
	}	
}
