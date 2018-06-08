package tactical.game.hudmenu;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.listener.MouseListener;
import tactical.game.ui.PaddedGameContainer;
import tactical.map.Map;

public class MapMoveMenu extends Panel
{
	private static final int CAMERA_MOVE = 32;
	private ArrayList<MapListener> listeners;
	private int selected;
	private Camera camera;
	private int maxX;
	private int maxY;

	public MapMoveMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_MAPMOVE);

		listeners = new ArrayList<MapListener>();
		listeners.add(new MapListener(new Rectangle(20, 0, stateInfo.getPaddedGameContainer().getPaddedWidth() - 40, 20), 1, 0, -CAMERA_MOVE));
		listeners.add(new MapListener(new Rectangle(20, stateInfo.getPaddedGameContainer().getHeight() - 20, stateInfo.getPaddedGameContainer().getPaddedWidth() - 40, 20), 2, 0, CAMERA_MOVE));
		listeners.add(new MapListener(new Rectangle(stateInfo.getPaddedGameContainer().getPaddedWidth() - 20, 20, 20, stateInfo.getPaddedGameContainer().getHeight() - 40), 3, CAMERA_MOVE, 0));
		listeners.add(new MapListener(new Rectangle(0, 20, 20, stateInfo.getPaddedGameContainer().getHeight() - 40), 4, -CAMERA_MOVE, 0));

		for (MouseListener ml : listeners)
			stateInfo.registerMouseListener(ml);
		Map map = stateInfo.getResourceManager().getMap();
		maxX = map.getMapWidth() * stateInfo.getTileWidth() - stateInfo.getCamera().getViewportWidth();
		maxY = map.getMapHeight() * stateInfo.getTileHeight() - stateInfo.getCamera().getViewportHeight();

		camera = stateInfo.getCamera();;

		selected = 0;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics)
	{

	}

	private class MapListener implements MouseListener
	{
		private Rectangle triggerArea;
		private int selectedId;
		private int moveX;
		private int moveY;


		public MapListener(Rectangle triggerArea, int selectedId, int moveX, int moveY) {
			this.triggerArea = triggerArea;
			this.selectedId = selectedId;
			this.moveX = moveX;
			this.moveY = moveY;
		}

		@Override
		public boolean mouseUpdate(int frameMX, int frameMY, int mapMX,
				int mapMY, boolean leftClicked, boolean rightClicked,
				StateInfo stateInfo)
		{
			if (triggerArea.contains(frameMX, frameMY))
			{
				selected = this.selectedId;
				// TODO Should the camera be directly influenced here?
				float mX = camera.getLocationX() + moveX;
				float mY = camera.getLocationY() + moveY;

				if (mX < 0)
					mX = 0;
				else if (mX > maxX)
					mX = maxX;

				if (mY < 0)
					mY = 0;
				else if (mY > maxY)
					mY = maxY;

				camera.setLocation(mX, mY, stateInfo);
			}
			else if (selected == selectedId)
				selected = 0;
			return false;
		}

		@Override
		public int getZOrder() {
			return MouseListener.ORDER_MAP_MOVE;
		}
	}
}
