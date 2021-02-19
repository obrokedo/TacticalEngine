package tactical.engine.config.provided;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Music;

import tactical.engine.config.BattleEffectFactory;
import tactical.engine.config.BattleFunctionConfiguration;
import tactical.engine.config.BattleStatisticConfigration;
import tactical.engine.config.CinematicActorConfiguration;
import tactical.engine.config.DfAnimationParser;
import tactical.engine.config.EngineConfigurationValues;
import tactical.engine.config.EngineConfigurator;
import tactical.engine.config.HealthPanelRenderer;
import tactical.engine.config.LevelProgressionConfiguration;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.config.MusicConfiguration;
import tactical.engine.config.PanelRenderer;
import tactical.engine.config.SpellFactory;
import tactical.engine.config.SpellMenuRenderer;
import tactical.engine.config.YesNoMenuRenderer;
import tactical.engine.config.intr.AnimationParser;
import tactical.engine.state.AttackCinematicState;
import tactical.engine.state.DefaultAttackCinematicState;
import tactical.engine.state.StateInfo;
import tactical.game.menu.DefaultHeroStatMenu;
import tactical.game.menu.Menu;
import tactical.game.sprite.CombatSprite;
import tactical.loading.LoadingScreenRenderer;

public class DefaultEngineConfiguration implements EngineConfigurator {
	
	protected DfAnimationParser animationParser = new DfAnimationParser();
	protected HealthPanelRenderer healthPanelRenderer = new DefaultHealthPanelRenderer();
	protected SpellMenuRenderer spellMenuRenderer = new DefaultSpellMenuRenderer();
	protected YesNoMenuRenderer yesNoMenuRenderer = new DefaultYesNoRenderer();
	protected CinematicActorConfiguration cinematicActorConfiguration = 
			new DefaultCinematicActorConfiguration();
	protected AttackCinematicState attackCinState = new DefaultAttackCinematicState();
	protected PanelRenderer panelRenderer = new DefaultPanelRenderer();
	protected BattleStatisticConfigration battleStatsConfig = new DefaultBattleStatisticsConfiguration();
	
	@Override
	public void getAttackCinematic() {
			}

	@Override
	public AnimationParser getAnimationParser() {
		return animationParser;
	}

	@Override
	public HealthPanelRenderer getHealthPanelRenderer() {
		return healthPanelRenderer;
	}

	@Override
	public SpellMenuRenderer getSpellMenuRenderer() {
		return spellMenuRenderer;
	}
	
	@Override
	public YesNoMenuRenderer getYesNoMenuRenderer() {
				return yesNoMenuRenderer;
	}

	@Override
	public SpellFactory getSpellFactory() {
				return null;
	}

	@Override
	public BattleEffectFactory getBattleEffectFactory() {
				return null;
	}

	@Override
	public CinematicActorConfiguration getCinematicActorConfiguration() {
				return cinematicActorConfiguration;
	}

	@Override
	public BattleFunctionConfiguration getBattleFunctionConfiguration() {
				return null;
	}

	@Override
	public PanelRenderer getPanelRenderer() {
		return panelRenderer;
	}

	@Override
	public MusicConfiguration getMusicConfiguration() {
				return null;
	}

	@Override
	public LevelProgressionConfiguration getLevelProgression() {
				return null;
	}

	@Override
	public EngineConfigurationValues getConfigurationValues() {
				return null;
	}

	@Override
	public MenuConfiguration getMenuConfiguration() {
				return null;
	}
		

	@Override
	public LoadingScreenRenderer getLogoLoadScreenRenderer(GameContainer container) {
				return null;
	}

	@Override
	public LoadingScreenRenderer getFirstLoadScreenRenderer(GameContainer container, Music mainMenuMusic) {
				return null;
	}

	@Override
	public LoadingScreenRenderer getLoadScreenRenderer(GameContainer container) {
				return null;
	}

	@Override
	public void initialize() {
				
	}

	@Override
	public AttackCinematicState getAttackCinematicState() {
		return attackCinState;
	}
	
	@Override
	public BattleStatisticConfigration getBattleStatisticsConfiguration() {
		return battleStatsConfig;
	}

	@Override
	public Menu getHeroStatMenu(GameContainer gc, CombatSprite selectedSprite, StateInfo stateInfo) {
		return new DefaultHeroStatMenu(gc, selectedSprite, stateInfo);
	}
}
