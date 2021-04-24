package tactical.renderer;

import org.newdawn.slick.Graphics;

import tactical.engine.message.Message;
import tactical.game.Camera;
import tactical.game.manager.Manager;
import tactical.game.ui.PaddedGameContainer;
import tactical.map.Map;
import tactical.map.MapLayer;
import tactical.map.Roof;

public class TileMapRenderer extends Manager
{
	private Map map;

	public TileMapRenderer() {}

	@Override
	public void initialize() {
		this.map = this.stateInfo.getResourceManager().getMap();
	}
	
	public void render(float xOffset, float yOffset, Camera camera, Graphics g, PaddedGameContainer gc)
	{	
		int maxScreenTilesX = camera.getViewportWidth() / map.getTileRenderWidth() + 1;
		int maxScreenTilesY = camera.getViewportHeight() / map.getTileRenderHeight() + 1;

		int lastTileX;
		int lastTileY;

		lastTileX =  (int) Math.min(map.getMapWidth(),
				camera.getLocationX() / map.getTileRenderWidth() + maxScreenTilesX + (xOffset == 0 ? 0 : 1));

		lastTileY =  (int) Math.min(map.getMapHeight(),
				camera.getLocationY() / map.getTileRenderHeight() + maxScreenTilesY + (yOffset == 0 ? 0 : 1));
		int camX = (int) camera.getLocationX() / map.getTileRenderWidth();
		int camY = (int) camera.getLocationY() / map.getTileRenderHeight();
		
		for (int mapX =  camX, frameX = 0; mapX < lastTileX; mapX++, frameX++)
		{
			for (int mapY =  camY, frameY = 0; mapY < lastTileY; mapY++, frameY++)
			{
				if (mapY >= 0 && mapX >= 0) {
					renderLayer(map.getMapLayer(0), mapX, mapY, frameX, frameY, xOffset, yOffset);
					/*
					map.renderSprite(map.getMapLayer(0).getTiles()[mapY][mapX],
						frameX * map.getTileRenderWidth() - xOffset, frameY * map.getTileRenderHeight() - yOffset); */
	
					for (MapLayer ml : map.getFlashingLayersByPosition(1))
						renderLayer(ml, mapX, mapY, frameX, frameY, xOffset, yOffset);
					
					renderLayer(map.getMapLayer(1), mapX, mapY, frameX, frameY, xOffset, yOffset);
					
					for (MapLayer ml : map.getFlashingLayersByPosition(2))
						renderLayer(ml, mapX, mapY, frameX, frameY, xOffset, yOffset);
					
					renderLayer(map.getMapLayer(2), mapX, mapY, frameX, frameY, xOffset, yOffset);
					
					for (MapLayer ml : map.getFlashingLayersByPosition(3))
						renderLayer(ml, mapX, mapY, frameX, frameY, xOffset, yOffset);
				}
			}
		}

		map.endUse();
	}
	
	private void renderLayer(MapLayer layer, int mapX, int mapY, int frameX, int frameY, float xOffset, float yOffset)
	{
		if (!layer.isVisibleFlashing())
			return;
		
		if (layer.getTiles()[mapY][mapX] != 0)
			map.renderSprite(layer.getTiles()[mapY][mapX],
				frameX * map.getTileRenderWidth() - xOffset, frameY * map.getTileRenderHeight() - yOffset);
	}

	public void renderForeground(float xOffset, float yOffset, Camera camera, Graphics g, PaddedGameContainer gc)
	{
		int maxScreenTilesX = camera.getViewportWidth() / map.getTileRenderWidth() + 1;
		int maxScreenTilesY = camera.getViewportHeight() / map.getTileRenderHeight() + 1;

		int lastTileX;
		int lastTileY;

		lastTileX = (int) Math.min(map.getMapWidth(),
				camera.getLocationX() / map.getTileRenderWidth() + maxScreenTilesX + (xOffset == 0 ? 0 : 1));

		lastTileY = (int) Math.min(map.getMapHeight(),
				camera.getLocationY() / map.getTileRenderHeight() + maxScreenTilesY + (yOffset == 0 ? 0 : 1));

		int camX = (int) camera.getLocationX() / map.getTileRenderWidth();
		int camY = (int) camera.getLocationY() / map.getTileRenderHeight();
		
		for (int mapX = camX, frameX = 0; mapX < lastTileX; mapX++, frameX++)
		{
			for (int mapY = camY, frameY = 0; mapY < lastTileY; mapY++, frameY++)
			{
				if (mapY >= 0 && mapX >= 0) { 
					for (int mapLayer = 3; mapLayer < map.getMapLayerAmount(); mapLayer++) {
						renderLayer(map.getMapLayer(mapLayer), mapX, mapY, frameX, frameY, xOffset, yOffset);
						for (MapLayer ml : map.getFlashingLayersByPosition(mapLayer + 1))
							renderLayer(ml, mapX, mapY, frameX, frameY, xOffset, yOffset);
					}
				}
			}
		}

		if (map.getRoofLayer() != null) {
			for (Roof roof : map.getRoofIterator())
			{				
				if (!roof.isVisible()) {
					continue;
				}
				for (int mapX = (int) roof.getRoofShape().getMinX() / map.getTileRenderWidth(); 
						mapX < roof.getRoofShape().getMaxX() / map.getTileRenderWidth(); mapX++)
				{
					for (int mapY = (int) roof.getRoofShape().getMinY() / map.getTileRenderHeight(); 
							mapY < roof.getRoofShape().getMaxY() / map.getTileRenderHeight(); mapY++)
					{
						if (mapY >= 0 && mapX >= 0) {
							if (map.getRoofLayer().getTiles()[mapY][mapX] != 0)
							{
								map.renderSprite(map.getRoofLayer().getTiles()[mapY][mapX],
									mapX * map.getTileRenderWidth() - camera.getLocationX(),
										mapY * map.getTileRenderHeight() - camera.getLocationY());
							}
							if (map.getRoofShadowLayer() != null && map.getRoofShadowLayer().getTiles()[mapY][mapX] != 0)
							{
								map.renderSprite(map.getRoofShadowLayer().getTiles()[mapY][mapX],
									mapX * map.getTileRenderWidth() - camera.getLocationX(),
										mapY * map.getTileRenderHeight() - camera.getLocationY());
							}
						}
					}
				}
			}
		}	

		map.endUse();
	}

	@Override
	public void recieveMessage(Message message) {

	}
}
