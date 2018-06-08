package tactical.game.move;

import java.awt.Point;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.LocationMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.battle.BattleEffect;
import tactical.game.constants.Direction;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.KeyboardListener;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.Sprite;
import tactical.game.turnaction.MoveToTurnAction;
import tactical.game.turnaction.TurnAction;
import tactical.game.ui.PaddedGameContainer;
import tactical.map.Map;

/**
 * Displays the possible moveable range for the currently acting CombatSprite, this also
 * handle user input to move around this area. Although this behaves much like a "Menu"
 * (is rendered, takes user input) it cannot be treated as such due to the location
 * it needs to be rendered (under the sprites) and the fact that there are cases
 * when input should not be recieved even though the menu is visible. This is the
 * case when the acting CombatSprite is moving. This means its' input and display
 * must be managed by some other entity
 */
public class MoveableSpace implements KeyboardListener, TileBasedMap
{
	private int[][] moveableTiles;
	private int topX, topY;
	private int tileWidth, tileHeight;
	private boolean owner;
	private boolean checkEvents = true;
	private Map map;
	private String spriteMovementType;
	int mapWidth, mapHeight;
	private static final int UNMOVEABLE_TILE = 99;
	private final Color MOVEABLE_COLOR = new Color(0, 0, 255, 40);
	private boolean checkMoveable = true;
	private ArrayList<Point> spritePoints = new ArrayList<Point>();

	public static MoveableSpace determineMoveableSpace(StateInfo stateInfo, CombatSprite currentSprite, boolean ownsSprite)
	{
		MoveableSpace ms = new MoveableSpace();

		ms.owner = ownsSprite;
		ms.map = stateInfo.getResourceManager().getMap();
		ms.mapWidth = ms.map.getMapEffectiveWidth();
		ms.mapHeight = ms.map.getMapEffectiveHeight();
		int currentMove = currentSprite.getCurrentMove();
		
		// Check to see if we can move
		for (BattleEffect effect : currentSprite.getBattleEffects()) {
			if (effect.preventsMovement()) {
				currentMove = 0;
				break;
			}
		}
		
		ms.moveableTiles = new int[currentMove * 2 + 1][currentMove * 2 + 1];
		for (int i = 0; i < ms.moveableTiles.length; i++)
			for (int j = 0; j < ms.moveableTiles.length; j++)
				ms.moveableTiles[i][j] = -1;
		ms.tileWidth = stateInfo.getTileWidth();
		ms.tileHeight = stateInfo.getTileHeight();
		int mapSpriteX = currentSprite.getTileX();
		int mapSpriteY = currentSprite.getTileY();

		ms.spriteMovementType = currentSprite.getMovementType();
		ms.topX = mapSpriteX - currentMove;
		ms.topY = mapSpriteY - currentMove;

		// Check to see if there are any sprites in your moveable area that you will not be able to move through
		Rectangle checkMoveableRect = new Rectangle(ms.topX,  ms.topY, ms.moveableTiles.length + 1, ms.moveableTiles.length + 1);
		for (Sprite s : stateInfo.getSprites())
		{
			if (s == currentSprite)
				continue;

			int sX = s.getTileX();
			int sY = s.getTileY();

			// If this sprite is an ally then it will not block the mover
			if (s.getSpriteType() == Sprite.TYPE_COMBAT)
				if (((CombatSprite) s).isHero() == currentSprite.isHero())
					continue;

			// A sprite was found in your moveable area, in this case set it to the special 99 value because it will not be moveable
			if (checkMoveableRect.contains(sX + 1, sY + 1))
				ms.moveableTiles[sY - ms.topY][sX - ms.topX] = UNMOVEABLE_TILE;
		}

		// Add the move cost of the current tile as it will be subtracted from the sprites move. Use 10 as the lowest cost so tha we can
		// have half-values (1.5, 2.5) without making the cost a double
		ms.determineMoveableSpacesRecursive(currentMove  * 10 + ms.map.getMovementCostByType(currentSprite.getMovementType(),
			mapSpriteX, mapSpriteY), currentMove,
				currentMove, mapSpriteX, mapSpriteY);

		// For any spaces that had sprites in them change the special place holder 99 that makes them unmovable
		// to -1 so they aren't shown as moveable
		for (int i = 0; i < ms.moveableTiles.length; i++)
		{
			for (int j = 0; j < ms.moveableTiles[0].length; j++)
			{
				if (ms.moveableTiles[i][j] == UNMOVEABLE_TILE)
					ms.moveableTiles[i][j] = -1;
			}
		}

		for (Sprite s : stateInfo.getSprites())
		{
			if (s == currentSprite)
				continue;

			int sX = s.getTileX();
			int sY = s.getTileY();

			if (s.getSpriteType() == Sprite.TYPE_STATIC_SPRITE)
				ms.spritePoints.add(new Point(sX, sY));

			// If this sprite is an ally then set it's location to the special non moveable
			// value. The allies will be able to move through this spot but not end there.
			if (s.getSpriteType() == Sprite.TYPE_COMBAT && ((CombatSprite) s).isHero() == currentSprite.isHero()
					&& checkMoveableRect.contains(sX + 1, sY + 1) && ms.canEndMoveHere(sX, sY))
						ms.moveableTiles[sY - ms.topY][sX - ms.topX] = UNMOVEABLE_TILE;
		}

		return ms;
	}


	public void renderHiddenMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics)
	{
		renderMoveable(gc, camera, graphics, true);
	}

	public void renderMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics)
	{
		renderMoveable(gc, camera, graphics, false);
	}
	
	private void renderMoveable(PaddedGameContainer gc, Camera camera, Graphics graphics, boolean showOnlyForeground)
	{
		float camX = camera.getLocationX();
		float camY = camera.getLocationY();
		graphics.setColor(MOVEABLE_COLOR);
		for (int i = 0; i < moveableTiles.length; i++)
		{
			for (int j = 0; j <moveableTiles[0].length; j++)
			{										
				if (moveableTiles[j][i] != -1)
				{
					boolean isKeyTileEmpty = false;
					
					outer: for (int offX = 0; offX < 2; offX++)
						for (int offY = 0; offY < 2; offY++) {
							if (map.getMapLayer(4).getTiles()[(j + topY) * 2 + offY][(i + topX) * 2 + offX] <= 0) {
								isKeyTileEmpty = true;
								break outer;
							}
							
						}
					
					if (showOnlyForeground && !isKeyTileEmpty) {
						
						graphics.fillRect((i + topX) * tileWidth - camX,
								(j + topY) * tileHeight - camY,
								tileWidth - 1, tileHeight - 1);												
					} else if (!showOnlyForeground && isKeyTileEmpty) {
						graphics.fillRect((i + topX) * tileWidth - camX,
								(j + topY) * tileHeight - camY,
								tileWidth - 1, tileHeight - 1);	
					}
				}
			
			}
		}
	}

	private void determineMoveableSpacesRecursive(int remainingMove, int spriteX, int spriteY, int mapX, int mapY)
	{
		int cost = map.getMovementCostByType(spriteMovementType, mapX, mapY);

		remainingMove -= cost;

		if (remainingMove >= 0 && remainingMove > moveableTiles[spriteY][spriteX])
		{
			moveableTiles[spriteY][spriteX] = remainingMove;
			// UP
			if (mapY - 1 >= 0)
				determineMoveableSpacesRecursive(remainingMove, spriteX, spriteY - 1, mapX, mapY - 1);
			// Down
			if (mapY + 1 < mapHeight)
				determineMoveableSpacesRecursive(remainingMove, spriteX, spriteY + 1, mapX, mapY + 1);
			// Left
			if (mapX - 1 >= 0)
				determineMoveableSpacesRecursive(remainingMove, spriteX - 1, spriteY, mapX - 1, mapY);
			// Right
			if (mapX + 1 < mapWidth)
				determineMoveableSpacesRecursive(remainingMove, spriteX + 1, spriteY, mapX + 1, mapY);
		}
	}

	/**
	 * Returns a boolean indicating whether a combat sprite can end their turn at the specified tile cooridinates
	 *
	 * @param tileX x tile index
	 * @param tileY y tile index
	 * @return a boolean indicating whether a combat sprite can end their turn at the specified tile cooridinates
	 */
	public boolean canEndMoveHere(int tileX, int tileY)
	{
		if (canMoveHere(tileX, tileY) &&
				// Check to see if this space can be ended on
				moveableTiles[tileY - topY][tileX - topX] != UNMOVEABLE_TILE)
			return true;
		else
			return false;
	}

	/**
	 * Returns a boolean indicating whether a combat sprite can move through the tile at the specified tile cooridinates
	 *
	 * @param tileX x tile index
	 * @param tileY y tile index
	 * @return a boolean indicating whether a combat sprite can move through the tile at the specified tile cooridinates
	 */
	public boolean canMoveHere(int tileX, int tileY)
	{
		if (tileX >= topX && tileX < topX + moveableTiles.length &&
				tileY >= topY && tileY < topY + moveableTiles.length &&
				moveableTiles[tileY - topY][tileX - topX] >= 0)
			return true;
		else
			return false;
	}

	public void addMoveActionsToLocation(int mapX, int mapY, Sprite currentSprite,
			ArrayList<TurnAction> turnActions)
	{
		AStarPathFinder pathFinder = new AStarPathFinder(this, 18, false);

		Path path = pathFinder.findPath(
				null, currentSprite.getTileX() - this.getTopX(),
					currentSprite.getTileY() - this.getTopY(), mapX - this.getTopX(), mapY - this.getTopY());

		if (path != null)
			for (int i = 0; i < path.getLength(); i++)
				turnActions.add(new MoveToTurnAction((path.getX(i) + this.getTopX()) * tileWidth, (path.getY(i) + this.getTopY()) * tileHeight));
	}

	/**
	 *
	 * @param mapX The X location on the map (Not tile X) of the destination
	 * @param mapY The Y location on the map (Not tile X) of the destination
	 * @param currentSprite
	 * @param turnActions
	 */
	public void addMoveActionsAlongPath(int mapX, int mapY, Sprite currentSprite, ArrayList<TurnAction> turnActions)
	{
		addMoveActionsAlongPath(mapX, mapY, currentSprite, turnActions, 9);
	}

	/**
	 *
	 * @param mapX The X location on the map (Not tile X) of the destination
	 * @param mapY The Y location on the map (Not tile Y) of the destination
	 * @param currentSprite
	 * @param turnActions
	 */
	public void addMoveActionsAlongPath(int mapX, int mapY, Sprite currentSprite, ArrayList<TurnAction> turnActions, int maxMove)
	{
		checkMoveable = false;
		AStarPathFinder pathFinder = new AStarPathFinder(this, 1000, false);

		Path path = pathFinder.findPath(
				null, currentSprite.getTileX(),
					currentSprite.getTileY(), mapX / tileWidth, mapY / tileHeight);
		checkMoveable = true;


		// If we found a path then traverse the path to find the last spot we can move to
		if (path != null)
		{
			ArrayList<TurnAction> uncommitedActions = new ArrayList<TurnAction>();
			for (int i = 0; i < Math.min(maxMove, path.getLength()); i++)
			{
				if (canEndMoveHere(path.getX(i), path.getY(i)))
				{
					turnActions.addAll(uncommitedActions);
					uncommitedActions.clear();
					turnActions.add(new MoveToTurnAction(path.getX(i) * tileWidth, path.getY(i) * tileHeight));
				}
				else
					uncommitedActions.add(new MoveToTurnAction(path.getX(i) * tileWidth, path.getY(i) * tileHeight));
			}
		}
	}

	public boolean doesPathExist(int startTileX, int startTileY, int endTileX, int endTileY)
	{
		checkMoveable = false;
		AStarPathFinder pathFinder = new AStarPathFinder(this, 1000, false);

		Path path = pathFinder.findPath(
				null, startTileX,
					startTileY, endTileX, endTileY);
		checkMoveable = true;
		return path != null;
	}

	public boolean isTileWithinMove(int mapX, int mapY, Sprite currentSprite, int maxMove)
	{
		checkMoveable = false;
		AStarPathFinder pathFinder = new AStarPathFinder(this, maxMove, false);

		Path path = pathFinder.findPath(
				null, currentSprite.getTileX(),
					currentSprite.getTileY(), mapX / tileWidth, mapY / tileHeight);
		checkMoveable = true;

		return path != null;
	}

	public int[][] getMoveableTiles() {
		return moveableTiles;
	}

	public int getTopX() {
		return topX;
	}

	public int getTopY() {
		return topY;
	}

	@Override
	public int getWidthInTiles() {
		if (checkMoveable)
			return moveableTiles.length;
		else
			return map.getMapEffectiveWidth();
	}

	@Override
	public int getHeightInTiles() {
		if (checkMoveable)
			return moveableTiles.length;
		else
			return map.getMapEffectiveHeight();
	}

	@Override
	public void pathFinderVisited(int x, int y) {}

	@Override
	public boolean blocked(PathFindingContext context, int tx, int ty) {
		if (checkMoveable)
			return moveableTiles[ty][tx] < 0;
		else
		{
			if (map.getMapEffectiveHeight() > ty && map.getMapEffectiveWidth() > tx)
			{
				for (Point p : spritePoints)
				{
					if (p.x == tx && p.y == ty)
						return true;
				}

				return !map.isMarkedMoveable(tx, ty);
			}
			return true;
		}
	}

	@Override
	public float getCost(PathFindingContext context, int tx, int ty) {
		return 1;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * Whether the moveable space should check for user input. This should only be used when the
	 * MoveableSpace is visible and the target of user input.
	 *
	 * @param checkEvents A boolean indicating whether user input should be checked on update
	 */
	public void setCheckEvents(boolean checkEvents) {
		this.checkEvents = checkEvents;
	}

	@Override
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo)
	{
		if (!checkEvents)
			return false;

		if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3))
		{
			if (canEndMoveHere(stateInfo.getCurrentSprite().getTileX(), stateInfo.getCurrentSprite().getTileY()))
			{
				stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT,
						TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration().getMenuAddedSoundEffect(), 1f, false));
				return true;
			}
			else
			{
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT,
						TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration().getInvalidActionSoundEffect(), 1f, false));
			}
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			stateInfo.sendMessage(MessageType.RESET_SPRITELOC);
			// stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
			return true;
		}
		else
		{
			switch (input.getMostRecentDirection())
			{
				case KeyMapping.BUTTON_UP:
					return handleKeyboardMovement(stateInfo, 0, -1, Direction.UP);
				case KeyMapping.BUTTON_DOWN:
					return handleKeyboardMovement(stateInfo, 0, 1, Direction.DOWN);
				case KeyMapping.BUTTON_LEFT:
					return handleKeyboardMovement(stateInfo, -1, 0, Direction.LEFT);
				case KeyMapping.BUTTON_RIGHT:
					return handleKeyboardMovement(stateInfo, 1, 0, Direction.RIGHT);
			}
		}

		return false;
	}

	private boolean handleKeyboardMovement(StateInfo stateInfo, int x, int y, Direction direction)
	{
		int sx = stateInfo.getCurrentSprite().getTileX() + x;
		int sy = stateInfo.getCurrentSprite().getTileY() + y;

		if (canMoveHere(sx, sy))
		{
			stateInfo.sendMessage(new LocationMessage(MessageType.MOVETO_SPRITELOC, sx * stateInfo.getTileWidth(), sy * stateInfo.getTileHeight()));
		}
		else
			stateInfo.getCurrentSprite().setFacing(direction);
		return false;
	}
}
