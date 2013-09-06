package com.apalya.myplex;



import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class SignUpActivity extends BaseActivity{

	private Button mSubmit;
	private EditText mEmail,mPhone,mPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

			}
		});


	}
}
