package tactical.utils;

import java.util.Set;

import org.newdawn.slick.Image;

import tactical.game.constants.Direction;

public class SingleImageSpriteAnims extends SpriteAnims {

	protected Image image;
	
	public SingleImageSpriteAnims(Image image) {
		super();
		this.image = image;
		Animation anim = new Animation("anim", null, null);
		AnimFrame af = new AnimFrame(10000);
		af.sprites.add(new AnimSprite(0, 0, 0, 0, false, false));
		anim.frames.add(af);
		this.animations.put("single", anim);
	}

	@Override
	public void addAnimation(String name, Animation anim) {

	}

	@Override
	public boolean hasAnimation(String name) {
		return true;
	}

	@Override
	public boolean hasCharacterAnimation(String name, boolean isPromoted) {
		return true;
	}

	@Override
	public Animation getCharacterAnimation(String name, boolean isPromoted) {
		return animations.get("single");
	}

	@Override
	public Animation getDirectionAnimation(Direction dir) {
		return animations.get("single");
	}

	@Override
	public Animation getCharacterDirectionAnimation(Direction dir, boolean isHeroPromoted) {
		
		return animations.get("single");
	}

	@Override
	public Animation getAnimation(String name) {
		return animations.get("single");
	}
	
	@Override
	public void printAnimations() {
	}

	@Override
	public Set<String> getAnimationKeys() {
		
		return super.getAnimationKeys();
	}

	
	
}
