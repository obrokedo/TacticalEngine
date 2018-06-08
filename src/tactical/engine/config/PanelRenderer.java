package tactical.engine.config;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.loading.ResourceManager;

/**
 * Interface to call the render method that renders the background/borders
 * of all default panels in the game.
 *
 *
 * @author Broked
 *
 */
public interface PanelRenderer
{
	/**
	 * Render the panel with the indicated bounds. If a color is specified render the background of the
	 * panel in that color. If a color is not specified then render the default color.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param graphics
	 * @param color
	 */
	public void render(int x, int y, int width, int height, Graphics graphics, Color color);
	
	/**
	 * Initialize graphic resources required to render the panel
	 * 
	 * @param rm
	 */
	public void initializeResources(ResourceManager rm);
}
