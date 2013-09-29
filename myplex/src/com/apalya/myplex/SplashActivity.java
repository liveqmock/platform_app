package com.apalya.myplex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashActivity extends Activity{

	private static final String TAG = "SplashActivity";
	
	private ProgressDialog mProgressDialog = null;
	private DeviceDetails mDevInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();
		setContentView(R.layout.splashscreen);

		/*if(Util.isPhoneRooted())
		{
			myplexUtils.showToast("Application is not supported on Rooted Device");
			finish();
		}
		else*/
		{
			mDevInfo=myplexapplication.getDevDetailsInstance();
			SetDeviceDetails();	

			

			String clientKey=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devclientkey));
			String clientKeyExp=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devclientkeyexp));

			//Check if client is available, if not give device registration request
			if(clientKey!=null)
			{
				//check if the client key is valid or not, if expired give generate key request
				if(isTokenValid(clientKeyExp))
				{
					mDevInfo.setClientKey(clientKey);
					mDevInfo.setClientDeviceId(SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
							getString(R.string.devclientdevid)));
					mDevInfo.setClientKeyExp(SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
							getString(R.string.devclientkeyexp)));
					Log.d(TAG, "---------------------------------------------------------");

					String username=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
							getString(R.string.devusername));
					
					finish();
					//check if user is already logged in, if so take him to main screen or else login screen
					if(username!=null)
					{
						myplexUtils.launchActivity(MainActivity.class,SplashActivity.this , null);
					}
					else
					{
						myplexUtils.launchActivity(LoginActivity.class,SplashActivity.this , null);	
					}	
				}
				else
				{
					//Generatey new Key
					String devId=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
							getString(R.string.devclientdevid));

					Map<String, String> params = new HashMap<String, String>();
					params.put("deviceId", devId);

					genKeyRequest(getString(R.string.genKeyReqPath),params);
				}
			}
			else
			{
				Log.d(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				Map<String, String> params = new HashMap<String, String>();
				params.put("serialNo", mDevInfo.getDeviceId());
				params.put("os", mDevInfo.getDeviceOs());
				params.put("osVersion", mDevInfo.getDeviceOsVer());
				params.put("make",mDevInfo.getDeviceMake());
				params.put("model", mDevInfo.getDeviceModel());
				params.put("resolution", mDevInfo.getDeviceRes());
				params.put("profile", "work");
				params.put("clientSecret", getString(R.string.clientsecret));

				showProgressBar();

				devRegRequest(getString(R.string.devRegPath),params);

			}
		}
	}


	private void genKeyRequest(String contextPath, final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=getString(R.string.url)+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				genKeyRegSuccessListener(),
				genKeyRegErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
	}

	protected ErrorListener genKeyRegErrorListener() {
		return new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {

				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					myplexUtils.showToast(getString(R.string.interneterr));
					finish();
					myplexUtils.launchActivity(MainActivity.class,SplashActivity.this , null);

				}
				else
				{
					myplexUtils.showToast(error.toString());	
				}

				Log.d(TAG, "@@@@@@@@@@@@@@@ LOGIN ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}


	protected Listener<String> genKeyRegSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);

				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "expiresAt: "+jsonResponse.getString("expiresAt"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "clientKey: "+jsonResponse.getString("clientKey"));
						Log.d(TAG, "########################################################");
						mDevInfo.setClientKey(jsonResponse.getString("clientKey"));
						mDevInfo.setClientKeyExp(jsonResponse.getString("expiresAt"));
						Log.d(TAG, "---------------------------------------------------------");



						SharedPrefUtils.writeToSharedPref(SplashActivity.this,
								getString(R.string.devclientkey), jsonResponse.getString("clientKey"));
						SharedPrefUtils.writeToSharedPref(SplashActivity.this,
								getString(R.string.devclientkeyexp), jsonResponse.getString("expiresAt"));

						String username=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
								getString(R.string.devusername));
						
						finish();
						if(username!=null)
						{
							myplexUtils.launchActivity(MainActivity.class,SplashActivity.this , null);
						}
						else
						{
							myplexUtils.launchActivity(LoginActivity.class,SplashActivity.this , null);	
						}	
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						myplexUtils.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	public void showProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(this,"", "Loading...", true,false);
	}
	public void dismissProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
	private boolean isTokenValid(String clientKeyExp) {
		
		//myplexUtils.showToast(clientKeyExp);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(clientKeyExp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date currentDate = new Date();
		if(convertedDate.compareTo(currentDate)>0)
		{
			//myplexUtils.showToast("Valid");
			return true;
		}
		else
		{
			//myplexUtils.showToast("Invalid");
			return false;
		}

	}


	private void devRegRequest(String contextPath, final Map<String, String> bodyParams) {

		RequestQueue queue = MyVolley.getRequestQueue();

		String url=getString(R.string.url)+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				DevRegSuccessListener(),
				DevRegErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
	}


	protected ErrorListener DevRegErrorListener() {
		return new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					myplexUtils.showToast(getString(R.string.interneterr));
					finish();
					myplexUtils.launchActivity(MainActivity.class,SplashActivity.this , null);

				}
				else
				{
					myplexUtils.showToast(error.toString());	
				}

				Log.d(TAG, "@@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}


	protected Listener<String> DevRegSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				dismissProgressBar();
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "expiresAt: "+jsonResponse.getString("expiresAt"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "deviceId: "+jsonResponse.getString("deviceId"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "clientKey: "+jsonResponse.getString("clientKey"));
						Log.d(TAG, "########################################################");
						mDevInfo.setClientKey(jsonResponse.getString("clientKey"));
						mDevInfo.setClientDeviceId(jsonResponse.getString("deviceId"));
						mDevInfo.setClientKeyExp(jsonResponse.getString("expiresAt"));
						Log.d(TAG, "---------------------------------------------------------");



						SharedPrefUtils.writeToSharedPref(SplashActivity.this,
								getString(R.string.devclientkey), jsonResponse.getString("clientKey"));
						SharedPrefUtils.writeToSharedPref(SplashActivity.this,
								getString(R.string.devclientdevid), jsonResponse.getString("deviceId"));
						SharedPrefUtils.writeToSharedPref(SplashActivity.this,
								getString(R.string.devclientkeyexp), jsonResponse.getString("expiresAt"));

						finish();
						myplexUtils.launchActivity(LoginActivity.class,SplashActivity.this , null);
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						myplexUtils.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		return true;
	}
	protected void SetDeviceDetails() {

		Log.d(TAG, "******************************************************************");

		mDevInfo.setDeviceOs(getString(R.string.osname));
		mDevInfo.setDeviceOsVer(android.os.Build.VERSION.RELEASE);
		mDevInfo.setDeviceModel(android.os.Build.DEVICE); 
		mDevInfo.setDeviceMake(android.os.Build.MANUFACTURER);
		mDevInfo.setDeviceSNo(android.os.Build.SERIAL);

		Log.d(TAG, "******************************************************************");

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		Log.d(TAG, String.valueOf(height));
		Log.d(TAG, String.valueOf(width));
		String devRes=String.valueOf(width)+"x"+String.valueOf(height);
		mDevInfo.setDeviceRes(devRes);

		Log.d(TAG, "******************************************************************");


		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mDevInfo.setDeviceId(mTelephonyMgr.getDeviceId());
		mDevInfo.setOperatorName(mTelephonyMgr.getNetworkOperatorName());
		mDevInfo.setMccMnc(String.valueOf(mTelephonyMgr.getSimOperator()));
		mDevInfo.setSimSNo(mTelephonyMgr.getSimSerialNumber());
		mDevInfo.setImsiNo(mTelephonyMgr.getSubscriberId());
		mDevInfo.setSimState(mTelephonyMgr.getSimState());

		Log.d(TAG, "******************************************************************");



	}
	
}
