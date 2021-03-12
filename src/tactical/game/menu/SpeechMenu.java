package tactical.game.menu;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.AudioMessage;
import tactical.engine.message.MessageType;
import tactical.engine.state.CinematicState;
import tactical.engine.state.StateInfo;
import tactical.game.Timer;
import tactical.game.constants.TextSpecialCharacters;
import tactical.game.hudmenu.Panel;
import tactical.game.input.KeyMapping;
import tactical.game.input.UserInput;
import tactical.game.listener.MenuListener;
import tactical.game.text.Speech;
import tactical.game.trigger.Trigger;
import tactical.game.ui.PaddedGameContainer;
import tactical.utils.StringUtils;

public class SpeechMenu extends Menu implements MenuListener
{
	private int x = 15;
	private int y = 58;
	private int width;
	protected ArrayList<String> panelText;
	private int textIndex = 0;
	private int[] triggerIds = Trigger.TRIGGER_LIST_NONE;
	private Portrait portrait;
	protected boolean menuIsMovedIn = false;
	private boolean isAttackCinematic = false;

	private boolean textMoving = true;
	private int textMovingIndex = 0;
	private long waitUntil = -1;
	private String waitingOn = null;
	private Timer timer;
	private Speech speech = null;
	protected boolean isDone = false;
	private int linesSincePause = 0;
	private Supplier<Boolean> onCloseSupplier = null;

	private int LINES_DISPLAYED_IN_TOWN = 3;
	private int LINES_DISPLAYED_IN_BATTLE = 2;
	public static int SPEECH_SPEED = 1;

	/**
	 * Constructor to create a SpeechMenu to be displayed in an Attack Cinematic
	 *
	 * @param text the text that should be displayed in the speech menu
	 * @param gc the graphics container that the menu will be displayed in
	 */
	public SpeechMenu(String text, PaddedGameContainer gc)
	{
		this(text, gc, Trigger.TRIGGER_LIST_NONE, null, null);
		y = 0;
		menuIsMovedIn = true;
		this.isAttackCinematic = true;
	}

	/**
	 * Constructor to create a SpeechMenu with no portrait, triggers or listener this should
	 * NOT be used in Attack Cinematics because the attack cinematic has no notion
	 * of the StateInfo
	 *
	 * @param text the text that should be displayed in the speech menu
	 * @param stateInfo the stateinfo that resources should be retrieved from
	 */
	public SpeechMenu(String text, StateInfo stateInfo)
	{
		this(text, stateInfo.getPaddedGameContainer(), Trigger.TRIGGER_LIST_NONE, null, null);
	}
	
	public SpeechMenu(String text, StateInfo stateInfo, Supplier<Boolean> onCloseSupplier)
	{
		this(text, stateInfo.getPaddedGameContainer(), Trigger.TRIGGER_LIST_NONE, null, null);
		this.onCloseSupplier = onCloseSupplier;
		this.listener = this;
	}
	
	public SpeechMenu(String text, Portrait portrait, StateInfo stateInfo, Supplier<Boolean> onCloseSupplier)
	{
		this(text, stateInfo.getPaddedGameContainer(), Trigger.TRIGGER_LIST_NONE, portrait, null);
		this.onCloseSupplier = onCloseSupplier;
		this.listener = this;
	}
	
	/**
	 * Constructor to create a SpeechMenu with no triggers, but with a portrait and a specified listener. This should
	 * NOT be used in Attack Cinematics because the attack cinematic has no notion
	 * of the StateInfo
	 *
	 * @param text the text that should be displayed in the speech menu
	 * @param portrait
	 * @param listener
	 * @param stateInfo the stateinfo that resources should be retrieved from
	 */
	public SpeechMenu(String text, Portrait portrait, MenuListener listener, StateInfo stateInfo)
	{
		this(text, stateInfo.getPaddedGameContainer(), Trigger.TRIGGER_LIST_NONE, portrait, listener);
	}
	
	/**
	 * Constructor to create a SpeechMenu from user speech bundle. Trigger and
	 * portrait will be retrieved from the speech bundle 
	 * 
	 * @param speech The speech bundle to display
	 * @param stateInfo the stateinfo that resources should be retrieved from
	 */
	public SpeechMenu(Speech speech, StateInfo stateInfo) {
		this(speech.getMessage(), stateInfo.getPaddedGameContainer(), speech.getTriggerIds(), speech.getPortrait(stateInfo), null);
		this.speech = speech;
	}
	
	/**
	 * Constructor to create a SpeechMenu with a given string, trigger, portrait 
	 * with no menu listener 
	 * 
	 * @param text
	 * @param triggerId
	 * @param portrait
	 * @param stateInfo
	 */
	public SpeechMenu(String text, int[] triggerIds,
			Portrait portrait, StateInfo stateInfo)
	{
		this(text, stateInfo.getPaddedGameContainer(), triggerIds, portrait, null);
	}

	public SpeechMenu(String text, PaddedGameContainer gc, int[] triggerIds,
			Portrait portrait, MenuListener listener)
	{
		super(PanelType.PANEL_SPEECH);
		this.listener = listener;
		width = PaddedGameContainer.GAME_SCREEN_SIZE.width - 30;
		x = 15;
		this.triggerIds = triggerIds;
		timer = new Timer(18);
		
		
		initialize(text, portrait);
	}

	private void initialize(String text, Portrait portrait) {
		textIndex = 0;
		menuIsMovedIn = false;

		textMoving = true;
		textMovingIndex = 0;
		waitUntil = -1;
		waitingOn = null;
		
		text = TextSpecialCharacters.replaceControlTagsWithInternalValues(text);

		int maxTextWidth = width - 13;
		int spaceWidth = StringUtils.getStringWidth("_", SPEECH_FONT);
		String[] splitText = text.split(" ");
		int currentLineWidth = 0;
		String currentLine = "";

		panelText = new ArrayList<String>();

		for (int i = 0; i < splitText.length; i++)
		{
			int wordWidth = StringUtils.getStringWidth(splitText[i], SPEECH_FONT);

			if (wordWidth + currentLineWidth <= maxTextWidth)
			{
				boolean lineBreak = false;
				if (splitText[i].contains(TextSpecialCharacters.INTERNAL_LINE_BREAK))
					lineBreak = true;

				currentLine += " " + splitText[i].replace(TextSpecialCharacters.INTERNAL_LINE_BREAK, "");
				currentLineWidth += wordWidth + spaceWidth;

				if (lineBreak)
				{
					currentLineWidth = 0;
					panelText.add(currentLine.trim());
					currentLine = "";
				}
			}
			else
			{
				i--;
				currentLineWidth = 0;
				panelText.add(currentLine.trim());
				currentLine = "";
			}
		}

		if (currentLineWidth > 0)
			panelText.add(currentLine.trim());


		if (portrait != null)
		{
			this.portrait = portrait;
			this.portrait.setTalking(true);
		}
		else
			this.portrait = null;
	}

	@Override
	public void render(PaddedGameContainer gc, Graphics graphics)
	{
		int posY = LINES_DISPLAYED_IN_TOWN - 1;
		if (isAttackCinematic)
			posY =  LINES_DISPLAYED_IN_BATTLE -1;

		if (isAttackCinematic)
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, PaddedGameContainer.GAME_SCREEN_SIZE.height - (posY + 1) * 20 + y - 3, width, 
					(posY + 1) * (20 + (posY == 1 ? 1 : 0)) + 1, graphics, null);
		else
			TacticalGame.ENGINE_CONFIGURATIOR.getPanelRenderer().render(x, PaddedGameContainer.GAME_SCREEN_SIZE.height - (posY + 1) * 20 + y - 1, width, 
					(posY + 1) * (20 + (posY == 1 ? 1 : 0)) - 1, graphics, null);
		
		if (!menuIsMovedIn)
			return;

		graphics.setFont(SPEECH_FONT);
		graphics.setColor(Panel.COLOR_FOREFRONT);

		for (int i = Math.max(0, textIndex - posY); i <= textIndex; i++)
		{
			if (isAttackCinematic)
				StringUtils.drawString((i == textIndex ? panelText.get(i).substring(0, textMovingIndex) : panelText.get(i)), x + 8,
						PaddedGameContainer.GAME_SCREEN_SIZE.height - (posY + 1) * 24 + 16 + (i - textIndex + (textIndex >= posY ? posY : textIndex)) * 15 - (posY == 1 ? 5 : 0), graphics);
			else
				StringUtils.drawString((i == textIndex ? panelText.get(i).substring(0, textMovingIndex) : panelText.get(i)), x + 8,
						PaddedGameContainer.GAME_SCREEN_SIZE.height - (posY + 1) * 24 + 18 + (i - textIndex + (textIndex >= posY ? posY : textIndex)) * 15 - (posY == 1 ? 5 : 0), graphics);
		}

		if (portrait != null)
		{
			portrait.render(x, y + 12, graphics);
		}
	}

	@Override
	public MenuUpdate update(long delta, StateInfo stateInfo) {
		super.update(delta, stateInfo);
		
		if (portrait != null)
			portrait.update(delta);

		timer.update(delta * SPEECH_SPEED);
		
		while (timer.perform())
		{
			if (!menuIsMovedIn)
			{
				if (y <= 0)
				{
					menuIsMovedIn = true;
				}
				else
					y = Math.max(y - 8, 0);
			}

			for (int i = 0; i < (CinematicState.cinematicSpeed > 1 ? CinematicState.cinematicSpeed : 1); i++)
			{
				if (textMoving)
				{
					if (textMovingIndex + 1 > panelText.get(textIndex).length())
					{
						if (textIndex + 1 < panelText.size())
						{
							if (++linesSincePause >= (isAttackCinematic ? LINES_DISPLAYED_IN_BATTLE : LINES_DISPLAYED_IN_TOWN))
							{
								textMoving = false;
								if (portrait != null)
									portrait.stopTalkingAfterAnimationComplete();
								waitingOn = TextSpecialCharacters.INTERNAL_HARD_STOP;
							} else {
								textMovingIndex = 0;
								textIndex++;
							}
						}
						else
						{
							if (speech == null || !speech.hasMoreSpeech()) {
								isDone = true;	
								return speechCompleted(stateInfo);
							} else {
								this.initialize(speech.getMessage(), speech.getPortrait(stateInfo));
							}
						}
						// textMoving = false;
					}
					else
					{
						String nextLetter = panelText.get(textIndex).substring(textMovingIndex, textMovingIndex + 1);
						if (nextLetter.equalsIgnoreCase(TextSpecialCharacters.INTERNAL_HARD_STOP))
						{
							textMoving = false;
							if (portrait != null)
								portrait.stopTalkingAfterAnimationComplete();
							waitingOn = TextSpecialCharacters.INTERNAL_HARD_STOP;
						}
						else if (nextLetter.equalsIgnoreCase(TextSpecialCharacters.INTERNAL_SOFT_STOP))
						{
							textMoving = false;
							if (portrait != null)
								portrait.stopTalkingAfterAnimationComplete();

							String[] softSplit = panelText.get(textIndex).substring(textMovingIndex).split(" ");

							if (softSplit[0].replaceFirst("[0-9]", "").length() != softSplit[0].length())
							{
								waitUntil = System.currentTimeMillis() + Integer.parseInt(softSplit[0].substring(1));
								waitingOn = softSplit[0];
							}
							else
							{
								waitUntil = System.currentTimeMillis() + 2500;
								waitingOn = TextSpecialCharacters.INTERNAL_SOFT_STOP;
							}
						}
						else if (nextLetter.equalsIgnoreCase(TextSpecialCharacters.INTERNAL_CHAR_PAUSE))
						{
							textMoving = false;
							if (portrait != null)
								portrait.stopTalkingAfterAnimationComplete();
							waitUntil = System.currentTimeMillis() + 400;
							waitingOn = TextSpecialCharacters.INTERNAL_CHAR_PAUSE;
						}
						else if (nextLetter.equalsIgnoreCase(TextSpecialCharacters.INTERNAL_NEXT_CIN))
						{
							panelText.set(textIndex, panelText.get(textIndex).replaceFirst("\\" + TextSpecialCharacters.INTERNAL_NEXT_CIN, ""));
							return MenuUpdate.MENU_NEXT_ACTION;
						}

						if (textMoving) {
							textMovingIndex += 1;
							if (portrait != null)
								if (nextLetter.equalsIgnoreCase(".") || 
										nextLetter.equalsIgnoreCase("!") || nextLetter.equalsIgnoreCase("?")) {
									if (portrait.isTalking())
										portrait.stopTalkingAfterAnimationComplete();
								} else {
									portrait.setTalking(true);
								}
						}
						else
							panelText.set(textIndex, panelText.get(textIndex).replaceFirst("\\" + waitingOn, ""));
						// Only display the speech blip when we are not in battle
						if (textMovingIndex % 6 == 0 && !isAttackCinematic)
							stateInfo.sendMessage(new AudioMessage(MessageType.SOUND_EFFECT, "speechblip", .15f, false));
					}
				}
			}

			if (waitUntil != -1 && waitUntil <= System.currentTimeMillis())
			{
				textMoving = true;
				String nextLetter = "";
				if (panelText.get(textIndex).length() > textMovingIndex + 1) 
					nextLetter = panelText.get(textIndex).substring(textMovingIndex, textMovingIndex + 1);
				if (portrait != null && !nextLetter.equalsIgnoreCase(".") && 
						!nextLetter.equalsIgnoreCase("!") && !nextLetter.equalsIgnoreCase("?"))
					portrait.setTalking(true);
				waitUntil = -1;
				linesSincePause = 0;
			}
		}

		return MenuUpdate.MENU_NO_ACTION;
	}

	protected MenuUpdate speechCompleted(StateInfo stateInfo) {
		if (triggerIds.length > 0) {
			for (int triggerId : triggerIds) {
				if (triggerId == -1)
					break;
				Log.debug("Speech Menu: Send Trigger " + triggerId);
				stateInfo.getResourceManager().getTriggerEventById(triggerId).perform(stateInfo);
			}
		}
		else if (stateInfo != null)
			stateInfo.sendMessage(MessageType.MENU_CLOSED);
		return MenuUpdate.MENU_CLOSE;
	}

	@Override
	public MenuUpdate handleUserInput(UserInput input, StateInfo stateInfo)
	{
		if (!menuIsMovedIn)
			return MenuUpdate.MENU_NO_ACTION;

		if (input.isKeyDown(KeyMapping.BUTTON_3) || TacticalGame.TEST_MODE_ENABLED)
		{
			if (waitingOn != null)
			{
				waitingOn = null;
				waitUntil = -1;
				textMoving = true;
				if (portrait != null && textIndex + 1 < panelText.size())
					portrait.setTalking(true);
				linesSincePause = 0;
			}
		}

		return MenuUpdate.MENU_NO_ACTION;
	}

	@Override
	public boolean makeAddSounds() {
		
		return true;
	}

	@Override
	public boolean makeRemoveSounds() {
		
		return true;
	}

	@Override
	public void valueSelected(StateInfo stateInfo, Object value) {
		if (onCloseSupplier != null)
			onCloseSupplier.get();
	}

	@Override
	public void menuClosed() {
		// TODO Auto-generated method stub
		
	}
}
