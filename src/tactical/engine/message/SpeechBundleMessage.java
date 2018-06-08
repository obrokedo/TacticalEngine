package tactical.engine.message;

/**
 * A message that indicates that a speech menu should be displayed with the given
 * text and portrait
 *
 * @author Broked
 *
 */
public class SpeechBundleMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private int speechId;
	private int speechIndex;
	private boolean immediate;
	
	public SpeechBundleMessage(int speechId, int speechIndex, boolean immediate) {
		super(MessageType.SPEECH);
		this.speechId = speechId;
		this.speechIndex = speechIndex;
		this.immediate = immediate;
	}

	public int getSpeechId() {
		return speechId;
	}

	public int getSpeechIndex() {
		return speechIndex;
	}

	@Override
	public boolean isImmediate() {
		return immediate;
	}
}