package tactical.game.move;

import java.awt.Point;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import lombok.Getter;
import tactical.game.Camera;
import tactical.game.ui.PaddedGameContainer;
import tactical.map.Map;
import tactical.map.MapObject;

public class LitTilesLocation {
	protected boolean oscillateUp = true;
	protected int[][] moveableTiles;
	protected int topX, topY;
	protected Color colorToFlash;
	protected int tileWidth, tileHeight;
	protected Map map;
	@Getter protected Point center;
	
	
	public static LitTilesLocation fromMapObject(MapObject mapObject, Map map, int tilesWidth, int tilesHeight) {
		LitTilesLocation ltc = new LitTilesLocation();
		ltc.topX = mapObject.getX() / tilesWidth;
		ltc.topY = mapObject.getY() / tilesHeight;
		ltc.map = map;
		
		ltc.moveableTiles = new int[mapObject.getHeight() / tilesHeight][mapObject.getWidth() / tilesWidth];
		
		for (int y = 0; y < ltc.moveableTiles.length; y++)
		{
			for (int x = 0; x < ltc.moveableTiles[0].length; x++)
			{			
				ltc.moveableTiles[y][x] = -1;
			}
		}
		
		ltc.center = new Point(mapObject.getX() + mapObject.getWidth() / 2, mapObject.getY() + mapObject.getHeight() / 2);
		
		for (Point p : mapObject.getTilePositionsContained(tilesWidth, tilesHeight)) {
			ltc.moveableTiles[(p.y - mapObject.getY()) / tilesHeight][(p.x - mapObject.getX()) / tilesWidth] = 1;
		}
		
		ltc.tileWidth = tilesWidth;
		ltc.tileHeight = tilesHeight;
		ltc.colorToFlash = new Color(128, 128, 255, 40);
		return ltc;
	}
	
	public Point getFirstTile() {
		for (int i = 0; i < moveableTiles.length; i++) {
			for (int j = 0; j < moveableTiles[0].length; j++) {
				if (moveableTiles[i][j] > 0) {
					return new Point((j + topX) * tileWidth , (i + topY) * tileHeight);
				}
			}
		}
		return null;
	}
	
	public boolean isInLocation(int tX, int tY) {
		for (int y = 0; y < moveableTiles.length; y++)
		{
			for (int x = 0; x < moveableTiles[0].length; x++)
			{		
				if (topX + x == tX && topY + y == tY) {
					return moveableTiles[y][x] == 1;
				}
			}
		}
		return false;
	}
	
	public boolean toggleTileAtLocation(int tX, int tY) {
		for (int y = 0; y < moveableTiles.length; y++)
		{
			for (int x = 0; x < moveableTiles[0].length; x++)
			{		
				if (topX + x == tX && topY + y == tY) {
					moveableTiles[y][x] *= -1;
				}
			}
		}
		return false;
	}
	
	public void renderHiddenMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics)
	{
		renderMoveable(gc, camera, graphics, true);
	}

	public void renderMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics)
	{
		if (oscillateUp) {
			colorToFlash.a += (1.5 / 255.0f);
			if (colorToFlash.a > (100 / 255.0f)) {
				oscillateUp = false;
			}
		}
		else {
			colorToFlash.a -= (1.5 / 255.0f);
			if (colorToFlash.a < (40 / 255.0f)) {
				oscillateUp = true;
			}
		}
			
		renderMoveable(gc, camera, graphics, false);
	}
	
	private void renderMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics, boolean showOnlyForeground)
	{
		float camX = camera.getLocationX();
		float camY = camera.getLocationY();
		graphics.setColor(colorToFlash);
		for (int i = 0; i < moveableTiles.length; i++)
		{
			for (int j = 0; j < moveableTiles[0].length; j++)
			{			
				if (moveableTiles[i][j] != -1)
				{
					boolean isKeyTileEmpty = false;
					
					outer: for (int offX = 0; offX < 2; offX++)
						for (int offY = 0; offY < 2; offY++) {
							if (map.getMapLayer(4).getTiles()[(i + topY) * 2 + offY][(i + topX) * 2 + offX] <= 0) {
								isKeyTileEmpty = true;
								break outer;
							}
							
						}
					
					// showOnlyForeground = true;
					// isKeyTileEmpty = false;
					
					if (showOnlyForeground != isKeyTileEmpty) {
						
						graphics.fillRect((j + topX) * tileWidth - camX,
								(i + topY) * tileHeight - camY,
								tileWidth, tileHeight);												
					}
				}
			}
		}
	}
}
