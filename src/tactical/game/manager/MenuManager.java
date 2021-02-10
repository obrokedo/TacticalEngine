package tactical.game.manager;

import java.util.ArrayList;

import tactical.engine.message.IntMessage;
import tactical.engine.message.Message;
import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.SpeechBundleMessage;
import tactical.engine.message.SpeechMessage;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.message.StringMessage;
import tactical.game.menu.HeroStatMenu;
import tactical.game.menu.HeroesStatMenu;
import tactical.game.menu.Menu;
import tactical.game.menu.Menu.MenuUpdate;
import tactical.game.menu.advisor.AdvisorMenu;
import tactical.game.menu.advisor.ChangePartyMenu;
import tactical.game.menu.advisor.DepositItemMenu;
import tactical.game.menu.advisor.RetrieveItemMenu;
import tactical.game.menu.advisor.StorageMenu;
import tactical.game.menu.MiniMapPanel;
import tactical.game.menu.MultiHeroJoinMenu;
import tactical.game.menu.PriestMenu;
import tactical.game.menu.SelectHeroMenu;
import tactical.game.menu.SpeechMenu;
import tactical.game.menu.SystemMenu;
import tactical.game.menu.YesNoMenu;
import tactical.game.menu.battle.BattleOptionMenu;
import tactical.game.menu.devel.DebugMenu;
import tactical.game.menu.shop.ShopBuyMenu;
import tactical.game.menu.shop.ShopChooseItemMenu;
import tactical.game.menu.shop.ShopOptionsMenu;
import tactical.game.menu.town.TownChooseItemMenu;
import tactical.game.menu.town.TownItemOptionMenu;
import tactical.game.menu.town.TownMenu;
import tactical.game.resource.HeroResource;
import tactical.game.sprite.CombatSprite;
import tactical.game.text.Speech;
import tactical.game.text.YesNoSpeech;

public class MenuManager extends Manager
{	
	@Override
	public void initialize()
	{
	}

	public boolean isBlocking()
	{
		return stateInfo.areMenusDisplayed();
	}

	public void update(long delta)
	{
		
		if (stateInfo.areMenusDisplayed()) {
			Menu topMenu = stateInfo.getTopMenu();
			handleMenuUpdate(topMenu.update(delta, stateInfo), topMenu);

			if (System.currentTimeMillis() > stateInfo.getInputDelay())
				handleMenuUpdate(topMenu.handleUserInput(stateInfo.getInput(), stateInfo), topMenu);
		}
	}

	private void handleMenuUpdate(MenuUpdate menuUpdate, Menu updatedMenu)
	{
		switch (menuUpdate)
		{
			case MENU_CLOSE:
				if (updatedMenu instanceof SpeechMenu)
				{
					stateInfo.setWaiting();
					stateInfo.sendMessage(MessageType.WAIT);
				}
				stateInfo.removeMenu(updatedMenu);
				if (updatedMenu.getMenuListener() != null)
				{
					updatedMenu.getMenuListener().valueSelected(stateInfo, updatedMenu.getExitValue());
					updatedMenu.getMenuListener().menuClosed();
				}

				stateInfo.setInputDelay(System.currentTimeMillis() + 200);
				break;
			case MENU_ACTION_SHORT:
				stateInfo.setInputDelay(System.currentTimeMillis() + 75);
				break;
			case MENU_ACTION_LONG:
				stateInfo.setInputDelay(System.currentTimeMillis() + 200);
				break;
			case MENU_NEXT_ACTION:
				stateInfo.sendMessage(MessageType.CIN_NEXT_ACTION);
				break;
			default:
				break;
		}
	}

	@Override
	public void recieveMessage(Message message)
	{
		switch (message.getMessageType())
		{
			case SPEECH:
				if (message instanceof SpeechMessage) {
					SpeechMessage spm = (SpeechMessage) message;
					if (spm.isYesNoMessage())
						stateInfo.addMenu(new YesNoMenu(spm.getText(), spm.getTriggerId(), spm.getNoTriggerId(), stateInfo));
					else
						stateInfo.addMenu(new SpeechMenu(spm.getText(), spm.getTriggerId(), spm.getPortrait(), stateInfo));
				} else if (message instanceof SpeechBundleMessage) {
					
					SpeechBundleMessage sbm = (SpeechBundleMessage) message;
					Speech speech = stateInfo.getResourceManager().getSpeechesById(sbm.getSpeechId()).get(sbm.getSpeechIndex());
					speech.initialize();
					if (!(speech instanceof YesNoSpeech))
						stateInfo.addMenu(new SpeechMenu(speech, stateInfo));
					else {
						YesNoSpeech yns = (YesNoSpeech) speech;
						stateInfo.addMenu(new YesNoMenu(speech.getMessage(), yns.getYesTrigger(), 
								yns.getNoTrigger(), yns.getPortrait(stateInfo), stateInfo));
					}
				}
				break;
			case SHOW_SYSTEM_MENU:
				stateInfo.addSingleInstanceMenu(new SystemMenu(stateInfo.getPaddedGameContainer()));
				break;
			case SHOW_SHOP:
				ShopMessage sm = (ShopMessage) message;
				// stateInfo.addSingleInstanceMenu(new ShopMenuTabled(stateInfo, sm.getSellPercent(), sm.getBuyPercent(), sm.getItemIds()));
				stateInfo.addSingleInstanceMenu(new ShopOptionsMenu(sm, stateInfo));
				break;
			case SHOW_SHOP_DEALS:
			case SHOW_SHOP_BUY:
				sm = (ShopMessage) message;
				stateInfo.addSingleInstanceMenu(new ShopBuyMenu(stateInfo, sm));
				break;
			case SHOW_SHOP_REPAIR:
			case SHOW_SHOP_SELL:
				sm = (ShopMessage) message;
				stateInfo.addSingleInstanceMenu(new ShopChooseItemMenu(stateInfo, null, sm));
				break;
			case SHOW_HEROES:
				stateInfo.addSingleInstanceMenu(new HeroesStatMenu(stateInfo));
				stateInfo.setInputDelay(System.currentTimeMillis() + 200);
				break;
			case SHOW_HERO:
				stateInfo.addMenu(new HeroStatMenu(stateInfo.getPaddedGameContainer(), ((SpriteContextMessage) message).getCombatSprite(
						stateInfo.getAllHeroes()), stateInfo));
				break;
			case SHOW_PRIEST:
				StringMessage stringMessage = (StringMessage) message;
				stateInfo.addMenu(new PriestMenu(stringMessage.getString(), stateInfo));
				break;
			case SHOW_ADVISOR_MENU:
				stateInfo.addMenu(new AdvisorMenu(stateInfo));
				break;
			case SHOW_PANEL_MULTI_JOIN_CHOOSE:
				ArrayList<CombatSprite> heroesToChooseList = new ArrayList<>();
				((SpriteContextMessage) message).getSpriteIds().forEach(id -> heroesToChooseList.add(HeroResource.getHero(id)));
				heroesToChooseList.forEach(cs -> { cs.initializeSprite(stateInfo.getResourceManager()); cs.initializeStats(); });
				stateInfo.addSingleInstanceMenu(new MultiHeroJoinMenu(heroesToChooseList, stateInfo));
				break;
			case SHOW_DEBUG:
				stateInfo.addMenu(new DebugMenu(stateInfo));
				break;
			case SHOW_BATTLE_OPTIONS:
				stateInfo.addMenu(new BattleOptionMenu(stateInfo));
				break;
			case SHOW_MINI_MAP:
				stateInfo.addMenu(new MiniMapPanel(stateInfo.getCurrentMap(), stateInfo));
				break;			
			case SHOW_CHANGE_PARTY_MENU:
				stateInfo.addMenu(new ChangePartyMenu(stateInfo));
				break;
			case SHOW_STORAGE_MENU:
				stateInfo.addMenu(new StorageMenu(stateInfo));
				break;
			case SHOW_TOWN_MENU:
				stateInfo.addMenu(new TownMenu(stateInfo));
				break;
			case SHOW_TOWN_SELECT_ITEM:
				IntMessage im = (IntMessage) message;
				stateInfo.addMenu(new TownChooseItemMenu(stateInfo, im.getValue()));
				break;
			case SHOW_TOWN_ITEM_OPTION_MENU:				
				stateInfo.addMenu(new TownItemOptionMenu(stateInfo));
				break;
			case SHOW_DEPOSIT_MENU:
				stateInfo.addMenu(new DepositItemMenu(stateInfo, null));
				break;
			case SHOW_WITHDRAW_MENU:
				stateInfo.addMenu(new RetrieveItemMenu(stateInfo));
				break;
			default:
				break;
		}
	}
}
