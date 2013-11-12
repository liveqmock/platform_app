package com.apalya.myplex.utils;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.data.myplexapplication;
import com.facebook.Session;
import com.flurry.android.monolithic.sdk.impl.mc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class LogOutUtil {

	private static Context logoutContext;
	private static String TAG="LogOut";
	private static ProgressDialog mProgressDialog = null;
	static final String PREF_KEY_OAUTH_TOKEN= "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET= "oauth_token_secret";

	private static boolean signOutRequest(String aUrlPath,final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReq = new StringRequest(Method.POST,
				aUrlPath,
				signOutSuccessListener(),
				signOutErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
		return true;
	}
	public static void showProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(logoutContext,"", "Loading...", true,false);
	}
	public static void dismissProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
	public static void onClickLogout(Context mContext) {

		//Log.d("BASE ACTIVITY", "@@@@@@@@@@@@@@ LOGOUT ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
		logoutContext=mContext;
		
		Session session = Session.getActiveSession();

		String username=SharedPrefUtils.getFromSharedPreference(mContext,
				mContext.getString(R.string.devusername));
		
		if(username!=null)
		{
			showProgressBar();

			if(session!=null)
			{
				if(session.isOpened())
				session.closeAndClearTokenInformation();

			}
			if(AccountUtils.isAuthenticated(logoutContext))
			{
				AccountUtils.signOut(logoutContext);
			}
			
			twitterlogOut();
			
			Log.d("Main ACTIVITY", "@@@@@@@@@@@@@@ LOGOUT ACTIVITY 3@@@@@@@@@@@@@@@@@@@@@");
			Map<String, String> params = new HashMap<String, String>();
			params.put("profile","work");
			params.put("clientKey",myplexapplication.getDevDetailsInstance().getClientKey());
			signOutRequest(ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+ConsumerApi.SIGN_OUT_ACTION, params);

		}
		
		myplexapplication.getUserProfileInstance().lastVisitedCardData.clear();
		myplexapplication.getUserProfileInstance().joinedDate="NA";
		myplexapplication.getUserProfileInstance().lastVisitedDate="NA";
		
		Util.serializeData(logoutContext);
		
		//Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),logoutContext);
		SharedPrefUtils.writeToSharedPref(logoutContext,
				((Activity) logoutContext).getString(R.string.devusername), "");
		SharedPrefUtils.writeToSharedPref(logoutContext,
				((Activity) logoutContext).getString(R.string.devpassword),"");
		SharedPrefUtils.writeToSharedPref(logoutContext,
				((Activity) logoutContext).getString(R.string.userprofilename), "");
		SharedPrefUtils.writeToSharedPref(logoutContext,
				((Activity) logoutContext).getString(R.string.userpic), "");
		
		myplexapplication.getUserProfileInstance().setLoginStatus(false);
		myplexapplication.getUserProfileInstance().setName("");
		myplexapplication.getUserProfileInstance().setProfilePic("");

		((Activity) logoutContext).finish();
		Util.launchActivity(LoginActivity.class,((Activity) logoutContext) , null);
	}
	public static void twitterlogOut(){
		SharedPreferences prefs = logoutContext.getSharedPreferences("TWITTERTIME", 0);
		String access_token= prefs.getString(PREF_KEY_OAUTH_TOKEN, null);
		String access_token_secret= prefs.getString(PREF_KEY_OAUTH_SECRET, null);
		if(access_token!=null && access_token_secret!=null)
		{
			Editor e= prefs.edit();
			e.putString(PREF_KEY_OAUTH_TOKEN, null);
			e.putString(PREF_KEY_OAUTH_SECRET, null);
			e.commit();
		}
	}
	private static ErrorListener signOutErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Log.d(TAG, "@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Util.showToast(logoutContext,"Code: "+error.toString(),Util.TOAST_TYPE_ERROR);
//				Util.showToast("Code: "+error.toString(),logoutContext);
				Log.d(TAG, "@@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	private static Listener<String> signOutSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				Log.d(TAG, "*****************BASE ACTIVITY************************");
				dismissProgressBar();
				JSONObject jsonResponse;
				try {
					jsonResponse = new JSONObject(response);
					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Util.showToast(logoutContext,"Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),Util.TOAST_TYPE_ERROR);
						
						if(jsonResponse.getString("code").equalsIgnoreCase("401")){
							String devId=SharedPrefUtils.getFromSharedPreference(logoutContext,
									((Activity) logoutContext).getString(R.string.devclientdevid));

							Map<String, String> params = new HashMap<String, String>();
							params.put("deviceId", devId);

							Util.genKeyRequest(logoutContext,((Activity) logoutContext).getString(R.string.genKeyReqPath),params);
						}
//						Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),logoutContext);
					}
				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

}
