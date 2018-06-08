package tactical.utils;

import tactical.game.sprite.CombatSprite;

public class HeroAnimationWrapper extends AnimationWrapper
{
	protected CombatSprite combatSprite;

	public HeroAnimationWrapper(CombatSprite combatSprite, String animationName)
	{
		this(combatSprite, animationName, false);
	}

	public HeroAnimationWrapper(CombatSprite combatSprite, String animationName, boolean loops)
	{
		super(combatSprite.getSpriteAnims());
		this.weapon = combatSprite.getCurrentWeaponImage();
		this.combatSprite = combatSprite;
		this.setAnimation(animationName, loops);
	}

	@Override
	public void setAnimation(String animationName, boolean loops) {
		super.setAnimation((combatSprite.isPromoted() ? "Pro" : "Un") + animationName, loops);
	}

	@Override
	public boolean hasAnimation(String animationName) {
		return combatSprite.hasAnimation(animationName);
	}
}
