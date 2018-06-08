package tactical.utils.planner.cinematic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

import de.jaret.util.date.Interval;
import de.jaret.util.ui.timebars.TimeBarMarkerImpl;
import de.jaret.util.ui.timebars.model.DefaultTimeBarRowModel;
import tactical.utils.planner.PlannerTimeBarViewer.ActorBar;
import tactical.utils.planner.PlannerTimeBarViewer.CameraLocation;
import tactical.utils.planner.PlannerTimeBarViewer.StaticSprite;

public class CinematicTimeline
{
	public DefaultTimeBarRowModel systemRow;
	public DefaultTimeBarRowModel soundRow;
	public DefaultTimeBarRowModel cameraRow;
	public Hashtable<String, ActorBar> rowsByName;
	public ArrayList<TimeBarMarkerImpl> markers;
	public ArrayList<Long> cinematicTime;
	public int duration;
	public ArrayList<CameraLocation> cameraLocations;
	public ArrayList<StaticSprite> staticSprites;

	public CinematicTimeline() {
		super();
	}

	public void dumpTimeline() {
		System.out.println("------ Duration ------");
		System.out.println(duration);
		System.out.println();

		System.out.println("------ Camera Locations ------");
		for (CameraLocation cl : cameraLocations)
			System.out.println(cl.toString());
		System.out.println();

		System.out.println("------ Camera Row ------");
		dumpModel(cameraRow);

		System.out.println("------ Actors ------");
		for (Entry<String, ActorBar> actor : rowsByName.entrySet()) {
			System.out.println("------ " + actor.getKey() + " -------");
			System.out.println("------ Action Row -------");
			dumpModel(actor.getValue().dt);
			System.out.println();
			System.out.println("------ Movement Row -------");
			dumpModel(actor.getValue().movementRowModel);
			System.out.println();
		}
	}

	public static void dumpModel(DefaultTimeBarRowModel model) {
		for (Interval i : model.getIntervals()) {
			System.out.println(i.toString());
		}
		System.out.println();
	}
}
