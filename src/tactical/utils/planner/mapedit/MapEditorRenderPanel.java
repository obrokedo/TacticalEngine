package tactical.utils.planner.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerTab;
import tactical.utils.planner.UIUtils;

public class MapEditorRenderPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener
{
	private static final long serialVersionUID = 1L;

	private PlannerMap plannerMap;
	private MapObject selectedMapObject = null;
	private MapEditorPanel parentPanel;
	private ArrayList<PlannerTab> tabsWithMapReferences;
	private ArrayList<Point> creatingShapePoints = new ArrayList<>();
	private boolean creatingShape = false;
	private Point lastMouse;

	public MapEditorRenderPanel(MapEditorPanel parentPanel)
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.parentPanel = parentPanel;
		
		UIUtils.addWindowKeyListener(this, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), () -> deleteLocation(), "delete");
		UIUtils.addWindowKeyListener(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), () -> stopMakingLocation(), "stopcreate");		
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (plannerMap != null)
		{
			plannerMap.renderMap(g, this);
			
			if (creatingShape) {
				for (int i = 0; i < plannerMap.getMapWidth(); i++)
				{			
					g.setColor(Color.BLACK);
					if (i % ((int) plannerMap.getTileRatio()) == 0)
						g.drawLine(i * plannerMap.getTileRenderWidth(), 0, i * plannerMap.getTileRenderWidth(), plannerMap.getMapHeightInPixels());
				}
				
				for (int j = 0; j < plannerMap.getMapHeight(); j += (int) plannerMap.getTileRatio())
				{
					g.drawLine(0, j * plannerMap.getTileRenderHeight(), plannerMap.getMapWidthInPixels(), j * plannerMap.getTileRenderHeight());
				}
			}
			
			plannerMap.renderMapLocations(g, selectedMapObject,
					parentPanel.isDisplayEnemy(), parentPanel.isDisplayOther(), parentPanel.isDisplayTerrain(),
					parentPanel.isDisplayUnused(), parentPanel.isDisplayInteractable());
			
			if (creatingShape) {
				if (creatingShapePoints.size() == 0) {
					g.setColor(Color.YELLOW);
					g.fillOval(lastMouse.x - 5, lastMouse.y - 5, 10, 10);
				} else {
					Point p = null;
					for (int i = 0; i < creatingShapePoints.size(); i++) {
						p = creatingShapePoints.get(i);
						g.setColor(Color.YELLOW);
						g.fillOval(p.x - 5, p.y - 5, 10, 10);
						if (i != 0) {
							Point p2 = creatingShapePoints.get(i - 1);
							g.drawLine(p.x - 1, p.y - 1, p2.x - 1, p2.y - 1);
							g.drawLine(p.x + 1, p.y + 1, p2.x + 1, p2.y + 1);
							g.drawLine(p.x, p.y, p2.x, p2.y);
						}
					}
					
					g.drawLine(p.x - 1, p.y - 1, lastMouse.x - 1, lastMouse.y - 1);
					g.drawLine(p.x + 1, p.y + 1, lastMouse.x + 1, lastMouse.y + 1);
					g.drawLine(p.x, p.y, lastMouse.x, lastMouse.y);
				}
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
			if (!creatingShape) {
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
					
					if (m.getClickCount() == 2 & selected != null) {
						parentPanel.editMapObject();
					}
				}
	
				if (selected != null)
				{
					this.selectedMapObject = selected;
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
			else {
				creatingShapeClicked(m);
			}
		}
	}
	
	private void creatingShapeClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) { 
			Point p = new Point(Math.round(e.getX() / (float) plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth(), 
					Math.round(e.getY() / (float) plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight());
			creatingShapePoints.add(p);			
			if (creatingShapePoints.size() > 2 && p.x == creatingShapePoints.get(0).x && 
					p.y == creatingShapePoints.get(0).y) {			
				this.creatingShape = false;
				String name = JOptionPane.showInputDialog("What is the name of this new location?");
				if (name == null) {
					this.repaint();
					return;
				}
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (creatingShape) {
			lastMouse = new Point(Math.round(e.getX() / (float) plannerMap.getTileEffectiveWidth()) * plannerMap.getTileEffectiveWidth(), 
					Math.round(e.getY() / (float) plannerMap.getTileEffectiveHeight()) * plannerMap.getTileEffectiveHeight());
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
		// TODO Auto-generated method stub
		
	}
}
