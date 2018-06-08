package tactical.game.sound;

import org.newdawn.slick.Sound;

public class LoopingSound 
{
	public Sound sound;
	public int duration;
	
	public LoopingSound(Sound sound, int duration) {
		super();
		this.sound = sound;
		this.duration = duration;
	}
}
