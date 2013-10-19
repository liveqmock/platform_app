package com.apalya.myplex.tablet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.data.myplexapplication;

public class MultiPaneActivity extends BaseActivity {

	private ListView mLeftNavigationListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator.inflate(R.layout.cardexplorer_tablet, null);
		setContentView(v);
		setParentView(v);
		super.onCreate(savedInstanceState);
		mLeftNavigationListView = (ListView) findViewById(R.id.left_drawer);
		prepareNavigationMenuList(mLeftNavigationListView);
		mContentLayout = (FrameLayout) findViewById(R.id.content_frame);
		prepareCustomActionBar();
		enableFilterAction(true);
	}
	@Override
	protected void onResume() {
		if(myplexapplication.mSelectedOption_Tablet == NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION){
			createCardExplorer();
		}else{
			OnSelectedOption(myplexapplication.mSelectedOption_Tablet, "");
		}
		super.onResume();
	}
	@Override
	public void fillMenuItem() {
		super.fillMenuItem();
	}
}
