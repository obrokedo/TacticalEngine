package tactical.game.resource;

import java.util.Hashtable;

import tactical.engine.TacticalGame;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.exception.BadResourceException;
import tactical.loading.ResourceManager;

public class SpellResource
{
	public static Hashtable<String, SpellDefinition> spells;

	public static void initSpells(ResourceManager frm)
	{
		spells = new Hashtable<String, SpellDefinition>();
		for (String spell : TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().getSpellList())
			addSpell(spell, frm);
	}

	private static void addSpell(String spellId, ResourceManager frm)
	{
		SpellDefinition s = TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().createSpell(spellId);
		if (s != null) {
			if (frm != null)
				s.setSpellIcon(frm.getSpriteSheet("spellicons").getSubImage(s.getSpellIconId(), 0));
			spells.put(spellId, s);
		}
	}

	public static SpellDefinition getSpell(String spellId)
	{
		SpellDefinition sd = spells.get(spellId);
		if (sd == null)
			throw new BadResourceException("The spell with id: " + spellId + 
					" has not been correctly configured and does not exist. Check to make sure the spell has been implemented");
		return sd;
	}
}
