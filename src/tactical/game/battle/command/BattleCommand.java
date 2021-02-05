package tactical.game.battle.command;

import java.io.Serializable;

import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.item.Item;
import tactical.loading.ResourceManager;

public class BattleCommand implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int COMMAND_ATTACK = 0;
	public static final int COMMAND_SPELL = 1;
	public static final int COMMAND_ITEM = 2;
	public static final int COMMAND_TURN_PREVENTED = 3;
	public static final int COMMAND_GIVE_ITEM = 4;

	private int command;
	private KnownSpell spell;
	private transient SpellDefinition jSpell;
	private Item item;
	private int level;

	public BattleCommand(int command) {
		super();
		this.command = command;
	}

	public BattleCommand(int command, Item item) {
		super();
		this.command = command;
		this.item = item;
	}

	public BattleCommand(int command, SpellDefinition jSpell, KnownSpell spell, int level) {
		super();
		this.command = command;
		this.spell = spell;
		this.level = level;
		this.jSpell = jSpell;
	}
	
	public int getCommand() {
		return command;
	}

	public SpellDefinition getSpell() {
		return jSpell;
	}

	public void setjSpell(SpellDefinition jSpell) {
		this.jSpell = jSpell;
	}

	public void initializeSpell(ResourceManager fcrm) {
		if (spell != null)
		{
			spell.initializeFromLoad(fcrm);
			jSpell = spell.getSpell();
		}
	}

	public Item getItem() {
		return item;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString() {
		String cmdStr = null;
		switch (command) {
			case COMMAND_ATTACK:
				cmdStr = "Attack";
				break;
			case COMMAND_SPELL:
				cmdStr = "Spell";
				break;
			case COMMAND_ITEM:
				cmdStr = "UseItem";
				break;
			case COMMAND_GIVE_ITEM:
				cmdStr = "GiveItem";
				break;
			case COMMAND_TURN_PREVENTED:
				cmdStr = "TurnPrevented";
				break;
		}
		return "BattleCommand[command=" + cmdStr + ",spell=" + (spell != null ? spell.getSpellId() : "none") + ",item=" + (item != null ? item.getName() : "none") + ",level=" + level + "]";
	}
	
	
}
