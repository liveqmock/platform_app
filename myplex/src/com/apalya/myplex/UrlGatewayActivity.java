package com.apalya.myplex;

import java.util.List;

import com.apalya.myplex.tablet.MultiPaneActivity;
import com.crashlytics.android.Crashlytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class UrlGatewayActivity extends Activity {

	private static final String TAG = "UrlGatewayActivity";
	private static final String PORTAL_WATCH_MOVIE_URLPATH = "watch-movie";
	private static final String PORTAL_TVSHOW_URLPATH = "tv-show";

	private static enum SchemeType {
		http, myplex
	};

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		try {
			handleExternalUrl();
		} catch (Throwable e) {
			e.printStackTrace();
			Log.d(TAG, "exception in handleExternalUrl "+e);
			Crashlytics.logException(e);
			startMainActivity(null);
			
		}
		
	}

	private void handleExternalUrl(){
		
		Log.d(TAG, "uri:" + getIntent().getData());

		Intent intent = new Intent(this, LoginActivity.class);

		if (getIntent() == null || getIntent().getData() == null) {
			startMainActivity(intent);
			return;
		}

		Uri uri = getIntent().getData();

		if (uri == null || uri.getScheme() == null) {
			startMainActivity(intent);
			return;
		}

		SchemeType scheme = SchemeType.valueOf(uri.getScheme());

		String url = uri.toString();

		if (url == null || !(url.contains(PORTAL_WATCH_MOVIE_URLPATH) || url.contains(PORTAL_TVSHOW_URLPATH))) {
			startMainActivity(intent);
			return;
		}

		List<String> list = uri.getPathSegments();

		switch (scheme) {

		case http:
				// example url : http://www.myplex.com/watch-movie/415/pacific-rim/
			if (list != null && list.size() >= 2) {
				String contentId = list.get(1);
				try {
					Integer.valueOf(contentId); // to handle invalid content id
					Log.d(TAG, "contentId:" + contentId);
					intent.putExtra(getString(R.string._id), contentId);
				} catch (Exception e) {
					Log.d(TAG, "Invalid content id:" + contentId);
					Log.d(TAG, e.getMessage());

				}
			}
			break;

		case myplex:

			// example url : myplex://watch-movie/415/pacific-rim/
			
			if (list != null && list.size() >= 1) {
				String contentId = list.get(0);
				try {
					Integer.valueOf(contentId); // to handle invalid content id
					Log.d(TAG, "contentId:" + contentId);
					intent.putExtra(getString(R.string._id), contentId);
				} catch (Exception e) {
					Log.d(TAG, "Invalid content id:" + contentId);
					Log.d(TAG, e.getMessage());

				}
			}

		}
		startMainActivity(intent);
	}
	
	
	private void startMainActivity(Intent intent) {
		
		if(intent == null){
			intent = new Intent(this, LoginActivity.class);
		}
		
		startActivity(intent);
		finish();

	}

}
