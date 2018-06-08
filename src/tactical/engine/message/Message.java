package tactical.engine.message;

import java.io.Serializable;

/**
 * Superclass that defines messages types that can be sent and
 * can be used to send a message that requires no additional parameters
 *
 * @author Broked
 *
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	protected MessageType messageType;
	private boolean immediate = false;
	private boolean internal = false;

	public Message(MessageType messageType, boolean immediate, boolean internal) {
		super();
		this.messageType = messageType;
		this.immediate = immediate;
		this.internal = internal;
	}

	public Message(MessageType messageType) {
		super();
		this.messageType = messageType;

		switch (messageType)
		{
			case PAUSE_MUSIC:
			case RESUME_MUSIC:
			case FADE_MUSIC:
			case SOUND_EFFECT:
			case MOVETO_SPRITELOC:
			case OVERLAND_MOVE_MESSAGE:
			case NEXT_TURN:
			case CIN_END:
				immediate = true;
				break;
			case SHOW_BATTLEMENU:
			case SHOW_SPELLMENU:
			case SHOW_ITEM_MENU:
			case SHOW_ITEM_OPTION_MENU:
				internal = true;
				break;
			case INTIIALIZE:
				immediate = true;
				internal = true;
				break;
			default:
				immediate = false;

		}
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public boolean isImmediate() {
		return immediate;
	}

	public boolean isInternal() {
		return internal;
	}
}
