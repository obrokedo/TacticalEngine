package tactical.game.manager;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
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
import tactical.game.ai.AIConfidence;
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
import tactical.game.menu.BattleActionsMenu;
import tactical.game.menu.HeroStatMenu;
import tactical.game.menu.ItemMenu;
import tactical.game.menu.ItemMenu.ItemOption;
import tactical.game.menu.ItemOptionMenu;
import tactical.game.menu.LandEffectPanel;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.SpellMenu;
import tactical.game.move.AttackableSpace;
import tactical.game.move.MoveableSpace;
import tactical.game.move.MovingSprite;
import tactical.game.sprite.CombatSprite;
import tactical.game.turnaction.AttackSpriteAction;
import tactical.game.turnaction.CheckDeathAction;
import tactical.game.turnaction.EndTurnAction;
import tactical.game.turnaction.ManualCursorMoveAction;
import tactical.game.turnaction.MoveCursorToActorAction;
import tactical.game.turnaction.MoveToTurnAction;
import tactical.game.turnaction.PerformAttackAction;
import tactical.game.turnaction.TurnAction;
import tactical.game.turnaction.WaitAction;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

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
	private ItemOptionMenu itemOptionMenu;
	private BattleActionsMenu battleActionsMenu;
	private LandEffectPanel landEffectPanel;
	private Image cursorImage;
	private Rectangle cursor;
	private int cursorTargetX, cursorTargetY;
	private int updateDelta = 0;
	private int activeCharFlashDelta = 0;

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
	private List<AIConfidence> debugConfidences;

	@Override
	public void initialize() {
		turnActions = new ArrayList<TurnAction>();
		battleActionsMenu = new BattleActionsMenu(stateInfo);
		spellMenu = new SpellMenu(stateInfo);
		itemMenu = new ItemMenu(stateInfo);
		itemOptionMenu = new ItemOptionMenu(stateInfo);
		landEffectPanel = new LandEffectPanel();
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
			handleDebugInput(game);
	}

	protected void handleDebugInput(StateBasedGame game) {
		if (debugConfidences != null && game.getContainer().getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			Input input = game.getContainer().getInput();
			int mx = (int)(input.getMouseX() / PaddedGameContainer.GAME_SCREEN_SCALE + stateInfo.getCamera().getLocationX()) / stateInfo.getCurrentMap().getTileEffectiveWidth();
			int my = (int)(input.getMouseY()  / PaddedGameContainer.GAME_SCREEN_SCALE + stateInfo.getCamera().getLocationY()) / stateInfo.getCurrentMap().getTileEffectiveWidth();
			JPanel panel = null;
			for (AIConfidence aic : debugConfidences) {
				if (aic.attackPoint != null && aic.attackPoint.x == mx && aic.attackPoint.y == my) {
					if (panel == null) {
						panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
						
						String approach = null;
						/*
						 *  public final static int APPROACH_REACTIVE = 0;
							public final static int APPROACH_KAMIKAZEE = 1;
							public final static int APPROACH_HESITANT = 2;
							public final static int APPROACH_FOLLOW = 3;
							public final static int APPROACH_MOVE_TO_POINT = 4;
							public final static int APPROACH_TARGET = 5;
						 */
						switch (currentSprite.getAi().getApproachType()) {
							case 0:
								approach = "Reactive";
								break;
							case 1:
								approach = "Kamikazee";
								break;
							case 2:
								approach = "Hesitant";
								break;
							case 3:
								approach = "Follow";
								break;
							case 4:
								approach = "Move to point";
								break;
							case 5:
								approach = "Approach target";
								break;
						}
						panel.add(new JLabel("Approach: " + approach));
					}
					JLabel label = new JLabel("<html><body style='width: 600px'>" + aic.toString() + "</body></html>");
					panel.add(label);					
				}
			}
			
			if (panel != null) {
				JFrame debugFrame = new JFrame("AI Debug");
				debugFrame.setContentPane(panel);
				debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				debugFrame.setMinimumSize(new Dimension(600, 400));
				debugFrame.pack();
				debugFrame.setVisible(true);
				debugFrame.toFront();
			}
		}
	}

	public void render(Graphics graphics)
	{

		if (displayMoveable)
			ms.renderMoveable(stateInfo.getPaddedGameContainer(), stateInfo.getCamera(), graphics);

		if (enableAIDebug) {
			renderDebugConfidences(graphics);
		}
		
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
	
	private void renderDebugConfidences(Graphics graphics) {
		if (debugConfidences != null && currentSprite != null) {		
			Hashtable<java.awt.Point, Integer> amtPerSpace = new Hashtable<>();
			for (AIConfidence aic : debugConfidences) {
				int amt = 0;
				
				if (aic.attackPoint != null) {
					graphics.setColor(Color.green);
					float x = aic.attackPoint.x * stateInfo.getCurrentMap().getTileEffectiveWidth() - 
							stateInfo.getCamera().getLocationX();
					float y = aic.attackPoint.y * stateInfo.getCurrentMap().getTileEffectiveHeight() - 
							stateInfo.getCamera().getLocationY();
					graphics.drawRect(x, y, stateInfo.getCurrentMap().getTileEffectiveWidth(), stateInfo.getCurrentMap().getTileEffectiveHeight());
					graphics.setColor(Color.white);
										
					if (amtPerSpace.containsKey(aic.attackPoint)) {
						amt = amtPerSpace.get(aic.attackPoint);
					}
					
					if (aic.confidence != Integer.MIN_VALUE)
						StringUtils.drawString("" + aic.confidence, (int) x + (10 * (amt / 4)), (int) y + (amt % 4) * 6, graphics);
					else
						StringUtils.drawString("L", (int) x + (10 * (amt / 4)), (int) y + (amt % 4) * 6, graphics);
					if (aic.target != null) {
						graphics.setColor(Color.orange);
						float tx = aic.target.getLocX() -  stateInfo.getCamera().getLocationX();
						float ty = aic.target.getLocY() -  stateInfo.getCamera().getLocationY();
						graphics.drawRect(tx, ty, 
								stateInfo.getCurrentMap().getTileEffectiveWidth(), stateInfo.getCurrentMap().getTileEffectiveHeight());
						graphics.drawRect(tx + 1, ty + 1, 
								stateInfo.getCurrentMap().getTileEffectiveWidth() - 2, stateInfo.getCurrentMap().getTileEffectiveHeight() - 2);
					}
					
					amtPerSpace.put(aic.attackPoint, new Integer(amt + 1));
				}
			}
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
		debugConfidences = null;

		as = null;
		this.battleResults = null;

		spriteStartPoint = new Point(sprite.getTileX(),
			sprite.getTileY());

		ownsSprite = false;

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
					debugConfidences = new ArrayList<>();
					sprite.getAi().performAI(stateInfo, ms, currentSprite, debugConfidences);
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
				Point currCam = stateInfo.getCamera().getCenterOfCamera(stateInfo.getCurrentMap());
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
	
	private boolean handleDebugKeyboardInput(UserInput input) {
		if (input.isKeyDown(KeyMapping.BUTTON_3))
		{
			stateInfo.removeKeyboardListeners();
			stateInfo.sendMessage(new TurnActionsMessage(false, currentSprite.getAi().performAI(stateInfo, ms, currentSprite)), true);
			return true;
		}
		else if (input.isKeyDown(KeyMapping.BUTTON_2))
		{
			debugConfidences = new ArrayList<>();
			currentSprite.getAi().performAI(stateInfo, ms, currentSprite, debugConfidences);
			return true;
		}
		return false;
	}

	@Override
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo)
	{
		if (enableAIDebug && debugConfidences != null) {
			return handleDebugKeyboardInput(input);
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
					stateInfo.addMenu(new HeroStatMenu(stateInfo.getPaddedGameContainer(), cs, stateInfo));
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
