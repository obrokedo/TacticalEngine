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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return yesNoMenuRenderer;
	}

	@Override
	public SpellFactory getSpellFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BattleEffectFactory getBattleEffectFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CinematicActorConfiguration getCinematicActorConfiguration() {
		// TODO Auto-generated method stub
		return cinematicActorConfiguration;
	}

	@Override
	public BattleFunctionConfiguration getBattleFunctionConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PanelRenderer getPanelRenderer() {
		return panelRenderer;
	}

	@Override
	public MusicConfiguration getMusicConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LevelProgressionConfiguration getLevelProgression() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EngineConfigurationValues getConfigurationValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuConfiguration getMenuConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
		

	@Override
	public LoadingScreenRenderer getLogoLoadScreenRenderer(GameContainer container) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoadingScreenRenderer getFirstLoadScreenRenderer(GameContainer container, Music mainMenuMusic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoadingScreenRenderer getLoadScreenRenderer(GameContainer container) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttackCinematicState getAttackCinematicState() {
		return attackCinState;
	}
	
	@Override
	public BattleStatisticConfigration getBattleStatisticsConfiguration() {
		return battleStatsConfig;
	}
}
