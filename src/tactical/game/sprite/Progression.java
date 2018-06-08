package tactical.game.sprite;

import java.io.Serializable;
import java.util.ArrayList;

import tactical.game.constants.AttributeStrength;

public class Progression implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int[] usuableWeapons;
	private int[] usuableArmor;
	private int move;
	private Object[] attack;
	private Object[] defense;
	private Object[] speed;
	private Object[] hp;
	private Object[] mp;
	// Battle Action stats
	private AttributeStrength 	counterStrength, evadeStrength,
								doubleStrength, critStrength,
								bodyStrength, mindStrength;

	// Elemental Affinities
	private int 				fireAffin, elecAffin, coldAffin,
								darkAffin, waterAffin, earthAffin,
								windAffin, lightAffin;
	private String bodyProgression, mindProgression;
	private String movementType;
	private int specialPromotionItemId;
	private String className;
	
	private ArrayList<String> spellIds;
	private ArrayList<int[]> spellLevelLearned;

	public Progression(int[] usuableWeapons, int[] usuableArmor, int move, String movementType,
			Object[] attackGains, Object[] defenseGains, Object[] speedGains, Object[] hpGains,
			Object[] mpGains, int fireAffin, int elecAffin,
			int coldAffin, int darkAffin, int waterAffin, int earthAffin,
			int windAffin, int lightAffin, AttributeStrength counterStrength, AttributeStrength evadeStrength,
			AttributeStrength doubleStrength, AttributeStrength critStrength, AttributeStrength bodyStrength,
			AttributeStrength mindStrength, String bodyProgression, String mindProgression, ArrayList<String> spellIds,
			 ArrayList<int[]> spellLevelLearned, int specialPromotionItemId, String className) {
		super();
		this.usuableWeapons = usuableWeapons;
		this.usuableArmor = usuableArmor;
		this.move = move;
		this.movementType = movementType;
		this.attack = attackGains;
		this.defense = defenseGains;
		this.speed = speedGains;
		this.hp = hpGains;
		this.mp = mpGains;
		this.className = className;
		this.counterStrength = counterStrength;
		this.evadeStrength = evadeStrength;
		this.doubleStrength = doubleStrength;
		this.critStrength = critStrength;
		this.bodyStrength = bodyStrength;
		this.mindStrength = mindStrength;
		this.fireAffin = fireAffin;
		this.elecAffin = elecAffin;
		this.coldAffin = coldAffin;
		this.darkAffin = darkAffin;
		this.waterAffin = waterAffin;
		this.earthAffin = earthAffin;
		this.windAffin = windAffin;
		this.lightAffin = lightAffin;
		this.bodyProgression = bodyProgression;
		this.mindProgression = mindProgression;
		this.spellIds = spellIds;
		this.spellLevelLearned = spellLevelLearned;
		this.specialPromotionItemId = specialPromotionItemId;
	}

	public int[] getUsuableWeapons() {
		return usuableWeapons;
	}
	public int[] getUsuableArmor() {
		return usuableArmor;
	}
	public int getMove() {
		return move;
	}
	public Object[] getAttack() {
		return attack;
	}
	public Object[] getDefense() {
		return defense;
	}
	public Object[] getSpeed() {
		return speed;
	}
	public Object[] getHp() {
		return hp;
	}
	public Object[] getMp() {
		return mp;
	}

	public AttributeStrength getCounterStrength() {
		return counterStrength;
	}

	public AttributeStrength getEvadeStrength() {
		return evadeStrength;
	}

	public AttributeStrength getDoubleStrength() {
		return doubleStrength;
	}

	public AttributeStrength getCritStrength() {
		return critStrength;
	}

	public AttributeStrength getBodyStrength() {
		return bodyStrength;
	}

	public AttributeStrength getMindStrength() {
		return mindStrength;
	}

	public int getFireAffin() {
		return fireAffin;
	}

	public int getElecAffin() {
		return elecAffin;
	}

	public int getColdAffin() {
		return coldAffin;
	}

	public int getDarkAffin() {
		return darkAffin;
	}

	public int getWaterAffin() {
		return waterAffin;
	}

	public int getEarthAffin() {
		return earthAffin;
	}

	public int getWindAffin() {
		return windAffin;
	}

	public int getLightAffin() {
		return lightAffin;
	}

	public String getMovementType() {
		return movementType;
	}
	public String getClassName() {
		return className;
	}
	public String getBodyProgression() {
		return bodyProgression;
	}
	public String getMindProgression() {
		return mindProgression;
	}

	public ArrayList<String> getSpellIds() {
		return spellIds;
	}

	public ArrayList<int[]> getSpellLevelLearned() {
		return spellLevelLearned;
	}

	public int getSpecialPromotionItemId() {
		return specialPromotionItemId;
	}
}