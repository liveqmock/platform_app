package com.apalya.myplex.utils;

import com.apalya.myplex.data.myplexapplication;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class Analytics {
	
	private static MixpanelAPI mMixPanel=myplexapplication.getMixPanel();

	public static void trackEvent(String aEventName){
		FlurryAgent.logEvent(aEventName);
		mMixPanel.track(aEventName, null);
	}

	public static void trackEvent(String aEventName, boolean aStatus) {
		// TODO Auto-generated method stub
		FlurryAgent.logEvent(aEventName,aStatus);
	}

	public static void endTimedEvent(String aEventName) {
		// TODO Auto-generated method stub
		FlurryAgent.endTimedEvent(aEventName);
	}
}
