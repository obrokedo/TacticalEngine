package tactical.utils;

import java.util.Set;

import org.newdawn.slick.Image;

import tactical.game.constants.Direction;

public class SingleImageSpriteAnims extends SpriteAnims {

	protected Image image;
	
	public SingleImageSpriteAnims(Image image) {
		super(null, null);
		this.image = image;
		Animation anim = new Animation("anim");
		AnimFrame af = new AnimFrame(10000);
		af.sprites.add(new AnimSprite(0, 0, 0, 0, false, false));
		anim.frames.add(af);
		this.animations.put("single", anim);
	}

	@Override
	public void addAnimation(String name, Animation anim) {

	}

	@Override
	public void initialize(Image image) {
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
		// TODO Auto-generated method stub
		return animations.get("single");
	}

	@Override
	public Animation getAnimation(String name) {
		return animations.get("single");
	}

	@Override
	public Image getImageAtIndex(int idx) {
		return image;
	}

	@Override
	public void printAnimations() {
	}

	@Override
	public Set<String> getAnimationKeys() {
		// TODO Auto-generated method stub
		return super.getAnimationKeys();
	}

	
	
}
