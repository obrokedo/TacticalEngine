package tactical.game.menu;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.game.hudmenu.Panel;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class LandEffectPanel extends Panel
{
	private int landEffect = 0;

	public LandEffectPanel() {
		super(PanelType.PANEL_LAND_EFFECT);
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(8, 5, 80, 35, graphics, null);
		graphics.setColor(Panel.COLOR_FOREFRONT);
		StringUtils.drawString("Land", 20, 2, graphics);
		StringUtils.drawString("Effect", 14, 14, graphics);
		if (landEffect == 0)
			StringUtils.drawString(" " + landEffect + "%", 55, 8, graphics);
		else
			StringUtils.drawString(" " + landEffect + "%", 55, 8, graphics);
		// StringUtils.drawString(landEffect + "%", 37, 26, graphics);
	}

	public void setLandEffect(int landEffect) {
		this.landEffect = landEffect;
	}

	@Override
	public boolean makeAddSounds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean makeRemoveSounds() {
		// TODO Auto-generated method stub
		return false;
	}
}
