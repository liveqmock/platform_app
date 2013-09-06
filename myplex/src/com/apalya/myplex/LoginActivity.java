package com.apalya.myplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;

import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements 
ConnectionCallbacks, OnConnectionFailedListener, OnClickListener {

	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private EditText mEmailField,mPwdField;
	private Button mFacebookButton,mSignup,mLogin;
	private RelativeLayout mTitle,mSocialLogins;
	private TextView mLetMeIn,mText;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginscreen);

		
		
		mEmailField = (EditText)findViewById(R.id.editEmail);
		findViewById(R.id.googlelogin).setOnClickListener(this);
		mFacebookButton = (Button) findViewById(R.id.fblogin);
		mSignup = (Button) findViewById(R.id.signup);
		mFacebookButton.setOnClickListener(this);
		mText=(TextView)findViewById(R.id.Msg);
		mLetMeIn=(TextView)findViewById(R.id.letmeinMsg);
		mLetMeIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launchActivity(CardExplorer.class,LoginActivity.this , null);
				
			}
		});

		mTitle=(RelativeLayout) findViewById(R.id.titleLayout);
		mSocialLogins=(RelativeLayout)findViewById(R.id.sociallogins);
		
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
	            	
	            	mTitle.setVisibility(View.VISIBLE);
					mSocialLogins.setVisibility(View.VISIBLE);
					mLetMeIn.setVisibility(View.VISIBLE);
					mText.setVisibility(View.VISIBLE);
	            }else{
	                //Soft KeyBoard Shown
	            	
	            	
	            	mTitle.setVisibility(View.GONE);
					mSocialLogins.setVisibility(View.GONE);
					mLetMeIn.setVisibility(View.GONE);
					mText.setVisibility(View.GONE);
	            }


	         }
	    });
		
		
		mPwdField =(EditText) findViewById(R.id.editPassword);
		mPlusClient = new PlusClient.Builder(this, this, this)
		.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
		.build();
		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");


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
				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
			}
		}
		else
		{
			updateView();
		}


		mLogin = (Button) findViewById(R.id.login);
		mLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mEmailField.getText().toString().length() == 0 )
					mEmailField.setError( "Username is required!" );
				if( mPwdField.getText().toString().length() == 0 )
					mPwdField.setError( "Password is required!" );

				if(mEmailField.getText().toString().length() > 0 &&  mPwdField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@"))
					{

						Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
						startActivity(intent);	
					}
					else
					{
						mPwdField.clearFocus();
						mEmailField.requestFocus();
						mEmailField.setError( "Enter Valid Email!" );
					}

				}
			}
		});

		mSignup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				

				if(mEmailField.getText().toString().length() > 0 &&  mEmailField.getText().toString().length()>0)
				{
					if(mEmailField.getText().toString().contains("@"))
					{

						Map<String, String> intentBundle = new HashMap<String, String>();
						String intentParam1 = "username";
						String intentParam2 = "userpwd";
						intentBundle.put(intentParam1,mEmailField.getText().toString() );
						intentBundle.put(intentParam2,  mEmailField.getText().toString());
						launchActivity(SignUpActivity.class,LoginActivity.this , intentBundle);

						/*Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
						startActivity(intent);*/	
					}
					else
					{
						launchActivity(SignUpActivity.class,LoginActivity.this , null);
					}

				}
				else
				{
					launchActivity(SignUpActivity.class,LoginActivity.this , null);	
				}
				
				
				
				
				
				/*Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivity(intent);*/

			}
		});

		/*mPwdField.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) 
					if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
						// Process the entered text here
						if( mEmailField.getText().toString().length() == 0 )
							mEmailField.setError( "Username is required!" );
						if( mPwdField.getText().toString().length() == 0 )
							mPwdField.setError( "Password is required!" );

						if(mEmailField.getText().toString().length() > 0 &&  mPwdField.getText().toString().length()>0)
						{
							if(mEmailField.getText().toString().contains("@"))
							{

								Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
								startActivity(intent);	
							}
							else
							{
								mPwdField.clearFocus();
								mEmailField.requestFocus();
								mEmailField.setError( "Enter Valid Email!" );
							}

						}
						return true;
					}
				return false;
			}
		});*/

	}
	
	

	@Override
	protected void onStart() {
		super.onStart();
		mPlusClient.connect();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
		Session.getActiveSession().removeCallback(statusCallback);
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
					mPlusClient.connect();
				}
			}
		}
		// Save the result and resolve the connection failure upon a user click.
		mConnectionResult = result;
	}

	private void sendRequestDialog() {
		Bundle params = new Bundle();

		params.putString("message", "Learn how to make your Android apps social");
		params.putString("data",
				"{\"badge_of_awesomeness\":\"1\"," +
				"\"social_karma\":\"5\"}");
		WebDialog requestsDialog = (
				new WebDialog.RequestsDialogBuilder(LoginActivity.this,
						Session.getActiveSession(),
						params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								if (error != null) {
									if (error instanceof FacebookOperationCanceledException) {
										Toast.makeText(LoginActivity.this.getApplicationContext(), 
												"Request cancelled", 
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(LoginActivity.this.getApplicationContext(), 
												"Network Error", 
												Toast.LENGTH_SHORT).show();
									}
								} else {
									final String requestId = values.getString("request");
									if (requestId != null) {
										Toast.makeText(LoginActivity.this.getApplicationContext(), 
												"Request sent",  
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(LoginActivity.this.getApplicationContext(), 
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
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
			mConnectionResult = null;
			mPlusClient.connect();
		}else
		{
			Session.getActiveSession().onActivityResult(this, requestCode, responseCode, intent);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {

			mFacebookButton.setText("Invite Friends");
			launchActivity(CardExplorer.class,LoginActivity.this , null);
			/*       textInstructionsOrLink.setText(URL_PREFIX_FRIENDS + session.getAccessToken());
            buttonLoginLogout.setText(R.string.logout);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });*/
		} else {
			mFacebookButton.setText("LogIn with Facebook");
			/* textInstructionsOrLink.setText(R.string.instructions);
            buttonLoginLogout.setText(R.string.login);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });*/
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mConnectionProgressDialog.dismiss();
		String accountName = mPlusClient.getAccountName();
		Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
		launchActivity(CardExplorer.class,LoginActivity.this , null);
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
		boolean status = mPlusClient.isConnected();
		Toast.makeText(this, status + " is connected.", Toast.LENGTH_LONG).show();
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
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

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			updateView();
		}
	}
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

		if (view.getId() == R.id.fblogin){

			if(Session.getActiveSession().isOpened())
			{
				//onClickLogout();
				sendRequestDialog();
			}
			else
			{
				onClickLogin();				
			}
			/*Intent intent = new Intent(LoginActivity.this, FacebookLoginFragment.class);
			startActivity(intent);*/
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
		/*if(view.getId() == R.id.sign_out_button){
			Log.d(TAG,"Sign Out cliked");
			if (mPlusClient.isConnected()) {
				Log.d(TAG,"Sign Out Done");
				mPlusClient.clearDefaultAccount();
				mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
					@Override
					public void onAccessRevoked(ConnectionResult status) {
						// mPlusClient is now disconnected and access has been revoked.
						// Trigger app logic to comply with the developer policies
					}
				});

				mPlusClient.disconnect();
				//mPlusClient.connect();
			}

		}*/

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
}
