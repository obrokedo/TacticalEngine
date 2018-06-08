package tactical.game.combat;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.game.battle.BattleEffect;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;

public class DamagedCombatAnimation extends CombatAnimation
{
	private static final Color DAMAGE_COLOR = new Color(204, 102, 0, 200);

	private CombatAnimation childAnimation;
	private int hpDamage;
	private int mpDamage;
	private ArrayList<BattleEffect> battleEffects;
	private CombatSprite attacker;
	private boolean isNegativeEffect = false;
	private int battleResultIndex;

	public DamagedCombatAnimation(CombatAnimation childAnimation, int hpDamage, int mpDamage,
			ArrayList<BattleEffect> battleEffects, CombatSprite attacker, int battleResultIndex)
	{
		super();
		this.minimumTimePassed = 200;
		this.childAnimation = childAnimation;
		this.hpDamage = hpDamage;
		this.mpDamage = mpDamage;
		this.battleEffects = battleEffects;
		this.attacker = attacker;
		this.battleResultIndex = battleResultIndex;
		if (hpDamage < 0 || mpDamage < 0)
			isNegativeEffect =  true;
		
		for (BattleEffect eff : battleEffects)
		{
			if (eff.isNegativeEffect())
				isNegativeEffect = true;
		}
	}

	@Override
	public boolean update(int delta)
	{
		this.totalTimePassed += delta;
		childAnimation.update(delta);
		if (isNegativeEffect)
		{
			childAnimation.xOffset = (int) ((TacticalGame.RANDOM.nextInt(20) - 10));
			childAnimation.yOffset = (int) ((TacticalGame.RANDOM.nextInt(20) - 10));
		}
		return totalTimePassed >= minimumTimePassed;
	}

	@Override
	public void render(PaddedGameContainer fcCont, Graphics g, int yDrawPos, float scale) {
		childAnimation.render(fcCont, g, yDrawPos, scale);
	}

	@Override
	public void initialize() {
		if (isNegativeEffect)
			childAnimation.renderColor = DAMAGE_COLOR;

		childAnimation.parentSprite.modifyCurrentHP(hpDamage);
		childAnimation.parentSprite.modifyCurrentMP(mpDamage);
		for (BattleEffect battleEffect : battleEffects)
		{
			// If the effect is already done then it is instantaneous so don't bother adding it to the target
			if (!battleEffect.isDone())
				childAnimation.parentSprite.addBattleEffect(battleEffect);
			battleEffect.effectStarted(attacker, childAnimation.parentSprite);
		}
	}

	@Override
	public CombatSprite getParentSprite() {
		return childAnimation.getParentSprite();
	}

	@Override
	public boolean isDrawSpell() {
		return childAnimation.isDrawSpell();
	}

	@Override
	public boolean isDamaging() {
		return true;
	}

	public boolean willSpriteDie()
	{
		return childAnimation.parentSprite.getCurrentHP() + hpDamage <= 0;
	}

	public int getBattleResultIndex() {
		return battleResultIndex;
	}
}
