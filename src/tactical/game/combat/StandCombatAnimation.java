package tactical.game.combat;

import org.newdawn.slick.Image;

import tactical.game.sprite.CombatSprite;
import tactical.utils.HeroAnimationWrapper;

public class StandCombatAnimation extends CombatAnimation
{
	public StandCombatAnimation(CombatSprite parentSprite, Image platformIm)
	{
		this(parentSprite, 500, platformIm);
	}

	public StandCombatAnimation(CombatSprite parentSprite, int mimimumTimePassed, Image platformIm)
	{
		super(new HeroAnimationWrapper(parentSprite, "Stand", true), parentSprite, false, platformIm);
		this.minimumTimePassed = mimimumTimePassed;
	}
	
	public void continueStandAnimation(CombatAnimation previousAnimation) {
		this.animationWrapper.copyAnimationLocation(previousAnimation.animationWrapper);
	}
}
