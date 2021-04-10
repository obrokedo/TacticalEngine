package tactical.loading;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.game.constants.Direction;
import tactical.game.exception.BadAnimationException;
import tactical.map.Map;
import tactical.map.MapLayer;
import tactical.map.MapObject;
import tactical.utils.Animation;
import tactical.utils.DirectoryLister;
import tactical.utils.ImageUtility;
import tactical.utils.SpriteAnims;
import tactical.utils.XMLParser.TagArea;
import tactical.utils.planner.PlannerContainer;
import tactical.utils.planner.PlannerFrame;
import tactical.utils.planner.PlannerLine;
import tactical.utils.planner.PlannerReference;
import tactical.utils.planner.PlannerTab;

public class PlannerMap extends Map {
	private final Color UNSELECTED_MO_FILL_COLOR = new Color(0, 0, 255, 50);
	private final Color UNSELECTED_MO_LINE_COLOR = new Color(0, 0, 255);
	
	private final Color HOVERED_MO_LINE_COLOR = new Color(255, 0, 0);

	private final Color SELECTED_MO_FILL_COLOR = new Color(0, 255, 0, 50);
	private final Color SELECTED_MO_LINE_COLOR = new Color(0, 255, 0);
	private Hashtable<MapObject, TagArea> tagAreaByMapObject = new Hashtable<>();
	private TagArea rootTagArea;
	private String mapName;
	private ArrayList<PlannerTab> tabsWithMapReferences;
	private ArrayList<PlannerReference> locationReferences;
	private Hashtable<String, BufferedImage> imagesByName = new Hashtable<>();
	private PlannerTab enemyTab;

	public PlannerMap(String mapName, ArrayList<PlannerReference> locationReferences, PlannerTab enemyTab) {
		super();
		this.mapName = mapName;
		this.locationReferences = locationReferences;
		this.enemyTab = enemyTab;
	}
	
	public boolean validateLayers() {
		TagArea newRootTA = new TagArea(this.rootTagArea);

		for (int i = 0; i < newRootTA.getChildren().size(); i++)
		{
			TagArea rootChildTA = newRootTA.getChildren().get(i);
			// We want to make a copy of this TagArea
			// because the shallow copy we did for the root does not extend into its'
			// children. So modifying this without a copy would mess up the original TAs
			if ("objectgroup".equalsIgnoreCase(rootChildTA.getTagType())) {				
				if (!("meta".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"npcs-objects".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"battle".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"terrain".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"trigger regions".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"trigger region".equalsIgnoreCase(rootChildTA.getParams().get("name")))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void loadSprites() {		
		loadImagesFromDir("sprite", ".png");
		loadAnimations();		
	}
	
	private void loadAnimations(){
		for (File file : DirectoryLister.listFilesInDir("animations/animationsheets"))
		{
			try {
				if (file.getName().endsWith(".anim")) {
					Log.debug(file.getName());
					SpriteAnims sa = TacticalGame.ENGINE_CONFIGURATIOR.getAnimationParser().
							parseAnimations(file.getPath());
					BufferedImage spriteSheet = ImageUtility.loadBufferedImage("animations/animationsheets/" + sa.getSpriteSheet() + ".png");
					Animation anim = null;
					try {
						anim = sa.getDirectionAnimation(Direction.DOWN);
					} catch (BadAnimationException ex) {
						anim = sa.getAnimation("Down");
					}
									
					if (anim != null) {
						Rectangle bounds = sa.imageLocs.get(anim.frames.get(0).sprites.get(0).imageIndex);
						imagesByName.put(file.getName().replaceFirst(".anim", ""), spriteSheet.getSubimage(
								(int) bounds.getX(), (int) bounds.getY(), 
								(int) bounds.getWidth(), (int) bounds.getHeight()));
					}			
				}
			} catch (Exception ex) {
			}
		}
	}
	
	private void loadImagesFromDir(String path, String ext) {
		for (File f : DirectoryLister.listFilesInDir(path)) {
			String name = f.getName();
			if (f.getName().endsWith(ext)) {
				imagesByName.put(name.replaceFirst(ext, ""), ImageUtility.loadBufferedImage(path + "/" + name));
			}
		}
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
			if (index == 1610612957)
				return null;
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

	public void renderMap(Graphics g, JPanel panel, float scale)
	{
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
		for (int i = 0; i < getMapWidth(); i++)
		{
			for (int j = 0; j < getMapHeight(); j++)
			{
				for (int k = 0; k < 5; k++)
				{
					if (k == 0 || getMapLayer(k).getTiles()[j][i] != 0)	{			
						g.drawImage(getPlannerSprite(getMapLayer(k).getTiles()[j][i]), 
								(int) (scale * i * getTileRenderWidth()), (int) (scale * j * getTileRenderHeight()), 
								(int) (scale * (i + 1) * getTileRenderWidth()), (int) (scale * (j + 1) * getTileRenderHeight()),
								0, 0, getTileRenderWidth(), getTileRenderHeight(), panel);
						
					}
				}				
			}
			
		}
	}

	public void renderMapLocations(Graphics g, MapObject selectedMO, float scale)
	{
		renderMapLocations(g, selectedMO, null, true, true, true, true, true, scale);
	}

	public void renderMapLocations(Graphics g, MapObject selectedMO, MapObject hoveredMO,
			boolean displayEnemy, boolean displayOther, boolean displayTerrain,
			boolean displayUnused, boolean displayInteractables, float scale)
	{
		for (MapObject mo : getMapObjects())
		{
			String name = mo.getName();
			
			boolean interactable = isInteractableMapObject(mo);
			
			if (isMapObjectFilteredOut(mo, displayEnemy, displayOther, displayTerrain, 
					displayUnused, displayInteractables, interactable))
				continue;
			
			

			int[] xP, yP;
			xP = new int[mo.getShape().getPointCount()];
			yP = new int[mo.getShape().getPointCount()];
			for (int i = 0; i < xP.length; i++)
			{
				xP[i] = (int) (mo.getShape().getPoint(i)[0] * scale);
				yP[i] = (int) (mo.getShape().getPoint(i)[1] * scale);
			}
						
			if (mo != selectedMO) {
				if (interactable)
					g.setColor(new Color(255, 128, 0, 150));
				else
					g.setColor(UNSELECTED_MO_FILL_COLOR);
				
			}
			else
				g.setColor(SELECTED_MO_FILL_COLOR);
			g.fillPolygon(xP, yP, xP.length);

			if (mo == selectedMO)
				g.setColor(SELECTED_MO_LINE_COLOR);
			else if (mo == hoveredMO)
				g.setColor(HOVERED_MO_LINE_COLOR);
			else
				g.setColor(UNSELECTED_MO_LINE_COLOR);
			
			g.drawPolygon(xP, yP, xP.length);
			
			String im = null;
			if (mo.getKey().equalsIgnoreCase("door")) {
				im = mo.getParam("image");				
			} else if (mo.getKey().equalsIgnoreCase("npc")) {			
				im = mo.getParam("animation");
			} else if (mo.getKey().equalsIgnoreCase("chest")) {
				im = mo.getParam("spriteimage");
			} else if (mo.getKey().equalsIgnoreCase("enemy")) {
				if ((im = mo.getParam("enemyid")) != null) {
					PlannerContainer pc = enemyTab.getPlannerContainerByReference(new PlannerReference(im));
					if (pc.getDefLine().getValues().size() > 23) {
						im = ((PlannerReference) pc.getDefLine().getValues().get(23)).getName();
					}
				}
			}
			
			if (im != null)
				renderMapObjectImage(im, mo, scale, g);
			
			if (name != null)
			{
				g.setColor(Color.white);
				g.drawString(name, xP[0] + 5, yP[0] + 15);
			}
		}
	}
	
	private void renderMapObjectImage(String im, MapObject mo, float scale, Graphics g) {
		Image i = imagesByName.get(im); 
		if (i != null) {
			g.drawImage(i.getScaledInstance((int) (i.getWidth(null) * scale), (int) (i.getWidth(null) * scale), 0), 
				(int) (mo.getX() * scale), (int) (mo.getY() * scale), null);
		}
	}
	
	public boolean isMapObjectFilteredOut(MapObject mo, 
			boolean displayEnemy, boolean displayOther, boolean displayTerrain,
			boolean displayUnused, boolean displayInteractables, boolean interactable) {
		if (mo.getKey() == null || mo.getKey().length() == 0)
		{
			if (!displayUnused && (!interactable || !displayInteractables))
				return true;
		}
		else
		{	
			if (mo.getKey().equalsIgnoreCase("enemy") && !displayEnemy)
				return true;
			if (mo.getKey().equalsIgnoreCase("terrain") && !displayTerrain)
				return true;
			else if (!mo.getKey().equalsIgnoreCase("enemy") &&
					!mo.getKey().equalsIgnoreCase("battletrigger")
					&& !mo.getKey().equalsIgnoreCase("terrain") && 
					!mo.getKey().equalsIgnoreCase("trigger") && 
					!interactable
					&& !displayOther)
				return true;
			
			
		}
		
		if (interactable && !displayInteractables)
			return true;
		return false;
	}

	public boolean isInteractableMapObject(MapObject mo) {
		boolean interactable = false;
		if (mo == null)
			return false;
		
		if (mo.getName() != null) {
			if (getPCReferencingMapObject(mo) != null)
				interactable = true;
		}
		
		if ("npc".equalsIgnoreCase(mo.getKey()) || "searcharea".equalsIgnoreCase(mo.getKey())) {
			interactable = true;
		}
		return interactable;
	}
	
	public PlannerContainer getPCReferencingMapObject(MapObject mo) {
		for (PlannerContainer pc : tabsWithMapReferences.get(PlannerFrame.TAB_CONDITIONS).getListPC()) {
			for (PlannerLine pl : pc.getLines()) {
				if ("On Location Enter".equalsIgnoreCase(pl.getPlDef().getName())) {
					if (pl.getValues().size() > 0 && pl.getValues().get(0) instanceof PlannerReference
							&& ((PlannerReference) pl.getValues().get(0)).getName().equalsIgnoreCase(mo.getName())) {
						return pc;
					}
				}
			}
		}
		
		return null;
	}
	
	public void updateMapObjectLocation(MapObject mo, TagArea ta) {
		tagAreaByMapObject.put(mo, ta);
	}

	public void addMapObject(MapObject mo, TagArea ta) {
		this.mapObjects.add(mo);
		tagAreaByMapObject.put(mo, ta);
		
		addMapObjectReference(mo);
	}
	
	private void addMapObjectReference(MapObject mo) {
		if (mo.getKey() == null)
			mo.setKey("");
		
		if (mo.getKey().trim().equalsIgnoreCase("") || mo.getKey().equalsIgnoreCase("searcharea")) {
			if (mo.getName() != null)
				locationReferences.add(new PlannerReference(mo.getName()));
			else
				locationReferences.add(new PlannerReference("Unamed Location"));
		}
	}
	
	public void renameMapObject(String newName, MapObject mo) {
		Optional<PlannerReference> ref = 
				locationReferences.stream().filter(lr -> lr.getName().equalsIgnoreCase(mo.getName())).findFirst();
		
		if (ref.isPresent())
			ref.get().setName(newName);
		mo.setName(newName);
	}
	
	public void updateMapObjectType(MapObject mo, String newType) {
		if (mo.getKey().equalsIgnoreCase(newType))
			return;
		removeMapObjectReference(mo);
		mo.setKey(newType);
		addMapObjectReference(mo);
		mo.getParams().clear();
	}
	
	public void removeMapObject(MapObject mo) {
		this.mapObjects.remove(mo);
		tagAreaByMapObject.remove(mo);
		
		removeMapObjectReference(mo);
	}

	private void removeMapObjectReference(MapObject mo) {
		if (mo.getKey().trim().equalsIgnoreCase("") || mo.getKey().equalsIgnoreCase("searcharea")) {
			Iterator<PlannerReference> refIt = locationReferences.iterator();
			
			while (refIt.hasNext()) {
				PlannerReference ref = refIt.next();
				if (ref.getName().equalsIgnoreCase(mo.getName())) {
					ref.setName("");
					refIt.remove();
					break;
				}
			}
		}
	}

	public String outputNewMap()
	{
		TagArea newRootTA = new TagArea(this.rootTagArea);

		TagArea metaTagArea = null;
		TagArea terrainTagArea = null;
		TagArea battleTagArea = null;
		TagArea triggerRegionsTagArea = null;
		for (int i = 0; i < newRootTA.getChildren().size(); i++)
		{
			TagArea rootChildTA = newRootTA.getChildren().get(i);
			// We want to make a copy of this TagArea
			// because the shallow copy we did for the root does not extend into its'
			// children. So modifying this without a copy would mess up the original TAs
			if ("objectgroup".equalsIgnoreCase(rootChildTA.getTagType())) {				
				if ("meta".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
					"npcs-objects".equalsIgnoreCase(rootChildTA.getParams().get("name"))) {					
					metaTagArea = new TagArea(rootChildTA);
					newRootTA.getChildren().remove(i);
					// newRootTA.getChildren().add(i, metaTagArea);
				} else if ("battle".equalsIgnoreCase(rootChildTA.getParams().get("name"))) { 
					battleTagArea = new TagArea(rootChildTA);
					newRootTA.getChildren().remove(i);
					// newRootTA.getChildren().add(i, battleTagArea);
				} else if ("terrain".equalsIgnoreCase(rootChildTA.getParams().get("name"))) {
					terrainTagArea = new TagArea(rootChildTA);
					newRootTA.getChildren().remove(i);
					// newRootTA.getChildren().add(i, terrainTagArea);
				} else if ("trigger regions".equalsIgnoreCase(rootChildTA.getParams().get("name")) ||
						"trigger region".equalsIgnoreCase(rootChildTA.getParams().get("name"))) {
					triggerRegionsTagArea = new TagArea(rootChildTA);
					newRootTA.getChildren().remove(i);
					// newRootTA.getChildren().add(i, triggerRegionsTagArea);
				} else {
					JOptionPane.showMessageDialog(null, 
							"The loaded map contains Object Layers that do not conform to the map naming schema."
							+ "\nOnly layers named 'Terrain', 'Meta', 'Trigger Region' and 'Battle' are allowed, \n"
							+ "all other layers will be removed upon map export", "Unknown Object Layers Found", JOptionPane.ERROR_MESSAGE);
					newRootTA.getChildren().remove(i);
					i--;
				}
			}
		}
		
		if (metaTagArea == null) {
			metaTagArea = new TagArea("<objectgroup name=\"NPCs-Objects\" visible=\"1\">");
			newRootTA.getChildren().add(metaTagArea);
		}
		if (terrainTagArea == null) {
			terrainTagArea = new TagArea("<objectgroup name=\"Terrain\" visible=\"1\">");
			newRootTA.getChildren().add(terrainTagArea);
		}
		if (battleTagArea == null) {
			battleTagArea = new TagArea("<objectgroup name=\"Battle\" visible=\"1\">");
			newRootTA.getChildren().add(battleTagArea);
		}
		if (triggerRegionsTagArea == null) {
			triggerRegionsTagArea = new TagArea("<objectgroup name=\"Trigger Region\" visible=\"1\">");
			newRootTA.getChildren().add(triggerRegionsTagArea);
		}
		triggerRegionsTagArea.getChildren().clear();
		battleTagArea.getChildren().clear();
		metaTagArea.getChildren().clear();
		terrainTagArea.getChildren().clear();
		
		for (MapObject mo : this.mapObjects)
		{
			TagArea childTA = getNewChild(mo);
			TagArea parentArea;
			
			if ("npc".equalsIgnoreCase(mo.getKey()) || 
					"door".equalsIgnoreCase(mo.getKey()) || 
					"roof".equalsIgnoreCase(mo.getKey()) ||
					"stairs".equalsIgnoreCase(mo.getKey()) ||
					"chest".equalsIgnoreCase(mo.getKey()) || 
					"sprite".equalsIgnoreCase(mo.getKey())) {
				parentArea = metaTagArea;
				battleTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				triggerRegionsTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				terrainTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
			} else if ("enemy".equalsIgnoreCase(mo.getKey()) ||
					"battleregion".equalsIgnoreCase(mo.getKey())) {
				parentArea = battleTagArea;
				metaTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				triggerRegionsTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				terrainTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
			} else if ("terrain".equalsIgnoreCase(mo.getKey())) {
				parentArea = terrainTagArea;
				battleTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				triggerRegionsTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				metaTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
			} else {
				// start, searchtrigger, unlabled
				parentArea = triggerRegionsTagArea;
				battleTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				metaTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
				terrainTagArea.getChildren().remove(tagAreaByMapObject.get(mo));
			}
			
			if (parentArea.getChildren().contains(tagAreaByMapObject.get(mo)))
			{
				parentArea.getChildren().remove(tagAreaByMapObject.get(mo));				
				parentArea.getChildren().add(childTA);
			}
			else
				parentArea.getChildren().add(childTA);
			
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(triggerRegionsTagArea.getOriginalText());
		sb.append("\n");
		sb.append(battleTagArea.getOriginalText());
		sb.append("\n");
		sb.append(metaTagArea.getOriginalText());
		sb.append("\n");
		sb.append(terrainTagArea.getOriginalText());

		return sb.toString();
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

		return ta;
	}

	public Hashtable<String, BufferedImage> getImagesByName() {
		return imagesByName;
	}

	public void setImagesByName(Hashtable<String, BufferedImage> imagesByName) {
		this.imagesByName = imagesByName;
	}

	public void setRootTagArea(TagArea rootTagArea) {
		this.rootTagArea = rootTagArea;
	}

	public String getMapName() {
		return mapName;
	}

	@Override
	public void intializeRoofs() {}

	public void setTabsWithMapReferences(ArrayList<PlannerTab> tabsWithMapReferences) {
		this.tabsWithMapReferences = tabsWithMapReferences;
	}
	
	public void removeReferences(boolean isTrigger, int idx) {
		for (MapObject mos : mapObjects) {
			if (isTrigger && "searcharea".equalsIgnoreCase(mos.getKey())) {
				replaceId("searchtrigger", idx, mos);
			} else if (!isTrigger && "npc".equalsIgnoreCase(mos.getKey())) {
				replaceId("textid", idx, mos);
			}
		}
	}

	private void replaceId(String key, int idx, MapObject mos) {
		if (mos.getParams().containsKey(key)) {
			try {
				int id = Integer.parseInt(mos.getParam(key));
				if (id > idx)
					id--;
				else if (id == idx)
					id = -1;
				mos.getParams().put(key, "" + id);
			} catch (NumberFormatException e) {}
		}
	}
	
	public boolean hasMoveableLayer() {
		return moveableLayer != null;
	}

	@Override
	public void addLayer(String layerName, MapLayer layer) {
		if (getMapLayerAmount() <= 3) {
			if (!layerName.startsWith("BG") && !layerName.startsWith("MG") &&
					!layerName.startsWith("Back") && !layerName.startsWith("Mid") && 
					!layerName.startsWith("bg") && !layerName.startsWith("mg") && 
					!layerName.startsWith("back") && !layerName.startsWith("mid")) {
				JOptionPane.showMessageDialog(null, "The loaded map contains strangely named map layers. Keep in mind that the four bottom layers\n"
						+ "on the map will be rendered behind sprites and any additional layers will be rendered above sprites.\n"
						+ "Expected first four layers BG,BG Shadow,MG Shadow.\n"
						+ "Found layer: " + layerName + " this layer will be rendered BELOW sprites");
			}
		}
		super.addLayer(layerName, layer);
	}
	
	
}
