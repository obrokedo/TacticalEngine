package tactical.game.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import tactical.engine.TacticalGame;
import tactical.engine.config.YesNoMenuRenderer;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.StateInfo;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.MenuListener;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.game.ui.RectUI;
import tactical.game.ui.TextUI;

public class YesNoMenu extends SpeechMenu
{
	private boolean yesSelected = true;
	private int[] yesTriggers = null;
	private int[] noTriggers = null;
	private boolean consumeInput = true;
	private RectUI goldPanel = null;
	private TextUI goldTitleText = null, goldAmountText = null;
	
	private YesNoMenuRenderer renderer;

	public YesNoMenu(String text, int[] yesTriggers, int[] noTriggers, StateInfo stateInfo) {
		this(text, Trigger.TRIGGER_LIST_NONE, null, stateInfo, null);
		this.yesTriggers = yesTriggers;
		this.noTriggers = noTriggers;
	}
	
	public YesNoMenu(String text, int[] yesTriggers, int[] noTriggers, Portrait portrait, StateInfo stateInfo) {
		this(text, Trigger.TRIGGER_LIST_NONE, portrait, stateInfo, null);
		this.yesTriggers = yesTriggers;
		this.noTriggers = noTriggers;
	}
	
	public YesNoMenu(String text, int[] yesTriggers, int[] noTriggers, StateInfo stateInfo, boolean showGold) {
		this(text, Trigger.TRIGGER_LIST_NONE, null, stateInfo, null, showGold);
		this.yesTriggers = yesTriggers;
		this.noTriggers = noTriggers;
		
	}
	
	public YesNoMenu(String text, StateInfo stateInfo, MenuListener listener) {
		this(text, Trigger.TRIGGER_LIST_NONE, null, stateInfo, listener, false);
	}
	
	public YesNoMenu(String text, int[] triggerIds,
			Portrait portrait, StateInfo stateInfo, MenuListener listener) {
		this(text, triggerIds, portrait, stateInfo, listener, false);	
	}

	public YesNoMenu(String text, int[] triggerIds,
			Portrait portrait, StateInfo stateInfo, MenuListener listener, boolean showGold) {
		super(replaceLastHardstop(text), stateInfo.getPaddedGameContainer(),triggerIds, portrait, listener);
		
		renderer = TacticalGame.ENGINE_CONFIGURATIOR.getYesNoMenuRenderer();
		renderer.initialize(stateInfo);
		
		if (showGold) {
			goldPanel = new RectUI(243, 148, 62, 32);
			goldTitleText = new TextUI("Gold", 249, 144);
			goldAmountText = new TextUI(stateInfo.getClientProfile().getGold() + "", 249, 156);
		}
	}	
	
	private static String replaceLastHardstop(String text) {
		if (text.endsWith("<hardstop>")) {
			return text.substring(0, text.length() - 10);
		}
		return text;
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo) {
		super.handleUserInput(input, stateInfo);
		if (isDone) {
			// This makes sure that if the user is holding down button 1 or 3
			// to speed up text that they don't accidently make a selection before
			// actually seeing the menu
			if (consumeInput) {
				input.clear();
				consumeInput = false;
			}
			if (input.isKeyDown(KeyMapping.BUTTON_1) || input.isKeyDown(KeyMapping.BUTTON_3))
			{
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				// Handle unlistened to selections
				if (this.getMenuListener() == null && yesTriggers != null && noTriggers != null) {
					if (yesSelected) {
						if (yesTriggers.length > 0)
							for (int yesTrigger : yesTriggers)
								if (yesTrigger != -1)
									stateInfo.getResourceManager().getTriggerEventById(yesTrigger).perform(stateInfo);
					}
					else {
						if (noTriggers.length > 0)
							for (int noTrigger : noTriggers)
								if (noTrigger != -1)
									stateInfo.getResourceManager().getTriggerEventById(noTrigger).perform(stateInfo);
					}
				}
				if (stateInfo != null)
					stateInfo.sendMessage(MessageType.MENU_CLOSED);
				return MenuUpdate.MENU_CLOSE;
			}
			else if (!yesSelected && input.isKeyDown(KeyMapping.BUTTON_LEFT))
			{
				renderer.yesPressed();
				yesSelected = true;
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
				return MenuUpdate.MENU_ACTION_SHORT;
			}
			else if (yesSelected && input.isKeyDown(KeyMapping.BUTTON_RIGHT))
			{
				renderer.noPressed();
				yesSelected = false;
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menumove", 1f, false));
				return MenuUpdate.MENU_ACTION_SHORT;
			}
			else if (input.isKeyDown(KeyMapping.BUTTON_2))
			{
				stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "menuselect", 1f, false));
				yesSelected = false;
				if (stateInfo != null)
					stateInfo.sendMessage(MessageType.MENU_CLOSED);
				return MenuUpdate.MENU_CLOSE;
			}
		}
		return MenuUpdate.MENU_NO_ACTION;
	}

	/**
	 * Override speech completed, as we don't actually want to do anything
	 * once speech is done, we need to wait for user selection
	 */
	@Override
	protected MenuUpdate speechCompleted(StateInfo stateInfo) {
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		super.update(delta, stateInfo);
		renderer.update(delta, stateInfo);
		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics)
	{
		super.render(gc, graphics);
		if (menuIsMovedIn && isDone)
		{
			renderer.render(gc, graphics);
			if (goldPanel != null) {
				goldPanel.drawPanel(graphics);
				goldTitleText.drawText(graphics, Color.white);
				goldAmountText.drawText(graphics, Color.white);
			}
		}
	}

	@Override
	public Object getExitValue() {
		return yesSelected;
	}
}
