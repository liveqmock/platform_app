package com.apalya.myplex.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GZipRequest;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardTabletAdapater;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataGenralInfo;
import com.apalya.myplex.data.CardDataGenre;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.FetchDownloadData;
import com.apalya.myplex.data.FetchDownloadData.FetchDownloadDataListener;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.tablet.TabletCardDetails;
import com.apalya.myplex.utils.AlertDialogUtil;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.FavouriteUtil.FavouriteCallback;
import com.apalya.myplex.utils.FetchDownloadProgress;
import com.apalya.myplex.utils.FetchDownloadProgress.DownloadProgressStatus;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardView;
import com.apalya.myplex.views.PackagePopUp;
import com.fasterxml.jackson.core.JsonParseException;

public class CardExplorer extends BaseFragment implements CardActionListener,CacheManagerCallback,DownloadProgressStatus,
		OnDismissCallback, AlertDialogUtil.NoticeDialogListener {
	public static final String TAG = "CardExplorer";
	private CardView mCardView;
	private GridView mGridView;
	private CardExplorerData mData;
	private CacheManager mCacheManager = new CacheManager();
	private CardTabletAdapater mTabletAdapter;
	private View mRootView;
	private RelativeLayout mNewArrivalLayout;
	private TextView mNewArrivalTextView;
	private ProgressDialog mProgressDialog = null;
	public CardData mSelectedCard = null;
	private GZipRequest mVolleyRequest;
	private String screenName;
	private LinearLayout mStackFrame;
	private HashMap<CardData,Integer> mDownloadTracker= new HashMap<CardData,Integer>();
	private boolean mRefreshOnce = false;
	
	@Override
	public void open(CardData object) {
		
		Map<String,String> params=new HashMap<String, String>();
		/*params.put("CardId", object._id);
		params.put("CardType", object.generalInfo.type);
		params.put("CardName", object.generalInfo.title);*/
		//???
		params.put(Analytics.CONTENT_ID_PROPERTY, object._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, object.generalInfo.title);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,object.generalInfo.type);
		//Analytics.trackEvent(Analytics.EVENT_PLAY,params);
		Analytics.trackEvent(Analytics.EVENT_BROWSE,params);
		mMainActivity.saveActionBarTitle();
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
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.BROWSE_TYPE_PROPERTY, Analytics.BROWSE_TYPES.Cards.toString());
		Analytics.trackEvent(Analytics.EVENT_BROWSE,params);
		Log.d(TAG,"onCreate");
	}

	public void showProgressBar() {
//		if (mProgressDialog != null) {
//			mProgressDialog.dismiss();
//		}
//		mProgressDialog = ProgressDialog.show(getContext(), "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
//		if (mProgressDialog != null) {
//			mProgressDialog.dismiss();
//		}
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
		Log.d(TAG,"onStop");
		closeSession();
		if(mDownloadProgressManager != null){
			mDownloadProgressManager.stopPolling();
		}
		super.onStop();
	}
	private void closeSession(){
		if(mCacheManager != null){
			mCacheManager.deRegistration();
		}
		if(mVolleyRequest != null){
			mVolleyRequest.cancel();
		}
	}
	private void showStackedFrame(){
		if(getResources() != null && getResources().getBoolean(R.bool.isTablet)){
			return;
		}
		if(mStackFrame != null){
			mStackFrame.setVisibility(View.VISIBLE);
		}
	}
	private void hideStackedFrame(){
		if(getResources() != null && getResources().getBoolean(R.bool.isTablet)){
			return;
		}
		if(mStackFrame != null){
			mStackFrame.setVisibility(View.INVISIBLE);
		}
	}
	private OnClickListener mNewArrivalClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if(getResources().getBoolean(R.bool.isTablet)){
				
			}else{
				mCardView.moveTo(mOldDataListSize);
			}
			hideNewArrivals();			
		}
	};
	private void showNewArrivals(){
		if(!isAdded())
			return;
		mNewArrivalLayout.setVisibility(View.VISIBLE);
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(mNewArrivalLayout, View.TRANSLATION_Y, -(mNewArrivalLayout.getHeight()+getResources().getDimension(R.dimen.margin_gap_4)),150));
		set.setDuration(1500);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
				AnimatorSet set1 = new AnimatorSet();
				set1.play(ObjectAnimator.ofFloat(mNewArrivalLayout, View.TRANSLATION_Y, 150,0));
				set1.setDuration(1500);
				set1.setInterpolator(new DecelerateInterpolator());
				set1.addListener(new AnimatorListenerAdapter(){
					public void onAnimationEnd(Animator animation) {
						
						startTimeOut();
					}
				});
				set1.start();
//				startTimeOut();
			}
		});
		set.start();
	}
	private void hideNewArrivals(){
		stopTimeOut();
		if(!isAdded())
			return;
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(mNewArrivalLayout, View.TRANSLATION_Y, 0,-(mNewArrivalLayout.getHeight()+getResources().getDimension(R.dimen.margin_gap_4))));
		set.setDuration(1500);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
//				mNewArrivalLayout.setVisibility(View.GONE);
			}
		});
		set.start();
	}
	private void startTimeOut(){
    	stopTimeOut();
    	Log.v(TAG,"startTimeOut");
    	mTimeOutHandler.sendEmptyMessageDelayed(TIMEOUT,timeoutInterval);
    }
    private void stopTimeOut(){
    	Log.v(TAG,"stopTimeOut");
    	try {
    		mTimeOutHandler.removeMessages(TIMEOUT);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	private long timeoutInterval = 1000 * 15; 
	private Handler mTimeOutHandler = new Handler(Looper.getMainLooper()) {
	
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case TIMEOUT:{
					Log.v(TAG,"mTimeOutHandler");
					hideNewArrivals();
					break;
				}
			}
		}
	};
	private static final int TIMEOUT = 1;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		System.gc();
		mAddDataAdded = false;
		if(isVisible()){
			mMainActivity.addFilterData(new ArrayList<FilterMenudata>(), mFilterMenuClickListener);
		}
		mRootView = inflater.inflate(R.layout.cardbrowsing, container, false);
		mNewArrivalLayout  = (RelativeLayout)mRootView.findViewById(R.id.new_arrival_layout);
		mNewArrivalLayout.setOnClickListener(mNewArrivalClickListener);
		mNewArrivalLayout.bringToFront();
		mNewArrivalLayout.setVisibility(View.INVISIBLE);
		mNewArrivalTextView  = (TextView)mRootView.findViewById(R.id.new_arrival_text);
		mNewArrivalTextView.setTypeface(FontUtil.Roboto_Medium);
		TextView newArrivalIcon = (TextView)mRootView.findViewById(R.id.new_arrival_icon);
		newArrivalIcon.setTypeface(FontUtil.ss_symbolicons_line);
		mCardView = (CardView) mRootView.findViewById(R.id.framelayout);
		mGridView = (GridView)mRootView.findViewById(R.id.tabletview);
		mStackFrame = (LinearLayout)mRootView.findViewById(R.id.cardstackframe);
		arangeWaterMark();
		if(getContext() == null)
		{
			return mRootView;
		}
		if(getResources().getBoolean(R.bool.isTablet)){
			mTabletAdapter = new CardTabletAdapater(getContext());
			mTabletAdapter.setCardActionListener(this);
			mCardView.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
			mGridView.setAdapter(mTabletAdapter);
			mGridView.setOnScrollListener(mTabletAdapter);
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
		mMainActivity.setSearchBarVisibilty(View.INVISIBLE);
		mMainActivity.setSearchViewVisibilty(View.VISIBLE);
		delayedAction();
		hideNewArrivals();
		return mRootView;
	}

	/*public void addCustomFavourites()
	{
		CardData addfav = new CardData();
		addfav._id = "257";
		addfav.generalInfo = new CardDataGenralInfo();
		addfav.generalInfo.title = "Bad Boys";
		addfav.generalInfo.type = "favourite";
		favouriteAction(addfav,FavouriteUtil.FAVOURITEUTIL_ADD);
		
		addfav = new CardData();
		addfav._id = "258";
		addfav.generalInfo = new CardDataGenralInfo();
		addfav.generalInfo.title = "Pursuit of happyness";
		addfav.generalInfo.type = "favourite";
		favouriteAction(addfav,FavouriteUtil.FAVOURITEUTIL_ADD);
		
		addfav = new CardData();
		addfav._id = "260";
		addfav.generalInfo = new CardDataGenralInfo();
		addfav.generalInfo.title = "Hitch";
		addfav.generalInfo.type = "favourite";
		favouriteAction(addfav,FavouriteUtil.FAVOURITEUTIL_ADD);
	}*/
	
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
				applyData();
			}
		});
	}
	@Override
	public void loadmore(int value) {
		if(mData.requestType != CardExplorerData.REQUEST_DOWNLOADS /*&& mData.requestType != CardExplorerData.REQUEST_SIMILARCONTENT*/){
			mData.mStartIndex++;
			fetchMinData();
		}
	}
	private boolean mOldDataAdded = false;
	private int mOldDataListSize  = 0;
	private boolean mAddDataAdded = false;
	private void fillOldData(final List<CardData> lastSavedData){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				mOldDataAdded = true;
				mOldDataListSize = lastSavedData.size();
				
				for(CardData data:lastSavedData){
					mData.mEntries.put(data._id,data);
					Log.d("CardExplorer","ID= "+data._id+ " olddata");
					mData.mMasterEntries.add(data);
				}
				applyData();
			}
		});
	}
	private void prepareLastSessionData(){
		Thread t = new Thread() {
			public void run() {
				CardExplorerData explorerData = myplexapplication.getCardExplorerData();
				if(explorerData.requestType == CardExplorerData.REQUEST_RECOMMENDATION){
					try {
						List<CardData> lastSavedData = (List<CardData>)Util.loadObject(myplexapplication.getApplicationConfig().lastViewedCardsPath);
						if(lastSavedData != null){
							Log.d("CardExplorer","last saved list size = "+lastSavedData.size());
						}
						if(lastSavedData != null && lastSavedData.size() > 0){
							fillOldData(lastSavedData);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "prepareLastSessionData loadObject exception");
					}
				}
			}
		};
		t.start();
	}
	private void fetchMinData() {
		mOldDataAdded = false;
		mOldDataListSize = 0;
		/*if(mData.requestType == CardExplorerData.REQUEST_SIMILARCONTENT){
			return;
		}*/
		mMainActivity.showActionBarProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		int requestMethod = Method.GET;
		String requestUrl = new String();
		if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			String searchScope;
			if(mData.searchScope == null || mData.searchScope.length() > 0)
			{
				Log.i(TAG,"Seachscope: "+ mData.searchScope);
				searchScope = mData.searchScope;
			}
			else
				searchScope = "movie";
			
				requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELDYNAMIC,mData.mStartIndex,searchScope);
			screenName="Search";
			mMainActivity.setActionBarTitle(mData.searchQuery.toLowerCase());
		}else if(mData.requestType == CardExplorerData.REQUEST_RECOMMENDATION){
			if(!mAddDataAdded){
				mAddDataAdded = true;
				prepareLastSessionData();
			}
			requestUrl = ConsumerApi.getRecommendation(ConsumerApi.LEVELDYNAMIC,mData.mStartIndex);
			screenName="Recommendation";
		}else if(mData.requestType == CardExplorerData.REQUEST_FAVOURITE){
			screenName="Favourite";
			requestUrl = ConsumerApi.getFavourites(ConsumerApi.LEVELDYNAMIC,mData.mStartIndex);
		}else if(mData.requestType == CardExplorerData.REQUEST_PURCHASES){
			screenName="Purchases";
			requestUrl = ConsumerApi.getPurchases(ConsumerApi.LEVELDYNAMIC,mData.mStartIndex);
			requestMethod = Method.POST;
		}else if(mData.requestType == CardExplorerData.REQUEST_BROWSE){
			screenName="Browse" + mData.searchQuery;
			requestUrl = ConsumerApi.getBrowse(mData.searchQuery,ConsumerApi.LEVELDYNAMIC, mData.mStartIndex);
		}else if(mData.requestType == CardExplorerData.REQUEST_INLINESEARCH)
		{
			screenName = "Inlinesearch";
			List<CardData> datatoSearch = new ArrayList<CardData>();
			CardData data = new CardData();
			data._id = mData.searchQuery;
			datatoSearch.add(data);
			mCacheManager.getCardDetails(datatoSearch,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
			return;
		}
		else if(mData.requestType == CardExplorerData.REQUEST_SIMILARCONTENT)
		{
			screenName="similarcontent for" +mData.searchQuery;
			Log.d(TAG,"query"+mData.searchQuery);
			if(mData.searchQuery.equals("0")){
				return;
			}
			requestUrl = ConsumerApi.getSimilarContent(mData.searchQuery,ConsumerApi.LEVELDYNAMIC);
			
		}
		
		
		/*attrib.put("Category", screenName);
		attrib.put("Duration", "");
		Analytics.trackEvent(Analytics.cardBrowseDuration,attrib,true);*/
		
		
		requestUrl = requestUrl.replaceAll(" ", "%20");
		mVolleyRequest = new GZipRequest(requestMethod, requestUrl, deviceMinSuccessListener(), responseErrorListener());
//		mVolleyRequest.printLogs(true);
//		myReg.setShouldCache(true);
		Log.d(TAG,"Min Request:"+requestUrl);
		queue.add(mVolleyRequest);
	}
	
	private Response.Listener<String> deviceMinSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				//Analytics.endTimedEvent(Analytics.cardBrowseDuration);
				try {
//					Log.d(TAG,"server response "+response);
//					updateText("parsing results");
					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(minResultSet.code != 200){
						Util.showToast(getContext(), minResultSet.message,Util.TOAST_TYPE_ERROR);
//						Toast.makeText(getContext(), minResultSet.message, Toast.LENGTH_SHORT).show();
					}
					/*if(mData.requestType == CardExplorerData.REQUEST_SIMILARCONTENT)
					{
						if(minResultSet.similarContent ==  null){showNoDataMessage(false);return;}
						if(minResultSet.similarContent.values == null || minResultSet.similarContent.values.size() == 0){showNoDataMessage(false);return;}
						if(minResultSet.similarContent != null){
							Log.d(TAG,"Number of results for similar request:"+minResultSet.similarContent.values.size());
						}
						mCacheManager.getCardDetails(minResultSet.similarContent.values,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
					}
					else*/
					{
						if(minResultSet.results != null){
							Log.d(TAG,"Number of Result for the MIN request:"+minResultSet.results.size());
						}
						if(minResultSet.results ==  null){showNoDataMessage(false);return;}
						if(minResultSet.results.size() ==  0){showNoDataMessage(false);return;}
						mCacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
					}
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
			
			String msg="seems like there's nothing here.";
			if(mData.requestType == CardExplorerData.REQUEST_FAVOURITE){
				msg="no favourites yet... \nUse "+ "+ symbol"/*getString(R.string.card_heart)*/ +" on any card to add some titles here.";
			}else if(mData.requestType == CardExplorerData.REQUEST_PURCHASES){
				msg=getString(R.string.purchaseserror);
			}else if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
				msg=getString(R.string.searcherror);
			}else if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
				msg=getString(R.string.downloadserror);
			}
			
			AlertDialogUtil.showAlert(mContext, msg, "cancel", "discover trending content?", this);
			
			//Util.showToast(getContext(),"No data found,Please try again.",Util.TOAST_TYPE_INFO);
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
					mMainActivity.setActionBarTitle("All");
					Map<String,String> params=new HashMap<String, String>();
					params.put(Analytics.SEARCH_TYPE_PROPERTY, Analytics.SEARCH_TYPES.Filter.toString());
					params.put(Analytics.SEARCH_FILTER_TYPE_PROPERTY,label);
					Analytics.trackEvent(Analytics.EVENT_SEARCH,params);
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
				mMainActivity.setActionBarTitle(label/*+" ("+localData.size()+"+)"*/);
				Map<String,String> params=new HashMap<String, String>();
				params.put(Analytics.SEARCH_TYPE_PROPERTY, Analytics.SEARCH_TYPES.Filter.toString());
				params.put(Analytics.SEARCH_NUMBER_FOUND_PROPERTY, String.valueOf(localData.size()));
				params.put(Analytics.SEARCH_FILTER_TYPE_PROPERTY,label);
				Analytics.trackEvent(Analytics.EVENT_SEARCH,params);
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
		if(!isAdded())
		{
			Log.e(TAG, "acitivty is NULL");
			return;
		}
//		updateText("preparing ui");
		if(getResources() != null && getResources().getBoolean(R.bool.isTablet)){
				startPolling(mData.mMasterEntries);
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
		
		String msg="seems like there's nothing here.";
		if(mData.requestType == CardExplorerData.REQUEST_FAVOURITE){
			msg="no favourites yet... \nUse "+ "+ symbol"/*getString(R.string.card_heart)*/ +" on any card to add some titles here.";
		}else if(mData.requestType == CardExplorerData.REQUEST_PURCHASES){
			msg=getString(R.string.purchaseserror);
		}else if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			msg=getString(R.string.searcherror);
		}else if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
			msg=getString(R.string.downloadserror);
		}
		
		AlertDialogUtil.showAlert(mContext, msg, "cancel", "discover trending content?", this);
		//Util.showToast(getContext(), "No Response from server", Util.TOAST_TYPE_INFO);
//		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
//		b.setMessage("No Response from server");
//		b.show();
	}

	private Response.ErrorListener responseErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//Analytics.endTimedEvent(Analytics.cardBrowseDuration);
				//Analytics.endTimedEvent(Analytics.cardBrowseScreen);
				Log.d(TAG,"Error from server "+error.networkResponse);
				if(mData.mMasterEntries == null || mData.mMasterEntries.size() == 0){
					showErrorDialog();
				}
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
			}
		};
	}
	@Override
	public void favouriteAction(final CardData data,final int type){
		FavouriteUtil favUtil = new FavouriteUtil();
		
		//long id=Util.startDownload("", "", getContext());
		//myplexapplication.getUserProfileInstance().downloadMap.put(data._id, id);
		favUtil.addFavourite(type, data, new FavouriteCallback() {
			
			@Override
			public void response(boolean value) {
				String status="";
				if(value){
					status="Not Favourite";
					if(type == FavouriteUtil.FAVOURITEUTIL_ADD)
						Util.showToast(getContext(), data.generalInfo.title+" set as favourite",Toast.LENGTH_SHORT);
					else
						Util.showToast(getContext(), data.generalInfo.title+" removed from favourites",Toast.LENGTH_SHORT);
//					Toast.makeText(getContext(), "Chan", Toast.LENGTH_SHORT).show();
				}else{
					status="Favourite";
//					Util.showToast(getContext(), data.generalInfo.title+"set as favourite",Toast.LENGTH_SHORT);
//					Toast.makeText(getContext(), "Add as Favourite", Toast.LENGTH_SHORT).show();
				}
				
				Map<String,String> params=new HashMap<String, String>();
				/*params.put("Status", status);
				params.put("CardId", data._id);
				if(data.generalInfo != null){
					params.put("CardType", data.generalInfo.type);
					params.put("CardName", data.generalInfo.title);
				}*/
				//???
				params.put(Analytics.BROWSE_TYPE_PROPERTY, status);
				params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
				if(data.generalInfo != null){
					params.put(Analytics.CONTENT_TYPE_PROPERTY, data.generalInfo.type);
					params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
				}
				
				//Analytics.trackEvent(Analytics.cardBrowseFavorite,params);
				Analytics.trackEvent(Analytics.EVENT_BROWSE,params);
				if(getResources().getBoolean(R.bool.isTablet)){
					mTabletAdapter.notifyDataSetChanged();
				}else{
					mCardView.updateData(data);
				}
			}
		});
	}
	private FetchDownloadProgress mDownloadProgressManager; 
	@Override
	public void selectedCard(CardData data,int index) {
		
		if(index == 0 ){
			hideStackedFrame();
		}else{
			showStackedFrame();
		}
		Map<String,String> params=new HashMap<String, String>();
		/*params.put("CardId", data._id);
		if(data.generalInfo != null){
			params.put("CardType", data.generalInfo.type);
			params.put("CardName", data.generalInfo.title);
		}
		Analytics.trackEvent(Analytics.cardBrowseSelect,params);*/
		
		params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
		if(data.generalInfo != null){
			params.put(Analytics.CONTENT_TYPE_PROPERTY, data.generalInfo.type);
			params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
		}
		//Analytics.trackEvent(Analytics.EVENT_BROWSE,params);
		
		mData.currentSelectedCard = index;
		mSelectedCard = data;
		isDownloadView(mSelectedCard);
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
	private void isDownloadView(CardData data){
		if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
			if(mDownloadProgressManager == null){
				mDownloadProgressManager = new FetchDownloadProgress(getContext());
			}
			mDownloadProgressManager.setDownloadProgressListener(this);
			mDownloadProgressManager.startPolling(data);
		}
	}
	
	private void startPolling(List<CardData> datalist) {
		if(mData.requestType == CardExplorerData.REQUEST_DOWNLOADS){
			if(mDownloadProgressManager == null){
				mDownloadProgressManager = new FetchDownloadProgress(getContext());
			}
			mDownloadProgressManager.setDownloadProgressListener(this);
			mDownloadProgressManager.startPolling(datalist);
		}
	}
	@Override
	public void deletedCard(CardData data) {
		// TODO Auto-generated method stub
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", data._id);
		if(data.generalInfo != null){
			params.put("CardType", data.generalInfo.type);
			params.put("CardName", data.generalInfo.title);
		}
		//Analytics.trackEvent(Analytics.cardBrowseCancel,params);
	}

	@Override
	public void purchase(CardData data) {
		PackagePopUp popup = new PackagePopUp(getContext(),mRootView);
		mData.cardDataToSubscribe = data;
		popup.showPackDialog(data, getActionBar().getCustomView());
		Map<String,String> params=new HashMap<String, String>();
		/*params.put("CardId", data._id);
		if(data.generalInfo != null){
			params.put("CardType", data.generalInfo.type);
			params.put("CardName", data.generalInfo.title);
		}
		Analytics.trackEvent(Analytics.cardBrowsePurchase,params);*/
		
		params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
		if(data.generalInfo != null){
			params.put(Analytics.CONTENT_TYPE_PROPERTY, data.generalInfo.type);
			params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
		}
		params.put(Analytics.PAY_STATUS_PROPERTY, Analytics.PAY_COMMERCIAL_TYPES.Buy.toString());
		Analytics.trackEvent(Analytics.EVENT_PAY,params);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewReady() {
		
		if(mData.mMasterEntries.size() > 0 ){
			isDownloadView(mData.mMasterEntries.get(0));
		}
		
		if(getResources().getBoolean(R.bool.isTablet)){
			
		}else{
			mCardView.moveTo(mData.currentSelectedCard);
			
			Map<String,String> params=new HashMap<String, String>();
			if(mData.currentSelectedCard <= mData.mMasterEntries.size())
				params.put(Analytics.CONTENT_ID_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard)._id);
			if(mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo != null){
				params.put(Analytics.CONTENT_TYPE_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.type);
				params.put(Analytics.CONTENT_NAME_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.title);
			}
			//doesnot have data how control comes her. whether through filter|search etc
			//params.put(Analytics.BROWSE_TYPE_PROPERTY,Analytics.BROWSE_CARDACTION_TYPES.Swipe.toString());
			//Analytics.trackEvent(Analytics.EVENT_BROWSE,params);
		}
	}
	

	@Override
	public void OnCacheResults(HashMap<String, CardData> object ,boolean issuedRequest) {
		if(object == null){
			showNoDataMessage(issuedRequest);
			return;
		}
		boolean itemsAdded = false;
		int count = 0;
		Set<String> keySet = object.keySet();
			
		for(String key:keySet){
//			mData.mEntries.add(object.get(key));
			if(mData.mEntries.get(key) == null){
				mData.mEntries.put(key,object.get(key));
				mData.mMasterEntries.add(object.get(key));
				Log.d("CardExplorer","ID= "+key+ " new arrived");
				itemsAdded = true;
				if(count == 0){
					mSelectedCard = object.get(key);
					count++;
				}
			}
		}
		if(mData.mMasterEntries.size() != 0){
			showNoDataMessage(issuedRequest);
		}
		applyData();
		if(mOldDataAdded){
			mOldDataAdded = false;
			if(itemsAdded){
				showNewArrivals();
			}
		}
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
			
			//Analytics.trackEvent(Analytics.cardBrowseScreen,params);
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
			
			//Analytics.trackEvent(Analytics.cardBrowseScreen,params);
		}
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		if(dataList == null){return;}
		boolean itemsAdded = false;
		for(CardData data:dataList){
			
//			mData.mEntries.add(data);
			if(mData.mEntries.get(data._id) == null){
				mData.mEntries.put(data._id,data);
				itemsAdded = true;
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
			
			//Analytics.trackEvent(Analytics.cardBrowseScreen,params);
		showNoDataMessage(false);
		applyData();		
		if(mOldDataAdded){
			mOldDataAdded = false;
			if(itemsAdded){
				showNewArrivals();
			}
		}
	}

	@Override
	public void OnOnlineError(VolleyError error) {
		showNoDataMessage(false);
	}

	@Override
	public void DownloadProgress(CardData cardData,
			CardDownloadData downloadData) {
		if(getResources() != null && getResources().getBoolean(R.bool.isTablet)){
			Log.i(TAG, "DownloadProgress");
			mTabletAdapter.setDownloadStatus(cardData,downloadData);
			
			//Refresh only when download percentage has changed to avoid continuus drawing
			if(!mRefreshOnce)
			{
				mRefreshOnce = true;
				mTabletAdapter.notifyDataSetChanged();
			}
			if(mDownloadTracker.containsKey(cardData) && mDownloadTracker.get(cardData) != downloadData.mPercentage)
			{
				mDownloadTracker.put(cardData, downloadData.mPercentage);
				mTabletAdapter.notifyDataSetChanged();
			}
			
		}else{
			mCardView.updateDownloadStatus(cardData,downloadData);
		}
		
	}

	@Override
	public void onDialogOption2Click() {
		// TODO Auto-generated method stub
		
		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();

		dataBundle.reset();
		dataBundle.requestType = CardExplorerData.REQUEST_RECOMMENDATION;
		
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		mMainActivity.setActionBarTitle("myplex");
		mMainActivity.bringFragment(fragment);
		
	}

	@Override
	public void onDialogOption1Click() {
		// TODO Auto-generated method stub
		
	}
}
