package com.apalya.myplex.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.myplexapplication;
import com.fasterxml.jackson.core.JsonParseException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MsisdnRetrivalEngine {
	private Context mContext;
	private static String TAG = "MsisdnRetrivalEngine";
	private String mUrl = "http://www.myplexnow.tv/SamsungBillingHub/MsisdnRetriever";
	MsisdnData mData;
	private MsisdnRetrivalEngineListener mListener;
	public interface MsisdnRetrivalEngineListener{
		public void onMsisdnData(MsisdnData data);
	}
	public MsisdnRetrivalEngine(Context context){
		this.mContext = context;
	}
	public void getMsisdnData(MsisdnRetrivalEngineListener listener){
		this.mListener = listener;
		String currentImsi = new String();
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = mTelephonyMgr.getSubscriberId();
		} catch (Exception e) {
			// TODO: handle exception
		}
		mData = (MsisdnData) Util.loadObject(myplexapplication.getApplicationConfig().msisdnPath); 
		if(mData == null){
			sendCallback();
			return;
		}
		// check whether SIM has changed/first launch
		if(mData.imsi == null || mData.imsi.length() == 0 ||!mData.imsi.equalsIgnoreCase(currentImsi)){
			fetchMsisdn();
		}else{
			sendCallback();
		}
	}
	private void fetchMsisdn(){
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(mUrl, successListener(), errorListener());
		myReg.setShouldCache(false);
		Log.d(TAG,"Min Request:"+mUrl);
		queue.add(myReg);
	}
	private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"server response "+response);
			}
		};
	}

	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG,"Error from server "+error.networkResponse);
				sendCallback();
			}
		};
	}
	private void sendCallback(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(mListener != null){
					mListener.onMsisdnData(mData);	
				}
			}
		});
	}
}
