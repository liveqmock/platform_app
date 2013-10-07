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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class LogOutUtil {

	private static Context logoutContext;
	private static String TAG="LogOut";
	private static ProgressDialog mProgressDialog = null;

	private static boolean signOutRequest(String aUrlPath,final Map<String, String> bodyParams) {
		Analytics.trackEvent("SIGN-OUT-REQUEST",true);

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
		showProgressBar();

		Session session = Session.getActiveSession();

		if(AccountUtils.isAuthenticated(logoutContext) || session!=null)
		{
			if(session.isOpened())
			{
				Analytics.trackEvent("FACEBOOK-SIGN-OUT-SELECTED");
				session.closeAndClearTokenInformation();

			}
			else if(AccountUtils.isAuthenticated(logoutContext))
			{
				Analytics.trackEvent("GOOGLE-SIGN-OUT-SELECTED");
				AccountUtils.signOut(logoutContext);
			}

		}
		Analytics.trackEvent("SIGN-OUT-SELECTED");
		Log.d("Main ACTIVITY", "@@@@@@@@@@@@@@ LOGOUT ACTIVITY 3@@@@@@@@@@@@@@@@@@@@@");
		Map<String, String> params = new HashMap<String, String>();
		params.put("profile","work");
		params.put("clientKey",myplexapplication.getDevDetailsInstance().getClientKey());
		signOutRequest(ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+ConsumerApi.SIGN_OUT_ACTION, params);
	}
	private static ErrorListener signOutErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Analytics.endTimedEvent("SIGN-OUT-REQUEST");
				Analytics.trackEvent("SIGN-OUT-REQUEST-ERROR");
				Log.d(TAG, "@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Util.showToast("Code: "+error.toString(),logoutContext);
				Log.d(TAG, "@@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	private static Listener<String> signOutSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Analytics.endTimedEvent("SIGN-OUT-REQUEST");
				Log.d(TAG,"Response: "+response);
				Log.d(TAG, "*****************BASE ACTIVITY************************");
				dismissProgressBar();
				JSONObject jsonResponse;
				try {
					jsonResponse = new JSONObject(response);
					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Analytics.trackEvent("SIGN-OUT-REQUEST-SUCESS");
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						
						//Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),logoutContext);
						SharedPrefUtils.writeToSharedPref(logoutContext,
								((Activity) logoutContext).getString(R.string.devusername), "");
						SharedPrefUtils.writeToSharedPref(logoutContext,
								((Activity) logoutContext).getString(R.string.devpassword),"");

						myplexapplication.getUserProfileInstance().setLoginStatus(false);
						myplexapplication.getUserProfileInstance().setName("");
						myplexapplication.getUserProfileInstance().setProfilePic("");

						((Activity) logoutContext).finish();
						Util.launchActivity(LoginActivity.class,((Activity) logoutContext) , null);
					}
					else
					{
						Analytics.trackEvent("SIGN-OUT-REQUEST-SERVER-ERROR");
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),logoutContext);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

}
