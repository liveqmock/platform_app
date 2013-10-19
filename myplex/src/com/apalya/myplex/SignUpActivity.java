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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

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
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.flurry.android.FlurryAgent;


public class SignUpActivity extends Activity{

	private Button mSubmit;
	private EditText mEmail,/*mPhone,*/mPassword;
	//private TextView mUserEmail,mUserPhone,mUserPwd,mUserName;
	//private Switch mSmsUpdates,mMailUpdates;
	private static final String TAG = "SignUpActivity";
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText,mFpwd,mTnc;
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
		
		//String login;

		if(getIntent().getExtras()!=null)
		{
			//login=getIntent().getExtras().getString("login");
			RelativeLayout loginLayout=(RelativeLayout)findViewById(R.id.login);
			loginLayout.setVisibility(View.VISIBLE);
			RelativeLayout signupLayout=(RelativeLayout)findViewById(R.id.signup);
			signupLayout.setVisibility(View.GONE);

			mFpwd=(TextView)findViewById(R.id.fpwd);
			mTnc=(TextView)findViewById(R.id.tnc);

			mFpwd.setVisibility(View.VISIBLE);
			mTnc.setVisibility(View.GONE);

			mEmail = (EditText) findViewById(R.id.loginEmail);
			mEmail.setTypeface(FontUtil.Roboto_Regular);
			mPassword = (EditText) findViewById(R.id.loginPassword);
			mPassword.setTypeface(FontUtil.Roboto_Regular);
			mSubmit = (Button) findViewById(R.id.loginsubmit);
			mSubmit.setTypeface(FontUtil.Roboto_Regular);
			mSubmit.setText("Sign into myplex");
			final View line=(View)findViewById(R.id.loginsp1);
			mFpwd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mPassword.getVisibility()==View.VISIBLE)
					{
					mPassword.setVisibility(View.GONE);
					line.setVisibility(View.GONE);
					mFpwd.setText("sign into myplex");
					mSubmit.setText("Submit");
					}
					else
					{
						mPassword.setVisibility(View.VISIBLE);
						line.setVisibility(View.VISIBLE);
						mFpwd.setText("Forgot password?");
						mSubmit.setText("sign into myplex");	
					}
				}
			});


			
			mSubmit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mSubmit, "alpha", 0.5f, 1f);
					fadeAnim2.setDuration(800);
					fadeAnim2.start();
					

					Analytics.trackEvent("USER-LOGIN-SELECTED");
					if(mEmail.getText().toString().length() > 0 &&  mPassword.getText().toString().length()>0)
					{
						if(mEmail.getText().toString().contains("@")&& mEmail.getText().toString().contains("."))
						{
							Map<String, String> params = new HashMap<String, String>();
							params.put("userid", mEmail.getText().toString());
							params.put("password", mPassword.getText().toString());
							params.put("profile", "work");
							params.put("clientKey",mDevInfo.getClientKey());
							Log.d(TAG, "clientKey-----------: "+mDevInfo.getClientKey());
							Analytics.trackEvent("USER-LOGIN-REQUEST",true);
							showProgressBar();
							userLoginRequest(getString(R.string.signin), params);
							//finish();
							//Util.launchActivity(CardExplorer.class,LoginActivity.this , null);
						}
						else
						{
							mEmail.setError( "Enter Valid Email!" );
							sendNotification("Hey, you might have entered wrong mail id!");

						}

					}
					else
					{
						if( mEmail.getText().toString().length() == 0 )
						{
							mEmail.setError( "Username is required!" );
						}
						if( mPassword.getText().toString().length() == 0 )
						{
							mPassword.setError( "Password is required!" );
						}
						sendNotification("Username and password are required");
					}
				}
			});

		}
		else
		{

			RelativeLayout loginLayout=(RelativeLayout)findViewById(R.id.login);
			loginLayout.setVisibility(View.GONE);
			RelativeLayout signupLayout=(RelativeLayout)findViewById(R.id.signup);
			signupLayout.setVisibility(View.VISIBLE);

			mFpwd=(TextView)findViewById(R.id.fpwd);
			mTnc=(TextView)findViewById(R.id.tnc);

			mFpwd.setVisibility(View.GONE);
			mTnc.setVisibility(View.VISIBLE);

			mEmail = (EditText) findViewById(R.id.editEmail);
			mEmail.setTypeface(FontUtil.Roboto_Regular);
			mPassword = (EditText) findViewById(R.id.editPassword);
			mPassword.setTypeface(FontUtil.Roboto_Regular);
			mSubmit = (Button) findViewById(R.id.signsubmit);
			mSubmit.setTypeface(FontUtil.Roboto_Regular);
			mSubmit.setText("Create Account");
			mSubmit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(findViewById(v.getId()), "alpha", 0.5f, 1f);
					fadeAnim2.setDuration(800);
					fadeAnim2.start();
					//hideKeypad();
					if(mEmail.getText().toString().length()>0 &&mPassword.getText().toString().length()>0) {

						if(mEmail.getText().toString().contains("@") && mEmail.getText().toString().contains("."))
						{
							Map<String, String> params = new HashMap<String, String>();
							params.put("email",mEmail.getText().toString());
							params.put("password", mPassword.getText().toString());
							params.put("password2", mPassword.getText().toString());
							params.put("profile", "work");
							params.put("clientKey",mDevInfo.getClientKey());
							Log.d(TAG, "email-----------: "+mEmail.getText().toString());
							Log.d(TAG, "password-----------: "+ mPassword.getText().toString());
							Log.d(TAG, "clientKey-----------: "+mDevInfo.getClientKey());
							showProgressBar();
							RegisterUserReq(getString(R.string.signuppath), params);
							//finish();
							//launchActivity(CardExplorer.class,SignUpActivity.this, null);
						}
						else
						{

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

						if(mPassword.getText().toString().length()==0)
						{
							mPassword.setError("Password is required");
						}
						sendNotification("Hey, Missed something, please check!!!");
					}
				}
			});

		}
		
		ImageView img1= (ImageView)findViewById(R.id.imageView1);
		ImageView img2= (ImageView)findViewById(R.id.imageView2);
		ImageView img3= (ImageView)findViewById(R.id.imageView3);
		ImageView img4= (ImageView)findViewById(R.id.imageView4);
		ImageView img5= (ImageView)findViewById(R.id.imageView5);
		ImageView img6= (ImageView)findViewById(R.id.imageView6);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		img1.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image1, width, height));
		img2.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image2, width, height));
		img3.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image3, width, height));
		img4.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image4, width, height));
		img5.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image5, width, height));
		img6.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(),R.drawable.image6, width, height));
		
		img1.setScaleType(ScaleType.FIT_XY);
		img2.setScaleType(ScaleType.FIT_XY);
		img3.setScaleType(ScaleType.FIT_XY);
		img4.setScaleType(ScaleType.FIT_XY);
		img5.setScaleType(ScaleType.FIT_XY);
		img6.setScaleType(ScaleType.FIT_XY);
		
		
		final HorizontalScrollView parentScrollView= (HorizontalScrollView) findViewById(R.id.parentScrollview);
		parentScrollView.setOnTouchListener(new View.OnTouchListener() {

	           
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				//parentScrollView.requestDisallowInterceptTouchEvent(false);
				return true;
			}
            });

		ViewTreeObserver vto = parentScrollView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		    	parentScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		    	int scrollWidth=parentScrollView.getChildAt(0).getMeasuredWidth()-getWindowManager().getDefaultDisplay().getWidth();
		    	//Util.showToast(Integer.toString(scrollWidth),SignUpActivity.this);
		        LinearLayout backgroundScrollLayout= (LinearLayout)findViewById(R.id.llayout);
		        backgroundScrollLayout.clearAnimation();
		        TranslateAnimation translateAnim = new TranslateAnimation(-scrollWidth,0, 0, 0);  
		        translateAnim.setDuration(80000);   
		        translateAnim.setRepeatCount(8);
		        translateAnim.setRepeatMode(2);
		        translateAnim.setFillEnabled(true);
		        backgroundScrollLayout.startAnimation(translateAnim);

		    }
		});

		/*EditText mFullName=(EditText)findViewById(R.id.editFullName);
		mFullName.setTypeface(FontUtil.Roboto_Regular);*/


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

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
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
						SharedPrefUtils.writeToSharedPref(SignUpActivity.this,
								getString(R.string.devusername), mEmail.getText().toString());
						SharedPrefUtils.writeToSharedPref(SignUpActivity.this,
								getString(R.string.devpassword), mPassword.getText().toString());
						finish();
						Util.launchMainActivity(SignUpActivity.this);
//						Util.launchActivity(MainActivity.class,SignUpActivity.this , null);
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
	protected void userLoginRequest(String contextPath, final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				userLoginSuccessListener(),
				userLoginErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
		
	}
	protected ErrorListener userLoginErrorListener() {
		dismissProgressBar();
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Analytics.endTimedEvent("USER-LOGIN-REQUEST");
				Analytics.trackEvent("USER-LOGIN-REQUEST-ERROR");
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					sendNotification(getString(R.string.interneterr));
					finish();
					Util.launchMainActivity(SignUpActivity.this);
//					Util.launchActivity(MainActivity.class,SignUpActivity.this , null);

				}
				else
				{
					sendNotification(error.toString());	
				}
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> userLoginSuccessListener() {
		dismissProgressBar();
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Analytics.endTimedEvent("USER-LOGIN-REQUEST");

				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Analytics.trackEvent("USER-LOGIN-REQUEST-SUCCESS");
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						SharedPrefUtils.writeToSharedPref(SignUpActivity.this,
								getString(R.string.devusername), mEmail.getText().toString());
						SharedPrefUtils.writeToSharedPref(SignUpActivity.this,
								getString(R.string.devpassword), mPassword.getText().toString());

						finish();
						Util.launchMainActivity(SignUpActivity.this);
//						Util.launchActivity(MainActivity.class,SignUpActivity.this , null);
					}
					else
					{
						Analytics.trackEvent("USER-LOGIN-REQUEST-SERVER-ERROR");
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						sendNotification("Err: "+jsonResponse.getString("code")+" \nErr Msg: "+jsonResponse.getString("message"));
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
