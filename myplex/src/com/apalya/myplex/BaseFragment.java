package com.apalya.myplex;

import java.util.List;

import com.apalya.myplex.data.FilterMenudata;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseFragment extends Fragment{

	public Context mContext;
	public ActionBar mActionBar;
	public int mActionBarHeight;
	public MenuItem mActionBarProgressItem;
	public MainActivity mMainActivity;
	public Object mDataObject;
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
	public void setActionBarHeight(int height){
		this.mActionBarHeight = height;
	}
	public void setMainActivity(MainActivity activity){
		this.mMainActivity = activity;
	}
	public void setDataObject(Object obj){
		this.mDataObject = obj;
	}
	public void showActionBarProgress(){
		if(mActionBarProgressItem != null)
		mActionBarProgressItem.setVisible(true);
	}
	public void hideActionBarProgress(){
		if(mActionBarProgressItem != null)
		mActionBarProgressItem.setVisible(false);
	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return super.onOptionsItemSelected(item);
//	}
}
