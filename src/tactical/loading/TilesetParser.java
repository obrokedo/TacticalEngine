package tactical.loading;

import java.util.Hashtable;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.Log;

import tactical.map.Map;

public class TilesetParser
{
	public void parseTileset(String image, Color trans, int tileWidth, int tileHeight,
			int startIndex, Map map, Hashtable<Integer, Integer> landEffectByTileId, float tileResize) throws SlickException
	{
		Image tileSheetImage = new Image(image, trans);
		Log.debug("LOAD TILESET " + tileSheetImage);
		tileSheetImage.setFilter(Image.FILTER_NEAREST);
		SpriteSheet ss = new SpriteSheet(tileSheetImage.getScaledCopy(tileResize), tileWidth, tileHeight);
		map.addTileset(ss, startIndex, tileWidth, tileHeight, landEffectByTileId);
	}
}
