package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	public static String SEARCH_TYPE = null; //useful in card explorer to differentiate discover and search
	public static String SELECTED_INLINE_WORD = null; //useful in card explorer to identify selected from inline search items
	public static boolean isTrailer = false;
	public static String trackingId = null;
	public static double priceTobecharged = 0.0;
	public static CardData cardData = null; //added for analytics
	public static long downloadStartTime;
		
	private static String TAG = "Analytics";
	public static String EVENT_LOGIN = "login";
	public static String EVENT_LOGIN_SOCIAL = "login social";
	public static String EVENT_SIGNUP = "signed up";
	public static String EVENT_BROWSE = "browsed";
	//public static String EVENT_CONTENT = "Content";
	public static String EVENT_SEARCH = "search";
	public static String EVENT_PLAY_TRAILER = "played trailer";
	public static String EVENT_PAY = "paid";
	public static String EVENT_SHARE = "share";
	public static String EVENT_CLICK = "Click";
	
	public static String EVENT_TYPE = "event type";
	public static String EVENT_SELECTED = "selected";
	
	public static String EMPTY_SPACE = " ";
	public static String APOS = "'";
	public static String EVENT_FILTERED = "filtered";
	//public static String EVENT_SEARCH_DISCOVERY = "discovery search";
	public static String EVENT_SEARCH_INLINE = "inline search";
	
	public static enum GA_CATEGORY_TYPES {Movies,LiveTv,Trailer};
		
	public static String LOGIN_TYPE_PROPERTY = "LoginType";
	public static enum LOGIN_TYPES {FaceBook,Twitter,Google,myplex,Guest};
	public static String LOGIN_DATE_PROPERTY = "LoginDate";
	public static String LOGIN_EMAIL_PROPERTY = "LoginEmail";
	public static String LOGIN_STATUS_PROPERTY = "LoginStatus";
	public static String LOGIN_STATUS_MESSAGE_PROPERTY = "LoginMessage";
	public static enum LOGIN_STATUS_TYPES {Clicked,Success,Failure,Cancel,Error};
	public static String LOGIN_FORGOT_PASSWORD_PROPERTY = "ForgotPassword";
	public static String LOGIN_AS_GUEST = "Guest";
	public static String LOGIN_FACEBOOK = "FacebookLogin";
	public static String LOGIN_TWITTER = "TwitterLogin";
	public static String LOGIN_GOOGLE = "GoogleLogin";
	public static String LOGIN_CLICK = "Click";
	
	public static String SIGNUP_TYPE_PROPERTY = "SignupType";
	public static String SIGNUP_DATE_PROPERTY = "SignupDate";
	public static String SIGNUP_EMAIL_PROPERTY = "SignEmail";
	public static enum SIGNUP_TYPES {myplex};
	public static String SIGNUP_STATUS_PROPERTY = "SignupStatus";
	public static enum SIGNUP_STATUS_TYPES {Success,Failure};
	
	public static String BROWSE_TYPE_PROPERTY = "BrowseType";
	public static enum BROWSE_TYPES {cards,navigation,filter,relatedcontent};
	public static enum BROWSE_CARDACTION_TYPES {Delete,Swipe};
	public static enum BROWSE_NAVIGATION_TYPES {Favourites,Purchases,Downloads,Discover,Settings,Logout,Home,Movies,LiveTv};
	
	
	
	public static String CONTENT_CATEGORY_PROPERTY = "ContentCategory";
	public static String CONTENT_DETAILS_PROPERTY = "ContentDetails";
	public static String CONTENT_CARD_STATUS = "ContentCardStatus";
	public static String CONTENT_CARD_OPENED = "ContentCardExpanded";
	//public static String CONTENT_CARD_DETAILS = "ContentCardExpanded";
	public static String CONTENT_CARD_DETAILS_PROPERTY = "ContentCardDescriptionExpanded";
	public static enum CONTENT_ACTION_TYPES {Favourite,Detailed,PlayTrailer,Share,movie};
	public static enum CONTENT_SHARE_TYPES {Facebook,Google};
	public static String CONTENT_PLAY_ERROR_Property = "ContentPlayError";
	public static String CONTENT_PLAY_ERROR = "Cannot Play Video";
	
	
	public static String SEARCH_TYPE_PROPERTY = "SearchType";
	public static String SEARCH_FILTER_TYPE_PROPERTY = "SearchFilterLabel";
	public static enum SEARCH_TYPES {DropDown,Discover,Filter,Inline};
	public static enum DROPDOWN_STATUS_TYPES {Success,Failure};
	public static enum DISCOVER_STATUS_TYPES {tag};
	public static String SEARCH_NUMBER_FOUND_PROPERTY = "NumberOfCardsFound";
	public static String SEARCH_QUERY_PROPERTY = "SearchQuery";
	public static String SEARCH_SCREEN_PROPERTY = "SearchScreen";
		
	public static String FILTERED_ALL = "all";
	
	
	public static String PLAY_CONTENT_ID_PROPERTY = "ContentId Playing";
	public static String PLAY_CONTENT_NAME_PROPERTY = "ContentName Playing";
	public static String PLAY_CONTENT_STATUS_PROPERTY = "Content Play Status";
	public static String PLAY_CONTENT_START_TIME_PROPERTY = "Content Start Time";
	public static String PLAY_CONTENT_END_TIME_PROPERTY = "Content End Time";
	public static String PLAY_CONTENT_PAUSE_TIME_PROPERTY = "Content Pause Time";
	public static String PLAY_CONTENT_RESUME_TIME_PROPERTY = "Content Resume Time";
	public static String PLAY_CONTENT_SEEK_TIME_PROPERTY = "Content Seek Time";
	public static String PLAY_CONTENT_ERROR_PROPERTY = "ErrorMessage";
	public static String PLAY_CONTENT_WIDEVINE_ERROR = "Widevine Authorization Failed";
	public static enum PLAY_CONTENT_STATUS_TYPES {Start,End,Pause,Resume,Seek,Playing,SeekComplete,PlayerRightsAcquisition,Error};
	
	public static enum PAY_MODEL_TYPES {CreditCard,DebitCard,InternetBanking,OperatorBilling,InAppPurchase};
	public static enum PAY_COMMERCIAL_TYPES {Rental,Buy};
	public static enum PAY_CONTENT_TYPES {SD,HD};
	public static enum PAY_CONTENT_STATUS_TYPES {Success,Failure};
	public static String PAY_STATUS_PROPERTY = "PayStatus";
	public static String PAY_COMMERCIAL_TYPE_PROPERTY =  "CommercialModel";
	public static String PAY_PAYMENT_MODEL =  "PaymentModel";
	
	public static String PAY_PACKAGE_ID = "PackageId";
	public static String PAY_PACKAGE_NAME = "PackageName";
	public static String PAY_PACKAGE_CHANNEL = "PackageChannel";
	public static String PAY_PACKAGE_PURCHASE_STATUS = "Status";
	
	
	public static String GA_AFFILIATION = "GoogleStore";
	public static String GA_CURRENCY = "INR";
	
	//public static String COMMENT_TEXT = "CommentEntered";
	
	public static enum SCREEN_NAMES {CardExplorer,Settings,CardDetails,CardDetailsTabletFrag,SearchSuggestions};
	
	public static long currentTime = 0;
	public static long resumedAt = 0;
	public static long pausedAt = 0;
	public static long playedTime = 0;
	public static long totalPlayedTime = 0;
	
	/*
	 * new mixpanel events 
	 */
	public static String EVENT_DEVICE_REGISTRATION_SUCCESS = "device registration success";
	public static String EVENT_DEVICE_REGISTRATION_FAILED = "device registration failed";
	public static String EVENT_DEVICE_DEREGISTRATION_INITIATED = "device deregistration initiated";
	public static String EVENT_DEVICE_DEREGISTRATION_SUCCEEDED = "device deregistration succeeded";
	public static String EVENT_JOIN_MYPLEX_INITIATED = "join myplex initiated";
	public static String EVENT_MYPLEX_SIGNUP_OPTION = "myplex sign up option"; 
	public static String EVENT_JOINED_MYPLEX_SUCCESSFULLY = "joined myplex successfully";
	public static String EVENT_JOINED_MYPLEX_FAILURE = "failed to create myplex account";
	public static String EVENT_LOGIN_OPTIONS_PRESENTED = "login options presented";
	public static String EVENT_GOOGLE_LOGIN_SELECTED = "google login selected";
	public static String EVENT_MYPLEX_LOGIN_SELECTED = "myplex login selected";
	public static String EVENT_FACEBOOK_LOGIN_SELECTED = "facebook login selected";
	public static String EVENT_TWITTER_LOGIN_SELECTED = "twitter login selected";
	public static String EVENT_BROWSING_AS_GUEST = "browsing as guest";
	public static String EVENT_BROWSED_MOVIES = "browsed movies";
	public static String EVENT_BROWSED_RECOMMENDATIONS = "browsed recommendations";
	public static String EVENT_BROWSED_TV_CHANNELS = "browsed live TV channels";
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
	public static String EVENT_EXPLORING_SIMILAR_CONTENT = "exploring similar items of";
	public static String EVENT_SELECTED_WIFI_ONLY = "selected wifi only";
	public static String EVENT_LOGGED_OUT = "logged out";
	public static String EVENT_ADDED = "added";
	public static String EVENT_REMOVED = "removed";
	public static String EVENT_DELETED = "deleted";
	public static String EVENT_SUBSCRIBED_FREE_FOR = "subscribed free for";
	public static String EVENT_SUBSCRIPTION_FAILURE = "subscription/payment failed for";
	public static String EVENT_UNABLE_TO_PLAY = "unable to play";
	public static String EVENT_DOWNLOAD_MOVIE = "downloads movie";
	public static String EVENT_INVITE_FRIENDS = "invite friends";
		
	public static String EVENT_GOOGLE_LOGIN_SUCCESS = "google login success";
	public static String EVENT_MYPLEX_LOGIN_SUCCESS = "myplex login success";
	public static String EVENT_FACEBOOK_LOGIN_SUCCESS = "facebook login success";
	public static String EVENT_TWITTER_LOGIN_SUCCESS = "twitter login success";
	
	public static String EVENT_GOOGLE_LOGIN_FAILURE = "google login failure";
	public static String EVENT_MYPLEX_LOGIN_FAILURE = "myplex login failure";
	public static String EVENT_FACEBOOK_LOGIN_FAILURE = "facebook login failure";
	public static String EVENT_TWITTER_LOGIN_FAILURE = "twitter login failure";
	
	public static String EVENT_FORGOT_PASSWORD_INITIATED = "forgot password initiated";
	public static String EVENT_FORGOT_PASSWORD_SUCCEEDED = "forgot password succeeded";
	public static String EVENT_FORGOT_PASSWORD_FAILED = "forgot password failed";
	
	public static String EVENT_FILTERED_BY = "filtered by";
	public static String FILTER_NAME = "filter name";
	public static String NUMBER_OF_RESULTS = "number of results";
	
	public static String TYPE_OF_CONTENT = "type of content";
	public static String EVENT_DISCOVERY_OPTION = "discovery option selected";
	public static String DISCOVER_KEYWORD = "discover keyword";
	public static String EVENT_CONTENT_DISCOVERY_INITIATED = "content discovery initiated";
	public static String EVENT_CONTENT_DISCOVERY_RESULTS = "content discovery results";
	public static String EVENT_SELECTED_A_KEYWORD_DISCOVERY = "selected a keyword";
	public static String SEARCHED_FOR = "searched for";
	public static String EVENT_SELECTED_IN_DROPDOWN_RESULTS = "in the drop down results";
	public static String EVENT_INLINE_SEARCH_INITIATED = "inline search initiated";
	
	public static String EVENT_BROWSED = "browsed";
	public static String NUMBER_OF_PURCHASES = "number of purchases";
	public static String NUMBER_OF_FAVORITES = "number of favorites";
	public static String NUMBER_OF_DOWNLOADS = "number of downloads";
	public static String NUMBER_OF_MOVIE_CARDS = "number of movie cards";
	public static String NUMBER_OF_LIVETV_CARDS = "number of live TV cards";
	public static String NUMBER_OF_KEYWORDS = "number of keywords";
	public static String NUMBER_OF_INVITEES = "number of invitees";
		
	public static String ACCOUNT_TYPE = "account type";
	public static String USER_ID = "user id";
	public static String ACCOUNT_TYPE_MYPLEX = "myplex";
	public static String DEVICE_ID = "device id";
	public static String DEVICE_DESC = "device description";
	public static String REASON_FAILURE = "reason for failure";
	public static enum ALL_LOGIN_TYPES {facebook,twitter,google,myplex,Guest};
	public static String SOCIAL_NETWORK = "social network";
	
	public static String CONTENT_ID_PROPERTY = "content id";
	public static String CONTENT_NAME_PROPERTY = "content name";
	public static String CONTENT_TYPE_PROPERTY = "content type";
	public static String CONTENT_PRICE = "content price";
	public static String TIME_PLAYED_PROPERTY = "time played (in seconds)";
	public static String FEEDBACK_TEXT = "feedback text";
	public static String FEEDBACK_RATING = "rating";
	public static String MOVIE_SIZE = "movie size";
	public static String TIME_TAKEN_TO_DOWNLOAD = "time taken to download (in minutes)";
	
	public static String EVENT_PLAY = "played";
	public static String TRAILER = "trailer";
	public static String MOVIE = "movie";
	public static String MOVIES = "movies";
	public static String KEYWORD = "keyword";
	public static String TV_CHANNEL = "TV channel";
	public static String TV_CHANNELS = "live TV channels";
	public static String TO_FAVORITES = "to favorites";
	public static String FROM_FAVORITES = "from favorites";
	public static String FROM_CARDS = "from cards";
	public static String STATUS = "status";
	
	public static String EVENT_PAYMENT_OPTIONS_PRESENTED = "payment options presented";
	public static String EVENT_PAYMENT_OPTION_SELECTED = "payment option selected";
	public static String EVENT_PAID_FOR = "paid for";
	public static String EVENT_COUPON_ENTERED = "coupon entered";
	public static String PAY_COUPON_CODE = "coupon code";
	public static String PAY_COUPON_VALUE = "coupon value";
	public static String PAY_PURCHASE_TYPE = "purchase type";
	public static String PAY_CONTENT_PRICE = "content price";
	public static String PAYMENT_METHOD =  "payment method";
	public static String CONTENT_QUALITY = "content quality";
	public static String PAYMENT_OPTION =  "payment option";
	
	public static String SCREEN_OPENED_FROM = "opened from";
	public static String DETAILS = "details";
	public static String COMMENT_TEXT = "comment text";
	public static String WIDEVINE_AUTH_FAILED = "Widevine authorization failed";
	public static String ACQUIRE_RIGHTS_FAILED = "acquire rights failed";
	public static String NO_URL_TO_PLAY = "no url to play";
	public static String FAILED_TO_FETCH_URL = "Failed in fetching the url";
	
	private static MixpanelAPI mMixPanel=myplexapplication.getMixPanel();

	public static void trackEvent(String aEventName,Map<String, String> params){
		
		if(ApplicationSettings.ENABLE_MIXPANEL_API) {
			mMixPanel.track(aEventName,getJSON(params));
		}
	}
	
	public static void trackCharge(double price){
		
		if(ApplicationSettings.ENABLE_MIXPANEL_API) {
			MixpanelAPI.People people = mMixPanel.getPeople();
			people.identify(trackingId);
			people.trackCharge(price, null);
		}
	}

	private static JSONObject getJSON(Map<String, String> params){
		JSONObject data= new JSONObject();
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

	public static void  createTransactionGA(EasyTracker easyTracker,String transactionid,String affiliation,Double revenue, Double tax, Double shippingCost) {
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
	
	public static void  createItemGA(EasyTracker easyTracker,String transactionid,String productName,String productSKU,String productCategory,
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
	
	public static void  createEventGA(EasyTracker easyTracker,String category,String action,String label) {
		
		easyTracker.send(
			     MapBuilder.createEvent(category, action, label, 0l).build());
	}
	
	public static void  createSocialGA(EasyTracker easyTracker,String category,String action,String label) {
		easyTracker.send(MapBuilder
			    .createSocial(Analytics.LOGIN_FACEBOOK,    // Social network (required)
			                  Analytics.LOGIN_STATUS_TYPES.Success.toString(), // Social action (required)
			                  "")   // Social target
			    .build()
			);
	}
	
	public static void  createScreenGA(EasyTracker easyTracker,String screenName) {
		easyTracker.set(Fields.SCREEN_NAME, screenName);
		easyTracker.send(MapBuilder.createAppView()
				  .build()
				);
	}
	
	public static void startActivity(EasyTracker easyTracker,Activity activity) {
		easyTracker.activityStart(activity);
	}
	
	public static String movieOrLivetv(String contentType) {
		String ctype = null;
		if(contentType.equalsIgnoreCase("SD") || contentType.equalsIgnoreCase("HD") || contentType.equalsIgnoreCase("movie") ) {
			ctype = "movies";
		}
		else if(contentType.equalsIgnoreCase("Monthly") || contentType.equalsIgnoreCase("Weekly") || 
				contentType.equalsIgnoreCase("Yearly") || contentType.equalsIgnoreCase("live")) {
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
			ctype= "favourites";
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
	
	/*
	 * begin at 10.00 currentTime = ct, totalPlayedTime = 0
	 * pause at 10.10 pausedAt=10.10  playedTime = 10.10 - x = 10 mts, totalPlayedTime = 0 + 10 = 10 mts
	 * resume at 10.20 currentTime = ct 
	 * one of the below could happen
	 * pause at 10.35 playedTime = 10.35 - ct = 15 mts totalPlayedTime = 10 + 15 = 25 mts
	 * stop at 10.45 playedTime = 10.35 - x(20) = 15 mts totalPlayedTime = 10 + 15 = 25 mts
	 *  
	 * 
	 */
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
	
	public static long getTotalPlayedTime() {
		//Log.d(TAG, "checktime Player closed total time is in seconds "+totalPlayedTime);
		Log.d(TAG, "checktime Player closed total time is in minutes"+TimeUnit.SECONDS.toMinutes(totalPlayedTime));
		//return TimeUnit.SECONDS.toMinutes(totalPlayedTime)+1;
		return totalPlayedTime+1;
	}
	
	//CardDetails
	public static void mixPanelVideoTimeCalculation(CardData mCardData) {
		CardData mData = mCardData; 
		String contentName = mData.generalInfo.title;
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		params.put(Analytics.TIME_PLAYED_PROPERTY, ""+Analytics.getTotalPlayedTime());
		String event = null;
		if("movies".equalsIgnoreCase(ctype))  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.MOVIE;
		}
		
		if("live tv".equalsIgnoreCase(ctype))  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.TV_CHANNEL;
		}
		
		if(Analytics.isTrailer)  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.TRAILER;
			Analytics.isTrailer = false;
		}
		Analytics.trackEvent(event,params);

	}
	
	public static void mixPanelVideoTimeCalculationOnCompletion() {
		int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		String contentName = mData.generalInfo.title;
		String contentType = mData.generalInfo.type; //movie or livetv
		String ctype = Analytics.movieOrLivetv(contentType);
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		params.put(Analytics.TIME_PLAYED_PROPERTY, ""+Analytics.getTotalPlayedTime());
		String event = null;
		if("movies".equalsIgnoreCase(ctype))  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.MOVIE;
		}
		
		if("live tv".equalsIgnoreCase(ctype))  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.TV_CHANNEL;
		}
		
		if(Analytics.isTrailer)  {
			event = Analytics.EVENT_PLAY+ Analytics.EMPTY_SPACE+mData.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.TRAILER;
			Analytics.isTrailer = false;
		}
		Analytics.trackEvent(event,params);
		Analytics.totalPlayedTime = 0;
	}

	
	public static void mixPanelSimilarContent(CardData mCardData) {
		String ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mCardData.generalInfo.title);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		params.put(Analytics.CONTENT_ID_PROPERTY,mCardData._id);
		String event = Analytics.EVENT_EXPLORING_SIMILAR_CONTENT +Analytics.EMPTY_SPACE+ mCardData.generalInfo.title;
		Analytics.trackEvent(event,params);
		EasyTracker easyTracker = myplexapplication.getGaTracker();		
		
		Analytics.createEventGA(easyTracker,ctype, Analytics.EVENT_BROWSE,mCardData.generalInfo.title);
	}
	
	public static void mixPanelCastCrewPopup(CardData mCardData) {
		String ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mCardData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,mCardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		Analytics.trackEvent(Analytics.EVENT_EXPLORED_CAST_CREW_POPUP,params);
	}
	
	public static void mixPanelcardSelected(CardData mCardData) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, mCardData._id);
		String ctype = Analytics.movieOrLivetv(mCardData.generalInfo.type);//movies or livetv
		params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype );
		params.put(Analytics.CONTENT_NAME_PROPERTY, mCardData.generalInfo.title);
		String event = Analytics.EVENT_SELECTED +Analytics.EMPTY_SPACE+mCardData.generalInfo.title+ Analytics.EMPTY_SPACE + ctype +Analytics.EMPTY_SPACE +Analytics.DETAILS;;
		Analytics.trackEvent(event, params);
		EasyTracker easyTracker = myplexapplication.getGaTracker();					 
		Analytics.createEventGA(easyTracker,ctype, Analytics.EVENT_BROWSE,mCardData.generalInfo.title);
	}
	
	public static  void mixPanelDiscoverySearchButtonClicked(String searchQuery,List<ButtonData> mSearchbleTags) {
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.DISCOVER_KEYWORD, searchQuery);
		params.put(Analytics.NUMBER_OF_KEYWORDS, mSearchbleTags.size()+"");
		Analytics.trackEvent(Analytics.EVENT_CONTENT_DISCOVERY_INITIATED,params);
		EasyTracker easyTracker = myplexapplication.getGaTracker();
		Analytics.createEventGA(easyTracker, Analytics.EVENT_CONTENT_DISCOVERY_INITIATED, Analytics.SEARCH_TYPES.Discover.toString(),Analytics.SEARCH_TYPES.Discover.toString());
	}
	
	/*
	 * Invokd from CardExplorer. It is not fired for 1) live tv 2) movies 3) recommendations
	 * It is fired for 1) favourites 2) downloads 3) purchases
	 */
	public static void mixPanelBrowsingEvents(CardExplorerData mData,boolean mfirstTime) {
		if(CardExplorer.mfirstTime) {
			Map<String,String> params=new HashMap<String, String>();
			String ctype = Analytics.movieOrLivetv(mData.searchQuery);
			int rtype = mData.requestType;
			String reqtype = Analytics.getRequestType(rtype);
			if(ctype == null) {
				ctype = reqtype;
			}
				
			if("favourites".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_FAVORITES, mData.mMasterEntries.size()+"");
			}
			else if("downloads".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_DOWNLOADS, mData.mMasterEntries.size()+"");
			}
			else if("purchases".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_PURCHASES, mData.mMasterEntries.size()+"");
			}
			else if("live tv".equalsIgnoreCase(ctype)) {
				//ctype = Analytics.TV_CHANNELS;
				params.put(Analytics.NUMBER_OF_LIVETV_CARDS,1+"");
			}
			else if("movies".equalsIgnoreCase(ctype)) {
				ctype = Analytics.MOVIES;
				params.put(Analytics.NUMBER_OF_MOVIE_CARDS,1+"");
			}
			else if("recommendations".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_MOVIE_CARDS,1+"");
			}
			//params.put(Analytics.NUMBER_OF_MOVIE_CARDS,swipeCount+"");
			String event = Analytics.EVENT_BROWSED +Analytics.EMPTY_SPACE+ ctype;
			
			//this code is placed here to capture the number of search results. till we discover a better place??
			if("discover".equalsIgnoreCase(ctype)) {
				params.put(Analytics.NUMBER_OF_RESULTS, mData.mMasterEntries.size()+"");
				event = Analytics.EVENT_CONTENT_DISCOVERY_RESULTS;
				if("actionbar".equalsIgnoreCase(Analytics.SEARCH_TYPE)) {
					event = Analytics.SEARCHED_FOR+Analytics.EMPTY_SPACE+mData.searchQuery;
					Analytics.SEARCH_TYPE = null;
				}
			}
			if("inline".equalsIgnoreCase(Analytics.SEARCH_TYPE)) {
				ctype = Analytics.SEARCH_TYPE;
				params.put(Analytics.NUMBER_OF_RESULTS,mData.mMasterEntries.size()+"");
				params.put(Analytics.KEYWORD,Analytics.SELECTED_INLINE_WORD);
				event = Analytics.EVENT_SELECTED+Analytics.EMPTY_SPACE+Analytics.SELECTED_INLINE_WORD+Analytics.EMPTY_SPACE+Analytics.EVENT_SELECTED_IN_DROPDOWN_RESULTS;
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
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
		if(data.generalInfo != null){
			params.put(Analytics.CONTENT_TYPE_PROPERTY, Analytics.movieOrLivetv(data.generalInfo.type));
			params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
		}
		String event = null;
		if(type == 1) {
			event = Analytics.EVENT_ADDED + Analytics.EMPTY_SPACE+data.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.TO_FAVORITES;
		}
		else {
			event = Analytics.EVENT_REMOVED + Analytics.EMPTY_SPACE+data.generalInfo.title+Analytics.EMPTY_SPACE+Analytics.FROM_FAVORITES;
		}
		Analytics.trackEvent(event,params);
	}
	
	public static void mixPanelFilter(CardExplorerData mData,String label,ArrayList<CardData> localData) {
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
		String eventFiltered = Analytics.EVENT_FILTERED_BY+ Analytics.EMPTY_SPACE+label.toLowerCase();
		Analytics.trackEvent(eventFiltered,params);
		EasyTracker easyTracker = myplexapplication.getGaTracker();	
		Analytics.createEventGA(easyTracker, Analytics.EVENT_FILTERED, Analytics.SEARCH_TYPES.Filter.toString(), label);
	}
	
	public static void mixPanelExpandedCastCrew(CardData mData) {
		String ctype = Analytics.movieOrLivetv(mData.generalInfo.type);
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,mData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		Analytics.trackEvent(Analytics.EVENT_EXPANDED_CAST_CREW,params);
	}
	
	public static void mixPanelEnteredCommentsReviews(CardData mData, String comment,String type) {
		String ctype = Analytics.movieOrLivetv(mData.generalInfo.type);
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,mData.generalInfo.title);
		params.put(Analytics.CONTENT_ID_PROPERTY,mData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,ctype);
		params.put(Analytics.COMMENT_TEXT,comment);
		if("comment".equalsIgnoreCase(type)) {
			Analytics.trackEvent(Analytics.EVENT_ENTERED_COMMENTS,params);
		}
		else if ("review".equalsIgnoreCase(type)) {
			Analytics.trackEvent(Analytics.EVENT_ENTERED_REVIEW,params);
		}
		
		EasyTracker easyTracker = myplexapplication.getGaTracker();	
		Analytics.createEventGA(easyTracker,ctype, Analytics.EVENT_COMMENT,mData.generalInfo.title);
		CardDetailViewFactory.COMMENT_POSTED = null;
	}
	
	public static void mixPanelSharedMyplexExperience() {
		Map<String,String> params = new HashMap<String, String>();
		CardData cardData = Analytics.cardData;
		if(cardData != null) {
			params.put(Analytics.CONTENT_NAME_PROPERTY,cardData.generalInfo.title);
			params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
			params.put(Analytics.CONTENT_TYPE_PROPERTY,cardData.generalInfo.type);
			Analytics.trackEvent(Analytics.EVENT_SHARED_MYPLEX_EXPERIENCE,params);
			EasyTracker easyTracker = myplexapplication.getGaTracker();					 
			//Analytics.createEventGA(easyTracker,Analytics.EVENT_SHARE, Analytics.EVENT_SHARE,aTitle);
			Analytics.cardData = null;			
		}
	}
	
	public static  void mixPanelPaymentOptionsPresented() {
		Map<String,String> params=new HashMap<String, String>();
		int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		Analytics.trackEvent(Analytics.EVENT_PAYMENT_OPTIONS_PRESENTED,params);
	}

	public static void mixPanelDeviceRegisterInitiated(int type) {
		
		UserProfile mUserInfo=myplexapplication.getUserProfileInstance();
		DeviceDetails mDevInfo=myplexapplication.getDevDetailsInstance();
		String deviceDesc  = mUserInfo.getName()+Analytics.APOS+ Analytics.EMPTY_SPACE+ mDevInfo.getDeviceOs();
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.DEVICE_ID, mDevInfo.getDeviceId());
		params.put(Analytics.DEVICE_DESC, deviceDesc);
		String event = null;
		if(type == 1)
			event = Analytics.EVENT_DEVICE_DEREGISTRATION_INITIATED;
		else
			event = Analytics.EVENT_DEVICE_DEREGISTRATION_SUCCEEDED; 
		Analytics.trackEvent(event,params);
	}
	
	public static void mixPanelUserLogout() {
		Map<String,String> params2 = new HashMap<String, String>();
		UserProfile mUserInfo=myplexapplication.getUserProfileInstance();
		DeviceDetails mDevInfo=myplexapplication.getDevDetailsInstance();
		params2.put(Analytics.USER_ID,mUserInfo.getUserEmail());
		params2.put(Analytics.DEVICE_ID,mDevInfo.getDeviceId());
		Analytics.trackEvent(Analytics.EVENT_LOGGED_OUT,params2);
	}
	
	public static void mixPanelBrowsedSettings() {
		Map<String,String> params=new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_BROWSED_SETTINGS,params);
		//EasyTracker easyTracker2 = EasyTracker.getInstance(getActivity());	
		//Analytics.createScreenGA(easyTracker2, Analytics.SCREEN_NAMES.Settings.toString());
	}
	
	public static void mixPanelFeedbackInitiation() {
		Map<String,String> params=new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_INITIATING_FEEDBACK,params);
	}
	
	public static void mixPanelNavigationOpened(String screenOpenedFrom) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.SCREEN_OPENED_FROM,screenOpenedFrom);
		Analytics.trackEvent(Analytics.EVENT_OPENED_NAVIGATION_MENU,params);
	}
	
	public static void mixPanelInlineSearchInitiated() {
		Map<String,String> params=new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_INLINE_SEARCH_INITIATED,params);
	}
	
	public static void mixPanelDiscoveryOptionSelected() {
		/*EasyTracker easyTracker = myplexapplication.getGaTracker();
		 Analytics.createEventGA(easyTracker, Analytics.EVENT_DISCOVERY_OPTION, Analytics.SEARCH_TYPES.Discover.toString(), "DiscoverScreen");
		*/
		Map<String,String> params=new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_DISCOVERY_OPTION,params);
		
	}
	
	public static void mixPanelProvidedFeedback(String feedBackText,String rating) {
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.FEEDBACK_TEXT, feedBackText);
		params.put(Analytics.FEEDBACK_RATING,rating);
		Analytics.trackEvent(Analytics.EVENT_PROVIDED_FEEDBACK,params);
		
	}
	
	public static void mixPanelWifiOnly() {
		Map<String,String> params = new HashMap<String, String>();
		Analytics.trackEvent(Analytics.EVENT_SELECTED_WIFI_ONLY,params);
	}
	
	
	public static void mixPanelUnableToPlayVideo(String error) {
		EasyTracker easyTracker = myplexapplication.getGaTracker();
    	int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  cardData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		String contentName = cardData.generalInfo.title;
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
		params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,Analytics.movieOrLivetv(cardData.generalInfo.type));
		params.put(Analytics.REASON_FAILURE,error);
		String event = Analytics.EVENT_UNABLE_TO_PLAY + Analytics.EMPTY_SPACE + contentName;
		Analytics.trackEvent(event,params);
		//Analytics.createEventGA(easyTracker, Analytics.EVENT_PLAY,Analytics.CONTENT_PLAY_ERROR,contentName ); 	
	        
	}
	
	//movie size and download time is not captured
	public static void mixPanelDownloadsMovie(String contentName,String contentId,String  bytesDownloaded,String downloadTime) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
		params.put(Analytics.CONTENT_ID_PROPERTY,contentId);
		params.put(Analytics.MOVIE_SIZE,bytesDownloaded);
		params.put(Analytics.TIME_TAKEN_TO_DOWNLOAD,downloadTime);
		Analytics.trackEvent(Analytics.EVENT_DOWNLOAD_MOVIE,params);
		
	}
	
	public static void mixPanelUnableToPlayVideo2(String error) {
    	
        EasyTracker easyTracker = myplexapplication.getGaTracker();
    	int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  cardData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		String contentName = cardData.generalInfo.title;
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
		params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,Analytics.movieOrLivetv(cardData.generalInfo.type));
		params.put(Analytics.REASON_FAILURE,error);
		String event = Analytics.EVENT_UNABLE_TO_PLAY + Analytics.EMPTY_SPACE + contentName;
		Analytics.trackEvent(event,params);
		//Analytics.createEventGA(easyTracker, Analytics.EVENT_PLAY,Analytics.CONTENT_PLAY_ERROR,contentName );
    }
	
	public static void mixPanelCouponEntered(String couponCode,String couponPrice) {
		
		Map<String,String> params=new HashMap<String, String>();
		int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		params.put(Analytics.PAY_COUPON_CODE, couponCode);
		params.put(Analytics.PAY_COUPON_VALUE, couponPrice);
		Analytics.trackEvent(Analytics.EVENT_COUPON_ENTERED,params);
		
	}
	
	public static void mixPanelPaymentOptionsSelected(String contentId,String contentName,String paymentOption,String contentPrice) {
		
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, contentId);
		params.put(Analytics.CONTENT_NAME_PROPERTY, contentName);
		params.put(Analytics.PAYMENT_OPTION, paymentOption);
		params.put(Analytics.CONTENT_PRICE, contentPrice);
		String event = paymentOption+ Analytics.EMPTY_SPACE + Analytics.EVENT_PAYMENT_OPTION_SELECTED;
		Analytics.trackEvent(event,params);
		
	}
	
	public static void mixPanelFacebookLoginSelected() {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.ACCOUNT_TYPE, Analytics.ALL_LOGIN_TYPES.facebook.toString());
		Analytics.trackEvent(Analytics.EVENT_FACEBOOK_LOGIN_SELECTED,attribs);
	}
	
	public static void mixPanelInviteFriends(String socialNetwork,String number,String status) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.SOCIAL_NETWORK,socialNetwork);
		params.put(Analytics.NUMBER_OF_INVITEES,1+"");
		params.put(Analytics.STATUS,status);
		Analytics.trackEvent(Analytics.EVENT_INVITE_FRIENDS,params);
	}
	
	public static void mixPanelGoogleLoginSelected() {
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.ACCOUNT_TYPE, Analytics.ALL_LOGIN_TYPES.google.toString());
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_SELECTED,attribs);
	}
	
	public static void mixPanelGoogleLoginSuccess(String googleId) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, LOGIN_TYPES.Google.toString());
		params.put(Analytics.USER_ID, googleId);
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_SUCCESS,params);
	}
	
	public static void mixPanelFacebookLoginSuccess(String fbId) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, LOGIN_TYPES.FaceBook.toString());
		params.put(Analytics.USER_ID, fbId);
		Analytics.trackEvent(Analytics.EVENT_FACEBOOK_LOGIN_SUCCESS,params);
	}
	
	public static void mixPanelGoogleLoginFailure(String googleId,String error) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.ACCOUNT_TYPE, LOGIN_TYPES.Google.toString());
		params.put(Analytics.USER_ID, googleId);
		params.put(Analytics.REASON_FAILURE, error);
		Analytics.trackEvent(Analytics.EVENT_GOOGLE_LOGIN_FAILURE,params);
	}
	
	public static void mixPanelJoinMyplexInitiated() {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.EVENT_MYPLEX_SIGNUP_OPTION, Analytics.ACCOUNT_TYPE_MYPLEX);
		Analytics.trackEvent(Analytics.EVENT_JOIN_MYPLEX_INITIATED,params);
	}
	
	public static void mixPanelMyplexJoinedSuccess(String email) {
		Map<String,String> params1 = new HashMap<String, String>();
		params1.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_TYPE_MYPLEX);
		params1.put(Analytics.USER_ID,email);
		Analytics.trackEvent(Analytics.EVENT_JOINED_MYPLEX_SUCCESSFULLY, params1);
	}
	
	public static void mixPanelMyplexJoinedFailure(String email,String error) {
		Map<String,String> params1=new HashMap<String, String>();
		params1.put(Analytics.EVENT_MYPLEX_SIGNUP_OPTION, Analytics.ACCOUNT_TYPE_MYPLEX);
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
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put(Analytics.USER_ID, email);
		attribs.put(Analytics.REASON_FAILURE, error);
		Analytics.trackEvent(Analytics.EVENT_FORGOT_PASSWORD_FAILED,attribs);  
	}
		
	/*final MixpanelAPI mMixPanel = myplexapplication.getMixPanel();
	mMixPanel.getPeople().identify("556678987");
	mMixPanel.getPeople().checkForSurvey(new SurveyCallbacks() {
	    public void foundSurvey(final Survey s) {
	        if (null != s) {
	           // View view = getActivity().findViewById(android.R.id.content);
	           // mMixPanel.getPeople().showSurvey(s, view);
	        	Log.d("Survey Tag", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	        }
	        else {
	        	Log.d("no Survey Tag", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	        	//Util.showToast(getActivity(), "survey is null",Util.TOAST_TYPE_ERROR);
	        }
	    }
	});*/
}

