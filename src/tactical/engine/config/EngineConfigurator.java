package tactical.engine.config;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Music;

import tactical.engine.config.intr.AnimationParser;
import tactical.engine.state.AttackCinematicState;
import tactical.engine.state.StateInfo;
import tactical.game.menu.Menu;
import tactical.game.sprite.CombatSprite;
import tactical.loading.LoadingScreenRenderer;

public interface EngineConfigurator {
	public void initialize();
	
	public void getAttackCinematic();
	
	public AnimationParser getAnimationParser();
	
	public HealthPanelRenderer getHealthPanelRenderer();
	
	public SpellMenuRenderer getSpellMenuRenderer();
	
	public SpellFactory getSpellFactory();
	
	public BattleEffectFactory getBattleEffectFactory();
	
	public CinematicActorConfiguration getCinematicActorConfiguration();
	
	public BattleFunctionConfiguration getBattleFunctionConfiguration();
	
	public PanelRenderer getPanelRenderer();
	
	public MusicConfiguration getMusicConfiguration();

	public LevelProgressionConfiguration getLevelProgression();

	public EngineConfigurationValues getConfigurationValues();

	public MenuConfiguration getMenuConfiguration();
	
	public YesNoMenuRenderer getYesNoMenuRenderer();
	
	public LoadingScreenRenderer getLogoLoadScreenRenderer(GameContainer container);
	
	public LoadingScreenRenderer getFirstLoadScreenRenderer(GameContainer container, Music mainMenuMusic);
	
	public LoadingScreenRenderer getLoadScreenRenderer(GameContainer container);
	
	public AttackCinematicState getAttackCinematicState();
	
	public BattleStatisticConfigration getBattleStatisticsConfiguration(); 
	
	public Menu getHeroStatMenu(GameContainer gc, CombatSprite selectedSprite, StateInfo stateInfo);
}
