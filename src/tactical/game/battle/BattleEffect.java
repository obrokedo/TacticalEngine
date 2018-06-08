package tactical.game.battle;

import java.io.Serializable;

import tactical.game.sprite.CombatSprite;
import tactical.loading.ResourceManager;
import tactical.utils.AnimationWrapper;

/**
 * Abstract class to be extended in Jython to create in interface that allows for creation of a 
 * BattleEffect via the Jython scripts. This class should be extended by any script that intends 
 * to create and specify values for a BattleEffect.
 * <p/>
 * BattleEffects are short lived statuses that can be applied to CombatSprites via weapons, spells
 * or items. They can grant positive and/or negative effects depending on how they are set up. 
 * BattleEffects are handled at the end of each effected CombatSprites turn and generally expire
 * in a set amount of turns.
 * <p/>
 * <b>Lifecycle of a JBattleEffect:</b><br>
 * <ul>
 * 	<li><b><i>createEffect</i></b> To create the specified effect with the given name and level</li>
 *  <li><b><i>isEffected</i></b> Determines if the effect should be applied to the given target. If not
 *  then the JBattleEffects "life" ends here.</li>
 *  <li><b><i>effectStartedText</i></b> It has been determined that the effect will be applied. Get the
 *  text that should be displayed to the user to indicate the effect was applied</li>
 *  <li><b><i>effectStarted</i></b> Initialize effect variables and effect the target with the initial
 *  effects of this JBattleEffect</li>
 *  <li><b><i>performEffectText<i></b>Performed at the end of the CombatSprite's turn until the effect is finished,
 *  retrieves the text that should be displayed to indicate that their is an ongoing effect.</li>
 *  <li><b><i>performEffectImpl<i></b> Performed at the end of the CombatSprite's turn until the effect is finished,
 *  perform any per-turn action on the CombatSprite</li>
 *  <li><b><i>effectEndedText<i></b> Get the text that describes that the effect has ended or been removed</li>
 *  <li><b><i>effectEnded<i></b> Reset any stats that this effect may have modified</li>
 * </ul>
 * 
 * <p/>
 * <b>Format for extending a Java class in Python: </b>
 * <p/>
 * <i>class ExampleScriptName(JBattleEffect):</i>
 * 
 * @author Broked
 */
public abstract class BattleEffect implements Serializable
{
	private static final long serialVersionUID = 1L;

	protected int currentTurn;
	protected int effectChance;
	protected int effectLevel;
	protected String battleEffectId;
	
	protected BattleEffect() {}
	
	public BattleEffect(String battleEffectId, int effectLevel) {
		this.battleEffectId = battleEffectId;
		this.effectLevel = effectLevel;
	}
	
	private transient AnimationWrapper effectAnimation;

	protected abstract void performEffectImpl(CombatSprite target, int currentTurn);
	
	public abstract String performEffectText(CombatSprite target, int currentTurn);

	public abstract void effectStarted(CombatSprite attacker, CombatSprite target);

	public abstract String effectStartedText(CombatSprite attacker, CombatSprite target);

	public abstract void effectEnded(CombatSprite target);
	
	public abstract String effectEndedText(CombatSprite target);

	public abstract String getAnimationFile();

	public abstract boolean isEffected(CombatSprite target);
	
	public abstract boolean isNegativeEffect();
	
	public abstract boolean isDone();
	
	public boolean preventsMovement() {
		return false;
	}
	
	public boolean preventsAttack(){
		return false;
	}
	
	public boolean preventsSpells(){
		return false;
	}
	
	public boolean preventsItems(){
		return false;
	}
	
	public boolean preventsTurn(){
		return false;
	}
	
	public boolean doesEffectPersistAfterBattle() {
		return false;
	}

	public void initializeAnimation(ResourceManager frm)
	{
		if (getAnimationFile() != null)
			effectAnimation = new AnimationWrapper(frm.getSpriteAnimation(getAnimationFile()), "Effect", true);
		else
			effectAnimation = null;
	}

	public void performEffect(CombatSprite target)
	{
		performEffectImpl(target, currentTurn);
	}
	
	public String getPerformEffectText(CombatSprite target)
	{
		return performEffectText(target, currentTurn);
	}
	
	public void setBattleEffectId(String battleEffectId) {
		this.battleEffectId = battleEffectId;
	}

	public String getBattleEffectId() {
		return battleEffectId;
	}

	public int getCurrentTurn() {
		return currentTurn;
	}

	public void incrementTurn() {
		this.currentTurn++;
	}
	public AnimationWrapper getEffectAnimation() {
		return effectAnimation;
	}

	public int getEffectChance() {
		return effectChance;
	}

	public void setEffectChance(int effectChance) {
		this.effectChance = effectChance;
	}

	public int getEffectLevel() {
		return effectLevel;
	}
	
	public void setEffectLevel(int level) {
		this.effectLevel = level;
	}
}
