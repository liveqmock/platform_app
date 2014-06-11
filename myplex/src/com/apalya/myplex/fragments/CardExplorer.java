package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.AuthFailureError;
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
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCurrentUserData;
import com.apalya.myplex.data.CardDataGenralInfo;
import com.apalya.myplex.data.CardDataGenre;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.FetchDownloadData;
import com.apalya.myplex.data.FetchDownloadData.FetchDownloadDataListener;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.media.VideoViewPlayer.OnLicenseExpiry;
import com.apalya.myplex.tablet.TabletCardDetails;
import com.apalya.myplex.utils.AlertDialogUtil;
import com.apalya.myplex.utils.AlertDialogUtil.NoticeDialogListener;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.FavouriteUtil.FavouriteCallback;
import com.apalya.myplex.utils.FetchDownloadProgress;
import com.apalya.myplex.utils.FetchDownloadProgress.DownloadProgressStatus;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardVideoPlayer;
import com.apalya.myplex.views.CardView;
import com.apalya.myplex.views.PackagePopUp;
import com.apalya.myplex.views.SensorScrollUtil;

public class CardExplorer extends BaseFragment implements CardActionListener,CacheManagerCallback,DownloadProgressStatus,
		OnDismissCallback, AlertDialogUtil.NoticeDialogListener,Util.KeyRenewListener {
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
	public static  boolean mfirstTime = false;
	private View mProgressView = null;
	private SensorScrollUtil mSensorScrollUtil ;
	private static final String EMPTY_CARD_ID = "0";
	
	//private EasyTracker easyTracker = null;
	

	NoticeDialogListener mLoginDialogListener = new NoticeDialogListener(){

		@Override
		public void onDialogOption2Click() {
			
			LogOutUtil.onClickLogout(mContext);
		}

		@Override
		public void onDialogOption1Click() {
			
			
		}
		
	};
	
	@Override
	public void open(CardData object) {
		
		if(object == null || object._id == null || object._id.equalsIgnoreCase(EMPTY_CARD_ID)){
			return;
		}
		
		if(object.generalInfo != null 
				&& object.generalInfo.type != null 
				&& object.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_YOUTUBE)){
			
			// Before playing any video we have to check whether user has logged In
			// or not.
			String email = myplexapplication.getUserProfileInstance()
					.getUserEmail();
			if (email == null || email.equalsIgnoreCase("NA") || email.equalsIgnoreCase("")) {
				AlertDialogUtil.showAlert(mContext, mContext.getResources()
						.getString(R.string.must_logged_in), mContext
						.getResources().getString(R.string.continiue_as_guest),
						mContext.getResources().getString(R.string.login_to_play),
						mLoginDialogListener);
				return ;
			}
			
			Util.launchYouyubePlayer((Activity) mContext, object._id);
			return ;
		}
		
		mMainActivity.saveActionBarTitle();
		if(getResources().getBoolean(R.bool.isTablet)){
			myplexapplication.mSelectedCard = object;
			startActivity(new Intent(getContext(),TabletCardDetails.class));
			getActivity().finish();
		}else{
			BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDDETAILS_ACTION);
			fragment.setDataObject(object);
			mMainActivity.overlayFragment(fragment);	
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mData = myplexapplication.getCardExplorerData();
		mfirstTime = true;
		Analytics.createScreenGA(Analytics.SCREEN_CARD_EXPLORER);
		mSensorScrollUtil= new SensorScrollUtil();
		mSensorScrollUtil.register(getContext());

	}


	public void showProgressBar() {
		if(mProgressView != null){
			ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mProgressView,
					"alpha", 0f, 1f);
			fadeAnim2.setDuration(800);
			fadeAnim2.start();
			mProgressView.setVisibility(View.VISIBLE);
		}
//		if (mProgressDialog != null) {
//			mProgressDialog.dismiss();
//		}
//		mProgressDialog = ProgressDialog.show(getContext(), "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
		if(mProgressView != null){
			ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mProgressView,
					"alpha", 1f, 0f);
			fadeAnim2.setDuration(800);
			fadeAnim2.start();
//			mProgressView.setVisibility(View.INVISIBLE);
		}
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
		mSensorScrollUtil.unregister();
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
		if(mMainActivity == null){
			return null;
		}
		
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
		mProgressView = mRootView.findViewById(R.id.card_loading_progress);
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
		mSensorScrollUtil.init(mCardView);
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
//						applyEmptyData();
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
		if(mCardView != null){
			mCardView.setActionBarHeight(height);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if( mMainActivity == null ){ return;}
		
		if(myplexapplication.getCardExplorerData().cardDataToSubscribe != null){
			mCardView.updateData(myplexapplication.getCardExplorerData().cardDataToSubscribe);
		}
		Log.d(TAG,"onResume");
		boolean isMovie = false;  
		if((mData.requestType == CardExplorerData.REQUEST_RECOMMENDATION )||( mData.requestType ==CardExplorerData.REQUEST_BROWSE)
																		  ||( mData.requestType ==CardExplorerData.REQUEST_TV_SHOWS))
		{
			if(mData.requestType == CardExplorerData.REQUEST_BROWSE 
					&& mData.searchQuery!=null 
						&& mData.searchQuery.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_MOVIE)){
				isMovie = true;
				mMainActivity.setUpLivetvOrMovie(isMovie);
			}else if(mData.searchQuery.equalsIgnoreCase(ConsumerApi.TYPE_TV_SERIES)){
				isMovie = true;
				mMainActivity.setUpLivetvOrMovie(isMovie);
			}else{
				if(mData.searchQuery.equalsIgnoreCase("live"))
					mMainActivity.setUpLivetvOrMovie(false);
			}
		}	
		if(mVolleyRequest == null)
			return;
		if(mVolleyRequest.isCanceled()){
			delayedAction();
		}
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		//for analytics		
		CardView cardView = getmCardView();
    	if(cardView != null) {
    		if(cardView.swipeCount > 1) {
    			cardView.mixpanelBrowsing();
    		}
    	}
    	if(mTabletAdapter != null) {
    		if(mTabletAdapter.swipeCount > 1) {
    			mTabletAdapter.mixpanelBrowsingTablet();
    		}
    	}
		super.onPause();
	}
	public void fillDownloadList(){
		FetchDownloadData d = new FetchDownloadData(getContext());
		d.fetchDownload(new FetchDownloadDataListener() {
			
			@Override
			public void completed() {
			showNoDataMessage(false);
				applyData();
				if(mData.mMasterEntries != null || mData.mMasterEntries.size() != 0){
					Analytics.mixPanelBrowsingEvents(mData,mfirstTime);				
				}
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
	private RequestQueue queue;
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
		if(!ApplicationSettings.ENABLE_SERIALIZE_LAST_SEESION){
			return;
		}
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
		showProgressBar();
		queue = MyVolley.getRequestQueue();
		int requestMethod = Method.GET;
		String requestUrl = new String();
		if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			String searchScope;
			if(mData.searchScope == null || mData.searchScope.length() > 0)
			{
				Log.i(TAG,"Seachscope: "+ mData.searchScope);
				searchScope = mData.searchScope;
				requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELDYNAMIC,mData.mStartIndex,searchScope);
			}else if(mData.searchScope.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
				
				requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELDYNAMIC,mData.mStartIndex,ConsumerApi.VIDEO_TYPE_LIVE);
			}
			else{
				searchScope = "movie";//+","+ConsumerApi.CONTENT_SPORTS_LIVE+","+ConsumerApi.CONTENT_SPORTS_VOD;			
				requestUrl = ConsumerApi.getSearch(mData.searchQuery,ConsumerApi.LEVELDYNAMIC,mData.mStartIndex,searchScope);
			}
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
			String searchType = ConsumerApi.VIDEO_TYPE_LIVE+","+ConsumerApi.VIDEO_TYPE_MOVIE+","+ConsumerApi.TYPE_TV_SEASON;
			requestUrl = ConsumerApi.getPurchases(ConsumerApi.LEVELDYNAMIC,mData.mStartIndex,searchType);
			requestUrl = requestUrl+ "&type=live,movie,tvseason";
			requestMethod = Method.GET;
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
				mMainActivity.hideActionBarProgressBar();
				mMainActivity.setActionBarTitle("last watched");
				return;
			}
			requestUrl = ConsumerApi.getSimilarContent(mData.searchQuery,ConsumerApi.LEVELDYNAMIC);
			
		}else if(mData.requestType == CardExplorerData.REQUEST_TV_SHOWS){
			screenName="Browse" + mData.searchQuery;
			requestUrl = ConsumerApi.getBrowse(mData.searchQuery,ConsumerApi.LEVELDYNAMIC, mData.mStartIndex);
		}
		
		
		requestUrl = requestUrl.replaceAll(" ", "%20");
		mVolleyRequest = new GZipRequest(requestMethod, requestUrl, deviceMinSuccessListener(), responseErrorListener());/*{
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				if(mData.requestType == CardExplorerData.REQUEST_PURCHASES){
					Map<String, String> params = new HashMap<String, String>();
					params.put("type", "live,movie,tvseason");	
					return params;
				}
				return super.getParams();
			}
		};*/
		mVolleyRequest.mStartIndex = mData.mStartIndex;
		

		Log.d(TAG,"Min Request:"+requestUrl);
		if(requestUrl.equals("0") || (requestUrl.length()==0)){
			mMainActivity.hideActionBarProgressBar();
		}else{
			queue.add(mVolleyRequest);
		}
	}
	
	private HashSet< Integer> mArrivedRequestHashSet = new HashSet<Integer>();

	
	private Response.Listener<CardResponseData> deviceMinSuccessListener() {
		return new Response.Listener<CardResponseData>() {
			@Override
			public void onResponse(CardResponseData minResultSet) {
				//Analytics.endTimedEvent(Analytics.cardBrowseDuration);
				try {
//					Log.d(TAG,"server response "+response);
//					updateText("parsing results");
					if(minResultSet ==  null){showNoDataMessage(false);return;}
//					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
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
						if(mArrivedRequestHashSet.contains(minResultSet.mStartIndex)){
							// cache-refresh hit response , just save to lucene
							CacheManager cacheManager = new CacheManager();
							cacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,null);
							return;
						}
						
						mVolleyRequest = null;						
						mCacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,CardExplorer.this);
						mArrivedRequestHashSet.add(minResultSet.mStartIndex);						
					}
				} catch (Exception e) {
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
		HashMap<String , Integer> genreCounter = new HashMap<String, Integer>();
		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
		List<String> tempList = new ArrayList<String>();
		for (CardData data : mData.mMasterEntries) {
			
			// Use categoryName instead of genre for youtube content
			if(data.generalInfo != null && data.generalInfo.type != null  && data.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_YOUTUBE)){					
				
				if(data.content != null && data.content.categoryName != null && !tempList.contains(data.content.categoryName)){
					tempList.add(data.content.categoryName);
					genreCounter.put(data.content.categoryName,1);
				}				
				continue;
			}
			
			if(data.content != null && data.content.genre != null){				
			
				for(CardDataGenre genreData:data.content.genre){
					if(genreData.name != null && !tempList.contains(genreData.name)){
						tempList.add(genreData.name);
						genreCounter.put(genreData.name,1);
					}else if(mData.searchQuery.equalsIgnoreCase("live")){
						genreCounter.put(genreData.name, genreCounter.get(genreData.name)+1);
					}
				}
			}
		}
		if(mData.searchQuery.equalsIgnoreCase("live")){
			if(tempList.size() > 1){
				filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, "All", 0));
			}
			Iterator<Entry<String, Integer>> iterator = genreCounter.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Integer> entry = iterator.next();
				String filterName  = entry.getKey()+" ("+entry.getValue()+")";
				filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, filterName, 0));
			}
		}else{
		if(tempList.size() > 1){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, "All", 0));
		}
		for(String filterName:tempList){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, filterName, 0));
		}
		}
		if(isVisible()){
			mMainActivity.addFilterData(filteroptions, mFilterMenuClickListener);
		}
	}

	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				String label = ((FilterMenudata) v.getTag()).label.replaceAll("\\(.*?\\)","").trim();
				if (label != null && label.equalsIgnoreCase("All")) {
					sort(mData.mMasterEntries);
					return;
				}
				
				ArrayList<CardData> localData = new ArrayList<CardData>();
				for (CardData data : mData.mMasterEntries) {
					boolean itemPresent = false;
					
					// Use categoryName instead of genre for youtube content
					if(data.generalInfo != null && data.generalInfo.type != null  && data.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_YOUTUBE)							
							&& data.content != null && data.content.categoryName != null 
							&& data.content.categoryName.equalsIgnoreCase(label)){					
						
						localData.add(data);		
						continue;
					}

					
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

				Analytics.mixPanelFilter(mData,label,localData);
//				mMainActivity.setActionBarTitle(label/*+" ("+localData.size()+"+)"*/);
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
	
	CardData emptyCard = new CardData();
	
	private void applyEmptyData(){
		
		if(!mData.mMasterEntries.isEmpty()){
			emptyCard = null;
			return;
		}
		emptyCard._id = EMPTY_CARD_ID;
		CardDataGenralInfo generalInfo = new CardDataGenralInfo();
		CardDataCurrentUserData currentUserData = new CardDataCurrentUserData();
		currentUserData.purchase = new ArrayList<CardDataPurchaseItem>();
		emptyCard.currentUserData = currentUserData;
		generalInfo.title = "loading";
		emptyCard.generalInfo = generalInfo;
		mData.mEntries.put(EMPTY_CARD_ID, emptyCard);
		mData.mMasterEntries.add(emptyCard);
		mData.mMasterEntries.add(emptyCard);
		mData.mMasterEntries.add(emptyCard);
		mData.mMasterEntries.add(emptyCard);
		mData.mMasterEntries.add(emptyCard);			
	}
	
	private void removeEmptyData(){
		
		if(emptyCard == null) return;
		
		mData.mEntries.remove(EMPTY_CARD_ID);
		mData.mMasterEntries.remove(emptyCard);
		mData.mMasterEntries.remove(emptyCard);
		mData.mMasterEntries.remove(emptyCard);
		mData.mMasterEntries.remove(emptyCard);
		mData.mMasterEntries.remove(emptyCard);
		emptyCard=null;
		
	}
	
	private void applyData() {
		
			
		if(mData.mMasterEntries == null || mData.mMasterEntries.size() == 0){
			return;
		}
		//Analytics.mixPanelBrowsingEvents(mData,mfirstTime);
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
			mCardView.show(false);
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
				
				if(!isAdded())
					return;
				
				String status="";
				if(value){
					status="Not Favourite";
					if(type == FavouriteUtil.FAVOURITEUTIL_ADD) {
						Analytics.mixPanelAddFavorite(data,Analytics.NUMBER_FAVOURITE);
						Util.showToast(getContext(), data.generalInfo.title+" set as favourite",Toast.LENGTH_SHORT);
					}
					else {
						Analytics.mixPanelAddFavorite(data,2);
						Util.showToast(getContext(), data.generalInfo.title+" removed from favourites",Toast.LENGTH_SHORT);
					}
//					Toast.makeText(getContext(), "Chan", Toast.LENGTH_SHORT).show();
				}else{
					status="Favourite";
//					Util.showToast(getContext(), data.generalInfo.title+"set as favourite",Toast.LENGTH_SHORT);
//					Toast.makeText(getContext(), "Add as Favourite", Toast.LENGTH_SHORT).show();
				}
				
				
				if(getResources().getBoolean(R.bool.isTablet)){
					mTabletAdapter.notifyDataSetChanged();
				}else{
					mCardView.updateData(data);
				}
			}
		});
		
		if (type == FavouriteUtil.FAVOURITEUTIL_ADD
				&& data.generalInfo != null
				&& data.generalInfo.type != null
				&& data.generalInfo.type
						.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)
				&& data.matchInfo != null
				&& data.matchInfo.matchStartTime != null) {

			// show reminder for sports content
			Util.showReminder(data.generalInfo.title,
					data.matchInfo.matchStartTime, data.generalInfo.title,
					data._id, mContext);
		}
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
			
		params.put(Analytics.CONTENT_ID_PROPERTY, data._id);
		if(data.generalInfo != null){
			params.put(Analytics.CONTENT_TYPE_PROPERTY, data.generalInfo.type);
			params.put(Analytics.CONTENT_NAME_PROPERTY, data.generalInfo.title);
		}
				
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
		
	}

	@Override
	public void purchase(CardData data) {
		PackagePopUp popup = new PackagePopUp(getContext(),mRootView);
		mData.cardDataToSubscribe = data;
		popup.showPackDialog(data, getActionBar().getCustomView());
		
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
			if(mData.currentSelectedCard < mData.mMasterEntries.size()){
				
				params.put(Analytics.CONTENT_ID_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard)._id);
				if(mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo != null){
					params.put(Analytics.CONTENT_TYPE_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.type);
					params.put(Analytics.CONTENT_NAME_PROPERTY, mData.mMasterEntries.get(mData.currentSelectedCard).generalInfo.title);
				}
			}
			
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
//		removeEmptyData();
		
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
		if(mData.requestType != CardExplorerData.REQUEST_SEARCH){
			Analytics.mixPanelBrowsingEvents(mData,mfirstTime);
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
			
			
		}		
	
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		if(dataList == null){return;}
		boolean itemsAdded = false;
//		removeEmptyData();
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
			
			
		showNoDataMessage(false);
		if(mData.requestType == CardExplorerData.REQUEST_SEARCH){
			Analytics.mixPanelBrowsingEvents(mData,mfirstTime);
		}
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
		mMainActivity.setActionBarTitle("myplex picks");
		mMainActivity.bringFragment(fragment);
		
	}

	@Override
	public void onDialogOption1Click() {
		try{
			//View mRootView = inflater.inflate(R.layout.cardbrowsing, container, false);
			DrawerLayout  mDrawerLayout = (DrawerLayout)  getActivity().findViewById(R.id.drawer_layout);
 			ListView mDrawerList = (ListView) getActivity().findViewById(R.id.left_drawer);
 			mDrawerLayout.openDrawer(mDrawerList);
		}catch(Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		
	}
	interface RenewKey{
		public void keyRenewed();
	}
	@Override
	public void onKeyRenewed() {
		fetchMinData();
	}

	@Override
	public void onKeyRenewFailed(String message) {		
		mMainActivity.hideActionBarProgressBar();
		dismissProgressBar();
		Util.showToast(mContext, message, Util.TOAST_TYPE_INFO);
	}
	//analytics
	public CardView getmCardView() {
		return mCardView;
	}
	
}
