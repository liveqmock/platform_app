package com.apalya.myplex;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;

public class BaseActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener{
	private View mContentView;
	protected PlusClient mPlusClient;
	protected ProgressDialog mConnectionProgressDialog;
	protected ConnectionResult mConnectionResult;
	protected DeviceDetails mDevInfo;
	protected UserProfile mUserInfo= new UserProfile();
	private RequestQueue queue;
	private static final String TAG = "BaseActivity";
	protected static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	protected String mGPlusToken;

	public static final String[] SCOPES = {
		Scopes.PLUS_LOGIN
	};

	public static final String[] VISIBLE_ACTIVITIES = {
		"http://schemas.google.com/AddActivity", "http://schemas.google.com/ReviewActivity"
	};

	protected static final String SCOPE_STRING = "oauth2:" + TextUtils.join(" ", SCOPES);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!FontUtil.isFontsLoaded) {
			FontUtil.loadFonts(getAssets());
		}

		mDevInfo = new DeviceDetails();
		queue = MyVolley.getRequestQueue();


		mPlusClient = new PlusClient.Builder(this, this, this)
		.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
		.build();
		mPlusClient.connect();


		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
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

	public int isInternetAvailable()
	{
		if(isWifiEnabled() && mDevInfo.isSimReady())
		{
			return 0;
		}
		else if(isWifiEnabled())
		{
			return 1;
		}
		else if(mDevInfo.isSimReady())
		{
			return 2;
		}
		else
		{
			return 3;
		}

	}

	public boolean isWifiEnabled()
	{
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()){
			//wifi is enabled
			return true;
		}
		else
		{
			return false;
		}
	}

	protected void animate(float fromX, float toX, final View v,
			final boolean showlist, int animationType) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		if (animationType == 1) {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_X, fromX, toX));
		} else {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromX, toX));
		}
		set.setDuration(getResources().getInteger(
				android.R.integer.config_longAnimTime));
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}
		});
		set.start();
	}
	public void setContentView(View v){
		mContentView = v;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}
	public void showToast(CharSequence aMsg){

		Toast.makeText(BaseActivity.this.getApplicationContext(), 
				aMsg, 
				Toast.LENGTH_LONG).show();
	}

	private void sendRequestDialog() {
		Bundle params = new Bundle();

		params.putString("message", "Learn how to make your Android apps social");
		params.putString("data",
				"{\"badge_of_awesomeness\":\"1\"," +
				"\"social_karma\":\"5\"}");
		WebDialog requestsDialog = (
				new WebDialog.RequestsDialogBuilder(BaseActivity.this,
						Session.getActiveSession(),
						params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								if (error != null) {
									if (error instanceof FacebookOperationCanceledException) {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request cancelled", 
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Network Error", 
												Toast.LENGTH_SHORT).show();
									}
								} else {
									final String requestId = values.getString("request");
									if (requestId != null) {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request sent",  
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request cancelled", 
												Toast.LENGTH_SHORT).show();
									}
								}   
							}

						})
						.build();
		requestsDialog.show();
	}



	

	@Override
	public void onConnected(Bundle connectionHint) {
		//getAndUseAuthTokenInAsyncTask();
	}

	@Override
	protected void onStart() {
		super.onStart();
		//mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mPlusClient!=null)
			mPlusClient.disconnect();

	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
		boolean status = mPlusClient.isConnected();
		Toast.makeText(this, status + " is disconnected.", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mConnectionProgressDialog.isShowing()) {
			// The user clicked the sign-in button already. Start to resolve
			// connection errors. Wait until onConnected() to dismiss the
			// connection dialog.
			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					if(!mPlusClient.isConnected())
						mPlusClient.connect();
				}
			}
		}
		// Save the result and resolve the connection failure upon a user click.
		mConnectionResult = result;
	}
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
			mConnectionResult = null;

			mPlusClient.connect();



		}else
		{
			Session.getActiveSession().onActivityResult(this, requestCode, responseCode, intent);
		}
	}

	public static void launchActivity(
			Class<? extends Activity> nextActivityClass,
			Activity currentActivity, Map<String, String> extrasMap) {
		Intent launchIntent = new Intent(currentActivity, nextActivityClass);
		if (extrasMap != null && extrasMap.size() > 0) {
			Set<String> keys = extrasMap.keySet();
			for (String key : keys) {
				launchIntent.putExtra(key, extrasMap.get(key));
			}
		}
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		currentActivity.startActivity(launchIntent);
	}

	// Example of how to use the GoogleAuthUtil in a blocking, non-main thread context
	void getAndUseAuthTokenBlocking() {
		int MY_ACTIVITYS_AUTH_REQUEST_CODE=200;
		try {
			// Retrieve a token for the given account and scope. It will always return either
			// a non-empty String or throw an exception.
			//final String token = GoogleAuthUtil.getToken(this, mPlusClient.getAccountName(), "oauth2:server:client_id:305644042032-0ebc7fgt75vmaapp2pbqchfjlve6vf42.apps.googleusercontent.com:api_scope:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email");
			Bundle bundle = new Bundle();
			bundle.putString(GoogleAuthUtil.KEY_SUPPRESS_PROGRESS_SCREEN,
					"http://schemas.google.com/AddActivity http://schemas.google.com/BuyActivity");
			final String token = GoogleAuthUtil.getToken(this, mPlusClient.getAccountName(), "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email",bundle);         
			// String token = GoogleAuthUtil.getToken(context, accountName, Scopes.PLUS_LOGIN, bundle);

			//final String token = GoogleAuthUtil.getToken(this, mPlusClient.getAccountName(), "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email");
			Log.d(TAG, "GOOGLE TOKEN<,,,,,,,,, "+token);

			//GoogleAuthUtil.invalidateToken(this, token);
			mGPlusToken=token;
			Log.d(TAG, "GOOGLE TOKEN<,,,,,,,,, "+mGPlusToken);
			Log.d(TAG, "Google User Id:   "+ mPlusClient.getCurrentPerson().getId());
			Map<String, String> params = new HashMap<String, String>();
			params.put("google_id", mPlusClient.getCurrentPerson().getId());
			params.put("auth_token", mGPlusToken);
			params.put("token_expiry", getDateTime());
			params.put("clientKey",mDevInfo.getClientKey());
			//googleLoginReq(getString(R.string.gplusloginpath), params,4);

			// Do work with token.
			/* if (server indicates token is invalid) {
	              // invalidate the token that we found is bad so that GoogleAuthUtil won't
	              // return it next time (it may have cached it)
	              GoogleAuthUtil.invalidateToken(Context, String)(context, token);
	              // consider retrying getAndUseTokenBlocking() once more
	              return;
	          }*/
			return;
		} catch (GooglePlayServicesAvailabilityException playEx) {
			Dialog alert = GooglePlayServicesUtil.getErrorDialog(
					playEx.getConnectionStatusCode(),
					this,
					MY_ACTIVITYS_AUTH_REQUEST_CODE);

		} catch (UserRecoverableAuthException userAuthEx) {
			// Start the user recoverable action using the intent returned by
			// getIntent()
			startActivityForResult(
					userAuthEx.getIntent(),
					MY_ACTIVITYS_AUTH_REQUEST_CODE);
			return;
		} catch (IOException transientEx) {
			// network or server error, the call is expected to succeed if you try again later.
			// Don't attempt to call again immediately - the request is likely to
			// fail, you'll hit quotas or back-off.
			return;
		} catch (GoogleAuthException authEx) {
			// Failure. The call is not expected to ever succeed so it should not be
			// retried.
			return;
		}
	}

	private String getDateTime() { 
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return date.toGMTString();
		//return dateFormat.format(date.toGMTString());
	}

	// Example of how to use AsyncTask to call blocking code on a background thread.
	protected void getAndUseAuthTokenInAsyncTask() {
		AsyncTask task = new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				getAndUseAuthTokenBlocking();
				return null;
			}
		};
		task.execute((Void)null);
	}

	protected boolean signOutRequest(String aUrlPath,final Map<String, String> bodyParams) {

		String url=getString(R.string.url)+aUrlPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
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

	public void onClickLogout() {
		//finish();
		//launchActivity(LoginActivity.class,this , null);

		if(mPlusClient.isConnected() || Session.getActiveSession().isOpened())
		{
			if(mPlusClient.isConnected())
			{
				//				//getGooglePlusToken();
				//getTokenReq.execute((Void)null);


				mPlusClient.clearDefaultAccount();
				mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
					@Override
					public void onAccessRevoked(ConnectionResult status) {
						// mPlusClient is now disconnected and access has been revoked.
						// Trigger app logic to comply with the developer policies
					}
				});
				
				mPlusClient.disconnect();
			}
			else if(Session.getActiveSession().isOpened())
			{
				Session session = Session.getActiveSession();
				if (!session.isClosed()) {
					session.closeAndClearTokenInformation();
					//finish();
					//launchActivity(LoginActivity.class,this , null);
				}
			}

		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("profile","work");
		params.put("clientKey",mDevInfo.getClientKey());
		signOutRequest(getString(R.string.signoutpath), params);
	}
	protected ErrorListener signOutErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	protected Listener<String> signOutSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				Log.d(TAG, "*****************BASE ACTIVITY************************");

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

						SharedPrefUtils.writeToSharedPref(BaseActivity.this,
								getString(R.string.devusername), "");
						SharedPrefUtils.writeToSharedPref(BaseActivity.this,
								getString(R.string.devpassword),"");
						
						mUserInfo.setLoginStatus(false);

						finish();
						launchActivity(LoginActivity.class,BaseActivity.this , null);
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
}
