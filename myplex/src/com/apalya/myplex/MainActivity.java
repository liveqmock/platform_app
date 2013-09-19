package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.R.color;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.menu.FilterMenuProvider;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;

public class MainActivity extends BaseActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	public LayoutInflater mInflater;
	public BaseFragment mCurrentFragment;
	private Stack<BaseFragment> mFragmentStack = new Stack<BaseFragment>();
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();

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
		mMenuItemList.add(new NavigationOptionsMenu("Profile",
				R.drawable.menu_profile, null, 0));
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
		mMenuItemList.add(new NavigationOptionsMenu("Home Tablet Animations",
				R.drawable.menu_home, null, 2));
		mMenuItemList.add(new NavigationOptionsMenu("Logout",
				R.drawable.menu_logout, null, 1));
	}
	private OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(mCurrentFragment instanceof CardExplorer){
				((CardExplorer)mCurrentFragment).setSelectedAnimation(arg2);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
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
			} else if(menu.type == 0){
				v = mInflater.inflate(R.layout.navigation_menuitemlarge, null);
				NetworkImageView image = (NetworkImageView) v
						.findViewById(R.id.drawer_list_item_image);
				image.setDefaultImageResId(menu.resID);
				image.setImageUrl(menu.ImageUrl, MyVolley.getImageLoader());
			}else {
				v = mInflater.inflate(R.layout.navigation_menuitemspinner, null);
				Spinner spinner = (Spinner)v.findViewById(R.id.drawer_spinner1);
				spinner.setOnItemSelectedListener(mItemSelectedListener);
				
			}
//			Random rnd = new Random();
//			int color = Color.argb(5, rnd.nextInt(128), rnd.nextInt(128),
//					rnd.nextInt(64));
//			v.setBackground(new ColorDrawable(color));
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
		
		mInflater = LayoutInflater.from(this);
		fillMenuItem();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
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
		// getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		// getActionBar().setCustomView(R.layout.applicationtitle);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(1);
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

	@Override
	public void onBackPressed() {
		try {
			BaseFragment fragment  = mFragmentStack.pop();
			if(fragment instanceof CardExplorer){
				finish();
				return;
			}
			fragment = mFragmentStack.peek();
			if (fragment != null) {
				bringFragment(fragment);
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		super.onBackPressed();

	}

	public void bringFragment(BaseFragment fragment) {
		if (fragment == null) {
			return;
		}
		mCurrentFragment = fragment;
		pushFragment();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mPlusClient.connect();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mPlusClient.disconnect();
	}
	
	private void selectItem(int position) {
		switch (position) {
		case 0:
			mCurrentFragment = new CardDetails();
			break;
		case 1:{
			CardExplorer fragment = new CardExplorer();
			fragment.setDisplayMode(CardExplorer.STACKVIEW);
			mCurrentFragment = fragment;
			}
			break;
		case 2:{
			CardExplorer fragment = new CardExplorer();
			fragment.setDisplayMode(CardExplorer.GOOGLECARDVIEW);
			mCurrentFragment = fragment;
			}
			break;
		case 3:
			mCurrentFragment = new SearchActivity();
			break;
		case 4:
			mCurrentFragment = new CardExplorer();
			break;
		case 5:
			mCurrentFragment = new CardExplorer();
			break;
		case 7:{
			onClickLogout();
			//finish();
			//startActivity(new Intent(MainActivity.this,LoginActivity.class));
			break;
		}
		default:
			mCurrentFragment = new CardExplorer();
			break;
		}
		pushFragment();
		mDrawerList.setItemChecked(position, true);
		setTitle(mMenuItemList.get(position).label);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void pushFragment() {
		mFragmentStack.push(mCurrentFragment);
		mCurrentFragment.setContext(this);
		mCurrentFragment.setActionBar(getActionBar());
		mCurrentFragment.setMainActivity(this);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(android.R.animator.fade_in,
				android.R.animator.fade_out);
		transaction.replace(R.id.content_frame, mCurrentFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
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
		
		MenuItem refresh;
		refresh = menu.findItem(R.id.menu_progress);
		refresh.setActionView(R.layout.actionbarprogress);
		refresh.setVisible(false);
		
		MenuItem filter;
		filter = menu.findItem(R.id.menu_filter);
		if(filter.getActionProvider() instanceof FilterMenuProvider){
			mFilterMenuProvider = (FilterMenuProvider)filter.getActionProvider();
		}
//		filter.setVisible(false);
		
		mCurrentFragment.mActionBarProgressItem = refresh;

//		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FAAC58")));
		// mCardView.setActionBarHeight(getActionBar().getHeight());
		mCurrentFragment.setActionBarHeight(getActionBar().getHeight());
		return super.onCreateOptionsMenu(menu);

		// MenuInflater inflater=getMenuInflater();
		// inflater.inflate(R.menu.optmenu, menu);
		// ActionBar actionBar = getActionBar();
		// mCardView.setActionBarHeight(actionBar.getHeight());
		// return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
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
		switch (item.getItemId()) {
		case R.id.menu_search:{
			if((mCurrentFragment instanceof SearchActivity)){
				CardExplorer fragment = new CardExplorer();
				bringFragment(fragment);
			}else{
				SearchActivity fragment = new SearchActivity();
				bringFragment(fragment);
			}
			break;
		}
		default:
			if(mCurrentFragment != null){
				return mCurrentFragment.onOptionsItemSelected(item);
			}
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public void addFilterData(List<FilterMenudata> datalist,OnClickListener listener) {
		if(mFilterMenuProvider != null){
			mFilterMenuProvider.addFilterData(datalist,listener);
		}
	}
}
