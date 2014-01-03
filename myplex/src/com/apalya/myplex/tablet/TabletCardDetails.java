package com.apalya.myplex.tablet;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardDetailsTabletFrag;
import com.apalya.myplex.fragments.CardExplorer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class TabletCardDetails extends BaseActivity{
	private ListView mLeftNavigationListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mainview);
		super.onCreate(savedInstanceState);
		mLeftNavigationListView = (ListView)findViewById(R.id.left_drawer);
		prepareNavigationMenuList(mLeftNavigationListView);
		prepareCustomActionBar();
		CardDetailsTabletFrag cardDetails = (CardDetailsTabletFrag) createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
		cardDetails.mDataObject = myplexapplication.mSelectedCard;
		mCurrentFragment = cardDetails;
		pushFragment();
		hideActionBarProgressBar();
		enableFilterAction(false);
		setSearchBarVisibilty(View.INVISIBLE);
		setUpShareButton(((CardData)cardDetails.mDataObject).generalInfo.title.toLowerCase());
		if(mDrawerLayout != null){
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}
	@Override
	public void onBackPressed() {
		startActivity(new Intent(TabletCardDetails.this,MultiPaneActivity.class));
		finish();
	}
	@Override
	public void fillMenuItem() {
		super.fillMenuItem();
	}
}
