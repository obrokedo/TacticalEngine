package tactical.game.definition;

import tactical.game.sprite.Progression;
import tactical.utils.XMLParser.TagArea;

public final class DefaultHeroDefinition extends HeroDefinition {
	public DefaultHeroDefinition(TagArea tagArea) {
		super(tagArea);
	}
	
	@Override
	protected Progression getProgression(int index) {
		return new Progression(usuableWeapons[index], null, move[index], movementType[index],
				new Object[] {attackGain[index], attackStart[index], attackEnd[index]},
				new Object[] {defenseGain[index], defenseStart[index], defenseEnd[index]},
				new Object[] {speedGain[index], speedStart[index], speedEnd[index]},
				new Object[] {hpGain[index], hpStart[index], hpEnd[index]},
				new Object[] {mpGain[index], mpStart[index], mpEnd[index]}, 
				spellIds.get(index), spellsPerLevel.get(index),
				specialPromotionItemId[index], className[index], classDescription[index]);
	}

	@Override
	protected void parseCustomHeroDefinition(int maxProgressions) {
		
		
	}

	@Override
	protected void parseCustomHeroProgression(int index, TagArea childTagArea) {
		
		
	}
}
