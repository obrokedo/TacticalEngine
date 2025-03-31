package tactical.game.menu.devel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import tactical.game.sprite.CombatSprite;

public class BattleLayout implements Serializable {
	ArrayList<CombatSprite> combatSprites = new ArrayList<>();
	int currentTurn;
	
	public BattleLayout(ArrayList<CombatSprite> combatSprites, CombatSprite currentTurn) {
		this.combatSprites = combatSprites;
		this.currentTurn = combatSprites.indexOf(currentTurn);	
	}
	
	public byte[] serialize(BattleLayout layout) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		OutputStream buffer = new BufferedOutputStream(bytes);
		ObjectOutput output = new ObjectOutputStream(buffer);
		output.writeObject(layout);
		output.flush();
		return bytes.toByteArray();
	}
	
	public static BattleLayout deserializeBattleLayout(byte[] layout) throws IOException, ClassNotFoundException {
		ByteArrayInputStream file = new ByteArrayInputStream(layout);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream (buffer);

		return (BattleLayout) input.readObject();
	}
}