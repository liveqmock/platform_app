package com.apalya.myplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.util.LangUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;

import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashActivity extends BaseActivity{

	private static final String TAG = "SplashActivity";
	private ProgressBar loader;
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().hide();
		setContentView(R.layout.splashscreen);

		prepareSlideNotifiation();
		SetDeviceDetails();	
		
		
		loader = (ProgressBar) findViewById(R.id.loading);
		
		
		String clientKey=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
				getString(R.string.devclientkey));
		if(clientKey!=null)
		{
			mDevInfo.setClientKey(clientKey);
			mDevInfo.setClientDeviceId(SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devclientdevid)));
			mDevInfo.setClientKeyExp(SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devclientkeyexp)));
			Log.d(TAG, "---------------------------------------------------------");
			
			String username=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devusername));
			String pwd=SharedPrefUtils.getFromSharedPreference(SplashActivity.this,
					getString(R.string.devpassword));
	
			finish();
			if(username!=null)
			{
				launchActivity(MainActivity.class,SplashActivity.this , null);
			}
			else
			{
				launchActivity(LoginActivity.class,SplashActivity.this , null);	
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
			
			loader.setVisibility(View.VISIBLE);
			
			devRegRequest(getString(R.string.devRegPath),params);
				
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


	private void prepareSlideNotifiation() {
		mSlideNotificationLayout = (RelativeLayout)findViewById(R.id.slidenotificationlayout);
		mSlideNotificationText = (TextView)findViewById(R.id.slidenotificationtextview);
		mSlideNotifcationHeight = (int) getResources().getDimension(R.dimen.slidenotification);
		mSlideNotificationLayout.setY(-mSlideNotifcationHeight);
		mSlideNotificationLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				hideNotification();				
			}
		});
	}
	private void showNotification(){
		animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2);
		
		
	}
	private void hideNotification(){
		animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2);
		
		finish();
		launchActivity(MainActivity.class, SplashActivity.this, null);
	}
	private void sendNotification(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			public void run() {
				mSlideNotificationText.setText(getString(R.string.interneterr));
				showNotification();
			}
		});
	}
	
	protected ErrorListener DevRegErrorListener() {
		return new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				loader.setVisibility(View.GONE);
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					showToast(getString(R.string.interneterr));
					finish();
					launchActivity(MainActivity.class,SplashActivity.this , null);
					
				}
				else
				{
					showToast(error.toString());	
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
				loader.setVisibility(View.GONE);
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
							launchActivity(LoginActivity.class,SplashActivity.this , null);
						}
						else
						{
							Log.d(TAG, "code: "+jsonResponse.getString("code"));
							Log.d(TAG, "message: "+jsonResponse.getString("message"));
							showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"));
						}
					} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			};
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		//super.onCreateOptionsMenu(menu);   

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {

		return true;
	}


}
