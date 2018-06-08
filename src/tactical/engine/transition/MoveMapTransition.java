package tactical.engine.transition;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.Transition;

import tactical.engine.state.StateInfo;
import tactical.engine.state.TownState;
import tactical.game.Camera;
import tactical.game.constants.Direction;
import tactical.game.ui.PaddedGameContainer;

public class MoveMapTransition implements Transition {
	private int transitionDuration = 2000;
	private TownState townState;
	private Image prevStateImage;
	private int timer = 0;
	private int distanceToMove;
	private Point initialSpritePoint;
	private Point destinationSpritePoint;
	private Camera camera;
	private Direction dir;
	private boolean spriteInPos = false;
	
	public MoveMapTransition(TownState townState, Direction dir) {
		this.townState = townState;
		this.initialSpritePoint = getSpriteScreenPoint(townState);
		this.dir = dir;
		this.camera = townState.getStateInfo().getCamera().duplicate();
		// Get the image before the state actually changes
		try {
			this.prevStateImage = townState.getStateImageScreenshot(false);
		} catch (SlickException e) {
			timer = transitionDuration;
		}
		distanceToMove = prevStateImage.getHeight();

	}

	private Point getSpriteScreenPoint(TownState townState) {
		StateInfo sInfo = townState.getStateInfo();
		return sInfo.getCamera().getSpriteScreenPosition(sInfo.getCurrentSprite(), sInfo.getTileHeight());
	}

	@Override
	public void update(StateBasedGame game, GameContainer container, int delta) throws SlickException {
		if (timer == 0)
			timer = 1;
		else
			timer = (int) Math.min(transitionDuration, timer + delta * (spriteInPos ? 3 : 3));
		townState.getStateInfo().getCurrentSprite().update(townState.getStateInfo());
	}

	@Override
	public void preRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		g.resetTransform();
		
		float newStateYPos = 0;
		float newStateXPos = 0;
		float oldStateOffsetX = 0;
		float oldStateOffsetY = 0;
		float spritePosX = 0;
		float spritePosY = 0;
		switch (dir) {
			case DOWN:
				newStateYPos = distanceToMove - (1.0f * timer / transitionDuration * distanceToMove);
				newStateXPos = 0;
				oldStateOffsetX = 0;
				oldStateOffsetY = -(1.0f * timer / transitionDuration) * distanceToMove;
				spritePosX = initialSpritePoint.getX();
				spritePosY = Math.max(
						initialSpritePoint.getY() + oldStateOffsetY / PaddedGameContainer.GAME_SCREEN_SCALE, destinationSpritePoint.getY());
				spriteInPos = (spritePosY == destinationSpritePoint.getY());
				break;
			case UP:
				newStateYPos = (1.0f * timer / transitionDuration) * distanceToMove - distanceToMove;
				newStateXPos = 0;
				oldStateOffsetX = 0;
				oldStateOffsetY = (1.0f * timer / transitionDuration) * distanceToMove;
				spritePosX = initialSpritePoint.getX();
				spritePosY = Math.min(
						initialSpritePoint.getY() + oldStateOffsetY / PaddedGameContainer.GAME_SCREEN_SCALE, destinationSpritePoint.getY());
				spriteInPos = (spritePosY == destinationSpritePoint.getY());
				break;
		}
		
		g.translate(newStateXPos, newStateYPos);
		renderNewState(game, container, g);
		
		g.drawImage(prevStateImage, oldStateOffsetX, oldStateOffsetY);
		g.scale(PaddedGameContainer.GAME_SCREEN_SCALE, PaddedGameContainer.GAME_SCREEN_SCALE);
		
		townState.getStateInfo().getCurrentSprite().renderDirect(spritePosX,
				spritePosY, camera, g, container, 
				townState.getStateInfo().getTileHeight());		
	}

	private void renderNewState(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		townState.getStateInfo().getCurrentSprite().setVisible(false);
		townState.render(container, game, g);
		townState.getStateInfo().getCurrentSprite().setVisible(true);
		g.resetTransform();
	}

	@Override
	public boolean isComplete() {
		return timer >= transitionDuration;
	}

	@Override
	public void init(GameState firstState, GameState secondState) {
		this.destinationSpritePoint = getSpriteScreenPoint(townState);
	}

}
