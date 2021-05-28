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

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.game.exception.BadResourceException;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;

public class ClientProfile implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String PROFILE_EXTENSION = ".profile";
	
	private ArrayList<CombatSprite> heroes;
	private HashSet<Integer> inBattleHeroIds;
	private int gold;
	private String name;
	private transient ArrayList<CombatSprite> networkHeroes;

	public ClientProfile(String name)
	{
		this.name = name;
		initializeStartingValues();
	}
	
	public void initializeStartingValues() {
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
	
	public void removeAllHeroes() {
		heroes.clear();
		inBattleHeroIds.clear();
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
	
	/**
	 * Returns a modifiable safe list of heroes that are currently in the 'active" party
	 * 
	 * @return a modifiable safe list of heroes that are currently in the 'active" party
	 */
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
	
	public void addHeroToParty(CombatSprite cs) {
		this.inBattleHeroIds.add(cs.getId());
	}
	
	public void removeHeroFromParty(CombatSprite cs) {
		this.inBattleHeroIds.remove(cs.getId());
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
		if (!TacticalGame.SAVE_ENABLED)
			return;
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
	
	public void initializeStartingHeroes()
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
		
	}

	public String getName() {
		return name;
	}
}
