package tactical.utils.planner;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import de.jaret.util.date.Interval;
import de.jaret.util.date.IntervalImpl;
import de.jaret.util.date.JaretDate;
import de.jaret.util.ui.timebars.TimeBarMarker;
import de.jaret.util.ui.timebars.TimeBarMarkerImpl;
import de.jaret.util.ui.timebars.TimeBarViewerDelegate;
import de.jaret.util.ui.timebars.model.DefaultRowHeader;
import de.jaret.util.ui.timebars.model.DefaultTimeBarModel;
import de.jaret.util.ui.timebars.model.DefaultTimeBarRowModel;
import de.jaret.util.ui.timebars.swing.TimeBarViewer;
import de.jaret.util.ui.timebars.swing.renderer.DefaultHeaderRenderer;
import de.jaret.util.ui.timebars.swing.renderer.DefaultMarkerRenderer;
import de.jaret.util.ui.timebars.swing.renderer.DefaultTimeBarRenderer;
import tactical.cinematic.event.CinematicEvent;
import tactical.utils.planner.cinematic.CinematicTimeline;

public class PlannerTimeBarViewer extends TimeBarViewer implements AdjustmentListener
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<TimeBarMarkerImpl> markers;

	public PlannerTimeBarViewer(ArrayList<CinematicEvent> ces, CinematicTimeline ct, int cameraStartX, int cameraStartY)
	{
		this.markers = new ArrayList<TimeBarMarkerImpl>();
		generateGraph(ces, ct, cameraStartX, cameraStartY);

		setDrawOverlapping(false);
		setRowHeight(50);

		// int duration = (int) (currentTime / 1000) + 1;
		this.setInitialDisplayRange(new JaretDate(0), 8);
		this.setTimeBarRenderer(new ZTimeBarRenderer());
		this.setOptimizeScrolling(true);
		this._xScrollBar.addAdjustmentListener(this);
		this._xScrollBar.setUnitIncrement(50);
		this._xScrollBar.setBlockIncrement(50);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		this.validate();
		this.repaint();
	}

	public void generateGraph(ArrayList<CinematicEvent> ces, CinematicTimeline cinematicTimeline, int cameraStartX, int cameraStartY)
	{
		System.out.println("Generate Graph");
		DefaultTimeBarRowModel cameraRow = new DefaultTimeBarRowModel(new DefaultRowHeader("Camera"));
		ZIntervalImpl cameraInterval = null;
		DefaultTimeBarRowModel systemRow = new DefaultTimeBarRowModel(new DefaultRowHeader("System"));
		DefaultTimeBarRowModel soundRow = new DefaultTimeBarRowModel(new DefaultRowHeader("Sound"));
		ArrayList<Long> cinematicTime = new ArrayList<Long>();
		ArrayList<CameraLocation> cameraLocations = new ArrayList<>();
		ArrayList<StaticSprite> staticSprites = new ArrayList<>();
		CameraLocation cl = new CameraLocation();
		cl.locX = cameraStartX;
		cl.locY = cameraStartY;
		cl.time = 0;
		cameraLocations.add(cl);

		cinematicTimeline.cinematicTime = cinematicTime;

		ZIntervalImpl soundInterval = null;

		for (TimeBarMarkerImpl m : markers)
			this.remMarker(m);

		markers.clear();

		Hashtable<String, ActorBar> rowsByName = new Hashtable<String, ActorBar>();

		long currentTime = 0;

		for (CinematicEvent ce : ces)
		{
			cinematicTime.add(currentTime);

			switch (ce.getType())
			{
				// Actor Stuff
				case ADD_ACTOR:
					DefaultTimeBarRowModel dt = new DefaultTimeBarRowModel(new DefaultRowHeader("Actor: " + (String) ce.getParam(2)));
					// System.out.println("ADD ACTOR " + ce.getParam(2) + " " + ce.getParam(0) + " " + ce.getParam(1));
					ActorBar ab = new ActorBar(dt, (int) ce.getParam(0), (int) ce.getParam(1));
					ab.imageName = (String) ce.getParam(3);
					rowsByName.put((String) ce.getParam(2), ab);

					// Check invisible
					if (!(boolean) ce.getParam(5))
					{
						ZIntervalImpl zi = new ZIntervalImpl("Invisible");
						zi.setBegin(new JaretDate(currentTime));
						zi.setEnd(new JaretDate(currentTime + 100));
						ab.indefiniteIntervals.put("invisible", zi);
						ab.dt.addInterval(zi);
					}

					if (currentTime != 0)
					{
						ZIntervalImpl zi = new ZIntervalImpl("Not in scene");
						zi.setBegin(new JaretDate(0));
						zi.setEnd(new JaretDate(currentTime));
						dt.addInterval(zi);
					}
					break;
				case ASSOCIATE_AS_ACTOR:
					dt = new DefaultTimeBarRowModel(new DefaultRowHeader("Actor: " + (String) ce.getParam(0)));
					ab = new ActorBar(dt, 0, 0);
					rowsByName.put((String) ce.getParam(0), ab);
					break;
				case ADD_STATIC_SPRITE:
					int ssX = (int) ce.getParam(0);
					int ssY = (int) ce.getParam(1);
					String ssID = (String) ce.getParam(2);
					staticSprites.add(new StaticSprite(ssID, ssX, ssY, currentTime, (String) ce.getParam(3)));
					break;
				case REMOVE_STATIC_SPRITE:
					ssID = (String) ce.getParam(0);
					for (int i = 0; i < staticSprites.size(); i++)
						if (staticSprites.get(i).id.equalsIgnoreCase(ssID) && staticSprites.get(i).timeRemoved == 0)
						{
							staticSprites.get(i).timeRemoved = currentTime;
						}
					break;
				case MOVE:
					handleMove(currentTime, ce, rowsByName, "Move ", false);
					break;
				case MOVE_ENFORCE_FACING:
					handleMove(currentTime, ce, rowsByName, "Move Face Enforced ", true);
					break;
				case HALTING_MOVE:
					currentTime += handleMove(currentTime, ce, rowsByName, "Halting Move to ", false);
					break;
				case HALTING_MOVE_PATHFIND:
					currentTime += handleMove(currentTime, ce, rowsByName, "Halting Move Pathfind to ", false);
					break;
				case LOOP_MOVE:
					 ab = rowsByName.get(ce.getParam(0));

					ZIntervalImpl zi = new ZIntervalImpl("Loop Move " + (int) ce.getParam(1) + " " + (int) ce.getParam(2));
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("loopmove", zi);
					ab.dt.addInterval(zi);
					break;
				case STOP_LOOP_MOVE:
					ab = rowsByName.get(ce.getParam(0));

					zi = ab.indefiniteIntervals.get("loopmove");
					zi.setEnd(new JaretDate(currentTime));
					ab.indefiniteIntervals.remove("loopmove");
					break;

				case SPIN:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Spin");
					zi.setBegin(new JaretDate(currentTime));

					if ((int) ce.getParam(2) != -1)
						zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(2)));
					else
					{
						zi.setEnd(new JaretDate(currentTime + 1));
						ab.indefiniteIntervals.put("spin", zi);
					}
					ab.dt.addInterval(zi);
					break;
				case STOP_SPIN:
					ab = rowsByName.get(ce.getParam(0));

					zi = ab.indefiniteIntervals.get("spin");
					try
					{
						zi.setEnd(new JaretDate(currentTime));
					}
					catch (Throwable t)
					{
						System.out.println("Error on " + ce.getParam(0));
					}
					ab.indefiniteIntervals.remove("spin");
					break;

				case SHRINK:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Shrink");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(1)));
					ab.dt.addInterval(zi);
					break;
				case GROW:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Grow");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(1)));
					ab.dt.addInterval(zi);
					break;
				case QUIVER:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Agitate");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("quiver", zi);
					ab.dt.addInterval(zi);
					break;
				case TREMBLE:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Tremble");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("tremble", zi);
					ab.dt.addInterval(zi);
					break;
				case LAY_ON_SIDE_LEFT:
				case LAY_ON_SIDE_RIGHT:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Lay on Side");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("layonside", zi);
					ab.dt.addInterval(zi);
					break;
				case LAY_ON_BACK:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Lay on Back");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("layonback", zi);
					ab.dt.addInterval(zi);
					break;
				case FALL_ON_FACE:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Fall on Face");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("fallonface", zi);
					ab.dt.addInterval(zi);
					break;
				case FLASH:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Flash");
					zi.setBegin(new JaretDate(currentTime));

					if ((int) ce.getParam(2) != -1)
						zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(2)));
					else
					{
						ab.indefiniteIntervals.put("flash", zi);
						zi.setEnd(new JaretDate(currentTime + 1));
					}
					ab.dt.addInterval(zi);
					break;
				case NOD:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Nod");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 500));
					ab.dt.addInterval(zi);
					break;
				case HEAD_SHAKE:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Head Shake");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(1)));
					ab.dt.addInterval(zi);
					break;
				case STOP_SE:
					ab = rowsByName.get(ce.getParam(0));
					stopEffects(currentTime, ab, "quiver");
					stopEffects(currentTime, ab, "flash");
					stopEffects(currentTime, ab, "fallonface");
					stopEffects(currentTime, ab, "layonside");
					stopEffects(currentTime, ab, "layonback");
					stopEffects(currentTime, ab, "tremble");
					break;

				case VISIBLE:
					ab = rowsByName.get(ce.getParam(0));
					if ((boolean) ce.getParam(1))
					{
						zi = ab.indefiniteIntervals.get("invisible");
						if (zi != null)
						{
							zi.setEnd(new JaretDate(currentTime));
							ab.indefiniteIntervals.remove("invisible");
						}
					}
					else
					{
						zi = new ZIntervalImpl("Invisible");
						zi.setBegin(new JaretDate(currentTime));
						zi.setEnd(new JaretDate(currentTime + 1));
						ab.indefiniteIntervals.put("invisible", zi);
						ab.dt.addInterval(zi);
					}
					break;
				case REMOVE_ACTOR:
					ab = rowsByName.get(ce.getParam(0));
					zi = new ZIntervalImpl("Not in scene");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 1));
					ab.indefiniteIntervals.put("notinscene", zi);
					ab.dt.addInterval(zi);
					break;
				case FACING:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Set Facing");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 100));
					ab.dt.addInterval(zi);
					break;

				case STOP_ANIMATION:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Stop Animating");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 100));
					ab.dt.addInterval(zi);
					break;
				case HALTING_ANIMATION:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Halting Animating " + (String) ce.getParam(1));
					zi.setBegin(new JaretDate(currentTime));
					currentTime += (int) ce.getParam(2);
					zi.setEnd(new JaretDate(currentTime));
					ab.dt.addInterval(zi);
					break;
				case ANIMATION:
					ab = rowsByName.get(ce.getParam(0));

					zi = new ZIntervalImpl("Animation " + (String) ce.getParam(1));
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime));
					ab.dt.addInterval(zi);
					break;


				// Camera Stuff
				case CAMERA_MOVE:
					this.moveCameraToLocation(currentTime, cameraInterval, cameraLocations, cameraRow, rowsByName, 
							(int) ce.getParam(0), (int) ce.getParam(1), (int) ce.getParam(2));
					break;
				case CAMERA_MOVE_TO_ACTOR:
					ActorBar followingAB = rowsByName.get(ce.getParam(0));
					Point followingPoint = followingAB.getActorLocationAtTime((int) currentTime - 1, true).currentPoint;
					int destX = followingPoint.x;
					int destY = followingPoint.y;
					this.moveCameraToLocation(currentTime, cameraInterval, cameraLocations, cameraRow, rowsByName, 
							destX, destY, (int) ce.getParam(1));
					break;
				case CAMERA_CENTER:
					if (cameraInterval != null)
						cameraInterval.setEnd(new JaretDate(currentTime));

					zi = new ZIntervalImpl("Center Camera " + (int) ce.getParam(0) + " " + (int) ce.getParam(1));
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 100));
					cameraInterval = zi;
					cameraRow.addInterval(zi);

					cl = new CameraLocation();
					cl.locX = (int) ce.getParam(0);
					cl.locY = (int) ce.getParam(1);
					cl.time = currentTime;
					cameraLocations.add(cl);
					break;

				case CAMERA_FOLLOW:
					if (cameraInterval != null)
						cameraInterval.setEnd(new JaretDate(currentTime));

					zi = new ZIntervalImpl("Camera Follow " + (String) ce.getParam(0));
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 100));
					cameraInterval = zi;
					cameraRow.addInterval(zi);

					cl = new CameraLocation();
					cl.time = currentTime;
					cl.following = (String) ce.getParam(0);
					cameraLocations.add(cl);
					break;

				case CAMERA_SHAKE:
					zi = new ZIntervalImpl("Camera Shake");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(0)));
					cameraInterval = zi;
					cameraRow.addInterval(zi);

					break;

				// System Stuff
				case SPEECH:
					zi = new ZIntervalImpl("Speech");
					zi.setBegin(new JaretDate(currentTime));
					TimeBarMarkerImpl tb = new TimeBarMarkerImpl(false, new JaretDate(currentTime));
					this.markers.add(tb);
					addMarker(tb);
					currentTime += 1;
					tb = new TimeBarMarkerImpl(false, new JaretDate(currentTime));
					this.markers.add(tb);
					addMarker(tb);
					zi.setEnd(new JaretDate(currentTime));
					systemRow.addInterval(zi);

					break;
				case WAIT:
					zi = new ZIntervalImpl("Wait");
					zi.setBegin(new JaretDate(currentTime));
					currentTime += (int) ce.getParam(0);
					zi.setEnd(new JaretDate(currentTime));
					systemRow.addInterval(zi);
					break;
				// Music stuff
				case PLAY_MUSIC:
					if (soundInterval != null)
						soundInterval.setEnd(new JaretDate(currentTime));

					zi = new ZIntervalImpl("Play Music " + (String) ce.getParam(0));
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime));
					soundInterval = zi;
					soundRow.addInterval(zi);
					break;
				case PAUSE_MUSIC:
					if (soundInterval != null)
						soundInterval.setEnd(new JaretDate(currentTime));

					soundInterval = null;

					break;
				case RESUME_MUSIC:
					zi = new ZIntervalImpl("Resume Music");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime));
					soundInterval = zi;
					soundRow.addInterval(zi);
					break;
				case FADE_MUSIC:

					zi = new ZIntervalImpl("Fade Music");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(0)));
					soundRow.addInterval(zi);
					break;
				case PLAY_SOUND:
					zi = new ZIntervalImpl("Play Music");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + 100));
					soundRow.addInterval(zi);
					break;
				case FADE_FROM_BLACK:
					if ((boolean) ce.getParam(2))
					{
						cinematicTime.remove(cinematicTime.size() - 1);
						continue;
					}

					zi = new ZIntervalImpl("Fade from black");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(0)));

					if ((boolean) ce.getParam(1))
						currentTime += (int) ce.getParam(0);

					systemRow.addInterval(zi);
					break;
				case FADE_TO_BLACK:
					zi = new ZIntervalImpl("Fade to black");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(0)));

					if ((boolean) ce.getParam(1))
						currentTime += (int) ce.getParam(0);

					systemRow.addInterval(zi);
					break;
				case FLASH_SCREEN:
					zi = new ZIntervalImpl("Flash Screen");
					zi.setBegin(new JaretDate(currentTime));
					zi.setEnd(new JaretDate(currentTime + (int) ce.getParam(0)));

					systemRow.addInterval(zi);
					break;
				default:
					break;
			}
		}

		DefaultTimeBarModel model = new DefaultTimeBarModel();

		model.addRow(systemRow);
		model.addRow(cameraRow);
		model.addRow(soundRow);

		for (ActorBar ab : rowsByName.values())
		{
			for (ZIntervalImpl zi : ab.indefiniteIntervals.values())
			{
				zi.setEnd(new JaretDate(currentTime));
			}

			ab.changeLocations(ab.locX, ab.locY, currentTime);

			model.addRow(ab.dt);
		}

		if (soundInterval != null)
			soundInterval.setEnd(new JaretDate(currentTime));

		if (cameraInterval != null)
			cameraInterval.setEnd(new JaretDate(currentTime));


		this.setModel(model);
		setHeaderRenderer(new ZHeaderRenderer());
		this.setSecondsDisplayed(8, false);
		this.repaint();

		cinematicTimeline.cameraRow = cameraRow;
		cinematicTimeline.systemRow = systemRow;
		cinematicTimeline.soundRow = soundRow;
		cinematicTimeline.markers = markers;
		cinematicTimeline.rowsByName = rowsByName;
		cinematicTimeline.duration = (int) currentTime;
		cinematicTimeline.cameraLocations = cameraLocations;
		cinematicTimeline.staticSprites = staticSprites;
	}
	
	private void moveCameraToLocation(long currentTime, ZIntervalImpl cameraInterval, ArrayList<CameraLocation> cameraLocations, 
			DefaultTimeBarRowModel cameraRow, Hashtable<String, ActorBar> rowsByName, int camDestX, int camDestY, int duration)
	{
		if (cameraInterval != null)
			cameraInterval.setEnd(new JaretDate(currentTime));

		ZIntervalImpl zi = new ZIntervalImpl("Move Camera to  " + camDestX + " " + camDestY);
		zi.setBegin(new JaretDate(currentTime));
		zi.setEnd(new JaretDate(currentTime + duration));
		cameraInterval = zi;
		cameraRow.addInterval(zi);

		CameraLocation cl = new CameraLocation();
		CameraLocation oldLoc = cameraLocations.get(cameraLocations.size() - 1);
		if (oldLoc.following != null)
		{
			ActorBar followingAB = rowsByName.get(oldLoc.following);
			Point followingPoint = followingAB.getActorLocationAtTime((int) currentTime - 1, true).currentPoint;

			if (followingPoint.x < 160)
				followingPoint.x = 0;
			else
				followingPoint.x = (Math.max(0, followingPoint.x - 160));

			if (followingPoint.y < 120)
				followingPoint.y = 0;
			else
				followingPoint.y = (Math.max(0, followingPoint.y - 120));

			cl.locX = followingPoint.x;
			cl.locY = followingPoint.y;
		}
		else
		{
			cl.locX = oldLoc.locX;
			cl.locY = oldLoc.locY;
		}

		cl.endLocX = camDestX - 160;
		cl.endLocY = camDestY - 120;
		cl.time = currentTime;
		cl.duration = duration;
		System.out.println(cl);
		cameraLocations.add(cl);

		cl = new CameraLocation();
		cl.locX = camDestX - 160;
		cl.locY = camDestY - 120;
		cl.time = currentTime + duration;
		cameraLocations.add(cl);
	}

	public class CameraLocation
	{
		public int locX, locY;
		public int endLocX = -1, endLocY = -1;
		public String following;
		public long time;
		public long duration;

		@Override
		public String toString() {
			return "CameraLocation [locX=" + locX + ", locY=" + locY
					+ ", endLocX=" + endLocX + ", endLocY=" + endLocY
					+ ", following=" + following + ", time=" + time
					+ ", duration=" + duration + "]";
		}
	}

	private void stopEffects(long currentTime, ActorBar ab, String command)
	{
		ZIntervalImpl zi = ab.indefiniteIntervals.get(command);
		if (zi != null)
		{
			zi.setEnd(new JaretDate(currentTime));
			ab.indefiniteIntervals.remove(command);
		}
	}

	private long handleMove(long currentTime, CinematicEvent ce, Hashtable<String, ActorBar> rowsByName, String title, boolean enforced)
	{

		ActorBar ab = rowsByName.get(ce.getParam(3));
		// Get the current location that the actor is at
		Point currentPoint =  ab.getActorLocationAtTime((int) currentTime, true).currentPoint;

		int xDistance = Math.abs(currentPoint.x - (int) ce.getParam(0));
		int yDistance = Math.abs(currentPoint.y - (int) ce.getParam(1));

		boolean moveHor = false;
		boolean moveDiag = false;

		if (enforced)
		{
			moveHor = (boolean) ce.getParam(5);
			moveDiag = (boolean) ce.getParam(6);
		}
		else
		{
			moveHor = (boolean) ce.getParam(4);
			moveDiag = (boolean) ce.getParam(5);
		}
		long duration = ((int) (Math.ceil(xDistance / (float) ce.getParam(2))) + (int) (Math.ceil(yDistance / (float) ce.getParam(2)))) * 20;

		ZIntervalImpl zi = new ZMoveIntervalImpl(title + (int) ce.getParam(0) + " " + (int) ce.getParam(1), currentPoint.x, currentPoint.y, (int) ce.getParam(0), (int) ce.getParam(1),
				currentTime, duration, moveHor, moveDiag);
		zi.setBegin(new JaretDate(currentTime));

		zi.setEnd(new JaretDate(currentTime + duration));
		ab.dt.addInterval(zi);
		ab.changeLocations((int) ce.getParam(0), (int) ce.getParam(1), currentTime + duration);
		return duration;
	}

	public class ZHeaderRenderer extends DefaultHeaderRenderer
    {
		@Override
		public int getWidth() {
			return 200;
		}
    }

    public class ZIntervalImpl extends IntervalImpl
    {
    	public String title;

    	public ZIntervalImpl(String title)
    	{
    		this.title = title;
    	}

		@Override
		public String toString() {
			return title;
		}

		public boolean isMove()
		{
			return false;
		}
    }

    public class ZLocationImpl extends IntervalImpl
    {
    	public int locX, locY;

		public ZLocationImpl(int locX, int locY) {
			super();
			this.locX = locX;
			this.locY = locY;
		}

		public int getLocX() {
			return locX;
		}

		public int getLocY() {
			return locY;
		}

		@Override
		public String toString() {
			return "ZLocationImpl [locX=" + locX + ", locY=" + locY
					+ ", _begin=" + _begin + ", _end=" + _end + "]";
		}
    }

    public class ZMoveIntervalImpl extends ZIntervalImpl
    {
    	private int startX, startY, endX, endY;
    	private long duration, currentTime;
    	private boolean moveHor, moveDiag;

		public ZMoveIntervalImpl(String title, int startX, int startY,
				int endX, int endY, long currentTime, long duration, boolean moveHor, boolean moveDiag) {
			super(title);
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
			this.duration = duration;
			this.currentTime = currentTime;
			this.moveHor = moveHor;
			this.moveDiag = moveDiag;
		}

		@Override
		public boolean isMove()
		{
			return true;
		}

		public int getStartX() {
			return startX;
		}

		public int getStartY() {
			return startY;
		}

		public int getEndX() {
			return endX;
		}

		public int getEndY() {
			return endY;
		}

		public long getDuration() {
			return duration;
		}

		public long getCurrentTime() {
			return currentTime;
		}

		public boolean isMoveHor() {
			return moveHor;
		}

		public boolean isMoveDiag() {
			return moveDiag;
		}

		@Override
		public String toString() {
			return "ZMoveIntervalImpl [startX=" + startX + ", startY=" + startY
					+ ", endX=" + endX + ", endY=" + endY + ", duration="
					+ duration + ", currentTime=" + currentTime + ", moveHor="
					+ moveHor + ", moveDiag=" + moveDiag + ", title=" + title
					+ "]";
		}
    }

    public class ActorBar
    {
    	/**
    	 * These contain the actions as ZIntervalImpls, which can
    	 * also be ZMoveIntervalImpls
    	 */
    	public DefaultTimeBarRowModel dt;

    	/**
    	 * These contain ZLocationImpls
    	 */
    	public DefaultTimeBarRowModel movementRowModel;
    	public int locX;
    	public int locY;
    	public Hashtable<String, ZIntervalImpl> indefiniteIntervals;
    	public long lastMoveTime = 0;
    	public String imageName;

		public ActorBar(DefaultTimeBarRowModel dt, int locX, int locY) {
			super();
			this.dt = dt;
			indefiniteIntervals = new Hashtable<String, ZIntervalImpl>();
			movementRowModel = new DefaultTimeBarRowModel();
			changeLocations(locX, locY, 0);
		}

		public void changeLocations(int locX, int locY, long currentTime)
		{
			ZLocationImpl zi = new ZLocationImpl(this.locX, this.locY);
			zi.setBegin(new JaretDate(lastMoveTime));
			zi.setEnd(new JaretDate(currentTime));
			movementRowModel.addInterval(zi);
			this.lastMoveTime = currentTime;
			this.locX = locX;
			this.locY = locY;
		}

		public MovingSprite getActorLocationAtTime(int time)
		{
			return getActorLocationAtTime(time, false);
		}

		public MovingSprite getActorLocationAtTime(int time, boolean duringConstruction)
		{
			List<Interval> actorIntervals = dt.getIntervals(new JaretDate(time));

			boolean moving = false;

			Point actorPoint = null;
			
			MovingSprite movingSprite = new MovingSprite();

			for (Interval i : actorIntervals)
			{
				if (((ZIntervalImpl) i).isMove())
				{
					ZMoveIntervalImpl zmi = (ZMoveIntervalImpl) i;
					movingSprite.moveInterval = zmi;
					int xDiff = zmi.getEndX() - zmi.getStartX();
					int yDiff = zmi.getEndY() - zmi.getStartY();
					int totalMove = Math.abs(xDiff) + Math.abs(yDiff);
					float percent = (time - 1.0f * zmi.getCurrentTime()) / zmi.getDuration();

					if (zmi.isMoveDiag())
					{
						actorPoint = new Point(zmi.getStartX() + (int)(xDiff * percent), zmi.getStartY() + (int)(yDiff * percent));
					}
					else if (zmi.isMoveHor())
					{
						float percentX = Math.abs(1.0f * xDiff / totalMove);
						if (percent > percentX)
						{
							actorPoint = new Point(zmi.getEndX(), (int)((percent - percentX) / (1 - percentX) * yDiff) + zmi.getStartY());
						}
						else
						{
							actorPoint = new Point((int)(percent / percentX * xDiff) + zmi.getStartX(), zmi.getStartY());
						}
					}
					else
					{
						float percentY = Math.abs(1.0f * yDiff / totalMove);
						System.out.println(percent + " " + percentY);
						if (percent > percentY)
						{
							actorPoint = new Point((int)((percent - percentY) / (1 - percentY) * xDiff) + zmi.getStartX(), zmi.getEndY());
						}
						else
							actorPoint = new Point(zmi.getStartX(), (int)(percent / percentY * yDiff) + zmi.getStartY());
					}

					moving = true;
					break;
				}

			}
			
			if (!moving)
			{
				if (!duringConstruction && movementRowModel.getIntervals(new JaretDate(time)).size() > 0)
				{
					// List<Interval> moveList =  movementRowModel.getIntervals(new JaretDate(0), new JaretDate(time));
					ZLocationImpl zli = (ZLocationImpl) movementRowModel.getIntervals(new JaretDate(time)).get(0);
					if (zli.getEnd().getMillis() == 0 && movementRowModel.getIntervals(new JaretDate(time)).size() > 1)
						zli = (ZLocationImpl) movementRowModel.getIntervals(new JaretDate(time)).get(1);
					actorPoint = new Point(zli.locX, zli.locY);
				}
				else
					actorPoint = new Point(this.locX, this.locY);
			}
			
			movingSprite.currentPoint = actorPoint;

			return movingSprite;
		}

		public boolean isActorInScene(int time)
		{
			for (Interval i : dt.getIntervals(new JaretDate(time)))
			{
				if (i.toString().equalsIgnoreCase("Not in scene"))
					return false;
			}
			return true;
		}
    }
    
    public static class MovingSprite
    {
    	public Point currentPoint;
    	public ZMoveIntervalImpl moveInterval;
		public MovingSprite(Point currentPoint) {
			super();
			this.currentPoint = currentPoint;
		}
    	
    	public MovingSprite() {}
    }

    public class StaticSprite
    {
    	public String id;
    	public int locX, locY;
    	public long timeAdded, timeRemoved;
    	public String image;

		public StaticSprite(String id, int locX, int locY, long timeAdded, String image) {
			super();
			this.id = id;
			this.locX = locX;
			this.locY = locY;
			this.timeAdded = timeAdded;
			this.image = image;
		}
    }

    public class ZTimeBarMarker extends TimeBarMarkerImpl
    {

		public ZTimeBarMarker(JaretDate date) {
			super(false, date);
		}

		@Override
		public String getDescription() {
			return "Can you see me";
		}

    }

    public class ZMarkerRenderer extends DefaultMarkerRenderer
    {

		@Override
		public void renderMarker(TimeBarViewerDelegate delegate,
				Graphics graphics, TimeBarMarker marker, int x,
				boolean isDragged) {
			super.renderMarker(delegate, graphics, marker, x, isDragged);
		}

    }

    public class ZTimeBarRenderer extends DefaultTimeBarRenderer
    {

    }
}
