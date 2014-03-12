package com.apalya.myplex.data;

import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.cache.CacheHolder;
import com.apalya.myplex.receivers.ConnectivityReceiver;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.LocationUtil;
import com.apalya.myplex.utils.MyVolley;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class myplexapplication extends Application {
	public static ApplicationConfig mDisplayInfo; 
	public static CardExplorerData mCardExplorerData;
	public static CardData mSelectedCard;
	public static CardDownloadedDataList mDownloadList;
	public static ApplicationSettings mApplicationSettings;
	private static UserProfile mUserProfile;
	private static DeviceDetails mDeviceDetails;
	private static CacheHolder mCache ;
	private static MixpanelAPI mixPanel;
	public static int mSelectedOption_Tablet = NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION;
	private static Context context;
	
	private static EasyTracker mTracker;
	

	public static boolean isInitlized=false;

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

	public static LocationUtil locationUtil;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		init();
		initializeGa();
		mixPanel = MixpanelAPI.getInstance(this,getResources().getString(R.string.config_mixpanel_token));
		myplexapplication.context = getApplicationContext();
	}
	
	public static Context getAppContext() {
        return myplexapplication.context;
    }
	
	private void init() {
		ConnectivityReceiver.isConnected=Util.isNetworkAvailable(this);
		MyVolley.init(this);
		locationUtil = LocationUtil.getInstance(this);
		locationUtil.init();
		ApplicationSettings.ENABLE_SENSOR_SCROLL = SharedPrefUtils.getBoolFromSharedPreference(
				getApplicationContext(), getApplicationContext().getString(R.string.isSensorScrollEnabled),
				false);
	}
	public static ApplicationConfig getApplicationConfig(){
		if(mDisplayInfo == null){
			mDisplayInfo = new ApplicationConfig();
		}
		return mDisplayInfo;
	}
	public static ApplicationSettings getApplicationSettings(){
		if(mApplicationSettings == null){
			mApplicationSettings = new ApplicationSettings();
		}
		return mApplicationSettings;
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
	

	 private void initializeGa() {
		mTracker = EasyTracker.getInstance(this);		    
	 }
	 
	 public static EasyTracker getGaTracker() {
	    return mTracker;
	}
	 
	 

	public static void init(Activity activity) {

		isInitlized=true;
		// Log.d(TAG,
		// "******************************************************************");

		getDevDetailsInstance();
		mDeviceDetails.setDeviceOs(activity.getString(R.string.osname));
		mDeviceDetails.setDeviceOsVer(android.os.Build.VERSION.RELEASE);
		mDeviceDetails.setDeviceModel(android.os.Build.DEVICE);
		mDeviceDetails.setDeviceMake(android.os.Build.MANUFACTURER);
		mDeviceDetails.setDeviceSNo(android.os.Build.SERIAL);
		//
		// Log.d(TAG,
		// "******************************************************************");

		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		// Log.d(TAG, String.valueOf(height));
		// Log.d(TAG, String.valueOf(width));
		String devRes = String.valueOf(width) + "x" + String.valueOf(height);
		mDeviceDetails.setDeviceRes(devRes);
		//
		// Log.d(TAG,
		// "******************************************************************");

		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) activity
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyMgr != null) {
			mDeviceDetails.setDeviceId(mTelephonyMgr.getDeviceId());
			mDeviceDetails.setOperatorName(mTelephonyMgr
					.getNetworkOperatorName());
			mDeviceDetails.setMccMnc(String.valueOf(mTelephonyMgr
					.getSimOperator()));
			mDeviceDetails.setSimSNo(mTelephonyMgr.getSimSerialNumber());
			mDeviceDetails.setImsiNo(mTelephonyMgr.getSubscriberId());
			mDeviceDetails.setSimState(mTelephonyMgr.getSimState());
		}
		// Log.d(TAG,
		// "******************************************************************");

		ConsumerApi.DOMAIN = activity.getString(R.string.config_domain_name);

		String clientKey = SharedPrefUtils.getFromSharedPreference(activity,
				activity.getString(R.string.devclientkey));
		if (clientKey != null) {
			ConsumerApi.DEBUGCLIENTKEY = clientKey;
		}
		String clientKeyExp = SharedPrefUtils.getFromSharedPreference(activity,
				activity.getString(R.string.devclientkeyexp));

		// Check if client is available, if not give device registration request
		if (clientKey != null) {
			getUserProfileInstance();
			mUserProfile.firstVisitStatus = false;

			long nowinms = System.currentTimeMillis();
			Date now = new Date(nowinms);
			mUserProfile.lastVisitedDate = now.toLocaleString();

			// clientKeyExp ="2014-01-22T10:36:04+00:00Z";
			// check if the client key is valid or not, if expired give generate
			// key request
			if (Util.isTokenValid(clientKeyExp)) {
				mDeviceDetails.setClientKey(clientKey);
				mDeviceDetails.setClientDeviceId(SharedPrefUtils
						.getFromSharedPreference(activity,
								activity.getString(R.string.devclientdevid)));
				mDeviceDetails.setClientKeyExp(SharedPrefUtils
						.getFromSharedPreference(activity,
								activity.getString(R.string.devclientkeyexp)));
				// Log.d(TAG,
				// "---------------------------------------------------------");

				String username = SharedPrefUtils.getFromSharedPreference(
						activity, activity.getString(R.string.devusername));

				// check if user is already logged in, if so take him to main
				// screen or else login screen
				if (username != null) {
					Crashlytics.setUserEmail(username);

					String userIdSha1 = Util.sha1Hash(username);
					FlurryAgent.setUserId(userIdSha1);
					Crashlytics.setUserName(userIdSha1);
					Crashlytics.setUserIdentifier(userIdSha1);

					String profilename = SharedPrefUtils
							.getFromSharedPreference(activity, activity
									.getString(R.string.userprofilename));
					String profilePic = SharedPrefUtils
							.getFromSharedPreference(activity,
									activity.getString(R.string.userpic));

					if (profilename != null) {
						mUserProfile.setName(profilename);
						// mMixpanel.getPeople().set("$first_name",
						// profilename);
					}
					if (profilePic != null)
						mUserProfile.setProfilePic(profilePic);
					mUserProfile.setUserEmail(username);

				}

			}
		}
	}

}
