package com.apalya.myplex.data;

import com.apalya.myplex.utils.MyVolley;
import com.flurry.android.FlurryAgent;

import android.app.Application;
import android.content.Context;

public class myplexapplication extends Application {
	
	private static UserProfile mUserProfile;
	private static DeviceDetails mDeviceDetails;
	private static Context mContext;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = getApplicationContext();
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
		init();
	}
	private void init() {
		MyVolley.init(this);
		
	}
	public static Context getContext(){
		return mContext;
	}
	public static UserProfile getUserProfileInstance(){
		if(mUserProfile==null)
			mUserProfile=new UserProfile();
		return mUserProfile;
	}
	public static DeviceDetails getDevDetailsInstance(){
		if(mDeviceDetails==null)
			mDeviceDetails=new DeviceDetails();
		return mDeviceDetails;
	}
}
