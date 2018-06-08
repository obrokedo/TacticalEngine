package tactical.engine.message;

/**
 * A message to indicate that a sound or music should be played
 *
 * @author Broked
 *
 */
public class AudioMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private String audio;
	private float volume;
	private boolean loop;
	private float position;

	public AudioMessage(MessageType messageType, String audio, float volume, boolean loop) {
		super(messageType);
		this.audio = audio;
		this.volume = volume;
		this.position = 0f;
		this.loop = loop;
	}

	public AudioMessage(MessageType messageType, String audio, float volume, float position, boolean loop) {
		super(messageType);
		this.audio = audio;
		this.volume = volume;
		this.loop = loop;
		this.position = position;
	}

	public String getAudio() {
		return audio;
	}

	public float getVolume() {
		return volume;
	}

	public boolean isLoop() {
		return loop;
	}

	public float getPosition() {
		return position;
	}
}
