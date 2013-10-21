package com.apalya.myplex.utils;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.apalya.myplex.data.myplexapplication;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	private String loginScreen="";
	
	
	
	private static MixpanelAPI mMixPanel=myplexapplication.getMixPanel();

	public static void trackEvent(String aEventName){
		FlurryAgent.logEvent(aEventName);
		mMixPanel.track(aEventName, null);
	}

	public static void trackEvent(String aEventName, boolean aStatus) {
		// TODO Auto-generated method stub
		FlurryAgent.logEvent(aEventName,aStatus);
	}
	
	public static void trackEvent(String aEventName,Map<String, String> params){
		FlurryAgent.logEvent(aEventName,params);
		
		mMixPanel.track(aEventName,getJSON(params));
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
					if(params.get(key) == null){
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
