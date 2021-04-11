package tactical.engine.state;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import tactical.engine.TacticalGame;
import tactical.engine.config.MusicConfiguration;
import tactical.engine.config.ParticleEmitterConfiguration;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.BattleResults;
import tactical.game.battle.BattleSceneCreator;
import tactical.game.battle.command.BattleCommand;
import tactical.game.combat.CombatAnimation;
import tactical.game.combat.DamagedCombatAnimation;
import tactical.game.combat.StandCombatAnimation;
import tactical.game.exception.BadMapException;
import tactical.game.hudmenu.Panel;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.hudmenu.SpriteContextPanel;
import tactical.game.input.UserInput;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item.ItemDurability;
import tactical.game.manager.SoundManager;
import tactical.game.menu.Menu;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.menu.PauseMenu;
import tactical.game.menu.SpeechMenu;
import tactical.game.sprite.CombatSprite;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.ResourceManager;
import tactical.particle.AnimatedParticleSystem;
import tactical.utils.AnimationWrapper;
import tactical.utils.StringUtils;

public class DefaultAttackCinematicState extends AttackCinematicState implements MusicListener
{
	private MusicConfiguration musicSelector = null;

	private ArrayList<CombatAnimation> heroCombatAnimations;
	private CombatAnimation heroCombatAnim;
	private Panel heroHealthPanel = null;

	private ArrayList<CombatAnimation> enemyCombatAnimations;
	private CombatAnimation enemyCombatAnim;
	private Panel enemyHealthPanel= null;

	private ArrayList<String> textToDisplay;

	private SpeechMenu textMenu;
	private UserInput input;
	private boolean consumedText = false;
	private PaddedGameContainer gc;
	private int bgXPos, bgYPos, combatAnimationYOffset;
	private Image backgroundImage;
	private BattleResults battleResults;
	private CombatSprite attacker;
	private ResourceManager frm;

	// Spell stuff
	private boolean spellStarted = false;
	private boolean drawingSpell = false;
	private int drawingSpellCounter = 0;
	private Color spellFlash = null;
	private AnimationWrapper spellAnimation;
	private boolean spellTargetsHeroes;
	private int spellOverlayFadeIn = 0;
	private int SPELL_OVERLAY_MAX_ALPHA = 80;
	private Color spellOverlayColor = null;
	private AnimatedParticleSystem animatedParticleSystem;
	private int clipBackgroundWidth = 0;

	// The amount that the background has been scaled to fit the screen,
	// other animations should be scaled up accordingly
	private int backgroundScale;

	private Music music;
	private Music introMusic;
	public static final int SPELL_FLASH_DURATION = 480;
	public static String DEFAULT_FLOOR_IMAGE = "attackplatform";
	
	public void setBattleInfo(CombatSprite attacker, ResourceManager frm,
			BattleResults battleResults, PaddedGameContainer gc, int exitState) {
		setBattleInfo(attacker, frm, battleResults, gc);
		this.exitState = exitState;
	}

	public void setBattleInfo(CombatSprite attacker, ResourceManager frm,
			BattleResults battleResults, PaddedGameContainer gc)
	{
		this.gc = gc;
		this.battleResults = battleResults;
		this.attacker = attacker;
		this.frm = frm;

		input = new UserInput();
		gc.getInput().addKeyListener(input);
		heroCombatAnim = null;
		enemyCombatAnim = null;
		heroHealthPanel = null;
		enemyHealthPanel = null;

		drawingSpell = false;
		spellStarted = false;
		drawingSpellCounter = 0;
		spellOverlayFadeIn = 0;
		spellFlash = null;
		textMenu = null;

		// Initialize battle effects
		for (CombatSprite cs : battleResults.targets)
			cs.initializeBattleAttributes(frm);

		for (ArrayList<BattleEffect> effs : battleResults.targetEffects)
		{
			for (BattleEffect eff : effs)
				eff.initializeAnimation(frm);
		}

		attacker.initializeBattleAttributes(frm);

		boolean targetsAllies = battleResults.targets.get(0).isHero() == attacker.isHero();

		if (musicSelector == null)
		{
			musicSelector = TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration();
		}

		String mus = attacker.getCustomMusic(); 
		int musicVolume = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getBattleMusicVolume();
		if (StringUtils.isEmpty(mus))
			mus = musicSelector.getAttackMusic(attacker, targetsAllies);
		music = frm.getMusicByName(mus);
		// Why the fuck is there a _L postfix. I think it's to make
		// finding a leadup intro easier to find?
		if (frm.containsMusic(mus + "_L"))
			introMusic = frm.getMusicByName(mus + "_L");

		if (introMusic == null)
		{
			if (music != null)
				music.loop(1f, musicVolume / 100f * SoundManager.GLOBAL_VOLUME );
		}
		else
		{
			introMusic.addListener(this);
			introMusic.play(1f, musicVolume / 100f * SoundManager.GLOBAL_VOLUME);
		}

		SpriteSheet battleBGSS = frm.getSpriteSheet("battlebg");
		
		// Check to see if there is a custom background specified on the map
		// if so, load the image
		Image bgIm = null;
		int battleBackgroundIndex = 0;
		if (frm.getMap().isCustomBackground()) {
			battleBackgroundIndex = frm.getMap().getBackgroundImageIndex();

		} else {			
			String terrainType = frm.getMap().getTerrainTypeByTile(attacker.getTileX(), attacker.getTileY());
			if (terrainType == null) {
				throw new BadMapException("Location " + attacker.getTileX() + " " + attacker.getTileY() + " has either no terrain type or a "
						+ "bad terrain type assigned to it and\n so the battle background image can not be determined");
			}
			battleBackgroundIndex = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().
					getBattleBackgroundImageIndexByTerrainType(terrainType);
		}
		
		bgIm = battleBGSS.getSprite(battleBackgroundIndex % battleBGSS.getHorizontalCount(),
				battleBackgroundIndex / battleBGSS.getHorizontalCount());
		backgroundScale = (int) (PaddedGameContainer.GAME_SCREEN_SIZE.width / (float) bgIm.getWidth());
		
		clipBackgroundWidth = (PaddedGameContainer.GAME_SCREEN_SIZE.width - (bgIm.getWidth() * backgroundScale)) / 2;
		
		backgroundImage = bgIm.getScaledCopy(backgroundScale);

		bgXPos = 0;
		bgYPos = (PaddedGameContainer.GAME_SCREEN_SIZE.height - backgroundImage.getHeight()) / 2;
		combatAnimationYOffset = bgYPos + backgroundImage.getHeight();
		
		/*****************************/
		/** Setup battle animations **/
		/*****************************/
		if (battleResults.battleCommand.getSpell() != null)
		{
			spellAnimation = new AnimationWrapper(frm.getSpriteAnimation(
					battleResults.battleCommand.getSpell().getSpellAnimationFile(battleResults.battleCommand.getLevel())));
			
			// Get the correct animation from the animation set
			String animationString = Integer.toString(battleResults.battleCommand.getLevel());
			boolean loops = battleResults.battleCommand.getSpell().isLoops();
			if (spellAnimation.hasAnimation(animationString))
				spellAnimation.setAnimation(animationString, loops);
			else
				spellAnimation.setAnimation("1", loops);
				
			spellOverlayColor = battleResults.battleCommand.getSpell().getSpellOverlayColor(battleResults.battleCommand.getLevel());
			spellTargetsHeroes = battleResults.targets.get(0).isHero();
			
			String rainFile = battleResults.battleCommand.getSpell().getSpellRainAnimationFile(battleResults.battleCommand.getLevel());
			if (rainFile != null)
			{
				/*rainParticleSystem.addEmitter(new RainEmitter(
						180, 
						battleResults.battleCommand.getSpell().getSpellRainFrequency(battleResults.battleCommand.getLevel()), 
								!battleResults.targets.get(0).isHero()));
								*/
				// Image im = frm.getImage(rainFile);
				String rainAnimation =  battleResults.battleCommand.getSpell().getSpellRainAnimationName(battleResults.battleCommand.getLevel());
				animatedParticleSystem = new AnimatedParticleSystem(rainFile, rainAnimation, frm, backgroundScale);
				ParticleEmitterConfiguration emitter = battleResults.battleCommand.getSpell().getEmitter(battleResults.battleCommand.getLevel());
				emitter.initialize(battleResults.targets.get(0).isHero());
				emitter.setFcResourceManager(frm);
				animatedParticleSystem.addEmitter(emitter);
			}
			else
				animatedParticleSystem = null;
		}
		else {
			spellAnimation = null;
			animatedParticleSystem = null;
		}
		
		BattleSceneCreator bsc = BattleSceneCreator.initializeBattleScene(attacker, frm, battleResults, gc, 
				targetsAllies, bgXPos, bgYPos, backgroundImage, backgroundScale);
		heroCombatAnimations = bsc.getHeroCombatAnimations();
		enemyCombatAnimations = bsc.getEnemyCombatAnimations();
		textToDisplay = bsc.getTextToDisplay();
		nextAction(null);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {

	}

	/**
	 * Initializes this state, this only gets called when coming
	 * from a loading state
	 */
	@Override
	public void initAfterLoad() {

	}

	@Override
	public void doRender(PaddedGameContainer container, StateBasedGame game, Graphics g) 
	{
		if (spellFlash != null)
		{
			g.setColor(spellFlash);
			g.fillRect(0, 0, PaddedGameContainer.GAME_SCREEN_SIZE.width, PaddedGameContainer.GAME_SCREEN_SIZE.height);
		}
		
		setCinematicClip(container, g);
		g.drawImage(backgroundImage, bgXPos, bgYPos);
		if (heroCombatAnim != null)
			heroCombatAnim.render(gc, g, combatAnimationYOffset, backgroundScale);
		if (enemyCombatAnim != null)
			enemyCombatAnim.render(gc, g, combatAnimationYOffset, backgroundScale);
		if (drawingSpell && spellFlash == null)
		{
			if (spellTargetsHeroes)
				spellAnimation.drawAnimation(184 * backgroundScale,
					combatAnimationYOffset - 30 / backgroundScale, g);
			else
				spellAnimation.drawAnimation(76 * backgroundScale,
					combatAnimationYOffset - 37 / backgroundScale, g);
		}
		
		clearCinematicClip(g);
		if (textMenu != null)
			textMenu.render(gc, g);

		if (heroHealthPanel != null)
			heroHealthPanel.render(gc, g);
		if (enemyHealthPanel != null)
			enemyHealthPanel.render(gc, g);
		
		setCinematicClip(container, g);
		if (drawingSpell && spellFlash == null)
		{
			if (animatedParticleSystem != null)
				animatedParticleSystem.render();	
		}
		clearCinematicClip(g);
		
		// Draw the spell overlay fade in
		if (spellOverlayFadeIn > 0)
		{
			g.setColor(spellOverlayColor);
			g.fillRect(0, 0, PaddedGameContainer.GAME_SCREEN_SIZE.width, PaddedGameContainer.GAME_SCREEN_SIZE.height);
		}

		// If dev mode is enabled and we're at the end of the cinematic
		// then display the text to indicate the animation can be
		// restarted
		if (TacticalGame.DEV_MODE_ENABLED && heroCombatAnimations.size() == 0)
		{
			g.setColor(Color.white);
			StringUtils.drawString("Press R to restart attack cinematic", 20, 40, g);
		}
	}

	private void clearCinematicClip(Graphics g) {
		g.clearClip();
		g.translate(-clipBackgroundWidth, 0);
	}

	private void setCinematicClip(PaddedGameContainer container, Graphics g) {
		g.clearClip();
		g.setClip(clipBackgroundWidth * PaddedGameContainer.GAME_SCREEN_SCALE, 0, container.getWidth() - 
				(2 * clipBackgroundWidth) * PaddedGameContainer.GAME_SCREEN_SCALE, container.getHeight());
		g.translate(clipBackgroundWidth, 0);
	}

	@Override
	public void doUpdate(PaddedGameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		// If this is test mode then we want to speed
		// up the game
		if (TacticalGame.TEST_MODE_ENABLED)
			delta *= TacticalGame.getTestMultiplier();

		// Update the input so that released keys are realized
		input.update(delta, container.getInput());

		// Whether the current animation says that
		// a spell should be rendered
		boolean currentAnimDrawSpell = false;

		// Whether the heroes/enemy current animation has finished yet
		// In other words whether the hero/enemy is ready to move to the next step
		// This is set to true initially so that if there is no current hero/enemy
		// animation it won't block
		boolean heroAnimationFinished = true;
		boolean enemyAnimationFinished = true;

		// If there is a hero combat animation check to see if it has
		// completed and whether the current animation should be drawn
		// with a spell
		if (heroCombatAnim != null)
		{
			heroAnimationFinished = heroCombatAnim.update(delta);
			if (heroCombatAnim.isDrawSpell())
			{
				currentAnimDrawSpell = true;
			}
		}
		
		// If there is a enemy combat animation check to see if it has
		// completed and whether the current animation should be drawn
		// with a spell
		if (enemyCombatAnim != null)
		{
			enemyAnimationFinished = enemyCombatAnim.update(delta);

			if (enemyCombatAnim.isDrawSpell())
			{
				currentAnimDrawSpell = true;
			}
		}

		// If the current animation says a spell should be drawn,
		// but the attack cinematic has not started drawing a spell
		// yet then we need to start drawing the "casting" of the spell
		// which is a flash and play the cast sound
		if (currentAnimDrawSpell && !spellStarted)
		{
			drawingSpell = true;
			spellStarted = true;
			spellOverlayFadeIn = 0;

			String sound = musicSelector.getCastSpellSoundEffect(attacker.isHero(), battleResults.battleCommand.getSpell().getName());

			if (sound != null)
			{
				Sound snd = frm.getSoundByName(sound);
				snd.play(1f, SoundManager.GLOBAL_VOLUME);
			}
		}
		// If we have started a spell but the current animation says no spell should
		// be displayed then we toggle off the drawing spell flag
		else if (spellStarted && !currentAnimDrawSpell)
		{
			drawingSpell = false;
		}

		// Check for whether we are in development mode, if so then the animation should be able to be re-run
		// but not actually effect anything on subsequent times through
		if (TacticalGame.DEV_MODE_ENABLED && heroCombatAnimations.size() == 0 && input.isKeyDown(Input.KEY_R))
		{
				for (int i = 0; i < battleResults.hpDamage.size(); i++)
					battleResults.hpDamage.set(i, 0);
				for (int i = 0; i < battleResults.mpDamage.size(); i++)
					battleResults.mpDamage.set(i, 0);
				for (int i = 0; i < battleResults.attackerHPDamage.size(); i++)
					battleResults.attackerHPDamage.set(i, 0);
				for (int i = 0; i < battleResults.attackerMPDamage.size(); i++)
					battleResults.attackerMPDamage.set(i, 0);
				battleResults.levelUpResult = null;

				// Restart this animation
				setBattleInfo(attacker, frm,
						battleResults, gc, exitState);
		}

		// Check to see if there is a text menu that is currently displayed
		// if so we want to handle the user input and potentially
		// drive the next action or level up a hero
		if (textMenu != null)
		{
			textMenu.handleUserInput(input, null);
			MenuUpdate update = textMenu.update(delta, null);

			if (update == MenuUpdate.MENU_CLOSE)
			{
				textMenu = null;
				nextAction(game);
			}
			else if (update == MenuUpdate.MENU_NEXT_ACTION)
			{
				if (battleResults.levelUpResult != null)
				{
					if (attacker.isHero())
						attacker.getHeroProgression().levelUp(attacker, battleResults.levelUpResult);
					else
						battleResults.targets.get(0).getHeroProgression().levelUp(battleResults.targets.get(0), battleResults.levelUpResult);
					String sound = musicSelector.getLevelUpSoundEffect(attacker);
					if (sound != null)
						frm.getSoundByName(sound).play();
				}
			}
		}
		// If there is no text menu then we don't transition to the
		// next animations until both the hero's and enemy's current
		// animation has finished
		else if (heroAnimationFinished && enemyAnimationFinished)
		{
			// Text appears after the animations at the same index
			// have completed. If we haven't yet checked to see if there
			// should be text after this action do that now and
			// potentially display a text box. Once that text box has been
			// viewed consumedText = true and we'll move onto the next action
			// If there was no text box to display then we'll do an additional update
			// and consumedText = true so we'll get the next action
			if (!consumedText)
			{
				consumedText = true;
				String text = textToDisplay.remove(0);
				if (text != null)
				{
					textMenu = new SpeechMenu(text, gc);
				} else {
					nextAction(game);
				}
			}
			else
			{
				nextAction(game);
			}
		}

		// If we're currently drawing a spell then we want to
		// update the spells animation
		if (drawingSpell)
			spellUpdate(delta);
		else if (spellOverlayFadeIn > 0)
		{
			spellOverlayFadeIn -= 2;
			spellOverlayColor.a = spellOverlayFadeIn / 255.0f;
		}
	}

	/**
	 * Updates spell flashing, spell animations and the spell overlay.
	 * Additionally handles playing the "After spell flash sound effect"
	 *
	 * @param delta the amount of time that has passed since the previous update
	 */
	private void spellUpdate(int delta)
	{
		// Check to see if we are still flashing,
		// if so then toggle the color based on the time
		// elapse
		if (drawingSpellCounter < SPELL_FLASH_DURATION)
		{
			if (drawingSpellCounter % 160 < 80)
				spellFlash = Color.white;
			else
				spellFlash = Color.darkGray;
			drawingSpellCounter += delta;
		}
		// Otherwise we are done flashing and
		// the actual spell should begin rendering
		else
		{
			// If the flashing just finished then play the "After spell flash sound effect"
			// and set the spell flash to null so that we don't play the sound again
			if (spellFlash != null)
			{
				String sound = musicSelector.getAfterSpellFlashSoundEffect(attacker.isHero(), battleResults.battleCommand.getSpell().getName());
				if (sound != null)
					frm.getSoundByName(sound).play(1f, SoundManager.GLOBAL_VOLUME);
				spellFlash = null;
			}

			// If the overlay is not yet at full alpha then increment it
			// so that it will fade in
			if (spellOverlayFadeIn < SPELL_OVERLAY_MAX_ALPHA)
			{
				spellOverlayFadeIn += 2;
				spellOverlayColor.a = spellOverlayFadeIn / 255.0f;
			}

			// Update the spell animation as it should
			// be rendering at this point
			spellAnimation.update(delta);
			if (animatedParticleSystem != null)
				animatedParticleSystem.update(delta);
		}
	}

	private void nextAction(StateBasedGame game)
	{
		consumedText = false;
		DamagedCombatAnimation damagedSprite = null;

		if (heroCombatAnimations.size() > 0)
		{
			CombatAnimation hero = heroCombatAnimations.remove(0);
			if (hero != null)
			{
				if (hero.getParentSprite() == null)
					heroHealthPanel = null;
				else
					heroHealthPanel = new SpriteContextPanel(
							PanelType.PANEL_HEALTH_BAR,
							hero.getParentSprite(), 
							TacticalGame.ENGINE_CONFIGURATIOR.getHealthPanelRenderer(),
							frm, gc);
				hero.initialize();
				if (hero instanceof StandCombatAnimation && heroCombatAnim instanceof StandCombatAnimation)
					((StandCombatAnimation) hero).continueStandAnimation(heroCombatAnim);
				heroCombatAnim = hero;
				// TODO I'M NOT SURE IF I LIKE THIS HERE MORE OR IN THE DAMAGEDCOMBATANIMATION
				if (heroCombatAnim.isDamaging()) damagedSprite = (DamagedCombatAnimation) heroCombatAnim;
			}

			CombatAnimation enemy = enemyCombatAnimations.remove(0);
			if (enemy != null)
			{
				if (enemy.getParentSprite() == null)
					enemyHealthPanel = null;
				else
					enemyHealthPanel = new SpriteContextPanel(
							PanelType.PANEL_TARGET_HEALTH_BAR,
							enemy.getParentSprite(), TacticalGame.ENGINE_CONFIGURATIOR.getHealthPanelRenderer(), 
							frm, gc);
				enemy.initialize();
				if (enemy instanceof StandCombatAnimation && enemyCombatAnim instanceof StandCombatAnimation)
					((StandCombatAnimation) enemy).continueStandAnimation(enemyCombatAnim);
				enemyCombatAnim = enemy;


				// TODO I'M NOT SURE IF I LIKE THIS HERE MORE OR IN THE DAMAGEDCOMBATANIMATION
				if (enemyCombatAnim.isDamaging()) damagedSprite = (DamagedCombatAnimation) enemyCombatAnim;
			}

			if (damagedSprite != null)
			{
				attacker.modifyCurrentHP(battleResults.attackerHPDamage.get(damagedSprite.getBattleResultIndex()));
				attacker.modifyCurrentMP(battleResults.attackerMPDamage.get(damagedSprite.getBattleResultIndex()));

				// Check to see if this is a counter attack situation and the "attacker"
				// is actually the target
				if (battleResults.countered && damagedSprite.getParentSprite() == attacker)
				{
					playOnHitSound(battleResults.targets.get(0), attacker.isHero() == damagedSprite.getParentSprite().isHero(),
							damagedSprite.getBattleResultIndex());
				}
				else
					playOnHitSound(attacker, attacker.isHero() == damagedSprite.getParentSprite().isHero(),
						damagedSprite.getBattleResultIndex());
			}
		}
		// EXIT
		else
		{
			// Check to see if an item was broken, if so unequip it
			if (battleResults.itemDamaged) {
				unequipIfBroken(attacker.getEquippedWeapon());
				unequipIfBroken(attacker.getEquippedRing());
				unequipIfBroken(attacker.getEquippedArmor());
			}
			
			if (music != null)
				music.stop();
			if (introMusic != null)
				introMusic.stop();
			gc.getInput().removeAllKeyListeners();
			exitToNextState(game, new FadeOutTransition(Color.black, 250), new EmptyTransition());
		}
	}	
	
	private void unequipIfBroken(EquippableItem item) {
		if (item != null && item.getDurability() == ItemDurability.BROKEN) {
			attacker.unequipItem(item);
		}
	}

	private void playOnHitSound(CombatSprite actionAttacker, boolean targetsAreAllies, int index)
	{
		String sound = null;
		if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_ATTACK)
		{
			if (actionAttacker.getEquippedWeapon() != null)
				sound = musicSelector.getAttackHitSoundEffect(actionAttacker.isHero(), battleResults.critted.get(index),
						battleResults.dodged.get(index), actionAttacker.getEquippedWeapon().getItemStyle());
			else
				sound = musicSelector.getAttackHitSoundEffect(actionAttacker.isHero(), battleResults.critted.get(index),
						battleResults.dodged.get(index), -1);
		}
		else if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_ITEM)
			sound = musicSelector.getUseItemSoundEffect(actionAttacker.isHero(), targetsAreAllies, battleResults.battleCommand.getItem().getName());
		else if (battleResults.battleCommand.getCommand() == BattleCommand.COMMAND_SPELL)
			sound = musicSelector.getSpellHitSoundEffect(actionAttacker.isHero(), targetsAreAllies, battleResults.battleCommand.getSpell().getName());

		if (sound != null)
		{
			Sound snd = frm.getSoundByName(sound);
			snd.play(1f, SoundManager.GLOBAL_VOLUME);
		}
	}

	@Override
	public void stateLoaded(ResourceManager resourceManager) {

	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_BATTLE_ANIM;
	}

	@Override
	public void musicEnded(Music music) {
		music.removeListener(this);
		this.music.loop(1f, TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getBattleMusicVolume() / 100f * SoundManager.GLOBAL_VOLUME);
	}

	@Override
	public void musicSwapped(Music music, Music newMusic) {

	}
	
	@Override
	protected Menu getPauseMenu() {
		if (this.music != null)
			this.music.pause();
		return new PauseMenu(null);
	}

	@Override
	protected void pauseMenuClosed() {
		super.pauseMenuClosed();
		if (this.music != null)
			this.music.resume();
	}

	@Override
	public void exceptionInState() {
		
		
	}
}
