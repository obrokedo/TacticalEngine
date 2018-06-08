package tactical.game;

import org.newdawn.slick.util.Log;

import tactical.game.exception.BadResourceException;
import tactical.game.move.AttackableSpace;

public enum Range {
	SELF_ONLY,
	ONE_ONLY,
	TWO_AND_LESS,
	THREE_AND_LESS,
	TWO_NO_ONE,
	THREE_NO_ONE,
	THREE_NO_ONE_OR_TWO;

	public boolean isInDistance(int range)
	{
		boolean inDistance = false;

		if (range == 1)
		{
			switch (this)
			{
				case ONE_ONLY:
				case TWO_AND_LESS:
				case THREE_AND_LESS:
					inDistance = true;
					break;
				default:
					inDistance = false;
					break;
			}
		}
		else if (range == 2)
		{
			switch (this)
			{
				case TWO_AND_LESS:
				case THREE_AND_LESS:
				case TWO_NO_ONE:
				case THREE_NO_ONE:
					inDistance = true;
					break;
				default:
					inDistance = false;
					break;
			}
		}
		else if (range == 3)
		{
			switch (this)
			{
				case THREE_AND_LESS:
				case THREE_NO_ONE:
				case THREE_NO_ONE_OR_TWO:
					inDistance = true;
					break;
				default:
					inDistance = false;
					break;
			}
		}

		Log.debug("Check is in distance. My range: " + this + " Check at range: " + range + " In Distance: " + inDistance);

		return inDistance;
	}

	public int[][] getAttackableSpace()
	{
		switch (this)
		{
			case SELF_ONLY:
				return AttackableSpace.AREA_0;
			case ONE_ONLY:
				return AttackableSpace.AREA_1;
			case TWO_AND_LESS:
				return AttackableSpace.AREA_2;
			case THREE_AND_LESS:
				return AttackableSpace.AREA_3;
			case TWO_NO_ONE:
				return AttackableSpace.AREA_2_NO_1;
			case THREE_NO_ONE:
				return AttackableSpace.AREA_3_NO_1;
			case THREE_NO_ONE_OR_TWO:
				return AttackableSpace.AREA_3_NO_1_2;
		}

		return null;
	}

	public int getMaxRange()
	{
		switch (this)
		{
			case THREE_AND_LESS:
			case THREE_NO_ONE:
			case THREE_NO_ONE_OR_TWO:
				return 3;
			case TWO_NO_ONE:
			case TWO_AND_LESS:
				return 2;
			case ONE_ONLY:
				return 1;
			default:
				return 0;
		}
	}

	public static Range convertIntToRange(int rangeInt)
	{
		switch (rangeInt)
		{
			case 0:
				return SELF_ONLY;
			case 1:
				return ONE_ONLY;
			case 2:
				return TWO_AND_LESS;
			case 3:
				return THREE_AND_LESS;
			case 4:
				return TWO_NO_ONE;
			case 5:
				return THREE_NO_ONE;
			case 6:
				return THREE_NO_ONE_OR_TWO;
			default:
				throw new BadResourceException("Attempted to create a resource with an illegal range");
		}
	}
}
