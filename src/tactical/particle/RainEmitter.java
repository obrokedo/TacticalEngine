package tactical.particle;

import org.newdawn.slick.Image;
import org.newdawn.slick.particles.Particle;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;

import tactical.game.ui.PaddedGameContainer;

public class RainEmitter implements ParticleEmitter
{

	private int width;

	/** The particle emission rate */
	private int interval = 100;
	/** Time til the next particle */
	private int timer;
	/** The size of the initial particles */
	private float size = 166;
	
	private int drawLeft = 0;

	public RainEmitter(int width, int interval, boolean onLeft) {
		super();
		this.width = width;
		this.interval = interval;
		if (!onLeft)
		{
			drawLeft = PaddedGameContainer.GAME_SCREEN_SIZE.width - width + 10;
		}
	}

	@Override
	public void update(ParticleSystem system, int delta) {
		timer -= delta;
		if (timer <= 0) {
			timer = 200;
			createRainParticle(system, 0);
			createRainParticle(system, 150);
			createRainParticle(system, 300);
			createRainParticle(system, 450);
		}
	}
	
	private void createSnowParticle(ParticleSystem system, int loc) {
		Particle p = system.getNewParticle(this, 1000);
		// p.setColor(1, 1, 1, 0.5f);

		p.setPosition((float) (loc) + drawLeft, -150);
		p.setSize(size);
		p.setLife(4000);

		p.setVelocity((float) (Math.random() * -.01f),.03f,3f + (float) Math.random() * 2f);
	}
	
	private void createRainParticle(ParticleSystem system, int loc) {
		Particle p = system.getNewParticle(this, 1000);
		// p.setColor(1, 1, 1, 0.5f);

		p.setPosition((float) (loc) + drawLeft, -150);
		p.setSize(size);
		p.setLife(2000);

		p.setVelocity(-.01f,.1f,3f + (float) Math.random() * 2f);
	}

	private void createParticle(ParticleSystem system) {
		Particle p = system.getNewParticle(this, 1000);
		// p.setColor(1, 1, 1, 0.5f);

		p.setPosition((float) (Math.random() * width) + drawLeft, 0);
		p.setSize(size);
		p.setLife(1000);

		p.setVelocity(-.01f,.1f,3f + (float) Math.random() * 2f);
	}

	@Override
	public boolean completed() {
		return false;
	}

	@Override
	public void wrapUp() {
	}

	@Override
	public void updateParticle(Particle particle, int delta) {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {

	}

	@Override
	public boolean useAdditive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOriented() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean usePoints(ParticleSystem system) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub

	}

}
