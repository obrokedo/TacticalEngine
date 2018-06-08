package tactical.game.resource;

import tactical.game.sprite.NPCSprite;

public class NPCResource
{
	public static int NPC_ID_COUNTER = -1;

	public static NPCSprite getNPC(String animation, int textId, String name, 
			boolean throughWall, boolean animate, boolean turnOnTalk)
	{
		return new NPCSprite(animation, textId, NPC_ID_COUNTER--, name, throughWall,
				animate, turnOnTalk);
	}

	public static void resetNPCIds()
	{
		NPC_ID_COUNTER = -1;
	}
}
