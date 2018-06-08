package tactical.game.exception;

public class MissingCodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MissingCodeException(String arg0) {
		super(arg0);
	}

	public MissingCodeException(Throwable arg0) {
		super(arg0);
	}
}
