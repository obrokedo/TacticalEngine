package tactical.loading;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

import tactical.cinematic.Cinematic;
import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurator;
import tactical.engine.state.StateInfo;
import tactical.game.definition.EnemyDefinition;
import tactical.game.definition.HeroDefinition;
import tactical.game.definition.ItemDefinition;
import tactical.game.exception.BadResourceException;
import tactical.game.resource.EnemyResource;
import tactical.game.resource.HeroResource;
import tactical.game.resource.ItemResource;
import tactical.game.text.Speech;
import tactical.game.trigger.Trigger;
import tactical.game.trigger.TriggerCondition;
import tactical.map.Map;
import tactical.utils.DirectoryLister;
import tactical.utils.SpriteAnims;
import tactical.utils.XMLParser;
import tactical.utils.XMLParser.TagArea;

public class ResourceManager {
	public static final String ANIMATIONS_FOLDER = "animations/animationsheets";
	public static final String ANIMATIONS_EXTENSION = ".anim";
	public static final String ANIMATIONS_FOLDER_IDENTIFIER = "animsheetdir";

	public static final String ANIMATION_IDENTIFIER = "anim";

	public static final String WEAPONS_ANIMATIONS_FOLDER = "animations/weaponanim";
	public static final String WEAPONS_FOLDER = "image/weapons";
	public static final String WEAPONS_EXTENSION = ".png";

	public static final String IMAGES_FOLDER_IDENTIFIER = "imagedir";

	private Hashtable<String, Image> images = new Hashtable<String, Image>();
	private Hashtable<String, SpriteSheet> spriteSheets = new Hashtable<String, SpriteSheet>();
	private Hashtable<String, SpriteAnims> spriteAnimations = new Hashtable<String, SpriteAnims>();
	private Hashtable<String, Music> musicByTitle = new Hashtable<String, Music>();
	private Hashtable<String, Sound> soundByTitle = new Hashtable<String, Sound>();
	private HashSet<TriggerCondition> conditions = new HashSet<>();
	private Hashtable<String, UnicodeFont> unicodeFonts = new Hashtable<String, UnicodeFont>();

	// These values need to be initialized each time a map is loaded
	private Hashtable<Integer, ArrayList<Speech>> speechesById = new Hashtable<Integer, ArrayList<Speech>>();
	private Hashtable<Integer, Trigger> triggerEventById = new Hashtable<Integer, Trigger>();
	private Hashtable<Integer, Cinematic> cinematicById = new Hashtable<Integer, Cinematic>();
	private Map map = new Map();

	public static final Color TRANSPARENT = new Color(255, 0, 255);

	public void reinitialize()
	{
		map.reinitalize();
		speechesById.clear();
		triggerEventById.clear();
		cinematicById.clear();
		conditions.clear();
	}

	/*
	public void getNewMap(String mapName) throws IOException, SlickException
	{
		map.reinitalize();
		MapParser.parseMap("/map/" + mapName + ".tmx", getMap());
	}

	public void getNewText(String text) throws IOException
	{
		speechesById.clear();
		triggerEventById.clear();
		cinematicById.clear();
		TextParser.parseText("/text/" + text, speechesById, triggerEventById, cinematicById);
	}
	*/
	
	/*
	public <T> void a(String file, Function<TagArea, T> tagAreaParser, Consumer<Hashtable<Integer, T>> initializeResource) throws IOException {
		ArrayList<TagArea> tagAreas = XMLParser.process(file);
		Hashtable<Integer, T> enemyDefinitionsById = new Hashtable<Integer,T>();

		for (TagArea ta : tagAreas)
		{
			T ed = tagAreaParser.apply(ta);
			enemyDefinitionsById.put(ed.getId(), ed);
		}

		initializeResource.accept(enemyDefinitionsById);
	}
	*/

	@SuppressWarnings("unchecked")
	public void addResource(String resource, EngineConfigurator configurator) throws IOException, SlickException {
		String[] split = resource.split(",");
		if (split[0].equalsIgnoreCase("image"))
		{
			Log.debug("Load image " + split[2]);
			Image nImage = new Image(split[2], TRANSPARENT);
			nImage.setFilter(Image.FILTER_NEAREST);
			images.put(split[1], nImage.getScaledCopy(split.length == 4 ?
					Float.parseFloat(split[3]) : 1));
		}
		else if (split[0].equalsIgnoreCase("ss"))
		{
			float scale = 1;
			if (split.length == 7)
			{
				if (Float.parseFloat(split[6]) == -1)
					scale = 1;
				else
					scale *= Float.parseFloat(split[6]);
			}
			Image ssIm = new Image(split[2], TRANSPARENT);
			ssIm.setFilter(Image.FILTER_NEAREST);
			ssIm = ssIm.getScaledCopy(scale);
			spriteSheets.put(split[1], new SpriteSheet(ssIm,
					(int) (Integer.parseInt(split[3]) * scale), (int) (Integer.parseInt(split[4])  * scale),
					(int) ((split.length >= 6 ? Integer.parseInt(split[5]) : 0)  * scale)));
		}
		else if (split[0].equalsIgnoreCase("anim"))
		{
			SpriteAnims sa = configurator.getAnimationParser().parseAnimations(split[2]);
			if (!images.containsKey(sa.getSpriteSheet()))
				throw new BadResourceException("Error while attempting to load animation file: " + split[2] + ".\n The"
					+ " animation file has refers to an image '" + sa.getSpriteSheet() + "' which does not exist\n."
							+ "Either change the name of the desired image in the 'sprite' folder to the correct name\n"
							+ "or update the animation file to refer to the correct image. Keep in mind that image names\n"
							+ "ARE case sensitive");
			sa.initialize(images.get(sa.getSpriteSheet()));
			spriteAnimations.put(split[1], sa);
		}
		else if (split[0].equalsIgnoreCase("animsheet"))
		{
			Image nImage = new Image(split[2], TRANSPARENT);
			nImage.setFilter(Image.FILTER_NEAREST);
			images.put(split[1], nImage);
		}
		else if (split[0].equalsIgnoreCase("text"))
		{
			String mapName = TacticalGame.TEXT_PARSER.parseText(split[1], speechesById, triggerEventById, cinematicById, conditions, this);
			Log.debug("Load map: " + mapName);
			map.setName(mapName);
			MapParser.parseMap("/map/" + mapName, map, new TilesetParser(), this);
		}
		else if (split[0].equalsIgnoreCase("herodefs"))
		{
			ArrayList<TagArea> tagAreas = XMLParser.process(split[1], true);
			Hashtable<Integer, HeroDefinition> heroDefinitionsById = new Hashtable<Integer, HeroDefinition>();

			for (TagArea ta : tagAreas)
			{
				HeroDefinition hd = TacticalGame.ENGINE_CONFIGURATIOR.getBattleStatisticsConfiguration().parseHeroDefinition(ta);
				heroDefinitionsById.put(hd.getId(), hd);
			}

			HeroResource.initialize(heroDefinitionsById);
		}
		else if (split[0].equalsIgnoreCase("itemdefs"))
		{
			ArrayList<TagArea> tagAreas = XMLParser.process(split[1], true);
			Hashtable<Integer, ItemDefinition> itemDefinitionsById = new Hashtable<Integer, ItemDefinition>();

			for (TagArea ta : tagAreas)
			{
				ItemDefinition id = ItemDefinition.parseItemDefinition(ta);
				itemDefinitionsById.put(id.getId(), id);
			}

			ItemResource.initialize(itemDefinitionsById);
		}
		else if (split[0].equalsIgnoreCase("enemydefs"))
		{
			ArrayList<TagArea> tagAreas = XMLParser.process(split[1], true);
			Hashtable<Integer, EnemyDefinition> enemyDefinitionsById = new Hashtable<Integer, EnemyDefinition>();

			for (TagArea ta : tagAreas)
			{
				EnemyDefinition ed = TacticalGame.ENGINE_CONFIGURATIOR.getBattleStatisticsConfiguration().parseEnemyDefinition(ta);
				enemyDefinitionsById.put(ed.getId(), ed);
			}

			EnemyResource.initialize(enemyDefinitionsById);
		}
		else if (split[0].equalsIgnoreCase("music"))
		{
			musicByTitle.put(split[1], new Music(split[2], true));
		}
		else if (split[0].equalsIgnoreCase("sound"))
		{
			soundByTitle.put(split[1], new Sound(split[2]));
		}
		else if (split[0].equalsIgnoreCase("font"))
		{
			InputStream inputStream	= ResourceLoader.getResourceAsStream(split[2]);
			try
			{
				Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
				boolean italic = false;
				boolean bold = false;
				if (split.length > 4) {
					italic = Boolean.parseBoolean(split[4]);
				}
				if (split.length > 5) {
					bold = Boolean.parseBoolean(split[5]);
				}
				UnicodeFont ufont = new UnicodeFont(awtFont, Integer.parseInt(split[3]), bold, italic);
				ufont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
				ufont.addAsciiGlyphs();
				ufont.addGlyphs(400, 600);
				ufont.loadGlyphs();
				unicodeFonts.put(split[1], ufont);
			} catch (FontFormatException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to load the font " + split[2] + ": " + e.getMessage(), "Error loading resource", JOptionPane.ERROR_MESSAGE);
			}

		}/*
		else if (split[0].equalsIgnoreCase("herodir") || 
				split[0].equalsIgnoreCase("enemydir") || 
				split[0].equalsIgnoreCase("npcdir"))
		{
			File dir = new File(split[1]);
			for (File file : dir.listFiles())
			{
				if (file.getName().endsWith(".png"))
				{
					Image im = new Image(file.getPath(), transparent);
					im.setFilter(Image.FILTER_NEAREST);
					spriteSheets.put(file.getName().replace(".png", ""), new SpriteSheet(im,
						Integer.parseInt(split[3]), Integer.parseInt(split[3])));
				}
			}
		}
		
		else if (split[0].equalsIgnoreCase("musicdir"))
		{
			File dir = new File(split[1]);
			for (File file : dir.listFiles())
			{
				if (file.getName().endsWith(".ogg"))
					musicByTitle.put(file.getName().replace(".ogg", ""), new Music(file.getPath()));
				else if (file.getName().endsWith(".wav"))
					musicByTitle.put(file.getName().replace(".wav", ""), new Music(file.getPath()));
			}
		}
		else if (split[0].equalsIgnoreCase("sounddir"))
		{
			File dir = new File(split[1]);
			for (File file : dir.listFiles())
			{
				if (file.getName().endsWith(".ogg"))
					soundByTitle.put(file.getName().replace(".ogg", ""), new Sound(file.getPath()));
				else if (file.getName().endsWith(".wav"))
					soundByTitle.put(file.getName().replace(".wav", ""), new Sound(file.getPath()));
			}
		}
		*/
		else if (split[0].equalsIgnoreCase("animsheetdir"))
		{
			for (File file : DirectoryLister.listFilesInDir(split[1]))
			{
				if (file.getName().endsWith(".png"))
				{
					Log.debug("Anim sheet " + file.getName());
					images.put(file.getName().replace(".png", ""), new Image(file.getPath(), TRANSPARENT));
				}
			}
			
			for (File file : DirectoryLister.listFilesInDir(split[1]))
			{
				if (file.getName().endsWith(".anim")) {
					Log.debug(file.getName());
					SpriteAnims sa = configurator.getAnimationParser().parseAnimations(file.getPath());
					if (!images.containsKey(sa.getSpriteSheet()))
					{
						throw new BadResourceException("Error while attempting to load animation file: " + file.getName() + ".\n The"
								+ " animation file has refers to an image '" + sa.getSpriteSheet() + "' which does not exist.\n"
										+ "Either change the name of the desired image in the 'sprite' folder to the correct name\n"
										+ "or update the animation file to refer to the correct image. Keep in mind that image names\n"
										+ "ARE case sensitive");
					}
					sa.initialize(images.get(sa.getSpriteSheet()));
					spriteAnimations.put(file.getName().replace(".anim", ""), sa);
				}
			}
		}
		else if (split[0].equalsIgnoreCase("spritedir"))
		{
			for (File file : DirectoryLister.listFilesInDir(split[1]))
			{
				if (file.getName().endsWith(".png"))
				{
					Image nIm = new Image(file.getPath(), TRANSPARENT);
					nIm.setFilter(Image.FILTER_NEAREST);
					images.put(file.getName().replace(".png", ""), nIm);
				}
			}
		}
		else if (split[0].equalsIgnoreCase("imagedir"))
		{
			for (File file : DirectoryLister.listFilesInDir(split[1]))
			{
				if (file.getName().endsWith(".png"))
				{
					Image nIm = new Image(file.getPath(), TRANSPARENT);
					nIm.setFilter(Image.FILTER_NEAREST);
					images.put(file.getName().replace(".png", ""), nIm);
				}
			}
		} 
		else {
			throw new BadResourceException("Unknown resource type to load: " + split[0]);
		}
	}

	public static Image swapColors( Image img, Hashtable<Color, Color> colorMap ){

		ImageBuffer imBuffer = new ImageBuffer(img.getWidth(), img.getHeight());

		for (int x = 0; x < img.getWidth(); x++)
		{
			for (int y = 0; y < img.getHeight(); y++)
			{
				Color oldColor = img.getColor(x, y);
				if (colorMap.containsKey(oldColor))
				{
					Color newColor = colorMap.get(oldColor);
					imBuffer.setRGBA(x, y, newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
				}
				else
					imBuffer.setRGBA(x, y, oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getAlpha());
			}
		}

		return imBuffer.getImage();
    }

	public Image getImage(String image) {
		if (image == null || !images.containsKey(image))
			throw new BadResourceException("Unable to find image: " + image);
		return images.get(image);
	}

	public SpriteSheet getSpriteSheet(String name) {
		if (name == null || !spriteSheets.containsKey(name))
			throw new BadResourceException("Unable to find  sprite sheet: " + name);
		return spriteSheets.get(name);
	}

	public SpriteAnims getSpriteAnimation(String spriteAnim) {
		if (spriteAnim == null || !spriteAnimations.containsKey(spriteAnim))
			throw new BadResourceException("Unable to find animation: " + spriteAnim);
		return spriteAnimations.get(spriteAnim);
	}

	public boolean containsSpriteAnimation(String spriteAnim) {
		return spriteAnimations.containsKey(spriteAnim);
	}

	public UnicodeFont getFontByName(String name)
	{
		if (name == null || !unicodeFonts.containsKey(name))
			throw new BadResourceException("Unable to find font: " + name);
		return unicodeFonts.get(name);
	}

	public Sound getSoundByName(String name)
	{
		if (name == null || !soundByTitle.containsKey(name))
			throw new BadResourceException("Unable to find sound: " + name);
		return soundByTitle.get(name);
	}
	
	public boolean containsMusic(String name)
	{
		return musicByTitle.containsKey(name);
	}

	public Music getMusicByName(String name)
	{
		if (musicByTitle.containsKey(name))
			return musicByTitle.get(name);
		try {
			return new Music("music/" + name + ".ogg", true);
		} catch (SlickException e) {
			e.printStackTrace();
			throw new BadResourceException("Unable to find music: " + name);
		}
	}
	
	public void addMusicByName(String name) {
		if (musicByTitle.containsKey(name))
			return;
		try {
			musicByTitle.put(name, new Music("music/" + name + ".ogg"));
		} catch (SlickException e) {
			e.printStackTrace();
			throw new BadResourceException("Unable to find music: " + name);
		}
	}

	public Map getMap()
	{
		return map;
	}

	public ArrayList<Speech> getSpeechesById(int id)
	{
		return speechesById.get(id);
	}

	public Trigger getTriggerEventById(int id)
	{
		// TODO This is just to exit the game on hero death
		if (id == Trigger.TRIGGER_ID_EXIT)
		{
			Trigger te = new Trigger("Game Exit", Trigger.TRIGGER_ID_EXIT, false, false, false, false, null, null);
			te.addTriggerable(te.new TriggerExit());
			return te;
		} else if (id == Trigger.TRIGGER_ID_SAVE_AND_EXIT)
		{
			Trigger te = new Trigger("Game Exit", 
					Trigger.TRIGGER_ID_SAVE_AND_EXIT, false, false, false, false, null, null);
			te.addTriggerable(te.new TriggerSave());
			te.addTriggerable(te.new TriggerExit());
			return te;
		} else if (id == Trigger.TRIGGER_CHEST_NO_ITEM) {
			Trigger te = new Trigger("Chest No Item", Trigger.TRIGGER_CHEST_NO_ITEM, 
					false, false, false, false, null, null);
			te.addTriggerable(te.new TriggerShowText(TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration().getNoItemInChestText()));
			return te;
		}
		

		Trigger retTE = triggerEventById.get(id);
		if (retTE == null)
			throw new BadResourceException("The specified trigger " + id + " could not be found");
		return retTE;
	}
	
	public void addTriggerEvent(int id, Trigger trigger)
	{
		this.triggerEventById.put(id, trigger);
	}
	
	public void addTriggerCondition(TriggerCondition triggerCondition)
	{
		this.conditions.add(triggerCondition);
	}

	public Collection<Trigger> getTriggers() {
		return triggerEventById.values();
	}

	public void setTriggerEventById(Hashtable<Integer, Trigger> triggerEventById) {
		this.triggerEventById = triggerEventById;
	}

	public Cinematic getCinematicById(int id)
	{
		return cinematicById.get(id);
	}

	public void checkTriggerCondtions(String location, 
			boolean locationEntered, boolean immediate, boolean onMapLoad, boolean searching, StateInfo stateInfo)
	{
		for (TriggerCondition tc : this.conditions)
		{
			tc.executeCondtions(location, locationEntered, immediate, onMapLoad, searching, stateInfo);
		}
	}

	public static ArrayList<String> readAllLines(String file) throws IOException
	{
		Log.debug("Read all lines " + file);
		BufferedReader br = null;
		if (LoadingState.inJar)
		{
			file = file.replaceAll("\\\\", "/");
			br = new BufferedReader(new InputStreamReader(LoadingState.MY_CLASS.getResourceAsStream(file)));
		}
		else
			br = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream(file)));
		ArrayList<String> allLines = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
            allLines.add(line);
        }
		br.close();
		return allLines;
	}
	
	public void reloadAnimations(EngineConfigurator configurator) {
		try {
			this.addResource("animsheetdir,animations/animationsheets", configurator);
		} catch (IOException | SlickException e) {
			JOptionPane.showMessageDialog(null, "An error occurred reloading animations: " + e.getMessage());
		}
	}
}
