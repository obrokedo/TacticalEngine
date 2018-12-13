package tactical.engine.state;

import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.Transition;

import tactical.engine.TacticalGame;
import tactical.game.battle.BattleResults;
import tactical.game.menu.Menu;
import tactical.game.menu.PauseMenu;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.LoadableGameState;
import tactical.loading.ResourceManager;

public abstract class AttackCinematicState extends LoadableGameState {
	
	protected int exitState;
	
	public void setBattleInfo(CombatSprite attacker, ResourceManager frm,
			BattleResults battleResults, PaddedGameContainer gc, int exitState) {
		setBattleInfo(attacker, frm, battleResults, gc, TacticalGame.STATE_GAME_BATTLE);
		this.exitState = exitState;
	}
	
	

	protected abstract void setBattleInfo(CombatSprite attacker, ResourceManager frm,
			BattleResults battleResults, PaddedGameContainer gc);

	/**
	 * Ends the attack cinematic and transitions to the next game state. This should always be called instead of manually
	 * transitioning out so that development tools work as expected
	 * 
	 * @param game
	 * @param transitionOut
	 * @param transitionIn
	 */
	public void exitToNextState(StateBasedGame game, Transition transitionOut, Transition  transitionIn) {
		game.enterState(exitState, transitionOut, transitionIn);
	}
	
	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_BATTLE_ANIM;
	}
	
	@Override
	protected Menu getPauseMenu() {
		return new PauseMenu(null);
	}

	@Override
	protected void pauseMenuClosed() {
		super.pauseMenuClosed();
	}
}
