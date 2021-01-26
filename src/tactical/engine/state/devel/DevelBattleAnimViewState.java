package tactical.engine.state.devel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import tactical.engine.TacticalGame;
import tactical.engine.state.AttackCinematicState;
import tactical.game.battle.BattleResults;
import tactical.game.battle.command.BattleCommand;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.menu.Menu;
import tactical.game.resource.EnemyResource;
import tactical.game.resource.HeroResource;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.Button;
import tactical.game.ui.ListUI;
import tactical.game.ui.ListUI.ResourceSelectorListener;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;

public class DevelBattleAnimViewState extends LoadableGameState implements ResourceSelectorListener {
	private WizardStep wizardIndex;
	private ListUI currentList;
	
	private CombatSprite attacker;
	private CombatSprite target;	

	private boolean attackerHero = true, targetHero = true;
	private ResourceManager fcrm;
	private StateBasedGame game;
	private Button reloadButton = new Button(660, 50, 165, 25, "Reload Animations");
	private Button reloadScriptsButton = new Button(660, 85, 165, 25, "Reload Scripts");
	private Button startButton = new Button(660, 155, 165, 25, "GO");
	
	private int backgroundIndex = 0;
	private Button chooseBackground = new Button(40, 60, 165, 25, "Background");
	
	private Button chooseAttackerType = new Button(40, 158, 165, 25, "Type");
	private Button chooseAttackerCombatant = new Button(225, 158, 165, 25, "Combatant");
	private Button chooseAttackerAction = new Button(410, 158, 165, 25, "Action");
	private Button chooseAttackerWeapon = new Button(595, 158, 165, 25, "Weapon");
	
	private Button chooseDefenderType = new Button(40, 256, 165, 25, "Type");
	private Button chooseDefenderCombatant = new Button(225, 256, 165, 25, "Combatant");
	private Button chooseDefenderAction = new Button(410, 256, 165, 25, "Action");
	private Button chooseDefenderWeapon = new Button(595, 256, 165, 25, "Weapon");
	
	private int nextInput = 0;
	
	private String attackAction;
	private String defenderAction;
	private String attackerWeapon = null;
	private String defenderWeapon = null;	
	
	private GameContainer container;
	
	public DevelBattleAnimViewState() {
		wizardIndex = null;
		startButton.setVisible(false);
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		currentList = null;
		this.container = container;
	}

	private void setupStep() {
		try {
			currentList = null;
			ArrayList<String> options = new ArrayList<>();
			String selectText = null;
			switch(wizardIndex) {
				case PICK_ATTACKER_TYPE:
					selectText = "Is the attacker a hero or enemy?";
					options.add("Hero"); options.add("Enemy");
					break;
				case PICK_TARGET_TYPE:
					selectText = "Is the target a hero or enemy?";
					options.add("Hero"); options.add("Enemy");
					break;
				case PICK_ATTACKER:
					selectText = getCombatantOptions(options, "attacking", attackerHero);
					break;
				case PICK_TARGET:
					selectText = getCombatantOptions(options, "the target", targetHero);
					break;
				case PICK_ATTACK_ACTION:
					selectText = "Choose the attackers action";
					options.add("Normal Attack"); options.add("Critical Attack"); options.add("Ranged Attack"); // options.add("Miss Attack"); options.add("Double Attack"); 
					String[] spells = TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().getSpellList();
					for (String spell : spells) {
						try {
							SpellDefinition initSpell = TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().createSpell(spell);
							for (int i = 0; i < initSpell.getMaxLevel(); i++) {
								options.add(spell + " " + (i + 1));
							}
						} catch (Exception e) {};
					}
					break;
				case PICK_DEFENDER_ACTION:
					selectText = "Choose the targets action";
					options.add("Take Damage"); options.add("Block");
					break;
				case PICK_BACKGROUND:
					selectText = "Select background index";
					for (int i = 0; i < 25; i++) {
						options.add(i + "");
					}
					break;
				case PICK_ATTACKER_WEAPON:
				case PICK_TARGET_WEAPON:
					selectText = "Choose Weapon";
					for (Item item : ItemResource.getAllWeapons()) {
						options.add(item.getName());
					}
					break;
			}
			if (options.size() > 0) {		
				container.getInput().removeAllListeners();
				currentList = new ListUI(container, selectText, 375, options);
				currentList.setListener(this);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error occurred preparing step " + wizardIndex + ": " + e.getMessage());
		}
	}

	private String getCombatantOptions(ArrayList<String> options, String replaceText, boolean hero) throws IOException {
		String selectText;
		if (hero) {
			selectText = "Select the hero that is " + replaceText;
			options.addAll(HeroResource.getHeroNames());
		} else {
			selectText = "Select the enemy that is " + replaceText;
			options.addAll(EnemyResource.getEnemyNames());
		}
		return selectText;
	}
	
	enum WizardStep {
		PICK_ATTACKER_TYPE,
		PICK_ATTACKER,
		PICK_TARGET_TYPE,
		PICK_TARGET,
		PICK_ATTACK_ACTION,
		PICK_DEFENDER_ACTION,
		PICK_BACKGROUND,
		PICK_ATTACKER_WEAPON,
		PICK_TARGET_WEAPON,
		GO
	}

	@Override
	public boolean resourceSelected(String selectedItem, ListUI parentSelector) {
		nextInput = 200;
		switch (wizardIndex) {
			case PICK_ATTACKER_TYPE:
				attackerHero = selectedItem.equalsIgnoreCase("Hero");
				chooseAttackerWeapon.setVisible(attackerHero);
				break;
			case PICK_TARGET_TYPE:
				targetHero = selectedItem.equalsIgnoreCase("Hero");
				chooseDefenderWeapon.setVisible(targetHero);
				break;
			case PICK_ATTACKER:
				if (attackerHero)
					attacker = HeroResource.getHero(selectedItem);
				else 
					attacker = EnemyResource.getEnemy(selectedItem);
				break;
			case PICK_TARGET:
				if (targetHero)
					target = HeroResource.getHero(selectedItem);
				else 
					target = EnemyResource.getEnemy(selectedItem);
				
				break;
			case PICK_ATTACKER_WEAPON:
				this.attackerWeapon = selectedItem;
				break;
			case PICK_TARGET_WEAPON:
				this.defenderWeapon = selectedItem;
				break;
			case PICK_ATTACK_ACTION:
				attackAction = selectedItem;
				break;
			case PICK_DEFENDER_ACTION:
				defenderAction = selectedItem;
				break;
			case PICK_BACKGROUND:
				this.backgroundIndex = Integer.parseInt(selectedItem);
				break;
			default:
				break;
		}		
		wizardIndex = null;
		currentList = null;
		
		if (attackAction != null && defenderAction != null && attacker != null && defenderAction != null) {
			startButton.setVisible(true);
		} else {
			startButton.setVisible(false);
		}
		
		return false;
	}
	
	private void showBattleAction() {
		BattleCommand battleCommand = null;
		if (attackAction.equalsIgnoreCase("Normal Attack") || attackAction.equalsIgnoreCase("Critical Attack") || attackAction.equalsIgnoreCase("Ranged Attack")) {
			battleCommand = new BattleCommand(BattleCommand.COMMAND_ATTACK);
		} else {
			String[] splitSpell = attackAction.split(" ");
			SpellDefinition spell = TacticalGame.ENGINE_CONFIGURATIOR.getSpellFactory().createSpell(splitSpell[0]);
			KnownSpell ks = new KnownSpell(spell.getId(), (byte) 4, spell);
			battleCommand = new BattleCommand(BattleCommand.COMMAND_SPELL, ks.getSpell(), ks, Integer.parseInt(splitSpell[1]));
		}
		
		attacker.initializeSprite(fcrm);
		target.initializeSprite(fcrm);
		
		if (attackerHero && attackerWeapon != null) {
			EquippableItem ei = (EquippableItem) ItemResource.getItem(ItemResource.getItemIdByName(attackerWeapon), fcrm);
			attacker.addItem(ei);
			attacker.equipItem(ei);
		}
		
		if (targetHero && defenderWeapon != null) {
			EquippableItem ei = (EquippableItem) ItemResource.getItem(ItemResource.getItemIdByName(defenderWeapon), fcrm);
			target.addItem(ei);
			target.equipItem(ei);
		}
		
		target.setCurrentHP(1);
		
		attacker.initializeStats();
		target.initializeStats();
		
		fcrm.getMap().setBackgroundImageIndex(backgroundIndex);
		// target.setCurrentHP(1);
		/*
		List<CombatSprite> targets = new ArrayList<>();
		
		CombatSprite cs = HeroResource.getHero(1);
		cs.initializeSprite(fcrm);
		cs.initializeStats();
		cs.setLocation(0, 1, 1, 1);
		*/
		// targets.add(target); targets.add(cs);
		// targets.add(target); targets.add(target);
		
		BattleResults br = BattleResults.determineBattleResults(attacker, Collections.singletonList(target) , battleCommand, fcrm);
		
		if (attackAction.equalsIgnoreCase("Critical Attack")) {
			br.critted.set(0, true);
		}
		
		if (defenderAction.equalsIgnoreCase("Block")) {
			br.dodged.set(0, true);
		}
		
		attacker.setLocation(0, 0, 1, 1);
		if (attackAction.equalsIgnoreCase("Ranged Attack")) {
			br.critted.set(0, true);
			target.setLocation(0, 2, 1, 1);
		} else {
			target.setLocation(0, 1, 1, 1);
		}
		
		AttackCinematicState acs = TacticalGame.ENGINE_CONFIGURATIOR.getAttackCinematicState();
		acs.setBattleInfo(attacker, fcrm, br, (PaddedGameContainer) game.getContainer(), TacticalGame.STATE_GAME_BATTLE_ANIM_VIEW);
		game.enterState(TacticalGame.STATE_GAME_BATTLE_ANIM, new FadeOutTransition(Color.black, 250), new EmptyTransition());
	}
	
	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_BATTLE_ANIM_VIEW;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		super.init(container, game);
		this.game = game;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {		
		reloadButton.render(g);
		reloadScriptsButton.render(g);
		renderNew(g);
		g.drawString("Press escape to return to the main menu", 300, container.getHeight() - 50);
		
		if (currentList != null) {
			g.setColor(Color.lightGray);
			g.fillRect(300, 0, 300, 490);
			g.setColor(Color.black);
			g.drawString("Right click to cancel", 315, 465);
			g.drawString("Search (Press enter)", 315, 410);
			currentList.render(container, g);
			
			g.setColor(Color.white);
		}
		
		startButton.setX(400);
		startButton.setY(400);
		
	}
	
	private void renderNew(Graphics g) {
		g.setColor(Color.white);
		int y = 30;
		int textGap = 18;
		int boxGap = 80;
		int boxSize = 70;
		
		g.drawString("Environment", 30, y);
		y += textGap;					
		g.drawRect(30, y, 600, boxSize);
		y += boxGap;
		chooseBackground.render(g);

		g.drawString("Attacker", 30, y);
		y += textGap;
		g.drawRect(30, y, 800, boxSize);
		y += boxGap;
		chooseAttackerType.render(g);
		chooseAttackerCombatant.render(g);
		chooseAttackerAction.render(g);
		chooseAttackerWeapon.render(g);
		
		g.drawString("Defender", 30, y);
		y += textGap;
		g.drawRect(30, y, 800, boxSize);
		y += boxGap;
		chooseDefenderType.render(g);
		chooseDefenderCombatant.render(g);
		chooseDefenderAction.render(g);
		chooseDefenderWeapon.render(g);
		
		g.setColor(Color.red);
		g.drawString("" + backgroundIndex, 40, 90);
		g.drawString((attackerHero ? "Hero" : "Enemy"), 40, 188);
		if (attacker != null)
			g.drawString(attacker.getName(), 225, 188);
		if (attackAction != null)
			g.drawString(attackAction, 410, 188);
		if (attackerWeapon != null)
			g.drawString(attackerWeapon, 595, 188);
		
		g.drawString((targetHero ? "Hero" : "Enemy"), 40, 286);
		
		if (target != null)
			g.drawString(target.getName(), 225, 286);
		if (defenderAction != null)
			g.drawString(defenderAction, 410, 286);
		if (defenderWeapon != null)
			g.drawString(defenderWeapon, 595, 286);
		
		
		/*
		private Button chooseBackground = new Button(40, 60, 165, 25, "Background");
		
		private Button chooseAttackerType = new Button(40, 158, 165, 25, "Type");
		private Button chooseAttackerCombatant = new Button(225, 158, 165, 25, "Combatant");
		private Button chooseAttackerAction = new Button(410, 158, 165, 25, "Action");
		private Button chooseAttackerWeapon = new Button(595, 158, 165, 25, "Weapon");
		
		private Button chooseDefenderType = new Button(40, 256, 165, 25, "Type");
		private Button chooseDefenderCombatant = new Button(225, 256, 165, 25, "Combatant");
		private Button chooseDefenderAction = new Button(410, 256, 165, 25, "Action");
		private Button chooseDefenderWeapon = new Button(595, 256, 165, 25, "Weapon");
		*/
		startButton.render(g);
	}
	

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		TacticalGame.TEST_MODE_ENABLED = false;
		fcrm = resourceManager;
	}

	@Override
	public void initAfterLoad() {
				
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException {
		if (currentList != null)
			currentList.update(container, delta);
		
		int x = container.getInput().getMouseX();
		int y = container.getInput().getMouseY();
		
		if (container.getInput().isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
			currentList = null;
			wizardIndex = null;
		}
		
		boolean click = container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);
		if (nextInput > 0) {
			nextInput -= delta;
			click = false;
		}
		
		if (currentList == null) {
			if (reloadButton.handleUserInput(x, y, click)) {
				fcrm.reloadAnimations(TacticalGame.ENGINE_CONFIGURATIOR);
				nextInput = 200;
			}
			
			if (reloadScriptsButton.handleUserInput(x, y, click)) {
				TacticalGame.ENGINE_CONFIGURATIOR.initialize();
				nextInput = 200;
			}
			
			if (chooseBackground.handleUserInput(x, y, click)) {
				wizardIndex = WizardStep.PICK_BACKGROUND;
			} else if (chooseAttackerType.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_ATTACKER_TYPE;
			} else if (chooseAttackerCombatant.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_ATTACKER;
			} else if (chooseAttackerAction.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_ATTACK_ACTION;
			} else if (chooseAttackerWeapon.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_ATTACKER_WEAPON;
			} else if (chooseDefenderType.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_TARGET_TYPE;
			} else if (chooseDefenderCombatant.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_TARGET;
			} else if (chooseDefenderAction.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_DEFENDER_ACTION;
			} else if (chooseDefenderWeapon.handleUserInput(x, y, click) ) {
				wizardIndex = WizardStep.PICK_TARGET_WEAPON;
			} else if (startButton.handleUserInput(x, y, click)) {
				showBattleAction();
			}
			
			if (wizardIndex != null)
				setupStep();
		}
		
		if (container.getInput().isKeyDown(Input.KEY_ESCAPE))
			game.enterState(TacticalGame.STATE_GAME_MENU_DEVEL);
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
				
	}

	@Override
	protected Menu getPauseMenu() {
		return null;
	}

	@Override
	public void exceptionInState() {
				
	}
}
