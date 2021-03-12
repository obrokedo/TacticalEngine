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
	private int[] triggerIds = Trigger.TRIGGER_LIST_NONE;
	private int[] noTriggerIds = Trigger.TRIGGER_LIST_NONE;
	private Portrait portrait = null;
	private boolean yesNoMessage = false;

	public SpeechMessage(String text, Portrait portrait) {
		super(MessageType.SPEECH);
		this.text = text;
		this.triggerIds = Trigger.TRIGGER_LIST_NONE;
		this.portrait = portrait;
	}
	
	public SpeechMessage(String text, int[] triggerIds, Portrait portrait) {
		super(MessageType.SPEECH);
		this.text = text;
		this.triggerIds = triggerIds;
		this.portrait = portrait;
	}
	
	public SpeechMessage(String text, int[] yesTriggerIds, int[] noTriggerIds, Portrait portrait) {
		super(MessageType.SPEECH);
		this.text = text;
		this.triggerIds = yesTriggerIds;
		this.noTriggerIds = noTriggerIds;
		this.portrait = portrait;
		this.yesNoMessage = true;
	}
	
	public SpeechMessage(String text) {
		this(text, null);
	}

	public String getText() {
		return text;
	}

	public int[] getTriggerIds() {
		return triggerIds;
	}

	public Portrait getPortrait() {
		return portrait;
	}

	public int[] getNoTriggerIds() {
		return noTriggerIds;
	}

	public boolean isYesNoMessage() {
		return yesNoMessage;
	}

	@Override
	public boolean isImmediate() {
		return true;
	}
}
