package com.apalya.myplex.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.R.id;
import com.apalya.myplex.R.layout;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardTabletAdapater;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataGenre;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.FetchDownloadData;
import com.apalya.myplex.data.FetchDownloadData.FetchDownloadDataListener;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.tablet.TabletCardDetails;
import com.apalya.myplex.utils.Analytics;
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
	private GridView mGridView;
	private CardExplorerData mData;
	private CacheManager mCacheManager = new CacheManager();
	private CardTabletAdapater mTabletAdapter;
	private View mRootView;
	private ProgressDialog mProgressDialog = null;
	public CardData mSelectedCard = null;
	private String screenName;

	@Override
	public void open(CardData object) {
		
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", object._id);
		params.put("CardType", object.generalInfo.type);
		params.put("CardName", object.generalInfo.title);
		Analytics.trackEvent(Analytics.cardBrowseTouch,params);
		
		if(getResources().getBoolean(R.bool.isTablet)){
			myplexapplication.mSelectedCard = object;
			startActivity(new Intent(getContext(),TabletCardDetails.class));
			getActivity().finish();
		}else{
			BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
			fragment.setDataObject(object);
			mMainActivity.bringFragment(fragment);	
		}
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
	private void arangeWaterMark(){
		ImageView waterMark = (ImageView) mRootView.findViewById(R.id.watermarkicon);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		int remainingSpace  = myplexapplication.getApplicationConfig().screenHeight;
		remainingSpace -= (int)getResources().getDimension(R.dimen.cardHeight);
		remainingSpace -= (int)getResources().getDimension(R.dimen.margin_gap_12);
		remainingSpace -= (int)getResources().getDimension(R.dimen.margin_gap_12);
		remainingSpace -= (int)getResources().getDimension(R.dimen.cardstatusheight);
		remainingSpace -= (int)getResources().getDimension(R.dimen.watermarkheight);
		waterMark.setPadding(0, 0, 0, remainingSpace/2);
		waterMark.setLayoutParams(params);
	}
	@Override
	public void onStop() {
	// TODO Auto-generated method stub
		Log.d(TAG,"onStop");
		super.onStop();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		System.gc();
		if(isVisible()){
			mMainActivity.addFilterData(new ArrayList<FilterMenudata>(), mFilterMenuClickListener);
		}
		mRootView = inflater.inflate(R.layout.cardbrowsing, container, false);
		mCardView = (CardView) mRootView.findViewById(R.id.framelayout);
		mGridView = (GridView)mRootView.findViewById(R.id.tabletview);
		arangeWaterMark();
		if(getResources().getBoolean(R.bool.isTablet)){
			mTabletAdapter = new CardTabletAdapater(getContext());
			mTabletAdapter.setCardActionListener(this);
			mCardView.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
			mGridView.setAdapter(mTabletAdapter);
			
		}else{
			mCardView.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
		}
		// mGoogleCardListView.setAdapter(mGoogleCardListViewAdapter);
		mCardView.setVerticalScrollBarEnabled(false);
		mCardView.setHorizontalScrollBarEnabled(false);
		mCardView.setContext(getContext());
		mCardView.setActionBarHeight(getActionBar().getHeight());
		mCardView.setCardActionListener(this);
//		mMainActivity.setTitle("Home");
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			mMainActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		mMainActivity.setSearchBarVisibilty(View.VISIBLE);
		mMainActivity.enableFilterAction(true);
		delayedAction();
		return mRootView;
	}

	public void delayedAction() {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				showProgressBar();
				if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
					fillDownloadList();
				}else{
	//				prepareDummyData();
					if(verifyResume()){
						fetchMinData();	
					}
					applyData();
				}
			}
		});
	}
	private boolean verifyResume() {
		if(mData.continueWithExisting && mData.mMasterEntries.size() > 0){
			applyData();
			if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
				dismissProgressBar();
				fetchMinData();
			}
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
		if(myplexapplication.getCardExplorerData().cardDataToSubscribe != null){
			mCardView.updateData(myplexapplication.getCardExplorerData().cardDataToSubscribe);
		}
		Log.d(TAG,"onResume");
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	public void fillDownloadList(){
		FetchDownloadData d = new FetchDownloadData(getContext());
		d.fetchDownload(new FetchDownloadDataListener() {
			
			@Override
			public void completed() {
			showNoDataMessage(false);
				
			}
		});
	}
	@Override
	public void loadmore(int value) {
		if(mData.requestType != CardExplorerData.REQUEST_DOWNLOADS && mData.requestType != CardExplorerData.REQUEST_SIMILARCONTENT){
			mData.mStartIndex++;
			fetchMinData();
		}
	}

	private void fetchMinData() {
		if(mData.requestType == CardExplorerData.REQUEST_SIMILARCONTENT){
			return;
		}
		mMainActivity.showActionBarProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		int requestMethod = Method.GET;
		String requestUrl = new String();
		if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELMIN,mData.mStartIndex);
			screenName="Search";
		}else if(mData.requestType == CardExplorerData.REQUEST_RECOMMENDATION){
			requestUrl = ConsumerApi.getRecommendation(ConsumerApi.LEVELMIN,mData.mStartIndex);
			screenName="Search";
		}else if(mData.requestType == CardExplorerData.REQUEST_FAVOURITE){
			screenName="Favourite";
			requestUrl = ConsumerApi.getFavourites(ConsumerApi.LEVELMIN,mData.mStartIndex);
		}else if(mData.requestType == CardExplorerData.REQUEST_PURCHASES){
			screenName="Purchases";
			requestUrl = ConsumerApi.getPurchases(ConsumerApi.LEVELMIN,mData.mStartIndex);
			requestMethod = Method.POST;
		}
		
		Map<String,String> attrib=new HashMap<String, String>();
		attrib.put("Category", screenName);
		attrib.put("Duration", "");
		Analytics.trackEvent(Analytics.cardBrowseDuration,attrib,true);
		
		StringRequest myReg = new StringRequest(requestMethod, requestUrl, deviceMinSuccessListener(), responseErrorListener());
//		myReg.setShouldCache(true);
		Log.d(TAG,"Min Request:"+requestUrl);
		queue.add(myReg);
	}
	
	private Response.Listener<String> deviceMinSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Analytics.endTimedEvent(Analytics.cardBrowseDuration);
				try {
//					Log.d(TAG,"server response "+response);
					updateText("parsing results");
					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(minResultSet.code != 200){
						Util.showToast(getContext(), minResultSet.message,Util.TOAST_TYPE_ERROR);
//						Toast.makeText(getContext(), minResultSet.message, Toast.LENGTH_SHORT).show();
					}
					if(minResultSet.results != null){
						Log.d(TAG,"Number of Result for the MIN request:"+minResultSet.results.size());
					}
					if(minResultSet.results ==  null){showNoDataMessage(false);return;}
					if(minResultSet.results.size() ==  0){showNoDataMessage(false);return;}
					mCacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
				} catch (JsonParseException e) {
					showNoDataMessage(false);
					e.printStackTrace();
				} catch (IOException e) {
					showNoDataMessage(false);
					e.printStackTrace();
				}
			}
		};
	}
	private void showNoDataMessage(boolean issuedRequest){
		if(!issuedRequest){
			mMainActivity.hideActionBarProgressBar();
		}
		dismissProgressBar();
		if(mData.mMasterEntries.size() == 0){
			Util.showToast(getContext(),"No data found,Please try again.",Util.TOAST_TYPE_INFO);
//			Toast.makeText(getContext(), "No data found,Please try again.", Toast.LENGTH_SHORT).show();
		}
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
					
					Map<String,String> params=new HashMap<String, String>();
					params.put("FilterType", label);
					params.put("NumOfCards", String.valueOf(mData.mMasterEntries.size()));
					Analytics.trackEvent(Analytics.cardBrowseFilter,params);
					
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
				Map<String,String> params=new HashMap<String, String>();
				params.put("FilterType", label);
				params.put("NumOfCards", String.valueOf(localData.size()));
				Analytics.trackEvent(Analytics.cardBrowseFilter,params);
				sort(localData);
			}
		}
	};

	private void sort(ArrayList<CardData> localData) {
		if(getResources().getBoolean(R.bool.isTablet)){
			mTabletAdapter.forceUpdateData(localData);
		}else{
			mCardView.forceUpdateData(localData);
		}
	}

	private void applyData() {
		if(mData.mMasterEntries == null || mData.mMasterEntries.size() == 0){
			return;
		}
		updateText("preparing ui");
		if(getResources().getBoolean(R.bool.isTablet)){
			mTabletAdapter.setData(mData.mMasterEntries);
		}else{
			mCardView.addData(mData.mMasterEntries);
			mCardView.show();
			mCardView.sendViewReadyMsg(true);
		}
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
				Analytics.endTimedEvent(Analytics.cardBrowseDuration);
				//Analytics.endTimedEvent(Analytics.cardBrowseScreen);
				Log.d(TAG,"Error from server "+error.networkResponse);
				showErrorDialog();
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
			}
		};
	}
	@Override
	public void favouriteAction(final CardData data,int type){
		FavouriteUtil favUtil = new FavouriteUtil();
		
		//long id=Util.startDownload("", "", getContext());
		//myplexapplication.getUserProfileInstance().downloadMap.put(data._id, id);
		favUtil.addFavourite(type, data, new FavouriteCallback() {
			
			@Override
			public void response(boolean value) {
				String status="";
				if(value){
					status="Not Favourite";
//					Toast.makeText(getContext(), "Chan", Toast.LENGTH_SHORT).show();
				}else{
					status="Favourite";
//					Toast.makeText(getContext(), "Add as Favourite", Toast.LENGTH_SHORT).show();
				}
				
				Map<String,String> params=new HashMap<String, String>();
				params.put("Status", status);
				params.put("CardId", data._id);
				params.put("CardType", data.generalInfo.type);
				params.put("CardName", data.generalInfo.title);
				Analytics.trackEvent(Analytics.cardBrowseFavorite,params);
				if(getResources().getBoolean(R.bool.isTablet)){
					mTabletAdapter.notifyDataSetChanged();
				}else{
					mCardView.updateData(data);
				}
			}
		});
	}

	@Override
	public void selectedCard(CardData data,int index) {
		
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", data._id);
		params.put("CardType", data.generalInfo.type);
		params.put("CardName", data.generalInfo.title);
		Analytics.trackEvent(Analytics.cardBrowseSelect,params);
		
		mData.currentSelectedCard = index;
		mSelectedCard = data;
	}

//	public void updateDownloadInformation(){
//		if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
//			Map<String, Long> ids=myplexapplication.getUserProfileInstance().downloadMap;
//			for(CardData data:mData.mMasterEntries){
//				data.isESTEnabled = true;
//				data.ESTId = ids.get(data._id);
//			}
//		}
//	}
	@Override
	public void deletedCard(CardData data) {
		// TODO Auto-generated method stub
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", data._id);
		params.put("CardType", data.generalInfo.type);
		params.put("CardName", data.generalInfo.title);
		Analytics.trackEvent(Analytics.cardBrowseCancel,params);
	}

	@Override
	public void purchase(CardData data) {
		PackagePopUp popup = new PackagePopUp(getContext(),mRootView);
		mData.cardDataToSubscribe = data;
		popup.showPackDialog(data, getActionBar().getCustomView());
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", data._id);
		params.put("CardType", data.generalInfo.type);
		params.put("CardName", data.generalInfo.title);
		Analytics.trackEvent(Analytics.cardBrowsePurchase,params);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewReady() {
		
		
		
		if(getResources().getBoolean(R.bool.isTablet)){
			
		}else{
			mCardView.moveTo(mData.currentSelectedCard);
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mData.mMasterEntries.get(mData.currentSelectedCard)._id);
			params.put("CardType", mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.type);
			params.put("CardName", mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.title);
			Analytics.trackEvent(Analytics.cardBrowseSwipe,params);
		}
	}

	@Override
	public void OnCacheResults(HashMap<String, CardData> object ,boolean issuedRequest) {
		if(object == null){
			showNoDataMessage(issuedRequest);
			return;
		}
		int count = 0;
		Set<String> keySet = object.keySet();
			
		for(String key:keySet){
//			mData.mEntries.add(object.get(key));
			if(mData.mEntries.get(key) == null){
				mData.mEntries.put(key,object.get(key));
				mData.mMasterEntries.add(object.get(key));
				if(count == 0){
					mSelectedCard = object.get(key);
					count++;
				}
			}
		}
//		updateDownloadInformation();
		if(mData.mMasterEntries.size() != 0){
			showNoDataMessage(issuedRequest);
		}
		applyData();
		
		if(mData.mStartIndex==10)
		{
			Map<String,String> params=new HashMap<String, String>();
			params.put("Category", screenName);
			params.put("NumOfCards", String.valueOf(mData.mMasterEntries.size()));
			params.put("CacheResults", "true");
			params.put("PreviousScreen", "None");
			if(Util.isWifiEnabled(mContext))
			{
				params.put("Network", "Wifi");	
			}
			else
			{
				params.put("Network", "Mobile");
			}
			
			Analytics.trackEvent(Analytics.cardBrowseScreen,params);
		}
		else
		{
			Map<String,String> params=new HashMap<String, String>();
			params.put("Category", screenName);
			params.put("NumCardsAvailable", String.valueOf(mCardView.getDataList().size()));
			params.put("NumCardsFetched", String.valueOf(mData.mMasterEntries.size()));
			params.put("CacheResults", "true");
			params.put("PreviousScreen", "None");
			if(Util.isWifiEnabled(mContext))
			{
				params.put("Network", "Wifi");	
			}
			else
			{
				params.put("Network", "Mobile");
			}
			
			Analytics.trackEvent(Analytics.cardBrowseScreen,params);
		}
		
		
//		if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
//			showProgress();
//		}
		
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
		
		Map<String,String> params=new HashMap<String, String>();
			params.put("Category", screenName);
			params.put("NumOfCards", String.valueOf(dataList.size()));
			params.put("CacheResults", "false");
			params.put("PreviousScreen", "None");
			if(Util.isWifiEnabled(mContext))
			{
				params.put("Network", "Wifi");	
			}
			else
			{
				params.put("Network", "Mobile");
			}
			
			Analytics.trackEvent(Analytics.cardBrowseScreen,params);
		
//		updateDownloadInformation();
		showNoDataMessage(false);
		applyData();		
	}

	@Override
	public void OnOnlineError(VolleyError error) {
		showNoDataMessage(false);
	}
//	private void showProgress(){
//		final DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				boolean downloading = true;
//
//				while (downloading) {
//					try {
//						if(mSelectedCard == null){
//							continue;
//						}
//						DownloadManager.Query q = new DownloadManager.Query();
//						if(q == null){
//							continue;
//						}
//						Log.d(TAG,"Download information for "+mSelectedCard.ESTId);
//						q.setFilterById(mSelectedCard.ESTId);
//						if(mSelectedCard.ESTId == 0){
//							continue;
//						}
//						Cursor cursor = manager.query(q);
//						if(cursor == null){
//							continue;
//						}
//						cursor.moveToFirst();
//						int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//						int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//
//						if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//							downloading = false;
//							mSelectedCard.ESTProgressStatus = CardData.ESTDOWNLOADCOMPLETE;
//							mCardView.updateDownloadStatus(mSelectedCard);
//						}
//						else{
//							final int dl_progress = (bytes_downloaded * 100) / bytes_total;
//							mSelectedCard.ESTStatus = CardData.ESTDOWNLOADINPROGRESS;
//							mSelectedCard.ESTProgressStatus = dl_progress;
//							Log.d(TAG,"Download inform progress  "+dl_progress);
//							mCardView.updateDownloadStatus(mSelectedCard);
//						}
//						cursor.close();
//					} catch (Exception e) {
//						// TODO: handle exception
//					}
//					
//				}
//			}
//		}).start();
//	}
}
