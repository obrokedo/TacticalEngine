package tactical.game.text;

import tactical.game.trigger.Trigger;

public class YesNoSpeech extends Speech {

	private int yesTrigger;
	private int noTrigger;
	
	public YesNoSpeech(String message, String[] requires, String[] excludes, int yesTrigger, int noTrigger, int heroPortrait,
			int enemyPortrait, String spriteAnimsName) {
		super(message, requires, excludes, Trigger.TRIGGER_NONE, heroPortrait, enemyPortrait, spriteAnimsName);
		this.yesTrigger = yesTrigger;
		this.noTrigger = noTrigger;
	}

	public int getYesTrigger() {
		return yesTrigger;
	}

	public int getNoTrigger() {
		return noTrigger;
	}
	
	
}
