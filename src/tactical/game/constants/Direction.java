package tactical.game.constants;

public enum Direction
{
	UP,
	DOWN,
	LEFT,
	RIGHT;

	public static Direction getDirectionFromInt(int dir) {
		if (dir == 0)
			return Direction.UP;
		else if (dir == 1)
			return Direction.LEFT;
		else if (dir == 2)
			return Direction.RIGHT;
		else if (dir == 3)
			return Direction.DOWN;
		return null;
	}
}
