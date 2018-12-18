package tactical.game.ai;

public class AISpellConfidence {
	public String name;
	public int level;
	public int mpCost;
	public int distanceFromTarget;
	public int damageInfluence;
	public int targetAmtDivisor;
	public int selfHealBonus = 0;
	
	public AISpellConfidence(String name, int level) {
		super();
		this.name = name;
		this.level = level;
	}

	@Override
	public String toString() {
		return "AISpellConfidence [name=" + name + ", level=" + level + ", mpCost=" + mpCost + ", distanceFromTarget="
				+ distanceFromTarget + ", damageInfluence=" + damageInfluence + ", targetAmtDivisor=" + targetAmtDivisor
				+ ", selfHealBonus=" + selfHealBonus + "]";
	}
}
