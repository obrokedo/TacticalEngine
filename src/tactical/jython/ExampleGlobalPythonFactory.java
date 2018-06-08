package tactical.jython;

import java.io.File;

import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PySystemState;

import tactical.engine.config.BattleEffectFactory;
import tactical.engine.config.BattleFunctionConfiguration;
import tactical.engine.config.CinematicActorConfiguration;
import tactical.engine.config.EngineConfigurationValues;
import tactical.engine.config.LevelProgressionConfiguration;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.config.MusicConfiguration;
import tactical.engine.config.SpellFactory;
import tactical.jython.JythonObjectFactory;
import tactical.loading.LoadingState;

/**
 * Factory to create Jython objects that are backed by the corresponding Python script. Methods
 * called on these objects will use the results as determined by the script.
 * NOTE: Currently the scripts can be re-loaded via the debug menu, which means subsequent calls
 * to the creation methods contained herein may result in objects whose methods return different values
 *
 * @TODO When we make it to a point where we don't want to be able to reload the Python scripts for testing purposes
 * then we should make this a "singleton" class that can only be initialized once
 *
 * @see /mb/fc/game/menu/DebugMenu
 * @see /scripts/
 *
 * @author Broked
 *
 */
public class ExampleGlobalPythonFactory
{
	/**
	 * Hold an instance of each of the Jython objects that correspond with a Python script
	 */
	private static CinematicActorConfiguration cinematicActor = null;
	private static BattleFunctionConfiguration battleFunctions = null;
	private static MusicConfiguration musicSelector = null;
	private static SpellFactory spell = null;
	private static BattleEffectFactory battleEffect = null;
	private static LevelProgressionConfiguration levelProgression = null;
	private static EngineConfigurationValues configurationValues = null;
	private static MenuConfiguration menuConfiguration = null;

	/**
	 * A boolean flag indicating whether this factory has been initialized.
	 * Creation methods will not work unless this value is set true
	 */

	private static boolean initialized = false;

	/**
	 * Initializes the GlobalPythonFactory by loading all of the python scripts and
	 * storing a copy
	 */
	public static void intialize()
	{
		if (initialized)
			configurationValues.clearPythonModules();
		
		initialized = true;

		/************************************************************************/
		/* Depending on whether this is being built for a single jar or not; 	*/
		/* single jar or not scripts will be loaded differently				  	*/
		/************************************************************************/
		// The build is being done for one large JAR that contains all resources
		if (LoadingState.inJar)
		{
			String jarPath = JythonObjectFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			jarPath = jarPath.replaceAll("%20", " ");
			PySystemState state = new PySystemState();
			state.path.insert(0,Py.newString(jarPath + java.io.File.separator + "scripts"));
			Py.setSystemState(state);
		}

		JythonObjectFactory.sys  = Py.getSystemState();

		// The build is being done for a jar that does not contain all of the resources
		if (!LoadingState.inJar)
		{
			JythonObjectFactory.sys.path.append(new PyString(JythonObjectFactory.sys.getPath("scripts")));

			File scriptsFolder = new File(JythonObjectFactory.sys.getPath("scripts"));
			if (scriptsFolder.exists()) {
				for (File file : scriptsFolder.listFiles())
				{
					if (file.getName().endsWith(".class"))
						file.delete();
				}
			} else {
				return;
			}
		}

		// There should only ever be a single instance of this class, so set all of the
		// values so they can be accessed in a static way
        battleFunctions = (BattleFunctionConfiguration) (new JythonObjectFactory(BattleFunctionConfiguration.class, "BattleFunctions", "BattleFunctions")).createObject();
        cinematicActor  = (CinematicActorConfiguration) (new JythonObjectFactory(CinematicActorConfiguration.class, "CinematicActor", "CinematicActor")).createObject();
        musicSelector  = (MusicConfiguration) (new JythonObjectFactory(MusicConfiguration.class, "MusicScript", "MusicScript")).createObject();
        spell  = (SpellFactory) (new JythonObjectFactory(SpellFactory.class, "Spells", "Spells")).createObject();
        battleEffect = (BattleEffectFactory) (new JythonObjectFactory(BattleEffectFactory.class, "BattleEffect", "BattleEffect")).createObject();
        levelProgression = (LevelProgressionConfiguration) (new JythonObjectFactory(LevelProgressionConfiguration.class, "LevelProgression", "LevelProgression")).createObject();
        configurationValues = (EngineConfigurationValues) (new JythonObjectFactory(EngineConfigurationValues.class, "ConfigurationValues", "ConfigurationValues")).createObject();
        menuConfiguration = (MenuConfiguration) (new JythonObjectFactory(MenuConfiguration.class, "MenuConfiguration", "MenuConfiguration")).createObject();
	}

	/**
	 * Gets a script-backed JCinematicActor. This
	 * method should only be called after the factory
	 * has been initialized
	 *
	 * @return a script-backed JCinematicActor
	 */
	public static CinematicActorConfiguration createJCinematicActor()
	{
		checkFactoryInitialized();
		return cinematicActor;
	}

	/**
	 * Gets a script-backed JBattleFunctions. This
	 * method should only be called after the factory
	 * has been initialized
	 *
	 * @return a script-backed JBattleFunctions
	 */
	public static BattleFunctionConfiguration createJBattleFunctions()
	{
		checkFactoryInitialized();
		return battleFunctions;
	}

	/**
	 * Gets a script-backed JMusicSelector. This
	 * method should only be called after the factory
	 * has been initialized
	 *
	 * @return a script-backed JMusicSelector
	 */
	public static MusicConfiguration createJMusicSelector()
	{
		checkFactoryInitialized();
		return musicSelector;
	}

	/**
	 * Gets a script-backed JSpell. This
	 * method should only be called after the factory
	 * has been initialized
	 *
	 * @return a script-backed JSpell
	 */
	public static SpellFactory getSpellFactory()
	{
		checkFactoryInitialized();
		return spell;
	}

	public static BattleEffectFactory getBattleEffectFactory()
	{
		checkFactoryInitialized();
		return battleEffect;
	}

	public static LevelProgressionConfiguration createLevelProgression()
	{
		checkFactoryInitialized();
		return levelProgression;
	}

	public static EngineConfigurationValues createConfigurationValues()
	{
		checkFactoryInitialized();
		return configurationValues;
	}
	
	public static MenuConfiguration createMenuConfig()
	{
		checkFactoryInitialized();
		return menuConfiguration;
	}
	
	/**
	 * Ensures that this factory has been initialized before attempting to
	 * return a Jython object
	 */
	private static void checkFactoryInitialized()
	{
		if (!initialized)
			throw new RuntimeException("Attempted to create Jython object before initializing the factory");
	}
}
