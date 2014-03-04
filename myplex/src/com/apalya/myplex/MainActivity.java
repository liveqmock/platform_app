package com.apalya.myplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.FliterMenuAdapter;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.ApplicationSettings.APP_TYPE;
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
import com.apalya.myplex.fragments.SearchSuggestions;
import com.apalya.myplex.fragments.SetttingsFragment;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.receivers.ConnectivityReceiver;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MessagePost.MessagePostCallback;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardView;
import com.apalya.myplex.views.RatingDialog;
import com.facebook.Session;

import com.google.analytics.tracking.android.EasyTracker;

public class MainActivity extends Activity implements MainBaseOptions, CacheManagerCallback {
	private SearchView mSearchView;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// private CharSequence mDrawerTitle;
	// private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;
	private boolean mIsUserLoggedIn = true ;
	public static final String TAG = "MainActivity";
	private CacheManager mCacheManager = new CacheManager();
	private EasyTracker easyTracker = null;
	public FrameLayout mContentLayout;
	public Context mContext;
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();
	private TextView socialShare;
	private TextView tvOrMovie;
	private Handler handler= new Handler();

	NavigationOptionsMenuAdapter mNavigationAdapter;
	private TextView mFilterLevle;
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
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
		changeVisibility(mTitleFilterSymbol,View.GONE);
	}
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();
	private String _id;
	private void fillMenuItem() {
		
	String email = myplexapplication.getUserProfileInstance().getUserEmail();
    if(email.equalsIgnoreCase("NA") || email.equalsIgnoreCase(""))
    {
            mIsUserLoggedIn = false;
    }
            
    mMenuItemList.add(new NavigationOptionsMenu(myplexapplication.getUserProfileInstance().getName(),
                    R.drawable.menu_profile, myplexapplication.getUserProfileInstance().getProfilePic(),NavigationOptionsMenuAdapter.CARDDETAILS_ACTION,R.layout.navigation_menuitemlarge));
    
    int screenType = NavigationOptionsMenuAdapter.NOFOCUS_ACTION;
    if(mIsUserLoggedIn)
    {
            screenType = NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION;
    }
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LIVETV,R.string.iconlivetv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.MOVIES,R.string.iconmovie, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.RECOMMENDED,R.string.iconhome, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.string.iconlivetv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
//    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.SPORTS,R.string.iconcricket, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGO,R.string.iconrate, null, NavigationOptionsMenuAdapter.NOFOCUS_ACTION,R.layout.applicationlogolayout));

    
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.FAVOURITE,R.string.iconfav, null, screenType,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.PURCHASES,R.string.iconpurchases, null,screenType,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.DOWNLOADS,R.string.icondnload, null, screenType,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.DISCOVER,R.string.icondiscover, null, NavigationOptionsMenuAdapter.SEARCH_ACTION,R.layout.navigation_menuitemsmall));
    
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGO,R.string.iconrate, null, NavigationOptionsMenuAdapter.NOFOCUS_ACTION,R.layout.applicationlogolayout));
    
    
    Session fbSession=Session.getActiveSession();
    if(fbSession!=null && fbSession.isOpened())
            mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.INVITEFRIENDS,R.string.iconfriends, null, NavigationOptionsMenuAdapter.INVITE_ACTION,R.layout.navigation_menuitemsmall));
    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.SETTINGS,R.string.iconsettings, null, NavigationOptionsMenuAdapter.SETTINGS_ACTION,R.layout.navigation_menuitemsmall));
    if(mIsUserLoggedIn)
            mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGOUT,R.string.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
    else
            mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGIN,R.string.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
//    mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.drawable.icontv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
    mNavigationAdapter.setMenuList(mMenuItemList);
    mNavigationAdapter.setLoginStatus(mIsUserLoggedIn);
    }
	private void fillMenuItemOffline() {
		String email = myplexapplication.getUserProfileInstance().getUserEmail();
        if(email.equalsIgnoreCase("NA") || email.equalsIgnoreCase(""))
        {
                mIsUserLoggedIn = false;
        }                
        mMenuItemList.add(new NavigationOptionsMenu(myplexapplication.getUserProfileInstance().getName(),
                        R.drawable.menu_profile, myplexapplication.getUserProfileInstance().getProfilePic(),NavigationOptionsMenuAdapter.CARDDETAILS_ACTION,R.layout.navigation_menuitemlarge));        
        int screenType = NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION;
        mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.PURCHASES,R.string.iconpurchases, null,screenType,R.layout.navigation_menuitemsmall));
        mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.DOWNLOADS,R.string.icondnload, null, screenType,R.layout.navigation_menuitemsmall));        
        mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGO,R.string.iconrate, null, NavigationOptionsMenuAdapter.NOFOCUS_ACTION,R.layout.applicationlogolayout));
        mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.SETTINGS,R.string.iconsettings, null, NavigationOptionsMenuAdapter.SETTINGS_ACTION,R.layout.navigation_menuitemsmall));
        if(mIsUserLoggedIn)
                mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGOUT,R.string.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
        else
                mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGIN,R.string.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
        mNavigationAdapter.setMenuList(mMenuItemList);
        mNavigationAdapter.setLoginStatus(mIsUserLoggedIn);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		mContext = this;
		Util.prepareDisplayinfo(this);
		if(!myplexapplication.isInitlized){
			myplexapplication.init(this);
		}
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
		if(ApplicationSettings.MODE_APP_TYPE == APP_TYPE.OFFLINE)
			fillMenuItemOffline();
		else
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
				showNavigationFullImage(true);
				mNavigationDrawerOpened = false;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				Util.closeKeyBoard(mContext, mTitleTextView);
				showNavigationFullImage(false);
				mNavigationDrawerOpened = true;
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if(!onHandleExternalIntent(getIntent())){
			if(ApplicationSettings.MODE_APP_TYPE == APP_TYPE.OFFLINE )
				selectItem(2);
			else
				selectItem(1);
		}
/*		if (savedInstanceState == null) {
			Session fbSession=Session.getActiveSession();
			if(fbSession!=null && fbSession.isOpened()){
				selectItem(9);
			}
			else{
				selectItem(8);
			}
		}*/
		
		Util.deserializeData(MainActivity.this);
	}

	private boolean onHandleExternalIntent(Intent intent) {
		if(intent==null)
			return false;
		
		boolean intentHandled = false;
		
		if(getIntent().hasExtra(mContext.getString(R.string._id))){
			showActionBarProgressBar();
			_id = getIntent().getExtras().getString(mContext.getString(R.string._id));
			List<CardData> cards  =  new ArrayList<CardData>();
			CardData cardData  = new CardData();
			cardData._id = _id;
			cards.add(cardData);
			if(_id!=null && _id.length() >0){
				mCacheManager.getCardDetails(cards, IndexHandler.OperationType.FTSEARCH, new CacheManagerCallback() {					
					@Override
					public void OnOnlineResults(List<CardData> dataList) {
						for (CardData cardData : dataList) {
							if(cardData._id.equalsIgnoreCase(_id)){
								mCacheManager.unRegisterCallback();
								hideActionBarProgressBar();
								BaseFragment fragment = createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
								fragment.setDataObject(cardData);
								bringFragment(fragment);
								break;
							}
						}
					}					
					@Override
					public void OnOnlineError(VolleyError error) {
					}					
					@Override
					public void OnCacheResults(HashMap<String, CardData> obj,
							boolean issuedRequest) {
						CardData data = null;
						
						 Iterator<Entry<String, CardData>> it = obj.entrySet().iterator();
						    while (it.hasNext()) {
						        Entry<String, CardData> pair = it.next();
						        if(pair.getValue()._id.equalsIgnoreCase(_id)){
						        	data = pair.getValue();
						        	break;
						        }
						    }
						
						if(data == null){
							if(!ConnectivityReceiver.isConnected){
								mCacheManager.unRegisterCallback();
								selectItem(3);								
							} 
							return;
						}
						    
						hideActionBarProgressBar();
						mCacheManager.unRegisterCallback();
						BaseFragment fragment = createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
						fragment.setDataObject(data);
						bringFragment(fragment);
					}
				});
			}
			intentHandled=true;
		}
		String action  = "";
		if(intent.hasExtra(mContext.getString(R.string.page))){
			action = intent.getStringExtra(mContext.getString(R.string.page));
		}
		if(action.length()>0){
			if(action.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE))
				selectItem(1);
			else if(action.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_MOVIE))
				selectItem(2);
			else if(action.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED))
				selectItem(3);
			else if(action.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS))
				selectItem(4);
			return true;
		}
		return intentHandled;
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
	public String getActionBarTitle() {
		return mTitle;
	}
	public void saveActionBarTitle(){
		mLastActionBarTitle = mTitle;
	}
	public String mLastActionBarTitle = new String();
	public void setActionBarTitle(String title) {
//		mFilterLevle.setVisibility(View.GONE);
		mFilterLevle.setText("All");
		this.mTitle = title;
		if (mTitleTextView != null) {
			mTitleTextView.setText(mTitle);
		}
	}

	private TextView mTitleTextView;
	private TextView mTitleFilterSymbol;
	private String mTitle;
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
	private void changeVisibility(View v,int value){
		if(v != null){
			v.setVisibility(value);
		}
	}
	
	private String getCurrentScreen() {
		String currentScreen = null;
		if(mCurrentFragment != null) {
			currentScreen = mCurrentFragment.getClass().getName();
			if(currentScreen.contains("CardDetails")) {
				currentScreen = "CardDetails";
			}
			else if(currentScreen.contains("SearchSuggestions")) {
				currentScreen = "SearchSuggestions";
			}
			else if(currentScreen.contains("SettingsFragment")) {
				currentScreen = "SettingsFragment";
			}
			else {
				currentScreen = "CardExplorer";
			}
			return currentScreen;
		}
		else{
			return "CardExplorer Screen";
		}		
	}
	
	private ImageView mNavigationMenu;
	public void prepareCustomActionBar() {
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		View v = getActionBar().getCustomView();
		if (v == null) {
			return;
		}
		RelativeLayout navigationMenuLayout = (RelativeLayout)v.findViewById(R.id.customactionbar_drawerLayout);
		Util.showFeedbackOnSame(navigationMenuLayout);
		mNavigationMenu = (ImageView) v.findViewById(R.id.customactionbar_drawer);
		navigationMenuLayout.setOnClickListener(navigationClickListener);
		mCustomActionBarTitleLayout = (RelativeLayout) v.findViewById(R.id.customactionbar_filter);
		mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		Util.showFeedbackOnSame(mCustomActionBarTitleLayout);
		
		mTitleTextView = (TextView) v.findViewById(R.id.customactionbar_filter_text);
		mTitleTextView.setTypeface(FontUtil.Roboto_Regular);
		
		mFilterLevle = (TextView)v.findViewById(R.id.filter_levle);
		mFilterLevle.setTypeface(FontUtil.Roboto_Regular);
		
		mTitleFilterSymbol = (TextView)v.findViewById(R.id.customactionbar_filter_text1);
		changeVisibility(mTitleFilterSymbol,View.GONE);		
		
		mSearchView = (SearchView)v.findViewById(R.id.customsearchview);
		setupSearchView(mSearchView);
		
		ImageView searchBack = (ImageView)v.findViewById(R.id.customactionbar_back);
		searchBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HideSearchView();
			}
		});
		
		mCustomActionBarSearch = (ImageView) v.findViewById(R.id.customactionbar_search_button);
		mCustomActionBarSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if ((mCurrentFragment instanceof SearchActivity)) {
					mCurrentFragment.searchButtonClicked();
				} else {
					SearchActivity fragment = new SearchActivity();
					setActionBarTitle("search");
					bringFragment(fragment);
				}
			}
		});
		Util.showFeedbackOnSame(mCustomActionBarSearch);
		mCustomActionBarProgressBar = (ProgressBar) v
				.findViewById(R.id.customactionbar_progressBar);
		socialShare = (TextView)v.findViewById(R.id.actionbar_share);		
		tvOrMovie = (TextView)v.findViewById(R.id.livetv);
		FontUtil.loadFonts(getAssets());
		tvOrMovie.setTypeface(FontUtil.ss_symbolicons_line);
		
	}
	OnClickListener navigationClickListener = new  OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (mNavigationDrawerOpened) {
				mDrawerLayout.closeDrawer(mDrawerList);
				mNavigationDrawerOpened = false;
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
				mNavigationDrawerOpened = true;
				String screenOpenedFrom = getCurrentScreen();
				Analytics.mixPanelNavigationOpened(screenOpenedFrom);
			}
		}
	};
	@Override
	public void enableFilterAction(boolean value){
		if(value){
			mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		}else{
			mCustomActionBarTitleLayout.setOnClickListener(null);
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
		Thread t = new Thread() {
			public void run() {
				saveCurrentSessionData();
			}
		};
		t.start();
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
					/*if(status){
						Util.showToast(mContext, "Thanks for your feedback.",Util.TOAST_TYPE_INFO);
						
					}else{
						Util.showToast(mContext, "Unable to post your review.",Util.TOAST_TYPE_ERROR);
					}*/
					Util.showToast(mContext, "Thanks for your valuable feedback.",Util.TOAST_TYPE_INFO);
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

	public boolean HideSearchView()
	{
		try {
			mSearchView.setQuery("", false);
			if(mSearchView !=null && !mSearchView.isIconified())
			{
				mSearchView.setIconified(true);
				//AddAnalytics
				return true;
			}
		} catch (Exception e) {
			
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		if(mCurrentFragment instanceof CardDetails){
			if(mCurrentFragment.onBackClicked())
				return;
			if(mFragmentStack.size() == 1){				
				exitApp();
				super.onBackPressed();
				return;
			}
		}
		try {
			if (mDrawerLayout!=null && mNavigationDrawerOpened) {
				mDrawerLayout.closeDrawer(mDrawerList);
				mNavigationDrawerOpened = false;
				return;
			}
			if(HideSearchView()){
				return;
			}
//			showActionBar();
			//			setSearchBarVisibilty(View.INVISIBLE);
			//			setSearchViewVisibilty(View.VISIBLE);
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
				setActionBarTitle(mLastActionBarTitle);
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
/*			String username=SharedPrefUtils.getFromSharedPreference(mContext, getString(R.string.devusername));
			if(username !=null && SharedPrefUtils.getIntFromSharedPreference(mContext, "CustomFav") !=1)
			{
				SharedPrefUtils.writeToSharedPref(mContext, "CustomFav", 1);
				mCardExplorer.addCustomFavourites();
			}*/
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
	private void saveCurrentSessionData(){
		if(!ApplicationSettings.ENABLE_SERIALIZE_LAST_SEESION){
			return;
		}
		CardExplorerData explorerData = myplexapplication.getCardExplorerData();
		if(explorerData.requestType == CardExplorerData.REQUEST_RECOMMENDATION){
			List<CardData> toStoreList = new ArrayList<CardData>();
			int count = 0;
			for(int i = explorerData.mMasterEntries.size()-1;i >= 0;i--){
				if( count >= 10){
					break;
				}
				count++;
				toStoreList.add(explorerData.mMasterEntries.get(i));
			}
			if(toStoreList.size() > 0){
				Util.saveObject(toStoreList, myplexapplication.getApplicationConfig().lastViewedCardsPath);
			}
		}
	}
	@Override
	public void bringFragment(BaseFragment fragment) {
		if (fragment == null) {
			return;
		}
		removeLiveTvActionBarIcon();
		HideSearchView();
		HideSearchView();
		mCurrentFragment = fragment;
		enableFilterAction(false);
		pushFragment();
	}


	private CardExplorer mCardExplorer;
	private CardDetails mCardDetails;
	private SearchActivity mSearchActivity;
	private SetttingsFragment mSettingsScreen;
	private void selectItem(int position) {		
		saveCurrentSessionData();		
		changeVisibility(mTitleFilterSymbol, View.GONE);
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		setSearchviewHint("search movies");
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
			// This line is added as email was not removed from shared preference And it was treated as user is logged in
			myplexapplication.getUserProfileInstance().setUserEmail("NA");
			return;
		}
		case NavigationOptionsMenuAdapter.CARDDETAILS_ACTION: {
//			if(mCurrentFragment==mCardDetails)
//			{
			    removeLiveTvActionBarIcon();
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
				setActionBarTitle("My Profile");
//				setUpShareview();
			//}else{
				mDrawerLayout.closeDrawer(mDrawerList);
//				return;
//			}
			break;
		}
		case NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION: {
			mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
			CardExplorerData data = myplexapplication.getCardExplorerData();
			data.reset();
			
			if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.FAVOURITE)){
				removeLiveTvActionBarIcon();
				setActionBarTitle("my "+NavigationOptionsMenuAdapter.FAVOURITE);
				data.requestType = CardExplorerData.REQUEST_FAVOURITE;
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)){
				showLiveTvOrMovieIcon("recommended");
				data.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
				setActionBarTitle(mContext.getString(R.string.myplex_home));				
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.MOVIES)){
				showLiveTvOrMovieIcon("movies");
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery ="movie";
				data.searchScope = "movie";
				setActionBarTitle(NavigationOptionsMenuAdapter.MOVIES);				
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.LIVETV)){
				showLiveTvOrMovieIcon("live");
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery ="live";
				data.searchScope = "live";
				setActionBarTitle(NavigationOptionsMenuAdapter.LIVETV);
				setSearchviewHint("search live tv");
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)){
				removeLiveTvActionBarIcon();
				data.requestType = CardExplorerData.REQUEST_TV_SHOWS;
				data.searchQuery = ConsumerApi.TYPE_TV_SERIES;
				data.searchScope = ConsumerApi.TYPE_TV_SERIES;
				setActionBarTitle(NavigationOptionsMenuAdapter.TVSHOWS);
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.DOWNLOADS)){
				removeLiveTvActionBarIcon();
				data.requestType = CardExplorerData.REQUEST_DOWNLOADS;
				setActionBarTitle("my "+NavigationOptionsMenuAdapter.DOWNLOADS);
			}else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.PURCHASES)){
				removeLiveTvActionBarIcon();
				data.requestType = CardExplorerData.REQUEST_PURCHASES;
				setActionBarTitle("my "+NavigationOptionsMenuAdapter.PURCHASES);
			}/*else if(menu.mLabel.equalsIgnoreCase(NavigationOptionsMenuAdapter.SPORTS)){
				removeLiveTvActionBarIcon();
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery =ConsumerApi.CONTENT_SPORTS_LIVE+","+ConsumerApi.CONTENT_SPORTS_VOD;
				data.searchScope = "sportsEvent";
				setActionBarTitle(NavigationOptionsMenuAdapter.SPORTS);
				setSearchviewHint("search live tv");
			}*/
			else
			{
				setActionBarTitle("myplex");
				Log.e(TAG, menu.mLabel);
			}
			mCurrentFragment = mCardExplorer;
			break;
		}
		case NavigationOptionsMenuAdapter.SEARCH_ACTION:
		{
			removeLiveTvActionBarIcon();
			mSearchActivity = (SearchActivity) createFragment(NavigationOptionsMenuAdapter.SEARCH_ACTION);
			mCurrentFragment = mSearchActivity;
			saveActionBarTitle();
			setActionBarTitle(NavigationOptionsMenuAdapter.DISCOVER);
			break;
		}
		case NavigationOptionsMenuAdapter.SETTINGS_ACTION:
		{
			removeLiveTvActionBarIcon();
			mSettingsScreen = (SetttingsFragment) createFragment(NavigationOptionsMenuAdapter.SETTINGS_ACTION);
			mCurrentFragment = mSettingsScreen;
			saveActionBarTitle();
			setActionBarTitle(NavigationOptionsMenuAdapter.SETTINGS);
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
		addFilterData(new ArrayList<FilterMenudata>(), null);
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
	
	private void overlayFragment(BaseFragment fragment) {
		if(fragment == null)
			return;
		fragment.setActionBar(getActionBar());
		fragment.setMainActivity(this);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.content_frame, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	private void removeFragment(BaseFragment fragment)
	{
		Log.i(TAG, "remove" + fragment);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.remove(fragment);
		transaction.commit();
		mSearchSuggestionFrag = null;
		
		fragment = mFragmentStack.peek();
		Log.i(TAG, "peeking" + fragment);
		bringFragment(fragment);
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
		if( mCurrentFragment != null )
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
				
				mFilterLevle.setTypeface(FontUtil.Roboto_Light);
				mFilterLevle.setTextSize(12);
				mFilterLevle.setVisibility(View.VISIBLE);
				mFilterLevle.setText(mMenuDataList.get(arg2).label);
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
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(mCustomActionBarTitleLayout);
	}
	private RelativeLayout mPopBlurredLayout;
	@Override
	public void addFilterData(List<FilterMenudata> datalist,
			OnClickListener listener) {
		if(datalist != null && datalist.size() > 0){
			enableFilterAction(true);
			changeVisibility(mTitleFilterSymbol,View.VISIBLE);
			mFilterLevle.setVisibility(View.VISIBLE);
		}else{
			enableFilterAction(false);
			changeVisibility(mTitleFilterSymbol,View.GONE);
			mFilterLevle.setVisibility(View.GONE);
		}
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
//			mCustomActionBarSearch.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void setSearchViewVisibilty(int visibility) {
		if (mSearchView != null) {
				mSearchView.setVisibility(visibility);
		}
		socialShare.setVisibility(View.GONE);
		socialShare.setOnClickListener(null);
		mNavigationMenu.setImageResource(R.drawable.iconmenu);
		mNavigationMenu.setOnClickListener(navigationClickListener);
		
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
	
	private void setupSearchView(final SearchView searchView) {

        if (isAlwaysExpanded()) {
        	searchView.setIconifiedByDefault(false);
        } 
        searchView.setOnSearchClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(TAG,"onClick");
				Analytics.mixPanelInlineSearchInitiated(getCurrentScreen());
				
//				changeVisibility(mCustomActionBarTitleLayout,View.GONE);
				if (mDrawerLayout!=null && mNavigationDrawerOpened) {
					mDrawerLayout.closeDrawer(mDrawerList);
					mNavigationDrawerOpened = false;
				}
				findViewById(R.id.customactionbar_drawer).setVisibility(View.INVISIBLE);
				findViewById(R.id.customactionbar_filter).setVisibility(View.INVISIBLE);
				findViewById(R.id.customactionbar_back).setVisibility(View.VISIBLE);
				mSearchSuggestionFrag = new SearchSuggestions(mContext);
				overlayFragment(mSearchSuggestionFrag);
			}
		});
        
        searchView.setOnCloseListener(new OnCloseListener() {
			
			@Override
			public boolean onClose() {
				Log.i(TAG,"onClose");
				if(mSearchView !=null && !mSearchView.isIconified())
				{
					findViewById(R.id.customactionbar_drawer).setVisibility(View.VISIBLE);
					findViewById(R.id.customactionbar_filter).setVisibility(View.VISIBLE);
					findViewById(R.id.customactionbar_back).setVisibility(View.INVISIBLE);
					tvOrMovie.setVisibility(View.VISIBLE);
//					changeVisibility(mCustomActionBarTitleLayout,View.VISIBLE);
//					mSearchView.setIconified(true);
					if(mSearchSuggestionFrag !=null)
					{
						removeFragment(mSearchSuggestionFrag);
					}
				}

				return false;
			}
		});
        
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.i(TAG, query);
				HideSearchView();
				doSearch(query);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				String type = null;
				if((myplexapplication.getCardExplorerData() != null) &&
						(myplexapplication.getCardExplorerData().searchScope!=null) &&
						(myplexapplication.getCardExplorerData().searchScope.equals(ConsumerApi.VIDEO_TYPE_LIVE))){
					type = ConsumerApi.VIDEO_TYPE_LIVE;
				}
				//Addanalytics just record textchanges
				if(mSearchSuggestionFrag != null && newText.length() >0)
					mSearchSuggestionFrag.setQuery(newText,type);
				return false;
			}
		});
    }
	
	private void setSearchviewHint(String hint)
	{
		if(mSearchView !=null)
			mSearchView.setQueryHint(hint);
	}
    
    protected boolean isAlwaysExpanded() {
        return false;
    }
    //action bar search
    private void doSearch(String query)
    {
		showActionBarProgressBar();
		Analytics.SEARCH_TYPE = "actionbar";//SEARCHED_FOR action bar search
		String searchQuery = new String();
		final List<CardData> searchString = new ArrayList<CardData>();
		CardData temp = new CardData();
		// temp._id = data.getButtonId() != null ? data.getButtonId() :
		// data.getButtonName();
		temp._id = query;
		searchString.add(temp);
		searchQuery = query;
				
		mSearchQuery = searchQuery;
		setActionBarTitle(query);
		IndexHandler.OperationType searchType = IndexHandler.OperationType.DONTSEARCHDB;
		if(!Util.isNetworkAvailable(mContext))
			searchType = IndexHandler.OperationType.FTSEARCH;
		mCacheManager.getCardDetails(searchString, searchType, MainActivity.this);
	
    }

    private String mSearchQuery = new String();
	private SearchSuggestions mSearchSuggestionFrag;
    
	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,
			boolean issuedRequest) {

		if (obj == null) {
			return;
		}

		String localSearchScope = null;
		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();

		if(dataBundle != null){			
			if((dataBundle.searchScope!=null) && dataBundle.searchScope.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
				localSearchScope = ConsumerApi.VIDEO_TYPE_LIVE;
			}
			dataBundle.reset();
			if(localSearchScope != null){
				dataBundle.searchScope = localSearchScope;
			}
			dataBundle.searchQuery = mSearchQuery;
			dataBundle.requestType = CardExplorerData.REQUEST_SEARCH;
		}
		

		addFilterData(new ArrayList<FilterMenudata>(), null);

		Set<String> keySet = obj.keySet();
		for (String key : keySet) {
			CardData data = obj.get(key);
			// dataBundle.mEntries.add(data);
			// if(dataBundle.mEntries.get(key) == null){
			dataBundle.mEntries.put(key, data);
			dataBundle.mMasterEntries.add(data);
			// }
			if (data.generalInfo != null)
				Log.i(TAG, "adding " + data._id + ":" + data.generalInfo.title
						+ " from Cache");
		}
		mCacheManager.unRegisterCallback();
		hideActionBarProgressBar();
		BaseFragment fragment = createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		bringFragment(fragment);
		
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnOnlineError(VolleyError error) {
		hideActionBarProgressBar();
	}

	/**
	 * This method is added to show share button instead of search icon.
	 * 
	 */
	@Override
	public void setUpShareButton(final String toBeshared) {
		if (mSearchView != null) 
			mSearchView.setVisibility(View.GONE);
		socialShare.setVisibility(View.VISIBLE);
		FontUtil.loadFonts(getAssets());
		socialShare.setTypeface(FontUtil.ss_symbolicons_line);
		socialShare.setText(R.string.iconshare);
		socialShare.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Util.shareData(mContext, 3, "",toBeshared);
			}
		});
		mNavigationMenu.setImageResource(R.drawable.abs__ic_ab_back_holo_dark);
		mNavigationMenu.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				onBackPressed();
//				removeFragment(mCurrentFragment);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.apalya.myplex.MainBaseOptions#setUpLivetvOrMovie(boolean)
	 *  0 to diable 
	 *  1 for moview 
	 *  2 for live tv
	 */
	@Override
	public void setUpLivetvOrMovie(boolean isMovie) {
		if(isMovie)
			showLiveTvOrMovieIcon("movie");
		else
			showLiveTvOrMovieIcon("live");
		
	}
	private class  LiveTvListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			tvOrMovie.setEnabled(false);
			handler.postDelayed(new Runnable() {				
				@Override
				public void run() {
					tvOrMovie.setEnabled(true);
				}
			}, 3000);
			selectItem(1);
		}		
	};
	private class  MovieListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			tvOrMovie.setOnClickListener(null);
			tvOrMovie.setEnabled(false);
			handler.postDelayed(new Runnable() {				
				@Override
				public void run() {
					tvOrMovie.setEnabled(true);
				}
			}, 3000);
			selectItem(2);
		}		
	};
	
	public void showLiveTvOrMovieIcon(String liveTv){
		if(liveTv.equalsIgnoreCase("live")){
			tvOrMovie.setVisibility(View.VISIBLE);
			tvOrMovie.setOnClickListener(new MovieListener());
			tvOrMovie.setText(R.string.iconmovie);
		}else{
			tvOrMovie.setVisibility(View.VISIBLE);
			tvOrMovie.setText(R.string.iconlivetv);
			tvOrMovie.setOnClickListener(new LiveTvListener());
		}
	}
	public void removeLiveTvActionBarIcon(){
		tvOrMovie.setVisibility(View.GONE);
	}
	
	//for analytics
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		Log.d(TAG, "Back button pressed");
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "Testing"); 
	    	if(mCardExplorer != null) {
	    		CardView cardView = mCardExplorer.getmCardView();
	    		if(cardView != null) {
	    			if(cardView.swipeCount > 1) {
	    				cardView.mixpanelBrowsing();
	    			}
	    		}//if(cardView != null)
	    	}//if(mCardExplorer != null)
		}
	    return super.onKeyDown(keyCode, event);
	}
			
}