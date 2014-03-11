package com.apalya.myplex.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.apalya.myplex.R;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	public static String SEARCH_TYPE = null; //useful in card explorer to differentiate discover and search
	public static String SELECTED_INLINE_WORD = null; //useful in card explorer to identify selected from inline search items
	public static boolean isTrailer = false;
	public static String trackingId = null;
	public static double priceTobecharged = 0.0;
	public static double couponDiscountINR = 0.0;
	public static CardData cardData = null; //added for analytics
	//public static long downloadStartTime;
	public static String downLoadStartTime;
		
	private static String TAG = "Analytics";
	public static String UNDERSCORE = "_";
	
	public static enum SCREEN_NAMES {CardExplorer,Settings,CardDetails,CardDetailsTabletFrag,SearchSuggestions};
	
	public static long currentTime = 0;
	public static long resumedAt = 0;
	public static long pausedAt = 0;
	public static long playedTime = 0;
	public static long totalPlayedTime = 0;
	public static int NUMBER_FAVOURITE = 1;
	public static enum LOGIN_OPTIONS {facebook,twitter,google,myplex};
	
	/*
	 * new mixpanel events 
	 */
	public static String EVENT_DEVICE_REGISTRATION_SUCCESS = "device registration success";
	public static String EVENT_DEVICE_REGISTRATION_FAILED = "device registration failed";
	public static String EVENT_DEVICE_DEREGISTRATION_INITIATED = "device deregistration initiated";
	public static String EVENT_DEVICE_DEREGISTRATION_SUCCEEDED = "device deregistration success";
	public static String EVENT_DEVICE_DEREGISTRATION_FAILURE = "device deregistration failure";
	public static String EVENT_JOIN_MYPLEX_INITIATED = "join myplex initiated";
	public static String EVENT_SIGN_UP_OPTIONS_PRESENTED = "sign up options presented";
	public static String EVENT_MYPLEX_SIGNUP_OPTION = "myplex sign up option"; 
	//public static String EVENT_JOINED_MYPLEX_SUCCESSFULLY = "joined myplex successfully";
	public static String EVENT_JOINED_MYPLEX_SUCCESSFULLY = "myplex sign up success";
	//public static String EVENT_JOINED_MYPLEX_SUCCESSFULLY = "sign up success";
	public static String EVENT_JOINED_MYPLEX_FAILURE = "failed to create myplex account";
	//public static String EVENT_JOINED_MYPLEX_FAILURE = "sign up failure";
	public static String EVENT_LOGIN_OPTIONS_PRESENTED = "login options presented";
	public static String EVENT_GOOGLE_LOGIN_SELECTED = "google login selected";
	public static String EVENT_MYPLEX_LOGIN_SELECTED = "myplex login selected";
	public static String EVENT_FACEBOOK_LOGIN_SELECTED = "facebook login selected";
	public static String EVENT_TWITTER_LOGIN_SELECTED = "twitter login selected";
	public static String EVENT_BROWSING_AS_GUEST = "browsing as guest";
	public static String EVENT_BROWSED_MOVIES = "browsed movies";
	public static String EVENT_BROWSED_RECOMMENDATIONS = "browsed recommendations";
	public static String EVENT_BROWSED_PURCHASES = "browsed purchases";
	public static String EVENT_BROWSED_TV_CHANNELS = "browsed live tv channels";
	public static String EVENT_BROWSED_TV_SHOWS = "browsed tv shows";
	public static String EVENT_BROWSED_LIVE_TV_GA = "browsed live tv";
	public static String EVENT_BROWSED_SETTINGS = "browsed settings";
	//public static String EVENT_SELECTED_FEEDBACK = "selected feedback option";
	public static String EVENT_INITIATING_FEEDBACK = "initiating feedback";
	public static String EVENT_PROVIDED_FEEDBACK = "provided feedback";
	public static String EVENT_OPENED_NAVIGATION_MENU = "opened navigation menu";
	public static String EVENT_ENTERED_COMMENTS = "entered comments";
	public static String EVENT_ENTERED_REVIEW = "entered review";
	public static String EVENT_COMMENT = "comment";
	public static String EVENT_SHARED_MYPLEX_EXPERIENCE = "shared myplex experience";
	public static String EVENT_EXPANDED_CAST_CREW = "expanded cast and crew details";
	public static String EVENT_EXPLORED_CAST_CREW_POPUP = "explored cast and crew pop up";
	public static String EVENT_EXPLORING_SIMILAR_CONTENT = "explored similar items";
	public static String EVENT_SELECTED_WIFI_ONLY = "selected wifi only";
	public static String EVENT_LOGGED_OUT = "logged out";
	public static String EVENT_ADDED = "added";
	public static String EVENT_REMOVED = "removed";
	public static String EVENT_DELETED = "deleted";
	public static String EVENT_SUBSCRIBED_FREE = "subscribed free";
	public static String EVENT_SUBSCRIPTION_FAILURE = "subscription/payment failed";
	public static String EVENT_UNABLE_TO_PLAY = "unable to play";
	public static String EVENT_DOWNLOAD_MOVIE = "downloaded movie";
	public static String EVENT_INVITE_FRIENDS = "invite friends";
	public static String EVENT_BROWSE = "browsed";
	public static String EVENT_SELECTED = "selected";
	public static String EVENT_SELECTED_DETAILS_CARD = "selected details card";
	public static String EVENT_ADDED_TO_FAVORITES = "added to favorites";
	public static String EVENT_REMOVED_FROM_FAVORITES = "removed from favorites";
	public static String EVENT_DELETED_FROM_CARDS = "deleted from cards";
	public static String EVENT_BROWSED_DOWNLOADS = "browsed downloads";
	public static String EVENT_BROWSED_FAVORITES = "browsed favorites";
	public static String EVENT_BROWSED_LIVE_TV = "browsed live tv channels";
	public static String EVENT_PLAYED_DOWNLOADED_MOVIE = "played downloaded movie";
	public static String EVENT_STREAMED_MOVIE = "streamed movie";
	
		
	public static String EVENT_GOOGLE_LOGIN_SUCCESS = "google login success";
	public static String EVENT_MYPLEX_LOGIN_SUCCESS = "myplex login success";
	public static String EVENT_FACEBOOK_LOGIN_SUCCESS = "facebook login success";
	public static String EVENT_TWITTER_LOGIN_SUCCESS = "twitter login success";
	
	public static String EVENT_GOOGLE_LOGIN_FAILURE = "google login failure";
	public static String EVENT_MYPLEX_LOGIN_FAILURE = "myplex login failure";
	public static String EVENT_FACEBOOK_LOGIN_FAILURE = "facebook login failure";
	public static String EVENT_TWITTER_LOGIN_FAILURE = "twitter login failure";
	
	public static String EVENT_FORGOT_PASSWORD_INITIATED = "forgot password initiated";
	public static String EVENT_FORGOT_PASSWORD_SUCCEEDED = "forgot password success";
	public static String EVENT_FORGOT_PASSWORD_FAILED = "forgot password failure";
	public static String FORGOT_PASSWORD_FAILED_ON = "forgot password failed on";
	
		
	public static String EVENT_FILTERED_BY_CATEGORY = "filtered by category";
	public static String EVENT_FILTERED = "filtered";
	public static String FILTER_NAME = "filter name";
	public static String NUMBER_OF_RESULTS = "number of results";
	
	public static String TYPE_OF_CONTENT = "type of content";
	public static String EVENT_DISCOVERY_OPTION = "discovery option selected";
	public static String DISCOVER_KEYWORD = "discover keyword";
	public static String EVENT_CONTENT_DISCOVERY_INITIATED = "content discovery initiated";
	public static String EVENT_CONTENT_DISCOVERY_RESULTS = "content discovery results";
	public static String EVENT_SELECTED_A_KEYWORD_DISCOVERY = "selected a keyword";
	public static String SEARCHED_MYPLEX = "searched myplex";
	public static String EVENT_SELECTED_IN_DROPDOWN_RESULTS = "in the drop down results";
	public static String EVENT_SELECTED_DROPDOWN_RESULT = "selected drop down result";
	public static String EVENT_INLINE_SEARCH_INITIATED = "inline search initiated";
	public static enum SEARCH_TYPES {DropDown,Discover,Filter,Inline};
	public static String ALL_LOGIN_OPTIONS = "all login options";
	public static String ALL_SIGN_UP_OPTIONS = "all sign up options";
	
	public static String EVENT_BROWSED = "browsed";
	public static String NUMBER_OF_PURCHASES = "number of purchases";
	public static String NUMBER_OF_FAVORITES = "number of favorites";
	public static String NUMBER_OF_DOWNLOADS = "number of downloads";
	public static String NUMBER_OF_MOVIE_CARDS = "number of movie cards";
	public static String NUMBER_OF_CARDS = "number of cards";
	public static String NUMBER_OF_LIVETV_CARDS = "number of live tv cards";
	public static String NUMBER_OF_LIVETV_SHOW_CARDS = "number of live tv show cards";
	public static String NUMBER_OF_KEYWORDS = "number of keywords";
	public static String NUMBER_OF_INVITEES = "number of invitees";
	public static String INVITED_FRIENDS = "invited friends";
		
	public static String ACCOUNT_TYPE = "account type";
	public static String USER_ID = "user id";
	public static String ACCOUNT_TYPE_MYPLEX = "myplex";
	public static String DEVICE_ID = "device id";
	public static String DEVICE_DESC = "device description";
	public static String REASON_FAILURE = "reason for failure";
	public static enum ALL_LOGIN_TYPES {facebook,twitter,google,myplex,Guest};
	public static String SOCIAL_NETWORK = "social network";
	public static String JOINED_ON = "joined on";
	public static String LAST_LOGGED_IN_DATE = "last logged in date";
	public static String LAST_LOGGED_IN_FAILURE_DATE = "last log in failure date";
	public static String GOOGLE_LOG_IN_FAILURE_DATE = "google log in failure date";
	public static String EMPTY_SPACE = " ";
	
	public static String CONTENT_ID_PROPERTY = "content id";
	public static String CONTENT_NAME_PROPERTY = "content name";
	public static String TV_CHANNEL_NAME = "tv channel name";
	public static String TV_SHOW_NAME = "tv show name";
	public static String CONTENT_TYPE_PROPERTY = "content type";
	public static String CONTENT_PRICE = "content price";
	public static String TIME_PLAYED_PROPERTY = "time played (in seconds)";
	public static String FEEDBACK_TEXT = "feedback text";
	public static String FEEDBACK_RATING = "rating";
	public static String TIME_TAKEN_TO_DOWNLOAD = "time taken to download (in minutes)";
	public static String TRAILER_DATA_RATE = "data rate";
	
	public static String EVENT_PLAY = "played";
	public static String EVENT_PLAYED_TRAILER = "played trailer";
	public static String EVENT_PLAYED_TV_CHANNEL = "played tv channel";
	public static String EVENT_PLAYED_TV_SHOW = "played tv show";
	public static String TRAILER = "trailer";
	//public static String MOVIE = "movie";
	public static String MOVIES = "movies";
	public static String KEYWORD = "keyword";
	public static String TV_CHANNEL = "TV channel";
	public static String TV_CHANNELS = "live TV channels";
	public static String TO_FAVORITES = "to favorites";
	public static String FROM_FAVORITES = "from favorites";
	public static String FROM_CARDS = "from cards";
	public static String STATUS = "status";
	public static String APOS = "'";
	public static String LOGIN_AS_GUEST = "Guest";
	
	public static String EVENT_PAYMENT_OPTIONS_PRESENTED = "payment options presented";
	public static String EVENT_PAYMENT_OPTION_SELECTED = "payment option selected";
	public static String EVENT_PAID_FOR = "paid for";
	public static String EVENT_PAID_FOR_CONTENT = "paid for content";
	public static String EVENT_COUPON_ENTERED = "coupon entered";
	public static String PAY_COUPON_CODE = "coupon code";
	public static String PAY_COUPON_VALUE = "coupon value";
	public static String COUPON_USED = "coupon used";
	public static String FALSE = "FALSE";
	public static String COUPON_DISCOUNT = "coupon discount";
	public static String PAY_PURCHASE_TYPE = "purchase type";
	public static String MOVIE_SIZE = "movie size (MB)";
	public static String PAY_CONTENT_PRICE = "content price";
	public static String PAYMENT_METHOD =  "payment method";
	public static String CONTENT_QUALITY = "content quality";
	public static String PAYMENT_OPTION =  "payment option";
	public static String PAYMENT_OPTIONS_PRESENTED =  "payment options presented";
	public static String PURCHASE_OPTIONS_AVAILABLE =  "purchase options available";
	public static String PRICING_OPTIONS_AVAILABLE =  "pricing options";
	public static String CONTENT_QUALITY_AVAILABLE =  "content quality available";
	public static String STUDIO =  "studio";
	public static String LANGUAGE =  "language";
	public static String ANALYTICS =  "analytics";
	public static String TOP_RESULT =  "top result";
	public static String INITIATED_FROM =  "initiated from";
	public static String DOWNLOAD_OPTION_AVAILABLE =  "download option available";
	public static String USERID_NOT_AVAILABLE =  "unavaliable";
	public static String UNAVAILABLE =  "unavaliable";
	
	public static String SCREEN_OPENED_FROM = "opened from";
	public static String DETAILS = "details";
	public static String COMMENT_TEXT = "comment text";
	public static String REVIEW_TEXT = "review text";
	public static String RATING = "rating";
	public static String WIDEVINE_AUTH_FAILED = "Widevine authorization failed";
	public static String ACQUIRE_RIGHTS_FAILED = "acquire rights failed";
	public static String NO_URL_TO_PLAY = "no url to play";
	public static String FAILED_TO_FETCH_URL = "Failed in fetching the url";
	public static String WIFI_OPTION_1 = "wifi only";
	public static String WIFI_OPTION_2 = "not limited to wifi";
	public static String WIFI_ONLY_STATUS = "wifi only status";
	public static String NETWORK_ERROR = "network error";
	public static String USER_ABANDONMENT_LOGIN_FAILURE = "User abandonment due to failure login";
	
	
	//people data
	public static String PEOPLE_TRAILERS_PLAYED = "trailers played for (m)";
	public static String PEOPLE_TV_STREAMED = "tv streamed for (m)";
	public static String PEOPLE_MOVIES_STREAMED_FOR = "movies streamed for (m)";
	public static String PEOPLE_MOVIES_PLAYED_LOCALLY_FOR = "movies played locally for (m)";
	public static String PEOPLE_DOWNLOADED_MOVIES = "downloaded movies";
	public static String PEOPLE_MOVIES_PURCHASED_FOR = "movies purchased for";
	public static String PEOPLE_LIVETV_PURCHASED_FOR = "live tv purchased for";
	public static String PEOPLE_LIVETV_SHOW_PURCHASED_FOR = "live tv show purchased for";
	public static String PEOPLE_TOTAL_PURCHASES = "total purchases";
	public static String PEOPLE_ENTERED_REVIEWS = "entered reviews";
	public static String PEOPLE_SHARED_ABOUT_MYPLEX = "shared about myplex";
	public static String PEOPLE_FREE_MOVIE_RENTALS = "free movie rentals";
	public static String PEOPLE_FREE_TV_SUBSCRIPTIONS = "free tv subscriptions (w)";
	public static String PEOPLE_FREE_TV_SHOW_SUBSCRIPTIONS = "free tv show subscriptions (w)";
	public static String PEOPLE_FREE_DOWNLOADS_TO_OWN = "free downloads to own";
	public static String PEOPLE_JOINING_DATE = "joining date";
	public static String PEOPLE_FAVORITES = "favorites";
	public static String PEOPLE_DEFAULT_NAME = "Guest";
	
	
	//Google Analytics
	public static String GA_AFFILIATION = "GoogleStore";
	public static String GA_CURRENCY = "INR";
	
	public static String CATEGORY_BROWSE = "browse";
	public static String CATEGORY_SEARCH = "search";
	public static String CATEGORY_SUBMIT = "submit";
	public static String CATEGORY_NETWORK = "network";
	public static String CATEGORY_MOVIE = "movie";
	public static enum  CATEGORY_SOCIAL_NETWORK_TYPES {facebook,twitter,google};
	public static String CATEGORY_PLAYED_MOVIE = "played movie";
	public static String CATEGORY_PLAYED_TRAILER = "played trailer";
	public static String CATEGORY_PLAYED_LIVETV = "played live tv";
	public static String CATEGORY_PLAYED_TV_EPISODE = "played tv episode";
	public static String GA_INLINE_SEARCH = "inline search";
	public static String CATEGORY_DISCOVERY = "discovery";
	public static String ACTION_FILTER = "filter";
	public static String ACTION_COMMENTS = "comments";
	public static String ACTION_REVIEWS = "reviews";
	public static String ACTION_RATING = "rating";
	public static String ACTION_LOGIN = "login";
	public static String ACTION_INVITE_FRIENDS = "invite friends";
	public static String ACTION_SHARE = "share";
	public static enum  ACTION_TYPES {play,pause,stop};
	//public static enum  CATEGORY_TYPES {live tv,movie,};
	
	//screen names
	public static String SCREEN_LOGINACTIVITY = "login activity";
	public static String SCREEN_CARDDETAILS = "card details";
	public static String SCREEN_SETTINGS = "settings";
	public static String SCREEN_CARD_EXPLORER = "card explorer";
	public static String SCREEN_SUBSCRIPTION_VIEW = "subscription view";
	public static String SCREEN_DISCOVER = "discover";
	public static String SCREEN_SIGNUP = "sign up";
	public static String SCREEN_SEARCH_SUGGESTIONS = "search suggestions";
	
	public static String TRAILER_BITRATE = "trailer_bitrate_";
	
	public static String CONSTANT_RECOMMENDATIONS = "recommendations";
	public static String CONSTANT_MOVIE = "movie";
	public static String CONSTANT_MOVIES = "movies";
	public static String CONSTANT_DOWNLOADED = "downloaded";
	public static String CONSTANT_LIVETV = "live tv";
	public static String CONSTANT_LIVE = "live";
	public static String CONSTANT_TV_SERIES = "tvseries";
	public static String CONSTANT_TV_SHOW = "tv shows";
	public static String CONSTANT_TV_EPISODE = "tvepisode";
	
	
	private static MixpanelAPI mMixPanel = myplexapplication.getMixPanel();
	private static EasyTracker easyTracker = myplexapplication.getGaTracker();

	public static void trackEvent(String aEventName,Map<String, String> params){
		
		if(ApplicationSettings.ENABLE_MIXPANEL_API) {
			mMixPanel.track(aEventName,getJSON(params));
		}
	}
	
	public static MixpanelAPI.People getMixpanelPeople() {
		MixpanelAPI.People people = mMixPanel.getPeople();
//		people.identify(getUserEmail());
		return people;
	}
	
	public static MixpanelAPI getMixpanelAPI() {
		 return mMixPanel;
	}
	public static void trackCharge(double price){
		
		if(ApplicationSettings.ENABLE_MIXPANEL_API) {
			MixpanelAPI.People people = mMixPanel.getPeople();
			people.identify(trackingId);
			people.trackCharge(price, null);
		}
	}

	private static JSONObject getJSON(Map<String, String> params){
		JSONObject data = new JSONObject();
		Set<String> keySet = params.keySet();
		
		for(String key:keySet){
//			mData.mEntries.add(object.get(key));
			
				try {
					if(params.get(key) != null){
					data.put(key,params.get(key));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
		return data;
	}

	public static void  createTransactionGA(String transactionid,String affiliation,Double revenue, Double tax, Double shippingCost) {
		easyTracker.send(MapBuilder
			      .createTransaction(transactionid,       // (String) Transaction ID
			                         Analytics.GA_AFFILIATION,   // (String) Affiliation
			                         revenue,            // (Double) Order revenue
			                         tax,            // (Double) Tax
			                         shippingCost,             // (Double) Shipping
			                         Analytics.GA_CURRENCY)            // (String) Currency code
			      .build()
			  );
	}
	
	public static void  createItemGA(String transactionid,String productName,String productSKU,String productCategory,
						Double productPrice,Long quantity) {
		easyTracker.send(MapBuilder
			      .createItem(transactionid,               // (String) Transaction ID
			    		  productName,      // (String) Product name
			    		  productSKU,    // content id (String) Product SKU
			    		  productCategory,        // (String) Product category
			    		  productPrice,                    // (Double) Product price
			    		  quantity,                       // (Long) Product quantity
			              Analytics.GA_CURRENCY)                    // (String) Currency code
			      .build()
			  );
	}
	
	public static void  createEventGA(String category,String action,String label,Long value) {
		easyTracker.send(
			     MapBuilder.createEvent(category, action, label, value).build());
	}
	
	public static void  createUserTimingGA(String category,Long time,String name,String  label) {
		
		easyTracker.send(MapBuilder.createTiming(category, time, name, label).build());
	}
	
	public static void  createSocialGA(String network,String action,String label) {
		easyTracker.send(MapBuilder
			    .createSocial(network,    // Social network (required)
			                  action, // Social action (required)
			                  label)   // Social target
			    .build()
			);
	}
	
	public static void  createExceptionGA(String exceptionDesc,boolean bool) {
		easyTracker.send(MapBuilder
			      .createException( exceptionDesc,  bool) .build());   // False indicates a fatal exception              
	}
	
	public static void  createScreenGA(String screenName) {
		easyTracker.set(Fields.SCREEN_NAME, screenName);
		easyTracker.send(MapBuilder.createAppView()
				  .build()
				);
	}
	
	public static void startActivity(EasyTracker easyTracker,Activity activity) {
		easyTracker.activityStart(activity);
	}
	
	public static void startActivity(Activity activity) {
		easyTracker.activityStart(activity);
	}
	
	public static String movieOrLivetv(String contentType) {
		String ctype = null;
		if("SD".equalsIgnoreCase(contentType) || "HD".equalsIgnoreCase(contentType) || "movie".equalsIgnoreCase(contentType) ) {
			ctype = "movies";
		}
		else if("Monthly".equalsIgnoreCase(contentType) || "Weekly".equalsIgnoreCase(contentType) || 
				"Yearly".equalsIgnoreCase(contentType) || "live".equalsIgnoreCase(contentType)) {
			ctype = "live tv";
		}
		else if("tvepisode".equalsIgnoreCase(contentType) || "tvseries".equalsIgnoreCase(contentType) || "tvseason".equalsIgnoreCase(contentType)) {
			ctype = "tv shows";
		}
		else {
			ctype = null;
		}
		return ctype;
	}
	
	public static String movieOrLivetv2(String contentType) {
		String ctype = null;
		if("movie".equalsIgnoreCase(contentType) ) {
			ctype = "movies";
		}
		else if("live".equalsIgnoreCase(contentType)) {
			ctype = "live tv";
		}
		else {
			ctype = null;
		}
		return ctype;
	}
	
	public static String getRequestType(int rType) {
		String ctype = "cards";
		switch (rType) {
		case 3:
			ctype= "favorites";
			break;
		case 5:
			ctype = "downloads";
			break;
		case 6:
			ctype= "purchases";
			break;
		case 1:
			ctype= "recommendations";
			break;
		case 2:
			ctype= "discover";
			break;
		case 7:
			ctype= "live tv";
			break;		
		default:
			ctype= null;
			break;
		}
		
		return ctype;
	}
	
	public static boolean isPlaying = false;
	public static boolean playerClosed = false;
	
	//// from begin 2 code flows can happen 1) pause  2) stop
	public static void startVideoTime() {
		totalPlayedTime = 0;
		playedTime = 0;
		pausedAt = 0;
		currentTime = System.currentTimeMillis(); //10.00
		isPlaying = true;
		Log.d(TAG, "checktime startVideoTime  "+ new Date(currentTime));
	}
	
	// from pause 2 code flows can happen 1) resume  2) stop
	public static void pausedAt() {
		pausedAt = System.currentTimeMillis(); //10.10 //10.35 //stop at 10.45
		playedTime = TimeUnit.MILLISECONDS.toSeconds(pausedAt) - TimeUnit.MILLISECONDS.toSeconds(currentTime); 
		isPlaying = false;
		Log.d(TAG, "checktime pausedAt  "+ new Date(pausedAt));
		Log.d(TAG, "checktime currentTime  "+ currentTime + "  paused time "+ pausedAt);
		Log.d(TAG, "checktime pausedAt  playedTime in seconds "+ playedTime);
		totalPlayedTime();
	}
	
	// from resumed 2 code flows can happen 1) pause  2) stop
	public static void resumedAt() {
		currentTime = System.currentTimeMillis(); //10.20
		isPlaying = true;
		Log.d(TAG, "checktime resumedAt  currentTime "+ new Date(currentTime));
	}
	
	public static void stoppedAt() {
		playerClosed = true;
		if(isPlaying) {
			pausedAt();
			getTotalPlayedTime();
		}
		else {
			//left intentionally to be filled
			getTotalPlayedTime();
		}
	}
	
	//invoked only when paused or stopped
	public static void totalPlayedTime() {
		//playedTime = pausedAt - currentTime; //.20
		totalPlayedTime = totalPlayedTime + playedTime;
		playedTime = 0;
		isPlaying = false; 		
		Log.d(TAG, "checktime totalPlayedTime() totalPlayedTime in seconds "+totalPlayedTime);
		Log.d(TAG, "checktime totalPlayedTime() totalPlayedTime in minutes "+TimeUnit.SECONDS.toMinutes(totalPlayedTime));
		
	}
	//returns in seconds
	public static long getTotalPlayedTime() {
		Log.d(TAG, "checktime Player closed total time is in minutes"+TimeUnit.SECONDS.toMinutes(totalPlayedTime));
		return totalPlayedTime+1;
	}
	
	public static long getTotalPlayedTimeInMinutes() {
		Log.d(TAG, "checktime Player closed total time is in minutes"+TimeUnit.SECONDS.toMinutes(totalPlayedTime));
		return TimeUnit.SECONDS.toMinutes(totalPlayedTime)+1;
	}
	
	public static void mixPanelVideoTimeCalculation(CardData mCardData) {
		if(mCardData == null ) return;
		if(mCardData.generalInfo == null ) return;
		CardData mData = mCardData; 
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.USER_ID, getUserEmail()); //1
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id); //2
		params.put(Analytics.TIME_PLAYED_PROPERTY, ""+Analytics.getTotalPlayedTime()); //3
		String event = null;
		long ptime = Analytics.getTotalPlayedTime();
		long ptimeMinutes = Analytics.getTotalPlayedTimeInMinutes();
		if(Analytics.CONSTANT_LIVETV.equalsIgnoreCase(ctype))  { 
			params.put(Analytics.TV_CHANNEL_NAME, mData.generalInfo.title); //3
			String bitrate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), Analytics.TRAILER_BITRATE+mCardData.generalInfo._id);
			params.put(Analytics.TRAILER_DATA_RATE, bitrate);
			event =  Analytics.EVENT_PLAYED_TV_CHANNEL;
			Analytics.trackEvent(event,params);
			mMixPanel.getPeople().increment(Analytics.PEOPLE_TV_STREAMED,ptimeMinutes);
			mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
			Analytics.gaPlayedLiveTvTimings(ptime, mData.generalInfo.title); 
			Analytics.createEventGA("live tv", "play", mData.generalInfo.title, ptimeMinutes);//ga
			return;
		}
		else if(Analytics.CONSTANT_TV_SHOW.equalsIgnoreCase(ctype))  {
			params.put(Analytics.TV_SHOW_NAME, mData.generalInfo.title); //3
			String bitrate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), Analytics.TRAILER_BITRATE+mCardData.generalInfo._id);
			params.put(Analytics.TRAILER_DATA_RATE, bitrate);
			event =  Analytics.EVENT_PLAYED_TV_SHOW;
			Analytics.trackEvent(event,params);
			mMixPanel.getPeople().increment(Analytics.PEOPLE_TV_STREAMED,ptimeMinutes);
			mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
			Analytics.gaPlayedTvShowsTimings(ptime, mData.generalInfo.title); 
			Analytics.createEventGA(Analytics.CONSTANT_TV_SHOW, "play", mData.generalInfo.title, ptimeMinutes);//ga
			return;
		}
		
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title); //4
		
		if(Analytics.isTrailer)  {
			String bitrate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), Analytics.TRAILER_BITRATE+mCardData.generalInfo._id);
			params.put(Analytics.TRAILER_DATA_RATE, bitrate);
			event = Analytics.EVENT_PLAYED_TRAILER;
			mMixPanel.getPeople().increment(Analytics.PEOPLE_TRAILERS_PLAYED,getTotalPlayedTimeInMinutes());
			Analytics.trackEvent(event,params);	
			Analytics.isTrailer = false;
			mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
			//mMixPanel.getPeople().increment("testcount",333);
			Analytics.gaPlayedTrailerTimings(ptime, mData.generalInfo.title);
			return;
		}
		
		if(Analytics.CONSTANT_MOVIES.equalsIgnoreCase(ctype))  {
			String contentQuality = null;
			String purchaseType = null;
			boolean localPlayback = isLocalPlayBack(mData._id);//based on this properties change 
			if(cardData != null && cardData.currentUserData != null &&
                    cardData.currentUserData.purchase != null && !cardData.currentUserData.purchase.isEmpty()){
            
				CardDataPurchaseItem  purchaseitem = cardData.currentUserData.purchase.get(0);
            
            if(purchaseitem.type != null) {
            	purchaseType = purchaseitem.type;
            }
			}
    
			
			if(localPlayback) {
				//people data remove comments later
				mMixPanel.getPeople().increment(Analytics.PEOPLE_MOVIES_PLAYED_LOCALLY_FOR,getTotalPlayedTimeInMinutes());
				event = Analytics.EVENT_PLAYED_DOWNLOADED_MOVIE; //5
				params.put(Analytics.MOVIE_SIZE,getDownloadedMovieSize(mData._id)+"");
				params.put(Analytics.PAY_PURCHASE_TYPE, purchaseType); //6 //to be reviewed
				params.put(Analytics.CONTENT_QUALITY, contentQuality); //7
				
			}
			else { //streaming
				params.put(Analytics.DOWNLOAD_OPTION_AVAILABLE,"TRUE"); //5 to-be-done
				params.put(Analytics.MOVIE_SIZE,"1000mb"); //6 to-be-done
				event = Analytics.EVENT_STREAMED_MOVIE;
				mMixPanel.getPeople().increment(Analytics.PEOPLE_MOVIES_STREAMED_FOR,getTotalPlayedTimeInMinutes());
			}
			Analytics.gaPlayedMovieTimings(ptime, mData.generalInfo.title, contentQuality);	
			Analytics.createEventGA(CONSTANT_MOVIES, ACTION_TYPES.play.toString(), mCardData.generalInfo.title, ptimeMinutes);
		}
		Analytics.trackEvent(event,params);		
		mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
		mMixPanel.getPeople().increment("testcount",333);
	}
	
	/*
	 *  2 methods can be clubbed into one after review. the above method takes CardData as param
	 *  and this one gets the CardData from myplexapplication
	 */
	public static void mixPanelVideoTimeCalculationOnCompletion() {
		boolean bool = myplexapplication.getAppContext().getResources().getBoolean(R.bool.isTablet);
		CardData  mData = null;
		if(bool) { //for tablet
			mData = myplexapplication.mSelectedCard; //for tablet
		}
		else{
			if(myplexapplication.getCardExplorerData() == null) return;
			if(myplexapplication.getCardExplorerData().mMasterEntries == null || myplexapplication.getCardExplorerData().mMasterEntries.size() == 0 ) return;
			int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
			mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		}
		if(mData == null ) return;
		if(mData.generalInfo == null ) return;
		String contentName = mData.generalInfo.title;
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.USER_ID, getUserEmail()); //1
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id); //2
		params.put(Analytics.TIME_PLAYED_PROPERTY, ""+Analytics.getTotalPlayedTime()); //3
		String event = null;
		long ptime = Analytics.getTotalPlayedTime();
		
		//not necessary //for any eventuality
		if(Analytics.CONSTANT_LIVETV.equalsIgnoreCase(ctype))  {
			if(ptime == 0) return;
			params.put(Analytics.TV_CHANNEL_NAME, mData.generalInfo.title); //3
			String bitrate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), Analytics.TRAILER_BITRATE+mData.generalInfo._id);
			params.put(Analytics.TRAILER_DATA_RATE, bitrate);
			//params.put(Analytics.TRAILER_DATA_RATE, "256kbps");
			event =  Analytics.EVENT_PLAYED_TV_CHANNEL;
			Analytics.trackEvent(event,params);
			mMixPanel.getPeople().increment(Analytics.PEOPLE_TV_STREAMED,ptime);
			mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
			Analytics.gaPlayedLiveTvTimings(ptime, mData.generalInfo.title);
			Analytics.createEventGA("live tv", "play", mData.generalInfo.title, ptime);//ga
			return;
		}
		
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title); //4
		
		if(Analytics.isTrailer)  {
			String bitrate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), Analytics.TRAILER_BITRATE+mData.generalInfo._id);
			params.put(Analytics.TRAILER_DATA_RATE, bitrate);
			//params.put(Analytics.TRAILER_DATA_RATE, "256kbps");
			event = Analytics.EVENT_PLAYED_TRAILER;
			mMixPanel.getPeople().increment(Analytics.PEOPLE_TRAILERS_PLAYED,ptime);
			Analytics.trackEvent(event,params);	
			Analytics.isTrailer = false;
			mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
			mMixPanel.getPeople().increment("testcount",333);
			Analytics.gaPlayedTrailerTimings(ptime, mData.generalInfo.title);
			return;
		}
		
		if(Analytics.CONSTANT_MOVIES.equalsIgnoreCase(ctype))  {
			
			boolean localPlayback = isLocalPlayBack(mData._id);//based on this properties change 
			String contentQuality = null;
			if(localPlayback) {
				//people data remove comments later
				mMixPanel.getPeople().increment(Analytics.PEOPLE_MOVIES_PLAYED_LOCALLY_FOR,ptime);
				event = Analytics.EVENT_PLAYED_DOWNLOADED_MOVIE; //5
				params.put(Analytics.MOVIE_SIZE,getDownloadedMovieSize(mData._id)+"");
				String key = mData._id+"analytics";
				String purchaseType = null; //rent or buy
				if(mData.currentUserData != null) {
					if(mData.currentUserData.purchase != null && mData.currentUserData.purchase.size() >0) {
						CardDataPurchaseItem cardDataPurchaseItem = mData.currentUserData.purchase.get(0);
						purchaseType = cardDataPurchaseItem.type;
					}
				}
				params.put(Analytics.PAY_PURCHASE_TYPE, purchaseType); //6 
				String value = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), key);
				if(value != null) {
					String[] sArray = value.split(":");
					if(sArray != null && sArray.length == 2) {
						purchaseType = sArray[0];
						contentQuality = sArray[1];
					}
					params.put(Analytics.PAY_PURCHASE_TYPE, getUserEmail()); //6
					params.put(Analytics.CONTENT_QUALITY, contentQuality); //7
				}
			}
			else { //streaming
				params.put(Analytics.DOWNLOAD_OPTION_AVAILABLE,"TRUE"); //5
				params.put(Analytics.MOVIE_SIZE,"1000mb"); //6
				event = Analytics.EVENT_STREAMED_MOVIE;
				//people data remove comments later
				mMixPanel.getPeople().increment(Analytics.PEOPLE_MOVIES_STREAMED_FOR,ptime);
			}
			
			Analytics.gaPlayedMovieTimings(ptime, mData.generalInfo.title, contentQuality);
		}
		Analytics.trackEvent(event,params);		
		mMixPanel.getPeople().increment(Analytics.TIME_PLAYED_PROPERTY,ptime);
		mMixPanel.getPeople().increment("testcount",333);
	}
	
	private static double getDownloadedMovieSize(String contentId) {
		if(myplexapplication.mDownloadList == null ) return 0;
		if(myplexapplication.mDownloadList.mDownloadedList == null || myplexapplication.mDownloadList.mDownloadedList.size() == 0) return 0;
		HashMap<String,CardDownloadData> mDownloadedList = myplexapplication.mDownloadList.mDownloadedList;
		CardDownloadData cardDownloadData = mDownloadedList.get(contentId);
		if(cardDownloadData == null) return 0;
		return cardDownloadData.mDownloadedBytes;
	}
	
	public static boolean isLocalPlayBack(String id) {
		if( myplexapplication.mDownloadList == null) return false;
		if( myplexapplication.mDownloadList.mDownloadedList == null || myplexapplication.mDownloadList.mDownloadedList.size() == 0) return false;
		CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(id);
		if (mDownloadData == null) {
			return false;
		}
		return true;
	}

	
	public static void mixPanelSimilarContent(CardData mCardData) {
		if(mCardData == null ) return;
		if(mCardData.generalInfo == null ) return;
		String ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mCardData.generalInfo.title);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		params.put(Analytics.CONTENT_ID_PROPERTY,mCardData._id);
		//String event = Analytics.EVENT_EXPLORING_SIMILAR_CONTENT +Analytics.EMPTY_SPACE+ mCardData.generalInfo.title;
		String event = Analytics.EVENT_EXPLORING_SIMILAR_CONTENT;
		Analytics.trackEvent(event,params);
			
	}
	
	public static void mixPanelCastCrewPopup(CardData mCardData) {
		if(mCardData == null ) return;
		if(mCardData.generalInfo == null ) return;
		String ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mCardData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,mCardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		Analytics.trackEvent(Analytics.EVENT_EXPLORED_CAST_CREW_POPUP,params);
	}
	
	public static void mixPanelcardSelected(CardData mCardData) {
		if(mCardData == null ) return;
		if(mCardData.generalInfo == null ) return;
		Map<String, String> params = new HashMap<String, String>();
		String ctype = null;
		params.put(Analytics.CONTENT_ID_PROPERTY, mCardData._id);
		if(mCardData.generalInfo != null) {
			ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);//movies or livetv
			params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype );
			params.put(Analytics.CONTENT_NAME_PROPERTY, mCardData.generalInfo.title);
		}
		
		params.put(Analytics.USER_ID, getUserEmail());
		String event = Analytics.EVENT_SELECTED_DETAILS_CARD;
		if(ctype != null) {
			Analytics.trackEvent(event, params);
		}
		}
		
	public static  void mixPanelDiscoverySearchButtonClicked(String searchQuery,List<ButtonData> mSearchbleTags) {
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.DISCOVER_KEYWORD, searchQuery);
		//params.put(Analytics.NUMBER_OF_KEYWORDS, mSearchbleTags.size()+"");
		Analytics.trackEvent(Analytics.EVENT_CONTENT_DISCOVERY_INITIATED,params);
	}
	
	/*
	 * Invokd from CardExplorer. It is not fired for 1) live tv 2) movies 3) recommendations
	 * It is fired for 1) favourites 2) downloads 3) purchases
	 */
	public static void mixPanelBrowsingEvents(CardExplorerData mData,boolean mfirstTime) {
		if(mData == null ) return;
		
		if(CardExplorer.mfirstTime) {
			Map<String,String> params=new HashMap<String, String>();
			String ctype = Analytics.movieOrLivetv(mData.searchQuery);
			int rtype = mData.requestType;
			if(ctype == null) {
				String reqtype = Analytics.getRequestType(rtype);
				ctype = reqtype;
			}
			String event = null;
			
			if("favorites".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_FAVORITES, mData.mMasterEntries.size()+"");
				event = Analytics.EVENT_BROWSED_FAVORITES;
			}
			else if("downloads".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_DOWNLOADS, mData.mMasterEntries.size()+"");
				event = Analytics.EVENT_BROWSED_DOWNLOADS;
			}
			else if("purchases".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_PURCHASES, mData.mMasterEntries.size()+"");
				event = Analytics.EVENT_BROWSED_PURCHASES;
			}
			if("discover".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_RESULTS, mData.mMasterEntries.size()+"");
				if("actionbar".equalsIgnoreCase(Analytics.SEARCH_TYPE)) {
					event = Analytics.SEARCHED_MYPLEX;
					params.put(Analytics.KEYWORD,mData.searchQuery);
					params.put(Analytics.USER_ID,getUserEmail());
					Analytics.SEARCH_TYPE = null;
					Analytics.SELECTED_INLINE_WORD = null;
					Analytics.trackEvent(event,params);
					return;
				}
				if(mData.mMasterEntries.size() > 0) {
					params.put(Analytics.TOP_RESULT, mData.mMasterEntries.get(0).generalInfo.title);
				}
				else {
					params.put(Analytics.TOP_RESULT,"no results");
				}
				event = Analytics.EVENT_CONTENT_DISCOVERY_RESULTS;
				Analytics.gaDiscoveryResults(mData.searchQuery, mData.mMasterEntries.size());
				
			}
			if("inline".equalsIgnoreCase(Analytics.SEARCH_TYPE)) {
				ctype = Analytics.SEARCH_TYPE;
				params.put(Analytics.NUMBER_OF_RESULTS,mData.mMasterEntries.size()+"");
				params.put(Analytics.KEYWORD,Analytics.SELECTED_INLINE_WORD);
				event = Analytics.EVENT_SELECTED_DROPDOWN_RESULT;
				Analytics.gaInlineSearch(Analytics.SELECTED_INLINE_WORD, mData.mMasterEntries.size());
			}
			//int count = mData.mMasterEntries.size();
			if(ctype != null && (!"recommendations".equalsIgnoreCase(ctype)) && (!"movies".equalsIgnoreCase(ctype)) && (!"live tv".equalsIgnoreCase(ctype))) {
				Analytics.trackEvent(event,params);
				Analytics.SEARCH_TYPE = null;
				Analytics.SELECTED_INLINE_WORD = null;
			}
		    }
		CardExplorer.mfirstTime = false;

	}
	
		
	public static void mixPanelAddFavorite(final CardData data,int type) {
		if(data == null ) return;
		if(data.generalInfo == null ) return;
		try{
			Map<String,String> params=new HashMap<String, String>();
			MixpanelAPI.People people = Analytics.getMixpanelPeople();
			params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
			if(data.generalInfo != null) {
				params.put(Analytics.CONTENT_TYPE_PROPERTY, Analytics.movieOrLivetv(data.generalInfo.type));
				params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
				params.put(Analytics.USER_ID, getUserEmail());
		
				String event = null;
				if(type == 1) {
					event = Analytics.EVENT_ADDED_TO_FAVORITES;
					people.increment(Analytics.PEOPLE_FAVORITES, 1);
				}
				else {
					event = Analytics.EVENT_REMOVED_FROM_FAVORITES;
					people.increment(Analytics.PEOPLE_FAVORITES, -1);
				}
				Analytics.trackEvent(event,params);
			}
			}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static void mixPanelFilter(CardExplorerData mData,String label,ArrayList<CardData> localData) {
		if(mData == null ) return;
		try {
			Map<String,String> params=new HashMap<String, String>();
			String ctype = Analytics.movieOrLivetv(mData.searchQuery);
			int rtype = mData.requestType;
			String reqtype = Analytics.getRequestType(rtype);
			if(ctype == null) {
				ctype = reqtype;
			}
			params.put(Analytics.NUMBER_OF_RESULTS, String.valueOf(localData.size()));
			params.put(Analytics.FILTER_NAME,label);
			params.put(Analytics.TYPE_OF_CONTENT,ctype);
			String eventFiltered = Analytics.EVENT_FILTERED_BY_CATEGORY;
			Analytics.trackEvent(eventFiltered,params);	
			gaFilter(label, localData.size());
		}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static void mixPanelExpandedCastCrew(CardData mData) {
		if(mData == null ) return;
		if(mData.generalInfo == null ) return;
		try {
			String ctype = Analytics.movieOrLivetv(mData.generalInfo.type);
			Map<String,String> params = new HashMap<String, String>();
			params.put(Analytics.CONTENT_NAME_PROPERTY,mData.generalInfo.title);
			params.put(Analytics.CONTENT_ID_PROPERTY,mData._id);
			params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
			Analytics.trackEvent(Analytics.EVENT_EXPANDED_CAST_CREW,params);
		}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static void mixPanelEnteredCommentsReviews(CardData mData, String comment,String type,String rating) {
		if(mData == null ) return;
		if(mData.generalInfo == null ) return;
		String ctype = Analytics.movieOrLivetv(mData.generalInfo.type);
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,mData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		params.put(Analytics.USER_ID,getUserEmail());
		if("comment".equalsIgnoreCase(type)) {
			params.put(Analytics.COMMENT_TEXT,comment);
			Analytics.trackEvent(Analytics.EVENT_ENTERED_COMMENTS,params); 
			Analytics.gaComments(mData.generalInfo.title);
		}
		else if ("review".equalsIgnoreCase(type)) { 
			params.put(Analytics.REVIEW_TEXT,comment);
			params.put(Analytics.RATING,rating);
			Analytics.trackEvent(Analytics.EVENT_ENTERED_REVIEW,params);
			Analytics.gaReviews(mData.generalInfo.title);
			Analytics.gaRating(mData.generalInfo.title, Long.parseLong(rating));
			mMixPanel.getPeople().increment(Analytics.PEOPLE_ENTERED_REVIEWS,1);
		}
		
		CardDetailViewFactory.COMMENT_POSTED = null;
	}
	
	public static void mixPanelSharedMyplexExperience() {
		Map<String,String> params = new HashMap<String, String>();
		CardData cardData = Analytics.cardData;
		if(cardData == null ) return;
		if(cardData.generalInfo == null ) return;
		params.put(Analytics.CONTENT_NAME_PROPERTY,cardData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,cardData.generalInfo.type);
		params.put(Analytics.USER_ID,getUserEmail());
		params.put(Analytics.SOCIAL_NETWORK,"Yet to be done");
		Analytics.trackEvent(Analytics.EVENT_SHARED_MYPLEX_EXPERIENCE,params);
		mMixPanel.getPeople().increment(Analytics.PEOPLE_SHARED_ABOUT_MYPLEX,1);
		EasyTracker easyTracker = myplexapplication.getGaTracker();					 
		Analytics.cardData = null;			
		Analytics.createSocialGA(CATEGORY_SOCIAL_NETWORK_TYPES.google.toString(), ACTION_SHARE, cardData.generalInfo.title);
	}
	
	public static  void mixPanelPaymentOptionsPresented() {
		try{
			Map<String,String> params=new HashMap<String, String>();
			int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
			CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
			if(mData == null ) return;
			if(mData.generalInfo == null ) return;
			params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
			params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
			Analytics.trackEvent(Analytics.EVENT_PAYMENT_OPTIONS_PRESENTED,params);
		}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static  void mixPanelPaymentOptionsPresented2(CardData mData, CardDataPackages packageItem) {
		if(mData == null ) return;
		if(mData.generalInfo == null ) return;
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		String paymentChannels = getPaymentChannelsForPackage(packageItem);
		String contentQuality = getQualityAvailableForPackage(mData);
		String purchaseOptions = getPurchaseOptionsForPackage(mData);
		String priceOptions = "{" + getPriceOptionsForContent(mData) + "}";
		if(paymentChannels != null && paymentChannels.length() >0 ) {
			params.put(Analytics.PAYMENT_OPTIONS_PRESENTED,paymentChannels );
		}
		else {
			params.put(Analytics.PAYMENT_OPTIONS_PRESENTED,Analytics.UNAVAILABLE );
		}
		
		if(contentQuality != null && contentQuality.length() >0 ) {
			params.put(Analytics.CONTENT_QUALITY_AVAILABLE, contentQuality);
		}
		else {
			params.put(Analytics.CONTENT_QUALITY_AVAILABLE, Analytics.UNAVAILABLE);
		}
		
		if(purchaseOptions != null && purchaseOptions.length() >0 ) {
			params.put(Analytics.PURCHASE_OPTIONS_AVAILABLE, purchaseOptions);
		}
		else {
			params.put(Analytics.PURCHASE_OPTIONS_AVAILABLE, Analytics.UNAVAILABLE);
		}
		
		if(priceOptions != null && priceOptions.length() >0 ) {
			params.put(Analytics.PRICING_OPTIONS_AVAILABLE, priceOptions);
		}
		else {
			params.put(Analytics.PRICING_OPTIONS_AVAILABLE, Analytics.UNAVAILABLE);
		}
		
		Analytics.trackEvent(Analytics.EVENT_PAYMENT_OPTIONS_PRESENTED,params);
	}
	
	private static String getPaymentChannelsForPackage(CardDataPackages packageItem) {
		if(packageItem == null) return "";
		List<CardDataPackagePriceDetailsItem> priceDetails = null;
		priceDetails = packageItem.priceDetails;
		if(priceDetails != null && priceDetails.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for(CardDataPackagePriceDetailsItem item: priceDetails) {
				sb.append(item.paymentChannel);
				sb.append(",");
			}
			String str = sb.toString();
			str = trimCommaFromEndOfString(str);
			return str;
			}
			
		return null;
	}
	
	private static String getQualityAvailableForPackage(CardData cardData) {
		if(cardData == null ) return "";
		if(cardData.generalInfo == null ) return "";
		String ctype = movieOrLivetv(cardData.generalInfo.type);
		if("live tv".equalsIgnoreCase(ctype)) return "not applicable";
		List<String> qualityList = new ArrayList<String>(); //analytics
		StringBuffer strBuffer = new StringBuffer();
		for(CardDataPackages packageitem:cardData.packages){
			if(!qualityList.contains(packageitem.contentType)) { //to avoid duplicates
				qualityList.add(packageitem.contentType);
				strBuffer.append(packageitem.contentType);
				strBuffer.append(",");
			}
		}//end of for-loop
		if(strBuffer.length() > 0) {
			String str = trimCommaFromEndOfString(strBuffer.toString());
			return str;
		}
		return "";
	}
	
	private static String getPurchaseOptionsForPackage(CardData cardData) {
		if(cardData == null ) return "";
		StringBuffer purchaseOptionsList = new StringBuffer();
		for(CardDataPackages packageitem:cardData.packages){
			purchaseOptionsList.append(packageitem.commercialModel);
			purchaseOptionsList.append(",");
		}
		if(purchaseOptionsList.length() > 0) {
			return trimCommaFromEndOfString(purchaseOptionsList.toString());
		}
		return null;
	}
	
	private static String getPriceOptionsForContent(CardData cardData) {
		StringBuffer strBuffer = new StringBuffer();
		if(cardData == null ) return "";
		if(cardData.packages == null || cardData.packages.size() == 0) return "";
		List<CardDataPackages> listCardDataPackages = cardData.packages;
		for(CardDataPackages cdpackage: listCardDataPackages) {
			if(cdpackage.priceDetails != null && cdpackage.priceDetails.size() > 0) {
				strBuffer.append(cdpackage.priceDetails.get(0).price+"");
				strBuffer.append(",");
			}			
		}
		if(strBuffer.length() > 0) {
			String str = trimCommaFromEndOfString(strBuffer.toString());
			return str;
		}
		return null;
	}
	
	private static String trimCommaFromEndOfString(String str) {
		int lastIndex = str.lastIndexOf(",");
		str = str.substring(0,lastIndex);
		return str;
	}

	public static void mixPanelDeviceDeRegisterInitiated(int type) {
		
		UserProfile mUserInfo = myplexapplication.getUserProfileInstance();
		DeviceDetails mDevInfo = myplexapplication.getDevDetailsInstance();
		String deviceDesc  = mUserInfo.getName()+Analytics.APOS+ Analytics.EMPTY_SPACE+ mDevInfo.getDeviceOs();
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.DEVICE_ID, mDevInfo.getClientDeviceId());
		params.put(Analytics.DEVICE_DESC, deviceDesc);
		String event = null;
		if(type == 1)
			event = Analytics.EVENT_DEVICE_DEREGISTRATION_INITIATED;
		else
			event = Analytics.EVENT_DEVICE_DEREGISTRATION_SUCCEEDED; 
		Analytics.trackEvent(event,params);
	}
	
	public static void mixPanelDeviceDeRegisterfailed(String error) {
		try{
			UserProfile mUserInfo = myplexapplication.getUserProfileInstance();
			DeviceDetails mDevInfo = myplexapplication.getDevDetailsInstance();
			String deviceDesc  = mUserInfo.getName()+Analytics.APOS+ Analytics.EMPTY_SPACE+ mDevInfo.getDeviceOs();
			Map<String,String> params = new HashMap<String, String>();
			params.put(Analytics.DEVICE_ID, mDevInfo.getClientDeviceId());
			params.put(Analytics.DEVICE_DESC, deviceDesc);
			params.put(Analytics.REASON_FAILURE, error);
			String event = Analytics.EVENT_DEVICE_DEREGISTRATION_FAILURE;
			Analytics.trackEvent(event,params);
		}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static void mixPanelUserLogout() {
		try{
			Map<String,String> params2 = new HashMap<String, String>();
			UserProfile mUserInfo = myplexapplication.getUserProfileInstance();
			DeviceDetails mDevInfo = myplexapplication.getDevDetailsInstance();
			params2.put(Analytics.USER_ID,mUserInfo.getUserEmail());
			params2.put(Analytics.DEVICE_ID,mDevInfo.getClientDeviceId()); 
			String str = mDevInfo.getDeviceSNo();
			Analytics.trackEvent(Analytics.EVENT_LOGGED_OUT,params2);
		}catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
	}
	
	public static void mixPanelBrowsedSettings() {
		Map<String,String> params = new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_BROWSED_SETTINGS,params);
	}
	
	public static void mixPanelFeedbackInitiation() {
		Map<String,String> params = new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_INITIATING_FEEDBACK,params);
	}
	
	public static void mixPanelNavigationOpened(String screenOpenedFrom) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.SCREEN_OPENED_FROM,screenOpenedFrom);
		Analytics.trackEvent(Analytics.EVENT_OPENED_NAVIGATION_MENU,params);
	}
	
	public static void mixPanelInlineSearchInitiated(String screenName) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.INITIATED_FROM,screenName);
		Analytics.trackEvent(Analytics.EVENT_INLINE_SEARCH_INITIATED,params);
	}
	
	public static void mixPanelDiscoveryOptionSelected() {
		Map<String,String> params=new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_DISCOVERY_OPTION,params);		
	}
	
	public static void mixPanelProvidedFeedback(String feedBackText,String rating) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.FEEDBACK_TEXT, feedBackText);
		params.put(Analytics.FEEDBACK_RATING,rating);
		Analytics.trackEvent(Analytics.EVENT_PROVIDED_FEEDBACK,params);
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.EVENT_PROVIDED_FEEDBACK,"TRUE");
	}
	
	public static void mixPanelWifiOnly(boolean isChecked) {
		Map<String,String> params = new HashMap<String, String>();
		if(isChecked){
			params.put(Analytics.WIFI_ONLY_STATUS,Analytics.WIFI_OPTION_1);
		}else {
			params.put(Analytics.WIFI_ONLY_STATUS,Analytics.WIFI_OPTION_2);
		}
		Analytics.trackEvent(Analytics.EVENT_SELECTED_WIFI_ONLY,params);
	}
	
	
	public static void mixPanelUnableToPlayVideo(String error) {
		try{
			int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
			CardData  cardData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
			if(cardData == null ) return;
			if(cardData.generalInfo == null ) return;
			String contentName = cardData.generalInfo.title;
			Map<String,String> params = new HashMap<String, String>();
			params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
			params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
			params.put(Analytics.CONTENT_TYPE_PROPERTY,Analytics.movieOrLivetv(cardData.generalInfo.type));
			params.put(Analytics.REASON_FAILURE,error);
			params.put(Analytics.USER_ID,getUserEmail());
			String event = Analytics.EVENT_UNABLE_TO_PLAY + Analytics.EMPTY_SPACE + contentName;
			Analytics.trackEvent(event,params);
		}
		catch(Exception excp) {
			Log.d(TAG, excp.toString());
		}
		 
	}
	
	//movie size and download time is not captured
	public static void mixPanelDownloadsMovie(String contentName,String contentId,String  bytesDownloaded,String downloadTime) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
		params.put(Analytics.CONTENT_ID_PROPERTY,contentId);
		params.put(Analytics.MOVIE_SIZE,bytesDownloaded);
		params.put(Analytics.TIME_TAKEN_TO_DOWNLOAD,downloadTime);
		params.put(Analytics.USER_ID,getUserEmail());
		Analytics.trackEvent(Analytics.EVENT_DOWNLOAD_MOVIE,params);
		long dtime = 0;
		if(downloadTime != null)
			dtime = Long.parseLong(downloadTime);
		Analytics.createEventGA(CONSTANT_MOVIES, CONSTANT_DOWNLOADED, contentName, dtime);
	}
	
	
	
	public static void mixPanelCouponEntered(String couponCode,String couponPrice) {
		
		Map<String,String> params=new HashMap<String, String>();
		int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		params.put(Analytics.PAY_COUPON_CODE, couponCode);
		params.put(Analytics.PAY_COUPON_VALUE, couponPrice);
		params.put(Analytics.USER_ID, getUserEmail());
		Analytics.trackEvent(Analytics.EVENT_COUPON_ENTERED,params);
		
	}
	
	public static void mixPanelPaymentOptionsSelected(String contentId,String contentName,String paymentOption,String contentPrice) {
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, contentId);
		params.put(Analytics.CONTENT_NAME_PROPERTY, contentName);
		params.put(Analytics.PAYMENT_OPTION, paymentOption);
		params.put(Analytics.CONTENT_PRICE, contentPrice);
		params.put(Analytics.USER_ID, getUserEmail());
		String event = Analytics.EVENT_PAYMENT_OPTION_SELECTED;
		Analytics.trackEvent(event,params);
		
	}
	
	public static String getUserEmail() {
		UserProfile mUserInfo = myplexapplication.getUserProfileInstance();
		return mUserInfo.getUserEmail();
	}
	
	public static String getUserLastLoggedInDate() {
		UserProfile mUserInfo = myplexapplication.getUserProfileInstance();
		return mUserInfo.lastVisitedDate;
	}
	
	public static void mixPanelFacebookLoginSelected() {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.ACCOUNT_TYPE, "social: facebook");
		Analytics.trackEvent(Analytics.EVENT_FACEBOOK_LOGIN_SELECTED,attribs);
	}
	
	public static void mixPanelFacebookLoginSuccess(String fbId) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, "social: facebook");
		params.put(Analytics.USER_ID, fbId);
		Analytics.trackEvent(Analytics.EVENT_FACEBOOK_LOGIN_SUCCESS,params);
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, "social: facebook");
		people.set(Analytics.USER_ID, fbId);
		people.set(Analytics.LAST_LOGGED_IN_DATE, getCurrentDate());
		Analytics.createSocialGA(CATEGORY_SOCIAL_NETWORK_TYPES.facebook.toString(), ACTION_LOGIN, "");
	}
	
	//ONLY FOR FACEBOOK
	public static void mixPanelInviteFriends(String socialNetwork,String number,String status) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.SOCIAL_NETWORK,socialNetwork);
		params.put(Analytics.NUMBER_OF_INVITEES,1+"");
		params.put(Analytics.STATUS,status);
		params.put(Analytics.USER_ID,getUserEmail());
		Analytics.trackEvent(Analytics.EVENT_INVITE_FRIENDS,params);
		
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.INVITED_FRIENDS, 1);
		Analytics.createSocialGA(socialNetwork, ACTION_INVITE_FRIENDS, "");
	}
	
	public static void mixPanelGoogleLoginSelected() {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.ACCOUNT_TYPE, "social: google");
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_SELECTED,attribs);
	}
	
	public static void mixPanelGoogleLoginSuccess(String googleId) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, "social: google");
		params.put(Analytics.USER_ID, googleId);
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_SUCCESS,params);
		
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, "social: google");
		people.set(Analytics.USER_ID, googleId);
		people.set(Analytics.LAST_LOGGED_IN_DATE,getUserLastLoggedInDate()); 
		Analytics.createSocialGA(CATEGORY_SOCIAL_NETWORK_TYPES.google.toString(), ACTION_LOGIN, "");
	}
	
	public static void mixPanelGoogleLoginFailure(String googleId,String error) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, "social: google");
		if(googleId != null && googleId.length() > 0) {
			params.put(Analytics.USER_ID, googleId);
		}
		else {
			params.put(Analytics.USER_ID, Analytics.USERID_NOT_AVAILABLE);
		}
		params.put(Analytics.REASON_FAILURE, error);
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_FAILURE,params);
		
		SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), GOOGLE_LOG_IN_FAILURE_DATE, getCurrentDate());
		String lastLogFailedDate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), GOOGLE_LOG_IN_FAILURE_DATE);
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, "social: google");
		people.set(Analytics.USER_ID, googleId);
		if(lastLogFailedDate == null) {
			lastLogFailedDate = "not available";
		}
		people.set(Analytics.LAST_LOGGED_IN_FAILURE_DATE, lastLogFailedDate); 
	}
	
	public static void mixPanelGoogleConnectionFailure(String googleId,String error) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, "social: google");
		params.put(Analytics.USER_ID, Analytics.USERID_NOT_AVAILABLE);
		params.put(Analytics.REASON_FAILURE, error);
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_FAILURE,params);
		
		SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), GOOGLE_LOG_IN_FAILURE_DATE, getCurrentDate());
		String lastLogFailedDate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), GOOGLE_LOG_IN_FAILURE_DATE);
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, "social: google");
		people.set(Analytics.USER_ID, Analytics.USERID_NOT_AVAILABLE);
		if(lastLogFailedDate == null) {
			lastLogFailedDate = "not available";
		}
		people.set(Analytics.LAST_LOGGED_IN_FAILURE_DATE, lastLogFailedDate); 
	}
	
	public static void mixPanelFacebookLoginFailure(String facebookId,String error) {
		
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, "social: facebook");
		if(facebookId != null && facebookId.length() > 0) {
			params.put(Analytics.USER_ID, facebookId);
		}
		else {
			params.put(Analytics.USER_ID, Analytics.USERID_NOT_AVAILABLE);
		}
		params.put(Analytics.REASON_FAILURE,error);
		Analytics.trackEvent(Analytics.EVENT_FACEBOOK_LOGIN_FAILURE,params);
		MixpanelAPI.People people = Analytics.getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, "social: facebook");
		if(facebookId != null && facebookId.length() > 0) {
			people.set(Analytics.USER_ID, facebookId);
		}
		else {
			people.set(Analytics.USER_ID, Analytics.USERID_NOT_AVAILABLE);
		}		
		people.set(Analytics.LAST_LOGGED_IN_FAILURE_DATE, Analytics.getCurrentDate()); 
	}
	
	public static void mixPanelJoinMyplexInitiated() {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ALL_SIGN_UP_OPTIONS, "facebook google twitter myplex");
		Analytics.trackEvent(Analytics.EVENT_SIGN_UP_OPTIONS_PRESENTED,params);
	}
	
	public static void mixPanelMyplexJoinedSuccess(String email) {
		Map<String,String> params1 = new HashMap<String, String>();
		params1.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_TYPE_MYPLEX);
		params1.put(Analytics.USER_ID,email);
		params1.put(Analytics.JOINED_ON, getCurrentDate());
		Analytics.trackEvent(Analytics.EVENT_JOINED_MYPLEX_SUCCESSFULLY, params1);
		
		MixpanelAPI.People people = getMixpanelPeople();
		people.setOnce(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_TYPE_MYPLEX);
		people.setOnce(Analytics.USER_ID, email);
		people.setOnce(Analytics.PEOPLE_JOINING_DATE, getCurrentDate());
	}
	
	public static void mixPanelMyplexJoinedFailure(String email,String error) {
		Map<String,String> params1=new HashMap<String, String>();
		params1.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_TYPE_MYPLEX);
		params1.put(Analytics.REASON_FAILURE, error);
		params1.put(Analytics.USER_ID,email);
		Analytics.trackEvent(Analytics.EVENT_JOINED_MYPLEX_FAILURE, params1);
			
	}
	
	public static void mixPanelForgotPasswordInitiated(String email) {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.USER_ID, email);
		Analytics.trackEvent(Analytics.EVENT_FORGOT_PASSWORD_INITIATED,attribs); 
	}
	
	public static void mixPanelForgotPasswordSucceeded(String email) {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.USER_ID, email);
		Analytics.trackEvent(Analytics.EVENT_FORGOT_PASSWORD_SUCCEEDED,attribs); 
	}
	
	public static void mixPanelForgotPasswordFailed(String email,String error) {
		Map<String,String> attribs = new HashMap<String, String>();
		attribs.put(Analytics.USER_ID, email);
		attribs.put(Analytics.REASON_FAILURE, error);
		Analytics.trackEvent(Analytics.EVENT_FORGOT_PASSWORD_FAILED,attribs);
		
		String lastFailedDate = SharedPrefUtils.getFromSharedPreference(myplexapplication.getAppContext(), EVENT_FORGOT_PASSWORD_FAILED);
		if (lastFailedDate == null ) {
			lastFailedDate = "Not Available";
		}
		SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), EVENT_FORGOT_PASSWORD_FAILED, getCurrentDate());
			
		MixpanelAPI.People people = getMixpanelPeople();
		people.set(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_TYPE_MYPLEX);
		people.set(Analytics.USER_ID, email);
		people.set(Analytics.FORGOT_PASSWORD_FAILED_ON, lastFailedDate);
	}
	
	public static String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new Date()); 
		return date;		
	}
	
	public static void gaBrowse(String ctype,long swipeCount) {
		if(Analytics.CONSTANT_MOVIE.equalsIgnoreCase(ctype)) {
			Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_MOVIES, Analytics.NUMBER_OF_CARDS, swipeCount);
		} else 	if(Analytics.CONSTANT_LIVE.equalsIgnoreCase(ctype)) {
			Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_LIVE_TV_GA, Analytics.NUMBER_OF_CARDS, swipeCount);
		}
		else if(Analytics.CONSTANT_TV_SERIES.equalsIgnoreCase(ctype)) {
			Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_TV_SHOWS, Analytics.NUMBER_OF_CARDS, swipeCount);
		}	
	}
	
	public static void gaInlineSearch(String keyword,long results) {
		Analytics.createEventGA(Analytics.CATEGORY_SEARCH, Analytics.GA_INLINE_SEARCH, keyword, results);
	}
	
	public static void gaDiscoveryResults(String keyword,long results) {
		Analytics.createEventGA(Analytics.CATEGORY_SEARCH, Analytics.CATEGORY_DISCOVERY, keyword, results);
	}
	
	public static void gaFilter(String label,long results) {
		Analytics.createEventGA(Analytics.CATEGORY_SEARCH, Analytics.ACTION_FILTER, label, results);
	}
	
	public static void gaComments(String contentName) {
		Analytics.createEventGA(Analytics.CATEGORY_SUBMIT, Analytics.ACTION_COMMENTS, contentName, 0l);
	}
	
	public static void gaReviews(String contentName) {
		Analytics.createEventGA(Analytics.CATEGORY_SUBMIT, Analytics.ACTION_REVIEWS, contentName, 0l);
	}
	
	public static void gaRating(String contentName,long rating) {
		Analytics.createEventGA(Analytics.CATEGORY_SUBMIT, Analytics.ACTION_RATING, contentName, rating);
	}
	
	public static void gaPlayedMovie(String contentName,long rating) {
		Analytics.createEventGA(Analytics.CATEGORY_PLAYED_MOVIE, Analytics.ACTION_REVIEWS, contentName, rating);
	}
	
	public static void gaPlayedMovieEvent(CardData cardData,long rating) {
		if(cardData == null) return;
		if(cardData.generalInfo == null) return;
		if(Analytics.CONSTANT_MOVIE.equals(cardData.generalInfo.type)) {
			Analytics.createEventGA(Analytics.CATEGORY_MOVIE, Analytics.ACTION_TYPES.play.toString(), cardData.generalInfo.title, 1l);
		}else if(Analytics.CONSTANT_TV_EPISODE.equals(cardData.generalInfo.type)) {
			Analytics.createEventGA(Analytics.CONSTANT_TV_SHOW, Analytics.ACTION_TYPES.play.toString(), cardData.generalInfo.title, 1l);
		}
	}
	
	/*
	 * pause,resume does not apply to livetv
	 */
	public static void gaStopPauseMediaTime(String action,long stopPauseLocation) {
		boolean bool = myplexapplication.getAppContext().getResources().getBoolean(R.bool.isTablet);
		CardData  mData = null;
		if(bool) { //for tablet
			mData = myplexapplication.mSelectedCard; //for tablet
		}
		else{
			if(myplexapplication.getCardExplorerData() == null) return;
			if(myplexapplication.getCardExplorerData().mMasterEntries == null || myplexapplication.getCardExplorerData().mMasterEntries.size() == 0 ) return;
			int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
			mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		}
		if(mData == null ) return;
		if(mData.generalInfo == null ) return;
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		if(CONSTANT_MOVIES.equalsIgnoreCase(ctype)) {
			Analytics.createEventGA(CONSTANT_MOVIES, action, mData.generalInfo.title, stopPauseLocation);
		}else if(CONSTANT_LIVETV.equalsIgnoreCase(ctype)){
			Analytics.createEventGA(CONSTANT_LIVETV, action, mData.generalInfo.title, stopPauseLocation);
		}else if(CONSTANT_TV_SHOW.equalsIgnoreCase(ctype)){
			Analytics.createEventGA(CONSTANT_TV_SHOW, action, mData.generalInfo.title, stopPauseLocation);
		}
	}
	public static void gaStopPauseMediaTime(String action,long stopPauseLocation,CardData mData) {
		
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		if(CONSTANT_MOVIES.equalsIgnoreCase(ctype)) {
			Analytics.createEventGA(CONSTANT_MOVIES, action, mData.generalInfo.title, stopPauseLocation);
		}else if(CONSTANT_LIVETV.equalsIgnoreCase(ctype)){
			Analytics.createEventGA(CONSTANT_LIVETV, action, mData.generalInfo.title, stopPauseLocation);
		}else if(CONSTANT_TV_SHOW.equalsIgnoreCase(ctype)){
			Analytics.createEventGA(CONSTANT_TV_SHOW, action, mData.generalInfo.title, stopPauseLocation);
		}
	}
	
	public static void gaPlayedMovieTimings(long timeInSeconds,String contentName,String label) {
		Analytics.createUserTimingGA(Analytics.CATEGORY_PLAYED_MOVIE,1L , contentName, label);
	}
	
	public static void gaPlayedTrailerTimings(long timeInSeconds,String contentName) {
		Analytics.createUserTimingGA(Analytics.CATEGORY_PLAYED_TRAILER,1L , contentName, null);
	}
	
	public static void gaPlayedLiveTvTimings(long timeInSeconds,String contentName) {
		Analytics.createUserTimingGA(Analytics.CATEGORY_PLAYED_LIVETV,1L , contentName, null);
	}
	
	public static void gaPlayedTvShowsTimings(long timeInSeconds,String contentName) {
		Analytics.createUserTimingGA(Analytics.CATEGORY_PLAYED_TV_EPISODE,1L , contentName, null);
	}
			
	public static void setMixPanelEmail(String email){
		mMixPanel.identify(email);		
		mMixPanel.getPeople().identify(email);
		mMixPanel.getPeople().set("$email", email);
	}
	
	public static void setMixPanelFirstName(String firstName){
		mMixPanel.getPeople().set("$first_name", firstName);
	}
}

