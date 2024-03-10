package tactical.engine.message;

public enum MessageType
{
	INITIALIZE_BATTLE,
	INITIALIZE_BATTLE_FROM_LOAD,
	NEXT_TURN,
	COMBATANT_TURN,
	ATTACK_PRESSED,
	HIDE_ATTACKABLE,
	SHOW_MOVEABLE,
	SHOW_BATTLEMENU,
	USE_ITEM,
	GIVE_ITEM,
	RESET_SPRITELOC,
	MOVETO_SPRITELOC,
	SET_INIT_ORDER,
	TURN_ACTIONS,
	CLIENT_ID,
	TARGET_SPRITE,
	BATTLE_RESULTS,
	SELECT_SPELL,
	SHOW_SPELLMENU,
	SEND_INTERNAL_MESSAGE,
	OVERLAND_MOVE_MESSAGE,
	SPEECH,
	COMPLETE_QUEST,
	UNCOMPLETE_QUEST,
	LOAD_MAP,
	START_BATTLE,
	CONTINUE,
	SHOW_WAIT,
	HIDE_WAIT,
	SHOW_SYSTEM_MENU,
	SHOW_SHOP,
	SHOW_SHOP_BUY,
	SHOW_SHOP_DEALS,
	SHOW_SHOP_SELL,
	SHOW_SHOP_REPAIR,
	SHOW_HEROES,
	SHOW_HERO,
	SHOW_TOWN_SELECT_ITEM,
	SHOW_TOWN_ITEM_OPTION_MENU,
	SHOW_BATTLE_OPTIONS,
	SHOW_MINI_MAP,
	INTIIALIZE_MANAGERS,
	INITIALIZE_STATE_INFO,
	SEND_HEROES,
	SHOW_PRIEST,
	SAVE,
	CLIENT_REGISTRATION,
	ASSIGN_HERO,
	SHOW_ASSIGN_HERO,
	PLAYER_LIST,
	START_GAME,
	PLAYER_END_TURN,
	SHOW_ITEM_MENU,
	SHOW_ITEM_OPTION_MENU,
	SHOW_ADVISOR_MENU,
	SHOW_CHANGE_PARTY_MENU,
	SHOW_CHOOSE_HERO_MENU,
	SHOW_STORAGE_MENU,
	SHOW_DEPOSIT_MENU,
	SHOW_WITHDRAW_MENU,
	SHOW_TOWN_MENU,
	SHOW_PANEL_MULTI_JOIN_CHOOSE,
	SHOW_CINEMATIC,
	INVESTIGATE,
	SOUND_EFFECT,
	PAUSE_MUSIC,
	RESUME_MUSIC,
	PLAY_MUSIC,
	FADE_MUSIC,
	SHOW_DEBUG,
	LOAD_CINEMATIC,
	LOAD_CHAPTER,
	BATTLE_COND,
	PUBLIC_SPEECH,
	CLIENT_BROADCAST_HERO,
	GAME_READY,
	HIDE_ATTACK_AREA,
	DISPLAY_MAP_ENTRY,
	SET_SELECTED_SPRITE,
	CIN_NEXT_ACTION,
	CIN_END,
	MENU_CLOSED,
	SHOW_SPELL_LEVEL,
	RETURN_FROM_ATTACK_CIN,
	SEARCH_IN_BATTLE
}