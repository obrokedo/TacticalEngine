package tactical.engine.config;

import org.newdawn.slick.Image;
import org.newdawn.slick.particles.Particle;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;

import tactical.loading.ResourceManager;

public abstract class ParticleEmitterConfiguration implements ParticleEmitter
{
	protected ResourceManager fcResourceManager;
	
	public abstract void initialize(boolean isHero);

	@Override
	public void update(ParticleSystem system, int delta) {
		
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
				return false;
	}

	@Override
	public Image getImage() {
				return null;
	}

	@Override
	public boolean isOriented() {
				return false;
	}

	@Override
	public boolean usePoints(ParticleSystem system) {
				return false;
	}

	@Override
	public void resetState() {
			}

	public ResourceManager getFcResourceManager() {
		return fcResourceManager;
	}

	public void setFcResourceManager(ResourceManager fcResourceManager) {
		this.fcResourceManager = fcResourceManager;
	}
}