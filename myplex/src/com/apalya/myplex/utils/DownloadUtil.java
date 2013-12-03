package com.apalya.myplex.utils;

import java.util.HashMap;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.myplexapplication;

public class DownloadUtil {

	public static final String TAG = "DownloadUtil";

	public void actionDownloadComplete(Context context, Intent intent) {

		long download_id = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, -1);

		Log.d(TAG, "DownlaodId:" + download_id);

		CardDownloadedDataList downloadlist = null;

		try {

			myplexapplication.getApplicationConfig().downloadCardsPath = context
					.getFilesDir() + "/" + "downloadlist.bin";
			downloadlist = (CardDownloadedDataList) Util
					.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);
		} catch (Exception e) {
			return;
		}

		HashMap<String, CardDownloadData> mDownloadedList = downloadlist.mDownloadedList;

		if (mDownloadedList == null || mDownloadedList.isEmpty()) {
			return;
		}

		CardDownloadData cardDownloadData = null;

		for (String contentId : mDownloadedList.keySet()) {

			cardDownloadData = mDownloadedList.get(contentId);

			if(cardDownloadData == null){
				continue;
			}
			
			if (cardDownloadData.mDownloadId == download_id) {
				
				Log.d(TAG, "download complete for content id :" + contentId);				

				if (ConsumerApi.DEBUGCLIENTKEY == null) {					
					
					String clientKey = SharedPrefUtils.getFromSharedPreference(
							context, context.getString(R.string.devclientkey));
					ConsumerApi.DEBUGCLIENTKEY = clientKey;
				}

				notifyDownloadComplete(contentId);
				
				break;
			}
		}

	}

	public void notifyDownloadComplete(String contentId) {

		String url = ConsumerApi.getDownloadNotifyUrl(contentId);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST, url,
				onlineRequestSuccessListener(), onlineRequestErrorListener());
		myReg.setShouldCache(false);
		queue.add(myReg);
		Log.e(TAG, "requestUrl: " + url);

	}

	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e(TAG, "response: " + response);

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "error response: ");
				if (error != null && error.networkResponse != null) {
					Log.e(TAG, "$$$  " + error.networkResponse.statusCode);
				}

			}
		};
	}
}
