package tactical.game.menu;

import tactical.game.sprite.CombatSprite;

public abstract class AbstractHeroStatMenu extends Menu {
	public AbstractHeroStatMenu(PanelType menuType) {
		super(menuType);
	}

	public abstract CombatSprite getSelectedSprite();
}
