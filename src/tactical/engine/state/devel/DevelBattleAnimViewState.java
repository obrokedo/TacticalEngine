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
import tactical.game.menu.Menu;
import tactical.game.resource.EnemyResource;
import tactical.game.resource.HeroResource;
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
	private boolean attackerHero, targetHero;
	private ResourceManager fcrm;
	private StateBasedGame game;
	private Button backButton = new Button(400, 15, 165, 25, "Previous Step");
	private Button reloadButton = new Button(400, 50, 165, 25, "Reload Animations");
	private Button reloadScriptsButton = new Button(400, 85, 165, 25, "Reload Scripts");
	private int nextInput = 0;
	
	private String attackAction;
	
	public DevelBattleAnimViewState() {
		wizardIndex = null;
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		
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
					
			}
			if (options.size() > 0) {
				currentList = new ListUI(selectText, 14, options);
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
		GO
	}

	@Override
	public boolean resourceSelected(String selectedItem, ListUI parentSelector) {
		switch (wizardIndex) {
			case PICK_ATTACKER_TYPE:
				attackerHero = selectedItem.equalsIgnoreCase("Hero");
				nextStep();
				break;
			case PICK_TARGET_TYPE:
				targetHero = selectedItem.equalsIgnoreCase("Hero");
				nextStep();
				break;
			case PICK_ATTACKER:
				if (attackerHero)
					attacker = HeroResource.getHero(selectedItem);
				else 
					attacker = EnemyResource.getEnemy(selectedItem);
				
				nextStep();
				break;
			case PICK_TARGET:
				if (targetHero)
					target = HeroResource.getHero(selectedItem);
				else 
					target = EnemyResource.getEnemy(selectedItem);
				
				nextStep();
				break;
			case PICK_ATTACK_ACTION:
				attackAction = selectedItem;
				nextStep();
				break;
			case PICK_DEFENDER_ACTION:
				handlePickTargetAction(selectedItem);
				break;
			default:
				break;
		}
		return false;
	}
	
	private void handlePickTargetAction(String selectedItem) {
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
		
		target.setCurrentHP(1);
		
		attacker.initializeStats();
		target.initializeStats();
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
		
		if (selectedItem.equalsIgnoreCase("Block")) {
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
	
	private void nextStep() {
		this.wizardIndex = WizardStep.values()[wizardIndex.ordinal() + 1];
		this.setupStep();
	}
	
	private void backStep() {
		TacticalGame.ENGINE_CONFIGURATIOR.initialize();
		
		if (wizardIndex.ordinal() != 0) {
			this.wizardIndex = WizardStep.values()[wizardIndex.ordinal() - 1];
			this.setupStep();
		}
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_BATTLE_ANIM_VIEW;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		this.game = game;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		if (currentList != null) {
			currentList.render(g);
		}
		backButton.render(g);
		reloadButton.render(g);
		reloadScriptsButton.render(g);
		g.drawString("Press escape to return to the main menu", 300, container.getHeight() - 50);
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {
		if (wizardIndex == null) {
			wizardIndex = WizardStep.PICK_ATTACKER_TYPE;
			setupStep();
		}
		TacticalGame.TEST_MODE_ENABLED = false;
		fcrm = resourceManager;
	}

	@Override
	public void initAfterLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta) throws SlickException {
		if (currentList != null)
			currentList.update(container, delta);
		if (container.getInput().isKeyDown(Input.KEY_BACK)) {
			backStep();
		}
		
		int x = container.getInput().getMouseX();
		int y = container.getInput().getMouseY();
		boolean click = container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);
		if (nextInput > 0) {
			nextInput -= delta;
			click = false;
		}
		if (backButton.handleUserInput(x, y, click)) {
			backStep();
			nextInput = 200;
		}
		if (reloadButton.handleUserInput(x, y, click)) {
			fcrm.reloadAnimations(TacticalGame.ENGINE_CONFIGURATIOR);
			nextInput = 200;
		}
		
		if (reloadScriptsButton.handleUserInput(x, y, click)) {
			TacticalGame.ENGINE_CONFIGURATIOR.initialize();
			nextInput = 200;
		}
		
		if (container.getInput().isKeyDown(Input.KEY_ESCAPE))
			game.enterState(TacticalGame.STATE_GAME_MENU_DEVEL);
	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) {
		// TODO Auto-generated method stub		
	}

	@Override
	protected Menu getPauseMenu() {
		return null;
	}

	@Override
	public void exceptionInState() {
		// TODO Auto-generated method stub
		
	}
	
	
}
