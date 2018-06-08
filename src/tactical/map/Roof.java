package tactical.map;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

public class Roof 
{
	private Shape locationShape;
	private Shape roofShape;
	private boolean isVisible;
	
	public Roof(Shape shape) {
		super();
		this.locationShape = shape;
		isVisible = true;
	}
	
	public void determineRoofShape(MapLayer roofLayer, int renderTileWidth, int renderTileHeight) {
		int xTileMin = (int) (locationShape.getMinX() / renderTileWidth);
		int xTileMax = (int) (locationShape.getMaxX() / renderTileWidth);
		int scanYTile = (int) (locationShape.getMinY() / renderTileHeight) - 1;
		
		yScanLoop: for (; scanYTile >= 0; scanYTile--) {
			for (int xIndex = xTileMin; xIndex < xTileMax; xIndex++) {
				if (roofLayer.getTiles()[scanYTile][xIndex] != 0)
					continue yScanLoop;
			}
			break;
		}
		
		scanYTile++;
		
		roofShape = new Rectangle(locationShape.getMinX(), scanYTile * renderTileHeight, 
				locationShape.getWidth(), locationShape.getMaxY() - (scanYTile * renderTileHeight));
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public Shape getLocationShape() {
		return locationShape;
	}

	public Shape getRoofShape() {
		return roofShape;
	}
}
