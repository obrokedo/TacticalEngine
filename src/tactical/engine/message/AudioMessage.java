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

	public AudioMessage(MessageType messageType, String audio, float volume, boolean loop) {
		super(messageType);
		this.audio = audio;
		this.volume = volume;
		this.loop = loop;
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
}
