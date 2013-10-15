package com.apalya.myplex.tablet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.NavigationOptionsMenu;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardExplorer;

public class MultiPaneActivity extends BaseActivity {

	private ListView mLeftNavigationListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.cardexplorer_tablet);
		super.onCreate(savedInstanceState);
		mLeftNavigationListView = (ListView) findViewById(R.id.left_drawer);
		prepareNavigationMenuList(mLeftNavigationListView);
		
		mContentLayout = (FrameLayout) findViewById(R.id.content_frame);
		prepareCustomActionBar();
	}
	@Override
	public void OnSelectedOption(int position) {
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		CardExplorer mCardExplorer = (CardExplorer) createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
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
		pushFragment();
		mDrawerList.setItemChecked(position, true);
		setTitle(mMenuItemList.get(position).mLabel);
		if(mDrawerLayout != null){
			mDrawerLayout.closeDrawer(mDrawerList);
		}
//		startActivity(new Intent(MultiPaneActivity.this,TabletCardDetails.class));
	}
	@Override
	public void fillMenuItem() {
		 mMenuItemList.add(new NavigationOptionsMenu(myplexapplication.getUserProfileInstance().getName(),R.drawable.menu_profile,myplexapplication.getUserProfileInstance().getProfilePic(),NavigationOptionsMenuAdapter.CARDDETAILS,R.layout.navigation_menuitemlarge));
		 mMenuItemList.add(new NavigationOptionsMenu(NavigationOptionsMenuAdapter.FAVOURITE,R.drawable.iconfav,null,NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("Purchases",R.drawable.iconpurchases, null,
		 NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("Downloads",R.drawable.icondnload, null,
		 NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("Settings",R.drawable.iconsearch, null,
		 NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("Invite Friends",R.drawable.iconfriends, null,
		 NavigationOptionsMenuAdapter.NOACTION,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("Logout",R.drawable.menu_logout, null,
		 NavigationOptionsMenuAdapter.LOGOUT,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu("ApplicationLogo",R.drawable.menu_logout, null,
		 NavigationOptionsMenuAdapter.NOFOCUS,R.layout.applicationlogolayout));
		 mMenuItemList.add(new
		 NavigationOptionsMenu(NavigationOptionsMenuAdapter.RECOMMENDED,R.drawable.menu_home,
		 null,
		 NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu(NavigationOptionsMenuAdapter.MOVIES,R.drawable.iconmovie,
		 null,
		 NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu(NavigationOptionsMenuAdapter.LIVETV,R.drawable.iconlivetv,
		 null,
		 NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
		 mMenuItemList.add(new
		 NavigationOptionsMenu(NavigationOptionsMenuAdapter.TVSHOWS,R.drawable.icontv,
		 null,
		 NavigationOptionsMenuAdapter.CARDEXPLORER,R.layout.navigation_menuitemsmall));
	}
}
