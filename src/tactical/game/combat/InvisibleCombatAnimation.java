package tactical.game.combat;

import org.newdawn.slick.Graphics;

import tactical.game.ui.PaddedGameContainer;

public class InvisibleCombatAnimation extends CombatAnimation {

	@Override
	public boolean update(int delta) {
		return true;
	}

	@Override
	public void render(PaddedGameContainer fcCont, Graphics g, int yDrawPos, float scale) {

	}

}
