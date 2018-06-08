package tactical.game.listener;

import tactical.engine.state.StateInfo;

public interface MouseListener 
{
	public static final int ORDER_BATTLE_ACTIONS = 0;
	public static final int ORDER_INIT = 0;	
	public static final int ORDER_NPC = 0;
	public static final int ORDER_MAP_MOVE = 1;	
	public static final int ORDER_ATTACKABLE_SPACE = 1;
	public static final int ORDER_SPRITE = 2;
	public static final int ORDER_MOVEABLE_SPACE = 3;
	
	public boolean mouseUpdate(int frameMX, int frameMY, int mapMX, int mapMY, 
			boolean leftClicked, boolean rightClicked, StateInfo stateInfo);

	public int getZOrder();
}
