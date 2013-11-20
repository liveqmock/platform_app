package com.apalya.myplex.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.ManageWifiConnection.OnNetworkStateListener;

public class MsisdnRetrivalEngine {
	private Context mContext;
	private static String TAG = "MsisdnRetrivalEngine";
	private String mUrl = "http://www.myplexnow.tv/SamsungBillingHub/MsisdnRetriever";
	MsisdnData mData;
	private static int FETCHINGMSISDN = 1;
	private static int SENDINGCALLBACK = 2;
	private int mState;
	private MsisdnRetrivalEngineListener mListener;
	private ManageWifiConnection mNetworkManager;
	public interface MsisdnRetrivalEngineListener{
		public void onMsisdnData(MsisdnData data);
	}
	public MsisdnRetrivalEngine(Context context){
		this.mContext = context;
		mNetworkManager = new ManageWifiConnection(mContext);
	}
	public void getMsisdnData(MsisdnRetrivalEngineListener listener){
		Log.e(TAG, "getMsisdnData");
		this.mListener = listener;
		String currentImsi = getIMSI();
		mData = (MsisdnData) Util.loadObject(myplexapplication.getApplicationConfig().msisdnPath); 
		if(mData != null){
			Log.e(TAG, "already available");
			sendCallback();
			return;
		}
		// check whether SIM has changed/first launch
		if(mData == null || mData.imsi == null || mData.imsi.length() == 0 ||!mData.imsi.equalsIgnoreCase(currentImsi)){
			Log.e(TAG, "sim has changed");
			mNetworkManager.changeConnection(new OnNetworkStateListener() {
				
				@Override
				public void networkStateChanged() {
					fetchMsisdn();
					
				}
			});
			
		}else{
			sendCallback();
		}
	}
	private String getIMSI(){
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			return mTelephonyMgr.getSubscriberId();
		} catch (Exception e) {
		}
		return null;
	}
	private void fetchMsisdn(){
		Log.e(TAG, "fetchMsisdn");
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
				Log.e(TAG, "successListener "+response);
				mData = parseMsisdn(response);
				if(mData != null){
					mData.imsi = getIMSI();
					Util.saveObject(mData,myplexapplication.getApplicationConfig().msisdnPath); 
				}
				sendCallback();
				Log.d(TAG,"server response "+response);
			}
		};
	}

	private MsisdnData parseMsisdn(String response) {
		MsisdnData data = null;
		try {
			if(response == null){
				return data;
			}
			XmlPullParserFactory xmlfactory = XmlPullParserFactory
					.newInstance();
			XmlPullParser parser = xmlfactory.newPullParser();
			InputStream is = new ByteArrayInputStream(response.getBytes());
			parser.setInput(is, null);
			int et;
			
			while ((et = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (et == XmlPullParser.START_TAG && "billingDetails".equalsIgnoreCase(parser.getName())) {
					data = new MsisdnData();
					data.msisdn = parseStringAttribute(parser,"msisdn");
					data.operator = parseStringAttribute(parser,"operator");
				}
			}
		} catch (Exception e) {
		}
		return data;
	}
	String parseStringAttribute(XmlPullParser parser, String attributeName) {
		String value = parser.getAttributeValue(null, attributeName);
		return (value == null) ? null : value.trim();
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
				mNetworkManager.resumeOldConnection(new OnNetworkStateListener() {
					
					@Override
					public void networkStateChanged() {
						if(mListener != null){
							mListener.onMsisdnData(mData);	
						}
					}
				});
			}
		});
	}
}
