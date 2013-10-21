package com.apalya.myplex;

import java.util.List;

import android.view.View.OnClickListener;

import com.apalya.myplex.data.FilterMenudata;

public interface MainBaseOptions {
	public BaseFragment createFragment(int fragmentType);
	
	public void hidefilterMenu();
	
	public void showfilterMenu();

	public void setPotrait();

	public void setSearchBarVisibilty(int visible);

	public void hideActionBar();

	public void showActionBar();

	public void hideActionBarProgressBar();

	public void addFilterData(List<FilterMenudata> datalist,
			OnClickListener listener);

	public void showActionBarProgressBar();

	public void bringFragment(BaseFragment fragment);

	public void enableFilterAction(boolean b);

	public void setActionBarTitle(String title);

	public void updateActionBarTitle();
	
	public void searchButtonClicked();
}
