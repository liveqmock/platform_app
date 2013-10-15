package com.apalya.myplex.data;

import android.app.Application;

import com.apalya.myplex.cache.CacheHolder;
import com.apalya.myplex.utils.MyVolley;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class myplexapplication extends Application {
	public static ApplicationConfig mDisplayInfo; 
	public static CardExplorerData mCardExplorerData;
	public static CardData mSelectedCard;
	private static UserProfile mUserProfile;
	private static DeviceDetails mDeviceDetails;
	private static CacheHolder mCache ;
	private static MixpanelAPI mixPanel;
	private static final String MIXPANEL_DISTINCT_ID_NAME = "Mixpanel Example $distinctid";
	/*
	 * You will use a Mixpanel API token to allow your app to send data to Mixpanel. To get your token
	 * - Log in to Mixpanel, and select the project you want to use for this application
	 * - Click the gear icon in the lower left corner of the screen to view the settings dialog
	 * - In the settings dialog, you will see the label "Token", and a string that looks something like this:
	 *
	 *        2ef3c08e8466df98e67ea0cfa1512e9f
	 *
	 *   Paste it below (where you see "YOUR API TOKEN")
	 */
	public static final String MIXPANEL_API_TOKEN = "83c5fe7d8e900e7d1c4f67f84ce41d5a";

	/*
	 * In order for your app to receive push notifications, you will need to enable
	 * the Google Cloud Messaging for Android service in your Google APIs console. To do this:
	 *
	 * - Navigate to https://code.google.com/apis/console
	 * - Select "Services" from the menu on the left side of the screen
	 * - Scroll down until you see the row labeled "Google Cloud Messaging for Android"
	 * - Make sure the switch next to the service name says "On"
	 *
	 * To identify this application with your Google API account, you'll also need your sender id from Google.
	 * You can get yours by logging in to the Google APIs Console at https://code.google.com/apis/console
	 * Once you have logged in, your sender id will appear as part of the URL in your browser's address bar.
	 * The URL will look something like this:
	 *
	 *     https://code.google.com/apis/console/b/0/#project:256660625236
	 *                                                       ^^^^^^^^^^^^
    // *
	 * The twelve-digit number after 'project:' is your sender id. Paste it below (where you see "YOUR SENDER ID")
	 *
	 * There are also some changes you will need to make to your AndroidManifest.xml file to
	 * declare the permissions and receiver capabilities you'll need to get your push notifications working.
	 * You can take a look at this application's AndroidManifest.xml file for an example of what is needed.
	 */
	public static final String ANDROID_PUSH_SENDER_ID = "317019093395";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		init();
		mixPanel = MixpanelAPI.getInstance(this, MIXPANEL_API_TOKEN);
	}
	private void init() {
		MyVolley.init(this);
	}
	public static ApplicationConfig getApplicationConfig(){
		if(mDisplayInfo == null){
			mDisplayInfo = new ApplicationConfig();
		}
		return mDisplayInfo;
	}
	public static MixpanelAPI getMixPanel(){		return mixPanel;	}	/*public static Context getContext(){		return mContext;	}*/	public static UserProfile getUserProfileInstance(){		if(mUserProfile==null)			mUserProfile=new UserProfile();		return mUserProfile;	}	public static DeviceDetails getDevDetailsInstance(){		if(mDeviceDetails==null)			mDeviceDetails=new DeviceDetails();		return mDeviceDetails;	}
	public static CardExplorerData getCardExplorerData(){
		if(mCardExplorerData==null)
			mCardExplorerData=new CardExplorerData();
		return mCardExplorerData;
	}
	public static CacheHolder getCacheHolder(){
		if(mCache == null){
			mCache = new CacheHolder();
		}
		return mCache;
	}
}
