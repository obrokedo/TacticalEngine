package tactical.particle;

import org.newdawn.slick.Image;
import org.newdawn.slick.particles.Particle;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;

public class ScriptDrivenEmitter implements ParticleEmitter
{
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
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
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

}
