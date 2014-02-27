package com.apalya.myplex.tablet;


import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardDetailsTabletFrag;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.utils.Analytics;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

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
		//Analytics.stoppedAt();
		//Analytics.mixPanelVideoTimeCalculation(myplexapplication.mSelectedCard);
		
		startActivity(new Intent(TabletCardDetails.this,MultiPaneActivity.class));
		finish();
	}
	@Override
	public void fillMenuItem() {
		//Analytics.stoppedAt();
		//Analytics.mixPanelVideoTimeCalculation(myplexapplication.mSelectedCard);
		super.fillMenuItem();
	}
}
