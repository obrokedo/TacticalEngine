package tactical.engine.config;

import java.io.Serializable;

import tactical.game.battle.spell.SpellDefinition;

/**
 * Interface to create new Spells along with their configuration:
 * spell statistics, experience gained by using a spell and the text
 * to display when using a spell
 *
 * @author Broked
 *
 */
public abstract class SpellFactory implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new Spell object and initializes it with
	 * the values associated with the given spellId.
	 *
	 * @param spellId the id of the spell that should be created and populated
	 * @return a new Spell object with values initialized as determined by
	 * the spellId
	 */
	public abstract SpellDefinition createSpell(String spellId);
	
	public abstract String[] getSpellList();
}
