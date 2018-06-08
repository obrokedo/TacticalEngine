package tactical.particle;

import org.newdawn.slick.particles.Particle;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;

import tactical.utils.AnimationWrapper;

public class AnimatedParticle extends Particle
{
	private AnimationWrapper animWrapper;
	private boolean flipHorz = false;
	private float scale;
	private float customScale = 1;
	
	public AnimatedParticle(AnimationWrapper animWrapper, ParticleSystem engine, float scale) {
		super(engine);
		this.animWrapper = animWrapper;
		this.scale = scale;
	}
	
	@Override
	public void init(ParticleEmitter emitter, float life) {
		super.init(emitter, life);
		animWrapper.resetCurrentAnimation();
		if (life == 0)
			this.setLife(animWrapper.getAnimationLength());
	}



	@Override
	public void update(int delta) {
		animWrapper.update(delta);
		super.update(delta);
	}

	@Override
	public void render() {
		animWrapper.drawAnimationDirect((int) x,  (int) y, scale * customScale, false, flipHorz);
	}
	
	public void flipHorizontal(boolean flip)
	{
		this.flipHorz = flip;
	}
	
	public void flipVertical()
	{
		
	}
	
	public void rotate(float angle)
	{
		
	}
	
	public void setScale(float customScale) {
		this.customScale = customScale;
	}
}