package tactical.engine.message;

import tactical.game.menu.Portrait;
import tactical.game.trigger.Trigger;

/**
 * A message that indicates that a speech menu should be displayed with the given
 * text and portrait
 *
 * @author Broked
 *
 */
public class SpeechMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private String text;
	private int triggerId = -1;
	private int noTriggerId = -1;
	private Portrait portrait = null;
	private boolean yesNoMessage = false;

	public SpeechMessage(String text, int triggerId, Portrait portrait) {
		super(MessageType.SPEECH);
		this.text = text;
		this.triggerId = triggerId;
		this.portrait = portrait;
	}
	
	public SpeechMessage(String text, int yesTriggerId, int noTriggerId, Portrait portrait) {
		super(MessageType.SPEECH);
		this.text = text;
		this.triggerId = yesTriggerId;
		this.noTriggerId = noTriggerId;
		this.portrait = portrait;
		this.yesNoMessage = true;
	}
	
	public SpeechMessage(String text) {
		this(text, Trigger.TRIGGER_NONE, null);
	}

	public String getText() {
		return text;
	}

	public int getTriggerId() {
		return triggerId;
	}

	public Portrait getPortrait() {
		return portrait;
	}

	public int getNoTriggerId() {
		return noTriggerId;
	}

	public boolean isYesNoMessage() {
		return yesNoMessage;
	}

	@Override
	public boolean isImmediate() {
		return true;
	}
}
