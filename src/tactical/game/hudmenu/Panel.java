package tactical.game.hudmenu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

import tactical.engine.TacticalGame;
import tactical.engine.config.MusicConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.menu.MenuTransition;
import tactical.game.ui.PaddedGameContainer;
import tactical.loading.ResourceManager;

/**
 * A container to display information to the screen that does not generally need to be interacted with.
 */
public abstract class Panel
{
	public enum PanelType
	{
		PANEL_HEALTH_BAR,
		PANEL_LAND_EFFECT,
		PANEL_INITIATIVE,
		PANEL_ENEMY_HEALTH_BAR,
		PANEL_TEXT,
		PANEL_BATTLE,
		PANEL_BATTLE_OPTIONS,
		PANEL_MAPMOVE,
		PANEL_SPELL,
		PANEL_MAPATTACK,
		PANEL_CHAT,
		PANEL_SPEECH,
		PANEL_WAIT,
		PANEL_SYSTEM,
		PANEL_CONNECTIONS,
		PANEL_SHOP,
		PANEL_HEROS_OVERVIEW,
		PANEL_HEROS_STATS,
		PANEL_YES_NO,
		PANEL_PRIEST,
		PANEL_ASSIGN_HERO,
		PANEL_STRING,
		PANEL_BATTLE_MOVE,
		PANEL_ITEM,
		PANEL_ITEM_OPTIONS,
		PANEL_ADVISOR,
		PANEL_STORAGE,
		PANEL_TOWN,
		PANEL_CHANGE_PARTY,
		PANEL_DEBUG,
		PANEL_TARGET_HEALTH_BAR,
		PANEL_MAP_ENTRY,
		PANEL_SHOP_OPTIONS,
		PANEL_MULTI_JOIN_CHOOSE,
		PANEL_MINI_MAP,
		PANEL_PAUSE,
		PANEL_CONTEXT_DEBUG,
		PANEL_SELECT_HERO
	}

	protected PanelType panelType;
	protected MenuTransition transitionIn, transitionOut;
	public final static Color COLOR_MOUSE_OVER = new Color(0, 0, 153);
	public final static Color COLOR_FOREFRONT = Color.white;
	protected static MusicConfiguration MUSIC_SELECTOR;
	
	public static UnicodeFont PANEL_FONT;
	public static UnicodeFont SPEECH_FONT;


	public Panel(PanelType menuType) {
		super();
		this.panelType = menuType;

		switch (menuType)
		{
			default:
				break;
		}
	}

	public static void intialize(ResourceManager frm)
	{		
		PANEL_FONT = frm.getFontByName("menufont");
		MUSIC_SELECTOR = TacticalGame.ENGINE_CONFIGURATIOR.getMusicConfiguration();		
		SPEECH_FONT = frm.getFontByName("speechfont");
		TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().initializeResources(frm);
	}

	public abstract void render(PaddedGameContainer gc, Graphics graphics);

	public MenuUpdate update(int delta)
	{
		return MenuUpdate.MENU_NO_ACTION;
	}

	// public boolean

	public PanelType getPanelType() {
		return panelType;
	}

	

	public static void drawRect(Rectangle rect, Graphics graphics)
	{
		graphics.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static void fillRect(Rectangle rect, Graphics graphics)
	{
		graphics.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static boolean contains(int lowX, int highX, int valX, int lowY, int highY, int valY)
	{
		return (between(lowX, highX, valX) && between(lowY, highY, valY));
	}

	public static boolean between(int low, int high, int val)
	{
		if (val >= low && val < high)
			return true;
		return false;
	}

	public void panelRemoved(StateInfo stateInfo)
	{
		if (makeRemoveSounds())
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, MUSIC_SELECTOR.getMenuRemovedSoundEffect(), 1f, false));
	}

	public void panelAdded(StateInfo stateInfo)
	{
		if (makeAddSounds())
			stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, MUSIC_SELECTOR.getMenuAddedSoundEffect(), 1f, false));
	}

	public boolean makeAddSounds()
	{
		return false;
	}
	
	public boolean makeRemoveSounds()
	{
		return false;
	}
}
