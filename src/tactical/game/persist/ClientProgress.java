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

import tactical.engine.state.StateInfo;
import tactical.game.sprite.CombatSprite;

public class ClientProgress implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String PROGRESS_EXTENSION = ".progress";

	private HashSet<String> questsCompleted;
	private Hashtable<String, ArrayList<Integer>> retriggerablesPerMapData;
	private Hashtable<String, ArrayList<Integer>> nonretriggerablesPerMapData;
	private ArrayList<Integer> dealItems;
	private Point inTownLocation;
	private String name;
	private String mapData;
	private long timePlayed;
	private boolean isBattle = false;
	private ArrayList<CombatSprite> battleEnemySprites;
	private ArrayList<Integer> battleHeroSpriteIds;
	private Integer currentTurn;
	private static final String BATTLE_PREFIX = "!!";
	private transient long lastSaveTime;
	private transient String lastSaveMapData;

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
		inTownLocation = null;
		mapData = null;
		timePlayed = 0;
		isBattle = false;
		battleEnemySprites = null;
		battleHeroSpriteIds = null;
		currentTurn = 0;
		lastSaveTime = System.currentTimeMillis();
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

	public void serializeToFile()
	{
		this.timePlayed += (System.currentTimeMillis() - lastSaveTime);
		this.lastSaveTime = System.currentTimeMillis();
		this.lastSaveMapData = mapData;
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
	      cp.lastSaveMapData = cp.mapData;
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
		if (!retriggerablesPerMapData.containsKey(mapData))
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

	public ArrayList<CombatSprite> getBattleSprites(StateInfo stateInfo) {
		for (Integer heroID : battleHeroSpriteIds)
		{
			this.battleEnemySprites.add(stateInfo.getHeroById(heroID));
		}
		
		return battleEnemySprites;
	}

	public void setBattleSprites(List<CombatSprite> battleSprites) {
		if (battleSprites == null)
		{
			this.battleHeroSpriteIds = null;
			this.battleEnemySprites = null;
		}
		else
		{
			battleHeroSpriteIds = new ArrayList<>();
			battleEnemySprites = new ArrayList<>();
			for (CombatSprite cs : battleSprites)
			{
				if (cs.isHero())
					battleHeroSpriteIds.add(cs.getId());
				else
					battleEnemySprites.add(cs);
			}
		}
	}

	public Integer getCurrentTurn() {
		return currentTurn;
	}

	public void setCurrentTurn(CombatSprite currentTurn) {
		if (currentTurn == null)
			this.currentTurn = null;
		else
			this.currentTurn = currentTurn.getId();
	}

	public String getMapData() {
		return mapData;
	}

	public void setMapData(String mapData) {
		this.mapData = mapData;
	}
	
	public Point getInTownLocation() {
		return inTownLocation;
	}

	public void setInTownLocation(Point inTownLocation) {
		this.inTownLocation = inTownLocation;
	}

	public ArrayList<Integer> getDealItems() {
		return dealItems;
	}

	public void setDealItems(ArrayList<Integer> dealItems) {
		this.dealItems = dealItems;
	}

	public HashSet<String> getQuestsCompleted() {
		return questsCompleted;
	}
	
	public String getLastSaveMapData() {
		return lastSaveMapData;
	}

	public void setLastSaveMapData(String lastSaveMapData) {
		this.lastSaveMapData = lastSaveMapData;
	}
}
