package com.apalya.myplex.data;

public class ApplicationSettings {

	public boolean downloadOnlyOnWifi = true;
	public boolean showPlayerLogs = false;
	public static final boolean ENABLE_LOG_DRM_ERRORS=true;
	public static final boolean ENABLE_MIXPANEL_API = true;
	// Enable below flag to display player log option in settings menu.
	public static boolean ENABLE_SHOW_PLAYER_LOGS_SETTINGS = false;
	public static final boolean ENABLE_SERIALIZE_LAST_SEESION=false; 
	public static final boolean ENABLE_FB_SHARE_FREE_MOVIE = false;
	public static boolean ENABLE_SENSOR_SCROLL = false;
	
	public enum APP_TYPE{
		OFFLINE, NORMAL,FIFA
	};
	
	public static APP_TYPE MODE_APP_TYPE=APP_TYPE.NORMAL;

	/**
	 * @param true - volley deliver the expired cached response first, but it will
	 *        also send the request to the network for refreshing.
	 * @param false - volley doesn't use expired cached response and sends the network
	 *        request.
	 */
	public static final boolean ENABLE_USE_EXPIRED_RESPONSE = true;
	
	/**
	 * @param true -  Enables the random default menu selection from live tv, movies and myplex picks.
	 * @param false - no random selection, live tv as default.
	 */
	public static boolean ENABLE_DEFAULT_RANDOM_MENUSELECTION = true;
	
	/**
	 * @param true -  enable reminder alert after live tv play
	 * @param false - disable reminder alert after live tv play
	 */
	public static boolean ENABLE_AUTO_REMINDER_FOR_LIVETV = true;
}
