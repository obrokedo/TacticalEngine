package tactical.engine.config.provided;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.config.SpellMenuRenderer;
import tactical.engine.state.StateInfo;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.hudmenu.Panel;
import tactical.game.sprite.CombatSprite;
import tactical.loading.ResourceManager;
import tactical.utils.StringUtils;

public class DefaultSpellMenuRenderer implements SpellMenuRenderer {
	public void render(String spellName, CombatSprite spriteCastingSpell, ResourceManager fcrm, 
			boolean spellHasBeenSelected, int selectedLevel, 
			KnownSpell selectedSpell, StateInfo stateInfo, Graphics graphics, Color forefrontColor) {
		graphics.setColor(Panel.COLOR_FOREFRONT);

		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(198,
				200 - 40, 75,
				36 + 18, graphics, null);

		graphics.setColor(forefrontColor);
		StringUtils.drawString(spellName, 205, 160, graphics);
		if (spellHasBeenSelected)
		{
			graphics.setColor(Color.red);
			graphics.setLineWidth(2);
			graphics.drawRoundRect(204,
					183,
					64, 11, 4);
		}
		else
			selectedLevel = 0;

		for (int i = 0; i < selectedSpell.getMaxLevel(); i++)
		{
			if (i <= selectedLevel)
			{
				graphics.setColor(Color.yellow);
				graphics.fillRoundRect(206 + i * 15,
						185,
						14, 7, 4);
				graphics.setColor(forefrontColor);
			}
			graphics.drawRoundRect(206 + i * 15,
					185,
					14, 7, 4);
		}
		// graphics.drawString(spellName, 410, 399);
		StringUtils.drawString("Cost:", 205, 185, graphics);

		if (spriteCastingSpell.getCurrentMP() 
				< selectedSpell.getSpell().getCosts()[selectedLevel])
			graphics.setColor(Color.red);
		StringUtils.drawString(selectedSpell.getSpell().getCosts()[selectedLevel] + "", 245, 185, graphics);
	}
	
	public void spellLevelChanged(int spellLevel) {}
	public void update(long delta) {}

}
