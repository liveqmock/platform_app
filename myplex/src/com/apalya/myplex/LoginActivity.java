package com.apalya.myplex;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.utils.FontUtil;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;



public class LoginActivity extends BaseActivity implements OnClickListener {



	private EditText mEmailField,mPwdField;
	private Button mFacebookButton,mSignup,mLogin,mGoogleLogin;
	private RelativeLayout mSocialLogins;
	private TextView mLetMeIn,mText;
	private ImageView mLogo;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	//private LinearLayout mUserFields;
	private static final String TAG = "BaseActivity";
	Animation mAnimationFadeIn;
	Animation mAnimationFadeOut;
	Animation mAnimationLinear;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();
		setContentView(R.layout.loginscreen);

		mEmailField = (EditText)findViewById(R.id.editEmail);
		//mEmailField.setFocusable(false);

		mGoogleLogin = (Button)findViewById(R.id.googlelogin);
		mGoogleLogin.setOnClickListener(this);

		mFacebookButton = (Button) findViewById(R.id.fblogin);
		mFacebookButton.setOnClickListener(this);

		mSignup = (Button) findViewById(R.id.signup);

		mText=(TextView)findViewById(R.id.Msg);

		mLetMeIn=(TextView)findViewById(R.id.letmeinMsg);
		mLetMeIn.setTypeface(FontUtil.Roboto_Medium);

		mPwdField =(EditText) findViewById(R.id.editPassword);





		mLogo = (ImageView)findViewById(R.id.logo);
		//mLogo.setAnimation(mAnimationLinear);


		mLetMeIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mLetMeIn, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				finish();
				launchActivity(CardExplorer.class,LoginActivity.this , null);

			}
		});

		/*mAnimationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		mAnimationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		mAnimationLinear = AnimationUtils.loadAnimation(this, R.anim.linear);*/

		//Check if Keyboard is visible or not
		final View root= findViewById(R.id.rootlayout);  
		root.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int heightDiff = root.getRootView().getHeight() - root.getHeight();

				Rect rectgle= new Rect();
				Window window= getWindow();
				window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
				int contentViewTop= 
						window.findViewById(Window.ID_ANDROID_CONTENT).getTop();

				if(heightDiff <= contentViewTop){
					//Soft KeyBoard Hidden
					mFacebookButton.setVisibility(View.VISIBLE);
					//mFacebookButton.setAnimation(mAnimationFadeIn);
					mGoogleLogin.setVisibility(View.VISIBLE);
					mText.setVisibility(View.VISIBLE);
					mLetMeIn.setVisibility(View.VISIBLE);
				}else{
					//Soft KeyBoard Shown
					mLetMeIn.setVisibility(View.GONE);
					mText.setVisibility(View.GONE);
					mFacebookButton.setVisibility(View.GONE);
					mGoogleLogin.setVisibility(View.GONE);
				}
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
				//mLogin.startAnimation(out);
				//mLogin.startAnimation(in);
				// TODO Auto-generated method stub
				if( mEmailField.getText().toString().length() == 0 )
					mEmailField.setError( "Username is required!" );
				if( mPwdField.getText().toString().length() == 0 )
					mPwdField.setError( "Password is required!" );

				if(mEmailField.getText().toString().length() > 0 &&  mPwdField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@")&& mEmailField.getText().toString().contains("."))
					{
						finish();
						launchActivity(CardExplorer.class,LoginActivity.this , null);
					}
					else
					{
						mPwdField.clearFocus();
						mEmailField.requestFocus();
						mEmailField.setError( "Enter Valid Email!" );
						showToast("Enter Valid Email!");
					}

				}
			}
		});

		mSignup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mSignup, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();

				if(mEmailField.getText().toString().length() > 0 &&  mEmailField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@"))
					{

						Map<String, String> intentBundle = new HashMap<String, String>();
						String intentParam1 = "username";
						String intentParam2 = "userpwd";
						intentBundle.put(intentParam1,mEmailField.getText().toString() );
						intentBundle.put(intentParam2,  mEmailField.getText().toString());
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



	}

	public void showSoftKeyboard(View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}


	@Override
	public void onConnected(Bundle connectionHint) {
		if (mConnectionProgressDialog.isShowing()) 
			mConnectionProgressDialog.dismiss();
		//String accountName = mPlusClient.getAccountName();
		// Retrieve the oAuth 2.0 access token.
		final Context context = this.getApplicationContext();
		AsyncTask task = new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				String scope = "oauth2:" + Scopes.PLUS_LOGIN;
				try {
					// We can retrieve the token to check via
					// tokeninfo or to pass to a service-side
					// application.
					String token = GoogleAuthUtil.getToken(context,
							mPlusClient.getAccountName(), scope);
					Log.d(TAG, token);
				} catch (UserRecoverableAuthException e) {
					// This error is recoverable, so we could fix this
					// by displaying the intent to the user.
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (GoogleAuthException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}
		};
		task.execute((Void) null);
        
		finish();
		launchActivity(CardExplorer.class,this , null);

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
		// TODO Auto-generated method stub
		super.onResume();
		if(Session.getActiveSession().isOpened() || mPlusClient.isConnected())
		{
			String token =Session.getActiveSession().getAccessToken();
			showToast(token);
			Log.d(TAG,token);
			finish();
			launchActivity(CardExplorer.class,LoginActivity.this , null);
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
			String token =session.getAccessToken();
			showToast(token);
			finish();
			launchActivity(CardExplorer.class,LoginActivity.this , null);
		} else {
			mFacebookButton.setText("LogIn with Facebook");
		}
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
		// TODO Auto-generated method stub

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
		if (view.getId() == R.id.googlelogin && !mPlusClient.isConnected()) {
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
}
