package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.adapters.FliterMenuAdapter;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.NavigationOptionsMenu;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardDetails;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.facebook.Session;

public class MainActivity extends Activity implements MainBaseOptions {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// private CharSequence mDrawerTitle;
	// private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;
	
	public FrameLayout mContentLayout;
	public Context mContext;
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();

	NavigationOptionsMenuAdapter mNavigationAdapter;
	

	public void setLandscape(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	@Override
	public void setPotrait(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	public void setSensor(){
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	@Override
	public void hideActionBar(){
		ActionBar actionBar = getActionBar();
		if(actionBar != null){
			actionBar.hide();
		}
	}
	@Override
	public void showActionBar(){
		ActionBar actionBar = getActionBar();
		if(actionBar != null){
			actionBar.show();
		}
	}
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();
	private void fillMenuItem() {
		mMenuItemList.add(new NavigationOptionsMenu(myplexapplication.getUserProfileInstance().getName(),
				R.drawable.menu_profile, myplexapplication.getUserProfileInstance().getProfilePic(),NavigationOptionsMenuAdapter.CARDDETAILS,R.layout.navigation_menuitemlarge));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.FAVOURITE,R.drawable.iconfav, null, NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Purchases",R.drawable.iconpurchases, null, NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Downloads",R.drawable.icondnload, null, NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Settings",R.drawable.iconsearch, null, NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Invite Friends",R.drawable.iconfriends, null, NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Logout",R.drawable.menu_logout, null, NavigationOptionsMenuAdapter.LOGOUT,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("ApplicationLogo",R.drawable.menu_logout, null, NavigationOptionsMenuAdapter.NOFOCUS,R.layout.applicationlogolayout));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.RECOMMENDED,R.drawable.menu_home, null, NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.MOVIES,R.drawable.iconmovie, null, NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LIVETV,R.drawable.iconlivetv, null, NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.drawable.icontv, null, NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		mNavigationAdapter.setMenuList(mMenuItemList);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		mContext = this;
		Util.prepareDisplayinfo(this);
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//				.detectDiskReads().detectDiskWrites().detectNetwork() // or
//																		// .detectAll()
//																		// for
//																		// all
//																		// detectable
//																		// problems
//				.penaltyDialog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
//				.penaltyLog().penaltyDeath().build());

		setContentView(R.layout.mainview);
		
		mContentLayout = (FrameLayout)findViewById(R.id.content_frame);
		
		mInflater = LayoutInflater.from(this);
		
		// mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mNavigationAdapter = new NavigationOptionsMenuAdapter(this);
		fillMenuItem();
		mDrawerList.setAdapter(mNavigationAdapter);
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
			selectItem(8);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.e("pref","onSaveInstanceState");
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.e("pref","onRestoreInstanceState");
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
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
		ImageView navigationDrawer = (ImageView) v
				.findViewById(R.id.customactionbar_drawer);
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
		Util.showFeedback(navigationDrawer);
		mCustomActionBarTitleLayout = (RelativeLayout) v
				.findViewById(R.id.customactionbar_filter);
		mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		Util.showFeedback(mCustomActionBarTitleLayout);
		mCustomActionBarFilterImage = (ImageView) v
				.findViewById(R.id.customactionbar_filter_button);
		mTitleTextView = (TextView) v
				.findViewById(R.id.customactionbar_filter_text);
		mCustomActionBarSearch = (ImageView) v
				.findViewById(R.id.customactionbar_search_button);
		mCustomActionBarSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if ((mCurrentFragment instanceof SearchActivity)) {
						mCurrentFragment.searchButtonClicked();
				} else {
					SearchActivity fragment = new SearchActivity();
					bringFragment(fragment);
				}
			}
		});
		Util.showFeedback(mCustomActionBarSearch);
		mCustomActionBarProgressBar = (ProgressBar) v
				.findViewById(R.id.customactionbar_progressBar);
	}
	@Override
	public void showActionBarProgressBar() {
		if (mCustomActionBarProgressBar != null) {
			mCustomActionBarProgressBar.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void hideActionBarProgressBar() {
		if (mCustomActionBarProgressBar != null) {
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

	private boolean closeApplication() {
		if (mShowExitToast) {
			Toast.makeText(this, "Press back again to close the application.",
					Toast.LENGTH_LONG).show();
			mShowExitToast = false;
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					mShowExitToast = true;
				}
			}, 3000);
			return false;
		} else {
			exitApp();
			return true;
		}

	}

	private void exitApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.runFinalizersOnExit(true);
		System.exit(0);
		finish();
	}

	@Override
	public void onBackPressed() {
		try {
			setSearchBarVisibilty(View.VISIBLE);
			BaseFragment fragment = mFragmentStack.peek();
			if (fragment instanceof CardExplorer) {
				if (closeApplication()) {
					mFragmentStack.pop();
				}
				return;
			}
			myplexapplication.getCardExplorerData().continueWithExisting = true;
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
		case NavigationOptionsMenuAdapter.CARDDETAILS:
			mCardDetails = new CardDetails();
			fragment = mCardDetails;
			break;
		case NavigationOptionsMenuAdapter.CARDEXPLORER:
			mCardExplorer = new CardExplorer();
			fragment = mCardExplorer;
			break;
		case NavigationOptionsMenuAdapter.SEARCH:
			mSearchActivity = new SearchActivity();
			fragment = mSearchActivity;
			break;
		default:
			mCardDetails = new CardDetails();
			fragment = mCardDetails;
			break;
		}
		return fragment;
	}
	@Override
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
	private DownloadsActivity mDownloadsActivity;
	private void selectItem(int position) {
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		
		switch (menu.mScreenType) {
		case NavigationOptionsMenuAdapter.DOWNLOADS:{
			if (mDownloadsActivity == null) {
				/*List<String> cardids = new ArrayList<String>();
				cardids.add("AAJTAK");
				cardids.add("TIMESNOW");
				cardids.add("NDTV");

				SharedPrefUtils.writeList(MainActivity.this, cardids, "cardids");
				
				List<String> cardimgs = new ArrayList<String>();
				cardimgs.add("http://myplexv2betaimages.s3.amazonaws.com/190/180x320_d8658644-5d1b-4227-be13-20c8abbe4d3e.jpg");
				cardimgs.add("http://myplexv2betaimages.s3.amazonaws.com/197/180x320_018cdd7e-66c2-4a56-8511-f65fa12cf008.jpg");
				cardimgs.add("http://myplexv2betaimages.s3.amazonaws.com/183/180x320_fc8037cc-f570-46ac-bc2d-e53886b4acbd.jpg");

				SharedPrefUtils.writeList(MainActivity.this, cardimgs, "cardimgs");
				
				long dwnlId1=Util.startDownload("http://122.248.233.48/wvm/100_ff_4.wvm","Test1",MainActivity.this);
				long dwnlId2=Util.startDownload("http://122.248.233.48/wvm/100_ff_4.wvm","Test2",MainActivity.this);
				long dwnlId3=Util.startDownload("http://122.248.233.48/wvm/100_ff_4.wvm","Test3",MainActivity.this);
				
				List<String> downloads = new ArrayList<String>();
				downloads.add(String.valueOf(dwnlId1));
				downloads.add(String.valueOf(dwnlId2));
				downloads.add(String.valueOf(dwnlId3));
				
				SharedPrefUtils.writeList(MainActivity.this, downloads, "downloads");*/
				
				mDownloadsActivity = new DownloadsActivity();
			}
			mCurrentFragment = mDownloadsActivity;
			break;
		}
		case NavigationOptionsMenuAdapter.INVITE:{
			Util.InviteFriends(MainActivity.this);
			return;
		}
		case NavigationOptionsMenuAdapter.NOACTION: {
			return;
		}
		case NavigationOptionsMenuAdapter.LOGOUT: {
			LogOutUtil.onClickLogout(MainActivity.this);
			return;
		}
		case NavigationOptionsMenuAdapter.CARDDETAILS: {
			break;
		}
		case NavigationOptionsMenuAdapter.CARDEXPLORER: {
			mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
			CardExplorerData data = myplexapplication.getCardExplorerData();
			data.reset();
			if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.FAVOURITE)){
				data.requestType = CardExplorerData.REQUEST_FAVOURITE;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)){
				data.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.MOVIES)){
				data.searchQuery ="movie";
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.LIVETV)){
				data.searchQuery ="live";
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)){
				data.searchQuery ="tvshows";
			}			
			mCurrentFragment = mCardExplorer;
			break;
		}
		default: {
			// mCardExplorer = (CardExplorer) createFragment(CARDEXPLORER);
			// CardExplorerData mData = myplexapplication.getCardExplorerData();
			// mData.reset();
			// mData.requestType = CardExplorerData.REQUEST_FAVOURITE;
			// mCurrentFragment = mCardExplorer;
		}
			break;
		}
		pushFragment();
		mDrawerList.setItemChecked(position, true);
		setTitle(mMenuItemList.get(position).mLabel);
		mDrawerLayout.closeDrawer(mDrawerList);
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
		setActionBarTitle("Myplex");
		updateActionBarTitle();
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
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private FilterMenuProvider mFilterMenuProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
//
//		 getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#19384F")));
//		 getActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#5519384F")));
		// mCardView.setActionBarHeight(getActionBar().getHeight());
		mCurrentFragment.setActionBarHeight(getActionBar().getHeight());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen =
//		 getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#19384F")));
//		 getActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#5519384F")));
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
		// switch (item.getItemId()) {
		// case R.id.menu_search: {
		// if ((mCurrentFragment instanceof SearchActivity)) {
		// CardExplorer fragment = new CardExplorer();
		// bringFragment(fragment);
		// } else {
		// SearchActivity fragment = new SearchActivity();
		// bringFragment(fragment);
		// }
		// break;
		// }
		// default:
		// if (mCurrentFragment != null) {
		// return mCurrentFragment.onOptionsItemSelected(item);
		// }
		// return super.onOptionsItemSelected(item);
		// }
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
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (mFilterDelegate != null) {
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

	private void addBlur(){
		if(mCurrentFragment == null){return;}
		if(mCurrentFragment.getView() == null){return;}
		if(mFilterMenuPopup == null){return;}
		try {
//			mFilterListView.setVisibility(View.INVISIBLE);
			ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mFilterListView, "alpha", 0f,1f);
			fadeAnim.setDuration(1200);
			fadeAnim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
//					mFilterListView.setVisibility(View.VISIBLE);
				}
			});
			fadeAnim.start();
			mCurrentFragment.getView().setDrawingCacheEnabled(true);
			Bitmap orginalBitmap = mCurrentFragment.getView().getDrawingCache();
			Blur blur = new Blur();
			Drawable bg = new ColorDrawable(Color.parseColor("#00000000"));
			mPopBlurredLayout.setBackgroundDrawable(bg);
			blur.fastblur(mContext, orginalBitmap, 12, new BlurResponse() {
				
				@Override
				public void BlurredBitmap(Bitmap b) {
					if( b == null || mFilterMenuPopup == null){return;}
					Drawable d = new BitmapDrawable(b); 
					mPopBlurredLayout.setBackgroundDrawable(d);
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mPopBlurredLayout, "alpha", 0f,1f);
					fadeAnim.setDuration(500);
					fadeAnim.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
//							mFilterListView.setVisibility(View.VISIBLE);
						}
					});
					fadeAnim.start();
					mCurrentFragment.getView().setDrawingCacheEnabled(false);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void showFilterMenuPopup() {
		dismissFilterMenuPopupWindow();
		addBlur();
		mFilterMenuPopupWindow = new PopupWindow(mFilterMenuPopup,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(mCustomActionBarTitleLayout);
	}
	private RelativeLayout mPopBlurredLayout;
	@Override
	public void addFilterData(List<FilterMenudata> datalist,
			OnClickListener listener) {
		mFilterDelegate = listener;
		
		mFilterMenuPopup = mInflater.inflate(R.layout.filtermenupopup, null);
		
		mPopBlurredLayout  = (RelativeLayout)mFilterMenuPopup.findViewById(R.id.fliterMenuBlurredLayout);
		
		mFilterListView = (PinnedSectionListView) mFilterMenuPopup.findViewById(R.id.listView1);
		
		mMenuDataList = datalist;
		
		FliterMenuAdapter adapter = new FliterMenuAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1,mMenuDataList);
		
		adapter.setDataList(mMenuDataList);
		
		mFilterListView.setAdapter(adapter);
		
		mFilterListView.setOnItemClickListener(mFilterItemClicked);

		if (mFilterMenuProvider != null) {
			mFilterMenuProvider.addFilterData(datalist, listener);
		}
	}

	@Override
	public void setSearchBarVisibilty(int visibility) {
		if (mCustomActionBarSearch != null) {

			if(mCustomActionBarSearch.getVisibility() != View.VISIBLE && visibility == View.VISIBLE)
			{
				mCustomActionBarSearch.setVisibility(visibility);
				ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(mCustomActionBarSearch, "alpha",0f, 1f);
				fadeinAnimation.setDuration(800);
				fadeinAnimation.start();
			}
			else
				mCustomActionBarSearch.setVisibility(visibility);
		}
	}

}
