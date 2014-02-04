package com.apalya.myplex.utils;

import com.apalya.myplex.data.myplexapplication;

public class ConsumerApi {
	public static String DOMAIN = "api-beta.myplex.in";
	public static final String SEARCH_ACTION = "search";
	public static final String INLINESEARCH_ACTION = "inlineSearch";
	public static final String CONTENTLIST = "contentList";
	public static final String TAG_ACTION = "tags";
	public static final String CONTENT_TAG = "content";
	public static final String FAVORITELIST_ACTION = "contentList/favorites";
	public static final String PURCHASEDLIST_ACTION = "contentList/purchased";
	public static final String FAVORITE_ACTION  = "favorite";
	public static final String DOWNLOAD_COMPLETE  = "downloaded";	
	public static final String RECOMMENDATIONS_ACTION = "recommendations";
	public static final String SIGN_OUT_ACTION = "signOut";
	public static final String CONTENTDETAILS_ACTION = "contentDetail";
	public static final String MATCHSTATUS_ACTION = "matchStatus";
	public static final String SIMILARCONTENT = "similar";
	public static final String COMMENT_TAG= "comment";
	public static final String RATING_TAG= "rating";
	public static final String SUBSCRIBE_TAG= "subscribe";
	public static final String FIELD_COMMENTS = "comments";
	public static final String FIELD_CURRENTUSERDATA_OLD = "currentUserData";
	public static final String FIELD_CURRENTUSERDATA = "user/currentdata";
	public static final String FIELD_USERREVIEWS = "reviews/user";
	public static final String FIELD_VIDEOS = "videos";
	public static final String PAYMENTCHANNEL = "paymentChannel=";
	public static final String PACKAGEID = "packageId=";
	public static final String CONTENT_CONTEXT = "content/v2";
	public static final String CONTENT_CONTEXT_V3 = "content/v3";
	public static final String USER_CONTEXT = "user/v2";
	public static final String BILLING_EVENT = "billing"; 
	public static final String CHECK_COUPON = "check/coupon";
	public static final String COUPON_CODE ="couponCode=";
	public static final String PACKAGE_ID  = "packageId=";
	public static final String SCHEME = "https://";
	public static final String HTTPSCHEME = "http://";
	public static final String SLASH = "/";
	public static final String QUESTION_MARK = "?";
	public static final String AMPERSAND = "&";
	public static final String QUERY = "query=";
	public static final String BROWSETYPE = "type=";
	public static final String CLIENTKEY = "clientKey=";
	public static final String LEVEL = "level=";
	public static final String FIELDS = "fields=";
	public static final String STARTLETTER = "startLetter=";
	public static final String QUALIFIERS = "qualifier=";
	public static final String STARTINDEX = "startIndex=";
	public static final String COUNT = "count=10";
	public static final String NUMPERQUALIFIER = "numPerQualifier=";
	public static final String NUMPERLETTERS = "numPerLetter=";
	public static final String BILLING_TAG = "billing";
	public static final String MODES_TAG = "modes";
	public static final String CONTENTID = "contenId=";
	public static final int SUBSCRIPTIONERROR = 1;
	public static final int SUBSCRIPTIONSUCCESS = 2;
	public static final int SUBSCRIPTIONREQUEST = 3;
	
	public static final String VIDEOQUALTYLOW = "Low";
	public static final String VIDEOQUALTYMEDIUM = "Medium";
	public static final String VIDEOQUALTYHIGH = "High";
	public static final String VIDEOQUALTYVERYHIGH = "VeryHigh";
	public static final String VIDEOQUALTYSD = "sd";
	public static final String VIDEOQUALTYHD = "hd";
	
	public static final String STREAMINGFORMATHTTP ="http";
	public static final String STREAMINGFORMATRTSP ="rtsp";
	public static final String STREAMINGFORMATHLS ="hls";
	
	public static final String STREAMADAPTIVE = "adaptive";
	public static final String STREAMNORMAL = "streaming";
	public static final String STREAMDOWNLOAD = "download";
	
	public static final String PAYMENT_CHANNEL_INAPP="INAPP";
	
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
	//public static String DEBUGCLIENTKEY = "dcb11454ccdafdd4706c7186d37abd2ff96cd02dc998d1111d16d4778a797f85";//"c86f79514ec7976bd20de36f1c6f15900d8e09f699818024283bad1bf0609650";
	public static String DEBUGCLIENTKEY = myplexapplication.getDevDetailsInstance().getClientKey();
	public static final String PLAYERDETAILSACTION = "player";
	public static final String UPDATE_STATUS = "updateStatus";
	public static final String ACTION = "action=";
	public static final String EVENTS = "events";
	public static final String ELAPSED_TIME = "elapsedTime=";
	public static final String DURATION = "";
	public static final String FORCE_TRUE = "&force=true";
	public static final String VIDEO_TYPE_MOVIE = "movie";
	public static final String VIDEO_TYPE_LIVE = "live";
	public static final String VIDEO_TYPE_TRAILER = "trailer";
	
	// list of content types
	
	public static final String CONTENT_SPORTS_LIVE = "sportsEvent";
	public static final String CONTENT_SPORTS_VOD = "sportsEventVod";
		
	public static String getSearch(String queryStr, String level,int startIndex, String searchType) {
		if(queryStr == null||(queryStr != null && queryStr.length() ==0)){
			queryStr = "*";
		}
		queryStr.replace(" ", "%20");
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ SEARCH_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + QUERY + queryStr +  AMPERSAND + BROWSETYPE + searchType 
				+ AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL 
				+ level + AMPERSAND+COUNT;
	}
	
	public static String getInlineSearch(String queryStr, String level){
		queryStr.replace(" ", "%20");
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ INLINESEARCH_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + QUERY + queryStr + AMPERSAND + LEVEL 
				+ level + AMPERSAND+COUNT;
	}
	
	public static String getBrowse(String type, String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ CONTENTLIST + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + BROWSETYPE + type + AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL 
				+ level + AMPERSAND+COUNT;
	}
	
	public static String getRecommendation(String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH
				+ RECOMMENDATIONS_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + BROWSETYPE + "movie" +  AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level + AMPERSAND+COUNT;
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
				+ level + AMPERSAND+COUNT;
	}
	public static String getPurchases(String level,int startIndex) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH 
				+ PURCHASEDLIST_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY
				+  AMPERSAND + STARTINDEX
				+ startIndex + AMPERSAND + LEVEL
				+ level + AMPERSAND+COUNT;
	}
	public static String getFavourite(String contentId) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + CONTENT_TAG +SLASH + contentId +SLASH
				+ FAVORITE_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY;
	}

	public static String getSearchTags(String startLetterstr,
			String level) {
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + TAG_ACTION
				+ SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY +  AMPERSAND + STARTLETTER + startLetterstr
				+ AMPERSAND + LEVEL + level ;
	}
	public static String getContentDetail(String contentID,String level){
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT_V3 + SLASH + CONTENTDETAILS_ACTION
				+ SLASH + contentID + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY  + AMPERSAND
		        + LEVEL + level + AMPERSAND+COUNT;
	}
	public static String getVideosDetail(String contentID){
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT_V3 +  SLASH + CONTENTDETAILS_ACTION
				+ SLASH + contentID + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY  + AMPERSAND
		        + FIELDS + FIELD_VIDEOS;
	}
	public static String getBillingMode(String contentID) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_TAG
				+ SLASH + MODES_TAG + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND+ CONTENTID +contentID;
	}
	public static String getSusbcriptionRequest(String paymentChannel,String packageId){
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_TAG
				+ SLASH + SUBSCRIBE_TAG+ SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND+ PAYMENTCHANNEL +paymentChannel+ AMPERSAND +PACKAGEID+packageId;
	}
	public static String getSusbcriptionRequest(String paymentChannel,String packageId,String couponCode){
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_TAG
				+ SLASH + SUBSCRIBE_TAG+ SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND+ PAYMENTCHANNEL +paymentChannel+
				AMPERSAND +PACKAGEID+packageId + AMPERSAND + COUPON_CODE + couponCode;
	}
	
	public static String getDownloadNotifyUrl(String contentId) {
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + CONTENT_TAG +SLASH + contentId +SLASH
				+ DOWNLOAD_COMPLETE + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY;
	}
	
	public static String getDrmProxy()
	{
		return HTTPSCHEME+DOMAIN+SLASH+"licenseproxy/v2/license";
		//return "https://api-beta.myplex.in/licenseproxy/v2/license"
	}
	
	public static String getSimilarContent(String contentID, String level)
	{
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + SIMILARCONTENT
				+ SLASH + contentID + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY  + AMPERSAND
		        + LEVEL + level + AMPERSAND+COUNT;
	}
	
	public static String getUnregisterDevice()
	{
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + "unregisterDevice"
				+ SLASH ;
	}
	public static String getPlayerEventDetails(String contentID,String action){
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + EVENTS +  SLASH + PLAYERDETAILSACTION
				+ SLASH + contentID + SLASH + UPDATE_STATUS +	QUESTION_MARK	+	ACTION + action +
				AMPERSAND + CLIENTKEY + DEBUGCLIENTKEY;	
	}
	public static String setPlayerEventDetails(String contentID,String action,int elapsedTime){
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + EVENTS +  SLASH + PLAYERDETAILSACTION
				+ SLASH + contentID + SLASH + UPDATE_STATUS +	QUESTION_MARK	+	ACTION + action +
				AMPERSAND + CLIENTKEY + DEBUGCLIENTKEY;
					
	}
	public static String checkCouponCode(String couponCode, String packages[]){
		String packageString = packages[0]; 
		for(int i=1;i<packages.length;i++){
			packageString += ","+packages[i];
		}
		return SCHEME + DOMAIN + SLASH + USER_CONTEXT + SLASH + BILLING_EVENT + SLASH +
			 CHECK_COUPON +  QUESTION_MARK +CLIENTKEY + DEBUGCLIENTKEY + AMPERSAND + COUPON_CODE  + couponCode + AMPERSAND +
			 PACKAGE_ID  + packageString;
		
	}
	
	public static String getMatchStatus(String contentID){
		return SCHEME + DOMAIN + SLASH + CONTENT_CONTEXT + SLASH + CONTENT_TAG +SLASH + contentID
				+ SLASH +  MATCHSTATUS_ACTION + SLASH + QUESTION_MARK + CLIENTKEY + DEBUGCLIENTKEY ;
	}
}
