package tactical.game.persist;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.newdawn.slick.util.Log;

import lombok.Getter;
import lombok.Setter;
import tactical.engine.message.LoadMapMessage;
import tactical.game.item.Item;
import tactical.game.sprite.CombatSprite;

public class ClientProgress implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String PROGRESS_EXTENSION = ".progress";

	@Getter private HashSet<String> questsCompleted;
	private Hashtable<String, ArrayList<Integer>> retriggerablesPerMapData;
	private Hashtable<String, ArrayList<Integer>> nonretriggerablesPerMapData;
	@Getter @Setter private ArrayList<Integer> dealItems;
	private String name;
	
	private long timePlayed;
	private static final String BATTLE_PREFIX = "!!";
	private transient long lastSaveTime;
	private ArrayList<Integer> storedItems;
	@Getter private SaveLocation lastSaveLocation;
	@Getter private EgressLocation lastEgressLocation;
	private boolean isBattle = false;
	
	// This value is used to load from AND is the current map that the player is on
	@Getter @Setter private transient String mapData;

	public ClientProgress(String name)
	{		
		this.name = name;
		initializeValues();
	}
	
	public void initializeValues() {
		questsCompleted = new HashSet<String>();
		retriggerablesPerMapData = new Hashtable<String, ArrayList<Integer>>();
		nonretriggerablesPerMapData = new Hashtable<String, ArrayList<Integer>>();
		dealItems = new ArrayList<>();
		lastSaveLocation = new SaveLocation();
		lastEgressLocation = new EgressLocation();
		mapData = null;
		timePlayed = 0;
		lastSaveTime = System.currentTimeMillis();
		storedItems = new ArrayList<>(); 
	}
	
	public void setQuestStatus(String questId, boolean completed)	
	{
		if (completed)
			questsCompleted.add(questId);
		else
			questsCompleted.remove(questId);
	}

	public boolean isQuestComplete(String questId)
	{
		return questsCompleted.contains(questId);
	}
	
	public void saveViaPriest(Point point) {
		this.lastSaveLocation = new SaveLocation();
		lastSaveLocation.setInTownPoint(point);
		lastSaveLocation.setLastSaveMapData(mapData);
		
		this.lastEgressLocation = new EgressLocation();
		this.lastEgressLocation.setInTownPoint(point);
		this.lastEgressLocation.setLastSaveMapData(mapData);
		serializeToFile();
	}
	
	public void saveViaBattle(List<CombatSprite> battleSprites, CombatSprite currentTurn) {
							
		this.lastSaveLocation = new SaveLocation();
		this.isBattle = true;
		if (currentTurn != null)
			this.lastSaveLocation.setCurrentTurn(currentTurn.getId());
		this.lastSaveLocation.setLastSaveMapData(mapData);
		
		if (battleSprites != null)
		{
			this.lastSaveLocation.setBattleHeroSpriteIds(new ArrayList<>());
			this.lastSaveLocation.setBattleEnemySprites(new ArrayList<>());
			for (CombatSprite cs : battleSprites)
			{
				if (cs.isHero())
					this.lastSaveLocation.getBattleHeroSpriteIds().add(cs.getId());
				else
					this.lastSaveLocation.getBattleEnemySprites().add(cs);
			}
		}
		serializeToFile();
	}
	
	public void saveViaChapter(LoadMapMessage mapMessage) {
		this.lastSaveLocation = new SaveLocation();
		this.lastSaveLocation.setChapterSaveMessage(mapMessage);
		serializeToFile();
	}
	
	public void setEgressLocation(Point point, String map) {
		this.lastEgressLocation = new EgressLocation();
		this.lastEgressLocation.setInTownPoint(point);
		this.lastEgressLocation.setLastSaveMapData(map);
	}
	
	public void setEgressLocation(String location, String map) {
		this.lastEgressLocation = new EgressLocation();
		this.lastEgressLocation.setInTownLocation(location);
		this.lastEgressLocation.setLastSaveMapData(map);
	}

	public void serializeToFile()
	{
		this.timePlayed += (System.currentTimeMillis() - lastSaveTime);
		try
		{
			OutputStream file = new FileOutputStream(name + ".progress");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeObject(this);
			output.flush();
			file.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static ClientProgress deserializeFromFile(String profile)
	{
	    try
	    {
	      InputStream file = new FileInputStream(profile);
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);

	      ClientProgress cp = (ClientProgress) input.readObject();
	      cp.lastSaveTime = System.currentTimeMillis();
	      cp.mapData = cp.lastSaveLocation.getLastSaveMapData();
	      file.close();
	      return cp;
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
	    }

	    return null;
	}

	public void setMapData(String mapData, boolean isBattle) {
		this.mapData = mapData;
		if (isBattle)
		{
			this.isBattle = isBattle;

			if (nonretriggerablesPerMapData.containsKey(BATTLE_PREFIX + mapData))
				nonretriggerablesPerMapData.get(BATTLE_PREFIX + mapData).clear();
		}
	}

	public String getTimePlayed() {
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timePlayed),
	            TimeUnit.MILLISECONDS.toMinutes(timePlayed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timePlayed)),
	            TimeUnit.MILLISECONDS.toSeconds(timePlayed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timePlayed)));
	}

	public ArrayList<Integer> getRetriggerablesByMap()
	{
		if (mapData == null || !retriggerablesPerMapData.containsKey(mapData))
			return null;
		return retriggerablesPerMapData.get(mapData);
	}

	public void addRetriggerableByMap(int triggerId)
	{
		if (!retriggerablesPerMapData.containsKey(mapData))
			retriggerablesPerMapData.put(mapData, new ArrayList<Integer>());
		retriggerablesPerMapData.get(mapData).add(triggerId);
	}

	public boolean isPreviouslyTriggered(int triggerId)
	{
		if (!retriggerablesPerMapData.containsKey(mapData))
			return false;
		return retriggerablesPerMapData.get(mapData).contains(triggerId);
	}

	public void addNonretriggerableByMap(int triggerId)
	{
		Log.debug("Add non retrig " + triggerId + " " + (isBattle ? BATTLE_PREFIX + mapData : mapData));
		if (!nonretriggerablesPerMapData.containsKey((isBattle ? BATTLE_PREFIX + mapData : mapData)))
			nonretriggerablesPerMapData.put((isBattle ? BATTLE_PREFIX + mapData : mapData), new ArrayList<Integer>());
		nonretriggerablesPerMapData.get((isBattle ? BATTLE_PREFIX + mapData : mapData)).add(triggerId);
	}

	public boolean isNonretriggableTrigger(int triggerId)
	{
		if (!nonretriggerablesPerMapData.containsKey((isBattle ? BATTLE_PREFIX + mapData : mapData)))
			return false;

		return nonretriggerablesPerMapData.get((isBattle ? BATTLE_PREFIX + mapData : mapData)).contains(triggerId);
	}

	public boolean isBattle() {
		return isBattle;
	}

	public void setBattle(boolean isBattle) {
		this.isBattle = isBattle;
	}
	
	/*
	public Point getInTownLocation() {
		return inTownLocation;
	}

	public void setInTownLocation(Point inTownLocation) {
		this.inTownLocation = inTownLocation;
	}
	*/
	
	/*
	public String getLastSaveMapData() {
		return lastSaveMapData;
	}

	public void setLastSaveMapData(String lastSaveMapData) {
		this.lastSaveMapData = lastSaveMapData;
	}
	*/
	
	public void depositItem(Item item) {
		this.storedItems.add(item.getItemId());
	}
	
	public void retrieveItem(int index) {
		this.storedItems.remove(index);
	}
	
	public ArrayList<Integer> getStoredItems() {
		return new ArrayList<Integer> (this.storedItems);
	}

	public LoadMapMessage getAndClearChapterSaveMessage() {
		
		LoadMapMessage lmm = this.lastSaveLocation.getChapterSaveMessage();
		this.lastSaveLocation.setChapterSaveMessage(null);
		return lmm;
	}
}
