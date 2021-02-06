package tactical.loading;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import tactical.game.exception.BadMapException;
import tactical.game.sprite.AnimatedSprite;
import tactical.map.Map;
import tactical.map.MapLayer;
import tactical.map.MapObject;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class MapParser
{
	private static float tileResize = 1;
	
	public static void parseMap(String mapFile, Map map, TilesetParser tilesetParser, HashSet<TagArea> mapAreas,
			ResourceManager frm) throws IOException, SlickException
	{
		HashSet<String> spriteToLoad = new HashSet<String>();
		ArrayList<TagArea> tagAreas = XMLParser.process(mapFile, false);

		TagArea tagArea = tagAreas.get(0);
		tagArea.getChildren().addAll(mapAreas);

		int width = Integer.parseInt(tagArea.getAttribute("width"));
		int height = Integer.parseInt(tagArea.getAttribute("height"));
		//int tileWidth = Integer.parseInt(tagArea.getAttribute("tilewidth")) * map.getTileScale();
		//int tileHeight = Integer.parseInt(tagArea.getAttribute("tileheight")) * map.getTileScale();
		int tileWidth = Integer.parseInt(tagArea.getAttribute("tilewidth"));
		int tileHeight = Integer.parseInt(tagArea.getAttribute("tileheight"));


		float desiredTileSize = Map.DESIRED_TILE_WIDTH;
		tileResize = 1;
		if (tileWidth != desiredTileSize && tileWidth != desiredTileSize / 2)
		{
			if (tileWidth < desiredTileSize)
				desiredTileSize /= 2;

			tileResize = desiredTileSize / tileWidth;
			tileWidth *= tileResize;
			tileHeight *= tileResize;
			map.setOriginalTileWidth((int) desiredTileSize);
		}
		else
			map.setOriginalTileWidth(tileWidth);

		Log.debug("Tile Dimensions " + tileWidth  + " " + tileHeight);

		String tileSet = null;
		// This is kind of a kludge...
		AnimatedSprite.SHADOW_OFFSET = AnimatedSprite.DEFAULT_SHADOW_OFFSET;
		AnimatedSprite.SHADOW_COLOR = AnimatedSprite.DEFAULT_SHADOW_COLOR;

		
		if (map instanceof PlannerMap)
			((PlannerMap) map).setRootTagArea(tagArea);

		for (TagArea childArea : tagArea.getChildren())
		{			
			if (childArea.getTagType().equalsIgnoreCase("tileset"))
			{
				tileSet = childArea.getChildren().get(0).getAttribute("source");
				String trans = childArea.getChildren().get(0).getAttribute("trans");
				int startIndex = Integer.parseInt(childArea.getAttribute("firstgid"));
				String[] tsSplit = tileSet.split(",")[0].split("/");

				Hashtable<Integer, Integer> landEffectByTileId = new Hashtable<Integer, Integer>();

				for (TagArea tile : childArea.getChildren())
				{
					if (tile.getTagType().equalsIgnoreCase("tile"))
					{
						int id = Integer.parseInt(tile.getAttribute("id"));
						if (tile.getChildren().size() > 0 && tile.getChildren().get(0).getChildren().size() > 0 &&
								tile.getChildren().get(0).getChildren().get(0).getAttribute("value") != null)
						{
							int landEffect = Integer.parseInt(tile.getChildren().get(0).getChildren().get(0).getAttribute("value"));
							landEffectByTileId.put(id, landEffect);
						}
					}
				}

				tilesetParser.parseTileset("image/" + tsSplit[tsSplit.length - 1], new Color(	Integer.parseInt(trans.substring(0, 2), 16),
						Integer.parseInt(trans.substring(2, 4), 16),
						Integer.parseInt(trans.substring(4, 6), 16)),
						tileWidth, tileHeight, startIndex, map, landEffectByTileId, tileResize);
							}
			else if (childArea.getTagType().equalsIgnoreCase("layer"))
			{
				MapLayer layer = null;
				try
				{
					layer = decodeLayer(childArea, width, height); // new int[height][width];
					
					if (childArea.getParams().get("name").startsWith("walk") ||
							childArea.getParams().get("name").startsWith("Walk") ||
							layer.containsParam("walk"))
						map.setMoveableLayer(layer);
					else if (childArea.getParams().get("name").startsWith("Roof") ||
							childArea.getParams().get("name").startsWith("roof") ||
							layer.containsParam("roof")) {
						if (childArea.getParams().get("name").contains("shadow") || 
								childArea.getParams().get("name").contains("Shadow"))
							map.setRoofShadowLayer(layer);
						else
							map.setRoofLayer(layer);
					}
					else if (layer.containsParam("flash"))
					{
						String value = layer.getParam("flash");
						if (value.indexOf("duration=") == -1 || value.indexOf("offset=") == -1)
							throw new BadMapException("Unable to parse map due to incorrectly formated 'flashing' layer. The 'flash' key should define parameters 'offset=###' and 'duration=###'");
						String[] values = value.split(" ");
						for (String s : values)
						{
							String[] flashSplit = s.split("=");
							try
							{
								if (flashSplit[0].equalsIgnoreCase("duration"))
								{
									layer.setFlashDuration(Integer.parseInt(flashSplit[1]));
								}
								
								if (flashSplit[0].equalsIgnoreCase("offset"))
								{
									layer.setFlashOffset(Integer.parseInt(flashSplit[1]));
								}
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
								throw new BadMapException("Numeric values must be specified for 'flashing' attributes 'duration' and 'offset'");
							}
							
						}
						
						map.addFlashingLayer(layer);
					} else if (childArea.getParams().get("name").startsWith("grid") ||
							childArea.getParams().get("name").startsWith("Grid") ||
							layer.containsParam("grid")) {
						continue;
					}
					else
					{
						map.addLayer(childArea.getParams().get("name"), layer);
					}
				}
				catch (BadMapException ex)
				{
					throw new BadMapException(ex.getMessage() + " For map: " + mapFile);
				}

				/*
				int index = 0;
				for (TagArea tileTag : childArea.getChildren().get(0).getChildren())
				{
					layer[index / width][index % width] = Integer.parseInt(tileTag.getAttribute("gid"));
					index++;
				}
				*/
			}
			else if (childArea.getTagType().equalsIgnoreCase("objectgroup"))
			{
				for (TagArea objectTag : childArea.getChildren())
				{
					parseMapObject(map, spriteToLoad, objectTag);
				}
			}
			else if (childArea.getTagType().equalsIgnoreCase("properties"))
			{
				for (TagArea prop : childArea.getChildren())
				{
					String propName = prop.getAttribute("name");
					if (propName.equalsIgnoreCase("background"))
					{
						try
						{
							map.setBackgroundImageIndex(Integer.parseInt(prop.getAttribute("value")));
						}
						catch (Throwable t)
						{
							throw new BadMapException("The map " + mapFile + " had a non-integer value specified for the battle background. Value was: " + prop.getAttribute("value"));
						}
					}
					else if (propName.equalsIgnoreCase("platform"))
					{
						try
						{
							map.setDefaultAttackPlatform(prop.getAttribute("value"));
						}
						catch (Throwable t)
						{
							throw new BadMapException("The map " + mapFile + " had a bad platorm name specified for the default platform. Value was: " + prop.getAttribute("value"));
						}
					}
					else if (propName.equalsIgnoreCase("shadow"))
					{
						try
						{
							AnimatedSprite.SHADOW_OFFSET = Integer.parseInt(prop.getAttribute("value"));
						}
						catch (Throwable t)
						{
							throw new BadMapException("The map " + mapFile + " had a non-integer value specified for the shadow property. Value was: " + prop.getAttribute("value"));
						}
					}
					
					else if (propName.equalsIgnoreCase("shadow-opacity"))
					{
						try
						{
							AnimatedSprite.SHADOW_COLOR = new Color(0, 0, 0, Integer.parseInt(prop.getAttribute("value")));
						}
						catch (Throwable t)
						{
							throw new BadMapException("The map " + mapFile + " had a non-integer value specified for the shadow property. Value was: " + prop.getAttribute("value"));
						}
					}
				}
			}
		}
		
		map.intializeRoofs();

		/*
		for (String resource : spriteToLoad)
			frm.addSpriteResource(resource);
		for (String resource : animToLoad)
			frm.addAnimResource(resource);*/
	}

	protected static void parseMapObject(Map map, HashSet<String> spriteToLoad, TagArea objectTag) {
		MapObject mapObject = new MapObject();
		mapObject.setName(objectTag.getAttribute("name"));
		if (objectTag.getAttribute("x") != null)
			mapObject.setX((int) (Integer.parseInt(objectTag.getAttribute("x")) * tileResize));
		if (objectTag.getAttribute("y") != null)
			mapObject.setY((int) (Integer.parseInt(objectTag.getAttribute("y")) * tileResize));
		if (objectTag.getAttribute("width") != null)
			mapObject.setWidth((int) (Integer.parseInt(objectTag.getAttribute("width")) * tileResize));
		if (objectTag.getAttribute("height") != null)
			mapObject.setHeight((int) (Integer.parseInt(objectTag.getAttribute("height"))  * tileResize));
		for (TagArea propArea : objectTag.getChildren())
		{
			if (propArea.getTagType().equalsIgnoreCase("properties"))
			{
				mapObject.setKey(propArea.getChildren().get(0).getAttribute("name"));
				mapObject.setValue(propArea.getChildren().get(0).getAttribute("value"));

				if (mapObject.getKey().equalsIgnoreCase("sprite"))
				{
					String image = mapObject.getParam("image");
					if (image != null)
						spriteToLoad.add(image);
				}
			}
			else if (propArea.getTagType().equalsIgnoreCase("polyline"))
			{
				String[] points = propArea.getAttribute("points").split(" ");
				ArrayList<Point> pointList = new ArrayList<Point>();
				for (String point : points)
				{
					String[] p = point.split(",");
					pointList.add(new Point((int) (Integer.parseInt(p[0])  * tileResize),
							(int) (Integer.parseInt(p[1])  * tileResize)));
				}

				mapObject.setPolyPoints(pointList);
			}
		}

		mapObject.determineShape();

		if (map instanceof PlannerMap)
			((PlannerMap) map).addMapObject(mapObject, objectTag);
		else
			map.addMapObject(mapObject);
	}

	/** The code used to decode Base64 encoding */
	private static byte[] baseCodes = new byte[256];

	/**
	 * Static initialiser for the codes created against Base64
	 */
	static {
		for (int i = 0; i < 256; i++)
			baseCodes[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			baseCodes[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			baseCodes[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			baseCodes[i] = (byte) (52 + i - '0');
		baseCodes['+'] = 62;
		baseCodes['/'] = 63;
	}

	/**
	 * Decode a Base64 string as encoded by TilED
	 *
	 * @param data
	 *            The string of character to decode
	 * @return The byte array represented by character encoding
	 */
	private static byte[] decodeBase64(char[] data) {
		int temp = data.length;
		for (int ix = 0; ix < data.length; ix++) {
			if ((data[ix] > 255) || baseCodes[data[ix]] < 0) {
				--temp;
			}
		}

		int len = (temp / 4) * 3;
		if ((temp % 4) == 3)
			len += 2;
		if ((temp % 4) == 2)
			len += 1;

		byte[] out = new byte[len];

		int shift = 0;
		int accum = 0;
		int index = 0;

		for (int ix = 0; ix < data.length; ix++) {
			int value = (data[ix] > 255) ? -1 : baseCodes[data[ix]];

			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}

		if (index != out.length) {
			throw new RuntimeException(
					"Data length appears to be wrong (wrote " + index
							+ " should be " + out.length + ")");
		}

		return out;
	}
	
	public static MapLayer decodeLayer(TagArea tagArea, int layerWidth, int layerHeight)
	{
		int[][] layer = new int[layerHeight][layerWidth];
		MapLayer mapLayer = new MapLayer(layer);

		try
		{
			
			for (TagArea childTagArea : tagArea.getChildren())
			{
				if (childTagArea.getTagType().equalsIgnoreCase("properties"))
				{
					for (TagArea propertyTagArea : childTagArea.getChildren())
					{
						mapLayer.addParam(propertyTagArea.getParams().get("name"), 
								propertyTagArea.getParams().get("value"));
					}
				}
				else if (childTagArea.getTagType().equalsIgnoreCase("data"))
				{
					String cdata = childTagArea.getValue();
					char[] enc = cdata.toCharArray();
					byte[] dec = decodeBase64(enc);
					byte[] gOut = new byte[layerHeight * layerWidth * 4];				
					
					GZIPInputStream is = new GZIPInputStream(
							new ByteArrayInputStream(dec));
					int read = 0;
					int start = 0;
					while ((read = is.read(gOut, start, gOut.length - start)) > 0) {
						start += read;
					}
					
					ByteBuffer bb = ByteBuffer.wrap(gOut);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					for (int y = 0; y < layerHeight; y++) {
						for (int x = 0; x < layerWidth; x++) {							
							layer[y][x] = bb.getInt();
						}
					}
				}
			}
		} catch (IOException | NullPointerException e) {
			Log.error(e);
			throw new BadMapException("Unable to decode base 64 block, make sure that you have\n set the map type type to Base 64 gzip compressed");
		}

		return mapLayer;
	}
}
