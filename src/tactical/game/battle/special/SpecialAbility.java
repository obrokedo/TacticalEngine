package tactical.game.battle.special;

import java.io.Serializable;

import tactical.game.battle.spell.SpellDefinition;
import tactical.game.resource.SpellResource;
import tactical.loading.ResourceManager;

public class SpecialAbility implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int chance;
	private transient SpellDefinition spell;
	private String spellId;
	
	public SpecialAbility(String spellId, int chance) {
		super();
		this.chance = chance;
		this.spell = SpellResource.getSpell(spellId);
		this.spellId = spellId;
	}
	
	public int getChance() {
		return chance;
	}
	
	public SpellDefinition getSpell() {
		return spell;
	}
	
	public void initializeFromLoad(ResourceManager fcrm)
	{
		this.spell = SpellResource.getSpell(spellId);
	}
}
