package tactical.utils.planner;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.game.constants.AttributeStrength;
import tactical.utils.DirectoryLister;
import tactical.utils.planner.layout.PlannerEnemyStatLayout;
import tactical.utils.planner.layout.PlannerEquippableItemLayout;
import tactical.utils.planner.layout.PlannerHeroStatLayout;

public class PlannerDefinitions {
	private static String PATH_ANIMATIONS = "animations/animationsheets";
	private static String PATH_WEAPON_ANIMATIONS = "animations/weaponanim";
	private static String PATH_SPRITE_IMAGE = "sprite";
	private static String PATH_PALETTE = "palette";
	private static String PATH_MAPDATA = "mapdata";
	private static String PATH_MUSIC = "music";
	private static String PATH_SOUND = "sound";

	public static void setupDefintions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName)
	{
		setupTriggerDefinition(referenceStore, containersByName);
		setupTextDefinitions(referenceStore, containersByName);
		setupHeroDefinitions(referenceStore, containersByName);
		setupConditionDefinition(referenceStore, containersByName);
		setupEnemyDefinitions(referenceStore, containersByName);
		setupItemDefinitions(referenceStore, containersByName);
		setupQuestDefinitions(referenceStore, containersByName);
		setupCinematicDefinitions(referenceStore, containersByName);
		// setupMapDefinitions(referenceStore, containersByName);
		setupMapEditorDefinitions(referenceStore, containersByName);
	}

	public static void setupRefererList(ReferenceStore referenceStore)
	{
		// TacticalGame.ENGINE_CONFIGURATIOR.initialize();
	
		// Setup AI Types
		referenceStore.addReference(ReferenceStore.REFERS_AI_APPROACH - 1, new PlannerReference("wait"),
				new PlannerReference("fast"), new PlannerReference("slow"), new PlannerReference("wander"));

		referenceStore.addReference(ReferenceStore.REFERS_AI - 1, new PlannerReference("wizard"),
				new PlannerReference("cleric"), new PlannerReference("fighter"));
		
		// Setup progression type
		referenceStore.addReference(ReferenceStore.REFERS_STAT_GAINS - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getLevelProgression().getStandardStatProgressionTypeList());

		// Setup usuable itemstyles
		referenceStore.addReference(ReferenceStore.REFERS_ITEM_STYLE - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getWeaponTypes());

		// Setup usuable item types
		referenceStore.addReference(ReferenceStore.REFERS_ITEM_TYPE - 1, "Weapon", "Ring");

		
		// Setup usuable item ranges
		referenceStore.addReference(ReferenceStore.REFERS_ITEM_RANGE - 1, 
			"Self only",
			"All within 1",
			"All within 2",
			"All within 3",
			"Only at range 2",
			"Only at range 2 and 3",
			"Only at range 3");

		// Setup usuable item areas
		referenceStore.addReference(ReferenceStore.REFERS_ITEM_AREA - 1, 
			"None",
			"One square",
			"Five squares",
			"Thirteen squares",
			"Everyone");

		// Setup movement types
		referenceStore.addReference(ReferenceStore.REFERS_MOVE_TYPE - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getMovementTypes());

		// Setup spells
		referenceStore.addReference(ReferenceStore.REFERS_SPELL - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().getSpellList());

		// Setup Direction
		referenceStore.addReference(ReferenceStore.REFERS_DIRECTION - 1, 
			"Up",
			"Down",
			"Left",
			"Right");

		// Animation files
		setupRefererListFromDir(PATH_ANIMATIONS, ReferenceStore.REFERS_ANIMATIONS, referenceStore, "");
		
		setupRefererListFromDir(PATH_WEAPON_ANIMATIONS, ReferenceStore.REFERS_WEAPON_ANIMATIONS, referenceStore, ".anim");

		// Sprite image files
		setupRefererListFromDir(PATH_SPRITE_IMAGE, ReferenceStore.REFERS_SPRITE_IMAGE, referenceStore, ".png");

		// Setup Battle Effects
		referenceStore.addReference(ReferenceStore.REFERS_EFFECT - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().getBattleEffectList());

		// Setup Attribute Strength
		referenceStore.addReference(ReferenceStore.REFERS_ATTRIBUTE_STRENGTH - 1, 
			new PlannerReference(AttributeStrength.WEAK.name()),
			new PlannerReference(AttributeStrength.MEDIUM.name()),
			new PlannerReference(AttributeStrength.STRONG.name()));

		// Setup Body/Mind progression
		referenceStore.addReference(ReferenceStore.REFERS_BODYMIND_GAIN - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getLevelProgression().getBodyMindProgressionTypeList());
		// Setup Terrain
		EngineConfigurationValues jConfigValues = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
		referenceStore.addReference(ReferenceStore.REFERS_TERRAIN - 1, 
				jConfigValues.getTerrainTypes());

		// Palette files
		setupRefererListFromDir(PATH_PALETTE, ReferenceStore.REFERS_PALETTE, referenceStore);

		// Setup affinities
		referenceStore.addReference(ReferenceStore.REFERS_AFFINITIES - 1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getAffinities());

		// Setup weapon damage types
		referenceStore.addReference(ReferenceStore.REFERS_WEAPON_DAMAGE_TYPE - 1, "NORMAL");
		
		referenceStore.addReference(ReferenceStore.REFERS_WEAPON_DAMAGE_TYPE - 1,
				referenceStore.getReferencesForType(ReferenceStore.REFERS_AFFINITIES - 1));
		
		// Operator list
		referenceStore.addReference(ReferenceStore.REFERS_OPERATOR - 1, 
			"Greater Than",
			"Less Than",
			"Equals");
		
		// Sprite image files
		for (File f : DirectoryLister.listFilesInDir(PATH_MAPDATA))
			if (f.isFile() && !f.isHidden())
				referenceStore.addReference(ReferenceStore.REFERS_MAPDATA - 1, new PlannerReference(f.getName()));
		
		// Music files
		setupRefererListFromDir(PATH_MUSIC, ReferenceStore.REFERS_MUSIC, referenceStore, ".ogg", ".wav");
		// Sound files
		setupRefererListFromDir(PATH_SOUND, ReferenceStore.REFERS_SOUND, referenceStore, ".ogg", ".wav");
	}
	
	private static void setupRefererListFromDir(String path, int referIndex, ReferenceStore referenceStore,
			String... exten) {
		for (File f : DirectoryLister.listFilesInDir(path)) {
			boolean match = exten.length == 0;
			String name = f.getName();
			for (String ext : exten) {
				if (f.getName().endsWith(ext)) {
					match = true;
					name = name.replaceFirst(ext, "");
					break;
				}
			}
			if (match) {
				referenceStore.addReference(referIndex - 1, new PlannerReference(name));
			}
		}
	}

	public static void setupCinematicDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef cinematicContainer;

		ArrayList<String> actorControl = new ArrayList<>();
		ArrayList<String> actorSpecialEffect = new ArrayList<>();
		ArrayList<String> actorMove = new ArrayList<>();
		ArrayList<String> cameraEffect = new ArrayList<>();
		ArrayList<String> sceneControl = new ArrayList<>();
		ArrayList<String> sceneMembership = new ArrayList<>();
		ArrayList<String> soundControl = new ArrayList<>();
		ArrayList<String> textControl = new ArrayList<>();
		ArrayList<String> mapControl = new ArrayList<>();
		ArrayList<String> progressControl = new ArrayList<>();
		
		Hashtable<String, ArrayList<String>> menuLayout = new Hashtable<>();
		menuLayout.put("Scene Membership", sceneMembership);
		menuLayout.put("Scene Control", sceneControl);
		menuLayout.put("Actor Control", actorControl);
		menuLayout.put("Actor Move", actorMove);
		menuLayout.put("Actor Special Effect", actorSpecialEffect);
		menuLayout.put("Speech", textControl);
		menuLayout.put("Camera", cameraEffect);
		menuLayout.put("Sound", soundControl);
		menuLayout.put("Progress", progressControl);
		menuLayout.put("Map", mapControl);
		
		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "description", false,
						"Description",
						"A description of the object that will be presented to the players"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "camerax", false,
				"Camera Start X",
				"The initial X location of the camera (in pixels)"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "cameray", false,
				"Camera Start Y",
				"The initial Y location of the camera (in pixels)"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "skippable", false, "Is Skippable",
				"Whether this cinematic is skippable with the enter key"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "showroofs", false, "Show Roofs ",
				"If true then roofs will be visible in the cinematic"));
		PlannerLineDef definingLine = new PlannerLineDef("cinematic",
				"Cinematic", "", definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Wait
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"time",
						false,
						"Wait Time",
						"The amount of time in ms that the cinematic should wait before more actions are processed"));

		allowableLines
				.add(new PlannerLineDef(
						"wait",
						"Wait",
						"Halts new actions from being processed for the specified time. This is a halting action",
						definingValues));
		sceneControl.add("Wait");

		// Add actor
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", false, "Start Location X",
				"The x coordinate (in pixels) to start the actor in"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", false, "Start Location Y",
				"The y coordinate (in pixels) to start the actor in"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"name",
						false,
						"Actor Name",
						"The name of the actor to be created. This will be used to reference the actor in the cinematic"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"anim", false, "Animation file",
				"The name of the animation file to be used for this actor"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "startanim", false,
				"Starting Animation",
				"The name of the animation that this actor should start in"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "visible", false,
				"Starts Visible", "Whether this actor should start visible"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN,
						"init",
						false,
						"Initialize before Cinematic",
						"Indicates that this action should be taken before the scene is rendered. Blocking actions should NEVER be initialized before the scene"));

		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING,
				"associatedhero",
				true,
				"Associated Hero",
				"The hero that should be used to determine if the unpromoted or promoted version of animations should be used."));

		allowableLines
				.add(new PlannerLineDef(
						"addactor",
						"Add Actor",
						"Adds an actor to the cinematic, this actor can be accessed in the future by its' name",
						definingValues));
		
		sceneMembership.add("Add Actor");

		// Associate Actor With Sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING,
				"name", false, "Actor Name",
				"The name that will be used to reference the actor in the cinematic"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "hero", true, "Associate Hero",
				"If true then the specified hero will be established as a cinematic actor. When in 'town' this only can be used to associate the main character. "
				+ "In 'battle' you must be sure the specified hero is actually in the battle. In 'cinematic' this doesn't really have a use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "enemyid", true, "Enemy ID",
				"The ID of the Enemy that should become a cinematic actor. This should only be used in 'battle'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "npcid", true, "NPC Name",
				"The Name of the NPC that should become a cinematic actor"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "staticid", true, "Static Sprite Name",
				"The Name of the static sprite that should become a cinematic actor"));
		allowableLines.add(new PlannerLineDef("assactor", "Establish Sprite as Actor",
						"Establishes a Sprite (NPC, Enemy, Hero, Static) as an actor. Only one of the options should be specified above", definingValues));

		sceneMembership.add("Establish Sprite as Actor");
		
		// Remove Actor
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should be removed from the cinematic"));

		allowableLines
				.add(new PlannerLineDef(
						"removeactor",
						"Remove Actor",
						"Removes the specified actor from the cinematic. This actor will no longer be able to be the target of actions.",
						definingValues));
		sceneMembership.add("Remove Actor");

		// Add Static Sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", false, "Start Location X",
				"The x coordinate (in pixels) to place the sprite at"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", false, "Start Location Y",
				"The y coordinate (in pixels) to place the sprite at"));


		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING,
				"spriteid",
				false,
				"Sprite Identifier",
				""));

		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_SPRITE_IMAGE,
						PlannerValueDef.TYPE_STRING,
						"spriteim",
						false,
						"Sprite image",
						""));

		allowableLines
				.add(new PlannerLineDef(
						"addstatic",
						"Add Static Sprite",
						"Adds a static sprite at the given location",
						definingValues));
		sceneMembership.add("Add Static Sprite");

		// Remove Static Sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"spriteid",
						false,
						"Sprite Identifier",
						""));

		allowableLines
				.add(new PlannerLineDef(
						"remstatic",
						"Remove Static Sprite",
						"Removes the static sprite with the given identifier",
						definingValues));
		sceneMembership.add("Remove Static Sprite");

		// Move
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the action"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", false, "X Coordinate",
				"The x coordinate (in pixels) that the actor should move to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", false, "Y Coordinate",
				"The y coordinate (in pixels) that the actor should move to"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"speed",
						false,
						"Move Speed",
						"The amount of pixels that the actor will move every 'getMoveUpdate' milliseconds defined in the CinematicActorConfiguration. With the default value of 20ms a normal movement speed is 2.4"));

		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"movehor",
				false,
				"Move Horizontal Before Vertical",
				"The sprite will move horizontal before it takes vertical moves if this is checked. Otherwise it will move vertical first"));

		// Diagonal movement
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"movediag",
				false,
				"Allow Diagonal Movement",
				"If checked then this sprite can move diagonally"));

		
		// Halting
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"halting",
				false,
				"Is Halting Movement",
				"If checked then this action is 'halting' which means no further actions will be issued until this action is complete"));
		
		// Pathfinding
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"pathfinding",
				false,
				"Enable pathfinding",
				"If checked orders the specified actor to move to the specified coordinate, the unit will find a path to the destination that "
				+ "respects 'unmovable' spaces. This is the prefered way to first move an 'associated' actor during battle or town "
				+ "where the original location is not known."));
		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "facing", true, "Facing",
				"If a value is selected then this actor will keep facing the same direction for the duration of the move."));
		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "freezeanim", true, "Freeze Animations",
				"If this value is selected then the current animation will be used for the duration of the movement. This overrides fixed facing."));
		
		allowableLines
				.add(new PlannerLineDef(
						"move",
						"Move",
						"Orders the specified actor to move to the specified coordinate.",
						definingValues));
		actorMove.add("Move");

		// Loop Move
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the looping move"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", false, "X Coordinate",
				"The x coordinate (in pixels) that the actor should move to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", false, "Y Coordinate",
				"The y coordinate (in pixels) that the actor should move to"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"speed",
						false,
						"Move Speed",
						"The amount of pixels that the actor will move every 'getMoveUpdate' milliseconds defined in the CinematicActorConfiguration. With the default value of 20ms a normal movement speed is 2.4"));

		allowableLines
				.add(new PlannerLineDef(
						"loopmove",
						"Move Actor in Loop",
						"Causes the specified actor to move to the specified location, once the actor gets to that location they will teleport back to where they started when this action was first called. This action will continue until a STOP LOOP MOVE action is called on this actor or another move command is issued for this actor",
						definingValues));
		actorMove.add("Move Actor in Loop");

		// Stop Loop Move
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should stop their looping move"));

		allowableLines
				.add(new PlannerLineDef(
						"stoploopmove",
						"Stop Actor Looping Move",
						"Causes the specified actor to stop looping their move, they will still walk to their target location but will not teleport",
						definingValues));
		actorMove.add("Stop Actor Looping Move");
	
		// Jump
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the action"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"jumpx",
						false,
						"Ending Location X",
						"The x location in pixels that the actor will end the jump at"));
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT,
				"height",
				false,
				"Ending Location Y",
				"The y location  in pixels that the actor will end the jump at"));
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT,
				"duration",
				false,
				"Duration",
				"The duration that the whole jump action should "));
		
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"landing",
				false,
				"Is Landing",
				"If checked this jump is the 'landing' portion, (speed increases over time). Otherwise speed decreases over time"));

		allowableLines
				.add(new PlannerLineDef(
						"jump",
						"Jump",
						"Causes the specified actor to jump in to the air, adding move or special effect actions while they are in the air may have undetermined effects. This action is NOT halting.",
						definingValues));
		actorControl.add("Jump");
		
		// Halting Anim
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the action"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"time",
						false,
						"Time",
						"The amount of time in milliseconds that this animation should be performed over. All frames will be shown for an equal time. General stand speed is 1000ms"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "anim", false,
				"Animation to Show",
				"The name of the animation that the actor should take. If the actor is 'hero backed' then an unpromoted/promoted prefix do not need to be included. If the actor is NOT 'hero backed' then the Unpromoted/Promoted prefix must be included."));

		allowableLines
				.add(new PlannerLineDef(
						"haltinganim",
						"Halting Animation",
						"Causes the specified actor to perform the specified animation, This action is 'halting' which means no further actions will be issued until this action is complete.",
						definingValues));
		actorControl.add("Halting Animation");

		// Anim
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the action"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"time",
						false,
						"Time",
						"The amount of time in milliseconds that this animation should be performed over. All frames will be shown for an equal time. General stand speed is 1000ms"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "anim", false,
				"Animation to Show",
				"The name of the animation that the actor should take. If the actor is 'hero backed' then an unpromoted/promoted prefix do not need to be included. If the actor is NOT 'hero backed' then the Unpromoted/Promoted prefix must be included."));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN, "loops", false,
						"Loop Animation",
						"Whether this animation should loop after it has finished playing."));

		allowableLines
				.add(new PlannerLineDef(
						"anim",
						"Animation",
						"Causes the specified actor to perform the specified animation.",
						definingValues));
		actorControl.add("Animation");

		// Stop Anim
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the action"));

		allowableLines.add(new PlannerLineDef("stopanim", "Stop Animation",
				"Causes the specified actor to stop its' current animation.",
				definingValues));
		actorControl.add("Stop Animation");

		// Spin
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should spin"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"speed",
						false,
						"Spin Speed",
						"The amount of time in ms that should pass in between changing facing direction"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"time",
						false,
						"Spin Duration",
						"The amount of time that this actor should spin for. A value of -1 indicates that this actor should spin for an indefinite amount of time. If an indefinite amount of time is specified then the actor will stop spinning when the a STOP SPIN action is issued"));

		allowableLines
				.add(new PlannerLineDef(
						"spin",
						"Spin Actor",
						"Causes the specified actor to begin spinning, this will cause other animations to stop for the duration of the spin. This can be used in conjunction with the grow and shrink special effect",
						definingValues));
		actorControl.add("Spin Actor");

		// Stop Spin
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should stop spinning"));

		allowableLines
				.add(new PlannerLineDef(
						"stopspin",
						"Stop Actor Spinning",
						"Stops the specified actor from spinning. If the actor is not spinning then no action will be taken",
						definingValues));
		actorControl.add("Stop Actor Spinning");

		// Facing
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should change their facing"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_DIRECTION, PlannerValueDef.TYPE_INT,
				"dir", false, "Facing Direction",
				"The direction that the actor should face"));

		allowableLines.add(new PlannerLineDef("facing", "Set Actor Facing",
				"Causes the specified actor to face the specified direction",
				definingValues));
		actorControl.add("Set Actor Facing");

		// Shrink
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the shrink special effect"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Shrink Time",
				"The amount of time that this actor should shrink over"));

		allowableLines
				.add(new PlannerLineDef(
						"shrink",
						"Shrink Actor",
						"Causes the specified actor to shrink over time, once the time is up the actor will immediately return to normal size, so if the intention is to remove the actor then it should be removed immediately before the end of this action. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Shrink Actor");

		// Grow
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the grow special effect"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Grow Time",
				"The amount of time that this actor should grow over"));

		allowableLines
				.add(new PlannerLineDef(
						"grow",
						"Grow Actor",
						"Causes the specified actor to grow over time from 1% height to 100% height, once the time is up the actor will immediately return to normal size. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations",
						definingValues));
		actorSpecialEffect.add("Grow Actor");

		// Quiver
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the tremble special effect"));

		allowableLines
				.add(new PlannerLineDef(
						"tremble",
						"Start Actor Trembling",
						"Causes the specified actor to begin trembling. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Start Actor Trembling");

		// Agitate
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the agitate special effect"));

		allowableLines
				.add(new PlannerLineDef(
						"quiver",
						"Start Actor Agitate",
						"Causes the specified actor to be 'Agitated'. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Start Actor Agitate");

		// Fall on Face
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the fall-on-face special effect"));
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "dir", false,
				"Head Direction",
				"The direction the sprites head should be facing"));

		allowableLines
				.add(new PlannerLineDef(
						"fallonface",
						"Actor Fall on Face",
						"Causes the specified actor to fall on their face. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Actor Fall on Face");

		// Lay on Side
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the lay-on-side special effect"));
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "dir", false,
				"Head Direction",
				"The direction the sprites head should be facing"));

		allowableLines
				.add(new PlannerLineDef(
						"layonsideright",
						"Actor Lay on Side Right",
						"Causes the specified actor to lay on their side. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Actor Lay on Side Right");

		// Lay on side left
		allowableLines
		.add(new PlannerLineDef(
				"layonsideleft",
				"Actor Lay on Side Left",
				"Causes the specified actor to lay on their side. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
				definingValues));
		actorSpecialEffect.add("Actor Lay on Side Left");

		// Lay on Back
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the lay-on-back special effect"));
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "dir", false,
				"Head Direction",
				"The direction the sprites head should be facing"));

		allowableLines
				.add(new PlannerLineDef(
						"layonback",
						"Actor Lay on Back",
						"Causes the specified actor to lay on their back. This effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		actorSpecialEffect.add("Actor Lay on Back");

		// Flash
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the flash special effect"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Flash Duration",
				"The amount of time in ms that this actor should flash for"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "speed", false, "Flash Speed",
				"The amount of time that a single flash should take"));

		allowableLines
				.add(new PlannerLineDef(
						"flash",
						"Actor Flash",
						"Causes the specified actor to flash white. If the duration is marked as indefinite then this effect will continue until a STOP SPECIAL EFFECT is issued for the actor. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		
		actorSpecialEffect.add("Actor Flash");

		// Nod
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should perform the nod effect"));

		allowableLines
				.add(new PlannerLineDef(
						"nod",
						"Actor Nod",
						"Causes the specified actor to nod. This effect lasts 1500ms. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		
		actorSpecialEffect.add("Actor Nod");

		// Shake head
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should perform the shake head effect"));
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false,
				"Shake Duration", "The amount of time in ms that this head shake should take to perform"));

		allowableLines
				.add(new PlannerLineDef(
						"shakehead",
						"Actor Shake Head",
						"Causes the specified actor to nod. This is a 'special effect'. Only one special effect can be active on a given actor at any time. This will stop any current animations.",
						definingValues));
		
		actorSpecialEffect.add("Actor Shake Head");

		// Stop Special Effect
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "name", false,
						"Actor Name",
						"The name of the actor that should stop performing special effects"));

		allowableLines
				.add(new PlannerLineDef(
						"stopse",
						"Stop Actor Special Effect",
						"Causes the specified actor to stop peforming any special effects that are currently active. This should be used to stop special effects of indefinite duration.",
						definingValues));
		actorSpecialEffect.add("Stop Actor Special Effect");

		// Visible
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor whose visibility should be changed"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN,
						"isvis",
						false,
						"Is Visible",
						"If true, the actor should become visible, otherwise they should become invisible"));

		allowableLines.add(new PlannerLineDef("visible", "Set Actor Visibility",
				"Sets the specified actors visiblity", definingValues));
		actorControl.add("Set Actor Visibility");

		// Move char to forefront
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should be displayed on top of all terrain layers"));

		allowableLines.add(new PlannerLineDef("rendertop", "Render on Top",
				"Causes the selected actor to be rendered on top of all of the terrain layers. This will continue until a 'Render on Normal' command is called for the actor.", definingValues));
		actorControl.add("Render on Top");
		
		// Remove char from forefront
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Actor Name",
				"The name of the actor that should stop their looping move"));

		allowableLines.add(new PlannerLineDef("rendernormal", "Render on Normal",
				"Causes the selected actor to be rendered in normal layer postion. This should be used to end the 'Render on Top' action.", definingValues));
		actorControl.add("Render on Normal");
		

		// Play Music
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MUSIC,
				PlannerValueDef.TYPE_STRING, "music", false, "Music Title",
				"The name of the music that should be played"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"volume",
						false,
						"Volume",
						"The percent volume that the music should be played at. This should be a value between 1-100"));

		allowableLines
				.add(new PlannerLineDef(
						"playmusic",
						"Play Music",
						"Loops music at the specified volume. Only one song can be playing at a time, starting music while one is already playing will end the other one.",
						definingValues));
		soundControl.add("Play Music");
		
		// Pause Music
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("pausemusic", "Pause Music",
				"Pauses the currently playing music", definingValues));
		soundControl.add("Pause Music");

		// Resume Music
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("resumemusic", "Resume Music",
				"Resumes any paused music", definingValues));
		soundControl.add("Resume Music");
		
		// Fade Music
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "duration", false,
						"Fade Duration",
						"The amount of time in ms that the music should fade out over"));
		allowableLines
				.add(new PlannerLineDef(
						"fademusic",
						"Fade Out Music",
						"Fades out any playing music to no volume over the specified period of time. The music stops playing after the fade",
						definingValues));
		soundControl.add("Fade Out Music");

		// Play Sound
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SOUND,
				PlannerValueDef.TYPE_STRING, "sound", false, "Sound Title",
				"The name of the sound that should be played"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"volume",
						false,
						"Volume",
						"The percent volume that the sound should be played at. This should be a value between 1-100"));

		allowableLines.add(new PlannerLineDef("playsound", "Play Sound",
				"Plays sound at the specified volume.", definingValues));
		soundControl.add("Play Sound");
		
		// Fade From Black
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Fade Time",
				"The amount of time that the screen should be faded over"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "halting", false, "Wait to Finish",
				"If true, this will be a halting action"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "init", false, "Intialize before cinematic",
				"If true, this action will be intialized before the cinematic starts. If you intend to fade in to the scene then you should check this"));

		allowableLines.add(new PlannerLineDef("fadein", "Fade in from black",
				"Fades the screen in from black.", definingValues));
		sceneControl.add("Fade in from black");

		// Fade To Black
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Fade Time",
				"The amount of time that the screen should be faded over"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "halting", false, "Wait to Finish",
				"If true, this will be a halting action"));

		allowableLines.add(new PlannerLineDef("fadeout", "Fade to black",
				"Fades the screen in to black.", definingValues));
		sceneControl.add("Fade to black");

		// Flash Screen
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Flash Time",
				"The amount of time that the screen should flash over"));

		allowableLines.add(new PlannerLineDef("flashscreen", "Flash Screen",
				"Flashes the screen white.", definingValues));
		sceneControl.add("Flash Screen");

		// Camera follow
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"name",
						false,
						"Actor Name",
						"The name of the actor that the camera should follow. This actor should already have been added to the cinematic"));

		allowableLines
				.add(new PlannerLineDef(
						"camerafollow",
						"Set Camera follows actor",
						"Causes the camera to always be centered on the current actor. You can cancel this function by calling another camera location command",
						definingValues));
		cameraEffect.add("Set Camera follows actor");

		// Camera Move
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", false, "X Coordinate",
				"The x coordinate (in pixels) that the camera should move to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", false, "Y Coordinate",
				"The y coordinate (in pixels) that the camera should move to"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "time", false, "Moving Time",
						"The amount of time in ms that the camera should be moved over"));

		allowableLines
				.add(new PlannerLineDef(
						"cameramove",
						"Camera Pan",
						"Pans the camera to the specified location over time. If the time is 0 then the camera immediate will move to the location",
						definingValues));
		cameraEffect.add("Camera Pan");
		
		// Camera Move To Actor
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "actor", false, "Actor Name",
				"The name of the actor that the camera should move to."));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "time", false, "Moving Time",
						"The amount of time in ms that the camera should be moved over"));

		allowableLines
				.add(new PlannerLineDef(
						"cameramovetoactor",
						"Camera Move To Actor",
						"Pans the camera to the specified actor over time. If the time is 0 then the camera immediate will move to the location. This is the prefered way to move the camera to an actor that has been associated with a sprite in town or battle",
						definingValues));
		cameraEffect.add("Camera Move to Actor");

		// Camera Shake
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "time", false, "Shake Time",
				"The amount of time that the camera should shake for"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "severity", false,
						"Severity",
						"The amount of pixels that the camera can be offset to during the shake"));

		allowableLines
				.add(new PlannerLineDef(
						"camerashake",
						"Shake Camera",
						"Shakes the camera to simulate an earthquake effect. After the camera is done shaking it will return to it's original location",
						definingValues));
		cameraEffect.add("Shake Camera");

		// Text Box
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_LONG_STRING, "text", false, "Text",
				"The text that should be displayed. Using the &lt;pause&gt; tag will cause a short pause, the &lt;softstop&gt; "
						+ "tag will do a soft stop (auto continue without user input after a time), the &lt;hardstop&gt; tag will "
						+ "do a hard stop which requires the player hit a button to continue, the &lt;linebreak&gt; tag "
						+ "will do a line break (as opposed to letting them happen naturally) and a &lt;nextcin&gt; tag "
						+ "will drive the next cinematic action if this message is being shown in a cinematic."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO, PlannerValueDef.TYPE_STRING,
				"heroportrait", true, "Hero Portrait",
				"The hero whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ENEMY, PlannerValueDef.TYPE_STRING,
				"enemyportrait", true, "Enemy Portrait",
				"The enemy whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"animportrait", true, "Portrait From Animation",
				"The animation that contains the portrait should be shown for this text."));

		allowableLines
				.add(new PlannerLineDef(
						"speech",
						"Show Speech Box",
						"Displays the specified text in a text box. This action is 'halting', which means subsequent actions will not be performed until the text box is dismissed via user input.",
						definingValues));
		textControl.add("Show Speech Box");
		
		// Load map
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapdata", false,
				"Map Data",
				"The name of the file containing the mapdata that should be loaded for this map"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"enter",
						false,
						"Map Entrance",
						"The entrance area definied in the map file that the hero should start in once the map loads. The area should be marked with a 'start' name and an 'exit' value"));

		allowableLines
				.add(new PlannerLineDef(
						"loadmap",
						"Load Map",
						"Loads the specified map and places the hero at the specified entrance.",
						definingValues));
		mapControl.add("Load Map");
		
		// Load Chapter
		definingValues = new ArrayList<PlannerValueDef>();		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING, "header", false,
				"Chapter Header Text",
				"The text that should appear at the top of the chapter page"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_LONG_STRING, "description", false,
				"Chapter Description Text",
				"The text that should appear at the bottom of the chapter page. This should NOT contain any special characters or stops."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER, PlannerValueDef.TYPE_INT, "exittrigger", false,
				"Exit Trigger",
				"The trigger that should be executed once the chapter screen has been shown. This should ALWAYS load a new map or the credits"));
		allowableLines.add(new PlannerLineDef("loadchapter", "Show Chapter",
				"Shows the chapter page.", definingValues));
		mapControl.add("Show Chapter");

		// Load Battle
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapdata", false,
				"Battle Map Data",
				"The name of the battle mapddata that should be loaded for this battle"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "entrance", false,
						"Entrance location",
						"The name of the map location that the force will be placed at when the battle loads"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "battbg", false,
				"Battle Background Index",
				"The index of the battle background that should be used for the battle"));
		allowableLines.add(new PlannerLineDef("loadbattle", "Start Battle",
				"Starts the battle with the given triggers and map",
				definingValues));
		mapControl.add("Start Battle");

		// Load Cinematic
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapdata", false,
				"Trigger File",
				"The name of the mapdata file that should be loaded for this cinematic"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"cinid", false, "Cinematic ID",
				"The ID of the cinematic that should be shown"));
		allowableLines.add(new PlannerLineDef("loadcin", "Load Cinematic",
				"Loads the specified map and text file with the same name and then runs the specified cinematic.", definingValues));
		mapControl.add("Load Cinematic");
		
		// Show End Game Credits
		definingValues = new ArrayList<PlannerValueDef>();		
		allowableLines.add(new PlannerLineDef("showcredits", "Show Credits",
				"Show the end game credits.", definingValues));
		mapControl.add("Show Credits");

		
		definingValues = new ArrayList<PlannerValueDef>();		
		allowableLines.add(new PlannerLineDef("showmainmenu", "Show Main Menu",
				"Show the game main menu.", definingValues));
		mapControl.add("Show Main Menu");
		
		definingValues = new ArrayList<PlannerValueDef>();		
		allowableLines.add(new PlannerLineDef("showintro", "Show Intro",
				"Show the intro scene.", definingValues));
		mapControl.add("Show Intro");
		
		// Exit Game
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("exit", "Exit Game",
				"Causes the game to exit",
				definingValues));
		mapControl.add("Exit Game");

		// Add hero
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "heroid", false, "Hero ID",
				"The ID of the hero that should be added to the force"));
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"init",
				false,
				"Initialize before Cinematic",
				"Indicates that this action should be taken before the scene is rendered. Blocking actions should NEVER be initialized before the scene"));
		allowableLines.add(new PlannerLineDef("addhero", "Add Hero",
				"Adds a new hero to the force", definingValues));
		progressControl.add("Add Hero");

		// Multi hero add selection
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_MULTI_STRING, "heroids", false, "Hero IDs",
				"The ID of the hero that should be added to the force"));
		allowableLines.add(new PlannerLineDef("addmultihero", "Add Hero from Selection",
				"Displays a menu that allows the user to select one of the specified heroes to the party", definingValues));
		progressControl.add("Add Hero from Selection");
		
		/*
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"init",
				false,
				"Initialize before Cinematic",
				"Indicates that this action should be taken before the scene is rendered. Blocking actions should NEVER be initialized before the scene"));
				*/

		cinematicContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_CINEMATIC - 1, menuLayout);
		containersByName.put("cinematic", cinematicContainer);
	}

	public static void setupItemDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef itemContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Name",
				"The name of the item"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_LONG_STRING, "description", false,
						"Description",
						"A description of the object that will be presented to the players during item evaluation"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "cost", false, "Cost",
				"The amount this item costs to purchase"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "isdeal", false, "Is Deal",
				"Indicates whether this item is a 'deal', if so it can always be purchased from a shop when sold"
				+ " or if an enemy drops it in battle"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "imageindexx", false, "X Index",
				"The x index of the items image"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "imageindexy", false, "Y Index",
				"The y index of the items image"));		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "droppable", false, "Is Quest Item",
				"Whether this item can be dropped or sold (used for 'quest' items)"));
		PlannerLineDef definingLine = new PlannerLineDef("item", "Item", "",
				definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Equippable
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "attack", false, "Attack Modifier",
				"The amount equipping this item will modify attack"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "defense", false, "Defense Modifier",
				"The amount equipping this item will modify defense"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "speed", false, "Speed Modifier",
				"The amount equipping this item will modify speed"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ITEM_TYPE, PlannerValueDef.TYPE_INT,
				"type", false, "Item Type",
				"Whether this item is a weapon or ring"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ITEM_STYLE, PlannerValueDef.TYPE_INT,
				"style", false, "Item Style",
				"What type of weapon this, use any value for rings"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM_RANGE,
						PlannerValueDef.TYPE_INT, "range", false, "Item Range",
						"The range this weapon can attack from, use any value for rings"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "weaponimage", false, "Weapon Attack Image",
					"The name of the weapon image that should be used for this weapon (should exist in the images/weapons folder). Use any value for rings. If an animation is specified then this value will be ignored."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_WEAPON_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "weaponanim", true, "Weapon Animation",
				"The animation file that should be used for animating the weapon. Selecting this value will override any weapon attack image."));
		
		//////////////////////////////// NEW STUFF
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "incmindam", false, "Minimum Damage Modifier",
				"The percent amount equipping this item will modify the minimum damage percent"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "inccrit", false, "Critical Chance Modifier",
				"The percent amount equipping this item will modify the critical chance percent"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "inccounter", false, "Counter Chance Modifier",
				"The percent amount equipping this item will modify the counter chance percent"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "incdouble", false, "Double Chance Modifier",
				"The percent amount equipping this item will modify the double chance percent"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "incevade", false, "Evade Chance Modifier",
				"The percent amount equipping this item will modify the evade chance percent"));
		// HP Regen
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "maxhpreg", false, "Max HP Regen",
				"The maximum amount of HP regen this item can grant per round. If you do not"
				+ " want a random range then this should equal the minimum amount"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "minhpreg", false, "Max HP Regen",
				"The minimum amount of HP regen this item can grant per round. If you do not"
				+ " want a random range then this should equal the maximum amount"));
		// MP Regen
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "maxmpreg", false, "Max MP Regen",
				"The maximum amount of MP regen this item can grant per round. If you do not"
				+ " want a random range then this should equal the minimum amount"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "minmpreg", false, "Min MP Regen",
				"The minimum amount of HP regen this item can grant per round. If you do not"
				+ " want a random range then this should equal the maximum amount"));

		// Battle Effect
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_EFFECT,
				PlannerValueDef.TYPE_STRING, "effect", true, "Attack Effect",
				"The effect type that can be applied on a successful hit with this weapon. "));

		// Battle Effect Level
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "efflvl", true, "Attack Effect Level",
				"The level of the battle effect that should be applied when it occurs"));

		// Battle Effect Chance
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "effchc", false, "Attack Effect Chance",
				"The chance that the associated battle effect will be applied on attack."));

		// Damage Affinity
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_WEAPON_DAMAGE_TYPE,
				PlannerValueDef.TYPE_STRING, "dmgaff", false, "Damage Affinity",
				"The affinity that will be used to determine damage for this weapon. A value other"
				+ " then normal will cause the associated affinity to be applied to the damage"));

		// Fire Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"fireAffin", false, "Fire Affinitiy",
				"The amount to modify the equippers fire affinity."));

		// Elec Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"elecAffin", false, "Electricity Affinitiy",
				"The amount to modify the equippers elec affinity."));

		// Cold Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"coldAffin", false, "Cold Affinitiy",
				"The amount to modify the equippers cold affinity."));

		// Dark Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"darkAffin", false, "Dark Affinitiy",
				"The amount to modify the equippers dark affinity."));

		// Water Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"waterAffin", false, "Water Affinitiy",
				"The amount to modify the equippers water affinity."));

		// Earth Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"earthAffin", false, "Earth Affinitiy",
				"The amount to modify the equippers earth affinity."));

		// Wind Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"windAffin", false, "Wind Affinitiy",
				"The amount to modify the equippers wind affinity."));

		// Light Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"lightAffin", false, "Light Affinitiy",
				"The amount to modify the equippers light affinity."));

		// OHKO chance
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"ohko", false, "OHKO Chance",
				"The percent chance of a OHKO occurring on an attack."));

		// OHKO on crit chance
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"ohkooc", false, "OHKO Chance on Critical",
				"The percent chance of a OHKO occurring on a critical attack."));
		// Usable only by promoted
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "promoteonly", false,
				"Promotable Only",
				"If true, this item will only be equippable by promoted heroes"));

		allowableLines.add(new PlannerLineDef("equippable", "Equippable Item",
				"Marks this item as equippable and defines stats for it",
				definingValues, new PlannerEquippableItemLayout()));
		
		
		// Use Custom
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN, "targetsenemy", false,
						"Targets Enemy",
						"Whether this item can be used on enemies, otherwise it is used on allies"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "damage", false,
						"Damage Dealt",
						"The amount of damage this item will deal on use (positive values will heal)"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"mpdamage",
						false,
						"MP Damage Dealt",
						"The amount of damage this item will deal to the targets MP on use (positive values will heal)"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM_RANGE,
						PlannerValueDef.TYPE_INT, "range", false, "Item Range",
						"The range this can be used from"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ITEM_AREA, PlannerValueDef.TYPE_INT,
				"area", false, "Item Area of Effect",
				"The area that this item can effect"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING,
						"text",
						false,
						"Item Use Text",
						"The text that will be appended after the targets name in the attack cinematic. An example value would be 'is healed for <value>'. "
								+ "This would cause the battle text to become 'Noah is healed for #'. A value of '<value>' will be replaced with the actual damage healed/done."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "singleuse", false,
				"Single Use Item",
				"If true, the item will be removed after it has been used. "));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "damageitem", false,
				"Damages Item",
				"If true, the item has a chance of being damaged on use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "damageitem", false,
				"Damages Item",
				"If true, the item has a chance of being damaged on use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "useoutsidebattle", false,
				"Usable Outside of Battle",
				"If true, this item is usable outside of battle. It should have a single target area"));
		
		allowableLines.add(new PlannerLineDef("use", "Custom Usuable Item",
				"Marks this item as usuable and defines its' use",
				definingValues));

		// Use Spell
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_SPELL,
						PlannerValueDef.TYPE_STRING, "spellid", false,
						"Spell Cast",
						"The spell that will be cast when this item is used"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "level", false,
						"Spell Level",
						"The level of the spell that will be cast upon use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "singleuse", false,
				"Single Use Item",
				"If true, the item will be removed after it has been used. "));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "damageitem", false,
				"Damages Item",
				"If true, the item has a chance of being damaged on use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "useoutsidebattle", false,
				"Usable Outside of Battle",
				"If true, this item is usable outside of battle. It should have a single target area"));

		allowableLines.add(new PlannerLineDef("usespell", "Usuable Item Spell",
				"Marks this item as usuable and defines the spell it casts on use",
				definingValues));

		itemContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_ITEM - 1);
		containersByName.put("item", itemContainer);
	}

	public static void setupQuestDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef textContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "description", false,
				"Description", "Description"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_LONG_STRING, "extradescription", false,
				"Extra Description", "Use this area to describe what this quest is supposed to control and who is responsible for toggling it. This is not used in the engine."));
		/*
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "count", false,
				"Counting Quest Success", "If set to a number greater then 0 then this quest numeric value must meet or exceed this number to be completed."));
				*/
		/*
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_MULTI_STRING, "count", false,
				"Sub Quests", "A list of sub quests that are associated with this quest. "
						+ "These can be referenced individually, or all must be completed for the 'parent' quest to be considered complete."));
		*/
		
		// definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
		// PlannerValueDef.TYPE_INT, "triggerid", false,
		// "Unique Trigger Id",
		// "Unique id that can be used to identify a given trigger"));
		PlannerLineDef definingLine = new PlannerLineDef("quest", "Quest", "",
				definingValues);
		
		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();
		
		// Complete Quest
		/*
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_MULTI_STRING, "subquestnames", false, "Subquest Name",
				"A list of the names of subquests of this quest"));
		allowableLines.add(new PlannerLineDef("completequest",
				"Subquests", "Defines subquests for the parent quest",
				definingValues));
		*/

		textContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_QUEST - 1);
		
		
		containersByName.put("quest", textContainer);
	}

	public static void setupTextDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef textContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "description", false,
				"Description", "Description"));
		// definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
		// PlannerValueDef.TYPE_INT, "triggerid", false,
		// "Unique Trigger Id",
		// "Unique id that can be used to identify a given trigger"));
		PlannerLineDef definingLine = new PlannerLineDef("text", "Text", "",
				definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Text
		definingValues = new ArrayList<PlannerValueDef>();
		
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "require", true,
						"Required Quest",
						"The ID of the quest that must be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "exclude", true,
						"Exclude Quest",
						"The ID of the quest that CAN NOT be complete for this to be shown"));
		//FIX THIS
		
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
						PlannerValueDef.TYPE_MULTI_INT, "trigger", true,
						"Trigger ID",
						"The IDs of the triggers that should be run after this message is complete."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO, PlannerValueDef.TYPE_STRING,
				"heroportrait", true, "Hero Portrait",
				"The hero whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ENEMY, PlannerValueDef.TYPE_STRING,
				"enemyportrait", true, "Enemy Portrait",
				"The enemy whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"animportrait", true, "Portrait From Animation",
				"The animation that contains the portrait should be shown for this text."));
		
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_LONG_STRING,
				"message",
				false,
				"Message Text",
				"The text that should be displayed. Using the &lt;pause&gt; tag will cause a short pause, the &lt;softstop&gt; "
						+ "tag will do a soft stop (auto continue without user input after a time), the &lt;hardstop&gt; tag will "
						+ "do a hard stop which requires the player hit a button to continue, the &lt;linebreak&gt; tag "
						+ "will do a line break (as opposed to letting them happen naturally) and a &lt;nextcin&gt; tag "
						+ "will drive the next cinematic action if this message is being shown in a cinematic."));
		allowableLines.add(new PlannerLineDef("string", "Message Text",
				"A message that should be displayed", definingValues));
		
		// Yes or No
		definingValues = new ArrayList<PlannerValueDef>();
		
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "require", true,
						"Required Quest",
						"The ID of the quest that must be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "exclude", true,
						"Exclude Quest",
						"The ID of the quest that CAN NOT be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
						PlannerValueDef.TYPE_MULTI_INT, "triggeryes", true,
						"Yes Trigger ID",
						"The ID of the trigger that should be run if a 'yes' is selected."));
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_MULTI_INT, "triggerno", true,
				"No Trigger ID",
				"The ID of the trigger that should be run if a 'no' is selected."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO, PlannerValueDef.TYPE_STRING,
				"heroportrait", true, "Hero Portrait",
				"The hero whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ENEMY, PlannerValueDef.TYPE_STRING,
				"enemyportrait", true, "Enemy Portrait",
				"The enemy whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"animportrait", true, "Portrait From Animation",
				"The animation that contains the portrait should be shown for this text."));
		
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_LONG_STRING,
				"message",
				false,
				"Message Text",
				"The text that should be displayed. Using the &lt;pause&gt; tag will cause a short pause, the &lt;softstop&gt; "
						+ "tag will do a soft stop (auto continue without user input after a time), the &lt;hardstop&gt; tag will "
						+ "do a hard stop which requires the player hit a button to continue, the &lt;linebreak&gt; tag "
						+ "will do a line break (as opposed to letting them happen naturally) and a &lt;nextcin&gt; tag "
						+ "will drive the next cinematic action if this message is being shown in a cinematic."));
		allowableLines.add(new PlannerLineDef("yesno", "Yes or No Text",
				"A question that should be displayed", definingValues));
		
		// Conversation
		definingValues = new ArrayList<PlannerValueDef>();
		
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "require", true,
						"Required Quest",
						"The ID of the quest that must be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "exclude", true,
						"Exclude Quest",
						"The ID of the quest that CAN NOT be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
						PlannerValueDef.TYPE_MULTI_INT, "trigger", true,
						"Trigger ID",
						"The ID of the trigger that should be run after this message is complete."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO, PlannerValueDef.TYPE_STRING,
				"heroportrait1", true, "Hero Portrait",
				"First Speaker: The hero whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ENEMY, PlannerValueDef.TYPE_STRING,
				"enemyportrait1", true, "Enemy Portrait",
				"First Speaker: The enemy whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"animportrait1", true, "Portrait From Animation",
				"First Speaker: The animation that contains the portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_HERO, PlannerValueDef.TYPE_STRING,
				"heroportrait2", true, "Hero Portrait",
				"Second Speaker: The hero whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ENEMY, PlannerValueDef.TYPE_STRING,
				"enemyportrait2", true, "Enemy Portrait",
				"Second Speaker: The enemy whose portrait should be shown for this text."));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ANIMATIONS, PlannerValueDef.TYPE_STRING,
				"animportrait2", true, "Portrait From Animation",
				"Second Speaker: The animation that contains the portrait should be shown for this text."));
		
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_MULTI_LONG_STRING,
				"message",
				false,
				"Alternating Dialog",
				"Displays a conversation between two characters. Text in odd numbered boxes will be said by the first speaker "
				+ "and the text in even numbered boxes will be said by the second speaker. Using the &lt;pause&gt; "
				+ "tag will cause a short pause, the &lt;softstop&gt; "
						+ "tag will do a soft stop (auto continue without user input after a time), the &lt;hardstop&gt; tag will "
						+ "do a hard stop which requires the player hit a button to continue, the &lt;linebreak&gt; tag "
						+ "will do a line break (as opposed to letting them happen naturally) and a &lt;nextcin&gt; tag "
						+ "will drive the next cinematic action if this message is being shown in a cinematic."));
		allowableLines.add(new PlannerLineDef("conversation", "Conversation",
				"A message that should be displayed", definingValues));

		textContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_TEXT - 1);
		containersByName.put("text", textContainer);
	}

	public static void setupEnemyDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef enemyContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Name",
				"The name of the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "hp", false, "HP",
				"Starting HP for the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "mp", false, "MP",
				"Starting MP for the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "attack", false, "Attack",
				"Starting Attack for the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "defense", false, "Defense",
				"Starting Defense for the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "speed", false, "Speed",
				"Starting Speed for the enemy"));
		// Fire Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"fireAffin", false, "Fire Affinitiy",
				"The enemies base fire affinity. Items can modify this value."));

		// Elec Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"elecAffin", false, "Electricity Affinitiy",
				"The enemies base electricity affinity. Items can modify this value."));

		// Cold Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"coldAffin", false, "Cold Affinitiy",
				"The enemies base cold affinity. Items can modify this value."));

		// Dark Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"darkAffin", false, "Dark Affinitiy",
				"The enemies base dark affinity. Items can modify this value."));

		// Water Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"waterAffin", false, "Water Affinitiy",
				"The enemies base water affinity. Items can modify this value."));

		// Earth Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"earthAffin", false, "Earth Affinitiy",
				"The enemies base earth affinity. Items can modify this value."));

		// Wind Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"windAffin", false, "Wind Affinitiy",
				"The enemies base wind affinity. Items can modify this value."));

		// Light Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"lightAffin", false, "Light Affinitiy",
				"The enemies base light affinity. Items can modify this value."));

		// Body Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"bodyStrength", false, "Body Strength",
				"Determines the base body value for the enemy."));

		// Mind Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"mindStrength", false, "Mind Strength",
				"Determines the base mind value for the enemy."));

		// Counter Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"counterStrength", false, "Counter Chance Strength",
				"Determines the base counter value for the enemy."));

		// Evade Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"evadeStrength", false, "Evade Chance Strength",
				"Determines the base evade value for the enemy."));

		// Double Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"doubleStrength", false, "Double Chance Strength",
				"Determines the base body value for the enemy."));

		// Crit Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"critStrength", false, "Critical Strength",
				"Determines the base critical value for the enemy."));

		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "level", false, "Level",
				"Starting Level for the enemy"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "move", false, "Move",
				"Starting Move for the enemy"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_MOVE_TYPE,
						PlannerValueDef.TYPE_STRING, "movementtype", false,
						"Movement Type",
						"The enemies movement type as it relates to land effect and barriers"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
						PlannerValueDef.TYPE_STRING, "animations", false,
						"Animation File",
						"The name of the animation file that should be used for this enemy"));
		
		definingValues
		.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "gold", false,
				"Gold Dropped",
				"The amount of gold that is dropped on death."));
		// Palette Swap
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_PALETTE,
				PlannerValueDef.TYPE_STRING, "palette", true, "Palette",
				"(CURRENTLY UNUSED) The palette that should be used to modify the selected animation colors"));
		// definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
		// PlannerValueDef.TYPE_INT, "triggerid", false,
		// "Unique Trigger Id",
		// "Unique id that can be used to identify a given trigger"));
		PlannerLineDef definingLine = new PlannerLineDef("enemy", "Enemy", "",
				definingValues, new PlannerEnemyStatLayout());

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Spell Progression
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPELL,
				PlannerValueDef.TYPE_STRING, "spellid", false, "Spell ID",
				"The ID of the spell that this enemy knows"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "level", false, "Max Level",
				"The max level known of the specified spell"));
		allowableLines.add(new PlannerLineDef("spell", "Spell",
				"A spell that this enemy knows", definingValues));

		// Items Equipped
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_STRING, "itemid", false, "Item ID",
				"The ID of the item that this enemy should start with"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "equipped", false,
				"Item Equipped", "If true, the item will start as equipped."));
		allowableLines.add(new PlannerLineDef("item", "Starting Item",
				"An item that this hero should start with", definingValues));

		// Attack Special Effect
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_EFFECT,
				PlannerValueDef.TYPE_STRING, "effectid", false, "Effect ID",
				"The ID of the effect that the enemies attack may cause. A value of CUSTOM"
				+ " means that this weapons effect will be passed to the BattleFunctions script to be performed."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "effectchance", false,
				"Effect Chance", "The percent chance that the effect will occur"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "effectlevel", false,
				"Effect Level", "The level of the effect that should be applied (1-4)"));
		allowableLines.add(new PlannerLineDef("attackeffect", "Attack Effect",
				"An effect that may occur on the enemy attack", definingValues));

		// Special Attack
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPELL,
				PlannerValueDef.TYPE_STRING, "spellid", false, "Spell ID",
				"The ID of the spell that this enemy will use for it's special attack."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "specialchance", false,
				"Special Chance", "The percent chance that this special attack can be used"));
		/*definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "animation", false, "Animation Name",
				"The name of the animation that should be used for this special attack as defined in the animation file."));
				*/
		allowableLines.add(new PlannerLineDef("specialattack", "Special Attack",
				"A special attack ability", definingValues));

		enemyContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_ENEMY - 1);
		containersByName.put("enemy", enemyContainer);
	}

	public static void setupHeroDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef heroContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Name",
				"The name of the hero"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "promoted", false, "Promoted",
				"If true, this hero is promoted when they initially join the party"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "level", false, "Level",
				"Starting Level for the hero"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
						PlannerValueDef.TYPE_STRING, "animations", false,
						"Animation File",
						"The name of the animation file that should be used for this hero"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "leader", true, "Is Leader",
				"Whether this hero is the leader of the force"));

		// definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
		// PlannerValueDef.TYPE_INT, "portrait", false,
		// "Portrait Index",
		// "Unique id that can be used to identify a given trigger"));
		PlannerLineDef definingLine = new PlannerLineDef("hero", "Hero", "",
				definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Progression
		definingValues = new ArrayList<PlannerValueDef>();
		
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_LONG_STRING,
				"evaluation",
				false,
				"Progression Evaluation",
				"The text that will be displayed when a hero with this progression is evaluated in the advisor"));
		
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN,
						"promoted",
						false,
						"Promoted Progression",
						"Whether this progression represents this heroes promoted or unpromoted progression"));
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN,
				"specialpromoted",
				false,
				"Special Promotion Progression",
				"If checked, this progression will be used for the SPECIAL promotion path (as opposed to default). Selecting this supercedes the 'promoted' checkbox"));
		
		// Special promotion required item
		definingValues
		.add(new PlannerValueDef(
				ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_STRING,
				"specialpromoteitem",
				true,
				"Special Promotion Required Item",
				"If special promotion is checked, this is the item that must be possesed by the group to allow the this special promotion to occur. If this item "
				+ "is not owned then this progression path will be unavailable. If special promotion is checked and this value is not specified then this"
				+ " promotion path will be impossible to take."));
		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "move", false, "Starting Move",
				"The heroes base move while in this progression"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_MOVE_TYPE, PlannerValueDef.TYPE_STRING,
				"movementtype", false, "Movement Type",
				"The movement type of this hero"));

		// ATTACK
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_STAT_GAINS, PlannerValueDef.TYPE_STRING,
				"attack", false, "Attack Gain",
				"The amount of attack the hero should gain per level"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"attackstart", false, "Attack Start",
				"The amount of attack the hero should start with"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"attackend", false, "Attack End",
				"The minimum amount of attack the hero should end with"));

		// DEFENSE
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_STAT_GAINS, PlannerValueDef.TYPE_STRING,
				"defense", false, "Defense Gain",
				"The amount of defense the hero should gain per level"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"defensestart", false, "Defense Start",
				"The amount of defense the hero should start with"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"defenseend", false, "Defense End",
				"The minimum amount of defense the hero should end with"));

		// SPEED
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_STAT_GAINS, PlannerValueDef.TYPE_STRING,
				"speed", false, "Speed Gain",
				"The amount of speed the hero should gain per level"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"speedstart", false, "Speed Start",
				"The amount of speed the hero should start with"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"speedend", false, "Speed End",
				"The minimum amount of speed the hero should end with"));

		// HP
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_STAT_GAINS, PlannerValueDef.TYPE_STRING,
				"hp", false, "HP Gain",
				"The amount of HP the hero should gain per level"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"hpstart", false, "HP Start",
				"The amount of HP the hero should start with"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"hpend", false, "HP End",
				"The minimum amount of HP the hero should end with"));

		// MP
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_STAT_GAINS, PlannerValueDef.TYPE_STRING,
				"mp", false, "MP Gain",
				"The amount of MP the hero should gain per level"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"mpstart", false, "MP Start",
				"The amount of MP the hero should start with"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"mpend", false, "MP End",
				"The minimum amount of MP the hero should end with"));

		// Fire Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"fireAffin", false, "Fire Affinitiy",
				"The heroes base fire affinity. Items can modify this value."));

		// Elec Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"elecAffin", false, "Electricity Affinitiy",
				"The heroes base electricity affinity. Items can modify this value."));

		// Cold Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"coldAffin", false, "Cold Affinitiy",
				"The heroes base cold affinity. Items can modify this value."));

		// Dark Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"darkAffin", false, "Dark Affinitiy",
				"The heroes base dark affinity. Items can modify this value."));

		// Water Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"waterAffin", false, "Water Affinitiy",
				"The heroes base water affinity. Items can modify this value."));

		// Earth Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"earthAffin", false, "Earth Affinitiy",
				"The heroes base earth affinity. Items can modify this value."));

		// Wind Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"windAffin", false, "Wind Affinitiy",
				"The heroes base wind affinity. Items can modify this value."));

		// Light Affin
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_UNBOUNDED_INT,
				"lightAffin", false, "Light Affinitiy",
				"The heroes base light affinity. Items can modify this value."));

		// Body Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"bodyStrength", false, "Body Strength",
				"Determines the base body value for the hero."));

		// Body Progression
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_BODYMIND_GAIN, PlannerValueDef.TYPE_STRING,
				"bodyProgress", false, "Body Progression Type",
				"The name of the progression type that dicates how this heroes body stat will increase."));

		// Mind Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"mindStrength", false, "Mind Strength",
				"Determines the base mind value for the hero."));

		// Mind Progression
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_BODYMIND_GAIN, PlannerValueDef.TYPE_STRING,
				"mindProgress", false, "Mind Progression Type",
				"The name of the progression type that dicates how this heroes mind stat will increase."));

		// Counter Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"counterStrength", false, "Counter Chance Strength",
				"Determines the base counter chance value for the hero."));

		// Evade Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"evadeStrength", false, "Evade Chance Strength",
				"Determines the base evade chance value for the hero."));

		// Double Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"doubleStrength", false, "Double Chance Strength",
				"Determines the base double attack chance value for the hero."));

		// Crit Strength
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ATTRIBUTE_STRENGTH, PlannerValueDef.TYPE_STRING,
				"critStrength", false, "Critical Strength",
				"Determines the base critical attack chance value for the hero."));

		// Usuable Items
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_ITEM_STYLE,
				PlannerValueDef.TYPE_MULTI_INT, "usuableitems", false,
				"Usuable Items", "The type of weapons that this hero can use"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "class", false, "Class Name",
				"The name of this characters class."));
		allowableLines.add(new PlannerLineDef("progression",
				"Hero Progression", "This heroes statistic progression",
				definingValues, new PlannerHeroStatLayout()));

		// Spell Progression
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPELL,
				PlannerValueDef.TYPE_STRING, "spellid", false, "Spell ID",
				"The ID of the spell that this hero knows"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "promotedprog", false,
				"Promoted Progression", "If true, these spells will be gained at the indicated levels ONLY for promoted heroes using the DEFAULT progression path."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "specialpromoted", false,
				"Special Promotion Progression", "If true, these spells will be gained at the indicated levels ONLY for promoted heroes using the SPECIAL progression path. Selecting this supercedes the 'Promoted Progression' option"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "gained", false,
						"Levels Gained",
						"A comma seperated list of the levels that the spell levels will be gained at."));
		allowableLines.add(new PlannerLineDef("spellprogression",
				"Spell Progression", "This heroes spell progression",
				definingValues));

		// Items Equipped
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_STRING, "itemid", false, "Item ID",
				"The ID of the item that this hero should start with"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "equipped", false,
				"Item Equipped", "If true, the item will start as equipped."));
		allowableLines.add(new PlannerLineDef("item", "Starting Item",
				"An item that this hero should start with", definingValues));

		heroContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_HERO - 1);
		containersByName.put("hero", heroContainer);
	}

	public static void setupTriggerDefinition(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef triggerContainer;

		ArrayList<String> partyControl = new ArrayList<>();
		ArrayList<String> npcControl = new ArrayList<>();
		ArrayList<String> battleControl = new ArrayList<>();
		ArrayList<String> soundControl = new ArrayList<>();
		ArrayList<String> textControl = new ArrayList<>();
		ArrayList<String> mapControl = new ArrayList<>();
		ArrayList<String> progressControl = new ArrayList<>();
		
		Hashtable<String, ArrayList<String>> menuLayout = new Hashtable<>();
		menuLayout.put("Party Control", partyControl);
		menuLayout.put("Sprites", npcControl);
		menuLayout.put("Speech", textControl);
		menuLayout.put("Battle", battleControl);
		menuLayout.put("Sound", soundControl);
		menuLayout.put("Progress", progressControl);
		menuLayout.put("Map", mapControl);
		
		
		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "description", false,
				"Description", "Description"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "require", true,
						"Required Quest",
						"The ID of the quest that must be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
						PlannerValueDef.TYPE_MULTI_STRING, "exclude", true,
						"Exclude Quest",
						"The ID of the quest that CAN NOT be complete for this to be shown"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN, "nonretrig", false,
						"Non Retriggerable",
						"If true, indicates that this trigger can only be executed once per game"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_BOOLEAN,
						"retrigonenter",
						false,
						"Retrigger Each Enter",
						"If true, indicates that each time the map has been entered that this trigger should be reactivated"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "triggeronce", false,
				"Trigger Once Per Map",
				"If true, indicates that this trigger can only be executed once per map"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "triggerimmediately", false,
				"Trigger Immediately",
				"If true, indicates that this trigger should be executed as soon as a unit begins moving onto this space. "
				+ "If unchecked, the tirgger will only take effect once the unit has stopped moving. Checking this will have "
				+ "no effect during battle"));
		PlannerLineDef definingLine = new PlannerLineDef("trigger", "Trigger",
				"", definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();
		
		// Complete Quest
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
				PlannerValueDef.TYPE_STRING, "questid", false, "Quest ID",
				"The ID of the equest that should be marked as complete"));
		allowableLines.add(new PlannerLineDef("completequest",
				"Complete Quest", "Marks a given quest as completed",
				definingValues));
		progressControl.add("Complete Quest");
		
		// Uncomplete Quest
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
				PlannerValueDef.TYPE_STRING, "questid", false, "Quest ID",
				"The ID of the equest that should be marked as NOT complete"));
		allowableLines.add(new PlannerLineDef("uncompletequest",
				"Uncomplete Quest", "Marks a given quest as NOT completed. This has no effect if the quest was not already completed.",
				definingValues));
		progressControl.add("Uncomplete Quest");
		
		// Start Battle
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
						PlannerValueDef.TYPE_STRING, "battletriggers", false,
						"Battle Trigger File",
						"The name of the mapdata file that should be loaded for this battle"));
		definingValues
				.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_STRING, "entrance", false,
						"Entrance location",
						"The name of the map location that the force will be placed at when the map loads"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT, "battbg", false,
						"Battle Background Index",
						"The index of the battle background that should be used for the battle"));
		allowableLines.add(new PlannerLineDef("startbattle", "Load Battle",
				"Starts the battle with the given triggers and map",
				definingValues));
		mapControl.add("Load Battle");

		// Load map
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapdata", false,
				"Trigger File",
				"The name of the mapdata file that should be loaded for this map"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING, "enter", false,
				"Entrance location",
				"The name of the map location that the hero will be placed at when the map loads"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION, PlannerValueDef.TYPE_STRING, "transdir", true,
				"Map Transition Direction",
				"If specified: the map will be transitioned out via a 'slide' effect in the direction indicated. This value should be set to the direction that the NEW map is relative to the current map."));
		allowableLines.add(new PlannerLineDef("loadmap", "Load Map",
				"Loads the given map and places the hero at the given location", definingValues));
		mapControl.add("Load Map");
		
		// Load Chapter
		definingValues = new ArrayList<PlannerValueDef>();		
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING, "header", false,
				"Chapter Header Text",
				"The text that should appear at the top of the chapter page"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_LONG_STRING, "description", false,
				"Chapter Description Text",
				"The text that should appear at the bottom of the chapter page. This should NOT contain any special characters or stops."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER, PlannerValueDef.TYPE_INT, "exittrigger", false,
				"Exit Trigger",
				"The trigger that should be executed once the chapter screen has been shown. This should ALWAYS load a new map or the credits"));
		allowableLines.add(new PlannerLineDef("loadchapter", "Show Chapter",
				"Shows the chapter page.", definingValues));
		mapControl.add("Show Chapter");
		
		// Load Egress
		definingValues = new ArrayList<PlannerValueDef>();		
		allowableLines.add(new PlannerLineDef("loadegress", "Go to Egress Location",
				"Transitions to the current egress location and revives dead leaders.", definingValues));
		mapControl.add("Go to Egress Location");

		// Show Cinematic
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapdata", false,
				"Trigger File",
				"The name of the mapdata file that should be loaded for this map"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_INT,
				"cinid", false, "Cinematic ID",
				"The ID of the cinematic that should be shown"));
		allowableLines.add(new PlannerLineDef("loadcin", "Load Cinematic",
				"Loads the specified map and text file with the same name and then runs the specified cinematic.", definingValues));
		mapControl.add("Load Cinematic");

		// Show priest
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "portrait", false, "Portrait Animation File",
				"The animation file containing the priest portrait"));
		allowableLines.add(new PlannerLineDef("showpriest", "Show Priest",
				"Displays the priests menu", definingValues));
		npcControl.add("Show Priest");
		
		// Show advisor
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("showadvisor", "Show Advisor",
				"Displays the advisor menu. The advisor portrait is defined in the ConfigurationValues script as getAdvisorPortraitAnimFile.", definingValues));
		npcControl.add("Show Advisor");

		// Play Music
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MUSIC,
				PlannerValueDef.TYPE_STRING, "music", false, "Music File",
				"The name of the music that should be played"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "volume", false, "Volume",
				"A number between 0-100 indicating the volume that the music will be played at"));
		allowableLines.add(new PlannerLineDef("playmusic", "Play Music",
				"Plays the specified music", definingValues));
		soundControl.add("Play Music");
		
		// Pause Music
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("pausemusic", "Pause Music",
				"Pause any playing music", definingValues));
		soundControl.add("Pause Music");
		
		// Resume Music
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("resumemusic", "Resume Music",
				"Resume paused music", definingValues));
		soundControl.add("Resume Music");

		// Play Sound
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SOUND,
				PlannerValueDef.TYPE_STRING, "sound", false, "Sound File",
				"The name of the sound that should be played"));
		definingValues
				.add(new PlannerValueDef(
						ReferenceStore.REFERS_NONE,
						PlannerValueDef.TYPE_INT,
						"volume",
						false,
						"Volume",
						"A number between 1-100 that represents the percent volume the sound should be played at"));
		allowableLines.add(new PlannerLineDef("playsound", "Play Sound",
				"Plays the specified sound effect", definingValues));
		soundControl.add("Play Sound");
		
		// Change AI - Vision
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false, "Unit",
				"The unit whose AI vision should be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "vision", false, "AI Vision",
				"The maximum range that the specified combatant will look for targets. "
				+ "This value can never be lower then the attack range of the combatant. A value of 0 = unlimited"));
		allowableLines.add(new PlannerLineDef("aivision", "Change AI - Vision",
				"Changes the range that the combatant will look for targets at", definingValues));
		battleControl.add("Change AI - Vision");
		
		// Change AI - Approach Type
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false, "Unit",
				"The unit whose AI should be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_AI_APPROACH,
				PlannerValueDef.TYPE_INT, "aitype", false, "AI Type",
				"The type of AI that the specified enemy should employ"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "priority", false, "AI Priority",
				"The priority that this AI change will have, if this change is "
				+ "lower then the current priority of the AI, then the AI will not be changed."));
		allowableLines.add(new PlannerLineDef("changeaiapproach", "Change AI - Approach Type",
				"Changes the specified enemies approach AI", definingValues));
		battleControl.add("Change AI - Approach Type");
		
		// Change AI - Target Hero
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false, "Unit",
				"The unit whose AI should be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_INT, "heroid", true, "Hero",
				"The index of the hero that should be targeted"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "priority", false, "AI Priority",
				"The priority that this AI change will have, if this change is "
				+ "lower then the current priority of the AI, then the AI will not be changed."));
		allowableLines.add(new PlannerLineDef("changeaitargethero", "Change AI - Target Hero",
				"Changes the specified enemies AI to target a given hero. The target will move as quickly as possible.", definingValues));
		battleControl.add("Change AI - Target Hero");
		
		// Change AI - Follow Enemy
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false, "Unit",
				"The unit whose AI should be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "targetid", true, "Target Unit",
				"The target unit (enemy) that this enemy should follow"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "priority", false, "AI Priority",
				"The priority that this AI change will have, if this change is "
				+ "lower then the current priority of the AI, then the AI will not be changed."));
		allowableLines.add(new PlannerLineDef("changeaitargetenemy", "Change AI - Follow Enemy",
				"Changes the specified enemies AI to follow a given enemy. The target will move as quickly as possible.", definingValues));
		battleControl.add("Change AI - Follow Enemy");
		

		// Change AI - Move to location
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false, "Unit",
				"The unit whose AI should be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "x", true, "X Location",
				"The x coordinate in tiles that this enemy should move to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "y", true, "Y Location",
				"The y coordinate in tiles that this enemy should move to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "priority", false, "AI Priority",
				"The priority that this AI change will have, if this change is "
				+ "lower then the current priority of the AI, then the AI will not be changed."));
		allowableLines.add(new PlannerLineDef("changeaimove", "Change AI - Move to Location",
				"Changes the specified enemies AI to move to the target location. The target will move as quickly as possible.", definingValues));
		battleControl.add("Change AI - Move to Location");

		// Show Text
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TEXT,
				PlannerValueDef.TYPE_INT, "textid", false, "Text ID",
				"The ID of the text that should be displayed"));
		allowableLines.add(new PlannerLineDef("showtext", "Show Text",
				"Shows the text with the specified ID", definingValues));
		textControl.add("Show Text");

		// Show Cinematic
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_CINEMATIC, PlannerValueDef.TYPE_INT,
				"cinid", false, "Cinematic ID",
				"The ID of the cinematic that should be shown"));
		definingValues.add(new PlannerValueDef(
				ReferenceStore.REFERS_TRIGGER, PlannerValueDef.TYPE_INT,
				"exittrigid", true, "Exit Trigger ID",
				"The ID of the trigger that should be run once this cinematic completes"));
		allowableLines.add(new PlannerLineDef("showcin", "Show Map Event",
				"Shows the specified cinematic on the current map", definingValues));
		mapControl.add("Show Map Event");

		// Show Shop
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "buypercent", false, "Buy Percent",
				"The percent of the item price that items will cost when purchased from this shop. Should be in the form #.# (0.8, 1.2)"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "sellpercent", false, "Buy Percent",
				"The percent of the item price that items will be sold for when sold to this shop. Should be in the form #.# (0.8, 1.2)"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_MULTI_STRING, "itemssold", false, "Items For Sale",
				"The items that are sold in this shop"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "portrait", false, "Portrait Animation File",
				"The animation file containing the shopkeepers portrait"));
		allowableLines
				.add(new PlannerLineDef("showshop", "Show Shop",
						"Shows the shop menu with the specified items",
						definingValues));
		npcControl.add("Show Shop");

		// Add hero
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "heroid", false, "Hero ID",
				"The ID of the hero that should be added to the force"));
		allowableLines.add(new PlannerLineDef("addhero", "Add Hero",
				"Adds a new hero to the force", definingValues));
		partyControl.add("Add Hero");
		
		// Add multiple hero selection
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_MULTI_STRING, "heroids", false, "Hero IDs",
				"The ID of the hero that should be added to the force"));
		allowableLines.add(new PlannerLineDef("addmultihero", "Add Hero from Selection",
				"Displays a menu that allows the user to select one of the specified heroes to the party", definingValues));
		partyControl.add("Add Hero from Selection");
		
		// Remove hero
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "heroid", false, "Hero ID",
				"The ID of the hero that should be removed from the force"));
		allowableLines.add(new PlannerLineDef("removehero", "Remove Hero",
				"Removes the specified hero from the force. If this is called during a battle, then the hero will remain in the battle until the battle is ended.", definingValues));
		partyControl.add("Remove Hero");
		
		// Hide Roof
		/*
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "roofid", false, "Roof ID",
				"The ID of the roof that should no longer be visible"));
		allowableLines
				.add(new PlannerLineDef(
						"hideroof",
						"Hide Roof",
						"Hides the roof with designated ID. The roof will remain hidden until a show roof command is issued for that roof.",
						definingValues));

		// Show Roof
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "roofid", false, "Roof ID",
				"The ID of the roof that should be visible"));
		allowableLines.add(new PlannerLineDef("showroof", "Show Roof",
				"Shows the roof with designated ID.", definingValues));
				*/

		// Add NPC
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TEXT,
				PlannerValueDef.TYPE_INT, "textid", false, "Text Id",
				"The id of the text that should be displayed when this npc is talked to'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING, 
				"name", true, "Name", "The unique name of this npc that should be used to identify it for use in triggers"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "animation", false, "Animation",
				"The animation that should be used to display this NPC. The portrait for the speech will be selected during text creation'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "wander", false, "Wander Distance",
				"The amount of tiles this NPC can wander from his start position. A value of 0 means he will stand still'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "facing", false, "Initial Facing",
				"The direction that this npc will initially face, if wander is greater then 0 then this is likely to change'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_LOCATIONS,
				PlannerValueDef.TYPE_STRING, "location", false, "Start Location",
				"The name of the map location to place this npc on"));
		allowableLines.add(new PlannerLineDef("addnpc", "Add NPC",
				"Adds an npc to the map at the specified location",
				definingValues));
		npcControl.add("Add NPC");
		
		// Change NPC animation
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Npc Name",
				"The name of the npc to be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "animation", false,
				"New Animations", "The new animations for this npc"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "facing", false, "Initial Facing",
				"The direction that this npc will initially face."));
		allowableLines.add(new PlannerLineDef("changenpc",
				"Change NPC Anim",
				"Changes an existing npcs animations to the specified animations.",
				definingValues));
		npcControl.add("Change NPC Anim");

		// Add Sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPRITE_IMAGE,
				PlannerValueDef.TYPE_STRING, "image", false, "Image Name",
				"The name of the image that should be displayed for this sprite"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Name",
				"The unique name of this sprite that should be used to identify it for use in triggers"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_INT, "searchtrigger", true, "Search Trigger",
				"The trigger (not battle trigger) that should be executed when this sprite is 'investigated'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_LOCATIONS,
				PlannerValueDef.TYPE_STRING, "location", false, "Start Location",
				"The name of the map location to place this sprite on"));
		allowableLines.add(new PlannerLineDef("addsprite", "Add Sprite",
				"Adds a sprite to the map at the specified location",
				definingValues));
		npcControl.add("Add Sprite");

		// Remove Sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Sprite Name",
				"The name of the sprite to be removed"));
		allowableLines.add(new PlannerLineDef("removesprite", "Remove Sprite",
				"Removes the sprite from the map with the specified name.",
				definingValues));
		npcControl.add("Remove Sprite");

		// Change Sprite Image
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Sprite Name",
				"The name of the sprite to be changed"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPRITE_IMAGE,
				PlannerValueDef.TYPE_STRING, "image", false,
				"New Sprite Image", "The new image for this sprite"));
		allowableLines.add(new PlannerLineDef("changesprite",
				"Change Sprite Image",
				"Changes an existing sprites image to the specified image.",
				definingValues));
		npcControl.add("Change Sprite Image");

		// Add Item
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_STRING, "itemid", false, "Item ID",
				"The item that should be given to the group"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TEXT,
				PlannerValueDef.TYPE_INT, "failuretext", false, "No Room Text ID",
				"The text that should be displayed if the party has no room for the item."));
		allowableLines
				.add(new PlannerLineDef(
						"additem",
						"Add Item to Group",
						"Gives the specified item to the first person with room in the group.",
						definingValues));
		partyControl.add("Add Item to Group");
		
		// Revive Heroes
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "hero", true, "Hero to Revive",
				"If specified only this hero will be revived."));
		allowableLines
				.add(new PlannerLineDef(
						"reviveheroes",
						"Revive Heroes",
						"'Revives' all heroes if no specific hero is specified or revives only the specified hero; this will not bring them back in to an active battle,"
						+ " but can be used between scenes so that they are not dead for the next battle.",
						definingValues));
		partyControl.add("Revive Heroes");
		
		// Kill Enemy
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "unitid", false, "Unit ID",
				"The unit id of the enemies should be killed"));
		allowableLines
				.add(new PlannerLineDef(
						"killenemies",
						"Kill Enemies",
						"'Kill all enemies with the specified unitid. This should only be usedin battle.",
						definingValues));
		battleControl.add("Kill Enemies");
		
		// Run Triggers
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_MULTI_INT, "triggers", true,
				"Triggers To Run",
				"The ID of the triggers to run"));
		allowableLines.add(new PlannerLineDef(
				"runtriggers",
				"Run Triggers",
				"Runs the specified triggers. This is primarily used to break up triggers with parts "
				+ "you don't want to run multiple times into smaller parts that can be run multiple times.",
				definingValues));
		progressControl.add("Run Triggers");
		
		// Show NPC Speech
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "npcname", true,
				"Npc Name",
				"The name of the npc to drive speech with"));
		allowableLines.add(new PlannerLineDef(
				"npcspeech",
				"Show NPC Speech",
				"Drives speech with the given npc as if they were spoken to directly. This allows"
				+ " for speaking with npcs over counters and at range.",
				definingValues));
		textControl.add("Show NPC Speech");
		
		// Set egress point
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapname", false,
				"Map name",
				"The name of map (map data) that the hero should egress to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "locx", false,
				"Map Tile X Coordinate",
				"The X coordinate of the tile that the hero should egress to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "locy", false,
				"Map Tile Y Coordinate",
				"The Y coordinate of the tile that the hero should egress to"));
		allowableLines.add(new PlannerLineDef(
				"setegress",
				"Set Egress Point",
				"Sets the location that the hero should egress to if they have not saved",
				definingValues));
		progressControl.add("Set Egress Point");
		
		// Set egress location
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MAPDATA,
				PlannerValueDef.TYPE_STRING, "mapname", false,
				"Map name",
				"The name of map (map data) that the hero should egress to"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "location", false,
				"Egress Location",
				"The name of the map location that the hero should egress to"));
		allowableLines.add(new PlannerLineDef(
				"setegressloc",
				"Set Egress Location",
				"Sets the location that the hero should egress to if they have not saved",
				definingValues));
		progressControl.add("Set Egress Location");
		
		// Exit Game
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines
				.add(new PlannerLineDef(
						"exit",
						"Exit Game",
						"Causes the game to exit.",
						definingValues));
		progressControl.add("Exit Game");
		
		// Save Game
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines
				.add(new PlannerLineDef(
						"save",
						"Save Game",
						"Saves the game.",
						definingValues));
		progressControl.add("Save Game");
		
		triggerContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_TRIGGER - 1, menuLayout);
		containersByName.put("trigger", triggerContainer);
	}
	
	public static void setupConditionDefinition(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef conditionContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "description", false,
				"Description", "Description"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_MULTI_INT, "triggerid", false, "Trigger ID",
				"The IDs of thes trigger that should be activated upon condition completion. If multiple conditions"
				+ " are specified then ALL of the conditions must be met simultaneously for these triggers to be executed."));

		PlannerLineDef definingLine = new PlannerLineDef("condition", "Condition",
				"", definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();

		// Set Battle Condition - Enemy Death
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "unitid", false,
				"Enemy Unit Id",
				"The unit id (as specified on the map) of the enemy whose death will trigger this condition"));
		allowableLines.add(new PlannerLineDef("enemydeath", "On Enemy Death",
				"Sets a condition for the battle that upon the specified enemies death, the specified trigger will be executed.",
				definingValues));
		
		// Set Battle Condition - Hero Death
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "unitid", false,
				"Hero Id",
				"The id of the hero whose death will trigger this condition"));
		allowableLines.add(new PlannerLineDef("herodeath", "On Hero Death",
				"Sets a condition that will activate the given trigger if the specified hero dies.",
				definingValues));
		
		// Enter location
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_LOCATIONS,
				PlannerValueDef.TYPE_STRING, "location", false,
				"Location Name",
				"The name of the location that needs to be entered"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "immediate", false,
				"Immediately Moved",
				"If true then this condition will be met the moment a hero first starts moving in to a location. If this is false "
				+ "then this condition will be met once a hero has completed moving in to the location. This value should ALWAYS "
				+ "be set to false for conditions defined in battles"));
		allowableLines.add(new PlannerLineDef("enterloc", "On Location Enter",
				"Sets a condition that will activate the given trigger if a hero enters the "
				+ "given location in the town state or if a hero ends their turn in the "
				+ "given location in the battle state.",
				definingValues));
		
		// Location contains
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_LOCATIONS,
				PlannerValueDef.TYPE_STRING, "location", false,
				"Location Name",
				"The name of the location that heroes/enemies will be checked for presence in"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "enemy", false,
				"Check Enemy Amount",
				"If true then this condition will be met when the amount of enemies in the given location "
				+ "meets the given numeric constraints. If false then this condition will be met when the "
				+ "amount of heroes in the given location meets the given numeric constraints"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_OPERATOR,
				PlannerValueDef.TYPE_STRING, "operator", false,
				"Numeric Operator",
				"The operator that should be used to determine if the given amount of heroes/enemies in the "
				+ "specified location satisfies the numeric condition."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "amount", false,
				"Amount",
				"The amount of heroes/enemies that should be compared against the actual amount of heroes/enemies "
				+ "in the given location using the specified operator."));
		allowableLines.add(new PlannerLineDef("loccontains", "Location Contains",
				"Sets a condition that will activate when the given amount of heroes/enemies "
				+ "are present in the specified area. This has no real use in the town state "
				+ "and therefore should only be used in the battle state.",
				definingValues));
		
		// Enemies Remaining
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_OPERATOR,
				PlannerValueDef.TYPE_STRING, "operator", false,
				"Numeric Operator",
				"The operator that should be used to determine if the given amount of enemies satisfies the numeric condition."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "amount", false,
				"Amount",
				"The amount of enemies that should be compared against the actual amount of enemies "
				+ "using the specified operator."));
		allowableLines.add(new PlannerLineDef("enemremain", "Enemies Remaining",
				"Sets a condition that will activate when the given amount of enemies meet the numeric requirements set.",
				definingValues));
		
		// Map Loaded
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("maploaded", "Map Loaded",
				"Sets a condition that will activate when the map is loaded.",
				definingValues));
		
		// Quest Completed
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_QUEST,
				PlannerValueDef.TYPE_STRING, "quest", false,
				"Quest ID",
				"The id of the quest that must be completed."));
		allowableLines.add(new PlannerLineDef("questcomp", "Quest Completed",
				"Sets a condition that will activate once the given quest is completed.",
				definingValues));
		
		// Hero in battle
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_HERO,
				PlannerValueDef.TYPE_STRING, "id", false,
				"Hero Id",
				"The id of the hero that is checked for participation and being alived in the current battle."));
		allowableLines.add(new PlannerLineDef("heroinbat", "(Qualifier): Hero In Battle",
				"Sets a condition that should be used as a Qualifier (that is, this can't actually drive triggers in itself,"
				+ "it should be used in conjunction with an on death or location based condition) with other conditions that ensures "
				+ "that the given hero is in the current battle and is alive. This should only be used in battle.",
				definingValues));
		
		// Enemy in battle
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "id", false,
				"Enemy Unit Id",
				"The unit id (as specified on the map) of the enemy that is checked for participation and being alived in the current battle."));
		allowableLines.add(new PlannerLineDef("enemyinbat", "(Qualifier): Enemy In Battle",
				"Sets a condition that should be used as a Qualifier (that is, this can't actually drive triggers in itself,"
				+ "it should be used in conjunction with an on death or location based condition) with other conditions that ensures "
				+ "that the given enemy is in the current battle and is alive. This should only be used in battle.",
				definingValues));
		
		// Search location
		// DISABLED BECAUSE WE ALREADY HAVE A SEARCH AREA 
		/*
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_LOCATIONS,
				PlannerValueDef.TYPE_STRING, "location", false,
				"Location Name",
				"The name of the location that needs to be entered"));
		allowableLines.add(new PlannerLineDef("searchloc", "On Location Searched",
				"Sets a condition that will activate the given trigger when the specified location is 'searched'",
				definingValues));
				*/

		conditionContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				ReferenceStore.REFERS_CONDITIONS - 1);
		containersByName.put("condition", conditionContainer);
	}

	public static void setupMapEditorDefinitions(ReferenceStore referenceStore,
			Hashtable<String, PlannerContainerDef> containersByName) {
		PlannerContainerDef plannerContainer;

		// Setup defining line
		ArrayList<PlannerValueDef> definingValues = new ArrayList<PlannerValueDef>();
		PlannerLineDef definingLine = new PlannerLineDef("mapedit", "MapEdit", "",
				definingValues);

		// Setup available types
		ArrayList<PlannerLineDef> allowableLines = new ArrayList<PlannerLineDef>();
		
		// empty
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("", "",
				"Unspecified location type",
				definingValues));

		// start
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "exit", false, "Start Name",
				"The name that this start location should be referenced by for triggers that must specify an 'Entrance Location'"));
		allowableLines.add(new PlannerLineDef("start", "start",
				"Marks this as a location that heroes can enter maps from or start battle at",
				definingValues));

		// npc
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TEXT,
				PlannerValueDef.TYPE_INT, "textid", false, "Text Id",
				"The id of the text that should be displayed when this npc is talked to'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE, PlannerValueDef.TYPE_STRING, 
				"name", true, "Name", "The unique name of this npc that should be used to identify it for use in triggers"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ANIMATIONS,
				PlannerValueDef.TYPE_STRING, "animation", false, "Animation",
				"The animation that should be used to display this NPC. The portrait for the speech will be selected during text creation'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "wander", false, "Wander Distance",
				"The amount of tiles this NPC can wander from his start position. A value of 0 means he will stand still'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_DIRECTION,
				PlannerValueDef.TYPE_INT, "facing", false, "Initial Facing",
				"The direction that this npc will initially face, if wander is greater then 0 then this is likely to change'"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "throughwall", false, "Can talk through wall",
				"If set to true then this NPC can be spoken to at a range of two tiles (twice normal) this should be used for shop keepers or places beghind bars/altars."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "noanimate", false, "Prevent NPC Animation",
				"If set to true, this NPC will only display the first frame of the direction they are facing and will not animate."));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_BOOLEAN, "noturn", false, "Do not talk when spoken to",
				"If set to true this NPC will not turn when they are spoken to (searched)."));
		allowableLines.add(new PlannerLineDef("npc", "npc",
				"Marks this locations as the starting place for an npc. Depending on the 'wander' the npc may not remain here.",
				definingValues));

		// enemy
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ENEMY,
				PlannerValueDef.TYPE_STRING, "enemyid", false, "Enemy ID",
				"The type of enemy that should be at this location"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_AI,
				PlannerValueDef.TYPE_STRING, "ai", false, "Enemy AI",
				"The type of AI this enemy should use once it is close to the heroes"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_AI_APPROACH,
				PlannerValueDef.TYPE_STRING, "aiapproach", false, "Enemy Approach Speed",
				"The type of AI this enemy should use to approach the heroes. Do not use follow or move to location here set it via a trigger"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "unit", false, "Unit ID",
				"Use this ID to specifiy which enemy should be the target of triggers (Change AI)"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_INT, "vision", false, "Vision",
				"Max range that this enemy will look to find targets to engage with. This value can not be less then the enemies attack range. A value of 0 = infinite"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_MUSIC,
				PlannerValueDef.TYPE_STRING, "music", true, "Custom Music",
				"The music that should be played when this enemy attacks, this overrides script values."));
		allowableLines.add(new PlannerLineDef("enemy", "enemy",
				"Creates an enemy at this location at the start of battle",
				definingValues));

		// terrain
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TERRAIN,
				PlannerValueDef.TYPE_STRING, "type", false, "Land Type",
				"Terrain type for combatants that end there turn on this location"));
		allowableLines.add(new PlannerLineDef("terrain", "terrain",
				"Defines terrain type for combatants that end there turn on this location",
				definingValues));

		// battleregion
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("battleregion", "battleregion",
				"Defines the area that any hero/enemy and the battle cursor can be moved to in a battle on this map",
				definingValues));
		
		// door
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPRITE_IMAGE,
				PlannerValueDef.TYPE_STRING, "image", false, "Door Image",
				"The image that should be used for this door"));
		allowableLines.add(new PlannerLineDef("door", "door",
				"Defines this area as a door, when the hero walks in to it it will disappear",
				definingValues));
		
		// roof
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("roof", "roof",
				"Defines this area as a roof location, any tiles on the 'Roof' layer on the map will be displayed when the character is not in this location",
				definingValues));

		// sprite
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "image", false, "Image Name",
				"The name of the image that should be displayed for this sprite"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_NONE,
				PlannerValueDef.TYPE_STRING, "name", false, "Name",
				"The unique name of this sprite that should be used to identify it for use in triggers"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_INT, "searchtrigger", true, "Search Trigger",
				"The trigger (not battle trigger) that should be executed when this sprite is 'investigated'"));
		allowableLines.add(new PlannerLineDef("sprite", "sprite",
				"Places a static sprite at this location", definingValues));

		// search area
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_TRIGGER,
				PlannerValueDef.TYPE_INT, "searchtrigger", false, "Search Trigger",
				"The trigger (not battle trigger) that should be executed when this area is 'investigated'"));
		allowableLines.add(new PlannerLineDef("searcharea", "searcharea",
				"Places a searchable area at this location, it has no visible aspect", definingValues));

		// chest
		definingValues = new ArrayList<PlannerValueDef>();
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_SPRITE_IMAGE,
				PlannerValueDef.TYPE_STRING, "spriteimage", false, "Sprite Image",
				"The image of the chest lid"));
		definingValues.add(new PlannerValueDef(ReferenceStore.REFERS_ITEM,
				PlannerValueDef.TYPE_INT, "itemid", true, "Item",
				"The item that will be found by 'searching' this chest."));
		allowableLines.add(new PlannerLineDef("chest", "chest",
				"Marks the location as a chest with the given item contained inside.", definingValues));

		// stairs
		definingValues = new ArrayList<PlannerValueDef>();
		allowableLines.add(new PlannerLineDef("stairs", "stairs",
				"Marks a given location as 'stairs', movement along this path will ignore whether a tile is marked moveable.", definingValues));

		
		plannerContainer = new PlannerContainerDef(definingLine,
				allowableLines, referenceStore,
				-1);
		containersByName.put("mapedit", plannerContainer);
	}
}
