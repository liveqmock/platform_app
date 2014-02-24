package com.apalya.myplex.receivers;

import com.apalya.myplex.utils.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

	public static final String TAG = "ConnectivityReceiver";

	public static boolean isConnected = true;

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			
			boolean isConnected = Util.isNetworkAvailable(context);

			Log.i(TAG, "connected :" + isConnected);
		} catch (Throwable e) {			
			e.printStackTrace();
		}

	}
}
