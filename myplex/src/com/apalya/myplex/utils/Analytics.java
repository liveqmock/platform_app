package com.apalya.myplex.utils;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.content.res.Resources;

import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.myplexapplication;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	public static String EVENT_LOGIN = "Login";
	public static String EVENT_SIGNUP = "Signup";
	public static String EVENT_BROWSE = "Browse";
	public static String EVENT_CONTENT = "Content";
	public static String EVENT_SEARCH = "Search";
	public static String EVENT_PLAY = "Play";
	public static String EVENT_PAY = "Pay";
	public static String EVENT_SHARE = "Share";
		
	
	public static String LOGIN_TYPE_PROPERTY = "LoginType";
	public static enum LOGIN_TYPES {FaceBook,Twitter,GooglePlus,myplex,Guest};
	public static String LOGIN_DATE_PROPERTY = "LoginDate";
	public static String LOGIN_EMAIL_PROPERTY = "LoginEmail";
	public static String LOGIN_STATUS_PROPERTY = "LoginStatus";
	public static enum LOGIN_STATUS_TYPES {Success,Failure};
	public static String LOGIN_FORGOT_PASSWORD_PROPERTY = "ForgotPassword";
	
	public static String SIGNUP_TYPE_PROPERTY = "SignupType";
	public static String SIGNUP_DATE_PROPERTY = "SignupDate";
	public static String SIGNUP_EMAIL_PROPERTY = "SignEmail";
	public static enum SIGNUP_TYPES {myplex};
	public static String SIGNUP_STATUS_PROPERTY = "SignupStatus";
	public static enum SIGNUP_STATUS_TYPES {Success,Failure};
	
	public static String BROWSE_TYPE_PROPERTY = "BrowseType";
	public static enum BROWSE_TYPES {Cards,navigation,Filter};
	public static enum BROWSE_CARDACTION_TYPES {Delete,Swipe};
	public static enum BROWSE_NAVIGATION_TYPES {Favourites,Purchases,Downloads,Discover,Settings,Logout,Home,Movies,LiveTv};
	
	public static String CONTENT_ID_PROPERTY = "ContentId";
	public static String CONTENT_NAME_PROPERTY = "ContentName";
	public static String CONTENT_TYPE_PROPERTY = "ContentType";
	public static enum CONTENT_ACTION_TYPES {Favourite,Detailed,PlayTrailer,Share,movie};
	public static enum CONTENT_SHARE_TYPES {Facebook,Google};
	
	public static String SEARCH_TYPE_PROPERTY = "SearchType";
	public static String SEARCH_FILTER_TYPE_PROPERTY = "SearchFilterLabel";
	public static enum SEARCH_TYPES {DropDown,Discover,Filter};
	public static enum DROPDOWN_STATUS_TYPES {Success,Failure};
	public static enum DISCOVER_STATUS_TYPES {tag};
	public static String SEARCH_NUMBER_FOUND_PROPERTY = "NumberOfCardsFound";
	public static String SEARCH_QUERY_PROPERTY = "SearchQuery";
	
	
	public static String PLAY_CONTENT_ID_PROPERTY = "ContentId Playing";
	public static String PLAY_CONTENT_NAME_PROPERTY = "ContentName Playing";
	public static String PLAY_CONTENT_STATUS_PROPERTY = "Content Play Status";
	public static String PLAY_CONTENT_START_TIME_PROPERTY = "Content Start Time";
	public static String PLAY_CONTENT_END_TIME_PROPERTY = "Content End Time";
	public static String PLAY_CONTENT_PAUSE_TIME_PROPERTY = "Content Pause Time";
	public static String PLAY_CONTENT_RESUME_TIME_PROPERTY = "Content Resume Time";
	public static String PLAY_CONTENT_SEEK_TIME_PROPERTY = "Content Seek Time";
	public static enum PLAY_CONTENT_STATUS_TYPES {Start,End,Pause,Resume,Seek,Playing,SeekComplete,PlayerRightsAcquisition};
	
	public static enum PAY_MODEL_TYPES {CreditCard,DebitCard,InternetBanking,OperatorBilling,InAppPurchase};
	public static enum PAY_COMMERCIAL_TYPES {Rental,Buy};
	public static enum PAY_CONTENT_TYPES {SD,HD};
	public static enum PAY_CONTENT_STATUS_TYPES {Success,Failure};
	public static String PAY_STATUS_PROPERTY = "PayStatus";
	public static String PAY_COMMERCIAL_TYPE_PROPERTY =  "PayCommercialType";
	
	/*
	 * use the above variables
	 */
	public static String loginScreen="Login-Screen-Shown";
	public static String loginFacebook="Login-Facebook";
	public static String loginGoogle="Login-Google";
	public static String loginTwitter="Login-Twitter";
	public static String loginSignIn="Login-SignIn";
	public static String loginSignUp="Login-SignUp";
	public static String loginGuest="Login-Guest";
	public static String cardBrowseScreen="Card-Browse-Screen-Shown";
	public static String cardBrowseDuration="Card-Browse-Duration";
	public static String cardBrowseMore="Card-Browse-More";
	public static String cardBrowseFilter="Card-Browse-Filter";
	public static String cardBrowseSwipe="Card-Browse-Swipe";
	public static String cardBrowseTouch="Card-Browse-Touch";
	public static String cardBrowseFavorite="Card-Browse-Favorite";
	public static String cardBrowseCancel="Card-Browse-Cancel";
	public static String cardBrowseSelect="Card-Browse-Select";
	public static String cardBrowsePurchase="Card-Browse-Purchase";
	public static String cardDetailsScreen="Card-Details-Screen-Shown";
	public static String cardDetailsDescription="Card-Details-Screen-Description";
	public static String cardDetailsMultimedia="Card-Details-Screen-Multimedia";
	public static String cardDetailsShare="Card-Details-Screen-Share";
	public static String cardDetailsComment="Card-Details-Screen-Comment";
	public static String cardDetailsReview="Card-Details-Screen-Review";
	public static String cardDetailsRate="Card-Details-Screen-Rate";
	public static String PackagesPricePointList="Packages-Price-Point-List";
	public static String PackagesPurchase="Packages-Purchase";
	public static String PlayerPlaySelect="Player-Play-Select";
	public static String PlayerDownload="Player-Download";
	public static String PlayerBuffering="Player-Buffering";
	public static String PlayerPlayComplete="Player-Play-Complete";
	public static String PlayerRightsAcqusition="Player-Rights-Acqusition";
	public static String SearchScreenShown="Search-Screen-Shown";
	public static String SearchScroll="Search-Sroll";
	public static String SearchQuery="Search-Query";
	public static String SidebarSelect="Sidebar-Select";
	public static String SidebarInvite="Sidebar-Invite";
	
	
	
	
	
	
	private static MixpanelAPI mMixPanel=myplexapplication.getMixPanel();

	/*public static void trackEvent(String aEventName){
		FlurryAgent.logEvent(aEventName);
		mMixPanel.track(aEventName, null);
	}

	public static void trackEvent(String aEventName, boolean aStatus) {
		// TODO Auto-generated method stub
		FlurryAgent.logEvent(aEventName,aStatus);
	}*/
	
	public static void trackEvent(String aEventName,Map<String, String> params){
		
		FlurryAgent.logEvent(aEventName,params);
		
		if(ApplicationSettings.ENABLE_MIXPANEL_API) {
			mMixPanel.track(aEventName,getJSON(params));
		}
	}

	public static void trackEvent(String aEventName,Map<String, String> params, boolean aStatus) {
		// TODO Auto-generated method stub
		FlurryAgent.logEvent(aEventName,params,aStatus);
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

	public static void endTimedEvent(String aEventName) {
		// TODO Auto-generated method stub
		FlurryAgent.endTimedEvent(aEventName);
	}
}
