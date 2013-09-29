package com.apalya.myplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.DeviceDetails;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.utils.AccountUtils;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.facebook.Session;
import com.flurry.android.FlurryAgent;

public class MainActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// private CharSequence mDrawerTitle;
	// private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;
	public final static int CARDDETAILS = 0;
	public final static int CARDEXPLORER = 1;
	public final static int SEARCH = 2;
	private ProgressDialog mProgressDialog = null;
	private String TAG="MAINACTIVITY";
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();
	private DeviceDetails mDevInfo;
	private UserProfile mUserInfo;

	public class NavigationOptionsMenu {
		public String label = new String();
		public int resID;
		public String ImageUrl = new String();
		public int type = 1; // 0 for biggerView, 2 for spinner

		public NavigationOptionsMenu(String label, int resId, String ImageUrl,
				int type) {
			this.label = label;
			this.resID = resId;
			this.ImageUrl = ImageUrl;
			this.type = type;
		}
	}

	private void fillMenuItem() {
		
		mMenuItemList.add(new NavigationOptionsMenu(myplexapplication.getUserProfileInstance().getName(),
				R.drawable.menu_profile, myplexapplication.getUserProfileInstance().getProfilePic(), 0));
		mMenuItemList.add(new NavigationOptionsMenu("Home",
				R.drawable.menu_home, null, 1));
		mMenuItemList.add(new NavigationOptionsMenu("Home Tablet",
				R.drawable.menu_home, null, 1));
		mMenuItemList.add(new NavigationOptionsMenu("Search",
				R.drawable.menu_search, null, 1));
		mMenuItemList.add(new NavigationOptionsMenu("Settings",
				R.drawable.menu_settings, null, 1));
		mMenuItemList.add(new NavigationOptionsMenu("Help",
				R.drawable.menu_help, null, 1));
		mMenuItemList.add(new NavigationOptionsMenu("Logout",
				R.drawable.menu_logout, null, 1));
	}

	public class NavigationOptionsMenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mMenuItemList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mMenuItemList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			NavigationOptionsMenu menu = mMenuItemList.get(position);
			if (menu.type == 1) {
				v = mInflater.inflate(R.layout.navigation_menuitemsmall, null);
				TextView text = (TextView) v
						.findViewById(R.id.drawer_list_item_text);
				ImageView image = (ImageView) v
						.findViewById(R.id.drawer_list_item_image);
				text.setText(menu.label);
				image.setImageResource(menu.resID);
			} else if (menu.type == 0) {
				v = mInflater.inflate(R.layout.navigation_menuitemlarge, null);
				NetworkImageView image = (NetworkImageView) v
						.findViewById(R.id.drawer_list_item_image);
				image.setDefaultImageResId(menu.resID);
				image.setImageUrl(menu.ImageUrl, MyVolley.getImageLoader());
			}
			return v;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainview);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		myplexUtils.mScreenHeight = dm.heightPixels;
		myplexUtils.mScreenWidth = dm.widthPixels;

		mDevInfo=myplexapplication.getDevDetailsInstance();
		mUserInfo=myplexapplication.getUserProfileInstance();
		
		mInflater = LayoutInflater.from(this);
		fillMenuItem();
		// mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new NavigationOptionsMenuAdapter());
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		// mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		// R.layout.drawer_list_item, mPlanetTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setHomeButtonEnabled(true);
		// getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setIcon(new ColorDrawable(Color.TRANSPARENT));

		prepareCustomActionBar();

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				mNavigationDrawerOpened = false;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				mNavigationDrawerOpened = true;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(1);
		}
	}

	public void updateActionBarTitle() {
		if (mTitleTextView != null) {
			mTitleTextView.setText(mTitle);
		}
	}

	public String getActionBarTitle() {
		return mTitle;
	}

	public void setActionBarTitle(String title) {
		this.mTitle = title;
	}

	private TextView mTitleTextView;
	private String mTitle;
	private ImageView mCustomActionBarFilterImage;
	private RelativeLayout mCustomActionBarTitleLayout;
	private ProgressBar mCustomActionBarProgressBar;
	private ImageView mCustomActionBarSearch;
	private boolean mNavigationDrawerOpened = false;
	private OnClickListener mOnFilterClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showFilterMenuPopup();
		}
	};

	public void prepareCustomActionBar() {
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		View v = getActionBar().getCustomView();
		if (v == null) {
			return;
		}
		ImageView navigationDrawer = (ImageView) v.findViewById(R.id.customactionbar_drawer);
		navigationDrawer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mNavigationDrawerOpened) {
					mDrawerLayout.closeDrawer(mDrawerList);
					mNavigationDrawerOpened = false;
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
					mNavigationDrawerOpened = true;
				}
			}
		});
		myplexUtils.showFeedback(navigationDrawer);
		mCustomActionBarTitleLayout = (RelativeLayout) v.findViewById(R.id.customactionbar_filter);
		mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		myplexUtils.showFeedback(mCustomActionBarTitleLayout);
		mCustomActionBarFilterImage = (ImageView) v.findViewById(R.id.customactionbar_filter_button);
		mTitleTextView = (TextView) v.findViewById(R.id.customactionbar_filter_text);
		mCustomActionBarSearch  = (ImageView) v.findViewById(R.id.customactionbar_search_button);
		mCustomActionBarSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if ((mCurrentFragment instanceof SearchActivity)) {
					CardExplorer fragment = new CardExplorer();
					bringFragment(fragment);
				} else {
					SearchActivity fragment = new SearchActivity();
					bringFragment(fragment);
				}	
			}
		});
		myplexUtils.showFeedback(mCustomActionBarSearch);
		mCustomActionBarProgressBar = (ProgressBar) v.findViewById(R.id.customactionbar_progressBar);
	}

	public void showActionBarProgressBar(){
		if(mCustomActionBarProgressBar != null){
			mCustomActionBarProgressBar.setVisibility(View.VISIBLE);
		}
	}
	public void hideActionBarProgressBar(){
		if(mCustomActionBarProgressBar != null){
			mCustomActionBarProgressBar.setVisibility(View.GONE);
		}
	}
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
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
		try {
			BaseFragment fragment = mFragmentStack.peek();
			if (fragment instanceof CardExplorer) {
				if(closeApplication()){
					mFragmentStack.pop();
				}
				return;
			}
			mFragmentStack.pop();
			fragment = mFragmentStack.peek();
			if (fragment != null) {
				bringFragment(fragment);
				return;
			}
		} catch (Exception e) {
			closeApplication();
		}
		super.onBackPressed();

	}

	public BaseFragment createFragment(int fragmentType) {
		BaseFragment fragment;
		switch (fragmentType) {
		case CARDDETAILS:
			if (mCardDetails == null) {
				mCardDetails = new CardDetails();
			}
			fragment = mCardDetails;
			break;
		case CARDEXPLORER:
			if (mCardExplorer == null) {
				mCardExplorer = new CardExplorer();
			}
			fragment = mCardExplorer;
			break;
		case SEARCH:
			if (mSearchActivity == null) {
				mSearchActivity = new SearchActivity();
			}
			fragment = mSearchActivity;
			break;
		default:
			if (mCardDetails == null) {
				mCardDetails = new CardDetails();
			}
			fragment = mCardDetails;
			break;
		}
		return fragment;
	}

	public void bringFragment(BaseFragment fragment) {
		if (fragment == null) {
			return;
		}
		mCurrentFragment = fragment;
		pushFragment();
	}

	private CardExplorer mCardExplorer;
	private CardDetails mCardDetails;
	private SearchActivity mSearchActivity;

	private void selectItem(int position) {
		switch (position) {
		case 1:
		case 4:
		case 5:
		default: {
			mCardExplorer = (CardExplorer) createFragment(CARDEXPLORER);
			mCardExplorer.setDisplayMode(CardExplorer.STACKVIEW);
			mCurrentFragment = mCardExplorer;
		}
			break;
		case 0: 
			mCardDetails = (CardDetails) createFragment(CARDDETAILS);
			mCurrentFragment = mCardDetails;
			break;
		case 2: {
			mCardExplorer = (CardExplorer) createFragment(CARDEXPLORER);
			mCardExplorer.setDisplayMode(CardExplorer.GOOGLECARDVIEW);
			mCurrentFragment = mCardExplorer;
		}
			break;
		case 3:
			mSearchActivity = (SearchActivity) createFragment(SEARCH);
			mCurrentFragment = new SearchActivity();
			break;
		case 6:
			mCurrentFragment=null;
			break;
		
		// default:
		// mCurrentFragment = new CardExplorer();
		// break;
		}
		if(mCurrentFragment!=null)
		{
			pushFragment();
			mDrawerList.setItemChecked(position, true);
			setTitle(mMenuItemList.get(position).label);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		else
		{
			onClickLogout();
		}
		
	}
	private void pushFragment() {
		mFragmentStack.push(mCurrentFragment);
		mCurrentFragment.setContext(this);
		mCurrentFragment.setActionBar(getActionBar());
		mCurrentFragment.setMainActivity(this);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// transaction.setCustomAnimations(android.R.animator.fade_in,
		// android.R.animator.fade_out);
		// transaction.
		transaction.replace(R.id.content_frame, mCurrentFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		//mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private FilterMenuProvider mFilterMenuProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);

		// getActionBar().setBackgroundDrawable(new
		// ColorDrawable(Color.parseColor("#FAAC58")));
		// mCardView.setActionBarHeight(getActionBar().getHeight());
		mCurrentFragment.setActionBarHeight(getActionBar().getHeight());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen =
		mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
//		switch (item.getItemId()) {
//		case R.id.menu_search: {
//			if ((mCurrentFragment instanceof SearchActivity)) {
//				CardExplorer fragment = new CardExplorer();
//				bringFragment(fragment);
//			} else {
//				SearchActivity fragment = new SearchActivity();
//				bringFragment(fragment);
//			}
//			break;
//		}
//		default:
//			if (mCurrentFragment != null) {
//				return mCurrentFragment.onOptionsItemSelected(item);
//			}
//			return super.onOptionsItemSelected(item);
//		}
		return super.onOptionsItemSelected(item);
	}

	private List<FilterMenudata> mMenuDataList = new ArrayList<FilterMenudata>();
	private View mFilterMenuPopup;
	private PinnedSectionListView mFilterListView;
	private OnClickListener mFilterDelegate;
	private PopupWindow mFilterMenuPopupWindow = null;
	private List<PopupWindow> mFilterMenuPopupWindowList = new ArrayList<PopupWindow>();
	
	private OnItemClickListener mFilterItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if(mFilterDelegate != null){
				mFilterDelegate.onClick(arg1);
			}
			dismissFilterMenuPopupWindow();
		}
	};
	private void dismissFilterMenuPopupWindow() {
		if (mFilterMenuPopupWindow != null) {
			mFilterMenuPopupWindowList.remove(mFilterMenuPopupWindow);
			mFilterMenuPopupWindow.dismiss();
			mFilterMenuPopupWindow = null;
		}
	}

	private void showFilterMenuPopup() {
		dismissFilterMenuPopupWindow();
		mFilterMenuPopupWindow = new PopupWindow(mFilterMenuPopup, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(mCustomActionBarTitleLayout);
	}
	public void addFilterData(List<FilterMenudata> datalist,
			OnClickListener listener) {
		mFilterDelegate = listener;
		mFilterMenuPopup = mInflater.inflate(R.layout.filtermenupopup, null);
		mFilterListView = (PinnedSectionListView)mFilterMenuPopup.findViewById(R.id.listView1);
		mMenuDataList = datalist;		
		MyPinnedSectionListAdapter adapter = new MyPinnedSectionListAdapter(
				this, android.R.layout.simple_list_item_1,
				android.R.id.text1, mMenuDataList);
		mFilterListView.setAdapter(adapter);
		mFilterListView.setOnItemClickListener(mFilterItemClicked);
		
		if (mFilterMenuProvider != null) {
			mFilterMenuProvider.addFilterData(datalist, listener);
		}
	}

	private class MyPinnedSectionListAdapter extends
			ArrayAdapter<FilterMenudata> implements PinnedSectionListAdapter {

		public MyPinnedSectionListAdapter(Context context, int resource,
				int textViewResourceId, List<FilterMenudata> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TextView view = (TextView) super.getView(position, convertView,
			// parent);
			// view.setTextColor(Color.DKGRAY);
			View v = null;
			if (getItem(position).type == FilterMenudata.SECTION) {
				v = mInflater.inflate(R.layout.filtermenuitem, null);
				TextView txt = (TextView) v.findViewById(R.id.filtermenutext);
				txt.setText(mMenuDataList.get(position).label);
				v.setTag(mMenuDataList.get(position));
			} else if (getItem(position).type == FilterMenudata.ITEM) {
				v = mInflater.inflate(R.layout.filtersubmenuitem, null);
				TextView txt = (TextView) v
						.findViewById(R.id.filtersubmenutext);
				txt.setText(mMenuDataList.get(position).label);
				v.setTag(mMenuDataList.get(position));
			}
			return v;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).type;
		}

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType == FilterMenudata.SECTION;
		}
	}
	
	public void shareData(){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent,  myplexapplication.getContext().getResources().getText(R.string.send_to)));
	}
	protected boolean signOutRequest(String aUrlPath,final Map<String, String> bodyParams) {
		showProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		String url=getString(R.string.url)+aUrlPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				signOutSuccessListener(),
				signOutErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
		return true;
	}
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
	public void onClickLogout() {
		
		Log.d("BASE ACTIVITY", "@@@@@@@@@@@@@@ LOGOUT ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
		
		Session session = Session.getActiveSession();
		
		if(AccountUtils.isAuthenticated(MainActivity.this) || session!=null)
		{
			if(session.isOpened())
			{
				session.closeAndClearTokenInformation();
			}
			else
			{
				AccountUtils.signOut(MainActivity.this);
			}

		}
		Log.d("Main ACTIVITY", "@@@@@@@@@@@@@@ LOGOUT ACTIVITY 3@@@@@@@@@@@@@@@@@@@@@");
		Map<String, String> params = new HashMap<String, String>();
		params.put("profile","work");
		params.put("clientKey",mDevInfo.getClientKey());
		signOutRequest(getString(R.string.signoutpath), params);
	}
	protected ErrorListener signOutErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				dismissProgressBar();
				Log.d(TAG, "@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@ BASE ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	protected Listener<String> signOutSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				Log.d(TAG, "*****************BASE ACTIVITY************************");
				dismissProgressBar();
				JSONObject jsonResponse;
				try {
					jsonResponse = new JSONObject(response);
					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						SharedPrefUtils.writeToSharedPref(MainActivity.this,
								getString(R.string.devusername), "");
						SharedPrefUtils.writeToSharedPref(MainActivity.this,
								getString(R.string.devpassword),"");
						
						mUserInfo.setLoginStatus(false);
						mUserInfo.setName("");
						mUserInfo.setProfilePic("");

						finish();
						myplexUtils.launchActivity(LoginActivity.class,MainActivity.this , null);
					}
					else
					{
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						myplexUtils.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
	
}
