package com.apalya.myplex.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadManagerReceiver extends BroadcastReceiver{
	public static final String TAG = "DownloadManagerReceiver";
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		try {
			Log.d(TAG,"DownloadManagerReceiver");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
