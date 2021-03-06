package tactical.engine.message;

import tactical.game.trigger.Trigger;

public class ShowCinMessage extends Message {
	private static final long serialVersionUID = 1L;
	
	private int cinId;
	private int exitTrigId;
	
	public ShowCinMessage(int cinId) {
		this(cinId, Trigger.TRIGGER_NONE);
	}
	
	public ShowCinMessage(int cinId, int exitTrigId) {
		super(MessageType.SHOW_CINEMATIC);
		this.cinId = cinId;
		this.exitTrigId = exitTrigId;
	}

	public int getCinId() {
		return cinId;
	}

	public int getExitTrigId() {
		return exitTrigId;
	}

	@Override
	public boolean isImmediate() {
				return true;
	}
	
	
}
