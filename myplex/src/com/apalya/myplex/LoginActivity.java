package com.apalya.myplex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.AccountUtils;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.PlayServicesUtils;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Twitter11;
import com.apalya.myplex.utils.Util;
import com.crashlytics.android.Crashlytics;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.mixpanel.android.mpmetrics.MixpanelAPI;




public class LoginActivity extends Activity implements OnClickListener, AccountUtils.AuthenticateCallback,Twitter11.TwitterAuthenticateCallback, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, PlusClient.OnPersonLoadedListener {

	private Button mFacebookButton,mGoogleLogin,mTwitterLogin;
	private TextView mLetMeIn,mLoginText,mSignupText;
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


	private static final String TWITTER_CONSUMER_KEY= "0ZHTN70CDo8JdwEKQWBag";
	private static final String TWITTER_CONSUMER_SECRET= "hX13meHYmTHb07gt78lwKZG97YJgcx1FyOg8MDDhzo";

	private static final int MESSAGE_SENT= 14;
	public static final int TWITTER_CALLBACK= 31;

	private static final String KEY_CHOSEN_ACCOUNT = "chosen_account";

	private static final int REQUEST_AUTHENTICATE = 100;
	private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
	private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
	private static final int REQUEST_PLAY_SERVICES_ERROR_DIALOG = 103;

	private static final String MIXPANEL_DISTINCT_ID_NAME = "Mixpanel Example $distinctid";

	private Account mChosenAccount;
	private boolean mCancelAuth = false;
	private boolean mAuthInProgress = false;
	private boolean mAuthProgressFragmentResumed = false;
	private boolean mCanRemoveAuthProgressFragment = false;
	private PlusClient mPlusClient;
	private DeviceDetails mDevInfo;
	private UserProfile mUserInfo;
	private MixpanelAPI mMixpanel;
	private Twitter11 twitter11;
	private static int scrollWidth;
	private TranslateAnimation translateAnim;
	private RelativeLayout backgroundScrollLayout;

	/*
	 * In order for your app to receive push notifications, you will need to enable
	 * the Google Cloud Messaging for Android service in your Google APIs console. To do this:
	 *
	 * - Navigate to https://code.google.com/apis/console
	 * - Select "Services" from the menu on the left side of the screen
	 * - Scroll down until you see the row labeled "Google Cloud Messaging for Android"
	 * - Make sure the switch next to the service name says "On"
	 *
	 * To identify this application with your Google API account, you'll also need your sender id from Google.
	 * You can get yours by logging in to the Google APIs Console at https://code.google.com/apis/console
	 * Once you have logged in, your sender id will appear as part of the URL in your browser's address bar.
	 * The URL will look something like this:
	 *
	 *     https://code.google.com/apis/console/b/0/#project:256660625236
	 *                                                       ^^^^^^^^^^^^
	 *
	 * The twelve-digit number after 'project:' is your sender id. Paste it below (where you see "YOUR SENDER ID")
	 *
	 * There are also some changes you will need to make to your AndroidManifest.xml file to
	 * declare the permissions and receiver capabilities you'll need to get your push notifications working.
	 * You can take a look at this application's AndroidManifest.xml file for an example of what is needed.
	 */
	public static final String ANDROID_PUSH_SENDER_ID = "317019093395";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Crashlytics.start(this);
		FontUtil.loadFonts(getAssets());
		String trackingDistinctId = getTrackingDistinctId();
		mMixpanel=myplexapplication.getMixPanel();
		// We also identify the current user with a distinct ID, and
		// register ourselves for push notifications from Mixpanel.

		mMixpanel.identify(trackingDistinctId); //this is the distinct_id value that
		// will be sent with events. If you choose not to set this,
		// the SDK will generate one for you

		mMixpanel.getPeople().identify(trackingDistinctId); //this is the distinct_id
		// that will be used for people analytics. You must set this explicitly in order
		// to dispatch people data.

		mMixpanel.getPeople().initPushHandling(ANDROID_PUSH_SENDER_ID);

		// You can call enableLogAboutMessagesToMixpanel to see
		// how messages are queued and sent to the Mixpanel servers.
		// This is useful for debugging, but should be disabled in
		// production code.
		//mMixpanel.logPosts();

		// People analytics must be identified separately from event analytics.
		// The data-sets are separate, and may have different unique keys (distinct_id).
		// We recommend using the same distinct_id value for a given user in both,
		// and identifying the user with that id as early as possible.

		if(getResources().getBoolean(R.bool.isTablet))
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		else
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if(getActionBar()!=null)
			getActionBar().hide();
		setContentView(R.layout.loginscreen);

		SharedPreferences prefs = getSharedPreferences("TWITTERTIME", 0);
		twitter11= new Twitter11(this,this, R.string.app_name, prefs, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);

		prepareSlideNotifiation();

		mDevInfo=myplexapplication.getDevDetailsInstance();
		mUserInfo=myplexapplication.getUserProfileInstance();
		mGoogleLogin = (Button)findViewById(R.id.google);
		mGoogleLogin.setOnClickListener(this);
		mGoogleLogin.setTypeface(FontUtil.Roboto_Regular);

		mTwitterLogin = (Button)findViewById(R.id.twitter);
		mTwitterLogin.setOnClickListener(this);
		mTwitterLogin.setTypeface(FontUtil.Roboto_Regular);


		mFacebookButton = (Button) findViewById(R.id.fb);
		mFacebookButton.setOnClickListener(this);
		mFacebookButton.setTypeface(FontUtil.Roboto_Regular);

		

		mLoginText=(TextView)findViewById(R.id.logintext);
		mLoginText.setTypeface(FontUtil.Roboto_Regular);

		mLoginText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLoginText, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(300);
				fadeAnim2.start();
				
				
				if(mDevInfo.getClientKey()!=null)
				{
					finish();
					Map<String, String> map = new HashMap<String, String>();
					String param1 = "login";
					map.put(param1, "true");
					Util.launchActivity(SignUpActivity.class,LoginActivity.this , map);
				}
				else
				{
					Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//					Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
				}

			}
		});

		mSignupText=(TextView)findViewById(R.id.signuptext);
		mSignupText.setTypeface(FontUtil.Roboto_Regular);

		mSignupText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mSignupText, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(300);
				fadeAnim2.start();
				
				if(mDevInfo.getClientKey()!=null)
				{
					finish();
					Util.launchActivity(SignUpActivity.class,LoginActivity.this , null);
				}
				else
				{
					Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//					Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
				}
				
				

			}
		});

		mLetMeIn=(TextView)findViewById(R.id.letmeinMsg);
		mLetMeIn.setTypeface(FontUtil.Roboto_Regular);
		mLetMeIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLetMeIn, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(300);
				fadeAnim2.start();
				
				
				if(mDevInfo.getClientKey()!=null)
				{
					Map<String,String> param1=new HashMap<String, String>();
					param1.put("status", "Success");
					Analytics.trackEvent(Analytics.loginGuest,param1);
					mUserInfo.setName("Guest");
					finish();
					Util.launchMainActivity(LoginActivity.this);
				}
				else
				{
					Map<String,String> param2=new HashMap<String, String>();
					param2.put("status", "Failure");
					Analytics.trackEvent(Analytics.loginGuest,param2);
					Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//					Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
				}
				
				
				//				Util.launchActivity(MainActivity.class,LoginActivity.this , null);

			}
		});


		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Settings.addLoggingBehavior(LoggingBehavior.REQUESTS);
		Session session = Session.getActiveSession();
		if (session == null) {
			//Util.showToast("Facebook No Session", this);
			if (savedInstanceState != null) {
			
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
			
				session = new Session(this);
			}
			Session.setActiveSession(session);
			
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				//Util.showToast("Facebook new Session2", this);
				//session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
				/*if (!session.isOpened() && !session.isClosed()) {
					Util.showToast("Facebook new Session3", this);
					session.openForRead(new Session.OpenRequest(this)
					.setPermissions(Arrays.asList("basic_info","email","read_friendlists","user_about_me","friends_about_me","user_hometown"))
					.setCallback(statusCallback));
				} else {
					Util.showToast("Facebook Opening Session", this);
					Session.openActiveSession(this, true, statusCallback);
				}*/
			}

		}
		else
		{
			
			if(session.isOpened())
			{
			
				Map<String,String> params=new HashMap<String, String>();
				params.put("status", "Success");
				Analytics.trackEvent(Analytics.loginFacebook,params);
				
				finish();
				Util.launchMainActivity(LoginActivity.this);
			}

		}

		mPlusClient = (new PlusClient.Builder(this, this, this))
				.setScopes(AccountUtils.AUTH_SCOPES)
				.build();

		/*if(AccountUtils.isAuthenticated(LoginActivity.this) )
		{

			mPlusClient.connect();

		}*/
		//CheckUserStatus();

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		final HorizontalScrollView parentScrollView= (HorizontalScrollView) findViewById(R.id.scrollView1);
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

				if(getResources().getBoolean(R.bool.isTablet))
				{
					scrollWidth=parentScrollView.getChildAt(0).getMeasuredWidth()-getWindowManager().getDefaultDisplay().getWidth();
					LinearLayout backgroundLayout= (LinearLayout)findViewById(R.id.relativeLayout1);
					backgroundLayout.clearAnimation();
					translateAnim = new TranslateAnimation(-scrollWidth,0,0,0); 
					translateAnim.setDetachWallpaper(true);
					translateAnim.setDuration((scrollWidth/2)*100);   
					translateAnim.setRepeatCount(Animation.INFINITE);
					translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
					translateAnim.setRepeatMode(2);
					translateAnim.scaleCurrentDuration(1f);
					backgroundLayout.startAnimation(translateAnim);
				}
				else
				{
					scrollWidth=parentScrollView.getChildAt(0).getMeasuredWidth()-getWindowManager().getDefaultDisplay().getWidth();
					backgroundScrollLayout= (RelativeLayout)findViewById(R.id.relativeLayout1);
					//backgroundScrollLayout.clearAnimation();
					//backgroundScrollLayout.isAlwaysDrawnWithCacheEnabled();
					translateAnim = new TranslateAnimation(0,-scrollWidth,0,0); 
					//translateAnim.setDetachWallpaper(true);
					//translateAnim.setFillEnabled(true);
					translateAnim.setDuration(width*100);   
					translateAnim.setRepeatCount(Animation.INFINITE);
					translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
					translateAnim.setRepeatMode(2);
					//translateAnim.scaleCurrentDuration(1f);
					backgroundScrollLayout.startAnimation(translateAnim);
				}
			}
		});
		
		//Check if Keyboard is visible or not
		//mRootLayout = findViewById(R.id.rootlayout);  

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		// To preserve battery life, the Mixpanel library will store
		// events rather than send them immediately. This means it
		// is important to call flush() to send any unsent events
		// before your application is taken out of memory.
		mMixpanel.flush();
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
			Util.animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2,LoginActivity.this);

		}
	};

	private void showNotification(){
		Util.animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2,LoginActivity.this);
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
			Util.showToast(LoginActivity.this, "Press back again to close the application.",Util.TOAST_TYPE_ERROR);
//			Toast.makeText(this, "Press back again to close the application.", Toast.LENGTH_LONG).show();
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
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Map<String,String> params=new HashMap<String, String>();
		params.put("Status", "Shown");
		params.put("PreviousScreen", "None");
		if(Util.isWifiEnabled(this))
		{
			params.put("Network", "Wifi");	
		}
		else
		{
			params.put("Network", "Mobile");
		}
		
		Analytics.trackEvent(Analytics.loginScreen,params);
		
		long nowInHours = hoursSinceEpoch();
		int hourOfTheDay = hourOfTheDay();

		// For our simple test app, we're interested tracking
		// when the user views our application.

		// It will be interesting to segment our data by the date that they
		// first viewed our app. We use a
		// superProperty (so the value will always be sent with the
		// remainder of our events) and register it with
		// registerSuperPropertiesOnce (so no matter how many times
		// the code below is run, the events will always be sent
		// with the value of the first ever call for this user.)
		// all the change we make below are LOCAL. No API requests are made.
		try {
			JSONObject properties = new JSONObject();
			properties.put("first viewed on", nowInHours);
			properties.put("user domain", "(unknown)"); // default value
			mMixpanel.registerSuperPropertiesOnce(properties);
		} catch (JSONException e) {
			throw new RuntimeException("Could not encode hour first viewed as JSON");
		}

		// Now we send an event to Mixpanel. We want to send a new
		// "App Resumed" event every time we are resumed, and
		// we want to send a current value of "hour of the day" for every event.
		// As usual,all of the user's super properties will be appended onto this event.
		try {
			JSONObject properties = new JSONObject();
			properties.put("hour of the day", hourOfTheDay);
			mMixpanel.track("App Resumed", properties);
		} catch(JSONException e) {
			throw new RuntimeException("Could not encode hour of the day in JSON");
		}

		CheckUserStatus();

		/*if(isAuthTwitter())
		{
			finish();
			Util.launchMainActivity(LoginActivity.this);
			//			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			//			LoginActivity.this.startActivity(intent);
		}*/
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

			Analytics.endTimedEvent(Analytics.loginFacebook);
			Map<String,String> attribs=new HashMap<String, String>();
			attribs.put("Status", "Cancel");
			attribs.put("Msg", "User Cancelled");
			Analytics.trackEvent(Analytics.loginFacebook,attribs);
			Log.d(TAG,getString(R.string.userCancelled));
		} else {

			updateView();
		}
	}

	private void updateView() {

		Session session = Session.getActiveSession();
		if (session.isOpened() && !AccountUtils.isAuthenticated(LoginActivity.this)) {

			//showToast("Session is active");
			//String token =session.getAccessToken();
			final String token =Session.getActiveSession().getAccessToken();
			final java.util.Date tokenExpiry=Session.getActiveSession().getExpirationDate();

			//showToast(tokenExpiry.toString());

			showProgressBar();

			if(mProgressDialog!=null)
				mProgressDialog.setMessage("Getting details from facebook....");

			//finish();
			//Util.launchActivity(MainActivity.class,LoginActivity.this , null);

			Request request = Request.newMeRequest(Session.getActiveSession(), new GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user,
						com.facebook.Response response) {
					if(user!=null)
					{
						fbUserId=user.getId();
						mUserInfo.setName(user.getName());
						mUserInfo.setUserEmail(user.getProperty("email").toString());
						//mUserInfo.location=user.getProperty("location").toString();
						//mUserInfo.setProfileDesc(user.getProperty("hometown").);
						mUserInfo.setLoginStatus(true);
						mUserInfo.setProfilePic("https://graph.facebook.com/"+fbUserId+"/picture?width=480&height=320");
						
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devusername), user.getProperty("email").toString());
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.userprofilename), user.getName());
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.userpic), "https://graph.facebook.com/"+fbUserId+"/picture?width=480&height=320");
						
						Crashlytics.setUserEmail(user.getProperty("email").toString());

						String userIdSha1=Util.sha1Hash(user.getProperty("email").toString());
						FlurryAgent.setUserId(userIdSha1);
						Crashlytics.setUserName(userIdSha1);
						Crashlytics.setUserIdentifier(userIdSha1);
						
						
						Log.d(TAG, "Facebook User Id:   "+fbUserId);
						Map<String, String> params = new HashMap<String, String>();
						params.put("facebookId", fbUserId);
						params.put("profile", "work");
						params.put("authToken", token);
						params.put("tokenExpiry", tokenExpiry.toGMTString());
						params.put("clientKey",mDevInfo.getClientKey());
						facebookLoginRequest(getString(R.string.fbloginpath), params);
					}
					else
					{
						if(response.getError().getErrorCode()==-1)
						{
							dismissProgressBar();
							Util.showToast(LoginActivity.this, "No internet connection.",Util.TOAST_TYPE_ERROR);
//							Util.showToast("No Internet Connection...", LoginActivity.this);	
							//finish();
							//Util.launchActivity(MainActivity.class, LoginActivity.this, null);

						}
						else
						{
							Util.showToast(LoginActivity.this, response.getError().getErrorMessage(),Util.TOAST_TYPE_ERROR);
//							Util.showToast(response.getError().getErrorMessage(), LoginActivity.this);
						}

					}

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

		if(mProgressDialog!=null)
			mProgressDialog.setMessage("Logging in, Please wait...");

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
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
		myReq.setShouldCache(false);
		Log.d(TAG,"Request sent ");
		queue.add(myReq);

	}
	protected ErrorListener fbLoginErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Analytics.endTimedEvent(Analytics.loginFacebook);
				Map<String,String> params=new HashMap<String, String>();
				params.put("Status", "Failure");
				params.put("Msg", error.toString());
				Analytics.trackEvent(Analytics.loginFacebook,params);
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					sendNotification(getString(R.string.loginerr));
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
				Analytics.endTimedEvent(Analytics.loginFacebook);
				
				try {	
					dismissProgressBar();
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);
					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Success");
						Analytics.trackEvent(Analytics.loginFacebook,attribs);
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						//sendNotification(jsonResponse.getString("message"));
						finish();
						Util.launchMainActivity(LoginActivity.this);
					}
					else
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Failure");
						attribs.put("Msg", jsonResponse.getString("code"));
						Analytics.trackEvent(Analytics.loginFacebook,attribs);
						if(jsonResponse.getString("code").equalsIgnoreCase("401"))
						{
							String devId=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
									getString(R.string.devclientdevid));

							Map<String, String> params = new HashMap<String, String>();
							params.put("deviceId", devId);

							Util.genKeyRequest(LoginActivity.this,getString(R.string.genKeyReqPath),params);
							sendNotification("Err: "+jsonResponse.getString("code")+" \nErr Msg: "+jsonResponse.getString("message"));
						}
						else
						{
							Log.d(TAG, "code: "+jsonResponse.getString("code"));
							Log.d(TAG, "message: "+jsonResponse.getString("message"));

							if(Session.getActiveSession()!=null)
								Session.getActiveSession().close();
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
			.setPermissions(Arrays.asList("basic_info","email","read_friendlists","user_about_me","friends_about_me","user_hometown"))
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

		if (view.getId() == R.id.fb){

			if(mDevInfo.getClientKey()!=null)
			{
				
				Map<String,String> param1=new HashMap<String, String>();
				param1.put("Duration", "");
				Analytics.trackEvent(Analytics.loginFacebook,param1,true);
				
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
			else
			{
				Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//				Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
			}


		}
		else if (view.getId() == R.id.google ) {

			//if(!AccountUtils.isAuthenticated(LoginActivity.this))
			{
				if(mDevInfo.getClientKey()!=null)
				{
					// Verifies the proper version of Google Play Services exists on the device.
					if(PlayServicesUtils.checkGooglePlaySevices(this))
					{
						if(mPlusClient!=null)
						{
							Map<String,String> param1=new HashMap<String, String>();
							param1.put("Duration", "");
							Analytics.trackEvent(Analytics.loginGoogle,param1,true);
							mPlusClient.connect();
						}
					}
				}
				else
				{
					Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//					Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
				}
			}
		}
		else if(view.getId()==R.id.twitter)
		{
			if(mDevInfo.getClientKey()!=null)
			{
				//if (!isAuthTwitter())
				{
					Map<String,String> param1=new HashMap<String, String>();
					param1.put("Duration", "");
					Analytics.trackEvent(Analytics.loginTwitter,param1,true);
					twitter11.login();
				}
			}
			else
			{
				Util.showToast(LoginActivity.this, "Your device registration has been failed, Please check your internet connectivity and reopen the application.",Util.TOAST_TYPE_ERROR);
//				Util.showToast("Your device registration has been failed, Please check your internet connectivity and reopen the app",  LoginActivity.this);
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
		if(mProgressDialog!=null)
			mProgressDialog.setMessage("Logging in, Please wait...");
		RequestQueue queue = MyVolley.getRequestQueue();
		// showToast("GOOGLE LOGIN REQ");
		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
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
		myReq.setShouldCache(false);
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
					//Util.launchActivity(MainActivity.class,LoginActivity.this , null);

				}
				else
				{
					Log.d(TAG, error.toString());

				}
				sendNotification(error.toString());
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
				Analytics.endTimedEvent(Analytics.loginGoogle);
				
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Success");
						
						Analytics.trackEvent(Analytics.loginGoogle,attribs);
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devusername), mUserInfo.getUserEmail());
						
						Crashlytics.setUserEmail(mUserInfo.getUserEmail());

						String userIdSha1=Util.sha1Hash(mUserInfo.getUserEmail());
						FlurryAgent.setUserId(userIdSha1);
						Crashlytics.setUserName(userIdSha1);
						Crashlytics.setUserIdentifier(userIdSha1);
						
						finish();
						Util.launchMainActivity(LoginActivity.this);
					}
					else
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Failure");
						attribs.put("Msg", jsonResponse.getString("code"));
						Analytics.trackEvent(Analytics.loginGoogle,attribs);
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						if(jsonResponse.getString("code").equalsIgnoreCase("419"))
						{
							AccountUtils.refreshAuthToken(LoginActivity.this);
							sendNotification(jsonResponse.getString("message"));
						}
						else if(jsonResponse.getString("code").equalsIgnoreCase("401"))
						{
							String devId=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
									getString(R.string.devclientdevid));

							Map<String, String> params = new HashMap<String, String>();
							params.put("deviceId", devId);

							Util.genKeyRequest(LoginActivity.this,getString(R.string.genKeyReqPath),params);
						}
							sendNotification(jsonResponse.getString("code")+" : "+jsonResponse.getString("message"));
							AccountUtils.signOut(LoginActivity.this);
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
		showProgressBar();
		if(mProgressDialog!=null)
			mProgressDialog.setMessage("Getting details from google....");
		mPlusClient.loadPerson(this, "me");

	}

	@Override
	public void onDisconnected() {
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Analytics.endTimedEvent(Analytics.loginGoogle);
		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put("Status", "Failure");
		attribs.put("Msg", connectionResult.toString());
		Analytics.trackEvent(Analytics.loginGoogle,attribs);
		
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
		gparams.put("google_id", mUserInfo.getGoogleId());
		gparams.put("authToken", AccountUtils.getAuthToken(this));
		gparams.put("tokenExpiry", clientKeyExp);
		gparams.put("clientKey",clientKey);
		googleLoginRequest(getString(R.string.gplusloginpath), gparams);	
	}

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

			if (requestCode == MESSAGE_SENT ||
					requestCode == TWITTER_CALLBACK)
			{
				if (resultCode == Activity.RESULT_OK){
					switch(requestCode){
					case MESSAGE_SENT:
						final String smsg= data.getExtras().getString(Twitter11.COM_REPLY);
						Util.showToast(this, smsg,Util.TOAST_TYPE_ERROR);
//						Toast.makeText(this, smsg, Toast.LENGTH_SHORT).show();
						break;
					case TWITTER_CALLBACK:
						if(data.getData() != null){	
							twitter11.logincallback(data, new Runnable(){
								public void run(){
									Log.i(TAG, "after ActivityResult ");
									Util.showToast(LoginActivity.this, isAuthTwitter()? "Logged In" : "Not Logged In",Util.TOAST_TYPE_ERROR);
//									Toast.makeText(LoginActivity.this, isAuthTwitter()? "Logged In" : "Not Logged In", Toast.LENGTH_SHORT).show();
								}
							});

						}else{
							Util.showToast(this, data.getExtras().getString(Twitter11.COM_REPLY),Util.TOAST_TYPE_ERROR);
//							Toast.makeText(this, data.getExtras().getString(Twitter11.COM_REPLY), Toast.LENGTH_SHORT).show();
						}
						break;
					}
				}else if(resultCode == Activity.RESULT_CANCELED){
					Analytics.endTimedEvent(Analytics.loginTwitter);
					Map<String,String> params=new HashMap<String, String>();
					params.put("Status", "Cancel");
					Analytics.trackEvent(Analytics.loginTwitter,params);
					if(requestCode==TWITTER_CALLBACK){
						Util.showToast(this, isAuthTwitter()? "Logged In" : "Request Cancelled",Util.TOAST_TYPE_ERROR);
//						Toast.makeText(LoginActivity.this, isAuthTwitter()? "Logged In" : "Request Cancelled", Toast.LENGTH_SHORT).show();
					}
				}	
			}
			else
			{
				super.onActivityResult(requestCode, resultCode, data);
				Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);	
			}


		}
	}
	private boolean isAuthTwitter(){
		return twitter11.isloggedin();
	}
	@Override
	public void onStop() {
		
		if (mAuthInProgress) mCancelAuth = true;
		if (mPlusClient != null)
			mPlusClient.disconnect();
		if(Session.getActiveSession()!=null)
			Session.getActiveSession().removeCallback(statusCallback);

		FlurryAgent.onEndSession(this);
		
		super.onStop();
	}

	@Override
	public void onPersonLoaded(ConnectionResult connectionResult, Person person) {
		if (connectionResult.isSuccess()) {
			// Se the profile id
			if (person != null) {
				AccountUtils.setPlusProfileId(this, person.getId());
				mUserInfo.setGoogleId(person.getId());
				mUserInfo.setName(person.getName().getGivenName());
				mUserInfo.setProfilePic("https://plus.google.com/s2/photos/profile/"+person.getId()+"?sz=480");
				SharedPrefUtils.writeToSharedPref(LoginActivity.this,
						getString(R.string.userprofilename), person.getName().getGivenName());
				SharedPrefUtils.writeToSharedPref(LoginActivity.this,
						getString(R.string.userpic), "https://plus.google.com/s2/photos/profile/"+person.getId()+"?sz=480");
				tryAuthenticate();

			}
		} else {
			Log.e(TAG, "Got " + connectionResult.getErrorCode() + ". Could not load plus profile.");
			dismissProgressBar();
			/*if(connectionResult.getErrorCode()==7)
			{
				Util.showToast("No Internet Connection...", LoginActivity.this);
				//finish();
				//Util.launchActivity(MainActivity.class, LoginActivity.this, null);
			}
			else*/
			{
				Util.showToast(LoginActivity.this, String.valueOf(connectionResult.getErrorCode())+": Connection to google plus server failed.",Util.TOAST_TYPE_ERROR);
			}

			/*if(!mPlusClient.isConnected())
				mPlusClient.connect();*/
		}
	}
	////////////////////////////////////////////////////

	private String getTrackingDistinctId() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);

		String ret = prefs.getString(MIXPANEL_DISTINCT_ID_NAME, null);
		if (ret == null) {
			ret = generateDistinctId();
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.putString(MIXPANEL_DISTINCT_ID_NAME, ret);
			prefsEditor.commit();
		}

		return ret;
	}

	// These disinct ids are here for the purposes of illustration.
	// In practice, there are great advantages to using distinct ids that
	// are easily associated with user identity, either from server-side
	// sources, or user logins. A common best practice is to maintain a field
	// in your users table to store mixpanel distinct_id, so it is easily
	// accesible for use in attributing cross platform or server side events.
	private String generateDistinctId() {
		Random random = new Random();
		byte[] randomBytes = new byte[32];
		random.nextBytes(randomBytes);
		return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
	}

	///////////////////////////////////////////////////////
	private int hourOfTheDay() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	private long hoursSinceEpoch() {
		Date now = new Date();
		long nowMillis = now.getTime();
		return nowMillis / 1000 * 60 * 60;
	}

	private String domainFromEmailAddress(String email) {
		String ret = "";
		int atSymbolIndex = email.indexOf('@');
		if ((atSymbolIndex > -1) && (email.length() > atSymbolIndex)) {
			ret = email.substring(atSymbolIndex + 1);
		}

		return ret;
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
		if(mTelephonyMgr!=null)
		{
			mDevInfo.setDeviceId(mTelephonyMgr.getDeviceId());
			mDevInfo.setOperatorName(mTelephonyMgr.getNetworkOperatorName());
			mDevInfo.setMccMnc(String.valueOf(mTelephonyMgr.getSimOperator()));
			mDevInfo.setSimSNo(mTelephonyMgr.getSimSerialNumber());
			mDevInfo.setImsiNo(mTelephonyMgr.getSubscriberId());
			mDevInfo.setSimState(mTelephonyMgr.getSimState());
		}
		Log.d(TAG, "******************************************************************");
	}
	


	private void devRegRequest(String contextPath, final Map<String, String> bodyParams) {


		RequestQueue queue = MyVolley.getRequestQueue();

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;

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
		myReq.setShouldCache(false);
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
					Util.showToast(LoginActivity.this, getString(R.string.interneterr),Util.TOAST_TYPE_ERROR);
//					Util.showToast(getString(R.string.interneterr),LoginActivity.this);
				}
				else
				{
					Util.showToast(LoginActivity.this, error.toString(),Util.TOAST_TYPE_ERROR);
//					Util.showToast(error.toString(),LoginActivity.this);	
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

						long nowinms=System.currentTimeMillis();
						Date now=new Date(nowinms);
						mUserInfo.joinedDate=now.toLocaleString();
						mUserInfo.lastVisitedDate=now.toLocaleString();

						ConsumerApi.DEBUGCLIENTKEY = jsonResponse.getString("clientKey");

						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devclientkey), jsonResponse.getString("clientKey"));
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devclientdevid), jsonResponse.getString("deviceId"));
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devclientkeyexp), jsonResponse.getString("expiresAt"));


					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Util.showToast(LoginActivity.this, "Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),Util.TOAST_TYPE_ERROR);
//						Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),LoginActivity.this);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	private void CheckUserStatus(){

		SetDeviceDetails();


		ConsumerApi.DOMAIN=getString(R.string.domain_name);

		String clientKey=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
				getString(R.string.devclientkey));
		if(clientKey != null ){
			ConsumerApi.DEBUGCLIENTKEY = clientKey;
		}
		String clientKeyExp=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
				getString(R.string.devclientkeyexp));

		//Check if client is available, if not give device registration request
		if(clientKey!=null)
		{
			mUserInfo.firstVisitStatus=false;

			long nowinms=System.currentTimeMillis();
			Date now=new Date(nowinms);
			mUserInfo.lastVisitedDate=now.toLocaleString();


			//check if the client key is valid or not, if expired give generate key request
			if(Util.isTokenValid(clientKeyExp))
			{
				mDevInfo.setClientKey(clientKey);
				mDevInfo.setClientDeviceId(SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
						getString(R.string.devclientdevid)));
				mDevInfo.setClientKeyExp(SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
						getString(R.string.devclientkeyexp)));
				Log.d(TAG, "---------------------------------------------------------");

				String username=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
						getString(R.string.devusername));


				//check if user is already logged in, if so take him to main screen or else login screen
				if(username!=null)
				{
					Crashlytics.setUserEmail(username);
					
					String userIdSha1=Util.sha1Hash(username);
					FlurryAgent.setUserId(userIdSha1);
					Crashlytics.setUserName(userIdSha1);
					Crashlytics.setUserIdentifier(userIdSha1);
					
					
					String profilename=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
							getString(R.string.userprofilename));
					String profilePic=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
							getString(R.string.userpic));
					
					if(profilename!=null)
						mUserInfo.setName(profilename);
					if(profilePic!=null)
						mUserInfo.setProfilePic(profilePic);
					mUserInfo.setUserEmail(username);
					
					finish();
					Util.launchMainActivity(LoginActivity.this);
				}

			}
			else
			{
				//Generatey new Key
				String devId=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
						getString(R.string.devclientdevid));

				Map<String, String> params = new HashMap<String, String>();
				params.put("deviceId", devId);

				Util.genKeyRequest(LoginActivity.this,getString(R.string.genKeyReqPath),params);
			}
		}
		else
		{

			mUserInfo.firstVisitStatus=true;
			Log.d(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			Map<String, String> params = new HashMap<String, String>();
			params.put("serialNo", mDevInfo.getDeviceSNo());
			params.put("os", mDevInfo.getDeviceOs());
			params.put("osVersion", mDevInfo.getDeviceOsVer());
			params.put("make",mDevInfo.getDeviceMake());
			params.put("model", mDevInfo.getDeviceModel());
			params.put("resolution", mDevInfo.getDeviceRes());
			params.put("profile", "work");
			params.put("clientSecret", getString(R.string.clientsecret));

			showProgressBar();

			//Util.showToast(mDevInfo.getDeviceSNo(),this);

			devRegRequest(getString(R.string.devRegPath),params);

		}		
	}


	@Override
	public void onTwitterLogin(String token, String secret) {
		mUserInfo.setLoginStatus(true);

		String clientKeyExp=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
				getString(R.string.devclientkeyexp)); 
		
		SharedPrefUtils.writeToSharedPref(LoginActivity.this,
				getString(R.string.userprofilename), mUserInfo.getName());
		SharedPrefUtils.writeToSharedPref(LoginActivity.this,
				getString(R.string.userpic), mUserInfo.getProfilePic());
		SharedPrefUtils.writeToSharedPref(LoginActivity.this,
				getString(R.string.devusername), mUserInfo.getUserEmail());

		Log.d(TAG, "Twitter User Id:   "+mUserInfo.getUserId());
		Map<String, String> params = new HashMap<String, String>();
		params.put("twitterId", mUserInfo.getUserId());
		params.put("profile", "work");
		params.put("authToken", token);
		params.put("tokenExpiry",clientKeyExp);
		params.put("clientKey",mDevInfo.getClientKey());
		facebookLoginRequest("social/login/Twitter", params);

	}
	
	protected void twitterLoginRequest(String contextPath,
			final Map<String, String> bodyParams) {

		RequestQueue queue = MyVolley.getRequestQueue();

		if(mProgressDialog!=null)
			mProgressDialog.setMessage("Logging in, Please wait...");

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				twitterLoginSuccessListener(),
				twitterLoginErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		myReq.setShouldCache(false);
		Log.d(TAG,"Request sent ");
		queue.add(myReq);

	}
	protected ErrorListener twitterLoginErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Analytics.endTimedEvent(Analytics.loginTwitter);
				Map<String,String> params=new HashMap<String, String>();
				params.put("Status", "Failure");
				params.put("Msg", error.toString());
				Analytics.trackEvent(Analytics.loginTwitter,params);
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					sendNotification(getString(R.string.loginerr));
				}
				else
				{
					sendNotification(error.toString());	
				}
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> twitterLoginSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				Analytics.endTimedEvent(Analytics.loginTwitter);
				
				try {	
					dismissProgressBar();
					
					
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);
					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Success");
						Analytics.trackEvent(Analytics.loginTwitter,attribs);
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");
						//sendNotification(jsonResponse.getString("message"));
						finish();
						Util.launchMainActivity(LoginActivity.this);
					}
					else
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Failure");
						attribs.put("Msg", jsonResponse.getString("code"));
						Analytics.trackEvent(Analytics.loginTwitter,attribs);
						if(jsonResponse.getString("code").equalsIgnoreCase("401"))
						{
							String devId=SharedPrefUtils.getFromSharedPreference(LoginActivity.this,
									getString(R.string.devclientdevid));

							Map<String, String> params = new HashMap<String, String>();
							params.put("deviceId", devId);

							Util.genKeyRequest(LoginActivity.this,getString(R.string.genKeyReqPath),params);
							sendNotification("Err: "+jsonResponse.getString("code")+" \nErr Msg: "+jsonResponse.getString("message"));
						}
						else
						{
							Log.d(TAG, "code: "+jsonResponse.getString("code"));
							Log.d(TAG, "message: "+jsonResponse.getString("message"));

							if(Session.getActiveSession()!=null)
								Session.getActiveSession().close();
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	
}
