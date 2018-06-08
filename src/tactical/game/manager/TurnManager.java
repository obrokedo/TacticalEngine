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
import tactical.game.turnaction.MoveToTurnAction;
import tactical.game.turnaction.PerformAttackAction;
import tactical.game.turnaction.TargetSpriteAction;
import tactical.game.turnaction.TurnAction;
import tactical.game.turnaction.WaitAction;

public class TurnManager extends Manager implements KeyboardListener
{
	private static final int UPDATE_TIME = 20;
	private static final int SPIN_TIME = 1500;
	
	/**
	 * So if AI is command people it just manually adds entries to the turn actions that determine
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

		if (!updated && turnActions.size() > 0 && turnActions.get(0).action == TurnAction.ACTION_MOVE_TO) {
			handleSpriteMovement(delta, turnActions.get(0));
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
	}

	private void processTurnActions(int delta, StateBasedGame game)
	{
		if (turnActions.size() == 0)
			return;
		TurnAction a = turnActions.get(0);
		outer: switch (a.action)
		{
			case TurnAction.ACTION_MANUAL_MOVE_CURSOR:

				// Get any combat sprite at the cursors location
				if (stateInfo.getCombatSpriteAtMapLocation(cursorTargetX, cursorTargetY, null) != null)
				{
					displayOverCursor = false;
				}

				if (cursor.getX() < cursorTargetX)
					cursor.setX(cursor.getX() + stateInfo.getTileWidth() / 6);
				else if (cursor.getX() > cursorTargetX)
					cursor.setX(cursor.getX() - stateInfo.getTileWidth() / 6);

				if (cursor.getY() < cursorTargetY)
					cursor.setY(cursor.getY() + stateInfo.getTileHeight() / 6);
				else if (cursor.getY() > cursorTargetY)
					cursor.setY(cursor.getY() - stateInfo.getTileHeight() / 6);

				stateInfo.getCamera().centerOnPoint((int) cursor.getX(), (int) cursor.getY(), stateInfo.getCurrentMap());

				if (cursorTargetX == cursor.getX() && cursorTargetY == cursor.getY())
				{
					// Get any combat sprite at the cursors location
					CombatSprite cs = stateInfo.getCombatSpriteAtMapLocation((int) cursor.getX(), (int) cursor.getY(), null);

					// if there is a combat sprite here display it's health panel
					if (cs != null)
					{
						stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
						cs.triggerOverEvent(stateInfo);
						landEffectPanel.setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(cs.getMovementType(),
								cs.getTileX(), cs.getTileY()));
						stateInfo.removePanel(landEffectPanel);
						stateInfo.addSingleInstancePanel(landEffectPanel);
					}
					else
					{
						displayOverCursor = true;
						stateInfo.removePanel(landEffectPanel);
						// Remove any health bar panels that may have been displayed from a sprite that we were previously over
						stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
					}
					turnActions.remove(0);
				}
				break;
			case TurnAction.ACTION_MOVE_CURSOR_TO_ACTOR:
				this.displayOverCursor = true;

				if (cursor.getX() == currentSprite.getLocX() &&
					cursor.getY() == currentSprite.getLocY())
				{
					if (ownsSprite)
					{
						stateInfo.addKeyboardListener(ms);
					}
					landEffectPanel.setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(currentSprite.getMovementType(),
							currentSprite.getTileX(), currentSprite.getTileY()));
					stateInfo.addSingleInstancePanel(landEffectPanel);
					displayMoveable = true;
					// The display cursor will toggled via the wait
					turnActions.remove(0);

					stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
					currentSprite.triggerOverEvent(stateInfo);

					if (turnActions.size() == 0)
						displayCursor = false;
				}

				// Move the cursor back to the target sprite
				if (cursor.getX() < currentSprite.getLocX())
				{
					if (currentSprite.getLocX() - cursor.getX() > stateInfo.getTileWidth() / 4)
						cursor.setX(cursor.getX() + stateInfo.getTileWidth() / 4);
					else
						cursor.setX(currentSprite.getLocX());
				}
				else if (cursor.getX() > currentSprite.getLocX())
				{
					if (cursor.getX() - currentSprite.getLocX() > stateInfo.getTileWidth() / 4)
						cursor.setX(cursor.getX() - stateInfo.getTileWidth() / 4);
					else
						cursor.setX(currentSprite.getLocX());
				}
				if (cursor.getY() < currentSprite.getLocY())
				{
					if (currentSprite.getLocY() - cursor.getY() > stateInfo.getTileWidth() / 4)
						cursor.setY(cursor.getY() + stateInfo.getTileHeight() / 4);
					else
						cursor.setY(currentSprite.getLocY());
				}
				else if (cursor.getY() > currentSprite.getLocY())
				{
					if (cursor.getY() - currentSprite.getLocY() > stateInfo.getTileWidth() / 4)
						cursor.setY(cursor.getY() - stateInfo.getTileHeight() / 4);
					else
						cursor.setY(currentSprite.getLocY());
				}

				stateInfo.getCamera().centerOnPoint((int) cursor.getX(), (int) cursor.getY(), stateInfo.getCurrentMap());
				break;
			case TurnAction.ACTION_MOVE_TO:
				handleSpriteMovement(delta, a);
				break;
			case TurnAction.ACTION_WAIT:
				WaitAction wait = (WaitAction) a;
				if (wait.waitAmt > 0)
				{
					wait.waitAmt -= delta;
				}
				else
				{
					displayCursor = false;
					turnActions.remove(0);
				}
				break;
			case TurnAction.ACTION_END_TURN:
				// This moveable space is no longer needed to destroy it
				turnActions.remove(0);
				if (currentSprite.getCurrentHP() > 0)
				{
					currentSprite.setFacing(Direction.DOWN);
	
					if (currentSprite.isHero())
					{
						stateInfo.checkTriggersMovement((int) currentSprite.getLocX(), (int) currentSprite.getLocY(), false);
					}
	
					if (currentSprite.getBattleEffects().size() > 0)
					{
						String text = "";
						for (int i = 0; i < currentSprite.getBattleEffects().size(); i++)
						{
							String effectText = null;
							BattleEffect be = currentSprite.getBattleEffects().get(i);
	
							 
							Log.debug("The battle effect: " + be.getBattleEffectId() + " was performed on " + currentSprite);
							effectText = be.getPerformEffectText(currentSprite);
							
							// If the sprite is still alive and the effect is done
							// then remove the effect and indicate such
							if (currentSprite.getCurrentHP() > 0 && be.isDone())
							{
								Log.debug("The battle effect: " + be.getBattleEffectId() + " has ended on " + currentSprite);
								currentSprite.getBattleEffects().remove(i--);
								be.effectEnded(currentSprite);
								effectText = effectText + "} " + be.effectEndedText(currentSprite);
								currentSprite.removeBattleEffect(be);
							}
							
							// If the current sprite is dead, there will be no more text added after this
							// so just end here
							if (currentSprite.getCurrentHP() <= 0) {
								if (effectText != null)
									text = text + effectText + "]";
								break;
							}
							
							if (effectText != null) {
								text = text + effectText;
								if (i + 1 == currentSprite.getBattleEffects().size())
									text += "]";
								else
									text += "} ";
							}
						}
	
						stateInfo.addMenu(new SpeechMenu(text, stateInfo));
						turnActions.add(new TurnAction(TurnAction.ACTION_WAIT_FOR_SPEECH));
						turnActions.add(new TurnAction(TurnAction.ACTION_PERFORM_EFFECTS));
						break;
					}
				}
	
				turnActions.add(new TurnAction(TurnAction.ACTION_WAIT_FOR_SPEECH));
				turnActions.add(new TurnAction(TurnAction.ACTION_CHECK_SPEECH_END_TURN));
				break;
			case TurnAction.ACTION_HIDE_MOVE_AREA:
				displayMoveable = false;
				turnActions.remove(0);
				break;
			case TurnAction.ACTION_TARGET_SPRITE:
				TargetSpriteAction tsa = (TargetSpriteAction) a;
				this.battleCommand = tsa.getBattleCommand();
				this.determineAttackableSpace(false);
				as.setTargetSprite(tsa.getTargetSprite(stateInfo.getCombatSprites()), stateInfo);
				displayAttackable = true;
				turnActions.remove(0);
				break;
			case TurnAction.ACTION_ATTACK_SPRITE:
				if (a.perform(delta, this, stateInfo))
					turnActions.remove(0);
				break;
			case TurnAction.ACTION_PERFORM_ATTACK:
				displayMoveable = false;
				stateInfo.removePanel(landEffectPanel);
				a.perform(delta, this, stateInfo);
				turnActions.remove(0);
				break;
			case TurnAction.ACTION_CHECK_DEATH:
				if (battleResults.death)
				{
					for (CombatSprite cs : stateInfo.getCombatSprites()) {
						if (cs.getCurrentHP() <= 0) {							
							turnActions.add(0, new WaitAction(0));
							break outer;
						}
					}
				}
				turnActions.add(new TurnAction(TurnAction.ACTION_END_TURN));
				turnActions.remove(0);
				break;
			case TurnAction.ACTION_PERFORM_EFFECTS:
				turnActions.remove(0);
				for (int i = 0; i < currentSprite.getBattleEffects().size(); i++)
				{
					BattleEffect be = currentSprite.getBattleEffects().get(i);
					be.performEffect(currentSprite);
					//TODO Add a new message saying the hero has died?
					if (currentSprite.getCurrentHP() <= 0) {
						// Wait for the "death spin"
						turnActions.add(new WaitAction(SPIN_TIME / UPDATE_TIME));
						turnActions.add(new TurnAction(TurnAction.ACTION_CURRENT_SPRITE_DEATH));
						break;
					}
					be.incrementTurn();
				}
				
				turnActions.add(new TurnAction(TurnAction.ACTION_WAIT_FOR_SPEECH));
				turnActions.add(new TurnAction(TurnAction.ACTION_CHECK_SPEECH_END_TURN));
				break;
			case TurnAction.ACTION_CURRENT_SPRITE_DEATH:
				turnActions.remove(0);
				stateInfo.addMenu(new SpeechMenu(TacticalGame.ENGINE_CONFIGURATIOR.getBattleFunctionConfiguration().getCombatantDeathText(null, currentSprite), stateInfo));
				break;
			case TurnAction.ACTION_CHECK_SPEECH_END_TURN:
				turnActions.remove(0);
				/*
				 * This path is ALWAYS taken at the end of the of a turn
				 * so although it seems to be strange that we remove panels here
				 * it makes sense as long as we want them on the screen while
				 * text is displayed
				 */
				stateInfo.removePanel(landEffectPanel);
				stateInfo.removePanel(PanelType.PANEL_HEALTH_BAR);
				stateInfo.removePanel(PanelType.PANEL_ENEMY_HEALTH_BAR);
				stateInfo.removeKeyboardListeners();
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, 
						TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration().getMenuRemovedSoundEffect(), 1f, false));
				displayAttackable = false;
				displayMoveable = false;
				
				if (!cinWasDisplayed)
					cursor.setLocation(currentSprite.getLocX(), currentSprite.getLocY());
				
				
				stateInfo.sendMessage(MessageType.NEXT_TURN, true);
				break;
			case TurnAction.ACTION_WAIT_FOR_SPEECH:
				if (!stateInfo.isMenuDisplayed(PanelType.PANEL_SPEECH))
				{
					turnActions.remove(0);
				}
				break;
		}
	}

	private void handleSpriteMovement(int delta, TurnAction turnAction)
	{
		if (movingSprite == null)
		{
			MoveToTurnAction move = (MoveToTurnAction) turnAction;

			Direction dir = Direction.UP;

			if (move.locX > currentSprite.getLocX())
				dir = Direction.RIGHT;
			else if (move.locX < currentSprite.getLocX())
				dir = Direction.LEFT;
			else if (move.locY > currentSprite.getLocY())
				dir = Direction.DOWN;
			else if (move.locY < currentSprite.getLocY())
				dir = Direction.UP;


			// This check catches enemies movement being ended and heroes location being "reset"
			if (move.locX == currentSprite.getLocX() && move.locY == currentSprite.getLocY())
			{
				turnActions.remove(0);
				if (!resetSpriteLoc)
					ms.setCheckEvents(true);
				if (turnActions.size() == 0)
				{
					// ms.setCheckEvents(true);
					if (resetSpriteLoc)
					{
						resetSpriteLoc = false;
						currentSprite.setFacing(Direction.DOWN);
						// If we are already reset then switch to cursor mode
						setToCursorMode();
					}
				}
				landEffectPanel.setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(currentSprite.getMovementType(),
						currentSprite.getTileX(), currentSprite.getTileY()));
				movingSprite = null;
				return;
			}


			movingSprite = new MovingSprite(currentSprite, dir, stateInfo);
		}

		// Check to see if we have arrived at our destination, if so
		// then we just remove this action and allow input for the moveablespace
		if (movingSprite.update(delta, resetSpriteLoc))
		{
			turnActions.remove(0);

			// If this a sprite resetting location then we don't end our movement
			// until they are actually at the start
			if (!resetSpriteLoc || (spriteStartPoint.getX() == currentSprite.getTileX() &&
				spriteStartPoint.getY() == currentSprite.getTileY()))
			{
				ms.setCheckEvents(true);
				// ms.handleKeyboardInput(stateInfo.getInput(), stateInfo);
				if (resetSpriteLoc)
					stateInfo.getInput().clear();
			}

			int movingRemainder = movingSprite.getMoveRemainder();
			movingSprite = null;

			if (turnActions.size() == 0)
			{

				if (resetSpriteLoc)
				{
					resetSpriteLoc = false;
					currentSprite.setFacing(Direction.DOWN);
					// If we are already reset then switch to cursor mode
					setToCursorMode();
				}
			}
			else if (turnActions.get(0).action == TurnAction.ACTION_MOVE_TO) {
				handleSpriteMovement(movingRemainder, turnActions.get(0));
			}

			landEffectPanel.setLandEffect(stateInfo.getCurrentMap().getLandEffectByTile(currentSprite.getMovementType(),
					currentSprite.getTileX(), currentSprite.getTileY()));

		}
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


		turnActions.add(new TurnAction(TurnAction.ACTION_MOVE_CURSOR_TO_ACTOR));
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
				stateInfo.sendMessage(new TurnActionsMessage(false, sprite.getAi().performAI(stateInfo, ms, currentSprite)), true);
			}
			// If we own this sprite then we add keyboard input listener
			else if (ownsSprite)
				stateInfo.addKeyboardListener(this);
		} else {
			stateInfo.addMenu(new SpeechMenu(sprite.getName() + " was unable to act due to the " + effectName, stateInfo));
			turnActions.add(new TurnAction(TurnAction.ACTION_END_TURN));
		}

		if (!cinWasDisplayed)
			displayCursor = true;
		cinWasDisplayed = false;
		displayOverCursor = false;
	}

	private void determineAttackableSpace(boolean playerAttacking)
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

	private void setToCursorMode()
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
						turnActions.add(new TurnAction(TurnAction.ACTION_END_TURN));
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
				turnActions.add(new TurnAction(TurnAction.ACTION_CHECK_DEATH));
				break;
			case PLAYER_END_TURN:
				turnActions.add(new TurnAction(TurnAction.ACTION_END_TURN));
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

	@Override
	public boolean handleKeyboardInput(UserInput input, StateInfo stateInfo)
	{
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
				turnActions.add(new TurnAction(TurnAction.ACTION_MOVE_CURSOR_TO_ACTOR));
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
				turnActions.add(new TurnAction(TurnAction.ACTION_MANUAL_MOVE_CURSOR));
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
}
