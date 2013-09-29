package com.apalya.myplex;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.AccountUtils;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;


public class LoginActivity extends Activity implements OnClickListener, AccountUtils.AuthenticateCallback, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, PlusClient.OnPersonLoadedListener {

	private EditText mEmailField;
	private EditText mPwdField;
	private Button mFacebookButton,mSignup,mLogin,mGoogleLogin;
	private TextView mLetMeIn,mText,mForgetMsg;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private static final String TAG = "LoginActivity";
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;
	private View mRootLayout;
	protected String fbUserId;
	//private TextToSpeech mTts;
	private Handler hidehandler=new Handler();
	private ProgressDialog mProgressDialog = null;
	private AsyncTask<Object, Void, String> mAuthTask;
	private int exceptionCount=0;

	public static final String EXTRA_FINISH_INTENT
	= "com.google.android.iosched.extra.FINISH_INTENT";


	private static final String KEY_CHOSEN_ACCOUNT = "chosen_account";

	private static final int REQUEST_AUTHENTICATE = 100;
	private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
	private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
	private static final int REQUEST_PLAY_SERVICES_ERROR_DIALOG = 103;


	private Account mChosenAccount;
	private boolean mCancelAuth = false;
	private boolean mAuthInProgress = false;
	private boolean mAuthProgressFragmentResumed = false;
	private boolean mCanRemoveAuthProgressFragment = false;
	private PlusClient mPlusClient;
	private DeviceDetails mDevInfo;
	private UserProfile mUserInfo;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();
		setContentView(R.layout.loginscreen);

		prepareSlideNotifiation();
		
		mDevInfo=myplexapplication.getDevDetailsInstance();
		mUserInfo=myplexapplication.getUserProfileInstance();
		//mUserFields = (LinearLayout)findViewById(R.id.userfields);
		TextView pwdName= (TextView) findViewById(R.id.passwordField);
		pwdName.setTypeface(FontUtil.Roboto_Regular);
		TextView loginName= (TextView) findViewById(R.id.loginname);
		loginName.setTypeface(FontUtil.Roboto_Regular);
		//mOptionFields = (LinearLayout) findViewById(R.id.linearLayout1);
		//mTitleIcon = (ImageView) findViewById(R.id.logo);
		mEmailField = (EditText)findViewById(R.id.editEmail);
		mGoogleLogin = (Button)findViewById(R.id.googlelogin);
		mGoogleLogin.setOnClickListener(this);
		mGoogleLogin.setTypeface(FontUtil.Roboto_Regular);

		mFacebookButton = (Button) findViewById(R.id.fblogin);
		mFacebookButton.setOnClickListener(this);
		mFacebookButton.setTypeface(FontUtil.Roboto_Regular);

		mSignup = (Button) findViewById(R.id.signup);
		mSignup.setTypeface(FontUtil.Roboto_Regular);

		mText=(TextView)findViewById(R.id.Msg);
		mText.setTypeface(FontUtil.Roboto_Regular);
		mForgetMsg = (TextView)findViewById(R.id.forgetPwdMsg);
		mForgetMsg.setTypeface(FontUtil.Roboto_Regular);
		mForgetMsg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mForgetMsg, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				hideKeypad();
				if( mEmailField.getText().toString().length() == 0 )
					mEmailField.setError( "Username is required!" );


				if(mEmailField.getText().toString().length() > 0)
				{
					if(mEmailField.getText().toString().contains("@")&& mEmailField.getText().toString().contains("."))
					{
						/*Map<String, String> params = new HashMap<String, String>();
						params.put("userid", mEmailField.getText().toString());
						params.put("clientKey",mDevInfo.getClientKey());
						mRequestType=2;
						Log.d(TAG, "clientKey-----------: "+mDevInfo.getClientKey());
						sendApiRequest(getString(R.string.signin), params);*/
						//finish();
						//myplexUtils.launchActivity(CardExplorer.class,LoginActivity.this , null);
					}
					else
					{
						sendNotification("Email id might be invalid, Please check");
						mEmailField.setError( "Enter Valid Email!" );

					}

				}


			}
		});

		mLetMeIn=(TextView)findViewById(R.id.letmeinMsg);
		mLetMeIn.setTypeface(FontUtil.Roboto_Regular);

		mPwdField =(EditText) findViewById(R.id.editPassword);

		mLetMeIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLetMeIn, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				finish();
				myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

			}
		});

		mPwdField.setTypeface(FontUtil.Roboto_Regular);
		mEmailField.setTypeface(FontUtil.Roboto_Regular);
		mLogin = (Button) findViewById(R.id.login);
		mLogin.setTypeface(FontUtil.Roboto_Regular);
		mLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLogin, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				hideKeypad();


				if(mEmailField.getText().toString().length() > 0 &&  mPwdField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@")&& mEmailField.getText().toString().contains("."))
					{
						Map<String, String> params = new HashMap<String, String>();
						params.put("userid", mEmailField.getText().toString());
						params.put("password", mPwdField.getText().toString());
						params.put("profile", "work");
						params.put("clientKey",mDevInfo.getClientKey());
						Log.d(TAG, "clientKey-----------: "+mDevInfo.getClientKey());
						showProgressBar();
						userLoginRequest(getString(R.string.signin), params);
						//finish();
						//myplexUtils.launchActivity(CardExplorer.class,LoginActivity.this , null);
					}
					else
					{
						mEmailField.setError( "Enter Valid Email!" );
						sendNotification("Hey, you might have entered wrong mail id!");

					}

				}
				else
				{
					if( mEmailField.getText().toString().length() == 0 )
					{
						mEmailField.setError( "Username is required!" );
					}
					if( mPwdField.getText().toString().length() == 0 )
					{
						mPwdField.setError( "Password is required!" );
					}
					sendNotification("Username and password are required");
				}
			}
		});

		mSignup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mSignup, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();

				if(mEmailField.getText().toString().length() > 0 &&  mPwdField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@"))
					{

						Map<String, String> intentBundle = new HashMap<String, String>();
						String intentParam1 = "username";
						String intentParam2 = "userpwd";

						intentBundle.put(intentParam1,mEmailField.getText().toString() );
						intentBundle.put(intentParam2,mPwdField.getText().toString());

						finish();
						myplexUtils.launchActivity(SignUpActivity.class,LoginActivity.this , intentBundle);

						/*Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
						startActivity(intent);*/	
					}
					else
					{
						finish();
						myplexUtils.launchActivity(SignUpActivity.class,LoginActivity.this , null);
					}

				}
				else
				{
					finish();
					myplexUtils.launchActivity(SignUpActivity.class,LoginActivity.this , null);	
				}
				/*Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivity(intent);*/
			}
		});

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				//session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
				if (!session.isOpened() && !session.isClosed()) {
					session.openForRead(new Session.OpenRequest(this)
					.setPermissions(Arrays.asList("basic_info","email","read_friendlists"))
					.setCallback(statusCallback));
				} else {
					Session.openActiveSession(this, true, statusCallback);
				}
			}

		}
		else
		{
			if(session.isOpened())
			{
				//showToast("Already Logged In");
				finish();
				myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);
			}

		}
		
		 mPlusClient = (new PlusClient.Builder(this, this, this))
                 .setScopes(AccountUtils.AUTH_SCOPES)
                 .build();

		//Check if Keyboard is visible or not
		mRootLayout = findViewById(R.id.rootlayout);  

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

	protected void userLoginRequest(String contextPath, final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=getString(R.string.url)+contextPath;
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
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					sendNotification(getString(R.string.interneterr));
					finish();
					myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

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
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devusername), mEmailField.getText().toString());
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devpassword), mPwdField.getText().toString());

						finish();
						myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);
					}
					else
					{
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


	public void showSoftKeyboard(View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	public boolean isKeyboardVisible()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if (imm.isAcceptingText()) {
			return true;
		} else {
			return false;
		}
	}

	private Runnable hideControllerThread = new Runnable() {

		public void run() {
			myplexUtils.animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2);

		}
	};

	private void showNotification(){
		myplexUtils.animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2);
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


	private void hideKeypad() {
		InputMethodManager imm = (InputMethodManager) 
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mRootLayout.getWindowToken(), 0);
	}

	/*@Override
	public void onConnected(Bundle connectionHint) {

		//super.onConnected(connectionHint);
		if (mConnectionProgressDialog.isShowing()) 
		{
			mConnectionProgressDialog.dismiss();

		}

		// Retrieve the oAuth 2.0 access token.
				final Context context = this.getApplicationContext();
				AsyncTask task = new AsyncTask() {
					@Override
					protected Object doInBackground(Object... params) {
						String scope = "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email";
						try {
							// We can retrieve the token to check via
							// tokeninfo or to pass to a service-side
							// application.
							String token = GoogleAuthUtil.getToken(context,
									mPlusClient.getAccountName(), scope);
							mGPlusToken=token;
							Log.d(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+token);
							Log.d(TAG, "Google token Id:   "+ mGPlusToken);
							Map<String, String> gparams = new HashMap<String, String>();
							gparams.put("google_id", mPlusClient.getCurrentPerson().getId());
							gparams.put("authToken", mGPlusToken);
							gparams.put("tokenExpiry", mDevInfo.getClientKeyExp());
							gparams.put("clientKey",mDevInfo.getClientKey());
							googleLoginRequest(getString(R.string.gplusloginpath), gparams);
						} catch (UserRecoverableAuthException e) {
							int REQUEST_AUTHORIZATION=9000;
							// This error is recoverable, so we could fix this
							// by displaying the intent to the user.
							startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (GoogleAuthException e) {
							e.printStackTrace();
						}
						return null;
					}

				};
				task.execute((Void) null);

		//loader.setVisibility(View.VISIBLE);
		//showToast("Connected");

		//showProgressBar();

		//getAuthToken();

		//getAndUseAuthTokenInAsyncTask();


	}*/

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
	private boolean mShowExitToast = true;
	private boolean closeApplication(){
		if(mShowExitToast){
			Toast.makeText(this, "Press back again to close the application.", Toast.LENGTH_LONG).show();
			mShowExitToast = false;
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					mShowExitToast = true;
				}
			}, 3000);
			return false;
		}else{
			FlurryAgent.onEndSession(this);
			exitApp();
			return true;
		}

	}
	private void exitApp(){
		android.os.Process.killProcess(android.os.Process.myPid());
		System.runFinalizersOnExit(true);
		System.exit(0);
		finish();
	}
	@Override
	public void onBackPressed() {

		/*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setMessage("Are you sure you want to exit?")
		        .setCancelable(false)
		        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                 finish();
		            }
		        })
		        .setNegativeButton("No", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                 dialog.cancel();
		            }
		        });
		 AlertDialog alert = builder.create();
		 alert.show();*/

		closeApplication();





	}
	/*	private void RunSlideDownAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, -100,0 );   
		slide.setDuration(300);   
		slide.setFillAfter(false);   
		mRootLayout.setAnimation(slide);
	}

	private void RunAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, 100,0 );   
		slide.setDuration(300);   
		slide.setFillAfter(false);   
		mRootLayout.setAnimation(slide);
	}
	 */
	@Override
	protected void onStart() {
		super.onStart();
		//mPlusClient.disconnect();
		//mPlusClient.connect();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(AccountUtils.isAuthenticated(LoginActivity.this))
		{
			finish();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			LoginActivity.this.startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		super.onCreateOptionsMenu(menu);   

		return true;
	}

	

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {

			onSessionStateChange(session, state, exception);

		}
	}
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException) {

			Log.d(TAG,getString(R.string.userCancelled));
			Map<String, String> articleParams = new HashMap<String, String>();
			articleParams.put("Author", mDevInfo.getClientKey()); // Capture author info
			articleParams.put("User_Status", "User Cancelled"); // Capture user status

			FlurryAgent.logEvent("Facebook", articleParams);
			// Cancelled by user, show alert
			//new AlertDialog.Builder(this).setTitle(R.string.cancelled).setMessage(R.string.permission_not_granted).setPositiveButton(R.string.ok, null).show();

		} else {

			updateView();
		}
	}

	private void updateView() {

		Session session = Session.getActiveSession();
		if (session.isOpened() && !AccountUtils.isAuthenticated(LoginActivity.this)) {

			Map<String, String> articleParams = new HashMap<String, String>();
			articleParams.put("Author", mDevInfo.getClientKey()); // Capture author info
			articleParams.put("User_Status", "Registred"); // Capture user status

			FlurryAgent.logEvent("Facebook", articleParams);
			//showToast("Session is active");
			//String token =session.getAccessToken();
			final String token =Session.getActiveSession().getAccessToken();
			final java.util.Date tokenExpiry=Session.getActiveSession().getExpirationDate();

			//showToast(tokenExpiry.toString());

			showProgressBar();

			//finish();
			//myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

			Request request = Request.newMeRequest(Session.getActiveSession(), new GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user,
						com.facebook.Response response) {
					fbUserId=user.getId();
					mUserInfo.setName(user.getName());
					mUserInfo.setLoginStatus(true);
					//mUserInfo.setProfilePic("http://graph.facebook.com/"+fbUserId+"/picture?type=large");
					mUserInfo.setProfilePic("https://graph.facebook.com/"+fbUserId+"/picture?width=300&height=300");

					Log.d(TAG, "Facebook User Id:   "+fbUserId);
					Map<String, String> params = new HashMap<String, String>();
					params.put("facebookId", fbUserId);
					params.put("profile", "work");
					params.put("authToken", token);
					params.put("tokenExpiry", tokenExpiry.toGMTString());
					params.put("clientKey",mDevInfo.getClientKey());
					facebookLoginRequest(getString(R.string.fbloginpath), params);

				}
			});
			request.executeAsync();


		} else {
			//showToast("Session is not active");
		}
	}

	protected void facebookLoginRequest(String contextPath,
			final Map<String, String> bodyParams) {

		RequestQueue queue = MyVolley.getRequestQueue();

		String url=getString(R.string.url)+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				fbLoginSuccessListener(),
				fbLoginErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);

	}
	protected ErrorListener fbLoginErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					sendNotification(getString(R.string.loginerr));
					//finish();
					//myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

				}
				else
				{
					sendNotification(error.toString());	
				}
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> fbLoginSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				try {	
					dismissProgressBar();
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						//sendNotification(jsonResponse.getString("message"));
						finish();
						myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

					}
					else
					{
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

	private void onClickLogin() {

		Map<String, String> articleParams = new HashMap<String, String>();
		articleParams.put("Author", mDevInfo.getClientKey()); // Capture author info
		articleParams.put("User_Status", "Registration Request"); // Capture user status

		FlurryAgent.logEvent("Facebook", articleParams);

		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
			.setPermissions(Arrays.asList("basic_info","email"))
			.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}


	}

	@Override
	public void onClick(View view) {
		ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(findViewById(view.getId()), "alpha", 0.5f, 1f);
		fadeAnim2.setDuration(800);
		fadeAnim2.start();

		if (view.getId() == R.id.fblogin){

			if(Session.getActiveSession().isOpened())
			{
				//onClickLogout();
				//sendRequestDialog();
			}
			else
			{
				onClickLogin();				
			}
		}
		else if (view.getId() == R.id.googlelogin ) {

			if(!AccountUtils.isAuthenticated(LoginActivity.this))
			{
				mPlusClient.connect();
			}
			else
			{
				sendNotification("Already Connected");
			}
		}
	}
	
	private void tryAuthenticate() {
        // Authenticate through the Google Play OAuth client.
        mAuthInProgress = true;
        AccountUtils.tryAuthenticate(this, this, mPlusClient.getAccountName(),
                REQUEST_RECOVER_FROM_AUTH_ERROR);
    }

    @Override
    public boolean shouldCancelAuthentication() {
        return mCancelAuth;
    }

    private void googleLoginRequest(String contextPath,
			final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();
		// showToast("GOOGLE LOGIN REQ");
		String url=getString(R.string.url)+contextPath;
		Log.d(TAG, url);
		Log.d(TAG,bodyParams.toString());
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				googleLoginSuccessListener(),
				googleLoginErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
		
	}
	protected ErrorListener googleLoginErrorListener() {
		//loader.setVisibility(View.GONE);
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					Log.d(TAG,"Error: NoConnectionError");
					//showToast(getString(R.string.loginerr));
					//finish();
					//myplexUtils.launchActivity(MainActivity.class,LoginActivity.this , null);

				}
				else
				{
					Log.d(TAG, error.toString());	
				}
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> googleLoginSuccessListener() {
		//loader.setVisibility(View.GONE);
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				dismissProgressBar();
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						
						
						finish();
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						LoginActivity.this.startActivity(intent);
						
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						if(jsonResponse.getString("code").equalsIgnoreCase("419"))
						{
							
							AccountUtils.refreshAuthToken(LoginActivity.this);
							//tryAuthenticate();
//							GoogleAuthUtil.invalidateToken(LoginActivity.this,AccountUtils.getAuthToken(LoginActivity.this));
							//sendNotification(jsonResponse.getString("message"));
							//mPlusClient.disconnect();
							//mPlusClient.connect();
							//getAndUseAuthTokenInAsyncTask();
						}
						else
						{
							//sendNotification(jsonResponse.getString("message"));
						}
						//(jsonResponse.getString("message"));
						//showToast("Err: "+jsonResponse.getString("code")+" \nErr Msg: "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
    
	// Google Plus client callbacks.
    @Override
    public void onConnected(Bundle connectionHint) {
        // It is possible that the authenticated account doesn't have a profile.
        mPlusClient.loadPerson(this, "me");
        showProgressBar();
        tryAuthenticate();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        REQUEST_RECOVER_FROM_AUTH_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Internal error encountered: " + e.getMessage());
            }
            return;
        }

        final int errorCode = connectionResult.getErrorCode();
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                    REQUEST_PLAY_SERVICES_ERROR_DIALOG).show();
        }
    }
    @Override
    public void onAuthTokenAvailable() {
    	
    	String clientKey=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
				getString(R.string.devclientkey));
		String clientKeyExp=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
				getString(R.string.devclientkeyexp));    	
    	
    	Map<String, String> gparams = new HashMap<String, String>();
		gparams.put("google_id", mPlusClient.getCurrentPerson().getId());
		gparams.put("authToken", AccountUtils.getAuthToken(this));
		gparams.put("tokenExpiry", clientKeyExp);
		gparams.put("clientKey",clientKey);
		googleLoginRequest(getString(R.string.gplusloginpath), gparams);
    	
    	/*
        // Cancel progress fragment.
        // Create set up fragment.
        mAuthInProgress = false;
        if (mAuthProgressFragmentResumed) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.root_container,
                            SignInSetupFragment.makeFragment(SETUP_ATTENDEE), "setup_attendee")
                    .addToBackStack("signin_main")
                    .commit();
        }
    */}
	
	@Override
    public void onRecoverableException(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog d = GooglePlayServicesUtil.getErrorDialog(
                        code,
                        LoginActivity.this,
                        REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR);
                d.show();
            }
        });
    }

    @Override
    public void onUnRecoverableException(final String errorMessage) {
        Log.w(TAG, "Encountered unrecoverable exception: " + errorMessage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mChosenAccount != null)
            outState.putString(KEY_CHOSEN_ACCOUNT, mChosenAccount.name);
        super.onSaveInstanceState(outState);
        
        Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTHENTICATE ||
                requestCode == REQUEST_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_PLAY_SERVICES_ERROR_DIALOG) {
            if (resultCode == RESULT_OK) {
                if (mPlusClient != null) mPlusClient.connect();
            } else {
                if (mAuthProgressFragmentResumed) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                } else {
                    mCanRemoveAuthProgressFragment = true;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthInProgress) mCancelAuth = true;
        if (mPlusClient != null)
            mPlusClient.disconnect();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onPersonLoaded(ConnectionResult connectionResult, Person person) {
        if (connectionResult.isSuccess()) {
            // Se the profile id
            if (person != null) {
                AccountUtils.setPlusProfileId(this, person.getId());
            }
        } else {
            Log.e(TAG, "Got " + connectionResult.getErrorCode() + ". Could not load plus profile.");
            mPlusClient.connect();
        }
    }
    
    
}
