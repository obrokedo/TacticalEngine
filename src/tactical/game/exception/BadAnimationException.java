package tactical.game.exception;

public class BadAnimationException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public BadAnimationException(String message) {
		super(message);
	}
}
