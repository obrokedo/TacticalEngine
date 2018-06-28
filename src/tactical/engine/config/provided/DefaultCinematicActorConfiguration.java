package tactical.engine.config.provided;

import tactical.engine.config.CinematicActorConfiguration;

public class DefaultCinematicActorConfiguration implements CinematicActorConfiguration {

	@Override
	public int getMoveUpdate() {
		return 20;
	}

	@Override
	public int getNodHeadDuration() {
		return 500;
	}

	@Override
	public int getQuiverUpdate() {
		return 25;
	}

	@Override
	public int getTrembleUpdate() {
		return 13;
	}

	@Override
	public int getAnimUpdateAfterSE() {
		return 500;
	}

	@Override
	public float getAnimSpeedForMoveSpeed(float moveSpeed) {
		return (float) (469.875 / moveSpeed);
	}
	
}
