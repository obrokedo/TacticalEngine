package tactical.engine.state.devel;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.particles.ParticleSystem;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import tactical.engine.TacticalGame;
import tactical.game.battle.spell.SpellDefinition;
import tactical.game.ui.ListUI;
import tactical.game.ui.ListUI.ResourceSelectorListener;
import tactical.game.ui.ResourceSelector;
import tactical.loading.ResourceManager;
import tactical.utils.AnimationWrapper;

public class DevelAnimationViewState extends BasicGameState implements ResourceSelectorListener
{
	private ResourceSelector animationFileSelector, weaponSelector; //, spellSelector;
	private ListUI animationSelector;
	private ResourceManager frm;
	private int updateDelta;
	private AnimationWrapper currentAnimation;
	private Rectangle playRect = new Rectangle(350, 675, 100, 30);
	private Rectangle loopRect = new Rectangle(500, 675, 100, 30);
	private int drawX, drawY;
	private static final Color BG_COLOR = new Color(172, 205, 183);
	private SpellDefinition spell = null;
	private ParticleSystem rainParticleSystem = null;
	private GameContainer container = null;

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {		
		this.container = container;
		//spellSelector = new ResourceSelector("Spells",  0,  false, "scripts/Spellz", ".py", container);
		//spellSelector.setListener(this);
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		super.enter(container, game);
		container.getInput().removeAllListeners();
		this.animationSelector = null;
		animationFileSelector = new ResourceSelector("Animations", 0, true, ResourceManager.ANIMATIONS_FOLDER, ResourceManager.ANIMATIONS_EXTENSION, container);
		animationFileSelector.setListener(this);
		weaponSelector = new ResourceSelector(
				"Weapons", 300, true, ResourceManager.WEAPONS_FOLDER, ResourceManager.WEAPONS_EXTENSION, container);
		weaponSelector.addResourceFromDir(
				ResourceManager.WEAPONS_ANIMATIONS_FOLDER, ResourceManager.ANIMATIONS_EXTENSION, container);
		weaponSelector.setListener(this);
		
		frm = new ResourceManager();
		try {
			frm.addResource(ResourceManager.IMAGES_FOLDER_IDENTIFIER + "," + ResourceManager.WEAPONS_FOLDER, TacticalGame.ENGINE_CONFIGURATIOR);
			frm.reloadAnimations(TacticalGame.ENGINE_CONFIGURATIOR);			
			frm.addResource("spritedir,sprite", TacticalGame.ENGINE_CONFIGURATIOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, container.getWidth(), container.getHeight());
		if (currentAnimation != null && currentAnimation.getCurrentAnimation() != null)
		{
			g.setColor(Color.black);
			g.drawRect(playRect.getX() - 50, playRect.getY() + 100, playRect.getWidth(), playRect.getHeight());
			g.draw(playRect);
			g.draw(loopRect);
			g.drawString("Play", 380,  680);
			g.drawString("Loop", 530,  680);
			g.scale(2, 2);
			currentAnimation.drawAnimationIgnoreOffset(drawX, drawY, g);
		}
		
		g.translate(100, 100);
		
		if (rainParticleSystem != null)
			rainParticleSystem.render();
		g.resetTransform();
		
		g.setColor(Color.black);
		animationFileSelector.render(container, g);
		g.setColor(Color.black);
		weaponSelector.render(container, g);
		g.setColor(Color.black);
		//spellSelector.render(g);

		g.setColor(Color.black);
		if (animationSelector != null)
			animationSelector.render(container, g);

		g.setColor(Color.black);

		g.drawString("Escape - Dev Menu", container.getWidth() - 250, container.getHeight() - 30);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if (currentAnimation != null && currentAnimation.getCurrentAnimation() != null)
		{
			currentAnimation.update(delta);
		}

		if (updateDelta > 0)
			updateDelta = Math.max(0, updateDelta - delta);
		else if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
		{
			this.updateDelta = 200;

			int x = container.getInput().getMouseX();
			int y = container.getInput().getMouseY();

			if (currentAnimation != null && currentAnimation.getCurrentAnimation() != null)
			{
				if (playRect.contains(x, y))
					currentAnimation.resetCurrentAnimation();
				if (loopRect.contains(x, y))
					currentAnimation.setAnimation(animationSelector.getSelectedResource(), true);
			}
		}
		
		animationFileSelector.update(container, delta);
		weaponSelector.update(container, delta);
		if (animationSelector != null)
			animationSelector.update(container, delta);
		
		//spellSelector.update(container, delta);
		

		if (container.getInput().isKeyPressed(Input.KEY_ESCAPE))
		{
			game.enterState(TacticalGame.STATE_GAME_MENU_DEVEL);
		}
		
		if (rainParticleSystem != null)
			rainParticleSystem.update(delta);
	}

	@Override
	public int getID() {
		return TacticalGame.STATE_GAME_ANIM_VIEW;
	}

	@Override
	public boolean resourceSelected(String selectedItem,
			ListUI parentSelector) {
		if (parentSelector == animationFileSelector)
		{
			try {
				frm.addResource(ResourceManager.ANIMATION_IDENTIFIER + "," + selectedItem + "," + ResourceManager.ANIMATIONS_FOLDER + "/" + selectedItem, 
						TacticalGame.ENGINE_CONFIGURATIOR);
				currentAnimation = new AnimationWrapper(frm.getSpriteAnimation(selectedItem));
				ArrayList<String> animNames = new ArrayList<>(currentAnimation.getAnimations());
				
				if (animationSelector != null)
					animationSelector.unregisterListeners(container);
				
				animationSelector = new ListUI(container, "Anim", 650, animNames);
				animationSelector.setListener(this);
				weaponSelector.setSelectedIndex(-1);
			} catch (Throwable t ) {
				t.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to load the selected animation: " + t.getMessage(),
						"Unable to load animation", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else if (parentSelector == animationSelector)
		{
			currentAnimation.setAnimation(selectedItem, false);
			Point p = currentAnimation.getCurrentAnimation().getFirstFramePosition();
			
			if (p.getX() > 50) {
				drawX = 150;
				drawY = 220;
			} else if (p.getX() < 0) {
				drawX = 200;
				drawY = 250;
			} else {
				drawX = 150;
				drawY = 220;
			}
			// drawX = 125;
			// drawY = 180;
			
		}
		else if (parentSelector == weaponSelector)
		{
			if (currentAnimation != null)
			{				
				if (selectedItem.endsWith(ResourceManager.WEAPONS_EXTENSION))
					currentAnimation.setWeapon(frm.getImage(selectedItem.replace(ResourceManager.WEAPONS_EXTENSION, "")));
				else
					currentAnimation.setWeaponAnim(frm.getSpriteAnimation(selectedItem.replace(ResourceManager.ANIMATIONS_EXTENSION, "")));
			}
		}
		/*
		else if (parentSelector == spellSelector)
		{
			GlobalPythonFactory.intialize(); 
			spell = GlobalPythonFactory.createJSpell().init(selectedItem.replace(".py", "").toUpperCase());
			
			String rainFile = spell.getSpellRainAnimationFile(1);
			if (rainFile != null)
			{
				rainParticleSystem = new ParticleSystem(frm.getImage(rainFile));
				rainParticleSystem.addEmitter(new RainEmitter(180, spell.getSpellRainFrequency(1), true));
			}
			else
				rainParticleSystem = null;
		}
		*/

		return true;
	}

}
