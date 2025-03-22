package tactical.game.battle;

import tactical.engine.TacticalGame;
import tactical.game.sprite.CombatSprite;

public class SerializedBattleEffect extends BattleEffect {
	
	public SerializedBattleEffect(BattleEffect jythonBattleEffect) {
		super();
		this.currentTurn = jythonBattleEffect.currentTurn;
		this.effectChance = jythonBattleEffect.effectChance;
		this.effectLevel = jythonBattleEffect.effectLevel;
		this.battleEffectId = jythonBattleEffect.battleEffectId;
	}
	
	public BattleEffect getJythonBattleEffect() {
		BattleEffect effect = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(battleEffectId, effectLevel);
		effect.currentTurn = this.currentTurn;
		return effect;
	}

	@Override
	protected void performEffectImpl(CombatSprite target, int currentTurn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String performEffectText(CombatSprite target, int currentTurn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void effectStarted(CombatSprite attacker, CombatSprite target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String effectStartedText(CombatSprite attacker, CombatSprite target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String effectEnded(CombatSprite target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAnimationFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEffected(CombatSprite target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegativeEffect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getIconName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemainingTurns() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
