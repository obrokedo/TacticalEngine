package tactical.game.item;

import tactical.game.battle.spell.SpellDefinition;
import tactical.game.resource.SpellResource;
import tactical.loading.ResourceManager;

public class SpellItemUse {
	private transient SpellDefinition spell;
	private int charges = 0;
	private String spellId;
	private int level;
	private boolean singleUse;
	private boolean useOutsideBattle;

	public SpellItemUse(String spellId, int level, boolean singleUse, boolean useOutsideBattle) {
		super();
		this.spellId = spellId;
		this.level = level;
		this.singleUse = singleUse;
		this.useOutsideBattle = useOutsideBattle;
	}

	public SpellDefinition getSpell() {
		return spell;
	}

	public String getSpellId() {
		return spellId;
	}

	public int getLevel() {
		return level;
	}

	public boolean isSingleUse() {
		return singleUse;
	}
	
	public boolean isUseOutsideBattle() {
		return useOutsideBattle;
	}

	public void initialize(ResourceManager fcrm) {
		spell = SpellResource.getSpell(spellId);
	}

	public int getCharges() {
		return charges;
	}

	public void setCharges(int charges) {
		this.charges = charges;
	}
}
