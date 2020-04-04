package tactical.game.persist;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.game.battle.LevelUpResult;
import tactical.game.dev.DevParams;
import tactical.game.exception.BadResourceException;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.loading.ResourceManager;

public class ClientProfile implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String PROFILE_EXTENSION = ".profile";
	
	private ArrayList<CombatSprite> heroes;
	private HashSet<Integer> inBattleHeroIds;
	private int gold;
	private String name;
	private transient ArrayList<CombatSprite> networkHeroes;
	private transient DevParams devParams;

	public void setDevParams(DevParams devParams) {
		this.devParams = devParams;
	}

	public ClientProfile(String name)
	{
		this.name = name;
		initializeValues();
	}
	
	public void initializeValues() {
		heroes = new ArrayList<>();
		inBattleHeroIds = new HashSet<>();
		networkHeroes = new ArrayList<>();
		gold = 100;
	}

	public void addHero(CombatSprite hero)
	{
		// Don't add someone who is already in the party
		boolean inParty = false;
		for (CombatSprite cs : heroes) {
			if (cs.getId() == hero.getId())
				inParty = true;
		}
		
		if (!inParty)
			this.heroes.add(hero);
		
		EngineConfigurationValues configValues = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
		int maxHeros = configValues.getMaxPartySize();
		
		if (this.inBattleHeroIds.size() < maxHeros)
			this.inBattleHeroIds.add(hero.getId());
	}

	public ArrayList<CombatSprite> getHeroes() {
		ArrayList<CombatSprite> hs = new ArrayList<>();
		hs.addAll(heroes);
		if (networkHeroes != null)
			hs.addAll(networkHeroes);
		Collections.sort(hs, new HeroComparator());
		return hs;
	}
	
	public void removeHeroById(int heroId) {
		Iterator<CombatSprite> heroItr = heroes.iterator(); 
		while (heroItr.hasNext()) {
			CombatSprite hero = heroItr.next();
			if (hero.getId() == heroId) {
				heroItr.remove();
			}
		}
	}
	
	public ArrayList<CombatSprite> getHeroesInParty()
	{
		ArrayList<CombatSprite> hs = new ArrayList<>();
		for (CombatSprite hero : heroes)
		{
			if (inBattleHeroIds.contains(hero.getId()))
				hs.add(hero);
		}
		return hs;
	}

	private class HeroComparator implements Comparator<CombatSprite>
	{
		@Override
		public int compare(CombatSprite c1, CombatSprite c2) {
			return  c1.getId() - c2.getId();
		}

	}

	public void addNetworkHeroes(ArrayList<CombatSprite> networkHeroes) {
		this.networkHeroes.addAll(networkHeroes);
	}
	
	public CombatSprite getMainCharacter()
	{
		for (CombatSprite cs : this.getHeroes())
			if (cs.isLeader())
				return cs;
		throw new BadResourceException("No heroes exist in the party that are marked as the 'Leader'");
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}
	
	public void modifyGold(int gold) {
		this.gold += gold;
	}

	public void serializeToFile()
	{
		try
		{
			OutputStream file = new FileOutputStream(name + ".profile");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeObject(this);
			output.flush();
			file.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static ClientProfile deserializeFromFile(String profile)
	{
	    try
	    {
	      InputStream file = new FileInputStream(profile);
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);

	      ClientProfile cp = (ClientProfile) input.readObject();
	      file.close();
	      return cp;
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
	    }

	    return null;
	}
	
	public void initialize()
	{
		// Add starting heroes if they haven't been added yet
		if (getHeroes().size() == 0)
		{
			// Add the heroes specified in the configuration values,
			// these are the heroes that the force will initially contain
			for (String heroName : TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getStartingHeroIds()) {
				addHero(HeroResource.getHero(heroName));			
			}			
		}
		
		applyDevParams();
	}
	
	private void applyDevParams()
	{
		if (devParams == null)
			return;
		

		// Add any heroes specified in the development params
		if (devParams.getHeroesToAdd() != null)
			for (Integer heroId : devParams.getHeroesToAdd())
			{
				Log.debug("DevParams adding hero with id: " + heroId + " to the party");
				addHero(HeroResource.getHero(heroId));
			}
		
		if (devParams.getHeroNamesToAdd() != null)
			for (String heroName : devParams.getHeroNamesToAdd())
			{
				Log.debug("DevParams adding hero with name: " + heroName + " to the party");
				addHero(HeroResource.getHero(heroName));
			}

		if (getHeroes().size() == 0)
			throw new BadResourceException("No starting heroes have been specified. Update the ConfigurationValues "
					+ "script to indicate the ids of the heroes that should start in the party.");

		if (devParams.getLevel() > 1)
		{
			Log.debug("DevParams setting hero level to: " + devParams.getLevel());
			
			for (CombatSprite cs : getHeroes())
			{
				while (cs.getLevel() < devParams.getLevel())
				{
					LevelUpResult lur = cs.getHeroProgression().getLevelUpResults(cs);
					cs.setExp(100);
					cs.getHeroProgression().levelUp(cs, lur);
				}
			}
		}
		
		System.out.println("Removing dev params");
		devParams = null;
	}

	public String getName() {
		return name;
	}
}
