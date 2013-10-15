package com.apalya.myplex.tablet;

import com.apalya.myplex.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
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
	}

}
