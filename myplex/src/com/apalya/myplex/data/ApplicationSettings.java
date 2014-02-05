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
	
	public enum APP_TYPE{
		OFFLINE, NORMAL
	};
	
	public static APP_TYPE MODE_APP_TYPE=APP_TYPE.NORMAL;

}
