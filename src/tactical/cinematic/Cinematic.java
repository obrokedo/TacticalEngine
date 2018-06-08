package tactical.cinematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.pathfinding.Path;

import tactical.cinematic.event.CinematicEvent;
import tactical.cinematic.event.CinematicEvent.CinematicEventType;
import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.IntMessage;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpeechMessage;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.state.CinematicState;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.constants.Direction;
import tactical.game.exception.BadResourceException;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.input.UserInput;
import tactical.game.menu.Portrait;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.AnimatedSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.NPCSprite;
import tactical.game.sprite.Sprite;
import tactical.game.sprite.StaticSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.map.Map;
import tactical.utils.StringUtils;

/**
 * Defines CinematicEvents that should be executed to display a cinematic and handles/renders
 * the results of each CinematicEvent
 *
 * @author Broked
 *
 */
public class Cinematic {

	/**
	 * List of CinematicEvents that will be run before the scene is actually started
	 */
	private ArrayList<CinematicEvent> initializeEvents;

	/**
	 * List of CinematicEvents that will be run one-by-one starting from index
	 * 0 once the scene has started (initialize events have finished)
	 */
	private ArrayList<CinematicEvent> cinematicEvents;

	/**
	 * Table that contains all of the CinematicActors currently in the scene
	 * and maps them to their name
	 */
	private Hashtable<String, CinematicActor> actors;

	/**
	 * A list of CinematicActors sorted in the order that they should
	 * be rendered on the screen in the normal fashion
	 */
	private ArrayList<CinematicActor> sortedActors;

	/**
	 * A list of CinematicActors that are rendered in the forefont of
	 * the scene.
	 */
	private ArrayList<CinematicActor> forefrontActors;

	/**
	 * The amount of CombatSprites currently on the screen who are performing "halting moves".
	 * While this value is non-zero the scene is "blocked"
	 */
	private int haltedMovers;

	/**
	 * The amount of CombatSprites currently on the screen who are performing "halting animations".
	 * While this value is non-zero the scene is "blocked"
	 */
	private int haltedAnims;

	/**
	 * The amount of time in milliseconds that scene is "waiting", while this value
	 * is non-zero the scene is "blocked"
	 */
	private int waitTime;
	
	/**
	 * Whether this cinematic is happening in the "CinematicState" if so
	 * the cameras location will be initialized and non-offset sprites will be assumed
	 */
	private boolean inCinematicState = true;

	/*************************/
	/* Scene Fade Parameters */
	/*************************/
	/**
	 * The color that the screen is fading to
	 */
	private Color fadingColor = null;

	/**
	 * A float that indicates the amount that the fade color will change
	 * on each fade delta
	 */
	private float fadeSpeed;

	/**
	 * A boolean indicating whether the scene is "fading in", if true
	 * the scene is fading in, if false the scene is fading out
	 */
	private boolean fadeIn;

	/**
	 * A long that indicates the amount of ms that have passed since the last
	 * fade step occurred
	 */
	private long fadeDelta;

	/**
	 * A boolean indicating whether the scene is fading to black. If true
	 * then the screen will not automatically fade back in after fading to
	 * black. Otherwise the screen will fade back in
	 */
	private boolean fadeToBlack = false;

	/*********************/
	/* Camera parameters */
	/*********************/
	/**
	 * The amount of time in ms that should pass been each camera move step
	 */
	private static final int CAMERA_UPDATE = 30;

	/**
	 * The CinematicActor that the camera is currently following.
	 * This value will be null if the camera should not follow an Actor
	 */
	private CinematicActor cameraFollow;

	/**
	 * The x and y pixel coordinates that the camera should be moving to as
	 * ordered by a cinematic event.
	 * These values will BOTH be -1 when the camera is not moving to a given
	 * location.
	 */
	private int cameraMoveToX = -1, cameraMoveToY = -1;

	/**
	 * The x and y pixel coordinates that the camera should start at when
	 * the cinematic begins
	 */
	private int cameraStartX, cameraStartY;

	/**
	 * The amount of time in ms since the camera has moved
	 */
	private long cameraMoveDelta = 0;

	/**
	 * A float indicating the speed that the camera will move in pixels
	 * during each camera move step
	 */
	private float cameraMoveSpeed;

	/**
	 * A boolean indicating whether the camera is currently "shaking"
	 */
	private boolean cameraShaking = false;

	/**
	 * The amount of time in ms that the camera should continue to shake
	 */
	private int cameraShakeDuration;

	/**
	 * An integer that indicates how severe the shake will, a larger number
	 * will result in "shakes" of a greater magnitude in terms of pixels offset
	 */
	private int cameraShakeSeverity;

	/**
	 * The amount of pixels that the last shake offset the camera in the x and y direction
	 */
	private int lastCameraShake;

	private ArrayList<StaticSprite> staticSprites = new ArrayList<StaticSprite>();

	private boolean rendering = false;

	/**
	 * Constructor to create the Cinematic with the given events and
	 * the given camera start position
	 *
	 * @param initializeEvents a list of CinematicEvents that should be executed before the cinematic starts
	 * @param cinematicEvents a list of CinematicEvents that should be run one-by-one
	 * @param cameraX The start x location in pixels for the camera
	 * @param cameraY The start y location in pixels for the camera
	 */
	public Cinematic(ArrayList<CinematicEvent> initializeEvents,
			ArrayList<CinematicEvent> cinematicEvents, int cameraX, int cameraY) {
		this.initializeEvents = initializeEvents;
		this.cinematicEvents = cinematicEvents;
		actors = new Hashtable<String, CinematicActor>();
		this.sortedActors = new ArrayList<CinematicActor>();
		this.forefrontActors = new ArrayList<CinematicActor>();
		this.haltedMovers = 0;
		this.haltedAnims = 0;
		this.waitTime = 0;
		this.cameraStartX = cameraX;
		this.cameraStartY = cameraY;
	}
	
	public Cinematic duplicateCinematic() {
		return new Cinematic(new ArrayList<CinematicEvent>(initializeEvents), 
				new ArrayList<CinematicEvent>(cinematicEvents), cameraStartX, cameraStartY);
	}

	/**
	 * Initializes the Cinematic and runs any CinematicEvents that are
	 * marked "initialize"
	 *
	 * @param stateInfo the StateInfo that holds information for the current state
	 * @param inCinematicState whether we are in the cinematic state or not,
	 * 			if so the cameras location will be initialized and non-offset tile location will be assumed
	 */
	public void initialize(StateInfo stateInfo, boolean inCinematicState) {
		this.inCinematicState = inCinematicState;
		if (inCinematicState)
			stateInfo.getCamera().setLocation(cameraStartX, cameraStartY, stateInfo);

		for (CinematicEvent ce : initializeEvents)
			handleEvent(ce, stateInfo);
	}

	/**
	 * Updates the Cinematic to account for the change in engine time as represented by
	 * the given delta
	 *
	 * @param delta the amount of time in ms that has passed since the last update
	 * @param camera the camera that defines what portion of the scene will be rendered
	 * @param input the FCInput that indicates what keys the user is pushing
	 * @param map the Map that this scene will be rendered over
	 * @param stateInfo the StateInfo that holds information for the current state
	 * @return a boolean indicating whether this cinematic is completed or not. A value of true
	 * means the cinematic has been completed, false otherwise.
	 */
	public boolean update(int delta, Camera camera, UserInput input, Map map, StateInfo stateInfo) {

		if (!rendering)
			return false;

		if (fadingColor != null)
		{
			fadeDelta += delta;
			while (fadingColor != null && fadeDelta >= 50)
			{
				fadeDelta -= 50;

				if (fadeIn)
				{
					fadingColor.a = Math.max(0, fadingColor.a - fadeSpeed);
					if (fadingColor.a == 0)
						fadingColor = null;
				}
				else
				{
					fadingColor.a = Math.min(1, fadingColor.a + fadeSpeed);
					if (fadingColor.a == 1)
					{
						if (fadeToBlack)
							fadingColor = null;
						else
							fadeIn = true;
					}
				}
			}
		}

		if (waitTime > 0)
			waitTime = Math.max(0, waitTime - delta);

		if (cameraMoveToX != -1) {
			cameraMoveDelta += delta;

			while (cameraMoveToX != -1 && cameraMoveDelta > CAMERA_UPDATE) {
				cameraMoveDelta -= CAMERA_UPDATE;
				float xDel = 0;
				float yDel = 0;
				if (cameraMoveToX > camera.getLocationX())
					xDel = Math.min(cameraMoveSpeed,
							cameraMoveToX - camera.getLocationX());
				else if (cameraMoveToX < camera.getLocationX())
					xDel = -Math.min(cameraMoveSpeed, camera.getLocationX()
							- cameraMoveToX);

				if (cameraMoveToY > camera.getLocationY())
					yDel = Math.min(cameraMoveSpeed,
							cameraMoveToY - camera.getLocationY());
				else if (cameraMoveToY < camera.getLocationY())
					yDel = -Math.min(cameraMoveSpeed, camera.getLocationY()
							- cameraMoveToY);

				if ((xDel != 0 || yDel != 0) 
						// Make sure we're not trying to pan off the screen in
						&& camera.canMoveTowards(cameraMoveToX, cameraMoveToY, stateInfo)) {
					camera.setLocation(camera.getLocationX() + xDel,
							camera.getLocationY() + yDel, stateInfo);
				} else {
					cameraMoveToX = -1;
					cameraMoveToY = -1;
				}
			}
		}

		for (CinematicActor ca : actors.values())
		{
			ca.update(delta, this, stateInfo);
			if (ca == cameraFollow)
			{
				camera.centerOnPoint(cameraFollow.getLocX(),
						cameraFollow.getLocY() + 
						(inCinematicState ? 0 : stateInfo.getResourceManager().getMap().getTileRenderHeight()), 
						map);
			}
		}

		Collections.sort(sortedActors);

		if (cameraShaking) {
			cameraShakeDuration -= delta;
			// Reset the camera to the correct location
			camera.setLocation(camera.getLocationX() - lastCameraShake,
					camera.getLocationY() - lastCameraShake, stateInfo);
			if (cameraShakeDuration > 0) {
				int newCameraShake = TacticalGame.RANDOM
						.nextInt(cameraShakeSeverity * 2) - cameraShakeSeverity;
				camera.setLocation(camera.getLocationX() + newCameraShake,
						camera.getLocationY() + newCameraShake, stateInfo);
				lastCameraShake = newCameraShake;
			} else
				cameraShaking = false;
		}

		// If nothing is currently blocking then continue processing the
		// cinematics
		while (waitTime == 0 && haltedAnims == 0 && haltedMovers == 0
				&& cameraMoveToX == -1 && !stateInfo.isMenuDisplayed(PanelType.PANEL_SPEECH)
				&& cinematicEvents.size() > 0) {
			CinematicEvent ce = cinematicEvents.remove(0);
			handleEvent(ce, stateInfo);
		}

		return waitTime == 0 && haltedAnims == 0 && haltedMovers == 0
				&& cameraMoveToX == -1 && !stateInfo.isMenuDisplayed(PanelType.PANEL_SPEECH)
				&& cinematicEvents.size() == 0;
	}
	
	private CinematicActor getCinematicActorByName(String actorName, CinematicEventType cinematicEventType) {
		CinematicActor ca = actors.get(actorName);
		if (ca == null)
			throw new BadResourceException(String.format("A cinematic event of type: %s referenced an actor name %s before that actor has been defined in the scene\n. Either add the actor by that name to the scene prior to this or ensure that the name is spelled correctly", cinematicEventType.toString(), actorName));
		return ca;
	}

	/**
	 * Processes a given CinematicEvent
	 *
	 * @param ce the CinematicEvent that should be processed
	 * @param stateInfo the StateInfo that holds information for the current state
	 */
	private void handleEvent(CinematicEvent ce, StateInfo stateInfo) {
		Log.debug("Handle event: " + ce.getType());
		CinematicActor ca = null;
		switch (ce.getType()) {
			case HALTING_MOVE:
				ca = getCinematicActorByName((String) ce.getParam(3), ce.getType());
				moveActorToLocation(ca, (int) ce.getParam(0),
						(int) ce.getParam(1), (float) ce.getParam(2), true, -1, (boolean) ce.getParam(4), (boolean) ce.getParam(5), stateInfo);
				haltedMovers++;
				break;
			case HALTING_MOVE_PATHFIND:
				if (actors.containsKey((String) ce.getParam(3))) {
					ca = getCinematicActorByName((String) ce.getParam(3), ce.getType());
					
					Path path = stateInfo.getCurrentMap().findPixelPathWithPixels
							((int) ca.getLocX(), (int) ca.getLocY() + 
									(!this.inCinematicState ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0), 
									(int) ce.getParam(0), (int) ce.getParam(1), stateInfo, true);
					
					if (path == null) {
						path = stateInfo.getCurrentMap().findPixelPathWithPixels
								((int) ca.getLocX(), (int) ca.getLocY() + 
										(!this.inCinematicState ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0), 
										(int) ce.getParam(0), (int) ce.getParam(1), stateInfo, false);
					}
					
					if (path != null) {
						ca.moveAlongPath(path, (float) ce.getParam(2), true, stateInfo);
						haltedMovers++;
					}
				}
				break;
			case MOVE_PATHFIND:
				if (actors.containsKey((String) ce.getParam(3))) {
					ca = getCinematicActorByName((String) ce.getParam(3), ce.getType());
					
					 Path path = stateInfo.getCurrentMap().findPixelPathWithPixels
							((int) ca.getLocX(), (int) ca.getLocY() + 
									(!this.inCinematicState ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0), 
									(int) ce.getParam(0), (int) ce.getParam(1), stateInfo, true);
					
					if (path == null) {
						path = stateInfo.getCurrentMap().findPixelPathWithPixels
								((int) ca.getLocX(), (int) ca.getLocY() + 
										(!this.inCinematicState ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0), 
										(int) ce.getParam(0), (int) ce.getParam(1), stateInfo, false);
					}
					
					if (path != null) {
						ca.moveAlongPath(path, (float) ce.getParam(2), false, stateInfo);
					}
				}
				break;
			case MOVE:
				ca = getCinematicActorByName((String) ce.getParam(3), ce.getType());
				moveActorToLocation(ca, (int) ce.getParam(0),
						(int) ce.getParam(1), (float) ce.getParam(2), false, -1, (boolean) ce.getParam(4), (boolean) ce.getParam(5), stateInfo);
				break;
			case MOVE_ENFORCE_FACING:
				ca = getCinematicActorByName((String) ce.getParam(3), ce.getType());
				moveActorToLocation(ca, (int) ce.getParam(0),
						(int) ce.getParam(1), (float) ce.getParam(2), false,
						(int) ce.getParam(4), (boolean) ce.getParam(5), (boolean) ce.getParam(6), stateInfo);
				break;
			case LOOP_MOVE:
				ca = getCinematicActorByName((String) ce.getParam(0), ce.getType());
				ca.loopMoveToLocation((int) ce.getParam(1),
						(int) ce.getParam(2), (float) ce.getParam(3));
				break;
			case STOP_LOOP_MOVE:
				ca = getCinematicActorByName((String) ce.getParam(0), ce.getType());
				ca.stopLoopMove();
				break;
			case CAMERA_MOVE:
				cameraMoveToX = (int) ce.getParam(0)
						- stateInfo.getCamera().getViewportWidth() / 2;
				cameraMoveToY = (int) ce.getParam(1)
						- stateInfo.getCamera().getViewportHeight() / 2;
				float distance = Math.abs(stateInfo.getCamera().getLocationX()
						- cameraMoveToX);
				distance += Math.abs(stateInfo.getCamera().getLocationY()
						- cameraMoveToY);
				cameraMoveSpeed = distance / (1.0f * ((int) ce.getParam(2)) / CAMERA_UPDATE);
				cameraFollow = null;
				break;
			case CAMERA_MOVE_TO_ACTOR:
				ca = actors.get(ce.getParam(0));
				cameraMoveToX = (int) (ca.getLocX()
						- stateInfo.getCamera().getViewportWidth() / 2);
				cameraMoveToY = (int) (ca.getLocY()
						- stateInfo.getCamera().getViewportHeight() / 2) 
						+ (inCinematicState ? 0 : stateInfo.getResourceManager().getMap().getTileRenderHeight());
				distance = Math.abs(stateInfo.getCamera().getLocationX()
						- cameraMoveToX);
				distance += Math.abs(stateInfo.getCamera().getLocationY()
						- cameraMoveToY);
				cameraMoveSpeed = distance / (1.0f * ((int) ce.getParam(1)) / CAMERA_UPDATE);
				cameraFollow = null;
				break;
			case CAMERA_CENTER:
				stateInfo
						.getCamera()
						.centerOnPoint(
								(int) ce.getParam(0),
								(int) ce.getParam(1),
								stateInfo.getCurrentMap());
				cameraFollow = null;
				break;
			case CAMERA_FOLLOW:
				ca = getCinematicActorByName((String) ce.getParam(0), ce.getType());
				cameraFollow = ca;
				stateInfo.getCamera().centerOnPoint(cameraFollow.getLocX(),
						cameraFollow.getLocY() + (inCinematicState ? 0 : stateInfo.getResourceManager().getMap().getTileRenderHeight()), stateInfo.getCurrentMap());
				break;
			case CAMERA_SHAKE:
				lastCameraShake = 0;
				cameraShakeDuration = (int) ce.getParam(0);
				cameraShakeSeverity = (int) ce.getParam(1);
				cameraShaking = true;
				break;
			case ADD_ACTOR:
				boolean isHeroAss = false;
				boolean isHeroPro = false;

				int associated = (int) ce.getParam(6);
				if (associated >= 0)
				{
					
					CombatSprite cs = stateInfo.getHeroById(associated);
					if (cs != null)
					{
						isHeroAss = true;
						isHeroPro = cs.isPromoted();
					}

					if (!isHeroAss)
					{
						throw new BadResourceException("Attempted to add an actor to the scene that is associated with a hero that has\n not yet joined the party. If you want the association to work then you first must add them to the force");
					}
				}

				ca = new CinematicActor(
						stateInfo.getResourceManager()
						.getSpriteAnimation((String) ce.getParam(3)), (String) ce
						.getParam(4), (int) ce.getParam(0),
				(int) ce.getParam(1), (boolean) ce.getParam(5),
				isHeroAss, isHeroPro);



				actors.put(
						(String) ce.getParam(2), ca);
				sortedActors.add(ca);
				break;
			case ADD_STATIC_SPRITE:
				Image im = stateInfo.getResourceManager().getImage((String) ce.getParam(3));
				if (im == null)
					throw new BadResourceException(String.format("Attempted to add a static sprite with an image that has not loaded: %s \n. Static sprite images must appear in the 'sprite' resource folder and ARE case sensitive", ce.getParam(3)));
				staticSprites.add(new StaticSprite((int) ce.getParam(0),
						(int) ce.getParam(1),
						(String) ce.getParam(2), im, null));
				break;
			case REMOVE_STATIC_SPRITE:
				String toRem = (String) ce.getParam(0);
				for (int i = 0; i < staticSprites.size(); i++)
					if (staticSprites.get(i).getName().equalsIgnoreCase(toRem))
						staticSprites.remove(i--);

				break;
			case ASSOCIATE_AS_ACTOR:
				ca = null;
				if ((int) ce.getParam(1) >= 0)
				{
					for (Sprite s : stateInfo.getSprites()) {
						if (s.getSpriteType() == Sprite.TYPE_COMBAT
								&& ((CombatSprite) s).isHero()
								&& ((CombatSprite) s).getId() == (int) ce
										.getParam(1)) {
							ca = new CinematicActor(
									(AnimatedSprite) s, stateInfo);
							break;
						}
					}
				}
				else if ((int) ce.getParam(2) != 0)
				{
					for (Sprite s : stateInfo.getSprites()) {
						if (s.getSpriteType() == Sprite.TYPE_COMBAT
								&& !((CombatSprite) s).isHero()
								&& ((CombatSprite) s).getUniqueEnemyId() == (int) ce
										.getParam(2)) {
							ca = new CinematicActor(
									(AnimatedSprite) s, stateInfo);
							break;
						}
					}
				}
				else if (StringUtils.isNotEmpty((String) ce.getParam(3)))
				{
					for (Sprite s : stateInfo.getSprites()) {
						if (s.getSpriteType() == Sprite.TYPE_NPC
								&& ((NPCSprite) s).getName() != null
								&& ((NPCSprite) s).getName().equalsIgnoreCase((String) ce.getParam(3))) {
							ca = new CinematicActor(
									(AnimatedSprite) s, stateInfo);
							break;
						}
					}
				}

				if (ca != null)
				{
					actors.put((String) ce.getParam(0), ca);
					sortedActors.add(ca);
				}

				break;
			case ASSOCIATE_NPC_AS_ACTOR:
				for (Sprite s : stateInfo.getSprites()) {
					if (s.getSpriteType() == Sprite.TYPE_NPC
							&& ((NPCSprite) s).getUniqueNPCId() == (int) ce
									.getParam(0)) {
						actors.put((String) ce.getParam(1), new CinematicActor(
								(AnimatedSprite) s, stateInfo));
						break;
					}
				}
				break;
			case SPEECH:
				int heroPortrait = (int) ce.getParam(1);
				int enemyPortrait = (int) ce.getParam(2);
				String specificAnim = (String) ce.getParam(3);
				if (StringUtils.isEmpty(specificAnim))
					specificAnim = null;

				
				Portrait port = Portrait.getPortrait(heroPortrait, enemyPortrait, specificAnim, stateInfo);
				stateInfo.sendMessage(new SpeechMessage((String) ce.getParam(0), Trigger.TRIGGER_NONE, port));
				break;
			case LOAD_MAP:
				stateInfo.getPersistentStateInfo().loadMap((String) ce.getParam(0),
						(String) ce.getParam(1));
				CinematicState.cinematicSpeed = 1;
				break;
			case LOAD_BATTLE:
				stateInfo.getPersistentStateInfo().loadBattle((String) ce.getParam(0), (String) ce.getParam(1), (int) ce.getParam(2));
				CinematicState.cinematicSpeed = 1;
				break;
			case LOAD_CIN:
				if (stateInfo.getClientProgress().getMapData().equalsIgnoreCase((String) ce.getParam(0))) {
					stateInfo.sendMessage(new IntMessage(MessageType.SHOW_CINEMATIC, (int) ce.getParam(1)));
					cinematicEvents.add(new CinematicEvent(CinematicEventType.WAIT, 500));
				}
				else
					stateInfo.getPersistentStateInfo().loadCinematic((String) ce.getParam(0), (int) ce.getParam(1));
				CinematicState.cinematicSpeed = 1;
				break;
			case HALTING_ANIMATION:
				actors.get(ce.getParam(0)).setAnimation(
						(String) ce.getParam(1), (int) ce.getParam(2), true, false);
				haltedAnims++;
				break;
			case ANIMATION:
				actors.get(ce.getParam(0)).setAnimation(
						(String) ce.getParam(1), (int) ce.getParam(2), false,
						(boolean) ce.getParam(3));
				break;
			case WAIT:
				waitTime = (int) ce.getParam(0);
				break;
			case SPIN:
				actors.get(ce.getParam(0)).setSpinning(
						(int) ce.getParam(1), (int) ce.getParam(2));
				break;
			case STOP_SPIN:
				actors.get(ce.getParam(0)).stopSpinning();
				break;
			case SHRINK:
				actors.get(ce.getParam(0)).shrink((int) ce.getParam(1));
				break;
			case GROW:
				actors.get(ce.getParam(0)).grow((int) ce.getParam(1));
				break;
			case QUIVER:
				actors.get(ce.getParam(0)).quiver();
				break;
			case TREMBLE:
				actors.get(ce.getParam(0)).tremble();
				break;
			case LAY_ON_BACK:
				actors.get(ce.getParam(0)).layOnBack(
						Direction.getDirectionFromInt((int) ce.getParam(1)));
				break;
			case LAY_ON_SIDE_RIGHT:
				actors.get(ce.getParam(0)).layOnSideRight(
						Direction.getDirectionFromInt((int) ce.getParam(1)));
				break;
			case LAY_ON_SIDE_LEFT:
				actors.get(ce.getParam(0)).layOnSideLeft(
						Direction.getDirectionFromInt((int) ce.getParam(1)));
				break;
			case FALL_ON_FACE:
				actors.get(ce.getParam(0)).fallOnFace(
						Direction.getDirectionFromInt((int) ce.getParam(1)));
				break;
			case FLASH:
				actors.get(ce.getParam(0)).flash((int) ce.getParam(1),
						(int) ce.getParam(2));
				break;
			case NOD:
				actors.get(ce.getParam(0)).nodHead();
				break;
			case HEAD_SHAKE:
				actors.get(ce.getParam(0)).shakeHead((int) ce.getParam(1));
				break;
			case STOP_SE:
				actors.get(ce.getParam(0)).stopSpecialEffect();
				break;
			case VISIBLE:
				actors.get(ce.getParam(0)).setVisible(
						(boolean) ce.getParam(1));
				break;
			case REMOVE_ACTOR:
				CinematicActor remAct = actors.remove(ce.getParam(0));
				if (remAct != null) {
					remAct.spriteRemoved(stateInfo);
					sortedActors.remove(remAct);
				}
				break;
			case FACING:
				int dir = (int) ce.getParam(1);
				actors.get(ce.getParam(0)).setFacing(dir);
				break;
			
			case PLAY_MUSIC:
				stateInfo.sendMessage(new AudioMessage(MessageType.PLAY_MUSIC,
						(String) ce.getParam(0), ((int) ce.getParam(1)) / 100.0f,
						true));
				break;
			case PAUSE_MUSIC:
				stateInfo.sendMessage(MessageType.PAUSE_MUSIC);
				break;
			case RESUME_MUSIC:
				stateInfo.sendMessage(MessageType.RESUME_MUSIC);
				break;
			case FADE_MUSIC:
				stateInfo.sendMessage(new IntMessage(MessageType.FADE_MUSIC,
						(int) ce.getParam(0)));
				break;
			case PLAY_SOUND:
				stateInfo.sendMessage(new AudioMessage(
						MessageType.SOUND_EFFECT, (String) ce.getParam(0),
						((int) ce.getParam(1)) / 100.0f, true));
				break;
			case STOP_ANIMATION:
				actors.get(ce.getParam(0)).stopAnimation();
				break;
			case FADE_FROM_BLACK:
				if ((boolean) ce.getParam(2))
				{
					fadingColor = new Color(0f, 0f, 0f, 1f);
					break;
				}

				fadeToBlack = true;
				fadeIn = true;
				fadingColor = new Color(0f, 0f, 0f, 1f);
				fadeDelta = 0;
				fadeSpeed =  1.0f / ((int) ce.getParam(0) / 50f);
				if ((boolean) ce.getParam(1))
					this.waitTime = (int) ce.getParam(0);
				break;
			case FADE_TO_BLACK:
				fadeToBlack = false;
				fadeIn = false;
				fadingColor = new Color(0f, 0f, 0f, 0f);
				fadeDelta = 0;
				fadeSpeed =  1.0f / ((int) ce.getParam(0) / 50f);
				if ((boolean) ce.getParam(1))
					this.waitTime = (int) ce.getParam(0);
				break;
			case FLASH_SCREEN:
				fadeToBlack = false;
				fadeIn = false;
				fadingColor = new Color(1f, 1f, 1f, 0f);
				fadeDelta = 0;
				fadeSpeed =  1.0f / ((((int) ce.getParam(0)) / 2) / 50f);
				break;
			case MOVE_TO_FOREFRONT:
				forefrontActors.add(actors.get(ce.getParam(0)));
				sortedActors.remove(actors.get(ce.getParam(0)));
				break;
			case MOVE_FROM_FOREFRONT:
				sortedActors.add(actors.get(ce.getParam(0)));
				forefrontActors.remove(actors.get(ce.getParam(0)));
				break;
			case SHOW_CREDITS:
				stateInfo.getPersistentStateInfo().getGame().enterState(TacticalGame.STATE_GAME_CREDITS);
				break;
			case EXIT_GAME:
				System.exit(0);
				break;
			case ADD_HERO:
				CombatSprite addHeroSprite = HeroResource.getHero((int) ce.getParam(0));
				addHeroSprite.initializeSprite(stateInfo.getResourceManager());
				stateInfo.getPersistentStateInfo().getClientProfile().addHero(addHeroSprite);
				break;
			default:
				break;
		}
	}
	
	private void moveActorToLocation(CinematicActor ca, int moveToLocX, int moveToLocY, float speed, boolean haltingMove, 
			int direction, boolean moveHorFirst, boolean moveDiag, StateInfo stateInfo)
	{
		ca.moveToLocation(moveToLocX, moveToLocY - (!this.inCinematicState ? stateInfo.getCurrentMap().getTileEffectiveHeight() / 2 : 0), 
				speed, haltingMove, direction, moveHorFirst, moveDiag);;
	}

	/**
	 * Renders the CinematicActors in this scene
	 *
	 * @param graphics the graphics to draw to
	 * @param camera the location of the camera to draw relative to
	 * @param cont the FCGameContainer that this game is displayed in
	 * @param stateInfo the StateInfo that holds information for the current state
	 */
	public void render(Graphics graphics, Camera camera, PaddedGameContainer cont,
			StateInfo stateInfo) {
		rendering = true;

		for (CinematicActor ca : sortedActors)
		{
			ca.render(graphics, camera, cont, stateInfo);
		}

		for (StaticSprite ss : staticSprites)
			ss.render(camera, graphics, cont, stateInfo.getTileHeight());
	}

	/**
	 * Render the foreground actors and effects
	 *
	 * @param graphics the graphics to draw to
	 * @param camera the location of the camera to draw relative to
	 * @param cont the FCGameContainer that this game is displayed in
	 * @param stateInfo the StateInfo that holds information for the current state
	 */
	public void renderPostEffects(Graphics graphics, Camera camera, PaddedGameContainer cont,
			StateInfo stateInfo) {
		for (CinematicActor ca : forefrontActors)
		{
			ca.render(graphics, camera, cont, stateInfo);
		}

		if (fadingColor != null)
		{
			graphics.setColor(fadingColor);
			graphics.fillRect(0, 0, camera.getViewportWidth(), camera.getViewportHeight());
		}


	}

	/**
	 * Decreases the amount of halted movers in the cinematic by 1.
	 * It is the responsibility of the CinematicActor to decrease this value
	 * once the action is complete
	 */
	public void decreaseMoves() {
		haltedMovers = Math.max(haltedMovers - 1, 0);
	}

	/**
	 * Decreases the amount of halted animations in the cinematic by 1.
	 * It is the responsibility of the CinematicActor to decrease this value
	 * once the action is complete
	 */
	public void decreaseAnims() {
		haltedAnims = Math.max(haltedAnims - 1, 0);
	}
	
	/**
	 * Performs the next action in the Cinematic regardless of whether a halting action is occurring or
	 * a menu is being shown 
	 * 
	 * @param stateInfo the stateInfo that defines the state this cinematic is being run in
	 */
	public void nextAction(StateInfo stateInfo)
	{
		CinematicEvent ce = cinematicEvents.remove(0);
		handleEvent(ce, stateInfo);
	}

	/**
	 * Resets all of the actors in the scene to be visible and
	 * offset them by the correct Y amount to make them renderable
	 * in the TownState
	 *
	 * @param stateInfo the StateInfo that holds information for the current state
	 */
	public void endCinematic(StateInfo stateInfo)
	{
		ArrayList<AnimatedSprite> associated = new ArrayList<>();
		for (CinematicActor ca : actors.values()) {
			AnimatedSprite as = ca.resetSprite(stateInfo);
			if (as != null)
				associated.add(as);
		}
		stateInfo.sendMessage(new SpriteContextMessage(MessageType.CIN_END, associated));
	}
	
	
}
