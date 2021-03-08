package tactical.cinematic.event;

import java.util.ArrayList;

/**
 * Container to hold parameters for a CinematicEvent and associate
 * a CinematicEventType with this event
 *
 * @author Broked
 *
 */
public class CinematicEvent
{
	public enum CinematicEventType
	{
		MOVE,
		MOVE_ENFORCE_FACING,
		LOOP_MOVE,
		STOP_LOOP_MOVE,
		REMOVE_ACTOR,
		ADD_ACTOR,
		ASSOCIATE_AS_ACTOR,
		ASSOCIATE_HERO_AS_ACTOR,
		ASSOCIATE_ENEMY_AS_ACTOR,
		ADD_STATIC_SPRITE,
		REMOVE_STATIC_SPRITE,
		ANIMATION_LOOP,
		ANIMATION,
		STOP_ANIMATION,
		HALTING_ANIMATION,
		WAIT,
		SPIN,
		STOP_SPIN,
		CREATE,
		LOAD_MAP,
		LOAD_CHAPTER,
		LOAD_BATTLE,
		LOAD_CIN,
		FLASH,
		NOD,
		HEAD_SHAKE,
		FACING,
		SHRINK,
		GROW,
		TREMBLE,
		QUIVER,
		FALL_ON_FACE,
		LAY_ON_SIDE_RIGHT,
		LAY_ON_SIDE_LEFT,
		LAY_ON_BACK,
		STOP_SE,
		VISIBLE,
		SPEECH,
		MULTI_HERO_JOIN_MENU,
		CAMERA_SHAKE,
		CAMERA_FOLLOW,
		CAMERA_CENTER,
		CAMERA_MOVE,
		CAMERA_MOVE_TO_ACTOR,
		STARTLOOPMAP,
		ENDLOOPMAP,
		FADE,
		PLAY_MUSIC,
		PAUSE_MUSIC,
		RESUME_MUSIC,
		FADE_MUSIC,
		PLAY_SOUND,
		FADE_TO_BLACK,
		FADE_FROM_BLACK,
		FLASH_SCREEN,
		MOVE_TO_FOREFRONT,
		MOVE_FROM_FOREFRONT,
		EXIT_GAME,
		SHOW_CREDITS,
		ADD_HERO,
		SHOW_MAIN_MENU
	}

	private CinematicEventType type;
	private ArrayList<Object> params;

	public CinematicEvent(CinematicEventType type, Object... params)
	{
		this.type = type;
		this.params = new ArrayList<Object>();

		for (Object o : params)
			this.params.add(o);
	}

	public CinematicEventType getType() {
		return type;
	}

	public Object getParam(int i)
	{
		return params.get(i);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type.name() + " ");
		for (Object o : params)
			sb.append(o.toString() + " ");
		return sb.toString();
	}


}
