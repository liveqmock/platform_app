package com.apalya.myplex.utils;

public class ConsumerApi {
	public static String DOMAIN = "alpha.myplex.in:8866";
	public static final String SEARCH_ACTION = "search";
	public static final String TAG_ACTION = "tags";
	public static final String CONTENTDETAILS_ACTION = "contentDetail";
	public static final String CONTENT_CONTEXT = "content/v2";
	public static final String USER_CONTEXT = "user/v2";
	public static final String SCHEME = "http://";
	public static final String SLASH = "/";
	public static final String QUESTION_MARK = "?";
	public static final String AMPERSAND = "&";
	public static final String QUERY = "query=";
	public static final String CLIENTKEY = "clientKey=";
	public static final String LEVEL = "level=";
	public static final String STARTLETTER = "startLetter=";
	public static final String STARTINDEX = "startIndex=";
	
	// min: returns only content ids.
	// static: returns static sub-entities.
	// dynamic: returns dynamic sub-entities.
	// static+dynamic: returns static and dynamic sub-entities.
	// devicemin: minimized data for devices
	// devicemax: maximized data for devices
	public static final String LEVELMIN = "min";
	public static final String LEVELSTATIC = "static";
	public static final String LEVELDYNAMIC = "dynamic";
	public static final String LEVELSTATIC_DYNAMIC = "static_dynamic";
	public static final String LEVELDEVICEMIN = "devicemin";
	public static final String LEVELDEVICEMAX = "devicemax";
	
	public static final String DEBUGCLIENTKEY = "c86f79514ec7976bd20de36f1c6f15900d8e09f699818024283bad1bf0609650";

	public static String getSearch(String queryStr, String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ SEARCH_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + QUERY + queryStr + AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level;
	}

	public static String getSearchTags(String startLetterstr,
			String level) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + TAG_ACTION
				+ SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY +  AMPERSAND + STARTLETTER + startLetterstr
				+ AMPERSAND + LEVEL + level;
	}
	public static String getContentDetail(String contentID,String level){
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + CONTENTDETAILS_ACTION
				+ SLASH + contentID + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY  + AMPERSAND
		        + LEVEL + level;
	}
}
