package com.apalya.myplex;



import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class SignUpActivity extends BaseActivity{

	private Button mSubmit;
	private EditText mEmail,mPhone,mPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().hide();
		
		setContentView(R.layout.signupscreen);
		
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

		mPhone = (EditText) findViewById(R.id.enterPhone);
		mPassword = (EditText) findViewById(R.id.editsPassword);
		mPassword.setText(userpwd);
		
		mSubmit = (Button) findViewById(R.id.submit);
		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(findViewById(v.getId()), "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				
				if((mEmail.getText().toString().length()>0 || mPhone.getText().toString().length()>0)&&mPassword.getText().toString().length()>0) {
					
					if(mEmail.getText().toString().contains("@") && mEmail.getText().toString().contains(".")&& isPhoneNoValid(mPhone.getText().toString()))
					{
						finish();
						launchActivity(CardExplorer.class,SignUpActivity.this, null);
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
						showToast("Enter Valid details");
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
					showToast("Hey, Missed something, please check!!!");
				}
			}
		});
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
}
