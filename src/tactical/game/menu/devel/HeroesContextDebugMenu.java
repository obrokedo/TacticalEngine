package tactical.game.menu.devel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import tactical.engine.message.MessageType;
import tactical.engine.message.ShopMessage;
import tactical.engine.message.StringMessage;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.resource.ItemResource;
import tactical.game.sprite.CombatSprite;

public class HeroesContextDebugMenu extends ContextDebugMenu {
	public HeroesContextDebugMenu() {
		this.title = ("HEROES DEBUG");
		this.options.addAll(Arrays.asList("Shop", "Priest", "Add Hero", "Export"));
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		MenuUpdate res = super.handleUserInput(input, stateInfo);
		if (res != MenuUpdate.MENU_NO_ACTION)
			return res;
		
		
		if (input.isKeyDown(KeyMapping.BUTTON_1) ||  input.isKeyDown(KeyMapping.BUTTON_3)) {
			switch (selectedIndex) {
				case 0: // SHOP
					stateInfo.removeMenu(PanelType.PANEL_HEROS_OVERVIEW);
					stateInfo.sendMessage(new ShopMessage(0, 0, ItemResource.getAllItems().stream().mapToInt(s -> s.getItemId()).toArray(), null));
					break;
				case 1: // PRIEST
					stateInfo.removeMenu(PanelType.PANEL_HEROS_OVERVIEW);
					stateInfo.sendMessage(new StringMessage(MessageType.SHOW_PRIEST, null));
					break;
				case 2: // ADD HERO
					stateInfo.addMenu(new AddHeroDebugMenu());
					break;
				case 3: // EXPORT
					ArrayList<String> buffer = new ArrayList<>();
					buffer.add("<partyconfig>");
					buffer.add("<gold amt=" + stateInfo.getClientProfile().getGold() + "/>");
					for (CombatSprite cs : stateInfo.getAllHeroes())
						buffer.add(cs.toXMLString());
					buffer.add("</partyconfig>");
				try {
					Files.write(Paths.get("PartyConfig"), buffer, StandardCharsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "An error occurred trying to save the party configuration file");
				}
					break;
			}		
			return MenuUpdate.MENU_ACTION_LONG;
		}
		return MenuUpdate.MENU_NO_ACTION;
	}	
}
