package com.apalya.myplex.receivers;

import android.app.IntentService;
import android.content.Intent;

import com.apalya.myplex.utils.DownloadUtil;

public class DownloadServiceReceiver extends IntentService {

	public DownloadServiceReceiver() {
		super("DownloadServiceReceiver");
	}
	
	public DownloadServiceReceiver(String name) {
		super(name);
		
	}
		

	@Override
	protected void onHandleIntent(Intent intent) {

		new DownloadUtil().actionDownloadComplete(getApplicationContext(), intent);

	}

	
}
