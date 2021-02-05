package tactical.game.ai;

import java.awt.Point;
import java.util.ArrayList;

import tactical.game.sprite.CombatSprite;
import tactical.game.turnaction.AttackSpriteAction;

public class AIConfidence {
	public int confidence;
	public boolean willKill = false;
	public boolean willHeal = false;
	public boolean foundHero = false;
	public CombatSprite target = null;
	public Point attackPoint = null;
	public AttackSpriteAction potentialAttackSpriteAction = null;
	public int allyInfluence;
	public int enemyInfluence;
	public int damageInfluence;
	public int distanceInfluence;
	public int landInfluence;
	// Spells
	public ArrayList<AISpellConfidence> aiSpellConfs;

	public AIConfidence(int confidence) {
		super();
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "AIConfidence [confidence=" + confidence + " willKill=" + willKill + " willHeal=" + willHeal
				+ " foundHero=" + foundHero + " target=" + (target != null ? target.getName() : "none") + 
				", attackPoint=Tile" + attackPoint.x + "," + attackPoint.y
				+ " potentialAttackSpriteAction=" + potentialAttackSpriteAction.getBattleCommand().toString() + " allyInfluence=" + allyInfluence
				+ " enemyInfluence=" + enemyInfluence + " damageInfluence=" + damageInfluence + " distanceInfluence=" + distanceInfluence 
				+ " landInfluence=" + landInfluence +" aiSpellConfs=" + aiSpellConfs + "]";
	}

	
}
