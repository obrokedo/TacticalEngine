package tactical.game.ai;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.move.MoveableSpace;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.Sprite;
import tactical.game.turnaction.AttackSpriteAction;
import tactical.game.turnaction.EndTurnAction;
import tactical.game.turnaction.HideMoveAreaAction;
import tactical.game.turnaction.TargetSpriteAction;
import tactical.game.turnaction.TurnAction;
import tactical.game.turnaction.WaitAction;

public abstract class AI implements Serializable
{
	private static final long serialVersionUID = 1L;
	// BESERKER
	// CASTER
	// HEALER
	// DEFENDER
	// ATTACKER
	// REACTIVE
	// Everyone but Beserker uses team dynamics, maybe just a general reactive class that also
	// doesn't listen, but doesn't actively charge
	public final static int APPROACH_REACTIVE = 0;
	public final static int APPROACH_KAMIKAZEE = 1;
	public final static int APPROACH_HESITANT = 2;
	public final static int APPROACH_FOLLOW = 3;
	public final static int APPROACH_MOVE_TO_POINT = 4;
	public final static int APPROACH_TARGET = 5;
	public final static int APPROACH_WANDER = 6;

	private int approachType;
	private boolean canHeal;
	private Point targetPoint;
	private int priority = 0;
	private int vision;

	// The target Combat sprite that has been specified by external commands
	private CombatSprite targetCS;

	public AI(int approachType, boolean canHeal, int vision) {
		super();
		this.approachType = approachType;
		this.canHeal = canHeal;
		this.vision = vision;
	}
	
	public void reinitialize(StateInfo stateInfo)
	{
		if (targetCS != null)
			targetCS = stateInfo.getCombatantById(targetCS.getId());
	}

	public ArrayList<TurnAction> performAI(StateInfo stateInfo, MoveableSpace ms, CombatSprite currentSprite)
	{
		return performAI(stateInfo, ms, currentSprite, null);
	}
	
	public ArrayList<TurnAction> performAI(StateInfo stateInfo, MoveableSpace ms, CombatSprite currentSprite, List<AIConfidence> debugConfidences)
	{
		AIConfidence conf = this.getBestConfidence(stateInfo, ms, currentSprite, debugConfidences);
		//if (debugConfidences != null)
			//debugConfidences.add(0, conf);
		return this.getActions(stateInfo, ms, currentSprite, conf);
	}

	public AIConfidence getBestConfidence(StateInfo stateInfo, MoveableSpace ms, CombatSprite currentSprite, List<AIConfidence> confidenceDebugList)
	{
		ArrayList<AttackableEntity> attackableSprites = this.getAttackableSprites(stateInfo, currentSprite, ms, getMaxRange(currentSprite));
		AIConfidence maxConfidence = new AIConfidence(0);
		int tileWidth = ms.getTileWidth();
		int tileHeight = ms.getTileHeight();
		
		ArrayList<AIConfidence> jankList = null;
		if (TacticalGame.RANDOM.nextInt(100) < 10) {
			jankList = new ArrayList<>();
		}

		Log.debug("------ " + currentSprite.getName());

		// People that can heal should have an opportunity to heal themselves
		if (canHeal)
		{
			attackableSprites.add(new AttackableEntity(currentSprite, getBestPoint(stateInfo, tileWidth, tileHeight, currentSprite, ms, true, 2), 0));
		}

		// Attempt to move to an area that has the least amount of enemies, most amount of allies next to it
		// and the most amount of damage. Want to balance this with the resources used
		if (attackableSprites.size() > 0)
		{
			for (AttackableEntity as : attackableSprites)
			{
				if (as.getCombatSprite().isHero() != currentSprite.isHero())
				{
					// If we have a target approach type then we want to ignore any hero who is not the target,
					// however we will still perform actions on our allies and self.
					if (approachType == AI.APPROACH_TARGET && as.getCombatSprite() != targetCS)
						continue;

					maxConfidence.foundHero = true;
				}

				for (int i = 0; i < as.getAttackablePoints().size(); i++)
				{
					Point attackPoint = as.getAttackablePoints().get(i);
					
					int distance = as.getDistances().get(i);
					AIConfidence currentConfidence = getConfidence(currentSprite, as.getCombatSprite(), tileWidth, tileHeight,
							attackPoint, distance, stateInfo);					
					
					// Don't let land effect make a bad decision better
					if (currentConfidence.confidence > 0)
					{
						currentConfidence.confidence += this.getLandEffectConfidence(attackPoint, currentSprite, stateInfo);
					}
					
					currentConfidence.attackPoint = attackPoint;
					currentConfidence.target = as.getCombatSprite();
					currentConfidence.potentialAttackSpriteAction = getPerformedTurnAction(as.getCombatSprite());
					currentConfidence.foundHero = true;
					
					if (confidenceDebugList != null) {
						confidenceDebugList.add(currentConfidence);
					}
					
					if (jankList != null)
						jankList.add(currentConfidence);

					if (currentConfidence.confidence > maxConfidence.confidence)
					{
						maxConfidence = currentConfidence;
					}
					else if (currentConfidence == maxConfidence)
					{
						Log.debug("Found equal confidence = " + currentConfidence);
						if (TacticalGame.RANDOM.nextInt(100) > 50)
						{
							maxConfidence = currentConfidence;
							Log.debug("Switched action randomly");
						}
					}
					
					Log.debug("Target " + as.getCombatSprite().getName() + " Confidence: " + currentConfidence + " Distance: " + distance);
				}
			}
		}
		
		if (jankList != null && jankList.size() > 0) {
			AIConfidence jankConf = jankList.get(TacticalGame.RANDOM.nextInt(jankList.size()));
			
			if (jankConf.confidence < 0) {
				jankConf.potentialAttackSpriteAction = null;
			}
			
			Log.debug("You've been JANKED");
			return jankConf;
		}

		return maxConfidence;
	}

	public ArrayList<TurnAction> getActions(StateInfo stateInfo,
			MoveableSpace ms, CombatSprite currentSprite, AIConfidence confidence)
	{
		int tileWidth = ms.getTileWidth();
		int tileHeight = ms.getTileHeight();
		ArrayList<TurnAction> turnActions = new ArrayList<TurnAction>();


		// We found no reasonable target, either because none was in range OR the available spaces were
		// really poor
		if (confidence.confidence == 0)
		{
			// If we have no confidence in what we're going to do then we want to move away from the enemies
			// to try and split them up.
			if (confidence.foundHero)
			{
				Point bestPoint = this.getBestPoint(stateInfo, tileWidth, tileHeight, currentSprite, ms, true, 2);
				ms.addMoveActionsToLocation(bestPoint.x, bestPoint.y, currentSprite, turnActions);
			}
			else
			{
				switch (approachType)
				{
					case AI.APPROACH_REACTIVE:
						performReactiveApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_KAMIKAZEE:
						performKamikazeeApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_HESITANT:
						performHesitantApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_FOLLOW:
						performFollowApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_MOVE_TO_POINT:
						performMoveToApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_TARGET:
						performFollowApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
					case AI.APPROACH_WANDER:
						performWanderApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
						break;
				}
			}
		}

		// If we found someone to target then add actions to affect that target
		if (confidence.target != null)
		{
			Log.debug("Move to attack the target from attack point " + confidence.attackPoint.x + ", " + confidence.attackPoint.y);
			Log.debug("Attack " + confidence.target.getName() + " at " + confidence.target.getTileX() + ", " + confidence.target.getTileY());
			ms.addMoveActionsToLocation(confidence.attackPoint.x, confidence.attackPoint.y, currentSprite, turnActions);
			if (confidence.potentialAttackSpriteAction != null) {
				turnActions.add(new WaitAction(500));
				turnActions.add(new TargetSpriteAction(confidence.potentialAttackSpriteAction.getBattleCommand(),
						confidence.target));
				turnActions.add(new WaitAction(500));
				turnActions.add(confidence.potentialAttackSpriteAction);
			}
			else {
				turnActions.add(new WaitAction(250));
				turnActions.add(new HideMoveAreaAction());
				turnActions.add(new WaitAction(250));
				turnActions.add(new EndTurnAction());
			}
		}
		else
		{
			turnActions.add(new WaitAction(250));
			turnActions.add(new HideMoveAreaAction());
			turnActions.add(new WaitAction(250));
			turnActions.add(new EndTurnAction());
		}


		return turnActions;
	}

	private void performWanderApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{
		int dir = TacticalGame.RANDOM.nextInt(4);
		int x = (int) currentSprite.getLocX();
		int y = (int) currentSprite.getLocY();
		switch (dir) {
			case 0:
				x -= tileWidth;
				break;
			case 1:
				x += tileWidth;
				break;
			case 2:
				y += tileHeight;
				break;
			case 3:
				y -= tileHeight;
				break;
		}
		
		if (ms.canEndMoveHere(x / tileWidth, y / tileHeight))
			ms.addMoveActionsAlongPath(x, y, currentSprite, turnActions);
	}

	private void performKamikazeeApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{
		CombatSprite target = this.getMostIsolatedHero(stateInfo, tileWidth, tileHeight, currentSprite, ms);
		ms.addMoveActionsAlongPath((int) target.getLocX(), (int) target.getLocY(), currentSprite, turnActions);
	}

	private void performHesitantApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{
		CombatSprite target = this.getMostIsolatedHero(stateInfo, tileWidth, tileHeight, currentSprite, ms);
		int move = 3;
		int rand = TacticalGame.RANDOM.nextInt(5);
		if (rand == 0)
			move = 2;
		else if (rand == 4)
			move = 4;

		ms.addMoveActionsAlongPath((int) target.getLocX(), (int) target.getLocY(), currentSprite, turnActions, move);
	}

	private void performReactiveApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{

	}

	private void performFollowApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{
		if (targetCS.getCurrentHP() > 0)
			ms.addMoveActionsAlongPath((int) targetCS.getLocX(), (int) targetCS.getLocY(), currentSprite, turnActions);
		else
		{
			this.approachType = AI.APPROACH_HESITANT;

			performHesitantApproach(stateInfo, tileWidth, tileHeight, currentSprite, ms, turnActions);
		}
	}

	private void performMoveToApproach(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms, ArrayList<TurnAction> turnActions)
	{
		ms.addMoveActionsAlongPath(targetPoint.x, targetPoint.y, currentSprite, turnActions);
	}

	protected CombatSprite getMostIsolatedHero(StateInfo stateInfo, int tileWidth, int tileHeight, CombatSprite currentSprite, MoveableSpace ms)
	{
		int leastAmt = Integer.MAX_VALUE;
		CombatSprite mostIsolatedCS = currentSprite;
		for (CombatSprite cs : stateInfo.getCombatSprites())
		{
			if (cs.isHero() == currentSprite.isHero())
				continue;
			int amt = getNearbySpriteAmount(stateInfo, !currentSprite.isHero(), tileWidth, tileHeight, new Point(cs.getTileX(), cs.getTileY()), 5, currentSprite);
			if (amt < leastAmt && ms.doesPathExist(currentSprite.getTileX(), currentSprite.getTileY(), cs.getTileX(), cs.getTileY()))
			{
				leastAmt = amt;
				mostIsolatedCS = cs;
			}
		}

		return mostIsolatedCS;
	}

	/**
	 * Gets the safest point to move to in a moveable area. If retreat is true,
	 * then this point will be as close to the enemies (this entities allies) and furthest from
	 * all heroes (this entities enemies). If retreat = false then this point will be closest
	 * to the heroes (this entities enemies) and closest to all enemies (this entities allies).
	 *
	 * @param stateInfo The StateInfo for the current game state
	 * @param tileWidth The width of a tile on the map
	 * @param tileHeight The height of a tile on the map
	 * @param attacker The entity for which AI is being performed for. (This is just used to omit the distance of this entity from the selected space)
	 * @param ms The space that is entity is able to move in
	 * @param retreat A boolean indicating whether the point should be as far from heroes (this entities enemies) if true,
	 * 			or whether the point should be as close to the heroes (this entities enemies) if false
	 * @return the safest point to move to in a moveable area.
	 */
	private Point getBestPoint(StateInfo stateInfo, int tileWidth, int tileHeight,
			CombatSprite attacker, MoveableSpace ms, boolean retreat, int searchRange)
	{
		int maxDistance = Integer.MIN_VALUE;

		if (!retreat)
			maxDistance = Integer.MAX_VALUE;

		int tx = attacker.getTileX();
		int ty = attacker.getTileY();

		Point bestPoint = new Point(tx, ty);

		for (int i = -searchRange; i <= searchRange; i++)
		{
			for (int j = -searchRange; j <= searchRange; j++)
			{
				if ((Math.abs(i) + Math.abs(j)) <= searchRange &&  ms.canEndMoveHere(tx + i, ty + j) &&
						ms.isTileWithinMove((tx + i) * tileWidth, (ty + j) * tileHeight, attacker, searchRange))
				{
					int heroDistance = getDistanceFromSprites(stateInfo, !attacker.isHero(), tileWidth, tileHeight, tx + i, ty + j, attacker);
					int enemyDistance = getDistanceFromSprites(stateInfo, attacker.isHero(), tileWidth, tileHeight, tx + i, ty + j, attacker);

					int distance = 0;
					if (retreat)
					{
						// Try and maximize this distance
						distance = heroDistance - enemyDistance;
						if (distance > maxDistance)
						{
							maxDistance = distance;
							bestPoint = new Point(tx + i, ty + j);
						}
					}
					else
					{
						// Try and minimize this distance
						distance = heroDistance + (int)(enemyDistance * .1);
						if (distance < maxDistance)
						{
							maxDistance = distance;
							bestPoint = new Point(tx + i, ty + j);
						}
					}
				}
			}
		}

		Log.debug("Get Best Point: " + attacker.getName() + " Distance " + maxDistance);

		return bestPoint;
	}

	/**
	 * Gets a number representing the total distance ALL heroes/enemies are from a given location.
	 * If "isHero" is true then this number represents the distance this space is from all heroes,
	 * otherwise this number represents the distance this space is from all enemies. This can be used
	 * to determine which spaces are furthest/closest to heroes/enemies.
	 *
	 * @param stateInfo
	 * @param isHero If true, the distance returned will represent the distance from all heroes, if false it will be for all heroes
	 * @param tileWidth The width of a tile on the map
	 * @param tileHeight The height of a tile on the map
	 * @param x The x index of the tile to be checked (Not the location that the tile is drawn)
	 * @param y The y index of the tile to be checked (Not the location that the tile is drawn)
	 * @param attacker The entity for which AI is being performed for. (This is just used to omit the distance of this entity from the selected space)
	 * @return The total distance of all heroes/enemies from the specified space.
	 */
	private static int getDistanceFromSprites(StateInfo stateInfo, boolean isHero,
			int tileWidth, int tileHeight, int x, int y, CombatSprite attacker)
	{
		int distance = 0;

		for (CombatSprite target : stateInfo.getCombatSprites())
		{
			if (target == attacker)
				continue;

			if (target.isHero() == isHero)
			{
				int tx = target.getTileX();
				int ty = target.getTileY();

				distance += Math.abs(x - tx) + Math.abs(y - ty);
			}
		}

		return distance;

	}

	/**
	 *
	 *
	 * @param stateInfo
	 * @param isHero
	 * @param tileWidth
	 * @param tileHeight
	 * @param point
	 * @param range
	 * @param attacker
	 * @return
	 */
	protected int getNearbySpriteAmount(StateInfo stateInfo, boolean isHero,
			int tileWidth, int tileHeight, Point point, int range, CombatSprite attacker)
	{
		int count = 0;

		for (CombatSprite target : stateInfo.getCombatSprites())
		{
			if (target == attacker)
				continue;

			if (target.isHero() == isHero)
			{
				int tx = target.getTileX();
				int ty = target.getTileY();

				if (Math.abs(point.x - tx) + Math.abs(point.y - ty) <= range)
					count++;
			}
		}

		return count;
	}

	protected ArrayList<CombatSprite> getNearbySprites(StateInfo stateInfo, boolean isHero,
			int tileWidth, int tileHeight, Point point, int range, CombatSprite attacker)
	{
		ArrayList<CombatSprite> css = new ArrayList<CombatSprite>();

		for (CombatSprite target : stateInfo.getCombatSprites())
		{
			if (target == attacker)
				continue;

			if (target.isHero() == isHero)
			{
				int tx = target.getTileX();
				int ty = target.getTileY();

				if (Math.abs(point.x - tx) + Math.abs(point.y - ty) <= range)
					css.add(target);
			}
		}

		return css;
	}

	/**
	 * Gets a list of all sprites that are in a targetable range from at least one point in the attackers current moveable-space
	 *
	 * @param stateInfo The StateInfo for the current game state
	 * @param attacker The combat sprite that AI is being performed for
	 * @param moveableSpace The moveable space of the attacker combat sprite
	 * @param maxAttackRange The maximum range that should be searched for targetable sprites
	 * @return A list of all sprites that are in a targetable range from at least one point in the attackers current moveable-space. The list
	 * 			will be empty if no sprites are in range
	 */
	private ArrayList<AttackableEntity> getAttackableSprites(StateInfo stateInfo, CombatSprite attacker,
			MoveableSpace moveableSpace, int maxAttackRange)
	{
		ArrayList<AttackableEntity> combatSprites = new ArrayList<AttackableEntity>();

		for (CombatSprite s : stateInfo.getCombatSprites())
		{
			if (s == attacker || !isInVisionRange(attacker, s, vision))
				continue;
			AttackableEntity as = determineAttackEntity(attacker, s, maxAttackRange, moveableSpace, stateInfo);
			if (as != null)
				combatSprites.add(as);
		}

		return combatSprites;
	}

	public int getApproachType() {
		return approachType;
	}

	public void setApproachType(int approachType) {
		this.approachType = approachType;
	}

	public void setApproachType(int approachType, Point p) {
		this.approachType = approachType;
		this.targetPoint = p;
	}

	public void setApproachType(int approachType, CombatSprite cs) {
		this.approachType = approachType;
		this.targetCS = cs;
	}

	/**
	 * Gets an AttackableEntity containing the locations from which the specified target combat-sprite is in range for the attacking sprite
	 *
	 * @param attacking The combat sprite that AI is being performed for
	 * @param target The combat sprite that is being checked for attackability
	 * @param maxAttackRange The maximum range that should be searched for targetable sprites
	 * @param ms The moveable space of the attacker combat sprite
	 * @param stateInfo The StateInfo for the current game state
	 * @return An AttackableEntity containing the locations from which the specified target entity is in range for the attacking entity, null
	 *			if the target is not in range
	 */
	private static AttackableEntity determineAttackEntity(CombatSprite attacking, Sprite target, int maxAttackRange, MoveableSpace ms, StateInfo stateInfo)
	{
		int tx = target.getTileX();
		int ty = target.getTileY();

		// TODO There are dymamic ranges....
		AttackableEntity attackable = null;

		for (int i = -maxAttackRange; i < maxAttackRange + 1; i++)
		{
			for (int j = -maxAttackRange; j < maxAttackRange + 1; j++)
			{
				int range = Math.abs(i) + Math.abs(j);
				if (Math.abs(i) + Math.abs(j) <= maxAttackRange && ms.canEndMoveHere(tx + i, ty + j))
				{
					if (attackable != null)
						attackable.addAttackablePoint(new Point(tx + i, ty + j), range);
					else
						attackable = new AttackableEntity((CombatSprite) target, new Point(tx + i, ty + j), range);
				}
			}
		}

		return attackable;
	}
	
	private boolean isInVisionRange(CombatSprite attacker, Sprite target, int vision) {
		return Math.abs(attacker.getTileX() - target.getTileX()) + Math.abs(attacker.getTileY() - target.getTileY()) <= vision;
	}

	/**
	 * Describes a CombatSprite that is in range of the sprite that AI is being performed for.
	 * Includes all points from which the target is in range and the distance the attacker will
	 * be from the taret when at a given point
	 */
	private static class AttackableEntity
	{
		private CombatSprite combatEntity;
		private ArrayList<Point> attackablePoints;
		private ArrayList<Integer> distances;

		public AttackableEntity(CombatSprite combatSprite, Point attackablePoint, int distance)
		{
			this.combatEntity = combatSprite;
			this.attackablePoints = new ArrayList<Point>();
			this.attackablePoints.add(attackablePoint);
			this.distances = new ArrayList<Integer>();
			distances.add(distance);
		}

		public void addAttackablePoint(Point attackPoint, int distance)
		{
			attackablePoints.add(attackPoint);
			distances.add(distance);
		}

		public CombatSprite getCombatSprite() {
			return combatEntity;
		}

		public ArrayList<Point> getAttackablePoints() {
			return attackablePoints;
		}

		public ArrayList<Integer> getDistances() {
			return distances;
		}
	}

	protected abstract AttackSpriteAction getPerformedTurnAction(CombatSprite target);

	protected abstract int getMaxRange(CombatSprite currentSprite);

	protected abstract AIConfidence getConfidence(CombatSprite currentSprite, CombatSprite targetSprite,
			int tileWidth , int tileHeight, Point attackPoint, int distance, StateInfo stateInfo);

	public void initialize(CombatSprite puppet) {
		this.vision = Math.max(vision, getMaxRange(puppet));
	}
	

	protected int getLandEffectConfidence(Point actionPoint, CombatSprite currentSprite, StateInfo stateInfo)
	{
		return getLandEffectWeight(stateInfo.getCurrentMap().getLandEffectByTile(currentSprite.getMovementType(),
				actionPoint.x, actionPoint.y));
	}

	protected abstract int getLandEffectWeight(int landEffect);

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setVision(int vision) {
		this.vision = vision;
	}
	
	public int getVision() {
		return vision;
	}
}
