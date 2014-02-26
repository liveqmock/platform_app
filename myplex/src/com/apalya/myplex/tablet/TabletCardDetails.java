package com.apalya.myplex.tablet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardDetailsTabletFrag;

public class TabletCardDetails extends BaseActivity{
	private ListView mLeftNavigationListView;
	private ImageView backkView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mainview_tablet);
		super.onCreate(savedInstanceState);
		//mLeftNavigationListView = (ListView)findViewById(R.id.left_drawer);
		///prepareNavigationMenuList(mLeftNavigationListView);
		prepareCustomActionBar();
		CardDetailsTabletFrag cardDetails = (CardDetailsTabletFrag) createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
		cardDetails.mDataObject = myplexapplication.mSelectedCard;
		mCurrentFragment = cardDetails;
		pushFragment();
		View v =getActionBar().getCustomView();
         backkView = (ImageView)v.findViewById(R.id.customactionbar_back);
		
		backkView.setVisibility(View.VISIBLE);
		backkView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				onBackPressed();
				
				// TODO Auto-generated method stub
				
			}
		});
		//super.setActionBackVisible(View.VISIBLE);
		
		/*
		hideActionBarProgressBar();
		enableFilterAction(false);
		setSearchBarVisibilty(View.INVISIBLE);
		setUpShareButton(((CardData)cardDetails.mDataObject).generalInfo.title.toLowerCase());
		if(mDrawerLayout != null){
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	
	*/
	
	
	
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
