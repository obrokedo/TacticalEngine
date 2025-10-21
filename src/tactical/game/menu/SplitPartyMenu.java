package tactical.game.menu;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.sprite.CombatSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class SplitPartyMenu extends Menu {

	protected ArrayList<CombatSprite> heroesLeft;
	protected ArrayList<CombatSprite> heroesRight;
	protected int heroesLeftAmt = 0;
	protected int totalHeroes = 0;
	protected int cursorLocation = 0;
	
	protected SpeechMenu speechMenu;
	
	public SplitPartyMenu(int heroesLeftAmt, StateInfo stateInfo) {
		super(PanelType.PANEL_SPLIT_PARTY);
		heroesLeft = new ArrayList<>();
		heroesRight = new ArrayList<>();
		
		for (CombatSprite hero : stateInfo.getClientProfile().getHeroesInParty()) {
			if (heroesLeft.size() < heroesLeftAmt)
				heroesLeft.add(hero);
			else
				heroesRight.add(hero);
		}
		
		this.heroesLeftAmt = heroesLeftAmt;
		this.totalHeroes = stateInfo.getClientProfile().getHeroesInParty().size();
		
		speechMenu = new SpeechMenu(menuConfig.getSplitPartyText(heroesLeftAmt), stateInfo.getPaddedGameContainer(),
				Trigger.TRIGGER_LIST_NONE, null, null);
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		
		if (input.isKeyDown(Input.KEY_RIGHT))
		{			
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(Input.KEY_LEFT))
		{			
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(Input.KEY_DOWN))
		{
			cursorLocation = (cursorLocation + 1) % totalHeroes;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		else if (input.isKeyDown(Input.KEY_UP))
		{
			if (cursorLocation != 0)
				cursorLocation = (cursorLocation - 1) % totalHeroes;
			else
				cursorLocation = totalHeroes - 1;
			return MenuUpdate.MENU_ACTION_LONG;
		}
		
		if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			return MenuUpdate.MENU_CLOSE;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(10, 10, 
				100, 160, graphics, null);
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(PaddedGameContainer.GAME_SCREEN_SIZE.width - 110, 10, 
				100, 160, graphics, null);
		
		graphics.setFont(PANEL_FONT);
		graphics.setColor(Color.white);
		int locY = 10;
		for (CombatSprite hero : heroesLeft) {
			StringUtils.drawString(hero.getName(), 20, locY, graphics);
			locY += 15;
		}
		
		locY = 10;
		for (CombatSprite hero : heroesRight) {
			StringUtils.drawString(hero.getName(), PaddedGameContainer.GAME_SCREEN_SIZE.width - 100, locY, graphics);
			locY += 15;
		}
		
		graphics.setLineWidth(3);
		graphics.drawRect((cursorLocation / 6) != 1 ? 17 : PaddedGameContainer.GAME_SCREEN_SIZE.width - 103, 15 + (cursorLocation % 6) * 15, 86, 15);
		
		speechMenu.render(gc, graphics);
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		speechMenu.update(delta, stateInfo);
		return super.update(delta, stateInfo);
	}	
	
	
}
