package tactical.engine.message;

import tactical.game.constants.Direction;

/**
 * A message that indicates that a new map, battle or cinematic should be loaded
 *
 * @author Broked
 *
 */
public class LoadMapMessage extends Message
{
	private static final long serialVersionUID = 1L;
	private String mapData;
	private String location;
	private int cinematicID;
	private int battleBG;
	private Direction transDir = null;

	public LoadMapMessage(MessageType messageType, String mapData, String location, Direction transitionDir)
	{
		super(messageType);
		this.mapData = mapData;
		this.location = location;
		this.transDir = transitionDir;
	}
	
	public LoadMapMessage(MessageType messageType, String mapData, String location, int battleBG)
	{
		super(messageType);
		this.mapData = mapData;
		this.location = location;
		this.battleBG = battleBG;
	}

	public LoadMapMessage(MessageType messageType, String mapData, int cinematicID)
	{
		super(messageType);
		this.mapData = mapData;
		this.cinematicID = cinematicID;
	}

	public String getMapData() {
		return mapData;
	}

	public String getLocation() {
		return location;
	}

	public int getCinematicID() {
		return cinematicID;
	}

	public int getBattleBG() {
		return battleBG;
	}

	public Direction getTransDir() {
		return transDir;
	}
}
