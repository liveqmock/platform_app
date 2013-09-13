package com.apalya.myplex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apalya.myplex.utils.FontUtil;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;


public class LoginActivity extends BaseActivity implements OnClickListener {

	private EditText mEmailField,mPwdField;
	private Button mFacebookButton,mSignup,mLogin,mGoogleLogin;
	private TextView mLetMeIn,mText,mForgetMsg;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private LinearLayout mUserFields,mOptionFields;
	private ImageView mTitleIcon;
	private static final String TAG = "LoginActivity";
	private boolean mAnimateStatus=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();
		setContentView(R.layout.loginscreen);

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
				launchActivity(CardExplorer.class,LoginActivity.this , null);

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

						if(heightDiff <= contentViewTop ){
							
							if(mAnimateStatus)
							{
								
								//Soft KeyBoard Hidden
								RunSlideDownAnimation() ;
								
								 
							    mFacebookButton.setVisibility(View.VISIBLE);
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
								fadeAnim5.start();
							}
							else
							{
								//mAnimateStatus=true;
							}
						}else{
							
							mAnimateStatus=true;
							//Soft KeyBoard Shown
							mTitleIcon.setVisibility(View.GONE);
							mLetMeIn.setVisibility(View.GONE);
							mText.setVisibility(View.GONE);
							mFacebookButton.setVisibility(View.GONE);
							mGoogleLogin.setVisibility(View.GONE);
							RunAnimation();
						}
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
	
	
	
	/*private void hideKeypad() {
	       InputMethodManager imm = (InputMethodManager) 
	        getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
	}*/

	@Override
	public void onConnected(Bundle connectionHint) {

		super.onConnected(connectionHint);
		if (mConnectionProgressDialog.isShowing()) 
			mConnectionProgressDialog.dismiss();
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
		getAndUseAuthTokenInAsyncTask();
		finish();
		launchActivity(CardExplorer.class,this , null);

	}

	private void RunSlideDownAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, -500,0 );   
	    slide.setDuration(1000);   
	    slide.setFillAfter(true);   
	    mUserFields.startAnimation(slide);
	    mUserFields.startAnimation(slide);
	    mForgetMsg.startAnimation(slide);
	    mOptionFields.startAnimation(slide);
	    //mText.startAnimation(slide);
	    mLetMeIn.startAnimation(slide);
	}
	
	private void RunAnimation() 
	{
		TranslateAnimation slide = new TranslateAnimation(0, 0, 500,0 );   
	    slide.setDuration(1000);   
	    slide.setFillAfter(true);   
	    mUserFields.startAnimation(slide);
	    mForgetMsg.startAnimation(slide);
	    mOptionFields.startAnimation(slide);
	    
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
			String token =Session.getActiveSession().getAccessToken();
			//showToast(token);
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
			//String token =session.getAccessToken();
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

			/* final int errorCode = GooglePlayServicesUtil.checkGooglePlusApp(this);
		      if (errorCode == GooglePlusUtil.SUCCESS) {
		      }else {
		          // Prompt the user to install the Google+ app.
		          GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
		      }*/


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
