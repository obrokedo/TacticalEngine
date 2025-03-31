package tactical.game.combat;

import org.newdawn.slick.Image;

import tactical.game.battle.BattleResults;
import tactical.game.battle.command.BattleCommand;
import tactical.game.sprite.CombatSprite;
import tactical.utils.AnimationWrapper;
import tactical.utils.HeroAnimationWrapper;

public class AttackCombatAnimation extends CombatAnimation
{
	/**
	 * Constructor to create a ranged attack
	 *
	 * @param animationWrapper
	 * @param parentSprite
	 */
	public AttackCombatAnimation(AnimationWrapper animationWrapper, CombatSprite parentSprite, Image platformIm)
	{
		super(animationWrapper, parentSprite, true, platformIm);
		minimumTimePassed = animationWrapper.getAnimationLength();
	}

	public AttackCombatAnimation(CombatSprite parentSprite, BattleResults battleResults, boolean blockingAnimation,
			boolean critted, Image platformIm)
	{
		this(parentSprite, battleResults, blockingAnimation, false, critted, platformIm);
	}

	public AttackCombatAnimation(CombatSprite parentSprite, BattleResults battleResults, boolean blockingAnimation,
			boolean rangedAttack, boolean critted, Image platformIm)
	{
		super(new HeroAnimationWrapper(parentSprite,
				(rangedAttack ? "Ranged" : "Attack")),
				parentSprite, false, platformIm);

		if (critted && animationWrapper.hasAnimation("Crit"))
			this.animationWrapper.setAnimation("Crit", false);
		else if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_SPELL)
		{
			if (animationWrapper.hasAnimation("Spell"))
				this.animationWrapper.setAnimation("Spell", false);
			else { 
				animationWrapper.setAnimation("Attack", false);
			}
		}
		else if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_ITEM)
		{
			if ( animationWrapper.hasAnimation("Item"))
				this.animationWrapper.setAnimation("Item", false);
			else 
				animationWrapper.setAnimation("Attack", false);
		}
		else if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_SPECIAL)
		{
			if ( animationWrapper.hasAnimation("Special"))
				this.animationWrapper.setAnimation("Special", false);
			else 
				animationWrapper.setAnimation("Attack", false);
		}

		this.blocks = blockingAnimation;

		minimumTimePassed = animationWrapper.getAnimationLength();
	}

	@Override
	public boolean update(int delta) {
		/*
		if (castingSpell)
		{
			int maxTime = getAnimationLength() - 600;
			if (this.totalTimePassed > maxTime)
				this.setDrawSpell(true);
		}
		*/
		return super.update(delta);
	}
}
