package tactical.engine.config;

import java.io.Serializable;

import tactical.game.battle.BattleEffect;

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
public abstract class BattleEffectFactory implements Serializable
{
	private static final long serialVersionUID = 1L;

	public abstract String[] getBattleEffectList();
	
	/**
	 * Creates a BattleEffect for an effect with the specified name and level.
	 * <p/>
	 * This method is marked "abstract" which means that Jython classes that extend
	 * JBattleEffect MUST implement this method defined like this (parameter names can differ):
	 * <p/>
	 * <i>def createEffect(self, id, level):</i>
	 * 
	 * @param battleEffectId a string indicating the name of the battle effect to be created.
	 * 		This name will be one that appears in the list returned via the <i>getBattleEffectList()</i>
	 * 		method.
	 * @param level an integer representing the level of the effect to instantiate
	 * @see JBattleEffect.getBattleEffectList()
	 * @return a JBattleEffect that describes how an effect with the given battleEffectId and level
	 * 		should be managed.
	 */
	public abstract BattleEffect createEffect(String battleEffectId, int level);
}
