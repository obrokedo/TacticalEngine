package tactical.game.sprite;

import java.util.Comparator;

public class SpriteZComparator implements Comparator<Sprite> {

	@Override
	public int compare(Sprite o1, Sprite o2) {
		return (int) o1.getLocY() - (int) o2.getLocY();
	}
}
