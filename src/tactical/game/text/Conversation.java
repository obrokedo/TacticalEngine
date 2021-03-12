package tactical.game.text;

import tactical.engine.state.StateInfo;
import tactical.game.menu.Portrait;

public class Conversation extends Speech {
	private int heroPortrait2;
	private int enemyPortrait2;
	private String spriteAnimsName2;
	private String[] messages;
	private int messageIndex;
	
	public Conversation(String[] messages, String[] requires, String[] excludes, int[] triggerIds, int heroPortrait,
			int enemyPortrait, String spriteAnimsName, int heroPortrait2, int enemyPortrait2, String spriteAnimsName2) {
		super(null, requires, excludes, triggerIds, heroPortrait, enemyPortrait, spriteAnimsName);
		this.heroPortrait2 = heroPortrait2;
		this.enemyPortrait2 = enemyPortrait2;
		this.spriteAnimsName2 = spriteAnimsName2;
		this.messages = messages;
	}

	@Override
	public String getMessage() {
		return messages[messageIndex];
	}

	@Override
	public Portrait getPortrait(StateInfo stateInfo) {
		// This is super ugly design-wise we allow the user to keep retrieving the same message
		// but don't increment the message index until the portrait is retrieved. Considered
		// a number of options to make this prettier, but none are really worth doing
		if (messageIndex++ % 2 == 0) {
			return Portrait.getPortrait(heroPortrait, enemyPortrait, spriteAnimsName, stateInfo);
		} else {
			return Portrait.getPortrait(heroPortrait2, enemyPortrait2, spriteAnimsName2, stateInfo);
		}
	}

	@Override
	public boolean hasMoreSpeech() {
		return messageIndex < messages.length;
	}

	@Override
	public void initialize() {
		messageIndex = 0;
	}
}
