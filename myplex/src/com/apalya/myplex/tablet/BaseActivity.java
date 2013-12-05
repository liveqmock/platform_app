package com.apalya.myplex.tablet;

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
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.R;
import com.apalya.myplex.SearchActivity;
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
import com.apalya.myplex.fragments.CardDetailsTabletFrag;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.fragments.SetttingsFragment;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.PinnedSectionListView;
import com.facebook.Session;

public class BaseActivity extends Activity implements MainBaseOptions{
	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// private CharSequence mDrawerTitle;
	// private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;
	private boolean mIsUserLoggedIn = true ;
	
	public FrameLayout mContentLayout;
	public Context mContext;
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();

	NavigationOptionsMenuAdapter mNavigationAdapter;

	@Override
	public void setOrientation(int value){
		setRequestedOrientation(value);
	}
	@Override
	public int getOrientation(){
		return getRequestedOrientation();
	}
	@Override
	public void hideActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
	}
	@Override
	public void showActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.show();
		}
		changeVisibility(mTitleFilterSymbol,View.GONE);
	}

	protected List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		Util.prepareDisplayinfo(this);
		// setContentView(R.layout.mainview);
		mContentLayout = (FrameLayout) findViewById(R.id.content_frame);
		mInflater = LayoutInflater.from(this);
//		if (savedInstanceState == null) {
//			OnSelectedOption(8);
//		}
	}

	public void prepareNavigationMenuList(ListView listview) {
		mDrawerList = null;
		mDrawerLayout = null;
		if (listview == null) {
			return;
		}
		if (listview.getParent() instanceof DrawerLayout) {
			mDrawerLayout = (DrawerLayout) listview.getParent();
		}
		mDrawerList = listview;
		mNavigationAdapter = new NavigationOptionsMenuAdapter(this);
		fillMenuItem();
		mNavigationAdapter.setMenuList(mMenuItemList);
		mDrawerList.setAdapter(mNavigationAdapter);
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
					GravityCompat.START);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					R.drawable.ic_drawer, R.string.drawer_open,
					R.string.drawer_close) {
				public void onDrawerClosed(View view) {
					showNavigationFullImage(true);
					mNavigationDrawerOpened = false;
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					showNavigationFullImage(false);
					mNavigationDrawerOpened = true;
					invalidateOptionsMenu();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
//		OnSelectedOption(8);
	}

	public void fillMenuItem() {
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
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.FAVOURITE,R.drawable.iconfav, null, screenType,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.PURCHASES,R.drawable.iconpurchases, null,screenType,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.DOWNLOADS,R.drawable.icondnload, null, screenType,R.layout.navigation_menuitemsmall));
		
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.SETTINGS,R.drawable.iconsettings, null, NavigationOptionsMenuAdapter.SETTINGS_ACTION,R.layout.navigation_menuitemsmall));
		Session fbSession=Session.getActiveSession();
		if(fbSession!=null && fbSession.isOpened())
			mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.INVITEFRIENDS,R.drawable.iconfriends, null, NavigationOptionsMenuAdapter.INVITE_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGOUT,R.drawable.iconlogout, null, NavigationOptionsMenuAdapter.LOGOUT_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LOGO,R.drawable.iconrate, null, NavigationOptionsMenuAdapter.NOFOCUS_ACTION,R.layout.applicationlogolayout));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.RECOMMENDED,R.drawable.iconhome, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.MOVIES,R.drawable.iconmovie, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
		mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.LIVETV,R.drawable.iconlivetv, null, NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
//		 mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.drawable.icontv,null,NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION,R.layout.navigation_menuitemsmall));
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
	}; 	private void changeVisibility(View v,int value){
		if(v != null){
			v.setVisibility(value);
		}
	}
	private ImageView mNavigationMenu;
	private void showNavigationFullImage(boolean value){
		AnimatorSet set = new AnimatorSet();
		int fromX = 0;
		int toX = -(mNavigationMenu.getWidth()/2);
		if(value){
			fromX = -(mNavigationMenu.getWidth()/2);
			toX = 0;
		}
		set.play(ObjectAnimator.ofFloat(mNavigationMenu, View.TRANSLATION_X, fromX,toX));
		set.setDuration(200);
		set.setInterpolator(new DecelerateInterpolator());
		set.start();
	}
	public void prepareCustomActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setIcon(new ColorDrawable(Color.TRANSPARENT));
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		View v = getActionBar().getCustomView();
		if (v == null) {
			return;
		}
		RelativeLayout navigationMenuLayout = (RelativeLayout)v.findViewById(R.id.customactionbar_drawerLayout);
		Util.showFeedbackOnSame(navigationMenuLayout);
		mNavigationMenu = (ImageView) v.findViewById(R.id.customactionbar_drawer);
		if (mDrawerLayout == null) {
			mNavigationMenu.setVisibility(View.INVISIBLE);
		}
		navigationMenuLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mDrawerLayout == null)
					return;
				if (mNavigationDrawerOpened) {
					mDrawerLayout.closeDrawer(mDrawerList);
					mNavigationDrawerOpened = false;
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
					mNavigationDrawerOpened = true;
				}
			}
		});
		mCustomActionBarTitleLayout = (RelativeLayout) v.findViewById(R.id.customactionbar_filter);
		mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
		Util.showFeedbackOnSame(mCustomActionBarTitleLayout);
		
		mTitleTextView = (TextView) v.findViewById(R.id.customactionbar_filter_text);
		mTitleTextView.setTypeface(FontUtil.Roboto_Regular);
		
		mTitleFilterSymbol = (TextView)v.findViewById(R.id.customactionbar_filter_text1);
		changeVisibility(mTitleFilterSymbol,View.GONE);		
		
		SearchView mSearchView = (SearchView)v.findViewById(R.id.customsearchview);
		mSearchView.setVisibility(View.INVISIBLE);
		
		mCustomActionBarSearch = (ImageView) v.findViewById(R.id.customactionbar_search_button);
		mCustomActionBarSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(getResources().getBoolean(R.bool.isTablet))
				{
					searchButtonClicked();
				}
				else if ((mCurrentFragment instanceof SearchActivity)) {
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
		
	}
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
			NavigationOptionsMenu menu = mMenuItemList.get(position);
			OnSelectedOption(menu.mScreenType,menu.mLabel);
		}
	}

	private boolean mShowExitToast = true;

	private boolean closeApplication() {
		if (mShowExitToast) {
//			Toast.makeText(this, "Press back again to close the application.",
//					Toast.LENGTH_LONG).show();
			Util.showToast(this,"Press back again to close the application.",Util.TOAST_TYPE_INFO);
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
			mCardDetails = new CardDetailsTabletFrag();
			fragment = mCardDetails;
			break;
		case NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION:
			mCardExplorer = new CardExplorer();
			fragment = mCardExplorer;
			String username=SharedPrefUtils.getFromSharedPreference(mContext, getString(R.string.devusername));
			if(username !=null && SharedPrefUtils.getIntFromSharedPreference(mContext, "CustomFav") !=1)
			{
				SharedPrefUtils.writeToSharedPref(mContext, "CustomFav", 1);
				mCardExplorer.addCustomFavourites();
			}
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
			mCardDetails = new CardDetailsTabletFrag();
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
		enableFilterAction(false);
		pushFragment();
	}

	private CardExplorer mCardExplorer;
	private CardDetailsTabletFrag mCardDetails;
	private SearchActivity mSearchActivity;
	private SetttingsFragment mSettingsScreen;
	
	protected void createCardExplorer(){
		mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		mCurrentFragment = mCardExplorer;
		pushFragment();
	}

	public void OnSelectedOption(int screenType,String label) {
		changeVisibility(mTitleFilterSymbol, View.GONE);
		
		
		switch (screenType) {
		case NavigationOptionsMenuAdapter.NOACTION_ACTION: {
			return;
		}
		case NavigationOptionsMenuAdapter.LOGOUT_ACTION: {
			LogOutUtil.onClickLogout(this);
			return;
		}
		case NavigationOptionsMenuAdapter.SETTINGS_ACTION:{
			setActionBarTitle("settings");
			if(this instanceof TabletCardDetails){
				myplexapplication.mSelectedOption_Tablet = screenType;
				startActivity(new Intent(this,MultiPaneActivity.class));
				finish();
				return;
			}else{
				mSettingsScreen = (SetttingsFragment) createFragment(NavigationOptionsMenuAdapter.SETTINGS_ACTION);
				mCurrentFragment = mSettingsScreen;
				break;
			}
		}
		case NavigationOptionsMenuAdapter.CARDDETAILS_ACTION: {
			mCardDetails = new CardDetailsTabletFrag();
			CardData profileData = new CardData();
			profileData._id="0";
			CardDataGenralInfo profileInfo=new CardDataGenralInfo();
			profileInfo.title=myplexapplication.getUserProfileInstance().getName();
			if(myplexapplication.getUserProfileInstance().joinedDate==null)
				myplexapplication.getUserProfileInstance().joinedDate=myplexapplication.getUserProfileInstance().lastVisitedDate;
			profileInfo.briefDescription="Joined myplex on: "+myplexapplication.getUserProfileInstance().joinedDate+" \n Last Visited on: "+myplexapplication.getUserProfileInstance().lastVisitedDate+" ";
			profileInfo.description="Joined myplex on: "+myplexapplication.getUserProfileInstance().joinedDate+" \n Last Visited on: "+myplexapplication.getUserProfileInstance().lastVisitedDate+" ";
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
			myplexapplication.mSelectedCard = profileData;
			startActivity(new Intent(this,TabletCardDetails.class));
				setActionBarTitle("My Profile");
			return;
		}
		case NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION: {
			mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
			CardExplorerData data = myplexapplication.getCardExplorerData();
			data.reset();
			if (label
					.equalsIgnoreCase(NavigationOptionsMenuAdapter.FAVOURITE)) {
				data.requestType = CardExplorerData.REQUEST_FAVOURITE;
				setActionBarTitle("my favourites");
			} else if (label
					.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)) {
				data.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
				setActionBarTitle("recommended");
			} else if (label
					.equalsIgnoreCase(NavigationOptionsMenuAdapter.MOVIES)) {
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery = "movie";
				setActionBarTitle("movies");
			} else if (label
					.equalsIgnoreCase(NavigationOptionsMenuAdapter.LIVETV)) {
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery = "live";
				setActionBarTitle("live TV");
			} else if (label
					.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)) {
				data.requestType = CardExplorerData.REQUEST_BROWSE;
				data.searchQuery = "tvshows";
				setActionBarTitle("TV Shows");
			}else if(label.equalsIgnoreCase(NavigationOptionsMenuAdapter.DOWNLOADS)){
				data.requestType = CardExplorerData.REQUEST_DOWNLOADS;
				setActionBarTitle("my downloads");
			}else if(label.equalsIgnoreCase(NavigationOptionsMenuAdapter.PURCHASES)){
				data.requestType = CardExplorerData.REQUEST_PURCHASES;
				setActionBarTitle("my purchases");
			}
			if(this instanceof TabletCardDetails){
				myplexapplication.mSelectedOption_Tablet = screenType;
				startActivity(new Intent(this,MultiPaneActivity.class));
				finish();
				return;
			}
			mCurrentFragment = mCardExplorer;
			break;
		}
		default: {
		}
			break;
		}
		pushFragment();
//		mDrawerList.setItemChecked(position, true);
//		setTitle(mMenuItemList.get(position).mLabel);
		if(mDrawerLayout != null){
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		
	}

	protected void pushFragment() {
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
		setActionBarTitle("myplex");
		transaction.replace(R.id.content_frame, mCurrentFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(mDrawerToggle != null){
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(mDrawerToggle != null){
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	private FilterMenuProvider mFilterMenuProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		if(mCurrentFragment != null){
			mCurrentFragment.setActionBarHeight(getActionBar().getHeight());
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mDrawerLayout != null){
			mDrawerLayout.isDrawerOpen(mDrawerList);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
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
	private View mParentLayoutView;
	public void setParentView(View v){
		mParentLayoutView = v;
	}
	private Bitmap mOrginalBitmap;
	private Blur mBlurEngine;
	private void addBlur() {
		if (mParentLayoutView == null) {
			return;
		}
		if (mFilterMenuPopup == null) {
			return;
		}
		try {
			// mFilterListView.setVisibility(View.INVISIBLE);
//			ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mFilterListView,
//					"alpha", 0f, 1f);
//			fadeAnim.setDuration(1200);
//			fadeAnim.addListener(new AnimatorListenerAdapter() {
//				public void onAnimationEnd(Animator animation) {
//					// mFilterListView.setVisibility(View.VISIBLE);
//				}
//			});
//			fadeAnim.start();
			mParentLayoutView.setDrawingCacheEnabled(true);
			mOrginalBitmap = mParentLayoutView.getDrawingCache();
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
					if (b == null || mFilterMenuPopup == null) {
						return;
					}
					
					Drawable d = new BitmapDrawable(b);
					mPopBlurredLayout.setBackgroundDrawable(d);
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(
							mPopBlurredLayout, "alpha", 0f, 1f);
					fadeAnim.setDuration(500);
					fadeAnim.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
							// mFilterListView.setVisibility(View.VISIBLE);
						}
					});
					fadeAnim.start();
					mParentLayoutView.setDrawingCacheEnabled(false);
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
		mFilterMenuPopupWindow = new PopupWindow(mFilterMenuPopup,LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(mCustomActionBarTitleLayout);
	}

	private RelativeLayout mPopBlurredLayout;
	@Override
	public void addFilterData(List<FilterMenudata> datalist,OnClickListener listener) {
		mFilterDelegate = listener;

		mFilterMenuPopup = mInflater.inflate(R.layout.filtermenupopup, null);
		mPopBlurredLayout = (RelativeLayout) mFilterMenuPopup.findViewById(R.id.fliterMenuBlurredLayout);
		mFilterListView = (ListView) mFilterMenuPopup.findViewById(R.id.listView1);

		mMenuDataList = datalist;
		FliterMenuAdapter adapter = new FliterMenuAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1,mMenuDataList);
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
			if (mCustomActionBarSearch.getVisibility() != View.VISIBLE && visibility == View.VISIBLE) {
				mCustomActionBarSearch.setVisibility(visibility);
				ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(mCustomActionBarSearch, "alpha", 0f, 1f);
				fadeinAnimation.setDuration(800);
				fadeinAnimation.start();
			} else{
				mCustomActionBarSearch.setVisibility(visibility);
			}
			mCustomActionBarSearch.setVisibility(View.GONE);
		}
	}
	@Override
	public void hidefilterMenu() {
		if(mCustomActionBarTitleLayout != null){
			mCustomActionBarTitleLayout.setOnClickListener(null);
		}
//		if(mCustomActionBarFilterImage != null){
//			mCustomActionBarFilterImage.setVisibility(View.INVISIBLE);
//		}
	}
	@Override
	public void showfilterMenu() {
		if(mCustomActionBarTitleLayout != null){
			mCustomActionBarTitleLayout.setOnClickListener(mOnFilterClickListener);
			Util.showFeedback(mCustomActionBarTitleLayout);
		}
//		if(mCustomActionBarFilterImage != null){
//			mCustomActionBarFilterImage.setVisibility(View.VISIBLE);
//		}
	}
	@Override
	public void searchButtonClicked() {
		// TODO Auto-generated method stub
	}
}
