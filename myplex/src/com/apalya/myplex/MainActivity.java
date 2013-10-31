package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.adapters.FliterMenuAdapter;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataGenralInfo;
import com.apalya.myplex.data.CardDataImages;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataSimilarContent;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.NavigationOptionsMenu;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardDetails;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.fragments.SetttingsFragment;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MessagePost.MessagePostCallback;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.LastWatchedCardDetails;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.RatingDialog;
import com.facebook.Session;
import com.flurry.android.FlurryAgent;

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
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		FlurryAgent.onEndSession(this);
		
		super.onStop();
	}
	
	@Override
	public void setOrientation(int value){
		setRequestedOrientation(value);
	}
	@Override
	public int getOrientation(){
		return getRequestedOrientation();
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
				R.drawable.menu_profile, myplexapplication.getUserProfileInstance().getProfilePic(),NavigationOptionsMenuAdapter.CARDDETAILS_ACTION,R.layout.navigation_menuitemlarge));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.FAVOURITE,R.drawable.iconfav, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.PURCHASES,R.drawable.iconpurchases, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.DOWNLOADS,R.drawable.icondnload, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Settings",R.drawable.iconsettings, null, NavigationOptionsMenuAdapter.SETTINGS_ACTION,R.layout.navigation_menuitemsmall));
		Session fbSession=Session.getActiveSession();
		if(fbSession!=null && fbSession.isOpened())
			mMenuItemList.add(new NavigationOptionsMenu("Invite Friends",R.drawable.iconfriends, null, NavigationOptionsMenuAdapter.INVITE_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("Logout",R.drawable.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu("ApplicationLogo",R.drawable.iconrate, null, NavigationOptionsMenuAdapter.NOFOCUS_ACTION,R.layout.applicationlogolayout));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.RECOMMENDED,R.drawable.iconhome, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.MOVIES,R.drawable.iconmovie, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LIVETV,R.drawable.iconlivetv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
//		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.drawable.icontv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
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


		CardExplorerData mExplorerData  = myplexapplication.getCardExplorerData();
		mExplorerData =  (CardExplorerData)Util.loadObject(myplexapplication.getApplicationConfig().lastViewedCardsPath);
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
				showNavigationFullImage(true);
				mNavigationDrawerOpened = false;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				showNavigationFullImage(false);
				mNavigationDrawerOpened = true;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			Session fbSession=Session.getActiveSession();
			if(fbSession!=null && fbSession.isOpened()){
				selectItem(8);
			}
			else{
				selectItem(7);
			}
		}
		
		Util.deserializeData(MainActivity.this);
	}

	private void showNavigationFullImage(boolean value){
//		if(value)
//			mNavigationMenu.setImageResource(R.drawable.iconmenu);
//		else
//			mNavigationMenu.setImageResource(R.drawable.iconmenuin);
		
		AnimatorSet set = new AnimatorSet();
		int fromX = 0;
		int toX = -(mNavigationMenu.getWidth()/4);
		if(value){
			fromX = -(mNavigationMenu.getWidth()/4);
			toX = 0;
		}
		set.play(ObjectAnimator.ofFloat(mNavigationMenu, View.TRANSLATION_X, fromX,toX));
		set.setDuration(200);
		set.setInterpolator(new DecelerateInterpolator());
		set.start();
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

	private ImageView mNavigationMenu;
	public void prepareCustomActionBar() {
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		View v = getActionBar().getCustomView();
		if (v == null) {
			return;
		}
		LinearLayout navigationMenuLayout = (LinearLayout)v.findViewById(R.id.customactionbar_drawerLayout);
		Util.showFeedbackOnSame(navigationMenuLayout);
		mNavigationMenu = (ImageView) v.findViewById(R.id.customactionbar_drawer);
		navigationMenuLayout.setOnClickListener(new OnClickListener() {

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
		mCustomActionBarTitleLayout = (RelativeLayout) v
				.findViewById(R.id.customactionbar_filter);
		mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		Util.showFeedbackOnSame(mCustomActionBarTitleLayout);
		mCustomActionBarFilterImage = (ImageView) v
				.findViewById(R.id.customactionbar_filter_button);
		mTitleTextView = (TextView) v
				.findViewById(R.id.customactionbar_filter_text);
		mTitleTextView.setTypeface(FontUtil.Roboto_Regular);
		mTitleTextView.setEnabled(true);
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
		Util.showFeedbackOnSame(mCustomActionBarSearch);
		mCustomActionBarProgressBar = (ProgressBar) v
				.findViewById(R.id.customactionbar_progressBar);
	}
	private void rotateUp(){
		 Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.rotateup);
		 animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mCustomActionBarFilterImage.setImageResource(R.drawable.iconhide);
			}
		});
		 mCustomActionBarFilterImage.startAnimation(animation);
	}
	private void rotateDown(){
		 Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.rotatedown);
		 animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					mCustomActionBarFilterImage.setImageResource(R.drawable.iconexpose);
				}
			});
		 mCustomActionBarFilterImage.startAnimation(animation);
	}
	@Override
	public void enableFilterAction(boolean value){
		if(value){
			mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
			mCustomActionBarFilterImage.setVisibility(View.VISIBLE);
		}else{
			mCustomActionBarTitleLayout.setOnClickListener(null);
			mCustomActionBarFilterImage.setVisibility(View.GONE);
		}
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
		Util.serializeData(MainActivity.this);
		Util.saveObject(myplexapplication.getCardExplorerData(), myplexapplication.getApplicationConfig().lastViewedCardsPath);
		
		if(myplexapplication.getUserProfileInstance().firstVisitStatus)
		{
			final boolean exitStatus=false;
			CardData profileData=new CardData();
			profileData._id="0";
			RatingDialog dialog = new RatingDialog(mContext);
			dialog.prepareRating();
			myplexapplication.getUserProfileInstance().firstVisitStatus=false;
			dialog.showDialog(new MessagePostCallback() {
				
				@Override
				public void sendMessage(boolean status) {
					if(status){
						Util.showToast(mContext, "Review has posted successfully.",Util.TOAST_TYPE_INFO);
						
					}else{
						Util.showToast(mContext, "Unable to post your review.",Util.TOAST_TYPE_ERROR);
					}
					exitApp();
				}
				
			}, profileData);
		
			return exitStatus;
		}
		else
		{
			if (mShowExitToast) {
				Util.showToast(this, "Press back again to close the application.",Util.TOAST_TYPE_INFO);
//				Toast.makeText(this, "Press back again to close the application.",
//						Toast.LENGTH_LONG).show();
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
			showActionBar();
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
				mFragmentStack.pop();
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
		case NavigationOptionsMenuAdapter.CARDDETAILS_ACTION:
			mCardDetails = new CardDetails();
			fragment = mCardDetails;
			break;
		case NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION:
			mCardExplorer = new CardExplorer();
			fragment = mCardExplorer;
			break;
		case NavigationOptionsMenuAdapter.SEARCH_ACTION:
			mSearchActivity = new SearchActivity();
			fragment = mSearchActivity;
			break;
		case NavigationOptionsMenuAdapter.SETTINGS_ACTION:
			mSettingsScreen = new SetttingsFragment();
			fragment = mSettingsScreen;
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
	private SetttingsFragment mSettingsScreen;
	private void selectItem(int position) {
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		
		switch (menu.mScreenType) {
		case NavigationOptionsMenuAdapter.INVITE_ACTION:{
			Util.InviteFriends(MainActivity.this);
			return;
		}
		case NavigationOptionsMenuAdapter.NOACTION_ACTION: {
			return;
		}
		case NavigationOptionsMenuAdapter.LOGOUT_ACTION: {
			LogOutUtil.onClickLogout(MainActivity.this);
			return;
		}
		case NavigationOptionsMenuAdapter.CARDDETAILS_ACTION: {
			if(mCurrentFragment!=mCardDetails)
			{
				mCardDetails = new CardDetails();
				CardData profileData = new CardData();
				profileData._id="0";
				CardDataGenralInfo profileInfo=new CardDataGenralInfo();
				profileInfo.title=myplexapplication.getUserProfileInstance().getName();
				if(myplexapplication.getUserProfileInstance().joinedDate==null)
					myplexapplication.getUserProfileInstance().joinedDate=myplexapplication.getUserProfileInstance().lastVisitedDate;
				profileInfo.briefDescription=myplexapplication.getUserProfileInstance().getUserProfile();
				profileInfo.description=myplexapplication.getUserProfileInstance().getUserProfile();
				CardDataImages pics=new CardDataImages();
				CardDataImagesItem profilePic=new CardDataImagesItem();
				profilePic.profile="xxhdpi";
				profilePic.link=myplexapplication.getUserProfileInstance().getProfilePic();
				pics.values.add(profilePic);
				CardDataSimilarContent lastVisited=new CardDataSimilarContent();
				lastVisited.values=myplexapplication.getUserProfileInstance().lastVisitedCardData;
				profileData.similarContent=lastVisited;
				profileData.generalInfo=profileInfo;
				profileData.images=pics;
				mCurrentFragment=mCardDetails;
				mCurrentFragment.mDataObject=profileData;
			}else{
				mDrawerLayout.closeDrawer(mDrawerList);
				return;
			}
			break;
		}
		case NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION: {
			mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
			CardExplorerData data = myplexapplication.getCardExplorerData();
			data.reset();
			
			if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.FAVOURITE)){
				data.requestType = CardExplorerData.REQUEST_FAVOURITE;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)){
				data.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.MOVIES)){
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery ="movie";
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.LIVETV)){
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery ="live";
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)){
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery ="tvshows";
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.DOWNLOADS)){
				data.requestType = CardExplorerData.REQUEST_DOWNLOADS;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.PURCHASES)){
				data.requestType = CardExplorerData.REQUEST_PURCHASES;
			}
			mCurrentFragment = mCardExplorer;
			break;
		}
		case NavigationOptionsMenuAdapter.SETTINGS_ACTION:
		{
			mSettingsScreen = (SetttingsFragment) createFragment(NavigationOptionsMenuAdapter.SETTINGS_ACTION);
			mCurrentFragment = mSettingsScreen;
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
		setActionBarTitle("myplex");
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
	private ListView mFilterListView;
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
	private Bitmap mOrginalBitmap;
	private Blur mBlurEngine;
	private void addBlur(){
		if(mCurrentFragment == null){return;}
		if(mCurrentFragment.getView() == null){return;}
		if(mFilterMenuPopup == null){return;}
		try {
//			mFilterListView.setVisibility(View.INVISIBLE);
//			ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mFilterListView, "alpha", 0f,1f);
//			fadeAnim.setDuration(1200);
//			fadeAnim.addListener(new AnimatorListenerAdapter() {
//				public void onAnimationEnd(Animator animation) {
////					mFilterListView.setVisibility(View.VISIBLE);
//				}
//			});
//			fadeAnim.start();
			mCurrentFragment.getView().setDrawingCacheEnabled(true);
			mOrginalBitmap = mCurrentFragment.getView().getDrawingCache();
			if(mBlurEngine != null){
				mBlurEngine.abort();
			}
			mBlurEngine = new Blur();
			Drawable bg = new ColorDrawable(Color.parseColor("#00000000"));
			mPopBlurredLayout.setBackgroundDrawable(bg);
			mBlurEngine.fastblur(mContext, mOrginalBitmap, 12, new BlurResponse() {
				
				@Override
				public void BlurredBitmap(Bitmap b) {
					if(mOrginalBitmap != null)
						mOrginalBitmap.recycle();
					mOrginalBitmap  = null;
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
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			public void run() {
				addBlur();
			}
		});
		mFilterMenuPopupWindow = new PopupWindow(mFilterMenuPopup,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		rotateUp();
		mFilterMenuPopupWindow.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				rotateDown();
			}
		});
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
		
		mFilterListView = (ListView) mFilterMenuPopup.findViewById(R.id.listView1);
		
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
	@Override
	public void hidefilterMenu() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void showfilterMenu() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void searchButtonClicked() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ConsumerApi.SUBSCRIPTIONREQUEST){
			
		}
	}
}
