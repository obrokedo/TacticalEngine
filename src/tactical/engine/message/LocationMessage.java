package tactical.engine.message;

/**
 * A reusable message that should be used to issue a message that has
 * a x and y coordinate as a parameter
 *
 * @author Broked
 *
 */
public class LocationMessage extends Message
{
	private static final long serialVersionUID = 1L;

	public int locX;
	public int locY;

	public LocationMessage(MessageType messageType, int locX, int locY) {
		super(messageType);
		this.locX = locX;
		this.locY = locY;
	}
}
