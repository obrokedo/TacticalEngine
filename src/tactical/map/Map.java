package tactical.map;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.engine.state.StateInfo;
import tactical.game.exception.BadResourceException;
import tactical.game.sprite.Door;
import tactical.game.sprite.Sprite;
import tactical.game.trigger.Trigger;
import tactical.game.trigger.TriggerCondition;
import tactical.game.trigger.TriggerCondition.HeroEntersLocation;

/**
 * @author Broked
 *
 * Holds values that describe a map created in Mappy, namely;
 * - Map dimensions
 * - Tileset
 * - Tile Size
 * - Polygon based map-objects that are created in "object" layers.
 * - Map layer data
 * In addition it provides methods for accessing this data in that allows the caller
 * to be unaware of multiple tilesets and tiles being handled differently then they are rendered
 */
public class Map
{
	private Hashtable<String, Integer> terrainEffectByType = new Hashtable<String, Integer>();
	private Hashtable<String, MovementCost> movementCostsByType = new Hashtable<String, Map.MovementCost>();

	/**
	 * A list of 2 dimensional int arrays, where each entry contains the tile indexs for each tile on that layer.
	 * A value of 0 in any given layer means that no tile was selected at this location.
	 */
	private String name;
	private ArrayList<MapLayer> mapLayer = new ArrayList<>();
	protected MapLayer moveableLayer;
	private MapLayer roofLayer;
	protected int tileWidth, tileHeight;
	protected ArrayList<TileSet> tileSets = new ArrayList<TileSet>();
	protected ArrayList<MapObject> mapObjects = new ArrayList<MapObject>();
	protected Hashtable<Integer, Integer> landEffectByTileId = new Hashtable<Integer, Integer>();
	private Hashtable<Point, Terrain> overriddenTerrain = new Hashtable<Point, Terrain>();
	private Hashtable<Integer, Roof> roofsById = new Hashtable<Integer, Roof>();
	private Hashtable<Integer, ArrayList<MapLayer>> flashingLayersByPosition = new Hashtable<>();
	private Shape battleRegion = null;
	private int roofCount = -1;
	private int backgroundImageIndex;
	private String defaultAttackPlatform = null;
	private EngineConfigurationValues jConfigValues;
	private boolean disableRoofs = false;
	private List<Stairs> stairs = new ArrayList<>();

	private float tileRatio = 2f;

	private TileSet inUseTileset;

	public static final float DESIRED_TILE_WIDTH = 24f;

	public Map() {
		super();
		jConfigValues = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
		for (String movementType : jConfigValues.getMovementTypes())
			movementCostsByType.put(movementType, new MovementCost(movementType, jConfigValues));
		for (String terrainType : jConfigValues.getTerrainTypes())
			terrainEffectByType.put(terrainType, jConfigValues.getTerrainEffectAmount(terrainType));
	}

	public void reinitalize()
	{
		battleRegion = null;
		mapObjects.clear();
		mapLayer.clear();
		landEffectByTileId.clear();
		tileSets.clear();
		overriddenTerrain.clear();
		roofsById.clear();
		roofCount = -1;
		backgroundImageIndex = -1;
		stairs = new ArrayList<>();
	}
	
	public void initializeObjects(boolean isCombat, StateInfo stateInfo)
	{
		int incrementingIds = 5000;
		for (MapObject mo : getMapObjects())
		{
			if (mo.getKey().equalsIgnoreCase("sprite"))
			{
				stateInfo.addSprite(mo.getSprite(stateInfo.getResourceManager()));
			}
			else if (mo.getKey().equalsIgnoreCase("door"))
			{
				Door door = (Door) mo.getDoor(stateInfo.getResourceManager(), incrementingIds++);
				stateInfo.addSprite(door);
				// stateInfo.addMapTrigger(new TriggerLocation(stateInfo, mo, door));
				Trigger event = new Trigger("Door Trigger " + door.getId(), -100, false, false, true, true, null, null);
				event.addTriggerable(event.new TriggerRemoveSprite(door.getName()));
				event.addTriggerable(event.new TriggerPlaySound("dooropen", 50));
				stateInfo.getResourceManager().addTriggerEvent(door.getId(), event);
				
				TriggerCondition condition = new TriggerCondition(door.getId(), "Door");
				condition.addCondition(new HeroEntersLocation(mo.getName(), true));
				stateInfo.getResourceManager().addTriggerCondition(condition);
			}
			else if (mo.getKey().equalsIgnoreCase("searcharea"))
			{
				mo.setName("Search Area " + incrementingIds++);
				mo.establishSearchArea(stateInfo.getResourceManager());
			}
			else if (mo.getKey().equalsIgnoreCase("chest")) {
				mo.setName("Chest " + incrementingIds++);
				stateInfo.addSprite(mo.establishChest(incrementingIds++, incrementingIds++, stateInfo.getResourceManager()));
			} 
			else if (mo.getKey().equalsIgnoreCase("stairs")) {
				Point p1 = null;
				Point p2 = null;
				int maxX = (int) mo.getShape().getMaxX();
				int minX = (int) mo.getShape().getMinX();
				int maxY = (int) mo.getShape().getMaxY();
				int minY = (int) mo.getShape().getMinY();
				
				for (int points = 0; points < mo.getShape().getPoints().length; points += 2) {
					float x = mo.getShape().getPoints()[points];
					float y = mo.getShape().getPoints()[points + 1];
					
					if (x == minX) {
						if (y == minY || y == maxY)
							p1 = new Point((int) x, (int) y);
					} else if (x == maxX) {
						if (y == minY || y == maxY)
							p2 = new Point((int) x, (int) y);
					}
				}
				
				// Now we have a line segment from p1 to p2, only allow movement along this
				// line. We'll fix x movement to the normal tile size, but y can be free floating
				stairs.add(new Stairs(
						new Point((int) p1.getX() / this.getTileEffectiveWidth(),
								(int) p1.getY() / this.getTileEffectiveHeight()), 
						new Point((int) p2.getX() / this.getTileEffectiveWidth(),
								(int) p2.getY() / this.getTileEffectiveHeight())));
			}
			/*
			else if (mo.getKey().equalsIgnoreCase("roof"))
			{
				addSprite(mo.getSprite(this));
			}
			*/
		}
	}
	
	public void addMapObject(MapObject mo) {
		if (mo.getKey() == null && mo.getName() == null)
			return;

		if (mo.getKey() != null)
		{
			if (mo.getKey().equalsIgnoreCase("terrain"))
			{
				for (int x = mo.getX(); x < mo.getX() + mo.getWidth(); x += getTileEffectiveWidth())
				{
					for (int y = mo.getY(); y < mo.getY() + mo.getHeight(); y += getTileEffectiveHeight())
					{
						if (mo.getShape().contains(x + 1, y + 1))
						{
							overriddenTerrain.put(new Point(x / getTileEffectiveWidth(), y / getTileEffectiveHeight()), new Terrain(mo.getParam("type"), mo.getParam("platform")));
						}
					}
				}
			}
			else if (mo.getKey().equalsIgnoreCase("battleregion"))
			{
				battleRegion = mo.getShape();
			}
			else if (mo.getKey().equalsIgnoreCase("roof"))
			{
				int roofId = 0;
	
				if (mo.getParam("roofid") != null)
				{
					roofId = Integer.parseInt(mo.getParam("roofid"));
				}
				else
					roofId = roofCount--;
	
				roofsById.put(roofId, new Roof(mo.getShape()));
	
			}
		}
		else
			mo.setKey("location");
		
		this.mapObjects.add(mo);
	}

	public void addLayer(String layerName, MapLayer layer)
	{
		mapLayer.add(layer);
	}
	
	public void intializeRoofs() {
		roofsById.values().stream().forEach(roof -> roof.determineRoofShape(roofLayer, tileWidth, tileHeight));
	}

	public int getMapWidth() {
		return mapLayer.get(0).getTiles()[0].length;
	}

	public int getMapHeight() {
		return mapLayer.get(0).getTiles().length;
	}

	public int getMapWidthInPixels() {
		return mapLayer.get(0).getTiles()[0].length * tileWidth;
	}

	public int getMapHeightInPixels() {
		return mapLayer.get(0).getTiles().length * tileHeight;
	}

	public MapLayer getMapLayer(int layer) {
		return mapLayer.get(layer);
	}

	public int getMapEffectiveWidth()
	{
		return (int) (mapLayer.get(0).getTiles()[0].length / tileRatio);
	}

	public int getMapEffectiveHeight()
	{
		return (int) (mapLayer.get(0).getTiles().length / tileRatio);
	}

	public int getTileEffectiveWidth()
	{
		return (int) (tileWidth * tileRatio);
	}

	public int getTileEffectiveHeight()
	{
		return (int) (tileHeight * tileRatio);
	}

	public int getTileRenderWidth() {
		return tileWidth;
	}

	public int getTileRenderHeight() {
		return tileHeight;
	}

	public ArrayList<MapObject> getMapObjects() {
		return mapObjects;
	}

	public void addTileset(SpriteSheet spriteSheet, int tileStartIndex, int tileWidth, int tileHeight, Hashtable<Integer, Integer> landEffectByTileId)
	{
		this.landEffectByTileId.putAll(landEffectByTileId);
		this.tileSets.add(new TileSet(spriteSheet, tileStartIndex));
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		Collections.sort(tileSets, new TileSetComparator());
	}

	public void renderSprite(int index, float xLoc, float yLoc)
	{
		for (TileSet ts : tileSets)
			if (index >= ts.getStartIndex()) {
				ts.renderSprite(xLoc, yLoc, index);
				return;
			}
	}

	public int getMovementCostByType(String moverType, int tileX, int tileY)
	{
		String tile;

		Point tti = new Point(tileX, tileY);
		if (overriddenTerrain.containsKey(tti))
		{
			try
			{
				tile = overriddenTerrain.get(tti).name;
				return movementCostsByType.get(moverType).getMovementCost(tile);
			}
			catch (NullPointerException e)
			{
				tile = overriddenTerrain.get(tti).name;
				throw new BadResourceException("The specified map has incorrect terrain cost types or the enemy/hero "
						+ "has an invalid movement type: Tile X "
						+ tileX + " Tile Y " + tileY + " Mover Type " + moverType);
			}
		}
		else
		{
			/*
			if (mapLayer.get(1)[tileY][tileX] != 0)
				tile = mapLayer.get(1)[tileY][tileX];
			else
				tile = mapLayer.get(0)[tileY][tileX];

			// Subtract one to account for the blank space at 0
			tile--;

			if (landEffectByTileId.containsKey(tile))
			{
				return movementCostsByType[moverType][landEffectByTileId.get(tile)];
			}
			else
				return 10000;
			*/
			return 10000;
		}
	}
	
	public String getAttackPlatformByTile(int tileX, int tileY)
	{
		Point locationOnMap = new Point(tileX, tileY);
		if (overriddenTerrain.containsKey(locationOnMap))
		{
			return overriddenTerrain.get(locationOnMap).platform;
		}
		return null;
	}

	public int getLandEffectByTile(String moverType, int tileX, int tileY)
	{
		// Check to see if given the movement type is effected by land effect
		// if not then just return 0
		if (!jConfigValues.isAffectedByTerrain(moverType))
			return 0;

		String tileTerrainType;

		Point locationOnMap = new Point(tileX, tileY);
		if (overriddenTerrain.containsKey(locationOnMap))
		{
			tileTerrainType = overriddenTerrain.get(locationOnMap).name;
			return terrainEffectByType.get(tileTerrainType);
		}
		else
		{
			/*
			if (mapLayer.get(1)[tileY][tileX] != 0)
				tile = mapLayer.get(1)[tileY][tileX];
			else
				tile = mapLayer.get(0)[tileY][tileX];

			// Subtract one to account for the blank space at 0
			tile--;

			if (landEffectByTileId.containsKey(tile))
			{
				return terrainEffectByType[landEffectByTileId.get(tile)];
			}
			else
			{
				return 0;
			}
			*/
			return 0;
		}
	}
	
	public String getTerrainTypeByTile(int tileX, int tileY) {
		Point locationOnMap = new Point(tileX, tileY);
		if (overriddenTerrain.containsKey(locationOnMap))
		{
			return overriddenTerrain.get(locationOnMap).name;
		} else {
			return null;
		}
	}

	public boolean isInBattleRegion(int mapX, int mapY)
	{
		if (battleRegion == null)
			return true;

		return battleRegion.contains(mapX + 1, mapY + 1);
	}

	public Shape getBattleRegion() {
		return battleRegion;
	}

	public boolean isMarkedMoveable(int tileX, int tileY)
	{
		if ((moveableLayer.getTiles().length > (tileY * tileRatio)) && ((moveableLayer.getTiles()[0].length > tileX * tileRatio)))
			return moveableLayer.getTiles()[(int) (tileY * tileRatio)][(int) (tileX * tileRatio)] != 0;
		return false;
	}

	protected class TileSet
	{	
		private SpriteSheet spriteSheet;
		protected int startIndex;
		private int ssWidth;

		public TileSet(SpriteSheet spriteSheet, int startIndex) {
			super();
			this.spriteSheet = spriteSheet;
			this.startIndex = startIndex;
			if (spriteSheet != null)
				this.ssWidth = spriteSheet.getHorizontalCount();
		}

		public void renderSprite(float x, float y, int index)
		{
			if (inUseTileset == null)
			{
				spriteSheet.startUse();
				inUseTileset = this;
			}
			else if (inUseTileset != this)
			{
				inUseTileset.endUse();
				spriteSheet.startUse();
				inUseTileset = this;
			}

			// System.out.println((index - startIndex) % ssWidth + " " + (index - startIndex) / ssWidth);
			// System.out.println("Bounds: " + spriteSheet.getHorizontalCount() + " " + spriteSheet.getVerticalCount());
			//spriteSheet.renderInUse(x, y, 
				//	(index - startIndex) % ssWidth, (index - startIndex) / ssWidth);
			if ((index - startIndex) / ssWidth > 100000)
				System.out.println();
			spriteSheet.getSubImage((index - startIndex) % ssWidth, (index - startIndex) / ssWidth).
				drawEmbedded(x, y, tileWidth, tileHeight);
		}

		public Image getSprite(int index) {
			// return spriteSheet.getSprite((index - startIndex) % ssWidth, (index - startIndex) / ssWidth);
			return spriteSheet.getSubImage((index - startIndex) % ssWidth, (index - startIndex) / ssWidth);
		}

		public void endUse() {
			spriteSheet.endUse();
		}

		public int getStartIndex() {
			return startIndex;
		}
	}



	public class TileSetComparator implements Comparator<TileSet>
	{
		@Override
		public int compare(TileSet ts0, TileSet ts1)
		{
			return ts1.startIndex - ts0.startIndex;
		}
	}

	public void checkRoofs(int mapX, int mapY)
	{
		for (Roof r : getRoofIterator())
		{
			if (r.getLocationShape().contains(mapX + .1f, mapY + .1f))
				r.setVisible(false);
			else
				r.setVisible(true);
		}
	}

	public Iterable<Roof> getRoofIterator()
	{
		return roofsById.values();
	}

	public Roof getRoofById(int id)
	{
		return roofsById.get(id);
	}

	public void setOriginalTileWidth(int origTileWidth)
	{
		tileRatio = DESIRED_TILE_WIDTH / origTileWidth;
	}
	
	public boolean isCustomBackground() {
		return -1 != backgroundImageIndex;
	}

	public int getBackgroundImageIndex() {
		return backgroundImageIndex;
	}

	public void setBackgroundImageIndex(int index) {
		this.backgroundImageIndex = index;
	}

	public String getDefaultAttackPlatform() {
		return defaultAttackPlatform;
	}

	public void setDefaultAttackPlatform(String defaultAttackPlatform) {
		this.defaultAttackPlatform = defaultAttackPlatform;
	}

	private class MovementCost
	{
		private Hashtable<String, Integer> movementCostByTerrain = new Hashtable<>();

		public MovementCost(String moveType, EngineConfigurationValues configValues)
		{
			for (String terrainType : configValues.getTerrainTypes())
				movementCostByTerrain.put(terrainType, configValues.getMovementCosts(moveType, terrainType));
		}

		public int getMovementCost(String terrain)
		{
			return movementCostByTerrain.get(terrain);
		}
	}

	public void setMoveableLayer(MapLayer moveableLayer) {
		this.moveableLayer = moveableLayer;
	}
	
	public void setRoofLayer(MapLayer roofLayer) {
		this.roofLayer = roofLayer;
	}

	public void endUse()
	{
		if (inUseTileset != null) {
			inUseTileset.endUse();
		}
		inUseTileset = null;
	}
	
	private class MapPathFinder implements TileBasedMap
	{
		private StateInfo stateInfo;
		private boolean checkSprites = true;
		
		public MapPathFinder(boolean checkSprites, StateInfo stateInfo) {
			super();
			this.stateInfo = stateInfo;
			this.checkSprites = checkSprites;
		}

		@Override
		public int getWidthInTiles() {
			return getMapEffectiveWidth();
		}

		@Override
		public int getHeightInTiles() {
			// TODO Auto-generated method stub
			return getMapEffectiveHeight();
		}

		@Override
		public void pathFinderVisited(int x, int y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean blocked(PathFindingContext context, int tx, int ty) {
			if (checkSprites) {
				for (Sprite s : stateInfo.getSprites())
				{
					if (tx == s.getTileX() && 
							ty == s.getTileY())
					{
						return true;
					}
				}
			}
			
			if (getHeightInTiles() > ty && getWidthInTiles() > tx)
			{
				return !isMarkedMoveable(tx, ty);
			}
			return true;
		}

		@Override
		public float getCost(PathFindingContext context, int tx, int ty) {
			return 1;
		}
	}
	
	public Path findTilePathWithPixels(int sx, int sy, int tx, int ty, StateInfo stateInfo, boolean checkSprites)
	{
		AStarPathFinder asf = new AStarPathFinder(new MapPathFinder(checkSprites, stateInfo), 1000, false);
		return asf.findPath(null, sx / getTileEffectiveWidth(), sy / getTileEffectiveHeight(), 
				tx / getTileEffectiveWidth(), ty / getTileEffectiveHeight());
	}
	
	public Path findPixelPathWithPixels(int sx, int sy, int tx, int ty, StateInfo stateInfo, boolean checkSprites)
	{
		Path tilePath = findTilePathWithPixels(sx, sy, tx, ty, stateInfo, checkSprites);
		if (tilePath == null)
			return null;
		Path pixelPath = new Path();
		for (int i = 0; i < tilePath.getLength(); i++)
		{
			pixelPath.appendStep(tilePath.getX(i) * getTileEffectiveWidth(), tilePath.getY(i) * getTileEffectiveHeight());
		}
		return pixelPath;
	}
	
	public void addFlashingLayer(MapLayer mapLayer) {
		ArrayList<MapLayer> layers = this.flashingLayersByPosition.get(this.mapLayer.size());
		if (layers == null)
			layers = new ArrayList<MapLayer>();
		layers.add(mapLayer);
		this.flashingLayersByPosition.put(this.mapLayer.size(), layers);
	}
	
	public List<MapLayer> getFlashingLayersByPosition(int layerPosition) {
		if (this.flashingLayersByPosition.containsKey(layerPosition))
			return this.flashingLayersByPosition.get(layerPosition);
		else
			return Collections.emptyList();
	}
	
	public void update(int delta)
	{
		for (ArrayList<MapLayer> layers : flashingLayersByPosition.values()) {
			for (MapLayer layer : layers) {
				layer.update(delta);
			}
		}
	}
	
	public MapLayer getRoofLayer() {
		if (!disableRoofs)
			return roofLayer;
		return null;
	}

	public void setDisableRoofs(boolean disableRoofs) {
		this.disableRoofs = disableRoofs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMapLayerAmount() {
		return (mapLayer == null ? 0 : mapLayer.size());
	}

	public float getTileRatio() {
		return tileRatio;
	}
	
	public class Terrain {
		public String name;
		public String platform;
		
		public Terrain(String name, String platform) {
			super();
			this.name = name;
			this.platform = platform;
		}		
	}
	
	public class Stairs implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private Point leftPoint, rightPoint;

		public Stairs(Point leftPoint, Point rightPoint) {
			super();
			this.leftPoint = leftPoint;
			this.rightPoint = rightPoint;
		}
		
		public boolean isOnRightEntry(int x, int y) {
			return x == rightPoint.x && y == rightPoint.y;
		}
		
		public boolean isOnLeftEntry(int x, int y) {
			return x == leftPoint.x && y == leftPoint.y;
		}
		
		public int getYCoordByTileX(int tileX) {
			float slope = ((float) (leftPoint.y - rightPoint.y) * getTileEffectiveHeight()) / 
					((float) (leftPoint.x - rightPoint.x) * getTileEffectiveWidth());
			int tilesToTheRight = tileX - leftPoint.x;
			return (int) ((leftPoint.y + slope * tilesToTheRight) * getTileEffectiveHeight()); 
		}
	}
	
	public Stairs isStartOfStairs(int x, int y) {
		for (Stairs s : stairs) {
			if (s.isOnLeftEntry(x, y) || s.isOnRightEntry(x, y))
				return s;
		}
		return null;
	}
}
