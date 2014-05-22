package com.apalya.myplex.utils;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.BaseReponseData;

public class UserProfileUpdateUtil {
	
	public static final String TAG = "UserProfileUpdateUtil";
	private Context mContext;
	
	
	private UserProfileUpdateCallback mListener;
	
	public interface UserProfileUpdateCallback{
		public void onComplete(boolean value, String message);
	}
	
	public UserProfileUpdateUtil(Context context) {
		this.mContext=context;
	}

	
	public void updateProfile(final String msisdn){
		String url = ConsumerApi.getUpdateUserProfile();
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
				 params.put("mobile",msisdn.substring(msisdn.length()-10));
				return params;
			}
		};
		myReg.setShouldCache(false);
		queue.add(myReg);	
		
		Log.e(TAG, "requestUrl: "+url);
		
	}
	
   
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					
					Log.e(TAG, "response: "+response);
					
					if(response == null){
						if(mListener != null){
							mListener.onComplete(false, null);
						}
						return;
					}
					
					BaseReponseData responseData = (BaseReponseData) Util.fromJson(response, BaseReponseData.class);
					
					if(responseData == null || responseData.status == null){
						if(mListener != null){
							mListener.onComplete(false, null);
						}
						return;
					}
					
					if(responseData.status.equalsIgnoreCase("SUCCESS")){
						if(mListener != null){
							mListener.onComplete(true,responseData.message );
						}
						return;
					}
					
					if(mListener != null){
						mListener.onComplete(false,responseData.message);
						return;
					}
					
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				if(mListener != null){
					mListener.onComplete(false,null);					
				}
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "error response: ");
				
				if(error != null && error.networkResponse != null){
					Log.e(TAG, "$$$  "+error.networkResponse.statusCode);	
				}
				
				if(mListener != null){
					mListener.onComplete(false, null);
				}
				
			}
		};
	}

	public void setListener(UserProfileUpdateCallback userProfileUpdateCallback) {
		this.mListener= userProfileUpdateCallback;
		
	}
	
	
}
