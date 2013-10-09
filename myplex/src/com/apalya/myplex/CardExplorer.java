package com.apalya.myplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataGenre;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.FavouriteUtil.FavouriteCallback;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardView;
import com.apalya.myplex.views.PackagePopUp;
import com.fasterxml.jackson.core.JsonParseException;

public class CardExplorer extends BaseFragment implements CardActionListener,CacheManagerCallback,
		OnDismissCallback {
	public static final String TAG = "CardExplorer";
	private CardView mCardView;
	private CardExplorerData mData;
	private CacheManager mCacheManager = new CacheManager();
	private View mRootView;
	private ProgressDialog mProgressDialog = null;

	@Override
	public void open(CardData object) {
		BaseFragment fragment = mMainActivity.createFragment(MainActivity.CARDDETAILS);
		fragment.setDataObject(object);
		mMainActivity.bringFragment(fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mData = myplexapplication.getCardExplorerData();
		Log.d(TAG,"onCreate");
	}

	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(getContext(), "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	public void updateText(final String str) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null) {
					mProgressDialog.setTitle(str);
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		mRootView = inflater.inflate(R.layout.cardbrowsing, container, false);
		mCardView = (CardView) mRootView.findViewById(R.id.framelayout);
		// mGoogleCardListView.setAdapter(mGoogleCardListViewAdapter);
		mCardView.setVisibility(View.VISIBLE);
		mCardView.setVerticalScrollBarEnabled(false);
		mCardView.setHorizontalScrollBarEnabled(false);
		mCardView.setContext(getContext());
		mCardView.setActionBarHeight(getActionBar().getHeight());
		mCardView.setCardActionListener(this);
		mMainActivity.setTitle("Home");
		mMainActivity.setPotrait();
		delayedAction();
		return mRootView;
	}

	public void delayedAction() {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				showProgressBar();
//				prepareDummyData();
				if(verifyResume()){
					fetchMinData();	
				}
				applyData();
			}
		});
	}
	private boolean verifyResume() {
		if(mData.continueWithExisting && mData.mMasterEntries.size() > 0){
			applyData();
			return false;
		}
		return true;
	}
	@Override
	public void setActionBarHeight(int height) {
		super.setActionBarHeight(height);
		mCardView.setActionBarHeight(height);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void loadmore(int value) {
		mData.mStartIndex = value;
		fetchMinData();
	}

	private void fetchMinData() {
		mMainActivity.showActionBarProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		String requestUrl = new String();
		if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELMIN,mData.mStartIndex);
		}else if(mData.requestType == CardExplorerData.REQUEST_RECOMMENDATION){
			requestUrl = ConsumerApi.getRecommendation(ConsumerApi.LEVELMIN,mData.mStartIndex);
		}else if(mData.requestType == CardExplorerData.REQUEST_FAVOURITE){
			requestUrl = ConsumerApi.getFavourites(ConsumerApi.LEVELMIN,mData.mStartIndex);
		}
		StringRequest myReg = new StringRequest(requestUrl, deviceMinSuccessListener(), responseErrorListener());
		myReg.setShouldCache(true);
		Log.d(TAG,"Min Request:"+requestUrl);
		queue.add(myReg);
	}
	
	private Response.Listener<String> deviceMinSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					updateText("parsing results");
					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(minResultSet.results != null){
						Log.d(TAG,"Number of Result for the MIN request:"+minResultSet.results.size());
					}
					mCacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	private void prepareFilterData() {
		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
		List<String> tempList = new ArrayList<String>();
		for (CardData data : mData.mMasterEntries) {
			if(data.content != null && data.content.genre != null){
				for(CardDataGenre genreData:data.content.genre){
					if(genreData.name != null && !tempList.contains(genreData.name))
						tempList.add(genreData.name);
				}
			}
		}
		if(tempList.size() > 1){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, "All", 0));
		}
		for(String filterName:tempList){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, filterName, 0));
		}
		if(isVisible()){
			mMainActivity.addFilterData(filteroptions, mFilterMenuClickListener);
		}
	}

	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				String label = ((FilterMenudata) v.getTag()).label;
				if (label != null && label.equalsIgnoreCase("All")) {
					sort(mData.mMasterEntries);
					return;
				}
				ArrayList<CardData> localData = new ArrayList<CardData>();
				for (CardData data : mData.mMasterEntries) {
					boolean itemPresent = false;
					if (data.content != null && data.content.genre!= null) {
						for(CardDataGenre genreData:data.content.genre){
							if(genreData.name!= null && genreData.name.equalsIgnoreCase(label)){
								itemPresent = true;
								break;
							}
						}
					}
					if(itemPresent){
						localData.add(data);
					}
				}
				sort(localData);
			}
		}
	};

	private void sort(ArrayList<CardData> localData) {
		mCardView.forceUpdateData(localData);
	}

	private void applyData() {
		if(mData.mMasterEntries.size() == 0){
			return;
		}
		updateText("preparing ui");
		mCardView.addData(mData.mMasterEntries);
		mCardView.show();
		mCardView.sendViewReadyMsg(true);
		prepareFilterData();
		dismissProgressBar();
		mMainActivity.hideActionBarProgressBar();
	}

	private void showErrorDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setMessage("No Response from server");
		b.show();
	}

	private Response.ErrorListener responseErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				showErrorDialog();
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
			}
		};
	}
	@Override
	public void favouriteAction(final CardData data,int type){
		FavouriteUtil favUtil = new FavouriteUtil();
		favUtil.addFavourite(type, data, new FavouriteCallback() {
			
			@Override
			public void response(boolean value) {
				if(value){
//					Toast.makeText(getContext(), "Chan", Toast.LENGTH_SHORT).show();
				}else{
//					Toast.makeText(getContext(), "Add as Favourite", Toast.LENGTH_SHORT).show();
				}
				mCardView.updateData(data);
			}
		});
	}

	@Override
	public void selectedCard(int index) {
		mData.currentSelectedCard = index;
	}

	@Override
	public void deletedCard(CardData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void purchase(CardData data) {
		PackagePopUp popup = new PackagePopUp(getContext());
		popup.showPackDialog(data, getActionBar().getCustomView());
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewReady() {
		mCardView.moveTo(mData.currentSelectedCard);		
	}

	@Override
	public void OnCacheResults(HashMap<String, CardData> object ) {
		if(object == null){return;}
		mMainActivity.hideActionBarProgressBar();
		dismissProgressBar();
		Set<String> keySet = object.keySet();
			
		for(String key:keySet){
//			mData.mEntries.add(object.get(key));
			if(mData.mEntries.get(key) == null){
				mData.mEntries.put(key,object.get(key));
				mData.mMasterEntries.add(object.get(key));
			}
		}
		applyData();
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		if(dataList == null){return;}
		for(CardData data:dataList){
//			mData.mEntries.add(data);
			if(mData.mEntries.get(data._id) == null){
				mData.mEntries.put(data._id,data);
				mData.mMasterEntries.add(data);
			}
			
		}
		applyData();		
	}

	@Override
	public void OnOnlineError(VolleyError error) {
		// TODO Auto-generated method stub
		
	}
}
