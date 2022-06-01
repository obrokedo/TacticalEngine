package tactical.utils.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class ReferenceStore {
	public static final int REFERS_NONE = 0;
	public static final int REFERS_TRIGGER = 1;
	public static final int REFERS_CINEMATIC = 2;
	public static final int REFERS_TEXT = 3;
	public static final int REFERS_HERO = 4;
	public static final int REFERS_ENEMY = 5;
	public static final int REFERS_ITEM = 6;
	public static final int REFERS_QUEST = 7;
	public static final int REFERS_AI_APPROACH = 8;
	public static final int REFERS_STAT_GAINS = 9;
	public static final int REFERS_ITEM_STYLE = 10;
	public static final int REFERS_ITEM_TYPE = 11;
	public static final int REFERS_ITEM_RANGE = 12;
	public static final int REFERS_MOVE_TYPE = 13;
	public static final int REFERS_SPELL = 14;
	public static final int REFERS_ITEM_AREA = 15;
	public static final int REFERS_DIRECTION = 16;
	public static final int REFERS_ANIMATIONS = 17;
	public static final int REFERS_MAP = 18;
	public static final int REFERS_SPRITE_IMAGE = 19;
	public static final int REFERS_EFFECT = 20;
	public static final int REFERS_ATTRIBUTE_STRENGTH = 21;
	public static final int REFERS_BODYMIND_GAIN = 22;
	public static final int REFERS_AI = 23;
	public static final int REFERS_TERRAIN = 24;
	public static final int REFERS_PALETTE = 25;
	// Extended weapon stats
	public static final int REFERS_WEAPON_DAMAGE_TYPE = 26;
	public static final int REFERS_AFFINITIES = 27;
	public static final int REFERS_CONDITIONS = 28;
	
	public static final int REFERS_OPERATOR = 29;
	public static final int REFERS_LOCATIONS = 30;
	public static final int REFERS_MAPDATA = 31;
	public static final int REFERS_MUSIC = 32;
	public static final int REFERS_SOUND = 33;
	public static final int REFERS_WEAPON_ANIMATIONS = 34;
	
	private Hashtable<Integer, List<PlannerReference>> referenceListByReferenceType;

	public ReferenceStore() {
		super();
		this.referenceListByReferenceType = new Hashtable<>();
	}
	
	private List<PlannerReference> getOrCreateList(int refersType) {
		List<PlannerReference> refs = null;
		if (referenceListByReferenceType.containsKey(refersType)) {
			refs = referenceListByReferenceType.get(refersType);
		} else {
			refs = new ArrayList<>();
			referenceListByReferenceType.put(refersType, refs);
		}
		
		return refs;
	}
	
	public void addReference(int refersType, PlannerReference... references) {
		List<PlannerReference> refs = getOrCreateList(refersType);
		
		for (PlannerReference ref : references)
			refs.add(ref);
	}
	
	public void addReference(int refersType, String... references) {
		List<PlannerReference> refs = getOrCreateList(refersType);
		
		for (String ref : references)
			refs.add(new PlannerReference(ref));
	}
	
	public void addReference(int refersType, List<PlannerReference> references) {
		List<PlannerReference> refs = getOrCreateList(refersType);
		
		refs.addAll(references);
	}
	
	public List<PlannerReference> getReferencesForType(int refersType) {
		return getOrCreateList(refersType);
	}
	
	public List<String> getReferencesAsStrings(int refersType) {
		
		return getOrCreateList(refersType).stream().map(referTo -> referTo.getName()).collect(Collectors.toList());		
	}
	
	public PlannerReference removeReference(int refersType, int referenceIndex) {
		PlannerReference ref = null;
		
		ref = referenceListByReferenceType.get(refersType).remove(referenceIndex);
		
		return ref;
	}
	
	public void clearReferenceList(int refersType) {
		if (referenceListByReferenceType.containsKey(refersType))
			referenceListByReferenceType.get(refersType).clear();
	}
}
