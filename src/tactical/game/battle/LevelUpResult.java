package tactical.game.battle;

import java.io.Serializable;

public class LevelUpResult implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int attackGain;
	public int defenseGain;
	public int speedGain;
	public int hitpointGain;
	public int magicpointGain;
	public int bodyGain;
	public int mindGain;
	public String text;
}
