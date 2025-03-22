package tactical.engine.message;

import tactical.game.battle.spell.KnownSpell;

public class SpellMessage extends Message
{
	private static final long serialVersionUID = 1L;

	public KnownSpell knownSpell;
	public int level;
	
	public SpellMessage(MessageType messageType, KnownSpell knownSpell,
			int level) {
		super(messageType);
		this.knownSpell = knownSpell;
		this.level = level;
	}
}
