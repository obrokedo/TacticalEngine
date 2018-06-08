package tactical.game.constants;

public class TextSpecialCharacters {
	public static final String CHAR_PAUSE = "<pause>";
	public static final String CHAR_SOFT_STOP = "<softstop>";
	public static final String CHAR_HARD_STOP = "<hardstop>";
	public static final String CHAR_LINE_BREAK = "<linebreak>";
	public static final String CHAR_NEXT_CIN = "<nextcin>";
	public static final String REPLACE_VALUE = "\\Q<value>\\E";
	
	public static final String INTERNAL_CHAR_PAUSE = "{";
	public static final String INTERNAL_SOFT_STOP = "}";
	public static final String INTERNAL_HARD_STOP = "]";
	public static final String INTERNAL_LINE_BREAK = "[";
	public static final String INTERNAL_NEXT_CIN = "|";
	
	/**
	 * Replace all of the user specified control tags with the single character values that
	 * are parsed internally
	 * 
	 * @param text the text to replace control tags in
	 * @return the text with all control tags replaced
	 */
	public static String replaceControlTagsWithInternalValues(String text) {
		text = text.replaceAll(TextSpecialCharacters.CHAR_HARD_STOP, TextSpecialCharacters.INTERNAL_HARD_STOP);
		text = text.replaceAll(TextSpecialCharacters.CHAR_SOFT_STOP, TextSpecialCharacters.INTERNAL_SOFT_STOP);
		text = text.replaceAll(TextSpecialCharacters.CHAR_LINE_BREAK, TextSpecialCharacters.INTERNAL_LINE_BREAK);
		text = text.replaceAll(TextSpecialCharacters.CHAR_NEXT_CIN, TextSpecialCharacters.INTERNAL_NEXT_CIN);
		text = text.replaceAll(TextSpecialCharacters.CHAR_PAUSE, TextSpecialCharacters.INTERNAL_CHAR_PAUSE);
		return text;
	}
}
