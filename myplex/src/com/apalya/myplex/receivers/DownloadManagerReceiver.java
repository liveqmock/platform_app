package com.apalya.myplex.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadManagerReceiver extends BroadcastReceiver {
	
	public static final String TAG = "DownloadManagerReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		try {

			Log.d(TAG, "DownloadManagerReceiver" + intent);
			Log.d(TAG, "DownloadManagerReceiver" + intent.getExtras());

			String action = intent.getAction();	
		

			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

				
				try {

					intent.setClass(context, DownloadServiceReceiver.class);
					
					context.startService(intent);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
