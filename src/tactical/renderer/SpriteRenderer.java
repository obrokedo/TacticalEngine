package tactical.renderer;

import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.message.Message;
import tactical.game.dev.BattleOptimizer;
import tactical.game.manager.Manager;
import tactical.game.sprite.Sprite;

public class SpriteRenderer extends Manager
{
	public void render(Graphics g)
	{
		if (TacticalGame.BATTLE_MODE_OPTIMIZE)
			BattleOptimizer.render(g);
		for (Sprite s : stateInfo.getSprites())
		{
			if (s.isVisible())
			{
				s.render(stateInfo.getCamera(), g, stateInfo.getPaddedGameContainer(), stateInfo.getTileHeight());
				/*
				switch (s.getSpriteType())
				{
					case Sprite.TYPE_STATIC_SPRITE:
						break;
					case Sprite.TYPE_COMBAT:
						break;
					case Sprite.TYPE_NPC:
						break;
				}
				*/
			}
		}
	}

	@Override
	public void initialize() {}

	@Override
	public void recieveMessage(Message message) {

	}
}
