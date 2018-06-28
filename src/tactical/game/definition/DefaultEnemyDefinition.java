package tactical.game.definition;

import java.util.ArrayList;

import tactical.game.battle.spell.KnownSpell;
import tactical.game.sprite.CombatSprite;
import tactical.utils.XMLParser.TagArea;

public final class DefaultEnemyDefinition extends EnemyDefinition {

	public DefaultEnemyDefinition(TagArea tagArea) {
		super(tagArea);
	}

	protected CombatSprite createNewCombatSprite(int myId, ArrayList<KnownSpell> knownSpells) {
		return new CombatSprite(leader, name, animations, hp, mp, attack, defense,
				speed, move, movementType, level, myId, knownSpells, ENEMY_COUNT--,
				effectId, effectChance, effectLevel);
	}

	@Override
	protected void parseCustomEnemyDefinition(TagArea tagArea) {}
}
