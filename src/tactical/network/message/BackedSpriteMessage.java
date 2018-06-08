package tactical.network.message;

import java.util.ArrayList;

import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.game.sprite.CombatSprite;

public class BackedSpriteMessage extends Message {
	private static final long serialVersionUID = 1L;

	private ArrayList<CombatSprite> sprites = new ArrayList<>();

	public BackedSpriteMessage(ArrayList<CombatSprite> sprites){
		super(MessageType.CLIENT_BROADCAST_HERO);
		this.sprites = sprites;
	}

	public ArrayList<CombatSprite> getSprites() {
		return sprites;
	}
}
