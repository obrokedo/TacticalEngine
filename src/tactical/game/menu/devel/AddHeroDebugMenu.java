package tactical.game.menu.devel;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;

public class AddHeroDebugMenu extends ContextDebugMenu {
	
	public AddHeroDebugMenu() {
		super();
		this.title = "ADD HERO";
		this.options = new ArrayList<>(HeroResource.getHeroNames());
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		// TODO Auto-generated method stub
		if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3) ) {
			CombatSprite hero = HeroResource.getHero(this.options.get(this.selectedIndex));
			boolean found = false;
			for (CombatSprite h : stateInfo.getAllHeroes()) {
				if (h.getId() == hero.getId()) {				
					found = true;
					break;
				}
			}
			
			if (!found) {
				stateInfo.getClientProfile().addHero(hero);
				hero.initializeSprite(stateInfo.getResourceManager());
			}
			return MenuUpdate.MENU_ACTION_LONG;
		}
		
		return super.handleUserInput(input, stateInfo);		
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		// TODO Auto-generated method stub
		super.render(gc, graphics);
	}
}
