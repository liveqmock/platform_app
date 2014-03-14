package com.apalya.myplex.receivers;

import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.google.analytics.tracking.android.CampaignTrackingReceiver;
import com.mixpanel.android.mpmetrics.InstallReferrerReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class CustomCampaignTrackingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if(intent == null || intent.getExtras() == null) return;
		
		String campaign = intent.getStringExtra("referrer");

		if ((!"com.android.vending.INSTALL_REFERRER".equals(intent.getAction()))
				|| (campaign == null)) {
			return;
		}	

		if (TextUtils.isEmpty(campaign)) {
			return;
		}	
		
		InstallReferrerReceiver mixpanelReferrerTracking = new InstallReferrerReceiver();
	    mixpanelReferrerTracking.onReceive(context, intent);
	     
//		SharedPrefUtils.writeToSharedPref(context, "referrer", campaign);

		// Pass along to google
		CampaignTrackingReceiver receiver = new CampaignTrackingReceiver();
		receiver.onReceive(context, intent);

	}

}
