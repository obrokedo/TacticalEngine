package tactical.utils.planner.mapedit;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tactical.loading.PlannerMap;
import tactical.map.MapObject;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerTab;

public class MapEditorRenderPanel extends JPanel implements MouseListener
{
	private static final long serialVersionUID = 1L;

	private PlannerMap plannerMap;
	private MapObject selectedMapObject = null;
	private MapEditorPanel parentPanel;
	private ArrayList<PlannerTab> tabsWithMapReferences;

	public MapEditorRenderPanel(MapEditorPanel parentPanel)
	{
		this.addMouseListener(this);
		this.parentPanel = parentPanel;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (plannerMap != null)
		{
			plannerMap.renderMap(g, this);
			plannerMap.renderMapLocations(g, selectedMapObject,
					parentPanel.isDisplayEnemy(), parentPanel.isDisplayOther(), parentPanel.isDisplayTerrain(),
					parentPanel.isDisplayUnused(), parentPanel.isDisplayInteractable());
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
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	public MapObject getSelectedMapObject() {
		return selectedMapObject;
	}
}
