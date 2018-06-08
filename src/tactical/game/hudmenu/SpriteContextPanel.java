package tactical.game.hudmenu;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tactical.engine.config.HealthPanelRenderer;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.ResourceManager;

public class SpriteContextPanel extends Panel
{
	private CombatSprite sprite;
	private HealthPanelRenderer healthPanelRenderer;
	private ResourceManager fcrm;

	public SpriteContextPanel(PanelType menuType, CombatSprite sprite, 
			HealthPanelRenderer healthPanelRenderer, ResourceManager fcrm, GameContainer gc) {
		super(menuType);
		this.sprite = sprite;
		this.healthPanelRenderer = healthPanelRenderer;
		this.fcrm = fcrm;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		HealthPanelRenderer.PanelLocation pl = HealthPanelRenderer.PanelLocation.HERO_HEALTH;
		switch (panelType)
		{
			case PANEL_ENEMY_HEALTH_BAR:
				pl = HealthPanelRenderer.PanelLocation.ENEMY_HEALTH;
				break;
			case PANEL_TARGET_HEALTH_BAR:
				pl = HealthPanelRenderer.PanelLocation.TARGET_HEALTH;
				break;
			default:
				break;
		}
		
		healthPanelRenderer.displayHealthPanel(fcrm, sprite, PANEL_FONT, gc, graphics, pl);
	}
}
