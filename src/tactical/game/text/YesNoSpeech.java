package tactical.game.text;

import tactical.game.trigger.Trigger;

public class YesNoSpeech extends Speech {

	private int[] yesTriggers;
	private int[] noTriggers;
	
	public YesNoSpeech(String message, String[] requires, String[] excludes, int[] yesTriggers, int[] noTriggers, int heroPortrait,
			int enemyPortrait, String spriteAnimsName) {
		super(message, requires, excludes, Trigger.TRIGGER_LIST_NONE, heroPortrait, enemyPortrait, spriteAnimsName);
		this.yesTriggers = yesTriggers;
		this.noTriggers = noTriggers;
	}

	public int[] getYesTriggers() {
		return yesTriggers;
	}

	public int[] getNoTriggers() {
		return noTriggers;
	}
	
	
}
