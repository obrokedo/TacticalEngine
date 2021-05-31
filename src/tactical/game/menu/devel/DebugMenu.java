package tactical.game.menu.devel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import tactical.engine.TacticalGame;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.battle.BattleEffect;
import tactical.game.constants.Direction;
import tactical.game.input.UserInput;
import tactical.game.manager.TurnManager;
import tactical.game.menu.Menu;
import tactical.game.sprite.CombatSprite;
import tactical.game.trigger.Trigger;
import tactical.game.ui.Button;
import tactical.game.ui.ListUI;
import tactical.game.ui.ListUI.ResourceSelectorListener;
import tactical.game.ui.PaddedGameContainer;

public class DebugMenu extends Menu implements ResourceSelectorListener
{
	private enum DebugMenuState {
		CHOOSE_TRIGGER,
		SEE_QUEST,
		CHOOSE_SPRITE,
		SPRITE_OPTIONS,
		PLACE_SPRITE,
		SHOW_QUESTS
	}
	
	private StateInfo stateInfo;
	private ListUI triggerList;
	private ListUI questList;
	private ListUI effectList;
	private CombatSprite selectedSprite = null;
	private Button moveButton = new Button(270, 35, 140, 20, "Move Combatant");
	private Button killButton = new Button(270, 65, 140, 20, "Kill Combatant");
	private Button levelButton = new Button(270, 95, 140, 20, "Level Up Hero");
	private Button healButton = new Button(270, 125, 140, 20, "Heal");
	private Button setToOneButton = new Button(270, 155, 140, 20, "Set to 1 HP");
	
	private Button setDisplayAttributes = new Button(15, 600, 140, 20, "Display Options");
	
	private Button healHeroesOnTurn = new Button(270, 115, 180, 20, (TurnManager.healOnTurn ? "Disable" : "Enable") + " Heal Heroes on Turn");
	private Button debugAI = new Button(270, 85, 140, 20, (TurnManager.enableAIDebug ? "Disable" : "Enable") + " AI Debug");
	private Button chooseSprite = new Button(270, 55, 140, 20, "Choose Sprite");
	private Button killAll = new Button(270, 145, 180, 20, "Kill All But One");	
	private Button showQuests = new Button(270, 25, 200, 20, "Show Completed Quests");
	
	private int inputTimer = 0;
	private String triggerStatus = null;
	private DebugMenuState state = DebugMenuState.CHOOSE_TRIGGER;

	public DebugMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_DEBUG);
		this.stateInfo = stateInfo;
		
		this.triggerList = new ListUI(stateInfo.getPaddedGameContainer(), "Triggers", 5, 
				new ArrayList<String>(stateInfo.getResourceManager().getTriggers().stream().map(t -> t.getName()).
						filter(s -> !s.startsWith("Door Trigger 5") && !s.startsWith("SearchChest5")).collect(Collectors.toList())),
				22, true);
		this.triggerList.setListener(this);
		

	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		if (stateInfo.getPaddedGameContainer().getInput().isKeyDown(Input.KEY_ENTER))
		{
			stateInfo.getCamera().centerOnSprite(stateInfo.getCurrentSprite(), stateInfo.getCurrentMap());
			this.dispose();
			return MenuUpdate.MENU_CLOSE;
		}

		return MenuUpdate.MENU_NO_ACTION;
	}
	
	@Override
	public MenuUpdate update(long delta, StateInfo si) {
		if (inputTimer > 0) {
			inputTimer -= delta;
			return MenuUpdate.MENU_NO_ACTION;
		}
		
		int x = stateInfo.getPaddedGameContainer().getInput().getMouseX();
		int y = stateInfo.getPaddedGameContainer().getInput().getMouseY();
		
		int mouseMove = 10;
		int mouseBounds = 20;
		Camera camera = stateInfo.getCamera();
		if (state == DebugMenuState.CHOOSE_SPRITE || state == DebugMenuState.PLACE_SPRITE) {
			if (x > stateInfo.getPaddedGameContainer().getWidth() - mouseBounds) {
				camera.setLocation(camera.getLocationX() + mouseMove, 
						camera.getLocationY(), stateInfo);
			} else if (x < mouseBounds) {
				camera.setLocation(camera.getLocationX() - mouseMove, 
						camera.getLocationY(), stateInfo);
			}
			
			if (y > stateInfo.getPaddedGameContainer().getHeight() - mouseBounds) {
				camera.setLocation(camera.getLocationX(), 
						camera.getLocationY() + mouseMove, stateInfo);
			} else if (y < mouseBounds) {
				camera.setLocation(camera.getLocationX(), 
						camera.getLocationY() - mouseMove, stateInfo);
			}
		}
		
		boolean leftClick = stateInfo.getPaddedGameContainer().getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);
		boolean rightClick = stateInfo.getPaddedGameContainer().getInput().isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON);
		
		if (rightClick) {
			if (state == DebugMenuState.PLACE_SPRITE)
				state = DebugMenuState.SPRITE_OPTIONS;
			else if (state == DebugMenuState.SPRITE_OPTIONS)
				state = DebugMenuState.CHOOSE_TRIGGER;
			else if (state == DebugMenuState.CHOOSE_SPRITE)
				state = DebugMenuState.CHOOSE_TRIGGER;
			else if (state == DebugMenuState.SHOW_QUESTS)
				state = DebugMenuState.CHOOSE_TRIGGER;
			
			timerReset();
		}
		
		if (leftClick && (state == DebugMenuState.CHOOSE_SPRITE || state == DebugMenuState.PLACE_SPRITE)) {
			int clickX =  (int) (x / PaddedGameContainer.GAME_SCREEN_SCALE + camera.getLocationX()) ; 
			int clickY =  (int) (y / PaddedGameContainer.GAME_SCREEN_SCALE + camera.getLocationY());
			
			int tilePixelX = clickX / stateInfo.getTileWidth() * stateInfo.getTileWidth();
			int tilePixelY = clickY / stateInfo.getTileHeight() * stateInfo.getTileHeight();
			CombatSprite cs = stateInfo.getCombatSpriteAtMapLocation(tilePixelX, 
					tilePixelY, false);
			if (cs == null) {
				cs = stateInfo.getCombatSpriteAtMapLocation(tilePixelX, 
						tilePixelY, true);
			}
			if (state == DebugMenuState.CHOOSE_SPRITE) {
				this.selectedSprite = cs;
				if (selectedSprite != null) {
					state = DebugMenuState.SPRITE_OPTIONS;
					
					if (effectList != null)
						effectList.unregisterListeners(stateInfo.getPaddedGameContainer());
					
					effectList = new ListUI(stateInfo.getPaddedGameContainer(), "Add Effects", 250, 200,
							Arrays.asList(TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().getBattleEffectList()));
					effectList.setListener(this);
				}
			} else if (state == DebugMenuState.PLACE_SPRITE && cs == null) {
				selectedSprite.setLocation(tilePixelX, tilePixelY, stateInfo.getTileWidth(), stateInfo.getTileHeight());
				selectedSprite.setFacing(Direction.DOWN);
			}
			timerReset();
		}
		
		if (state == DebugMenuState.SPRITE_OPTIONS) {
			if (moveButton.handleUserInput(x, y, leftClick)) {
				state = DebugMenuState.PLACE_SPRITE;
				timerReset();
			}
			if (killButton.handleUserInput(x, y, leftClick)) {
				selectedSprite.setCurrentHP(0);
				state = DebugMenuState.CHOOSE_TRIGGER;
				selectedSprite = null;
				timerReset();
			}
			if (levelButton.handleUserInput(x, y, leftClick)) {
				if (selectedSprite.isHero()) {
					selectedSprite.levelUpCustomStatistics();
					selectedSprite.getHeroProgression().levelUp(selectedSprite, selectedSprite.getHeroProgression().
							getLevelUpResults(selectedSprite));
					selectedSprite.setExp(0);
					
				}
				timerReset();
			}
			if (healButton.handleUserInput(x, y, leftClick)) {
				selectedSprite.setCurrentHP(selectedSprite.getMaxHP());
				selectedSprite.setCurrentMP(selectedSprite.getMaxMP());
				timerReset();
			}
			
			if (setToOneButton.handleUserInput(x, y, leftClick)) {
				selectedSprite.setCurrentHP(1);
				timerReset();
			}
			
			if (setToOneButton.handleUserInput(x, y, leftClick)) {
				selectedSprite.setCurrentHP(1);
				timerReset();
			}
			
			effectList.update(stateInfo.getPaddedGameContainer(), (int) delta);
		} 
		if (state == DebugMenuState.CHOOSE_TRIGGER) {
			if (setDisplayAttributes.handleUserInput(x, y, leftClick)) {
				String frame = JOptionPane.showInputDialog("Frame rate");
				if (frame != null && frame.trim().length() > 0)
					stateInfo.getPaddedGameContainer().setTargetFrameRate(Integer.parseInt(frame));
				String vsync = JOptionPane.showInputDialog("VSync on? (Y or N)");
				if (vsync != null && vsync.trim().length() > 0) {
					if (vsync.equalsIgnoreCase("y")) {
						stateInfo.getPaddedGameContainer().setVSync(true);
					} else {
						stateInfo.getPaddedGameContainer().setVSync(false);
					}
				}
			}
			
			if (stateInfo.isCombat() && chooseSprite.handleUserInput(x, y, leftClick)) {
				state = DebugMenuState.CHOOSE_SPRITE;
				triggerStatus = null;
				timerReset();
			}
			if (stateInfo.isCombat() && killAll.handleUserInput(x, y, leftClick)) {
				boolean first = true;
				for (CombatSprite cs : this.stateInfo.getCombatSprites()) {
					if (!cs.isHero()) {
						if (first) {
							cs.setCurrentHP(1);
							first = false;
						} else {
							cs.setCurrentHP(0);
						}
					}
				}
				timerReset();
			}
			if (stateInfo.isCombat() && debugAI.handleUserInput(x, y, leftClick)) {
				TurnManager.enableAIDebug = !TurnManager.enableAIDebug; 
				triggerStatus = null;
				timerReset();
			}
			if (stateInfo.isCombat() && healHeroesOnTurn.handleUserInput(x, y, leftClick)) {
				TurnManager.healOnTurn = !TurnManager.healOnTurn;
				triggerStatus = null;
				timerReset();
			}
			
			if (showQuests.handleUserInput(x, y, leftClick)) {
				state = DebugMenuState.SHOW_QUESTS;
				triggerStatus = null;
				if (questList != null)
					questList.unregisterListeners(stateInfo.getPaddedGameContainer());
				questList = new ListUI(stateInfo.getPaddedGameContainer(), "Completed Quests", 5, 
				new ArrayList<String>(stateInfo.getClientProgress().getQuestsCompleted()),
				22, true);
				timerReset();
			}
		}
		
		
		// stateInfo.getCamera().setLocation(x, y, stateInfo);
		
			
		triggerList.update(stateInfo.getPaddedGameContainer(), (int) delta);
		return super.update(delta, stateInfo);
	}

	private void timerReset() {
		inputTimer = 200;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics) {
		
		graphics.setColor(new Color(0, 0, 0, 120));
		graphics.scale(1.0f / PaddedGameContainer.GAME_SCREEN_SCALE, 1.0f / PaddedGameContainer.GAME_SCREEN_SCALE);
		if (state == DebugMenuState.CHOOSE_TRIGGER || state == DebugMenuState.SPRITE_OPTIONS) {
			graphics.fillRect(0, 0, gc.getWidth() / 2, gc.getHeight());
			triggerList.render(stateInfo.getPaddedGameContainer(), graphics);
			setDisplayAttributes.render(graphics);
			graphics.drawString("Vsync: " + gc.isVSyncRequested() + " Frame Rate: " + gc.getTargetFrameRate(), 15, 625);
		}
		else if (state == DebugMenuState.SHOW_QUESTS) {
			graphics.fillRect(0, 0, gc.getWidth() / 2, gc.getHeight());
			questList.render(stateInfo.getPaddedGameContainer(), graphics);
		}
		
		int mouseBounds = 20;
		
		graphics.setColor(Color.yellow);
		if (state == DebugMenuState.CHOOSE_SPRITE || state == DebugMenuState.PLACE_SPRITE)
			graphics.drawRect(mouseBounds, mouseBounds, gc.getWidth() - mouseBounds * 2, gc.getHeight() - mouseBounds * 2);
		graphics.setColor(Color.white);
		if (state == DebugMenuState.SPRITE_OPTIONS) {
			graphics.drawString("Selected " + selectedSprite.getName() + " " + selectedSprite.getUniqueEnemyId(), 200, 15);
			moveButton.render(graphics);
			killButton.render(graphics);
			levelButton.render(graphics);
			healButton.render(graphics);
			setToOneButton.render(graphics);
			
			effectList.render(gc, graphics);
		} 
		
		if (state == DebugMenuState.CHOOSE_TRIGGER) {
			if (stateInfo.isCombat()) {
				chooseSprite.render(graphics);
				killAll.render(graphics);
				debugAI.render(graphics);
				healHeroesOnTurn.render(graphics);
			}
			showQuests.render(graphics);
		}
		graphics.drawString("Right Click to Go Back", 5, 650);
		
		if (triggerStatus != null) {
			graphics.drawString(triggerStatus, 5, 620);
		}
		
		graphics.scale(PaddedGameContainer.GAME_SCREEN_SCALE, PaddedGameContainer.GAME_SCREEN_SCALE);
	}

	@Override
	public boolean resourceSelected(String selectedItem, ListUI parentSelector) {
		// What the fuck is this for?
		if (!selectedItem.equalsIgnoreCase(parentSelector.getSelectedResource())) {
			if (parentSelector == triggerList) {
				Optional<Trigger> trigger = stateInfo.getResourceManager().getTriggers().stream().filter(t -> t.getName().equalsIgnoreCase(selectedItem)).findFirst();
				if (trigger.isPresent()) {
					switch (trigger.get().perform(stateInfo)) {
					case EXCLUDED_QUEST_DONE:
						triggerStatus = "Trigger Not Run: Excluded Quest Completed";
						break;
					case IS_IMMEDIATE:
						trigger.get().perform(stateInfo, true);
						break;
					case NON_RETRIG:
						triggerStatus = "Trigger Not Run: Can only be run once per game";
						break;
					case REQUIRED_QUEST_NOT_DONE:
						triggerStatus = "Trigger Not Run: Required Quest Incomplete";
						break;
					case TRIGGERED:
						triggerStatus = "Trigger Run (May need to close menu)";
						break;
					case TRIGGER_ONCE:
						triggerStatus = "Trigger Not Run: Can only be run once per map";
						break;
					default:
						break;
					
					}
				}
			} else if (parentSelector == effectList) {
				BattleEffect be = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(selectedItem, 1);
				be.effectStarted(null, selectedSprite);
				selectedSprite.addBattleEffect(be);
			}
		}
		return true;
	}

	@Override
	public void dispose() {
		triggerList.unregisterListeners(stateInfo.getPaddedGameContainer());
		
		if (effectList != null)
			effectList.unregisterListeners(stateInfo.getPaddedGameContainer());
		if (questList != null)
			questList.unregisterListeners(stateInfo.getPaddedGameContainer());
	}
	
	
}
