package tactical.utils.planner.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.UIUtils;

public class MapEditorRenderPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener
{
	private static final long serialVersionUID = 1L;

	private PlannerMap plannerMap;
	private MapObject selectedMapObject = null;
	private MapEditorPanel parentPanel;
	private ArrayList<PlannerTab> tabsWithMapReferences;
	private ArrayList<Point> creatingShapePoints = new ArrayList<>();
	private boolean creatingShape = false, creatingStamp = false;
	private Point lastMouse;
	private float scale = 1.0f;
	private int dragStartX, dragStartY = 0;

	public MapEditorRenderPanel(MapEditorPanel parentPanel)
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.parentPanel = parentPanel;
		
		UIUtils.addWindowKeyListener(this, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), () -> deleteLocation(), "delete");
		UIUtils.addWindowKeyListener(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), () -> stopMakingLocation(), "stopcreate");		
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (plannerMap != null)
		{
			plannerMap.renderMap(g, this, scale);
			
			if (creatingShape || creatingStamp) {
				for (int i = 0; i < plannerMap.getMapWidth(); i++)
				{			
					g.setColor(Color.BLACK);
					if (i % ((int) plannerMap.getTileRatio()) == 0)
						g.drawLine((int) (i * plannerMap.getTileRenderWidth() * scale), 0, (int) (i * plannerMap.getTileRenderWidth() * scale), (int) (plannerMap.getMapHeightInPixels() * scale));
				}
				
				for (int j = 0; j < plannerMap.getMapHeight(); j += (int) plannerMap.getTileRatio())
				{
					g.drawLine(0, (int) (scale * j * plannerMap.getTileRenderHeight()), (int) (scale * plannerMap.getMapWidthInPixels()), (int) (scale * j * plannerMap.getTileRenderHeight()));
				}
			}
			
			plannerMap.renderMapLocations(g, selectedMapObject,
					parentPanel.isDisplayEnemy(), parentPanel.isDisplayOther(), parentPanel.isDisplayTerrain(),
					parentPanel.isDisplayUnused(), parentPanel.isDisplayInteractable(), scale);
			
			if (creatingShape) {
				if (creatingShapePoints.size() == 0) {
					g.setColor(Color.YELLOW);
					g.fillOval((int) (scale * lastMouse.x - 5), (int) (scale * lastMouse.y - 5), 
							(int) (10), (int) (10));
				} else {
					Point p = null;
					for (int i = 0; i < creatingShapePoints.size(); i++) {
						p = creatingShapePoints.get(i);
						g.setColor(Color.YELLOW);
						g.fillOval((int) (scale * p.x - 5), (int) (scale * p.y - 5), 
								(int) ( 10), (int) (10));
						if (i != 0) {
							Point p2 = creatingShapePoints.get(i - 1);
							g.drawLine((int) (scale * p.x - 1), (int) (scale * p.y - 1), (int) (scale * p2.x - 1), (int) (scale * p2.y - 1));
							g.drawLine((int) (scale * p.x + 1), (int) (scale * p.y + 1), (int) (scale * p2.x + 1), (int) (scale * p2.y + 1));
							g.drawLine((int) (scale * p.x), (int) (scale * p.y), (int) (scale * p2.x), (int) (scale * p2.y));
						}
					}
					
					g.drawLine((int) (scale * p.x - 1), (int) (scale * p.y - 1), (int) (scale * lastMouse.x - 1), (int) (scale * lastMouse.y - 1));
					g.drawLine((int) (scale * p.x + 1), (int) (scale * p.y + 1), (int) (scale * lastMouse.x + 1), (int) (scale * lastMouse.y + 1));
					g.drawLine((int) (scale * p.x), (int) (scale * p.y), (int) (scale * lastMouse.x), (int) (scale * lastMouse.y));
				}
			} else if (creatingStamp) {
				g.setColor(Color.YELLOW);
				g.fillRect((int) (scale * lastMouse.x), (int) (scale * lastMouse.y), 
						(int) (scale * plannerMap.getTileEffectiveWidth()), (int) (scale * plannerMap.getTileEffectiveHeight()));
			}
		}
	}

	public void setPlannerMap(PlannerMap plannerMap) {
		this.plannerMap = plannerMap;
		this.setPreferredSize(new Dimension(plannerMap.getMapWidthInPixels(), plannerMap.getMapHeightInPixels()));
		selectedMapObject = null;
	}
	
	public void setTabsWithMapReferences(ArrayList<PlannerTab> tabsWithMapReferences) {
		this.tabsWithMapReferences = tabsWithMapReferences;
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

	@Override
	public void mousePressed(MouseEvent m) {
		if (m.getButton() == MouseEvent.BUTTON1 || m.getButton() == MouseEvent.BUTTON3)
		{
			if (!creatingShape && !creatingStamp) {
				MapObject selected = null;
				int size = Integer.MAX_VALUE;
	
				if (PlannerFrame.SHOW_CIN_LOCATION)
				{
					for (MapObject mo : plannerMap.getMapObjects())
					{
						boolean interactable = plannerMap.isInteractableMapObject(mo);
						
						if (plannerMap.isMapObjectFilteredOut(mo, 
								parentPanel.isDisplayEnemy(), parentPanel.isDisplayOther(), parentPanel.isDisplayTerrain(),
								parentPanel.isDisplayUnused(), parentPanel.isDisplayInteractable(), interactable))
							continue;					
	
						if (mo.getShape().contains(m.getX() / scale, m.getY() / scale))
						{
							int newSize = mo.getWidth() * mo.getHeight();
	
							if (newSize < size)
							{
								selected = mo;
								size = newSize;
							}
						}
					}
					
					if (m.getClickCount() == 2 & selected != null) {
						parentPanel.editMapObject();
					}
				}
	
				if (selected != null)
				{
					this.selectedMapObject = selected;
					this.dragStartX = (int) (m.getX() / scale);
					this.dragStartY = (int) (m.getY() / scale);
					try
					{
						parentPanel.mouseDown(selectedMapObject);
					}
					catch (IllegalArgumentException ex)
					{
						JOptionPane.showMessageDialog(this, "This location was unable to be opened because it references triggers that are not currently open.\n"
								+ "check to make sure that you have the correct text file open before trying again. Remember that battle triggers may use\n"
								+ "a different text file then normal triggers.");
					}
				}
				else
				{
					this.selectedMapObject = null;
				}
	
				this.repaint();
	
				if (m.getButton() == MouseEvent.BUTTON3) {
					if (plannerMap.isInteractableMapObject(selectedMapObject)) {				
						parentPanel.getPlannerFrame().getUnifiedViewPanel().setMoToEdit(selectedMapObject);
						parentPanel.getPlannerFrame().setSelectedTabIndex(PlannerFrame.TAB_UNIFIED_VIEW);
					}
						
				}
			}
			else if (creatingShape) {
				creatingShapeClicked(m);
			} else if (creatingStamp) {
				if (m.getButton() == MouseEvent.BUTTON1) {
					MapObject stampMO = this.parentPanel.getStampMapObject();
					MapObject mo = new MapObject();
					mo.setKey(stampMO.getKey());
					
					for (Entry<String, String> param : stampMO.getParams().entrySet()) {
						mo.getParams().put(param.getKey(), param.getValue());
					}
					
					int count = 0;
					outer: while (mo.getName() == null) {
						for (MapObject existingMO : plannerMap.getMapObjects()) {
							if (existingMO.getName() != null && existingMO.getName().equalsIgnoreCase(mo.getKey() + count)) {
								count++;
								continue outer;
							}
						}
						mo.setName(mo.getKey() + count);
					}
					
					mo.setX(lastMouse.x);
					mo.setY(lastMouse.y);
					mo.setWidth(plannerMap.getTileEffectiveWidth());
					mo.setHeight(plannerMap.getTileEffectiveHeight());
					mo.determineShape();
					
					String tagAreaText = "<object name=\"" + mo.getName() + "\" x=\"0\" y=\"0\">";
					TagArea tagArea = new TagArea(tagAreaText);
					tagAreaText = "<polyline points=\"";
					for (int i = 0; i < mo.getShape().getPointCount(); i++) {
						tagAreaText += ((int) mo.getShape().getPoint(i)[0]) + "," + ((int) mo.getShape().getPoint(i)[1]);
						if (i != mo.getShape().getPointCount() - 1) 
							tagAreaText += " ";
					}
					tagAreaText += "\"/>";
					tagArea.getChildren().add(new TagArea(tagAreaText));
					
					plannerMap.addMapObject(mo, tagArea);
					
				} else if (m.getButton() == MouseEvent.BUTTON3) {
					this.stopStamping();
					this.repaint();
				}
			}
		}
	}
	
	private void creatingShapeClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) { 
			Point p = new Point(Math.round(e.getX() /scale / (float) plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth(), 
					Math.round(e.getY() / scale / (float) plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight());
			creatingShapePoints.add(p);			
			if (creatingShapePoints.size() > 2 && p.x == creatingShapePoints.get(0).x && 
					p.y == creatingShapePoints.get(0).y) {			
				this.creatingShape = false;
				String name = null;
				do
				{
					name = JOptionPane.showInputDialog("What is the name of this new location (Cannot be empty)?");
					if (name == null) {
						this.repaint();
						return;
					}
					
					name = name.trim();
					
					for (MapObject mapObjectCheckForDups : plannerMap.getMapObjects()) {
						if (name.equalsIgnoreCase(mapObjectCheckForDups.getName())) {
							name = "";
							JOptionPane.showMessageDialog(this, "A location with that name already exists");
						}
					}
					
				} while (name.length() == 0);
				
				MapObject mo = new MapObject();
				mo.setPolyPoints(creatingShapePoints);
				String tagAreaText = "<object name=\"" + name + "\" x=\"0\" y=\"0\">";
				TagArea tagArea = new TagArea(tagAreaText);
				tagAreaText = "<polyline points=\"";
				for (int i = 0; i < creatingShapePoints.size(); i++) {
					tagAreaText += creatingShapePoints.get(i).x + "," + creatingShapePoints.get(i).y;
					if (i != creatingShapePoints.size() - 1) 
						tagAreaText += " ";
				}
				tagAreaText += "\"/>";
				tagArea.getChildren().add(new TagArea(tagAreaText));
				mo.determineShape();
				mo.setName(name);
				mo.setKey("");
				plannerMap.addMapObject(mo, tagArea);
				this.repaint();
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (creatingShapePoints.size() > 0) {
				creatingShapePoints.remove(creatingShapePoints.size() - 1);
				this.repaint();
			} else {
				this.creatingShape = false;
				this.repaint();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	public MapObject getSelectedMapObject() {
		return selectedMapObject;
	}
	
	public void startCreatingLocation() {
		this.creatingShapePoints = new ArrayList<>();
		this.creatingShape = true;
	}
	
	public void startStamping() {
		this.creatingStamp = true;
	}
	
	public void stopStamping() {
		this.creatingStamp = false;
	}
	
	public boolean deleteLocation() {
		if (selectedMapObject != null) {
			int res = JOptionPane.showConfirmDialog(this, 
					"Are you sure you want to delete the selected Map Location: " + selectedMapObject.getName(), 
					"Delete Map Location", JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				plannerMap.removeMapObject(selectedMapObject);
				selectedMapObject = null;
				this.repaint();
				return true;
			}
			
		}
		return false;
	}
	
	public boolean stopMakingLocation() {
		if (this.creatingShape) {
			creatingShape = false;
			this.repaint();
			return true;
		}
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (selectedMapObject != null) {		
			Point p = new Point(Math.round(e.getX() / scale), 
						Math.round(e.getY() / scale));
			//selectedMapObject.determineShape();
			int tX = ((p.x - dragStartX) / plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth();
			int tY = ((p.y - dragStartY) / plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight();
			selectedMapObject.translate(tX, tY);
						
			String tagAreaText = "<object name=\"" + selectedMapObject.getName() + "\" x=\"0\" y=\"0\">";
			TagArea tagArea = new TagArea(tagAreaText);
			tagAreaText = "<polyline points=\"";
			for (int i = 0; i < selectedMapObject.getPolyPoints().size(); i++) {
				tagAreaText += selectedMapObject.getPolyPoints().get(i).x + "," + selectedMapObject.getPolyPoints().get(i).y;
				if (i != selectedMapObject.getPolyPoints().size() - 1) 
					tagAreaText += " ";
			}
			tagAreaText += "\"/>";
			tagArea.getChildren().add(new TagArea(tagAreaText));
			plannerMap.updateMapObjectLocation(selectedMapObject, tagArea);
			
			dragStartX += tX;
			dragStartY += tY;
			this.repaint();
			
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (creatingShape) {
			lastMouse = new Point(Math.round(e.getX() /scale / (float) plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth(), 
					Math.round(e.getY() / scale / (float) plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight());
			this.repaint();
		} else if (creatingStamp) {
			lastMouse = new Point((int) Math.floor(e.getX() / scale / (float) plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth(), 
					(int) Math.floor(e.getY() / scale / (float) plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight());
			this.repaint();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if (e.isControlDown()) {
			if (e.getWheelRotation() < 0) {
				scale = Math.min(4, scale + .5f);
			} else {
				scale = Math.max(.5f, scale - .5f);
			}
			
			this.setPreferredSize(new Dimension((int) (plannerMap.getMapWidthInPixels() * scale), (int) (plannerMap.getMapHeightInPixels() * scale)));
			this.repaint();
			this.validate();
			this.getParent().revalidate();
			this.getParent().repaint();
		} else {
			this.getParent().dispatchEvent(e);
		}
		
	}
}
