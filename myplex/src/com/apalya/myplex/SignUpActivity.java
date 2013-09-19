package com.apalya.myplex;



import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SignUpActivity extends BaseActivity{

	private Button mSubmit;
	private EditText mEmail,mPhone,mPassword;
	private static final String TAG = "SignUpActivity";
	private RelativeLayout mSlideNotificationLayout;
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().hide();

		setContentView(R.layout.signupscreen);

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

		mEmail = (EditText) findViewById(R.id.enterEmail);
		mEmail.setText(username);
		mEmail.setTypeface(FontUtil.Roboto_Regular);

		findViewById(R.id.okalert).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hideNotification();
			}
		});


		mPhone = (EditText) findViewById(R.id.enterPhone);
		mPhone.setTypeface(FontUtil.Roboto_Regular);
		mPassword = (EditText) findViewById(R.id.editsPassword);
		mPassword.setText(userpwd);
		mPassword.setTypeface(FontUtil.Roboto_Regular);
		
		EditText mFullName=(EditText)findViewById(R.id.editFullName);
		mFullName.setTypeface(FontUtil.Roboto_Regular);

		mSubmit = (Button) findViewById(R.id.submit);
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
					// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
		launchActivity(LoginActivity.class,SignUpActivity.this, null);
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

private void RegisterUserReq(String contextPath, final Map<String,String> bodyParams) {
		
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
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}

	protected Listener<String> RegisterUserSuccessListener() {
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
						launchActivity(MainActivity.class,SignUpActivity.this , null);
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
}
