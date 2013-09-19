package com.apalya.myplex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R.color;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;





public class LoginActivity extends BaseActivity implements OnClickListener {

	static EditText mEmailField;
	static EditText mPwdField;
	
	private static String googleToken = null;
	private Button mFacebookButton,mSignup,mLogin,mGoogleLogin;
	private TextView mLetMeIn,mText,mForgetMsg;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private LinearLayout mUserFields,mOptionFields;
	private ImageView mTitleIcon;
	private static final String TAG = "LoginActivity";
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;
	private boolean mAnimateStatus=false;
	private View mRootLayout;
	protected String fbUserId;
	private ProgressBar loader;
	private int mFacebookReqSent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();
		setContentView(R.layout.loginscreen);
		
		prepareSlideNotifiation();
		loader = (ProgressBar) findViewById(R.id.loading);
		
		findViewById(R.id.okalert).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hideNotification();
			}
		});

		mUserFields = (LinearLayout)findViewById(R.id.userfields);
 		mOptionFields = (LinearLayout) findViewById(R.id.linearLayout1);
 		mTitleIcon = (ImageView) findViewById(R.id.logo);
		mEmailField = (EditText)findViewById(R.id.editEmail);
		mGoogleLogin = (Button)findViewById(R.id.googlelogin);
		mGoogleLogin.setOnClickListener(this);

		mFacebookButton = (Button) findViewById(R.id.fblogin);
		mFacebookButton.setOnClickListener(this);

		mSignup = (Button) findViewById(R.id.signup);

		mText=(TextView)findViewById(R.id.Msg);
		mForgetMsg = (TextView)findViewById(R.id.forgetPwdMsg);
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
						//launchActivity(CardExplorer.class,LoginActivity.this , null);
					}
					else
					{
						sendNotification("Hey, you might have missed to enter valid mail id!");
						mEmailField.setError( "Enter Valid Email!" );
						Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
						mEmailField.startAnimation(shake);
					}

				}
				
				
			}
		});

		mLetMeIn=(TextView)findViewById(R.id.letmeinMsg);
		mLetMeIn.setTypeface(FontUtil.Roboto_Medium);

		mPwdField =(EditText) findViewById(R.id.editPassword);
		
		mLetMeIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLetMeIn, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				finish();
				launchActivity(MainActivity.class,LoginActivity.this , null);

			}
		});

		mPwdField.setTypeface(FontUtil.Roboto_Regular);
		mEmailField.setTypeface(FontUtil.Roboto_Regular);
		mLogin = (Button) findViewById(R.id.login);
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
						loader.setVisibility(View.VISIBLE);
						userLoginRequest(getString(R.string.signin), params);
						//finish();
						//launchActivity(CardExplorer.class,LoginActivity.this , null);
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
					sendNotification("Hey, you might have missed some fields!");
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
						launchActivity(SignUpActivity.class,LoginActivity.this , intentBundle);

						/*Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
						startActivity(intent);*/	
					}
					else
					{
						finish();
						launchActivity(SignUpActivity.class,LoginActivity.this , null);
					}

				}
				else
				{
					finish();
					launchActivity(SignUpActivity.class,LoginActivity.this , null);	
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
					.setPermissions(Arrays.asList("basic_info","email"))
					.setCallback(statusCallback));
				} else {
					Session.openActiveSession(this, true, statusCallback);
				}
			}

		}
		else
		{

		}

		//Check if Keyboard is visible or not
		mRootLayout = findViewById(R.id.rootlayout);  
		mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int heightDiff = mRootLayout.getRootView().getHeight() - mRootLayout.getHeight();

						Rect rectgle= new Rect();
						Window window= getWindow();
						window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
						int contentViewTop= 
								window.findViewById(Window.ID_ANDROID_CONTENT).getTop();

						if(heightDiff <= contentViewTop ){
							
							if(mAnimateStatus)
							{
								//mTitleIcon.setVisibility(View.VISIBLE);
								//Soft KeyBoard Hidden
								//RunSlideDownAnimation() ;
								
								 
							    /*mFacebookButton.setVisibility(View.VISIBLE);
								mGoogleLogin.setVisibility(View.VISIBLE);
								mText.setVisibility(View.VISIBLE);
								mLetMeIn.setVisibility(View.VISIBLE);
								mTitleIcon.setVisibility(View.VISIBLE);
								
								ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mFacebookButton, "alpha", 0.1f, 1f);
								fadeAnim2.setDuration(2000);
								fadeAnim2.start();
								ValueAnimator fadeAnim3 = ObjectAnimator.ofFloat(mGoogleLogin, "alpha", 0.0f, 1f);
								fadeAnim3.setDuration(2000);
								fadeAnim3.start();
								
								ValueAnimator fadeAnim4 = ObjectAnimator.ofFloat(mText, "alpha", 0.0f, 1f);
								fadeAnim4.setDuration(2000);
								fadeAnim4.start();
								
								ValueAnimator fadeAnim5 = ObjectAnimator.ofFloat(mTitleIcon, "alpha", 0.0f, 1f);
								fadeAnim5.setDuration(2000);
								fadeAnim5.start();*/
							}
							else
							{
								//mAnimateStatus=true;
							}
						}else{
							
							mAnimateStatus=true;
							//mTitleIcon.setVisibility(View.GONE);
							//Soft KeyBoard Shown
							/*mTitleIcon.setVisibility(View.GONE);
							mLetMeIn.setVisibility(View.GONE);
							mText.setVisibility(View.GONE);
							mFacebookButton.setVisibility(View.GONE);
							mGoogleLogin.setVisibility(View.GONE);*/
							//RunAnimation();
						}
					}
				});

	}

	
	
	private void prepareSlideNotifiation() {
		mSlideNotificationLayout = (RelativeLayout)findViewById(R.id.slidenotificationlayout);
		mSlideNotificationText = (TextView)findViewById(R.id.slidenotificationtextview);
		mSlideNotifcationHeight = (int) getResources().getDimension(R.dimen.slidenotificationwithbutton);
		mSlideNotificationLayout.setY(-mSlideNotifcationHeight);
		mSlideNotificationLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				hideNotification();				
			}
		});
	}
	
	protected void userLoginRequest(String contextPath, final Map<String, String> bodyParams) {
		// TODO Auto-generated method stub
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
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> userLoginSuccessListener() {
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
								getString(R.string.devusername), LoginActivity.mEmailField.getText().toString());
						SharedPrefUtils.writeToSharedPref(LoginActivity.this,
								getString(R.string.devpassword), LoginActivity.mPwdField.getText().toString());
						
						finish();
						launchActivity(MainActivity.class,LoginActivity.this , null);
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						sendNotification("Err: "+jsonResponse.getString("code")+" "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
	
	private void showNotification(){
		animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2);
		
		
	}
	private void hideNotification(){
		animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2);
		
		/*finish();
		launchActivity(CardExplorer.class, SplashActivity.this, null);*/
	}
	private void sendNotification(final String aMsg){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			public void run() {
				mSlideNotificationText.setText(aMsg);
				showNotification();
			}
		});
	}
	
		
	private void hideKeypad() {
	       InputMethodManager imm = (InputMethodManager) 
	        getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(mRootLayout.getWindowToken(), 0);
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		//super.onConnected(connectionHint);
		if (mConnectionProgressDialog.isShowing()) 
		{
			mConnectionProgressDialog.dismiss();
			
		}
		
		/*mUserInfo.setLoginStatus(true);
		mUserInfo.setName(mPlusClient.getCurrentPerson().getDisplayName());
		mUserInfo.setProfilePic(mPlusClient.getCurrentPerson().getImage().getUrl());
*/		
		AsyncTask<Object, Void, String> mAuthTask = new AsyncTask<Object, Void, String>() {
			@Override
			protected String doInBackground(Object... o) {
				return authenticate(LoginActivity.this, mPlusClient.getAccountName(),mPlusClient.getCurrentPerson().getId(),mDevInfo.getClientKeyExp(),mDevInfo.getClientKey());
			}

			@Override
			protected void onPostExecute(String result) {
				
			}
		};

		mAuthTask.execute();
		
		finish();
		launchActivity(MainActivity.class,LoginActivity.this , null);
		//getAndUseAuthTokenInAsyncTask();
			/*String accountName = mPlusClient.getAccountName();
		String pic = mPlusClient.getCurrentPerson().getImage().getUrl();
		Log.d(LTAG, "IMAGE:::: ::: "+pic);


		if(pic.length()>0)
		{
		FadeInNetworkImageView Image = (FadeInNetworkImageView)findViewById(R.id.logo);
		Image.setImageUrl(pic, MyVolley.getImageLoader());
		}
		else
		{
			showToast("No URL");

		}*/
		
		//finish();
		//launchActivity(CardExplorer.class,this , null);

	}

	private void RunSlideDownAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, -100,0 );   
	    slide.setDuration(300);   
	    slide.setFillAfter(false);   
	    mRootLayout.setAnimation(slide);
	    /*mUserFields.startAnimation(slide);
	    mUserFields.startAnimation(slide);
	    mForgetMsg.startAnimation(slide);
	    mOptionFields.startAnimation(slide);
	    //mText.startAnimation(slide);
	    mLetMeIn.startAnimation(slide);*/
	}
	
	private void RunAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, 100,0 );   
	    slide.setDuration(300);   
	    slide.setFillAfter(false);   
	    mRootLayout.setAnimation(slide);
	    /*mUserFields.startAnimation(slide);
	    mForgetMsg.startAnimation(slide);
	    mOptionFields.startAnimation(slide);*/
	    
	}
	
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
		if(Session.getActiveSession().isOpened() || mPlusClient.isConnected())
		{
			finish();
			launchActivity(MainActivity.class,LoginActivity.this , null);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		super.onCreateOptionsMenu(menu);   

		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		//mPlusClient.disconnect();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			
			updateView();
		}
	}

	private void updateView() {
		
		Session session = Session.getActiveSession();
		if (session.isOpened() && !mPlusClient.isConnected()) {
			//String token =session.getAccessToken();
			final String token =Session.getActiveSession().getAccessToken();
			final java.util.Date tokenExpiry=Session.getActiveSession().getExpirationDate();
			
			//Session.getActiveSession().g
			//showToast(token);
			Log.d(TAG,token);
			Log.d(TAG,tokenExpiry.toGMTString());
			
			
			Request request = Request.newMeRequest(Session.getActiveSession(), new GraphUserCallback() {

			   	@Override
				public void onCompleted(GraphUser user,
						com.facebook.Response response) {
					// TODO Auto-generated method stub
			   		fbUserId=user.getId();
			   		if(mFacebookReqSent==0)
					{
			   		
			   			mUserInfo.setName(user.getName());
			   			mUserInfo.setLoginStatus(true);
			   			//mUserInfo.setProfilePic(user.)
			   			
					Log.d(TAG, "Facebook User Id:   "+fbUserId);
					Map<String, String> params = new HashMap<String, String>();
					params.put("facebook_id", fbUserId);
					params.put("profile", "work");
					params.put("auth_token", token);
					params.put("token_expiry", tokenExpiry.toGMTString());
					params.put("clientKey",mDevInfo.getClientKey());
					facebookLoginRequest(getString(R.string.fbloginpath), params);
					}
					
				}
			});
			request.executeAsync();
			
			
		} else {
			
		}
	}

	protected void facebookLoginRequest(String contextPath,
			final Map<String, String> bodyParams) {
		RequestQueue queue = MyVolley.getRequestQueue();
		 
		mFacebookReqSent=1;
		
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
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
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
						launchActivity(MainActivity.class,LoginActivity.this , null);
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						sendNotification("Err: "+jsonResponse.getString("code")+" "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}


	private void onClickLogin() {
		/*Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}*/


		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
			.setPermissions(Arrays.asList("basic_info","email"))
			.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}


	}

	/*private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}*/


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
		else if (view.getId() == R.id.googlelogin && !mPlusClient.isConnected()) {

			if (mConnectionResult == null) {
				mConnectionProgressDialog.show();
			} else {
				try {
					mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					// Try connecting again.
					mConnectionResult = null;
					mPlusClient.connect();

				}
			}
		}
	}
	
	
	
	public static String authenticate(Context ctx, String account,String id, String expiry,String clientKey) {
        HttpURLConnection urlConnection = null;
        OutputStream outStream = null;
        String response = null;
        int statusCode = 0;

        try {
            URL url = new URL(ctx.getString(R.string.url) + ctx.getString(R.string.gplusloginpath));

        	googleToken = GoogleAuthUtil.getToken(ctx, account, SCOPE_STRING);
            
            Log.v(TAG, "Authenticating at [" + url + "] with: " + googleToken);
            
            URLConnection connection =url.openConnection();
         // Http Method becomes POST
         connection.setDoOutput(true);

         // Encode according to application/x-www-form-urlencoded specification
         String content =
             "clientKey=" + URLEncoder.encode (clientKey) +
             "&google_id=" + URLEncoder.encode (id) +
             "&auth_token=" + URLEncoder.encode (googleToken) +
             "&token_expiry=" + URLEncoder.encode (expiry);
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 

         // Try this should be the length of you content.
         // it is not neccessary equal to 48. 
         // content.getBytes().length is not neccessarily equal to content.length() if the String contains non ASCII characters.
         //connection.setRequestProperty("Content-Length", content.getBytes().length); 

         // Write body
         OutputStream output = connection.getOutputStream(); 
         output.write(content.getBytes());
         response=output.toString();
         output.close();
         
         return response;
         
         
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (GoogleAuthException e) {
            GoogleAuthUtil.invalidateToken(ctx, googleToken);
        } 

        return null;
    }
}
