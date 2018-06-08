package tactical.map;

import java.util.Hashtable;

public class MapLayer {
	private int[][] tiles;
	private Hashtable<String, String> params;
	private int flashOffset, flashDuration, flashDelta;
	
	public MapLayer(int[][] tiles) {
		super();
		this.tiles = tiles;
		this.params = new Hashtable<>();
	}
	public int[][] getTiles() {
		return tiles;
	}
	public void setTiles(int[][] tiles) {
		this.tiles = tiles;
	}
	
	public void addParam(String key, String value) {
		params.put(key, value);
	}
	
	public String getParam(String key) {
		return params.get(key);
	}
	
	public boolean containsParam(String key) {
		return params.containsKey(key);
	}
	
	public void setFlashOffset(int flashOffset) {
		this.flashOffset = flashOffset;
		if (flashOffset > 0)
			this.flashDelta = -1;
	}
	
	public void setFlashDuration(int flashDuration) {
		this.flashDuration = flashDuration;
		this.flashDelta = flashDuration;
	}
	
	public void update(int delta) {
		if (flashOffset - delta > 0) {
			flashOffset -= delta;
			return;
		} else if (flashOffset != 0){
			
			flashOffset = 0;
			flashDelta = flashDuration;
			delta = 0;
		}
		
		// WHAT THE FUCK IS THIS MATH
		if (flashDelta - delta < -flashDuration)
			flashDelta = flashDelta - delta + 2 * flashDuration;
		else
			flashDelta -= delta;
	}
	
	public boolean isVisibleFlashing()
	{
		return flashDelta >= 0;
	}
}
