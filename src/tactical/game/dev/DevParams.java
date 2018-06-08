package tactical.game.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.newdawn.slick.util.Log;

public class DevParams {
	private static final String DEV_PARAMS_FILE = "DevParams";

	private ArrayList<Integer> heroesToAdd;
	private ArrayList<String> heroNamesToAdd;
	private int level = 1;

	private DevParams(ArrayList<Integer> heroesToAdd, ArrayList<String> heroNamesToAdd, int level) {
		super();
		this.heroesToAdd = heroesToAdd;
		this.heroNamesToAdd = heroNamesToAdd;
		this.level = level;
	}
	
	public static DevParams parseDevParams()
	{
		return parseDevParams(DEV_PARAMS_FILE);
	}

	public static DevParams parseDevParams(String fileName)
	{
		ArrayList<Integer> heroesToAdd = new ArrayList<>();
		ArrayList<String> heroNamesToAdd = new ArrayList<>();
		int level = 1;

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
			String line;
			while ((line = br.readLine()) != null) {
	            if (line.startsWith("HERO ")) {
	            	String heroId = line.split(" ")[1];
	            	try {
	            		
	            		heroesToAdd.add(Integer.parseInt(heroId));
	            	} catch (NumberFormatException ex) {
	            		heroNamesToAdd.add(heroId);
	            	}
	            }
	            else if (line.startsWith("LEVEL "))
	            	level = Integer.parseInt(line.split(" ")[1]);
	        }

			return new DevParams(heroesToAdd, heroNamesToAdd, level);
		}
		catch (Exception ex) {
			Log.debug("An error occurred while trying to load the dev params from: " + DEV_PARAMS_FILE);
		}
		finally
		{
			if (br != null)
				try { br.close(); } catch (IOException e) {}
		}

		return null;
	}

	public ArrayList<Integer> getHeroesToAdd() {
		return heroesToAdd;
	}

	public ArrayList<String> getHeroNamesToAdd() {
		return heroNamesToAdd;
	}

	public int getLevel() {
		return level;
	}
}
