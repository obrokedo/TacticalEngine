package tactical.loading;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.JPanel;

import tactical.map.Map;
import tactical.map.MapObject;
import tactical.utils.XMLParser.TagArea;

public class PlannerMap extends Map {
	private final Color UNSELECTED_MO_FILL_COLOR = new Color(0, 0, 255, 50);
	private final Color UNSELECTED_MO_LINE_COLOR = new Color(0, 0, 255);

	private final Color SELECTED_MO_FILL_COLOR = new Color(0, 255, 0, 50);
	private final Color SELECTED_MO_LINE_COLOR = new Color(0, 255, 0);
	private Hashtable<MapObject, TagArea> tagAreaByMapObject = new Hashtable<>();
	private TagArea rootTagArea;
	private String mapName;

	public PlannerMap(String mapName) {
		super();
		this.mapName = mapName;
	}

	public void addTileset(Image[] sprites, int tileStartIndex,
			int tileWidth, int tileHeight,
			Hashtable<Integer, Integer> landEffectByTileId) {

		this.landEffectByTileId.putAll(landEffectByTileId);
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.tileSets.add(new PlannerTileSet(sprites, tileStartIndex));
		Collections.sort(tileSets, new TileSetComparator());
	}

	public class PlannerTileSet extends TileSet
	{
		private Image[] sprites;

		public PlannerTileSet(Image[] sprites, int startIndex) {
			super(null, startIndex);
			this.sprites = sprites;
		}

		public Image getPlannerSprite(int index) {
			return sprites[index - startIndex];
		}
	}

	public Image getPlannerSprite(int index)
	{
		for (TileSet ts : tileSets)
			if (index >= ts.getStartIndex())
				return ((PlannerTileSet) ts).getPlannerSprite(index);
		return null;
	}

	public void renderMap(Graphics g, JPanel panel)
	{
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
		for (int i = 0; i < getMapWidth(); i++)
		{
			for (int j = 0; j < getMapHeight(); j++)
			{
				for (int k = 0; k < 5; k++)
				{
					if (k == 0 || getMapLayer(k).getTiles()[j][i] != 0)
						g.drawImage(getPlannerSprite(getMapLayer(k).getTiles()[j][i]), i * getTileRenderWidth(), j * getTileRenderHeight(), panel);
				}
			}
		}
	}

	public void renderMapLocations(Graphics g, MapObject selectedMO)
	{
		renderMapLocations(g, selectedMO, true, true, true, true);
	}

	public void renderMapLocations(Graphics g, MapObject selectedMO,
			boolean displayEnemy, boolean displayOther, boolean displayTerrain,
			boolean displayUnused)
	{
		for (MapObject mo : getMapObjects())
		{
			if (mo.getKey() == null || mo.getKey().length() == 0)
			{
				if (!displayUnused)
					continue;
			}
			else
			{
				if (mo.getKey().equalsIgnoreCase("enemy") && !displayEnemy)
					continue;
				if (mo.getKey().equalsIgnoreCase("terrain") && !displayTerrain)
					continue;
				else if (!mo.getKey().equalsIgnoreCase("enemy") &&
						!mo.getKey().equalsIgnoreCase("battletrigger")
						&& !mo.getKey().equalsIgnoreCase("terrain") && !mo.getKey().equalsIgnoreCase("trigger")
						&& !displayOther)
					continue;
			}

			int[] xP, yP;
			xP = new int[mo.getShape().getPointCount()];
			yP = new int[mo.getShape().getPointCount()];
			for (int i = 0; i < xP.length; i++)
			{
				xP[i] = (int) mo.getShape().getPoint(i)[0];
				yP[i] = (int) mo.getShape().getPoint(i)[1];
			}
			
			String name = mo.getName();

			if (mo != selectedMO)
				g.setColor(UNSELECTED_MO_FILL_COLOR);
			else
				g.setColor(SELECTED_MO_FILL_COLOR);
			g.fillPolygon(xP, yP, xP.length);

			if (mo != selectedMO)
				g.setColor(UNSELECTED_MO_LINE_COLOR);
			else
				g.setColor(SELECTED_MO_LINE_COLOR);
			
			g.drawPolygon(xP, yP, xP.length);
			if (name != null)
			{
				g.setColor(Color.white);
				g.drawString(name, xP[0] + 5, yP[0] + 15);
			}
		}
	}

	public void addMapObject(MapObject mo, TagArea ta) {
		this.mapObjects.add(mo);
		tagAreaByMapObject.put(mo, ta);
	}

	public String outputNewMap()
	{
		TagArea newRootTA = new TagArea(this.rootTagArea);

		for (MapObject mo : this.mapObjects)
		{
			TagArea childTA = getNewChild(mo);

			for (int i = 0; i < newRootTA.getChildren().size(); i++)
			{
				TagArea rootChildTA = newRootTA.getChildren().get(i);
				// If the child is contained then we want to make a copy of this TagArea
				// because the shallow copy we did for the root does not extend into its'
				// children. So modifying this without a copy would mess up the original TAs
				if (rootChildTA.getChildren().contains(tagAreaByMapObject.get(mo)))
				{
					// Create the new object
					rootChildTA = new TagArea(rootChildTA);

					rootChildTA.getChildren().remove(tagAreaByMapObject.get(mo));
					rootChildTA.getChildren().add(childTA);
					newRootTA.getChildren().remove(i);
					newRootTA.getChildren().add(i, rootChildTA);
					break;
				}
			}
		}

		return newRootTA.getOriginalText();
	}

	private TagArea getNewChild(MapObject mo)
	{
		TagArea ta = new TagArea(tagAreaByMapObject.get(mo));

		for (int i = 0; i < ta.getChildren().size(); i++)
		{
			if (ta.getChildren().get(i).getTagType().equalsIgnoreCase("properties"))
			{
				ta.getChildren().remove(i);
				break;
			}
		}


		try {
			TagArea propTA = new TagArea("<properties>");
			String generatedProperty = "<property name=\"" + mo.getKey() + "\" value=\"";

			if (mo.getParams().size() > 0)
			{
				for (Entry<String, String> param : mo.getParams().entrySet())
				{
					generatedProperty = generatedProperty + param.getKey() + "=" + param.getValue() + " ";
				}
			}

			generatedProperty = generatedProperty.trim();

			generatedProperty = generatedProperty + "\"/>";
			propTA.getChildren().add(new TagArea(generatedProperty));
			ta.getChildren().add(0, propTA);
		} catch (IOException e) {
		}

		return ta;
	}

	public void setRootTagArea(TagArea rootTagArea) {
		this.rootTagArea = rootTagArea;
	}

	public String getMapName() {
		return mapName;
	}

	@Override
	public void intializeRoofs() {}
	
	
}
