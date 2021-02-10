package tactical.game.menu.advisor;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.newdawn.slick.Image;

import tactical.engine.TacticalGame;
import tactical.engine.config.EngineConfigurationValues;
import tactical.engine.config.MenuConfiguration;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.listener.MenuListener;
import tactical.game.menu.Portrait;
import tactical.game.menu.QuadMenu;
import tactical.game.menu.SelectHeroMenu;
import tactical.game.menu.SpeechMenu;
import tactical.game.sprite.CombatSprite;
import tactical.utils.StringUtils;

public class ChangePartyMenu extends QuadMenu implements MenuListener {

	public enum ChangePartyStep {
		ADD_HERO,
		REMOVE_HERO,
		INSPECT_HERO
	}

	private ChangePartyStep currentStep;
	
	public ChangePartyMenu(StateInfo stateInfo) {
		super(PanelType.PANEL_CHANGE_PARTY, stateInfo);
				
		this.portrait = Portrait.getPortrait(-1, -1, 
				TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().getAdvisorPortraitAnimFile(), stateInfo);
		
		menuConfig = TacticalGame.ENGINE_CONFIGURATIOR.getMenuConfiguration();
		
		icons = new Image[8];

		icons[0] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(25, 0);
		icons[1] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(26, 0);
		icons[2] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 0);
		icons[3] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(6, 0);
		icons[4] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(25, 1);
		icons[5] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(26, 1);
		icons[6] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(19, 1);
		icons[7] = stateInfo.getResourceManager().getSpriteSheet("actionicons").getSubImage(6, 1);
		enabled = new boolean[] {true, true, true, false};
		text = new String[] {"Join", "Rest", "Inspect", "Talk"};
	}

	@Override
	public void initialize() {
		
		
	}

	@Override
	protected MenuUpdate onBack() {
		stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuback", 1f, false));
		return MenuUpdate.MENU_CLOSE;
	}
	
	public boolean showCurrentSelectHeroMenu() {
		currentStep = ChangePartyStep.REMOVE_HERO;
		stateInfo.addMenu(new SelectHeroMenu(stateInfo.getClientProfile().getHeroesInParty().stream().filter(h -> !h.isLeader()).collect(Collectors.toList()), stateInfo, this));
		return true;
	}
	
	public boolean showAbsentSelectHeroMenu() {
		currentStep = ChangePartyStep.ADD_HERO;
		ArrayList<CombatSprite> heroes = stateInfo.getClientProfile().getHeroesInParty();
		ArrayList<CombatSprite> notSelected = new ArrayList<>();
		for (CombatSprite cs : stateInfo.getAllHeroes()) {
			if (!heroes.stream().anyMatch(h -> h.getId() == cs.getId()))
				notSelected.add(cs);
		}
		stateInfo.addMenu(new SelectHeroMenu(notSelected, stateInfo, this));
		return true;
	}
	
	public boolean showAllSelectHeroMenu() {
		currentStep = ChangePartyStep.INSPECT_HERO;
		stateInfo.addMenu(new SelectHeroMenu(stateInfo.getAllHeroes(), stateInfo, this));
		return true;
	}

	@Override
	protected MenuUpdate onConfirm() {
		switch (selected) {
			case UP:
				EngineConfigurationValues configValues = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues();
				int maxHeros = configValues.getMaxPartySize();
				
				// We've already got everyone available in the party
				if (stateInfo.getClientProfile().getHeroes().size() == stateInfo.getClientProfile().getHeroesInParty().size()) {
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyNoOneToJoinText(), -1, this.portrait, stateInfo));
				// Group is full
				} else if (stateInfo.getClientProfile().getHeroesInParty().size() == maxHeros) { 
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyGroupIsFull(), -1, this.portrait, stateInfo));
				// We can add someone
				} else {
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyWhoToAdd(), this.portrait, stateInfo, this::showAbsentSelectHeroMenu));
				}
				break;
			case LEFT:
				// Check if there any non-leaders left in the group
				if (stateInfo.getClientProfile().getHeroesInParty().stream().filter(h -> !h.isLeader()).findAny().isPresent()) {
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyWhoToRemove(), this.portrait,stateInfo, this::showCurrentSelectHeroMenu));
				// No one to remove
				} else {
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyNoOneToRemove(), -1, this.portrait,stateInfo));
				}					
				break;
			case RIGHT:
				stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyWhoToInpsect(), this.portrait, stateInfo, this::showAllSelectHeroMenu));
				break;
			case DOWN:
				break;
			default:
				break;		
		}
		return MenuUpdate.MENU_ACTION_LONG;
	}

	@Override
	protected int getTextboxWidth() {
		switch (selected) {
			case RIGHT:
				return 70;
		}
	
		return super.getTextboxWidth();
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		if (value != null) {
			switch (currentStep) {
				case ADD_HERO:
					stateInfo.getClientProfile().addHeroToParty((CombatSprite) value);
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyMemberAdded(((CombatSprite) value).getName())
							, -1, this.portrait, stateInfo));
					break;
				case INSPECT_HERO:
					String desc = ((CombatSprite) value).getCurrentProgression().getClassDescription();
					if (StringUtils.isEmpty(desc))
						desc = ((CombatSprite) value).getName() + " enjoys staring at walls and sorting small pieces of paper.<softstop> His favorite food is hotdog and his greatest fear is slow moving objects.<hardstop>";
					stateInfo.addMenu(new SpeechMenu(desc, -1, this.portrait, stateInfo));
					break;
				case REMOVE_HERO:
					stateInfo.getClientProfile().removeHeroFromParty((CombatSprite) value);
					stateInfo.addMenu(new SpeechMenu(menuConfig.getPartyMemberRemoved(((CombatSprite) value).getName()),
							-1, this.portrait, stateInfo));
					break;
				default:
					break;				
			}
		}
		
	}

	@Override
	public void menuClosed() {
		
	}
	
	
}
