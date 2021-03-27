package tactical.game.manager;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.BattleResultsMessage;
import tactical.engine.message.BattleSelectionMessage;
import tactical.engine.message.IntMessage;
import tactical.engine.message.LocationMessage;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpeechMessage;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.message.TurnActionsMessage;
import tactical.engine.state.StateInfo;
import tactical.game.Range;
import tactical.game.ai.AIController;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.BattleResults;
import tactical.game.battle.command.BattleCommand;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.constants.Direction;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.item.Item;
import tactical.game.listener.KeyboardListener;
import tactical.game.menu.DefaultHeroStatMenu;
import tactical.game.menu.ItemMenu;
import tactical.game.menu.ItemOption;
import tactical.game.menu.LandEffectPanel;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.SpellMenu;
import tactical.game.menu.battle.BattleActionsMenu;
import tactical.game.menu.battle.BattleItemOptionMenu;
import tactical.game.menu.devel.BattleAIDebug;
import tactical.game.move.AttackableSpace;
import tactical.game.move.MoveableSpace;
import tactical.game.move.MovingSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.sprite.StaticSprite;
import tactical.game.turnaction.AttackSpriteAction;
import tactical.game.turnaction.CheckDeathAction;
import tactical.game.turnaction.EndTurnAction;
import tactical.game.turnaction.ManualCursorMoveAction;
import tactical.game.turnaction.MoveCursorToActorAction;
import tactical.game.turnaction.MoveToTurnAction;
import tactical.game.turnaction.PerformAttackAction;
import tactical.game.turnaction.TurnAction;
import tactical.game.turnaction.WaitAction;

public class TurnManager extends Manager implements KeyboardListener
{
	public static final int UPDATE_TIME = 20;
	public static final int SPIN_TIME = 1500;
	
	/**
	 * So if a Combatant is AI controlled it just manually adds entries to the turn actions that determine
	 * what type of battle command should be contained in the AttackSpriteAction. If this is being controlled
	 * by someone then we need to keep track of what kind of BattleCommand has been chosen via the menus
	 */
	private ArrayList<TurnAction> turnActions;
	private MoveableSpace ms;
	private AttackableSpace as;
	private CombatSprite currentSprite;
	private MovingSprite movingSprite;
	private Point spriteStartPoint;
	private BattleResults battleResults;
	private BattleCommand battleCommand;
	private SpellMenu spellMenu;
	private ItemMenu itemMenu;
	private BattleItemOptionMenu itemOptionMenu;
	private BattleActionsMenu battleActionsMenu;
	private LandEffectPanel landEffectPanel;
	private Image cursorImage;
	private Rectangle cursor;
	private int cursorTargetX, cursorTargetY;
	private int updateDelta = 0;
	private int activeCharFlashDelta = 0;
	private AIController aiController;

	private boolean ownsSprite;
	private boolean resetSpriteLoc = false;
	private boolean turnManagerHasFocus = false;
	private boolean displayOverCursor = false;
	// This describes the location and size of the moveable tiles array on the world
	private boolean displayMoveable = false;

	// Trying to draw attack coordinates
	private boolean displayAttackable = false;

	private boolean displayCursor = false;
	// A boolean indicating whether a cinematic was
	// displayed at the end of the last turn, in that
	// case do NOT show the cursor when it moves to the next character
	private boolean cinWasDisplayed = false;
	
	
	// DEBUG Variables
	public static boolean enableAIDebug = false;
	public static boolean healOnTurn = false;
	private BattleAIDebug battleAIDebug;

	@Override
	public void initialize() {
		turnActions = new ArrayList<TurnAction>();
		battleActionsMenu = new BattleActionsMenu(stateInfo);
		spellMenu = new SpellMenu(stateInfo);
		itemMenu = new ItemMenu(stateInfo);
		itemOptionMenu = new BattleItemOptionMenu(stateInfo);
		landEffectPanel = new LandEffectPanel();
		aiController = new AIController();
		movingSprite = null;
		ms = null;
		as = null;
		currentSprite = null;
		spriteStartPoint = null;
		battleResults = null;
		battleCommand = null;
		displayMoveable = displayAttackable = displayCursor = displayOverCursor =
				turnManagerHasFocus = resetSpriteLoc = ownsSprite = false;

		cursorImage = stateInfo.getResourceManager().getImage("battlecursor");
		cursor = new Rectangle(0, 0, stateInfo.getTileWidth(), stateInfo.getTileHeight());
		cursorTargetX = cursorTargetY = updateDelta = activeCharFlashDelta = 0;
		battleAIDebug = new BattleAIDebug(this, stateInfo);
	}

	public void update(StateBasedGame game, int delta)
	{
		updateDelta += delta;
		boolean updated = false;
		while (updateDelta >= UPDATE_TIME)
		{
			updateDelta -= UPDATE_TIME;
			// If there are actions to process then handle those
			if (turnActions.size() > 0)
			{
				processTurnActions(UPDATE_TIME, game);
			}

			if (displayAttackable && (!currentSprite.isHero() || currentSprite.getClientId() != stateInfo.getPersistentStateInfo().getClientId()))
				as.update(stateInfo);
			updated = true;
		}

		// To smooth out movement we allow updating of movement outside of the timestep
		if (!updated && turnActions.size() > 0 && turnActions.get(0).action == TurnAction.ACTION_MOVE_TO) {
			processTurnActions(delta, game);
		}

		if (turnManagerHasFocus)
		{
			activeCharFlashDelta += delta;

			if (activeCharFlashDelta > 500)
			{
				activeCharFlashDelta -= 500;
				currentSprite.setVisible(!currentSprite.isVisible());
			}
		}
		
		if (enableAIDebug)
			battleAIDebug.handleDebugInput(game.getContainer().getInput());
	}

	public void render(Graphics graphics)
	{

		if (displayMoveable)
			ms.renderMoveable(stateInfo.getPaddedGameContainer(), stateInfo.getCamera(), graphics);
		
		if (displayCursor)
		{
			cursorImage.draw(cursor.getX() - stateInfo.getCamera().getLocationX(),
					cursor.getY() - stateInfo.getCamera().getLocationY());
			/*
			graphics.setColor(Color.white);
			graphics.drawRect(cursor.getX() - stateInfo.getCamera().getLocationX() + stateInfo.getGc().getDisplayPaddingX(),
					cursor.getY() - stateInfo.getCamera().getLocationY(),
						stateInfo.getTileWidth() - 1, stateInfo.getTileHeight() - 1);
						*/
		}
	}
	
	

	public void renderCursor(Graphics graphics)
	{
		if (displayCursor && displayOverCursor)
		{
			cursorImage.draw(cursor.getX() - stateInfo.getCamera().getLocationX(),
					cursor.getY() - stateInfo.getCamera().getLocationY(), new Color(1f, 1f, 1f, .5f));
		}
		
		if (displayAttackable)
			as.render(stateInfo.getPaddedGameContainer(), stateInfo.getCamera(), graphics);
		
		if (displayMoveable)
			ms.renderHiddenMoveable(stateInfo.getPaddedGameContainer(), stateInfo.getCamera(), graphics);
		
		if (enableAIDebug) {
			battleAIDebug.renderDebugConfidences(graphics);
		}
	}

	private void processTurnActions(int delta, StateBasedGame game)
	{
		if (turnActions.size() == 0)
			return;
		TurnAction a = turnActions.get(0);
		if (a.perform(delta, this, stateInfo, turnActions))
			turnActions.remove(0);		
	}


	private void determineMoveableSpaces()
	{
		ms = MoveableSpace.determineMoveableSpace(stateInfo, currentSprite, this.ownsSprite);
	}

	private void initializeCombatantTurn(CombatSprite sprite)
	{
		stateInfo.removeKeyboardListeners();
		currentSprite = sprite;
		stateInfo.setCurrentSprite(currentSprite);
		battleAIDebug.clearDebugConfidences();

		as = null;
		this.battleResults = null;

		spriteStartPoint = new Point(sprite.getTileX(),
			sprite.getTileY());

		ownsSprite = false;
		
		// Check for heal debug
		if (TurnManager.healOnTurn && sprite.isHero()) {
			sprite.setCurrentHP(sprite.getMaxHP());
		}

		// This is the first combatant to act in the battle, the cursor will
		// not have been set to any location yet, so set it on the current sprite
		if (ms == null)
		{
			cursor.setLocation(currentSprite.getLocX(), currentSprite.getLocY());
			stateInfo.getCamera().centerOnSprite(currentSprite, stateInfo.getCurrentMap());
		}

		if (sprite.isHero() && sprite.getClientId() == stateInfo.getPersistentStateInfo().getClientId())
		{
			// stateInfo.sendMessage(new Message(MessageType.SHOW_BATTLEMENU));
			this.ownsSprite = true;
		}

		determineMoveableSpaces();


		turnActions.add(new MoveCursorToActorAction());
		turnActions.add(new WaitAction(150));

		boolean turnPrevented = false;
		String effectName = null;
		// Check to see if we can move
		for (BattleEffect effect : currentSprite.getBattleEffects()) {
			if (effect.preventsTurn()) {
				turnPrevented = true;
				effectName = effect.getBattleEffectId();
				break;
			}
		}



		if (!turnPrevented) {
			if (sprite.getAi() != null)
			{
				Log.debug("Perform AI for " + sprite.getName());
				if (!TurnManager.enableAIDebug)
					stateInfo.sendMessage(new TurnActionsMessage(false, sprite.getAi().performAI(stateInfo, ms, currentSprite)), true);
				else {
					battleAIDebug.determineConfidences();
					stateInfo.addKeyboardListener(this);
				}
			}
			// If we own this sprite then we add keyboard input listener
			else if (ownsSprite)
				stateInfo.addKeyboardListener(this);
		} else {
			stateInfo.addMenu(new SpeechMenu(sprite.getName() + " was unable to act due to the " + effectName, stateInfo));
			turnActions.add(new EndTurnAction());
		}

		if (!cinWasDisplayed)
			displayCursor = true;
		cinWasDisplayed = false;
		displayOverCursor = false;
	}

	public void determineAttackableSpace(boolean playerAttacking)
	{
		displayMoveable = false;

		// Determine how big the range should be
		int[][] range = null;
		int[][] area = null;
		boolean targetsHero = !currentSprite.isHero();
		boolean canTargetSelf = true;

		// If the command is to attack, get the characters attack range
		if (battleCommand.getCommand() == BattleCommand.COMMAND_ATTACK)
		{
			range = currentSprite.getAttackRange().getAttackableSpace();
			area = AttackableSpace.AREA_0;
		}
		// Otherwise it's an item or spell
		else {
			int areaSize = 1;
			if (battleCommand.getCommand() == BattleCommand.COMMAND_SPELL)

			{
				range = battleCommand.getSpell().getRange()[battleCommand.getLevel() - 1].getAttackableSpace();
				areaSize = battleCommand.getSpell().getArea()[battleCommand.getLevel() - 1];

				if (!battleCommand.getSpell().isTargetsEnemy())
					targetsHero = currentSprite.isHero();
			}
			else if (battleCommand.getCommand() == BattleCommand.COMMAND_ITEM)
			{
				Item item = battleCommand.getItem();
				// If the item does not have a "spell use" then just use the item use to get range and target
				if (item.getSpellUse() == null)
				{
					range = battleCommand.getItem().getItemUse().getRange().getAttackableSpace();

					if (!battleCommand.getItem().getItemUse().isTargetsEnemy())
						targetsHero = currentSprite.isHero();
				}
				// Otherwise load the spell from the item
				else
				{
					range = item.getSpellUse().getSpell().getRange()[item.getSpellUse().getLevel() - 1].getAttackableSpace();
					areaSize = item.getSpellUse().getSpell().getArea()[item.getSpellUse().getLevel() - 1];

					if (!item.getSpellUse().getSpell().isTargetsEnemy())
						targetsHero = currentSprite.isHero();
				}
			}
			else if (battleCommand.getCommand() == BattleCommand.COMMAND_GIVE_ITEM) {
				area = AttackableSpace.AREA_0;
				range = Range.ONE_ONLY.getAttackableSpace();
				targetsHero = currentSprite.isHero();
				canTargetSelf = false;
			}
			
			area = getAreaFromAreaSize(areaSize);
		}

		as = new AttackableSpace(stateInfo, currentSprite, targetsHero, range, area, canTargetSelf);
		if (as.getTargetAmount() == 0)
		{
			// Because the speech message is immediate this message also needs to be
			// otherwise the battle menu is shown after the speech menu
			stateInfo.sendMessage(new Message(MessageType.SHOW_BATTLEMENU, true, false));
			stateInfo.sendMessage(new SpeechMessage("No targets in range!<hardstop>"));
		}
		else
		{
			if (playerAttacking && currentSprite.getClientId() == stateInfo.getPersistentStateInfo().getClientId())
				stateInfo.addKeyboardListener(as);

			displayAttackable = true;
		}
	}

	public void setToCursorMode()
	{
		if (!ownsSprite)
			return;

		// If we are already reset then switch to cursor mode
		displayMoveable = false;
		displayCursor = true;
		displayOverCursor = false;
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration().getMenuAddedSoundEffect(), 1f, false));
		stateInfo.removeKeyboardListener();
		this.turnManagerHasFocus = true;
	}

	@Override
	public void recieveMessage(Message message) {
		switch (message.getMessageType())
		{
			case SHOW_BATTLEMENU:
				displayMoveable = false;
				displayAttackable = false;
				battleActionsMenu.initialize();
				stateInfo.addMenu(battleActionsMenu);
				break;
			case SHOW_MOVEABLE:
				displayMoveable = true;
				break;
			case SHOW_SPELLMENU:
				spellMenu.initialize();
				stateInfo.addMenu(spellMenu);
				break;
			case SHOW_ITEM_MENU:
				itemMenu.initialize(ItemOption.values()[((IntMessage) message).getValue()]);
				stateInfo.addMenu(itemMenu);
				break;
			case SHOW_ITEM_OPTION_MENU:
				itemOptionMenu.initialize();
				stateInfo.addMenu(itemOptionMenu);
				break;
			// THIS IS SENT BY THE OWNER
			case ATTACK_PRESSED:
				battleCommand = new BattleCommand(BattleCommand.COMMAND_ATTACK);
				determineAttackableSpace(true);
				break;
			// This message should never be sent by AI
			// THIS IS SENT BY THE OWNER
			case TARGET_SPRITE:
				// At this point we know who we intend to target, but we need to inject the BattleCommand.
				// Only the owner will have a value for the battle command so they will have to be
				// the one to send the BattleResults
				if (!(battleCommand.getCommand() == BattleCommand.COMMAND_GIVE_ITEM)) {										
					displayAttackable = false;
					displayMoveable = true;
					// Once we've targeted a sprite there can not be anymore keyboard input
					stateInfo.removeKeyboardListeners();
					if (battleCommand.getCommand() == BattleCommand.COMMAND_SPELL && !battleCommand.getSpell().showCombatAnimation()) {
						battleCommand.getSpell().performSkippedSpellAction(stateInfo);						
						turnActions.add(new EndTurnAction());
					} else
						turnActions.add(new AttackSpriteAction(
								((SpriteContextMessage) message).getSprites(stateInfo.getCombatSprites()), battleCommand));
				} else {
					Item item = battleCommand.getItem();
					CombatSprite targetSprite = ((SpriteContextMessage) message).getSprites(stateInfo.getCombatSprites()).get(0);
					
					if (targetSprite.getItemsSize() < 4) {
						displayAttackable = false;
						displayMoveable = true;
						
						// Once we've targeted a sprite there can not be anymore keyboard input
						stateInfo.removeKeyboardListeners();
						stateInfo.addMenu(new SpeechMenu(currentSprite.getName() + " gave the " + item.getName() + " to " + 
								targetSprite.getName() + ".<hardstop>", stateInfo));
						currentSprite.removeItem(item);
						targetSprite.addItem(item);
						turnActions.add(new EndTurnAction());
					} else {
						stateInfo.addMenu(new SpeechMenu(targetSprite.getName() + " has no room!<hardstop>", stateInfo));
					}
				}
				break;
			// THIS IS SENT BY THE OWNER
			case SELECT_SPELL:
				BattleSelectionMessage bsm = (BattleSelectionMessage) message;
				battleCommand = new BattleCommand(BattleCommand.COMMAND_SPELL,
						currentSprite.getSpellsDescriptors().get(bsm.getSelectionIndex()).getSpell(),
						currentSprite.getSpellsDescriptors().get(bsm.getSelectionIndex()), bsm.getLevel());
				determineAttackableSpace(true);
				break;
			case USE_ITEM:
				BattleSelectionMessage ibsm = (BattleSelectionMessage) message;
				battleCommand = new BattleCommand(BattleCommand.COMMAND_ITEM,
						currentSprite.getItem(ibsm.getSelectionIndex()));
				determineAttackableSpace(true);
				break;
			case GIVE_ITEM:
				ibsm = (BattleSelectionMessage) message;
				battleCommand = new BattleCommand(BattleCommand.COMMAND_GIVE_ITEM,
						currentSprite.getItem(ibsm.getSelectionIndex()));
				determineAttackableSpace(true);
				break;
			case COMBATANT_TURN:
				initializeCombatantTurn(((SpriteContextMessage) message).getSprite(stateInfo.getSprites()));
				break;
			case RESET_SPRITELOC:
				if (((int) spriteStartPoint.getX()) == currentSprite.getTileX() &&
						((int) spriteStartPoint.getY()) == currentSprite.getTileY())
				{
					currentSprite.setFacing(Direction.DOWN);
					// If we are already reset then switch to cursor mode
					setToCursorMode();
				}
				else
				{
					ms.setCheckEvents(false);
					ms.addMoveActionsToLocation((int) spriteStartPoint.getX(), (int) spriteStartPoint.getY(), currentSprite, turnActions);
					this.resetSpriteLoc = true;
				}
				break;
			case MOVETO_SPRITELOC:
				ms.setCheckEvents(false);

				MoveToTurnAction mtta = new MoveToTurnAction(((LocationMessage) message).locX,
						((LocationMessage) message).locY);

				turnActions.add(mtta);

				// Grab the final TurnAction, it should be a MoveToTurnAction. Process the first move
				// handleSpriteMovement(0, turnActions.get(turnActions.size() - 1));
				break;
			case HIDE_ATTACKABLE:
				displayAttackable = false;
				displayMoveable = true;

				if (ownsSprite)
					stateInfo.sendMessage(MessageType.SHOW_BATTLEMENU);
				break;
			case BATTLE_RESULTS:
				battleResults = ((BattleResultsMessage) message).getBattleResults();
				battleResults.initialize(stateInfo.getResourceManager());
				ArrayList<CombatSprite> transposedTargets = new ArrayList<>();
				for (CombatSprite oldTargets : battleResults.targets)
				{
					for (CombatSprite cs : stateInfo.getCombatSprites())
					{
						if (oldTargets.getId() == cs.getId())
						{
							transposedTargets.add(cs);
							break;
						}
					}
				}
				battleResults.targets = transposedTargets;
				stateInfo.sendMessage(MessageType.PAUSE_MUSIC);

				turnActions.add(new PerformAttackAction(battleResults));				
				break;
			case RETURN_FROM_ATTACK_CIN:
				turnActions.add(new CheckDeathAction());
				turnActions.add(new EndTurnAction());
				break;
			case PLAYER_END_TURN:
				turnActions.add(new EndTurnAction());
				break;
			case TURN_ACTIONS:
				TurnActionsMessage tam = (TurnActionsMessage) message;
				turnActions.addAll(tam.getTurnActions());
				break;
			case HIDE_ATTACK_AREA:
				stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
				displayAttackable = false;
				break;
			case SET_SELECTED_SPRITE:
				SpriteContextMessage scm = (SpriteContextMessage) message;
				as.setTargetSprite(scm.getCombatSprite(stateInfo.getCombatSprites()), stateInfo);
				break;
			case CIN_END:
				Point currCam = stateInfo.getCamera().getCenterOfCamera();
				cursor.setLocation(currCam.getX(), currCam.getY());
				cinWasDisplayed = true;
				break;
			case SHOW_SPELL_LEVEL:
				LocationMessage lm = (LocationMessage) message;
				KnownSpell ks = currentSprite.getSpellsDescriptors().get(lm.locX);
				int[][] range = ks.getSpell().getRange()[lm.locY].getAttackableSpace();
				int areaSize = ks.getSpell().getArea()[lm.locY];
				int[][] area = getAreaFromAreaSize(areaSize);
				
				as = new AttackableSpace(stateInfo, currentSprite, range, area);
				displayAttackable = true;
				break;
			case INITIALIZE_BATTLE:
				aiController.initialize(stateInfo.getCombatSprites());
				break;
			case SEARCH_IN_BATTLE:
				range = AttackableSpace.getAttackableArea(Range.ONE_ONLY);
				int rangeOffset = (range.length - 1) / 2;
				
				OUTER: for (int i = 0; i < range.length; i++)
				{
					for (int j = 0; j < range[0].length; j++)
					{
						if (range[i][j] == 1)
						{
							StaticSprite targetable = (StaticSprite) stateInfo.getSearchableAtTile(currentSprite.getTileX() - rangeOffset + i,
									currentSprite.getTileY() - rangeOffset + j);
							if (targetable != null) {
								targetable.triggerButton1Event(stateInfo);								
							}
							break;
						}
					}
				}
				turnActions.add(new EndTurnAction());
			default:
				break;

		}
	}
	
	private int[][] getAreaFromAreaSize(int areaSize) {
		int[][] area = null;
		switch (areaSize)
		{
			case AttackableSpace.AREA_ALL_INDICATOR:
				area = AttackableSpace.AREA_ALL;
				break;
			case 1:
				area = AttackableSpace.AREA_0;
				break;
			case 2:
				area = AttackableSpace.AREA_1;
				break;
			case 3:
				area = AttackableSpace.AREA_2;
				break;
		}
		return area;
	}

	@Override
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo)
	{
		if (enableAIDebug && battleAIDebug.acceptingInput()) {
			return battleAIDebug.handleDebugKeyboardInput(input);
		}
		
		if (turnActions.size() == 0)
		{
			boolean moved = false;
			if (input.isKeyDown(KeyMapping.BUTTON_1))
			{

			}
			else if (input.isKeyDown(KeyMapping.BUTTON_2))
			{
				this.turnManagerHasFocus = false;
				currentSprite.setVisible(true);
				turnActions.add(new MoveCursorToActorAction());
				stateInfo.removePanel(landEffectPanel);
				stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
				return true;
			}
			else if (input.isKeyDown(KeyMapping.BUTTON_3))
			{
				// Get any combat sprite at the cursors location
				CombatSprite cs = stateInfo.getCombatSpriteAtMapLocation((int) cursor.getX(), (int) cursor.getY(), null);

				// if there is a combat sprite here display it's health panel
				if (cs != null)
				{
					stateInfo.addMenu(TacticalGame.ENGINE_CONFIGURATIOR.getHeroStatMenu(stateInfo.getPaddedGameContainer(), cs, stateInfo));
					return true;
				}
				else {
					stateInfo.sendMessage(MessageType.SHOW_BATTLE_OPTIONS);
					return true;
				}
			}

			cursorTargetX = (int) cursor.getX();
			cursorTargetY = (int) cursor.getY();

			if (input.isKeyDown(KeyMapping.BUTTON_UP))
			{
				if (cursor.getY() > 0)
				{
					cursorTargetY = (int) (cursor.getY() - stateInfo.getTileHeight());
					moved = true;
				}
			}
			else if (input.isKeyDown(KeyMapping.BUTTON_DOWN))
			{
				if (cursor.getY() + stateInfo.getTileHeight() < stateInfo.getCurrentMap().getMapHeightInPixels())
				{
					cursorTargetY = (int) (cursor.getY() + stateInfo.getTileHeight());
					moved = true;
				}
			}

			if (!stateInfo.getResourceManager().getMap().isInBattleRegion(cursorTargetX, cursorTargetY))
			{
				cursorTargetY = (int) cursor.getY();
				moved = false;
			}

			if (input.isKeyDown(KeyMapping.BUTTON_LEFT))
			{
				if (cursor.getX() > 0)
				{
					cursorTargetX = (int) (cursor.getX() - stateInfo.getTileWidth());
					moved = true;
				}
			}
			else if (input.isKeyDown(KeyMapping.BUTTON_RIGHT))
			{
				if (cursor.getX() + stateInfo.getTileWidth() < stateInfo.getCurrentMap().getMapWidthInPixels())
				{
					cursorTargetX = (int) (cursor.getX() + stateInfo.getTileWidth());
					moved = true;
				}
			}

			if (!stateInfo.getResourceManager().getMap().isInBattleRegion(cursorTargetX, cursorTargetY))
			{
				cursorTargetX = (int) cursor.getX();
				moved = false;
			}

			if (moved)
			{
				turnActions.add(new ManualCursorMoveAction());
			}
		}

		return false;
	}

	public AIController getAiController() {
		return aiController;
	}

	public CombatSprite getCurrentSprite() {
		return currentSprite;
	}

	public void setDisplayAttackable(boolean displayAttackable) {
		this.displayAttackable = displayAttackable;
	}

	public boolean isDisplayOverCursor() {
		return displayOverCursor;
	}

	public void setDisplayOverCursor(boolean displayOverCursor) {
		this.displayOverCursor = displayOverCursor;
	}

	public int getCursorTargetX() {
		return cursorTargetX;
	}

	public int getCursorTargetY() {
		return cursorTargetY;
	}

	public Rectangle getCursor() {
		return cursor;
	}

	public LandEffectPanel getLandEffectPanel() {
		return landEffectPanel;
	}

	public BattleResults getBattleResults() {
		return battleResults;
	}

	public void setDisplayMoveable(boolean displayMoveable) {
		this.displayMoveable = displayMoveable;
	}

	public boolean isCinWasDisplayed() {
		return cinWasDisplayed;
	}

	public void setDisplayCursor(boolean displayCursor) {
		this.displayCursor = displayCursor;
	}

	public MoveableSpace getMoveableSpace() {
		return ms;
	}

	public boolean isOwnsSprite() {
		return ownsSprite;
	}

	public MovingSprite getMovingSprite() {
		return movingSprite;
	}

	public void setMovingSprite(MovingSprite movingSprite) {
		this.movingSprite = movingSprite;
	}

	public boolean isResetSpriteLoc() {
		return resetSpriteLoc;
	}

	public void setResetSpriteLoc(boolean resetSpriteLoc) {
		this.resetSpriteLoc = resetSpriteLoc;
	}
	
	public Point getSpriteStartPoint() {
		return spriteStartPoint;
	}

	public AttackableSpace getAttackableSpace() {
		return as;
	}

	public void setBattleCommand(BattleCommand battleCommand) {
		this.battleCommand = battleCommand;
	}
}
