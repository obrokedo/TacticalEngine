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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.newdawn.slick.util.Log;

import lombok.Getter;
import lombok.Setter;
import tactical.engine.TacticalGame;
import tactical.engine.message.LoadMapMessage;
import tactical.engine.state.devel.SaveState;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.SerializedBattleEffect;
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
	@Getter @Setter private SaveLocation lastSaveLocation;
	@Getter private EgressLocation lastEgressLocation;
	private boolean isBattle = false;
	
	@Getter @Setter
	private transient LinkedList<SaveState> saveStates = null;
	
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
	
	public SaveLocation createBattleSaveLocation(List<CombatSprite> battleSprites, CombatSprite currentTurn) {
		SaveLocation saveLocation = new SaveLocation();		
		if (currentTurn != null)
			saveLocation.setCurrentTurn(currentTurn.getId());
		saveLocation.setLastSaveMapData(mapData);
		
		if (battleSprites != null)
		{
			saveLocation.setBattleHeroSpriteIds(new ArrayList<>());
			saveLocation.setBattleEnemySprites(new ArrayList<>());
			for (CombatSprite cs : battleSprites)
			{
				if (cs.isHero())
					saveLocation.getBattleHeroSpriteIds().add(cs.getId());
				else {
					// Convert each battle effect in to a serialized battle effect so we're not persisting a jython object
					for (BattleEffect effect : cs.getBattleEffects()) {
						cs.getPersistedBattleEffects().add(new SerializedBattleEffect(effect));
					}
					saveLocation.getBattleEnemySprites().add(cs);
				}
			}
		}
		return saveLocation;
	}
	
	public void saveViaBattle(List<CombatSprite> battleSprites, CombatSprite currentTurn) {
							
		this.lastSaveLocation = createBattleSaveLocation(battleSprites, currentTurn);
		this.isBattle = true;
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
	
	public void serializeToFile() {
		serializeToFile(name + ".progress", false);
	}

	public void serializeToFile(String fileName, boolean debugSave)
	{		
		this.timePlayed += (System.currentTimeMillis() - lastSaveTime);
		if (!TacticalGame.SAVE_ENABLED && !debugSave)
			return;
		try
		{
			OutputStream file = new FileOutputStream(fileName);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeObject(this);
			output.flush();
			file.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * We stash some values in the save location, set them in the progress object now
	 */
	public void setPostDeserializationValues() {
		lastSaveTime = System.currentTimeMillis();
	    mapData = lastSaveLocation.getLastSaveMapData();
	}

	public static ClientProgress deserializeFromFile(String profile)
	{
	    try
	    {
	      InputStream file = new FileInputStream(profile);
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);

	      ClientProgress cp = (ClientProgress) input.readObject();
	      
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
