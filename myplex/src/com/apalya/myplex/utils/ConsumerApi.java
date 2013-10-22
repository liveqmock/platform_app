package com.apalya.myplex.utils;

public class ConsumerApi {
	public static String DOMAIN = "alpha.myplex.in:8866";
	public static final String SEARCH_ACTION = "search";
	public static final String TAG_ACTION = "tags";
	public static final String CONTENT_TAG = "content";
	public static final String FAVORITELIST_ACTION = "contentList/favorites";
	public static final String PURCHASEDLIST_ACTION = "contentList/purchased";
	public static final String FAVORITE_ACTION  = "favorite";
	public static final String RECOMMENDATIONS_ACTION = "recommendations";
	public static final String SIGN_OUT_ACTION = "signOut";
	public static final String CONTENTDETAILS_ACTION = "contentDetail";
	public static final String COMMENT_TAG= "comment";
	public static final String RATING_TAG= "rating";
	public static final String SUBSCRIBE_TAG= "subscribe";
	public static final String FIELD_COMMENTS = "comments";
	public static final String FIELD_USERREVIEWS = "userReviews";
	public static final String FIELD_VIDEOS = "videos";
	public static final String PAYMENTCHANNEL = "paymentChannel=";
	public static final String PACKAGEID = "packageId=";
	public static final String CONTENT_CONTEXT = "content/v2";
	public static final String USER_CONTEXT = "user/v2";
	public static final String SCHEME = "http://";
	public static final String SLASH = "/";
	public static final String QUESTION_MARK = "?";
	public static final String AMPERSAND = "&";
	public static final String QUERY = "query=";
	public static final String CLIENTKEY = "clientKey=";
	public static final String LEVEL = "level=";
	public static final String FIELDS = "fields=";
	public static final String STARTLETTER = "startLetter=";
	public static final String QUALIFIERS = "qualifier=";
	public static final String STARTINDEX = "startIndex=";
	public static final String NUMPERQUALIFIER = "numPerQualifier=";
	public static final String NUMPERLETTERS = "numPerLetter=";
	public static final String BILLING_TAG = "billing";
	public static final String MODES_TAG = "modes";
	public static final String CONTENTID = "contenId=";
	public static final int SUBSCRIPTIONERROR = 1;
	public static final int SUBSCRIPTIONSUCCESS = 2;
	public static final int SUBSCRIPTIONREQUEST = 3;
	
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
	public static String DEBUGCLIENTKEY = "dcb11454ccdafdd4706c7186d37abd2ff96cd02dc998d1111d16d4778a797f85";//"c86f79514ec7976bd20de36f1c6f15900d8e09f699818024283bad1bf0609650";
	
	public static String getSearch(String queryStr, String level,int startIndex) {
		if(queryStr == null||(queryStr != null && queryStr.length() ==0)){
			queryStr = "*";
		}
		queryStr.replace(" ", "%20");
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ SEARCH_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + QUERY + queryStr + AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level;
	}
	public static String getRecommendation(String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ RECOMMENDATIONS_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level;
	}

	public static String getCommentPostUrl(String contentID) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + CONTENT_TAG + SLASH + contentID +SLASH+COMMENT_TAG+SLASH
				+ QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY;
	}
	public static String getRatingPostUrl(String contentID) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + CONTENT_TAG + SLASH + contentID +SLASH+RATING_TAG+SLASH
				+ QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY;
	}
	public static String getFavourites(String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH  
				+ FAVORITELIST_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level;
	}
	public static String getPurchases(String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH 
				+ PURCHASEDLIST_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level;
	}
	public static String getFavourite(String contentId) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + CONTENT_TAG +SLASH + contentId +SLASH
				+ FAVORITE_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY;
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
	public static String getVideosDetail(String contentID){
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + CONTENTDETAILS_ACTION
				+ SLASH + contentID + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY  + AMPERSAND
		        + FIELDS + FIELD_VIDEOS;
	}
	public static String getBillingMode(String contentID) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_TAG
				+ SLASH + MODES_TAG + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND+ CONTENTID +contentID;
	}
	public static String getSusbcriptionRequesr(String paymentChannel,String packageId){
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_TAG
				+ SLASH + SUBSCRIBE_TAG+ SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND+ PAYMENTCHANNEL +paymentChannel+ AMPERSAND +PACKAGEID+packageId;
	}
}
