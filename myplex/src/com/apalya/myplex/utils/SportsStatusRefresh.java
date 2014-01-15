package com.apalya.myplex.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.BaseReponseData;
import com.apalya.myplex.data.MatchStatus;
import com.apalya.myplex.data.Result;
import com.apalya.myplex.data.ResultsData;

public class SportsStatusRefresh {

	public static final String TAG = "SportsContentUtil";
	
	private String contentId;

	private static final int sDefaultInterval = 10*1000;
	private static final int STOP = 1;
	private static final int SHOW_PROGRESS = 2;

	private boolean mShowing;
	private int counter=0;
	private static final int MAX_REFRESH_ALLOWED=50;
	
	public SportsStatusRefresh(String contentId, OnResponseListener onResponseListener) {
		this.contentId=contentId;
		this.onResponseListener=onResponseListener;
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case STOP:
				stop();
				break;
			case SHOW_PROGRESS:				
				if (mShowing) {
					getStatus(contentId, onResponseListener);
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, sDefaultInterval);
				}
				break;
			}
		}
	};
	
    public void stop() {
    	
    	Log.e(TAG, "stop fetching score");
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);               
            } catch (Throwable ex) {
                Log.w("TAG", "already removed "+ex.getMessage());
            }
            mShowing = false;
        }
    }
    
    public void start() {    	
    	 mShowing=true;
         mHandler.sendEmptyMessage(SHOW_PROGRESS);       
    }
	    

	public void setShowing(boolean mShowing) {
		this.mShowing = mShowing;
	}

	public interface OnResponseListener {
		public void response(boolean value, MatchStatus matchStatus);
	}

	private OnResponseListener onResponseListener;

	public void getStatus(String contentId,
			OnResponseListener onResponseListener) {

		counter++;
		
		if(counter >= MAX_REFRESH_ALLOWED ){
			stop();
			return;
		}
		
		Log.e(TAG, "fetching latest score..start");
		this.onResponseListener = onResponseListener;
		String url = ConsumerApi.getMatchStatus(contentId);
//		String url = "https://demostb.s3.amazonaws.com/status.txt";
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.GET, url,
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

					if (response == null) {
						if (onResponseListener != null) {
							onResponseListener.response(false, null);
						}
						return;
					}

					ResultsData responseData = (ResultsData) Util
							.fromJson(response, ResultsData.class);

					if (responseData.code != 200) {
						if (onResponseListener != null) {
							onResponseListener.response(false, null);
						}
						return;
					}
					if (!responseData.status.equalsIgnoreCase("SUCCESS")) {
						if (onResponseListener != null) {
							onResponseListener.response(false, null);
						}
						return;
					}

					if (responseData.results !=null && responseData.results.matchStatus != null) {
						if (onResponseListener != null) {
							onResponseListener.response(true,
									responseData.results.matchStatus);
						}
						return;
					}

				} catch (Exception e) {
					Log.e(TAG, "Exception" + e);
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
				if (onResponseListener != null) {
					onResponseListener.response(false, null);
				}

			}
		};
	}
}
