package tactical.engine.config;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.state.StateInfo;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.sprite.CombatSprite;
import tactical.loading.ResourceManager;

public interface SpellMenuRenderer {
	public void render(String spellName, CombatSprite spriteCastingSpell, ResourceManager fcrm, 
			boolean spellHasBeenSelected, int selectedLevel, 
			KnownSpell selectedSpell, StateInfo stateInfo, Graphics graphics, Color forefrontColor);
	
	public void spellLevelChanged(int spellLevel);
	
	public void update(long delta);
}
