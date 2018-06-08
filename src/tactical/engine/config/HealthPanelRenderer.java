package tactical.engine.config;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;

import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.ResourceManager;

public interface HealthPanelRenderer {
	enum PanelLocation {
		HERO_HEALTH,
		ENEMY_HEALTH,
		TARGET_HEALTH
	}
	public void displayHealthPanel(ResourceManager fcrm, CombatSprite sprite, UnicodeFont panelFont, 
			PaddedGameContainer gc, Graphics graphics, PanelLocation panelLocation);
}
