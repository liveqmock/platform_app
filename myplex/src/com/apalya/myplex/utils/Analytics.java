package com.apalya.myplex.utils;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.apalya.myplex.data.myplexapplication;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	public static String loginScreen="Login-Screen-Shown";
	public static String loginFacebook="Login-Facebook";
	public static String loginGoogle="Login-Google";
	public static String loginTwitter="Login-Twitter";
	public static String loginSignIn="Login-SignIn";
	public static String loginSignUp="Login-SignUp";
	public static String loginGuest="Login-Guest";
	public static String cardBrowseScreen="Card-Browse-Screen-Shown";
	public static String cardBrowseMore="Card-Browse-More";
	public static String cardBrowseFilter="Card-Browse-Filter";
	public static String cardBrowseSwipe="Card-Browse-Swipe";
	public static String cardBrowseTouch="Card-Browse-Touch";
	public static String cardBrowseFavorite="Card-Browse-Favorite";
	public static String cardBrowseCancel="Card-Browse-Cancel";
	public static String cardBrowseSelect="Card-Browse-Select";
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
		
		mMixPanel.track(aEventName,getJSON(params));
	}

	/*public static void trackEvent(String aEventName,Map<String, String> params, boolean aStatus) {
		// TODO Auto-generated method stub
		FlurryAgent.logEvent(aEventName,params,aStatus);
	}*/
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

	/*public static void endTimedEvent(String aEventName) {
		// TODO Auto-generated method stub
		FlurryAgent.endTimedEvent(aEventName);
	}*/
}
