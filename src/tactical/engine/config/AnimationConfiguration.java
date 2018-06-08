package tactical.engine.config;

import tactical.game.constants.Direction;

public class AnimationConfiguration {
	public static String getUpAnimationName() {
		return "Up";
	}
	
	public static String getDownAnimationName() {
		return "Down";
	}
	
	public static String getLeftAnimationName() {
		return "Left";
	}
	
	public static String getRightAnimationName() {
		return "Right";
	}
	
	public static String getUnpromotedUpAnimationName() {
		return getUnpromotedPrefix() + getUpAnimationName();
	}
	
	public static String getUnpromotedDownAnimationName() {
		return getUnpromotedPrefix() + getDownAnimationName();
	}
	
	public static String getUnpromotedLeftAnimationName() {
		return getUnpromotedPrefix() + getLeftAnimationName();
	}
	
	public static String getUnpromotedRightAnimationName() {
		return getUnpromotedPrefix() + getRightAnimationName();
	}
	
	public static String getPromotedUpAnimationName() {
		return getPromotedPrefix() + getUpAnimationName();
	}
	
	public static String getPromotedDownAnimationName() {
		return getPromotedPrefix() + getDownAnimationName();
	}
	
	public static String getPromotedLeftAnimationName() {
		return getPromotedPrefix() + getLeftAnimationName();
	}
	
	public static String getPromotedRightAnimationName() {
		return getPromotedPrefix() + getRightAnimationName();
	}
	
	public static String getPromotedPrefix() {
		return "Pro";
	}
	
	public static String getUnpromotedPrefix() {
		return "Un";
	}
	
	public static String getUnpromotedDirection(Direction dir) {
		switch (dir)
		{
			case DOWN:
				return getUnpromotedDownAnimationName();
			case LEFT:
				return getUnpromotedLeftAnimationName();
			case RIGHT:
				return getUnpromotedRightAnimationName();
			case UP:
				return getUnpromotedUpAnimationName();
		}
		return null;
	}
	
	public static String getPromotedDirection(Direction dir) {
		switch (dir)
		{
			case DOWN:
				return getPromotedDownAnimationName();
			case LEFT:
				return getPromotedLeftAnimationName();
			case RIGHT:
				return getPromotedRightAnimationName();
			case UP:
				return getPromotedUpAnimationName();
		}
		return null;
	}
}
