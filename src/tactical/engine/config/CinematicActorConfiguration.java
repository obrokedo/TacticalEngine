package tactical.engine.config;

/**
 * Interface to call the CinematicActor python methods that will determine
 * speeds that certain actions in cinematics should take place
 *
 * @see /scripts/CinematicActor.py
 *
 * @author Broked
 *
 */
public interface CinematicActorConfiguration
{
	public int getMoveUpdate();	// 20
	public int getNodHeadDuration(); // 500
	public int getQuiverUpdate(); // 25
	public int getTrembleUpdate(); // 13
	public int getAnimUpdateAfterSE(); // 500
	public float getAnimSpeedForMoveSpeed(float moveSpeed); // (469.875 / speed);
}
