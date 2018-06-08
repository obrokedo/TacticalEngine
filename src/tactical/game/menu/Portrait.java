package tactical.game.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.resource.EnemyResource;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.utils.AnimFrame;
import tactical.utils.AnimationWrapper;
import tactical.utils.SpriteAnims;

public class Portrait
{
	private AnimationWrapper idleAnim, blinkAnim, talkAnim;
	private int topHeight;
	private boolean isBlinking = false, isTalking = false;
	private int blinkCounter = 0;
	private boolean portraitFound = true;

	/**
	 * Retrieves a portrait for the hero with the given id if one is specified, or a portrait for
	 * an enemy with the given id if one is specifed or a portrait contained in the specified spriteAnimName.
	 * Returns null if none of the options are specified
	 *
	 * @param heroId the id of the hero whose portrait should be used, -1 if a hero portrait should not be used
	 * @param enemyId the id of the enemy whose portrait should be used, -1 if an enemy portrait should not be used
	 * @param spriteAnimName the name of the sprite animation contained in the resource manager that
	 * 			the portrait should be retrieved from
	 * @param stateInfo the StateInfo describing the current game state
	 * @return The loaded portrait or null if no options are specified
	 */
	public static Portrait getPortrait(int heroId, int enemyId, String spriteAnimName, StateInfo stateInfo)
	{
		SpriteAnims sa = null;

		if (enemyId != -1)
		{
			sa = stateInfo.getResourceManager().getSpriteAnimation(EnemyResource.getAnimation(enemyId));
			return getPortrait(sa, false);
		}
		else if (heroId != -1)
		{
			for (CombatSprite cs : stateInfo.getAllHeroes())
			{
				if (cs.getHeroProgression().getHeroID() == heroId)
				{
					return getPortrait(cs);
				}
			}

			sa = stateInfo.getResourceManager().getSpriteAnimation(HeroResource.getAnimation(heroId));
			return getPortrait(sa, false);
		}
		else if (spriteAnimName != null) {
			return getPortrait(stateInfo.getResourceManager().getSpriteAnimation(spriteAnimName), false);
		}

		return null;
	}

	/**
	 * Retrieves the portrait for the given CombatSprite, if the given CombatSprite is a member
	 * of the current party the Portrait will respect their promoted status.
	 *
	 * @param combatSprite the CombatSprite whose portrait should be retrieved
	 * @return the portrait of the specified CombatSprite
	 */
	public static Portrait getPortrait(CombatSprite combatSprite)
	{
		Portrait p = getPortrait(combatSprite.getSpriteAnims(), combatSprite.isPromoted());
		// Enemies can have portraits, but if one isn't specified assume that it shouldn't have one
		if (p.portraitFound && combatSprite.isHero())
			return p;
		return null;
	}

	private static Portrait getPortrait(SpriteAnims spriteAnim, boolean promoted)
	{
		Portrait p = new Portrait();
		if ((promoted || spriteAnim.hasAnimation("UnPortIdle")) && spriteAnim.hasAnimation("ProPortIdle"))
		{
			p.idleAnim = new AnimationWrapper(spriteAnim, "ProPortIdle", true);
			p.blinkAnim = new AnimationWrapper(spriteAnim, "ProPortBlink", false);
			p.talkAnim = new AnimationWrapper(spriteAnim, "ProPortTalk", true);

			AnimFrame af = p.blinkAnim.getCurrentAnimation().frames.get(0);
			p.topHeight = spriteAnim.images.get(af.sprites.get(0).imageIndex).getHeight();
			return p;
		}
		else if (spriteAnim.hasAnimation("UnPortIdle"))
		{
			p.idleAnim = new AnimationWrapper(spriteAnim, "UnPortIdle", true);
			p.blinkAnim = new AnimationWrapper(spriteAnim, "UnPortBlink", false);
			p.talkAnim = new AnimationWrapper(spriteAnim, "UnPortTalk", true);

			AnimFrame af = p.blinkAnim.getCurrentAnimation().frames.get(0);
			p.topHeight = spriteAnim.images.get(af.sprites.get(0).imageIndex).getHeight();

			return p;
		}

		p.portraitFound = false;
		return p;
	}

	public void render(int x, int y, Graphics graphics)
	{
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, y, 62,
				78, graphics, Color.black);

		if (!portraitFound)
		{
			graphics.setColor(Color.red);
			graphics.fillRect(x + 7,
					y + 7, 100, 100);
			return;
		}

		idleAnim.drawAnimationPortrait(x + 7,
				y + 7, topHeight, graphics);

		if (isBlinking)
			blinkAnim.drawAnimationIgnoreOffset(x + 7,
				y + 7, graphics);

		if (isTalking)
			talkAnim.drawAnimationIgnoreOffset(x + 7,
					y + 7 + topHeight, graphics);
	}

	public void update(long delta)
	{
		if (!portraitFound)
			return;

		delta = (int) (delta * (TacticalGame.RANDOM.nextFloat() * 2));
		if (isBlinking)
		{
			if (blinkAnim.update(delta))
			{
				isBlinking = false;
				blinkAnim.resetCurrentAnimation();
			}
		}
		else
		{
			blinkCounter += delta;
			if (blinkCounter >= 1500)
			{
				isBlinking = true;
				blinkCounter = 0;
			}
		}

		if (isTalking)
			talkAnim.update(delta);
	}

	public void setTalking(boolean talking)
	{
		if (!portraitFound)
			return;

		if (talking && !isTalking()) {
			talkAnim.resetCurrentAnimation();
		}
		isTalking = talking;
		talkAnim.setLoops(true);
	}
	
	public void stopTalkingAfterAnimationComplete() {
		if (!portraitFound)
			return;

		talkAnim.setLoops(false);
	}

	public boolean isTalking() {
		return isTalking;
	}
}
