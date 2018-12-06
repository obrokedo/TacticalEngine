package tactical.engine.message;

import java.util.ArrayList;

import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.Sprite;

/**
 * A reusuable message that takes a custom message type and has an associated
 * CombatSprite or list of CombatSprites that should be related to the message
 *
 * @author Broked
 *
 */
public class SpriteContextMessage extends Message
{
	private static final long serialVersionUID = 1L;

	private ArrayList<Integer> sprites;

	public SpriteContextMessage(MessageType messageType, int[] heroIds, Object erasureSucks)
	{
		super(messageType);
		this.sprites = new ArrayList<Integer>();
		for (int hId : heroIds)
			sprites.add(hId);
	}
	
	public SpriteContextMessage(MessageType messageType, CombatSprite sprite)
	{
		super(messageType);
		this.sprites = new ArrayList<Integer>();
		sprites.add(sprite.getId());
	}
	
	public SpriteContextMessage(MessageType messageType, ArrayList<AnimatedSprite> sprites)
	{
		super(messageType);
		this.sprites = new ArrayList<Integer>();
		for (AnimatedSprite cs : sprites)
			this.sprites.add(cs.getId());
	}

	public CombatSprite getSprite(Iterable<Sprite> sprites) {
		for (Sprite s : sprites)
		{
			if (s.getId() == this.sprites.get(0))
				return (CombatSprite) s;
		}
		return null;
	}

	public CombatSprite getCombatSprite(Iterable<CombatSprite> sprites) {
		for (Sprite s : sprites)
		{
			if (s.getId() == this.sprites.get(0))
				return (CombatSprite) s;
		}
		return null;
	}

	public <T extends Sprite> ArrayList<T> getSprites(Iterable<T> sprites) {
		ArrayList<T> cSprites = new ArrayList<>();
		for (T s : sprites)
		{
			for (Integer id : this.sprites)
			{
				if (id == s.getId())
				{
					cSprites.add(s);
					break;
				}
			}
		}
		return cSprites;
	}
	
	

	public ArrayList<Integer> getSpriteIds() {
		return sprites;
	}
}
