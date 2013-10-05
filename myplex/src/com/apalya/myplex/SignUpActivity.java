package com.apalya.myplex;



import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.flurry.android.FlurryAgent;


public class SignUpActivity extends Activity{

	private Button mSubmit;
	private EditText mEmail,mPhone,mPassword;
	private TextView mUserEmail,mUserPhone,mUserPwd,mUserName;
	private Switch mSmsUpdates,mMailUpdates;
	private static final String TAG = "SignUpActivity";
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;
	private Handler hidehandler=new Handler();
	private ProgressDialog mProgressDialog = null;
	private DeviceDetails mDevInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();

		setContentView(R.layout.signupscreen);

		mDevInfo=myplexapplication.getDevDetailsInstance();

		prepareSlideNotifiation();

		String username;
		String userpwd;

		if(getIntent().getExtras()!=null)
		{
			username=getIntent().getExtras().getString("username");
			userpwd = getIntent().getExtras().getString("userpwd");
		}
		else
		{
			username="";
			userpwd="";
		}

		mSmsUpdates=(Switch)findViewById(R.id.toggleButton1);
		mSmsUpdates.setTypeface(FontUtil.Roboto_Regular);

		mMailUpdates=(Switch)findViewById(R.id.toggleButton2);
		mMailUpdates.setTypeface(FontUtil.Roboto_Regular);

		mUserEmail=(TextView) findViewById(R.id.semailId);
		mUserEmail.setTypeface(FontUtil.Roboto_Regular);
		mUserPhone=(TextView) findViewById(R.id.sphone);
		mUserPhone.setTypeface(FontUtil.Roboto_Regular);
		mUserPwd=(TextView) findViewById(R.id.pwd);
		mUserPwd.setTypeface(FontUtil.Roboto_Regular);
		mUserName=(TextView) findViewById(R.id.fullname);
		mUserName.setTypeface(FontUtil.Roboto_Regular);
		mEmail = (EditText) findViewById(R.id.enterEmail);
		mEmail.setText(username);
		mEmail.setTypeface(FontUtil.Roboto_Regular);



		mPhone = (EditText) findViewById(R.id.enterPhone);
		mPhone.setTypeface(FontUtil.Roboto_Regular);
		mPassword = (EditText) findViewById(R.id.editsPassword);
		mPassword.setText(userpwd);
		mPassword.setTypeface(FontUtil.Roboto_Regular);

		EditText mFullName=(EditText)findViewById(R.id.editFullName);
		mFullName.setTypeface(FontUtil.Roboto_Regular);

		mSubmit = (Button) findViewById(R.id.submit);
		mSubmit.setTypeface(FontUtil.Roboto_Regular);
		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(findViewById(v.getId()), "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				//hideKeypad();
				if((mEmail.getText().toString().length()>0 || mPhone.getText().toString().length()>0)&&mPassword.getText().toString().length()>0) {

					if(mEmail.getText().toString().contains("@") && mEmail.getText().toString().contains(".")&& isPhoneNoValid(mPhone.getText().toString()))
					{
						Map<String, String> params = new HashMap<String, String>();
						params.put("email",mEmail.getText().toString());
						params.put("mobile",mPhone.getText().toString());
						params.put("password", mPassword.getText().toString());
						params.put("password2", mPassword.getText().toString());
						params.put("profile", "work");
						params.put("clientKey",mDevInfo.getClientKey());
						Log.d(TAG, "email-----------: "+mEmail.getText().toString());
						Log.d(TAG, "mobile-----------: "+mPhone.getText().toString());
						Log.d(TAG, "password-----------: "+ mPassword.getText().toString());
						Log.d(TAG, "clientKey-----------: "+mDevInfo.getClientKey());
						showProgressBar();
						RegisterUserReq(getString(R.string.signuppath), params);
						//finish();
						//launchActivity(CardExplorer.class,SignUpActivity.this, null);
					}
					else
					{
						if(!isPhoneNoValid(mPhone.getText().toString()))
						{
							mPhone.setError("Enter Valid Phone Number!");
						}
						else
						{
							mEmail.setError("Enter Valid Email!");
						}
						sendNotification("Enter Valid details");
					}
				}
				else{
					if(mEmail.getText().toString().length()==0 )
					{
						mEmail.setError("Email/Phone No. is required!");

					}
					if( mPhone.getText().toString().length()==0)
					{
						mPhone.setError("Email/Phone No. is required!");
					}
					if(mPassword.getText().toString().length()==0)
					{
						mPassword.setError("Password is required");
					}
					sendNotification("Hey, Missed something, please check!!!");
				}
			}
		});

	}



	public void showSoftKeyboard(View view) {

		InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		Util.launchActivity(LoginActivity.class,SignUpActivity.this, null);
	}

	private boolean isPhoneNoValid(String aPhNo)
	{
		String regexStr = "^[0-9]{10,13}$";

		if(aPhNo.length()<10 || aPhNo.length()>13 ||  aPhNo.matches(regexStr)==false  ) {
			return false;
		}
		else
		{
			return true;
		}
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
	private void RegisterUserReq(String contextPath, final Map<String,String> bodyParams) {

		Analytics.trackEvent("REGISTRATION-REQUEST",true);
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=getString(R.string.url)+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				RegisterUserSuccessListener(),
				RegisterUserErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
	}

	protected ErrorListener RegisterUserErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Analytics.endTimedEvent("REGISTRATION-REQUEST");
				Analytics.trackEvent("REGISTRATION-REQUEST-ERROR");
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Analytics.trackEvent("REGISTER-SCREEN-VIEWED");
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	protected Listener<String> RegisterUserSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				dismissProgressBar();
				Analytics.endTimedEvent("REGISTRATION-REQUEST");
				
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Analytics.trackEvent("REGISTRATION-REQUEST-SUCCESS");
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						finish();
						Util.launchActivity(MainActivity.class,SignUpActivity.this , null);
					}
					else
					{
						Analytics.trackEvent("REGISTRATION-REQUEST-SERVER-ERROR");
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						sendNotification("Err: "+jsonResponse.getString("code")+" "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void prepareSlideNotifiation() {
		mSlideNotificationLayout = (RelativeLayout)findViewById(R.id.slidenotificationlayout);
		mSlideNotificationText = (TextView)findViewById(R.id.slidenotificationtextview);
		mSlideNotifcationHeight = (int) getResources().getDimension(R.dimen.actionbarheight);
		mSlideNotificationLayout.setY(-mSlideNotifcationHeight);
		mSlideNotificationLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				hideNotification();				
			}
		});
	}
	private Runnable hideControllerThread = new Runnable() {

		public void run() {
			Util.animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2,SignUpActivity.this);

		}
	};
	private void showNotification(){
		Util.animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2,SignUpActivity.this);
		hidehandler.removeCallbacks(hideControllerThread);
		hideNotification();
	}
	private void hideNotification(){
		hidehandler.postDelayed(hideControllerThread, 3000);
	}
	private void sendNotification(final String aMsg){
		mSlideNotificationText.setText(aMsg);
		showNotification();
	}
}
