package com.apalya.myplex.utils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexapplication;

public class DeviceRegUtil {
	public static final String TAG = "DeviceRegUtil";
	public static final int ONE = 1;
	public static final int TWO = 2;
	private Context mContext;
	ProgressDialog mProgressDialog;
	
	public DeviceRegUtil(Context context) {
		this.mContext=context;
	}

	public void unregister(){
		String url = ConsumerApi.getUnregisterDevice();
		RequestQueue queue = MyVolley.getRequestQueue();
		if (ConsumerApi.DEBUGCLIENTKEY == null) {					
			
			String clientKey = SharedPrefUtils.getFromSharedPreference(
					mContext, mContext.getString(R.string.devclientkey));
			ConsumerApi.DEBUGCLIENTKEY = clientKey;
		}
		StringRequest myReg = new StringRequest(Method.POST,url, onlineRequestSuccessListener(), onlineRequestErrorListener()){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				HashMap<String, String> params = new HashMap<String, String>();
				 params.put("clientKey", ConsumerApi.DEBUGCLIENTKEY);
				return params;
			}
		};
		myReg.setShouldCache(false);
		queue.add(myReg);
		Analytics.mixPanelDeviceDeRegisterInitiated(ONE);
		mProgressDialog = ProgressDialog.show(mContext,"", "UnRegistering device..", true,false);
		
		Log.e(TAG, "requestUrl: "+url);
		
	}
	
   
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					
					if(TextUtils.isEmpty(response)){
						Util.showToast(mContext, "unregister failed.", Util.TOAST_TYPE_ERROR);
						return;
					}
					
					Log.e(TAG, "response: " + response);
					dismissProgressBar();

					JSONObject jsonResponse = new JSONObject(response);

					
					if (jsonResponse.getString("status").equalsIgnoreCase(
							"SUCCESS"))

					{
						String msg =jsonResponse.getString("message");
						Analytics.mixPanelDeviceDeRegisterInitiated(TWO);
						if(!TextUtils.isEmpty(msg)){
							Util.showToast(mContext, msg, Util.TOAST_TYPE_ERROR);
							LogOutUtil.onClickLogout(mContext);
							return;
						}
						//mixPanelDeviceRegisterInitiated(2);
						
						
					}
					
					Util.showToast(mContext, response, Util.TOAST_TYPE_ERROR);

					
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "error response: ");
				
				String msg="";
				if(error != null && error.networkResponse != null){
					Log.e(TAG, "$$$  "+error.networkResponse.statusCode);	
					msg=""+error.networkResponse.statusCode+":";
				}
				dismissProgressBar();
				Analytics.mixPanelDeviceDeRegisterfailed(error.toString());
				Util.showToast(mContext, msg+error.toString(),Util.TOAST_TYPE_ERROR);
				
			}
		};
	}
	
	public void dismissProgressBar(){
		try {
			if(mProgressDialog != null && mProgressDialog.isShowing()){
				mProgressDialog.dismiss();
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
