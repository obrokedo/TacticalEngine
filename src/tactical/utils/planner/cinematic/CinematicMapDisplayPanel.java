package tactical.utils.planner.cinematic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.jaret.util.date.Interval;
import de.jaret.util.date.JaretDate;
import tactical.cinematic.event.CinematicEvent;
import tactical.engine.TacticalGame;
import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerIO;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.PlannerTimeBarViewer;
import tactical.utils.planner.PlannerTimeBarViewer.ActorBar;
import tactical.utils.planner.PlannerTimeBarViewer.MovingSprite;
import tactical.utils.planner.PlannerTimeBarViewer.StaticSprite;
import tactical.utils.planner.PlannerTimeBarViewer.ZIntervalImpl;
import tactical.utils.planner.PlannerTimeBarViewer.ZMoveIntervalImpl;

public class CinematicMapDisplayPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;

	private PlannerMap plannerMap;
	private MapObject selectedMO;
	private CinematicCreatorPanel mapPanel;
	private CinematicTimeline timeline;
	private int mouseX, mouseY;
	private JPopupMenu systemPopup, actorPopup, actorMovePopup;

	private final Color SPRITE_FILL_COLOR = new Color(230, 230, 230, 50);
	private final Color SPRITE_LINE_COLOR = new Color(230, 230, 230);

	private ArrayList<String> actorImages = new ArrayList<String>();
	private ArrayList<Point> actorLocations = new ArrayList<Point>();
	private ArrayList<ZMoveIntervalImpl> actorMoving = new ArrayList<>();
	private ArrayList<Point> spriteLocations = new ArrayList<Point>();
	private ArrayList<String> spriteImages = new ArrayList<String>();
	private int selectedActor = -1;
	private int popupType = 0;
	private Point cameraLocation = null;

	private PlannerContainer currentPC;

	public CinematicMapDisplayPanel(PlannerMap map, CinematicCreatorPanel mapPanel)
	{
		systemPopup = new JPopupMenu();
		systemPopup.add(createMenuItem("Wait"));
		systemPopup.add(createMenuItem("Add Actor"));
		systemPopup.add(createMenuItem("Establish Sprite as Actor"));
		systemPopup.add(createMenuItem("Add Static Sprite"));
		systemPopup.add(createMenuItem("Remove Static Sprite"));
		systemPopup.add(createMenuItem("Play Music"));
		systemPopup.add(createMenuItem("Pause Music"));
		systemPopup.add(createMenuItem("Resume Music"));
		systemPopup.add(createMenuItem("Fade Out Music"));
		systemPopup.add(createMenuItem("Play Sound"));
		systemPopup.add(createMenuItem("Fade in from black"));
		systemPopup.add(createMenuItem("Fade to black"));
		systemPopup.add(createMenuItem("Flash Screen"));
		systemPopup.add(createMenuItem("Camera Pan"));
		systemPopup.add(createMenuItem("Camera Move To Actor"));
		systemPopup.add(createMenuItem("Shake Camera"));
		systemPopup.add(createMenuItem("Show Speech Box"));
		systemPopup.add(createMenuItem("Load Map"));
		systemPopup.add(createMenuItem("Start Battle"));
		systemPopup.add(createMenuItem("Add Hero"));
		systemPopup.add(createMenuItem("Add Hero from Selection"));
		systemPopup.add(createMenuItem("Exit Game"));



		actorPopup = new JPopupMenu();
		actorPopup.add(createMenuItem("Spin Actor"));
		actorPopup.add(createMenuItem("Stop Actor Spinning"));
		actorPopup.add(createMenuItem("Set Actor Facing"));
		actorPopup.add(createMenuItem("Shrink Actor"));
		actorPopup.add(createMenuItem("Grow Actor"));
		actorPopup.add(createMenuItem("Start Actor Trembling"));
		actorPopup.add(createMenuItem("Start Actor Agitate"));
		actorPopup.add(createMenuItem("Actor Fall on Face"));
		actorPopup.add(createMenuItem("Actor Lay on Side Right"));
		actorPopup.add(createMenuItem("Actor Lay on Side Left"));
		actorPopup.add(createMenuItem("Actor Lay on Back"));
		actorPopup.add(createMenuItem("Actor Flash"));
		actorPopup.add(createMenuItem("Actor Nod"));
		actorPopup.add(createMenuItem("Actor Shake Head"));
		actorPopup.add(createMenuItem("Stop Actor Special Effect"));
		actorPopup.add(createMenuItem("Set Actor Visiblity"));
		actorPopup.add(createMenuItem("Halting Animation"));
		actorPopup.add(createMenuItem("Animation"));
		actorPopup.add(createMenuItem("Stop Animation"));
		actorPopup.add(createMenuItem("Render on Top"));
		actorPopup.add(createMenuItem("Render on Normal"));
		actorPopup.add(createMenuItem("Set Camera follows actor"));
		actorPopup.add(createMenuItem("Stop Actor Looping Move"));
		actorPopup.add(createMenuItem("Remove Actor"));

		actorMovePopup = new JPopupMenu();
		actorMovePopup.add(createMenuItem("Halting Move"));
		actorMovePopup.add(createMenuItem("Halting Move with Pathfinding"));
		actorMovePopup.add(createMenuItem("Move"));
		actorMovePopup.add(createMenuItem("Move Forced Facing"));
		actorMovePopup.add(createMenuItem("Move Actor in Loop"));

		plannerMap = map;
		this.mapPanel = mapPanel;

		this.setPreferredSize(new Dimension(plannerMap.getMapWidthInPixels(), plannerMap.getMapHeightInPixels()));
		this.addMouseListener(this);
		this.addMouseMotionListener(mapPanel);
		this.addMouseMotionListener(this);
	}

	private JMenuItem createMenuItem(String menuItem)
	{
		JMenuItem jmi = new JMenuItem(menuItem);
		jmi.addActionListener(this);
		jmi.setActionCommand(menuItem);
		return jmi;
	}

	public void setStaticSpritesAtTime(int time)
	{
		spriteLocations.clear();
		spriteImages.clear();
		for (StaticSprite ss : timeline.staticSprites)
		{
			if (ss.timeAdded <= time && (ss.timeRemoved > time || ss.timeRemoved == 0)) {
				spriteLocations.add(new Point(ss.locX, ss.locY));
				spriteImages.add(ss.image);
			}			
		}
	}

	public ArrayList<String> getSystemValuesAtTime(int time)
	{
		ArrayList<String> values = new ArrayList<String>();
		addIntervals(timeline.systemRow.getIntervals(new JaretDate(time)), values);
		return values;
	}

	public ArrayList<String> getCameraValuesAtTime(int time)
	{
		ArrayList<String> values = new ArrayList<String>();
		addIntervals(timeline.cameraRow.getIntervals(new JaretDate(time)), values);
		return values;
	}

	public ArrayList<String> getSoundValuesAtTime(int time)
	{
		ArrayList<String> values = new ArrayList<String>();
		addIntervals(timeline.soundRow.getIntervals(new JaretDate(time)), values);
		return values;
	}

	public ArrayList<ArrayList<String>> getActorValuesAtTime(int time, ArrayList<String> names)
	{
		actorLocations.clear();
		actorMoving.clear();
		actorImages.clear();
		ArrayList<ArrayList<String>> lol = new ArrayList<ArrayList<String>>();

		ArrayList<Entry<String, ActorBar>> listOfActorEntrys = new ArrayList<>(timeline.rowsByName.entrySet());
		Collections.sort(listOfActorEntrys, new Comparator<Entry<String, ActorBar>>() {

			@Override
			public int compare(Entry<String, ActorBar> arg0,
					Entry<String, ActorBar> arg1) {
				return arg0.getKey().compareTo(arg1.getKey());
			}});

		for (Entry<String, ActorBar> ab : listOfActorEntrys)
		{
			ArrayList<String> values = new ArrayList<String>();

			if (ab.getValue().isActorInScene(time))
			{
				names.add(ab.getKey());

				MovingSprite movingSprite = ab.getValue().getActorLocationAtTime(time);
				Point actorPoint = movingSprite.currentPoint;

				values.add("Current Location: " + actorPoint.x + " " + actorPoint.y);
				// values.add("Actor is Moving: " + moving);
				actorLocations.add(actorPoint);
				actorMoving.add(movingSprite.moveInterval);
				actorImages.add(ab.getValue().imageName);
				

				addIntervals(ab.getValue().dt.getIntervals(new JaretDate(time)), values);

				lol.add(values);
			}
		}

		return lol;
	}

	public MovingSprite getActorLocationAtTime(int time, String name)
	{
		if (!timeline.rowsByName.containsKey(name))
			return new MovingSprite(new Point(0, 0));

		ActorBar ab = timeline.rowsByName.get(name);

		if (ab.isActorInScene(time))
			return ab.getActorLocationAtTime(time);

		return new MovingSprite(new Point(0, 0));
	}

	private void addIntervals(List<Interval> intervals, ArrayList<String> values)
	{
		for (Interval i : intervals)
		{
			if (i instanceof ZIntervalImpl)
				values.add(((ZIntervalImpl) i).title);
			else
				values.add(i.toString());
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		plannerMap.renderMap(g, this, 1);

		if (PlannerFrame.SHOW_CIN_LOCATION)
		{
			plannerMap.renderMapLocations(g, selectedMO, 1);
		}

		for (int i = 0; i < spriteLocations.size(); i++)
		{
			Point sp = spriteLocations.get(i);
			g.setColor(SPRITE_FILL_COLOR);
			g.fillRect(sp.x, sp.y, plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());
			g.setColor(SPRITE_LINE_COLOR);
			g.drawRect(sp.x, sp.y, plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());
			BufferedImage bim = plannerMap.getImagesByName().get(spriteImages.get(i));
			if (bim != null)
				g.drawImage(bim, sp.x, sp.y, this);
		}

		g.setColor(Color.red);
		for (int a = 0; a < actorLocations.size(); a++)
		{
			Point ap = actorLocations.get(a);
			BufferedImage bim = plannerMap.getImagesByName().get(actorImages.get(a));
			g.setColor(Color.red);
			if (selectedActor != -1 && a == selectedActor)
				g.setColor(Color.yellow);
			g.fillRect(ap.x, ap.y, plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());
			g.setColor(Color.white);
			g.drawRect(ap.x, ap.y, plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());
			
			if (bim != null) {
				g.drawImage(bim, ap.x, ap.y, this);
			}
			
			ZMoveIntervalImpl zmi = actorMoving.get(a);
			if (zmi != null)
			{
				g.setColor(Color.YELLOW);
				if (zmi.isMoveDiag())
				{
					g.drawLine(ap.x + plannerMap.getTileEffectiveWidth() / 2, 
							ap.y + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndX() + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndY() + plannerMap.getTileEffectiveWidth() / 2);
				}
				else if (zmi.isMoveHor())
				{
					g.drawLine(ap.x + plannerMap.getTileEffectiveWidth() / 2, 
							ap.y + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndX() + plannerMap.getTileEffectiveWidth() / 2, 
							ap.y + plannerMap.getTileEffectiveWidth() / 2);
					g.drawLine(zmi.getEndX() + plannerMap.getTileEffectiveWidth() / 2, 
							ap.y + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndX() + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndY() + plannerMap.getTileEffectiveWidth() / 2);
				}
				else
				{
					g.drawLine(ap.x + plannerMap.getTileEffectiveWidth() / 2, 
							ap.y + plannerMap.getTileEffectiveWidth() / 2, 
							ap.x + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndY() + plannerMap.getTileEffectiveWidth() / 2);
					g.drawLine(ap.x + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndY() + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndX() + plannerMap.getTileEffectiveWidth() / 2, 
							zmi.getEndY() + plannerMap.getTileEffectiveWidth() / 2);
				}
				
				g.drawRect(zmi.getEndX(), zmi.getEndY(), plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());
			}
		}

		g.setColor(Color.white);
		g.drawRect(mouseX, mouseY, plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight());

		if (cameraLocation != null)
		{
			g.drawRect(cameraLocation.x, cameraLocation.y, 320, 240);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	public void setSelectedActor(int actor)
	{
		selectedActor = actor;
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent m) {

		if (m.getButton() == MouseEvent.BUTTON1)
		{
			MapObject selected = null;
			int size = Integer.MAX_VALUE;
			boolean foundActor = false;

			int i = 0;
			for (Point al : actorLocations)
			{
				Rectangle r = new Rectangle(al, new Dimension(plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight()));
				if (r.contains(m.getPoint()))
				{
					mapPanel.setActorSelected(i);
					foundActor = true;
					break;
				}
				i++;
			}

			if (!foundActor)
			{
				mapPanel.setActorSelected(-1);

				if (PlannerFrame.SHOW_CIN_LOCATION)
				{
					for (MapObject mo : plannerMap.getMapObjects())
					{
						if (mo.getShape().contains(m.getX(), m.getY()))
						{
							int newSize = mo.getWidth() * mo.getHeight();

							if (newSize < size)
							{
								selected = mo;
								size = newSize;
							}
						}
					}
				}
			}

			if (selected != null)
			{
				this.selectedMO = selected;
				mapPanel.locationClicked(selectedMO);
			}
			else
			{
				this.selectedMO = null;
			}
		}
		else if (m.getButton() == MouseEvent.BUTTON3)
		{
			if (selectedActor == -1)
				systemPopup.show(this, m.getX(), m.getY());
			else
			{
				if ((new Rectangle(actorLocations.get(selectedActor), new Dimension(plannerMap.getTileEffectiveWidth(), plannerMap.getTileEffectiveHeight()))).contains(m.getPoint()))
				{
					popupType = 1;
					actorPopup.show(this, m.getX(), m.getY());
				}
				else
				{
					popupType = 2;
					actorMovePopup.show(this, m.getX(), m.getY());
				}
			}
		}
		else if (m.getButton() == MouseEvent.BUTTON2)
		{
			mapPanel.middleButtonPushed(m);
		}

		System.out.println(m.getButton());

		this.repaint();
	}

	public PlannerMap getPlannerMap() {
		return plannerMap;
	}

	public MapObject getSelectedMO() {
		return selectedMO;
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	public CinematicTimeline getTimeline() {
		return timeline;
	}

	public long loadCinematicItem(int index)
	{
		long maxTime = 0;
		this.actorLocations.clear();

		PlannerTab pt = mapPanel.getPlannerFrame().getPlannerTabAtIndex(PlannerFrame.TAB_CIN);
		pt.setSelectedListItem(index, null);
		currentPC = pt.getCurrentPC();

		try
		{
			ArrayList<PlannerContainer> pcs = new ArrayList<PlannerContainer>();
			if (currentPC != null)
				pcs.add(currentPC);
			ArrayList<String> results = PlannerIO.export(pcs, "cinematics");

			ArrayList<TagArea> tas = XMLParser.process(results, true);
			if (tas.size() > 0)
			{
				ArrayList<CinematicEvent> initEvents = new ArrayList<CinematicEvent>();
				ArrayList<CinematicEvent> ces = TacticalGame.TEXT_PARSER.parseCinematicEvents(tas.get(0), initEvents,
						new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
				ces.addAll(0, initEvents);
				timeline = new CinematicTimeline();
				new PlannerTimeBarViewer(ces, timeline, Integer.parseInt(tas.get(0).getParams().get("camerax")), Integer.parseInt(tas.get(0).getParams().get("cameray")));
				maxTime = timeline.duration;
			}

			mapPanel.stateChanged(null);

		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"An error occurred while parsing the cinematics, if you have just edited or added a new\n"
					+ "event make sure that you have filled out all of the values. If this does not fix the\n"
					+ "the problem then it is possible that your cinematic file has been corrupted." );
			ex.printStackTrace();
		}
		return maxTime;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = (e.getX() / (plannerMap.getTileRenderWidth() * 2) * (plannerMap.getTileRenderWidth() * 2));
		mouseY = (e.getY() / (plannerMap.getTileRenderHeight() * 2) * (plannerMap.getTileRenderHeight() * 2));
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Add Actor") ||
				e.getActionCommand().equalsIgnoreCase("Add Static Sprite") ||
				e.getActionCommand().equalsIgnoreCase("Camera Pan"))
			mapPanel.addCinematicLineByName(e.getActionCommand(), -1, mouseX, mouseY);
		else if (popupType != 2)
			mapPanel.addCinematicLineByName(e.getActionCommand(), selectedActor, -1, -1);
		else
			mapPanel.addCinematicLineByName(e.getActionCommand(), selectedActor, mouseX, mouseY);

		popupType = 0;
	}

	public void setCameraLocation(Point cameraLocation) {
		this.cameraLocation = cameraLocation;
	}

	public PlannerContainer getCurrentPC() {
		return currentPC;
	}
}
