package tactical.game.manager;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.cinematic.Cinematic;
import tactical.engine.message.IntMessage;
import tactical.engine.message.Message;
import tactical.engine.message.ShowCinMessage;
import tactical.game.trigger.Trigger;

public class CinematicManager extends Manager
{
	private Cinematic cinematic;
	private boolean initializeCamera = true;
	private int exitTrigId = Trigger.TRIGGER_NONE;
	private Color fadingColor = null;

	public CinematicManager(boolean initializeCamera) {
		super();
		this.initializeCamera = initializeCamera;
	}

	@Override
	public void initialize() {
		fadingColor = null;
		this.cinematic = null;
		this.exitTrigId = Trigger.TRIGGER_NONE;
	}

	public void update(int delta)
	{
		if (cinematic != null)
		{
			stateInfo.getCurrentMap().checkRoofs((int) stateInfo.getCamera().getCenterOfCamera().getX(),
					(int) stateInfo.getCamera().getCenterOfCamera().getY());
			
			if (cinematic.update(delta, stateInfo.getCamera(),
				stateInfo.getInput(), stateInfo.getCurrentMap(), stateInfo)) {
				cinematic.endCinematic(stateInfo);
				fadingColor = cinematic.getFadingColor();
				cinematic = null;
				stateInfo.getCurrentMap().setDisableRoofs(false);
				if (exitTrigId != Trigger.TRIGGER_NONE) {
					stateInfo.getResourceManager().getTriggerEventById(exitTrigId).perform(stateInfo);
					exitTrigId = Trigger.TRIGGER_NONE;
				}
			}
		}

	}

	public void render(Graphics g)
	{
		if (cinematic != null)
			cinematic.render(g, stateInfo.getCamera(), stateInfo.getPaddedGameContainer(), stateInfo);
	}

	public void renderPostEffects(Graphics g)
	{
		if (cinematic != null)
			cinematic.renderPostEffects(g, stateInfo.getCamera(), stateInfo.getPaddedGameContainer(), stateInfo);
		else if (fadingColor != null) {
			g.setColor(fadingColor);
			g.fillRect(0, 0, stateInfo.getCamera().getViewportWidth(), stateInfo.getCamera().getViewportHeight());
		}
	}

	@Override
	public void recieveMessage(Message message)
	{
		switch (message.getMessageType())
		{
			case SHOW_CINEMATIC:
				if (stateInfo.isInCinematicState())
					stateInfo.getCurrentMap().setDisableRoofs(true);
				int cinId = -1;
				if (message instanceof ShowCinMessage) {
					ShowCinMessage m = ((ShowCinMessage) message);
					cinId = m.getCinId();
					this.exitTrigId = m.getExitTrigId();
				} else if (message instanceof IntMessage) {
					cinId = ((IntMessage) message).getValue();
					this.exitTrigId = Trigger.TRIGGER_NONE;
				}
				
				fadingColor = null;
				Cinematic cin = stateInfo.getResourceManager().getCinematicById(cinId).duplicateCinematic();
				cin.initialize(stateInfo, initializeCamera);
				cinematic = cin;
				break;
			case CIN_NEXT_ACTION:
				if (cinematic != null)
					cinematic.nextAction(stateInfo);
				break;
			default:
				break;
		}
	}

	public boolean isBlocking()
	{
		return cinematic != null;
	}

	public Cinematic getCinematic() {
		return cinematic;
	}
}
