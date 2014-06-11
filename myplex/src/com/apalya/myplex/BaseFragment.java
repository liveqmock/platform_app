package com.apalya.myplex;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;

public class BaseFragment extends Fragment {

	public Context mContext;
	public ActionBar mActionBar;
	public int mActionBarHeight;
	public MainBaseOptions mMainActivity;
	public Object mDataObject;
	public boolean mEnableMinimizedView = true;

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}

	public ActionBar getActionBar() {
		return mActionBar;
	}

	public void setActionBar(ActionBar mActionBar) {
		this.mActionBar = mActionBar;
	}

	public void setActionBarHeight(int height) {
		this.mActionBarHeight = height;
	}

	public void setMainActivity(MainBaseOptions activity) {
		this.mMainActivity = activity;
	}

	public void setDataObject(Object obj) {
		this.mDataObject = obj;
	}

	public void searchButtonClicked() {

	}
	public boolean onBackClicked() {
		return false;
	}
	
	public void setEnableMinimizedView(boolean mEnableMinimizedView) {
		this.mEnableMinimizedView = mEnableMinimizedView;
	}
}
