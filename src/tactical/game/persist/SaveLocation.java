package tactical.game.persist;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tactical.engine.message.LoadMapMessage;
import tactical.engine.state.StateInfo;
import tactical.game.sprite.CombatSprite;

@NoArgsConstructor
@Getter
@Setter
public class SaveLocation implements Serializable {
	// This value is the map that was last loaded from, the priest was saved at or
	// an egress location was set
	private String lastSaveMapData;
	private Point inTownPoint;
	
	private LoadMapMessage chapterSaveMessage;
	
	// Battle save
	private ArrayList<CombatSprite> battleEnemySprites;
	private ArrayList<Integer> battleHeroSpriteIds;
	private Integer currentTurn;
	
	public ArrayList<CombatSprite> getBattleSprites(StateInfo stateInfo) {
		for (Integer heroID : battleHeroSpriteIds)
		{
			this.battleEnemySprites.add(stateInfo.getHeroById(heroID));
		}
		
		return battleEnemySprites;
	}
}
