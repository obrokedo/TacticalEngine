package tactical.loading;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.Transition;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.load.BulkLoader;
import tactical.game.hudmenu.Panel;
import tactical.game.resource.SpellResource;
import tactical.game.trigger.Trigger;
import tactical.game.trigger.Triggerable;

public class LoadingState extends BasicGameState
{
	private String mapName;
	private String textName;
	private LoadableGameState nextState;
	private LoadingScreenRenderer loadingRenderer;
	private ResourceManager resourceManager;
	private boolean loadResources;
	private BulkLoader bulkLoader;
	private LoadingStatus loadingStatus;
	private int stateId;
	private int loadIndex;
	private int loadAmount;
	private boolean loadingMap;
	private String errorMessage = null;
	private Transition enterNextStateTransition;
	private Image intermediateImage = null;	
	public static boolean inJar = false;
	public static Class<?> MY_CLASS;

	public LoadingState(int stateId)
	{
		this.stateId = stateId;
		if (LoadingState.inJar)
			MY_CLASS = this.getClass();
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException
	{
		// LoadingList.setDeferredLoading(true);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		container.setShowFPS(false);
		if (intermediateImage != null)
			g.drawImage(intermediateImage, 0, 0);
		else 
			loadingRenderer.render(loadingStatus);
		if (errorMessage != null)
		{
			g.setColor(Color.white);
			int strWidth = container.getDefaultFont().getWidth(errorMessage);
			g.drawString(errorMessage, (container.getWidth() - strWidth) / 2, container.getHeight() / 2);
		}
	}
	
	

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException
	{
		if (errorMessage != null)
			return;

		// Check to see if this is the first time through the update loop, if so then
		// intialize the list of resources that need to be loaded
		if (loadIndex == -1)
		{
			if (intermediateImage == null)
				loadingRenderer.initialize();
			try
			{
				if (loadingMap)
				{
					if (loadResources)
					{
						if (!bulkLoader.hasStarted()) {
							// If we are loading maps and resources then this is the first load before enter the actual game state.
							// In this case initialize the default resources
							bulkLoader.start("/loader/Default");
						}
					}

					// Regardless of whether we are loading other resources, add the text file
					// that was specified to be loaded
					bulkLoader.addLine("text,/mapdata/" + textName);
					
					// If we are not loading resources then the bulkloader may not have started yet
					if (!bulkLoader.hasStarted())
						bulkLoader.start();
					
					// If we're not loading resources then just 
					// blast through loading the map data in one go
					if (!loadResources)
					{
						while (!bulkLoader.isDone()) {
							bulkLoader.update();							
						}
						loadIndex = 0;
					}

				}
				// If we are not loading the map then we just want to load the specified resources
				else if (loadResources)
					bulkLoader.start(textName);
				
				
				loadAmount = bulkLoader.getResourceAmount();
			}
			catch (Throwable e)
			{
				Log.debug("Error loading resource list: " + mapName);
				errorMessage = "Error loading resource list: " + mapName;
				e.printStackTrace();
				// System.exit(0);
			}
		}
		else if (!bulkLoader.isDone())
		{
			bulkLoader.update();
		}

		
		
		if (loadingStatus != null)
		{
			loadingStatus.currentIndex = loadIndex;
			loadingStatus.maxIndex = loadAmount;
		}
		
		if (errorMessage != null && bulkLoader.getErrorMessage() != null)
			errorMessage = bulkLoader.getErrorMessage();
		
		if (bulkLoader.isDone())
		{
			if (nextState.getID() == TacticalGame.STATE_GAME_BATTLE && resourceManager.getTriggerEventById(0) != null) {
				ArrayList<Triggerable> trigs = resourceManager.getTriggerEventById(0).getTriggerables();
				for (Triggerable trig : trigs) {
					if (trig instanceof Trigger.TriggerPlayMusic) {
						String mus = ((Trigger.TriggerPlayMusic) trig).getSong();
						resourceManager.addMusicByName(mus);
					}
				}
			}
			
			
			loadingRenderer.doneLoading();
			
			if (intermediateImage != null || loadingRenderer.canTransition(delta)) {
				bulkLoader = null;
				
				// This is the entry point into the actual game. Initialize static variables here
				if (loadingMap && loadResources)
				{
					TacticalGame.ENGINE_CONFIGURATIOR.initialize();
					Panel.intialize(resourceManager);
					SpellResource.initSpells(resourceManager);
	
					loadIndex = loadAmount;
				}
	
				// Only alert the loadable state if resources are being loaded. If they are not being loaded
				// then map data will be updated in the current resource manager
				if (loadResources)	
					nextState.stateLoaded(resourceManager);
				nextState.initAfterLoad();
				
				loadingRenderer.doneLoading();
				
				if (enterNextStateTransition == null)
					game.enterState(nextState.getID(), new EmptyTransition(), new FadeInTransition(Color.black, 500));
				else
					game.enterState(nextState.getID(), new EmptyTransition(), enterNextStateTransition);
			}
		}
		else {
			loadIndex++;
		}		
	}
	
	public void setLoadingInfo(String textName, boolean loadMap, boolean loadResources,
			ResourceManager resourceManager, LoadableGameState nextState,
				LoadingScreenRenderer loadingRenderer) {
		setLoadingInfo(textName, loadMap, loadResources, resourceManager, nextState, loadingRenderer, null, null);
	}

	public void setLoadingInfo(String textName, boolean loadMap, boolean loadResources,
			ResourceManager resourceManager, LoadableGameState nextState,
				LoadingScreenRenderer loadingRenderer, Image intermediateImage, Transition transition)
	{
		System.out.println("IS FIRST LOAD " + loadResources);
		this.errorMessage = null;
		this.textName = textName;
		this.loadingMap = loadMap;
		this.nextState = nextState;
		this.loadingRenderer = loadingRenderer;
		this.loadResources = loadResources;
		this.resourceManager = resourceManager;
		this.loadIndex = -1;
		this.intermediateImage = intermediateImage;
		this.enterNextStateTransition = transition;
		if (bulkLoader == null)
			this.bulkLoader = new BulkLoader(resourceManager);
		loadingStatus = new LoadingStatus();
	}
	
	public void setLoadingRenderer(LoadingScreenRenderer loadingRenderer) {
		this.loadingRenderer = loadingRenderer;
	}

	public void setBulkLoader(BulkLoader bulkLoader) {
		this.bulkLoader = bulkLoader;
	}

	@Override
	public int getID() {
		return stateId;
	}
}
