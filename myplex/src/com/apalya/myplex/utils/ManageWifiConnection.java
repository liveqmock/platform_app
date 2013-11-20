package com.apalya.myplex.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ManageWifiConnection {
	private static String TAG = "ManageWifiConnection";
	private Context mContext;
	private int mAction;
	private static final int WIFICONNECTED = 1;
    private static final int WIFIDISCONNECTED = 2;
    private static final int SWITCHOFFWIFI = 3;
    private static final int SWITCHONWIFI = 4;
    private boolean registered = true;
    private boolean mConnectionChanged = false; 		// addressing if the user is already using the mobile data
	private OnNetworkStateListener listener;
	public interface OnNetworkStateListener{
		public void networkStateChanged();
	}
	public ManageWifiConnection(Context context) {
		this.mContext = context;
		
	}
	public void resumeOldConnection(OnNetworkStateListener listener){
		if(listener == null){return;}
		this.listener = listener;
		Log.e(TAG, "resumeOldConnection");
		if(mConnectionChanged){
			switchonWifi();
		}else{
			sendCallBack();
		}
	}
	public void changeConnection(OnNetworkStateListener listener){
		try {
			Log.e(TAG, "changeConnection");
			if(listener == null){return;}
			this.listener = listener;
			mConnectionChanged = false;
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
			if(wifiManager.isWifiEnabled()){
				mConnectionChanged = true;
			}
			wifiManager.setWifiEnabled(false);
			mAction = SWITCHOFFWIFI;
			registerReceivers();
//			Toast.makeText(mContext, "switching off wifi", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "changeConnection exceprtion"+e.getMessage());
		}
		
	}
	private boolean switchonWifi(){
		try {
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);
			mAction = SWITCHONWIFI;
			registerReceivers();
//			Toast.makeText(mContext, "switching on wifi", Toast.LENGTH_SHORT).show();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
            	
            	ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
				NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();
				if(currentNetworkInfo != null){
					if(currentNetworkInfo.isConnected()){
						if(mAction == SWITCHONWIFI && (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ||currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIMAX)){
							Log.v(TAG,"Wifi connected");
							mConnectionHandler.sendEmptyMessage(WIFICONNECTED);
						}else if(mAction == SWITCHOFFWIFI && !(currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ||currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIMAX)){
							Log.v(TAG,"Wifi disconnected");
							mConnectionHandler.sendEmptyMessage(WIFIDISCONNECTED);
						}
					}	
				}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    };
	public void deRegisterReceivers() {
		registered = false;
    	try {
    		mContext.unregisterReceiver(mConnReceiver); 
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	public void registerReceivers(){
		registered = true;
		try {
    		mContext.registerReceiver(mConnReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendCallBack(){
		Log.e(TAG, "sendCallBack");
		Handler h  = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(listener != null){
					listener.networkStateChanged();
				}
			}
		});
	}
    private Handler mConnectionHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case WIFICONNECTED:{
//					Toast.makeText(mContext, "wifi connected", Toast.LENGTH_SHORT).show();
					if(registered){
						sendCallBack();
					}
					deRegisterReceivers();
					break;
				}
				case WIFIDISCONNECTED:{
//					Toast.makeText(mContext, "wifi disconnected", Toast.LENGTH_SHORT).show();
					if(registered){
						sendCallBack();
					}
					deRegisterReceivers();
					break;
				}
			}
		}
	};

}
