package com.apalya.myplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.slidemenudata;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.views.FliterMenu;
import com.apalya.myplex.views.slidemenuadapter;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class BaseActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener{
	private boolean mSlideInMenuToggle = false;
	private boolean mFilterMenuToggle = false,mSearchToggle = false;;
	private slidemenuadapter mSlidemenuAdapter;
	private int mSlideInMenuWidth;
	private RelativeLayout mSlideMenuLayout;
	private ListView mSlideMenuList;
	private View mContentView;
	private RelativeLayout mRightFilterLayout,mDownFilterLayout;
	private TextView mRightFilterTitle,mDownFilterTitle;
	private ImageView mRightFilterButton,mDownFilterButton;
	protected PlusClient mPlusClient;
	protected ProgressDialog mConnectionProgressDialog;
	protected ConnectionResult mConnectionResult;
	private FliterMenu mFliterMenuLayout;
	private ListView mFliterListView;
	private slidemenuadapter mFliterMenuAdapter;

	private static final String TAG = "BaseActivity";
	protected static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!FontUtil.isFontsLoaded) {
			FontUtil.loadFonts(getAssets());
		}
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		ImageView button = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.menu_settings);
		mRightFilterLayout = (RelativeLayout) getActionBar().getCustomView()
				.findViewById(R.id.title_withfilterright);
		mDownFilterLayout = (RelativeLayout) getActionBar().getCustomView()
				.findViewById(R.id.title_withfilterdown);
		mRightFilterTitle = (TextView) getActionBar().getCustomView()
				.findViewById(R.id.applicationtitle_right);
		mDownFilterTitle = (TextView) getActionBar().getCustomView()
				.findViewById(R.id.applicationtitle_down);
		mRightFilterButton = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.menu_fliter_right);
		mDownFilterButton = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.menu_fliter_down);

		ImageView searchButton = (ImageView)getActionBar().getCustomView().findViewById(R.id.menu_search);

		mPlusClient = new PlusClient.Builder(this, this, this)
		.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
		.build();


		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");

		mRightFilterTitle.setTypeface(FontUtil.Roboto_Medium);
		mDownFilterTitle.setTypeface(FontUtil.Roboto_Medium);

		mDownFilterButton.setOnClickListener(mFliterClickListener);
		mRightFilterButton.setOnClickListener(mFliterClickListener);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!mSlideInMenuToggle) {
					showMenu();
				} else {
					hideMenu();
				}
				mSlideInMenuToggle = !mSlideInMenuToggle;

			}
		});

		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(!mSearchToggle)
				{
					mSearchToggle = true;
					Intent searchIntent = new Intent(BaseActivity.this, SearchActivity.class);
					startActivity(searchIntent);
				}
			}
		});
		//		prepareSlideMenu();
		prepareFilterMenu();
		enableSideFilter();
	}

	public void setFilterData(List<FilterMenudata> datalist) {
		if (mFliterMenuLayout == null) {
			enableSideFilter();
			mRightFilterButton.setVisibility(View.INVISIBLE);
			return;
		}
		mRightFilterButton.setVisibility(View.VISIBLE);
		mDownFilterButton.setVisibility(View.VISIBLE);
		mFliterMenuLayout.setData(datalist);
		mFliterMenuLayout
		.setonMenuItemSelectedListener(new com.apalya.myplex.views.FliterMenu.onMenuItemSelected() {

			@Override
			public void menuItemSelected(FilterMenudata data) {
				onFilterMenuItemSelected(data);
			}
		});

		mFliterMenuLayout
		.setonMenuOpened(new com.apalya.myplex.views.FliterMenu.onMenuOpened() {

			@Override
			public void menuOpened(boolean value) {
				if (value) {
					mRightFilterButton
					.setImageResource(R.drawable.navigation_collapse);
					mDownFilterButton
					.setImageResource(R.drawable.navigation_collapse);
					mFilterMenuToggle = true;
				} else {
					mRightFilterButton
					.setImageResource(R.drawable.navigation_expand);
					mDownFilterButton
					.setImageResource(R.drawable.navigation_expand);
					mFilterMenuToggle = false;
				}
			}
		});
	}

	public void onFilterMenuItemSelected(FilterMenudata data) {

	}

	private void prepareFilterMenu() {

		mFliterMenuLayout = (FliterMenu) findViewById(R.id.fliterMenuLayout);
		if (mFliterMenuLayout == null) {
			enableSideFilter();
			mRightFilterButton.setVisibility(View.INVISIBLE);
			return;
		}
		mFliterMenuLayout.init(this);
	}
	public OnClickListener mFliterClickListener =  new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!mFilterMenuToggle) {
				mRightFilterButton
				.setImageResource(R.drawable.navigation_collapse);
				mDownFilterButton
				.setImageResource(R.drawable.navigation_collapse);
				mFliterMenuLayout.show();
			} else {
				mRightFilterButton
				.setImageResource(R.drawable.navigation_expand);
				mDownFilterButton
				.setImageResource(R.drawable.navigation_expand);
				mFliterMenuLayout.hide();
			}
			mFilterMenuToggle = !mFilterMenuToggle;		
		}
	};
	public void enableSideFilter(){
		mRightFilterLayout.setVisibility(View.VISIBLE);
		mDownFilterLayout.setVisibility(View.GONE);
	}
	public void enableDownFilter(){
		mRightFilterLayout.setVisibility(View.GONE);
		mDownFilterLayout.setVisibility(View.VISIBLE);
	}

	public void OnMenuSelected(int menuId) {

	}

	private void prepareSlideMenu() {
		//		mSlideInMenuWidth = (int) getResources().getDimension(
		//				R.dimen.slidemenugap);
		//		mSlideMenuLayout = (RelativeLayout) findViewById(R.id.slideinmenulayout);
		//		if (mSlideMenuLayout != null) {
		//			mSlideMenuList = (ListView) findViewById(R.id.slideinmenulistbox);
		//			mSlidemenuAdapter = new slidemenuadapter(this);
		//			mSlideMenuList.setAdapter(mSlidemenuAdapter);
		//			mSlideMenuList.setOnItemClickListener(new OnItemClickListener() {
		//
		//				@Override
		//				public void onItemClick(AdapterView<?> arg0, View arg1,
		//						int arg2, long arg3) {
		//					mSlideInMenuToggle = false;
		//					hideMenu();
		//				}
		//
		//			});
		//			mSlideMenuLayout.setX(-mSlideInMenuWidth);
		//		}
		// fillSlideMenuData();
	}

	private void fillSlideMenuData() {
		List<slidemenudata> data = new ArrayList<slidemenudata>();
		data.add(new slidemenudata("Recommended", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("Movies", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Tv Shows", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Special Programming", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("Popular", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Trending", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Recent", "", R.drawable.social_send_now));
		data.add(new slidemenudata("New", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Sports", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Spotlight", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Featured", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Beauty & Fashion", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("Science & Education", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("Cooking & Health", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("News & Politics", "",
				R.drawable.social_send_now));
		data.add(new slidemenudata("Lifestyle", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Purchase", "", R.drawable.social_send_now));
		mSlidemenuAdapter.setData(data);
	}

	protected void animate(float fromX, float toX, final View v,
			final boolean showlist, int animationType) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		if (animationType == 1) {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_X, fromX, toX));
		} else {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromX, toX));
		}
		set.setDuration(getResources().getInteger(
				android.R.integer.config_longAnimTime));
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				if(showlist){
					fillSlideMenuData();
				}
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}
		});
		set.start();
	}
	public void setContentView(View v){
		mContentView = v;
	}

	private void hideMenu() {
		if (mSlideMenuLayout == null) {
			return;
		}
		// Toast.makeText(this, "slide out", Toast.LENGTH_SHORT).show();
		animate(0, -mSlideInMenuWidth, mSlideMenuLayout, false, 1);
		animate(mSlideInMenuWidth, 0, mContentView, false, 1);
		List<slidemenudata> data = new ArrayList<slidemenudata>();
		mSlidemenuAdapter.setData(data);
	}

	private void showMenu() {
		if (mSlideMenuLayout == null) {
			return;
		}
		// Toast.makeText(this, "slide in", Toast.LENGTH_SHORT).show();
		animate(-mSlideInMenuWidth, 0, mSlideMenuLayout, true, 1);
		animate(0, mSlideInMenuWidth, mContentView, false, 1);

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(mSearchToggle)
			mSearchToggle = false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {     
		super.onCreateOptionsMenu(menu);   
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.optmenu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (!Session.getActiveSession().isOpened())
			menu.getItem(0).setEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.invite:
		{
			//getAndUseAuthTokenInAsyncTask();
			sendRequestDialog();


			break;
		}
		case R.id.logout:

			if(mPlusClient.isConnected() || Session.getActiveSession().isOpened())
			{
				if(mPlusClient.isConnected())
				{

					//getGooglePlusToken();
					//getTokenReq.execute((Void)null);


					mPlusClient.clearDefaultAccount();
					mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
						@Override
						public void onAccessRevoked(ConnectionResult status) {
							// mPlusClient is now disconnected and access has been revoked.
							// Trigger app logic to comply with the developer policies
						}
					});
					mPlusClient.disconnect();
				}
				else if(Session.getActiveSession().isOpened())
				{
					onClickLogout();
				}

				//mPlusClient.connect();
			}
			finish();
			launchActivity(LoginActivity.class,this , null);

			break;

		}
		return true;
	}

	public void showToast(CharSequence aMsg){

		Toast.makeText(BaseActivity.this.getApplicationContext(), 
				aMsg, 
				Toast.LENGTH_SHORT).show();
	}

	private void sendRequestDialog() {
		Bundle params = new Bundle();

		params.putString("message", "Learn how to make your Android apps social");
		params.putString("data",
				"{\"badge_of_awesomeness\":\"1\"," +
				"\"social_karma\":\"5\"}");
		WebDialog requestsDialog = (
				new WebDialog.RequestsDialogBuilder(BaseActivity.this,
						Session.getActiveSession(),
						params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								if (error != null) {
									if (error instanceof FacebookOperationCanceledException) {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request cancelled", 
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Network Error", 
												Toast.LENGTH_SHORT).show();
									}
								} else {
									final String requestId = values.getString("request");
									if (requestId != null) {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request sent",  
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(BaseActivity.this.getApplicationContext(), 
												"Request cancelled", 
												Toast.LENGTH_SHORT).show();
									}
								}   
							}

						})
						.build();
		requestsDialog.show();
	}



	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
			finish();
			launchActivity(LoginActivity.class,this , null);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();

	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
		boolean status = mPlusClient.isConnected();
		Toast.makeText(this, status + " is disconnected.", Toast.LENGTH_LONG).show();
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
					if(!mPlusClient.isConnected())
						mPlusClient.connect();
				}
			}
		}
		// Save the result and resolve the connection failure upon a user click.
		mConnectionResult = result;
	}
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
			mConnectionResult = null;
			if(!mPlusClient.isConnected())
				mPlusClient.connect();


		}else
		{
			Session.getActiveSession().onActivityResult(this, requestCode, responseCode, intent);
		}
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

	// Example of how to use the GoogleAuthUtil in a blocking, non-main thread context
	void getAndUseAuthTokenBlocking() {
		int MY_ACTIVITYS_AUTH_REQUEST_CODE=200;
		try {
			// Retrieve a token for the given account and scope. It will always return either
			// a non-empty String or throw an exception.
			//final String token = GoogleAuthUtil.getToken(this, mPlusClient.getAccountName(), "oauth2:server:client_id:305644042032-0ebc7fgt75vmaapp2pbqchfjlve6vf42.apps.googleusercontent.com:api_scope:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email");

			final String token = GoogleAuthUtil.getToken(this, mPlusClient.getAccountName(), "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email");
			Log.d(TAG, "GOOGLE TOKEN<,,,,,,,,, "+token);
			// Do work with token.
			/* if (server indicates token is invalid) {
	              // invalidate the token that we found is bad so that GoogleAuthUtil won't
	              // return it next time (it may have cached it)
	              GoogleAuthUtil.invalidateToken(Context, String)(context, token);
	              // consider retrying getAndUseTokenBlocking() once more
	              return;
	          }*/
			return;
		} catch (GooglePlayServicesAvailabilityException playEx) {
			Dialog alert = GooglePlayServicesUtil.getErrorDialog(
					playEx.getConnectionStatusCode(),
					this,
					MY_ACTIVITYS_AUTH_REQUEST_CODE);

		} catch (UserRecoverableAuthException userAuthEx) {
			// Start the user recoverable action using the intent returned by
			// getIntent()
			startActivityForResult(
					userAuthEx.getIntent(),
					MY_ACTIVITYS_AUTH_REQUEST_CODE);
			return;
		} catch (IOException transientEx) {
			// network or server error, the call is expected to succeed if you try again later.
			// Don't attempt to call again immediately - the request is likely to
			// fail, you'll hit quotas or back-off.
			return;
		} catch (GoogleAuthException authEx) {
			// Failure. The call is not expected to ever succeed so it should not be
			// retried.
			return;
		}
	}

	// Example of how to use AsyncTask to call blocking code on a background thread.
	protected void getAndUseAuthTokenInAsyncTask() {
		AsyncTask task = new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				getAndUseAuthTokenBlocking();
				return null;
			}
		};
		task.execute((Void)null);
	}
}
