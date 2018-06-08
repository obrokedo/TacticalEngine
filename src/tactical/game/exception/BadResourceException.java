package tactical.game.exception;

public class BadResourceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BadResourceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BadResourceException(String arg0) {
		super(arg0);
	}

	public BadResourceException(Throwable arg0) {
		super(arg0);
	}
}
