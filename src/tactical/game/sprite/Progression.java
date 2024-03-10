package tactical.game.sprite;

import java.io.Serializable;
import java.util.ArrayList;

import tactical.game.battle.spell.SpellDefinition;
import tactical.game.constants.AttributeStrength;

public class Progression implements Serializable
{
	private static final long serialVersionUID = 1L;

	protected int[] usuableWeapons;
	protected int[] usuableArmor;
	protected int move;
	protected Object[] attack;
	protected Object[] defense;
	protected Object[] speed;
	protected Object[] hp;
	protected Object[] mp;	
	protected String movementType;
	protected int specialPromotionItemId;
	protected String className, classDescription;
	
	protected ArrayList<String> spellIds;
	protected ArrayList<int[]> spellLevelLearned;

	public Progression(int[] usuableWeapons, int[] usuableArmor, int move, String movementType,
			Object[] attackGains, Object[] defenseGains, Object[] speedGains, Object[] hpGains,
			Object[] mpGains, ArrayList<String> spellIds,
			 ArrayList<int[]> spellLevelLearned, int specialPromotionItemId, String className, String classDescription) {
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
		this.spellIds = spellIds;
		this.spellLevelLearned = spellLevelLearned;
		this.specialPromotionItemId = specialPromotionItemId;
		this.classDescription = classDescription;
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
	
	public String getMovementType() {
		return movementType;
	}
	public String getClassName() {
		return className;
	}

	public String getClassDescription() {
		return classDescription;
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