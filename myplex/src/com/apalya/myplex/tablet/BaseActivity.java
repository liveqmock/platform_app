package com.apalya.myplex.tablet;

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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.R;
import com.apalya.myplex.SearchActivity;
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
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

public class BaseActivity extends Activity implements MainBaseOptions{
	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	// private CharSequence mDrawerTitle;
	// private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;

	public FrameLayout mContentLayout;
	public Context mContext;
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();

	NavigationOptionsMenuAdapter mNavigationAdapter;

	public void setLandscape() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	@Override
	public void setPotrait() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public void setSensor() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
					mNavigationDrawerOpened = false;
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					mNavigationDrawerOpened = true;
					invalidateOptionsMenu();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		OnSelectedOption(8);
	}

	public void fillMenuItem() {

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
		ImageView navigationDrawer = (ImageView) v
				.findViewById(R.id.customactionbar_drawer);
		if (mDrawerLayout == null) {
			navigationDrawer.setVisibility(View.INVISIBLE);
		}
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
		mCustomActionBarSearch.setVisibility(View.INVISIBLE);

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
			OnSelectedOption(position);
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

	public void OnSelectedOption(int position) {
//		NavigationOptionsMenu menu = mMenuItemList.get(position);
//
//		switch (menu.mScreenType) {
//		case NavigationOptionsMenuAdapter.DOWNLOADS: {
//			Util.showDownloads(this);
//			return;
//		}
//		case NavigationOptionsMenuAdapter.NOACTION: {
//			return;
//		}
//		case NavigationOptionsMenuAdapter.LOGOUT: {
//			LogOutUtil.onClickLogout(this);
//			return;
//		}
//		case NavigationOptionsMenuAdapter.CARDDETAILS: {
//			break;
//		}
//		case NavigationOptionsMenuAdapter.CARDEXPLORER: {
//			mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
//			CardExplorerData data = myplexapplication.getCardExplorerData();
//			data.reset();
//			if (menu.mLabel
//					.equalsIgnoreCase(NavigationOptionsMenuAdapter.FAVOURITE)) {
//				data.requestType = CardExplorerData.REQUEST_FAVOURITE;
//			} else if (menu.mLabel
//					.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)) {
//				data.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
//			} else if (menu.mLabel
//					.equalsIgnoreCase(NavigationOptionsMenuAdapter.MOVIES)) {
//				data.searchQuery = "movie";
//			} else if (menu.mLabel
//					.equalsIgnoreCase(NavigationOptionsMenuAdapter.LIVETV)) {
//				data.searchQuery = "live";
//			} else if (menu.mLabel
//					.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)) {
//				data.searchQuery = "tvshows";
//			}
//			mCurrentFragment = mCardExplorer;
//			break;
//		}
//		default: {
//		}
//			break;
//		}
//		pushFragment();
//		mDrawerList.setItemChecked(position, true);
//		setTitle(mMenuItemList.get(position).mLabel);
//		mDrawerLayout.closeDrawer(mDrawerList);
	}

	protected void pushFragment() {
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

	private void addBlur() {
		if (mCurrentFragment == null) {
			return;
		}
		if (mCurrentFragment.getView() == null) {
			return;
		}
		if (mFilterMenuPopup == null) {
			return;
		}
		try {
			// mFilterListView.setVisibility(View.INVISIBLE);
			ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mFilterListView,
					"alpha", 0f, 1f);
			fadeAnim.setDuration(1200);
			fadeAnim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
					// mFilterListView.setVisibility(View.VISIBLE);
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

		mPopBlurredLayout = (RelativeLayout) mFilterMenuPopup
				.findViewById(R.id.fliterMenuBlurredLayout);

		mFilterListView = (PinnedSectionListView) mFilterMenuPopup
				.findViewById(R.id.listView1);

		mMenuDataList = datalist;

		FliterMenuAdapter adapter = new FliterMenuAdapter(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				mMenuDataList);

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

			if (mCustomActionBarSearch.getVisibility() != View.VISIBLE
					&& visibility == View.VISIBLE) {
				mCustomActionBarSearch.setVisibility(visibility);
				ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(
						mCustomActionBarSearch, "alpha", 0f, 1f);
				fadeinAnimation.setDuration(800);
				fadeinAnimation.start();
			} else
				mCustomActionBarSearch.setVisibility(visibility);
		}
	}

}
