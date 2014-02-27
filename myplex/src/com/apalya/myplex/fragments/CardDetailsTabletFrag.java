package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.adapters.ScrollingDirection;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataRelatedCastItem;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.tablet.MultiPaneActivity;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.EpgView;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.NumberPicker;
import com.apalya.myplex.utils.SeasonFetchHelper;
import com.apalya.myplex.utils.SeasonFetchHelper.ShowFetchListener;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.apalya.myplex.views.CardDetailViewFactory.CardDetailViewFactoryListener;
import com.apalya.myplex.views.CardVideoPlayer;
import com.apalya.myplex.views.CardVideoPlayer.PlayerFullScreen;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.CustomScrollView;
import com.apalya.myplex.views.FadeInNetworkImageView;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;
import com.apalya.myplex.views.JazzyViewPager;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.apalya.myplex.views.OutlineContainer;
import com.apalya.myplex.views.TVShowView;
import com.apalya.myplex.views.TVShowView.TVShowSelectListener;
import com.apalya.myplex.views.docketVideoWidget;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class CardDetailsTabletFrag extends BaseFragment implements
ItemExpandListenerCallBackListener,CardDetailViewFactoryListener,ScrollingDirection,CacheManagerCallback,PlayerFullScreen {
	public static final String TAG = "CardDetails";
	private LayoutInflater mInflater;
	private CardDetailViewFactory mCardDetailViewFactory;
	
	private LinearLayout mDescriptionContentLayout;
	private LinearLayout mMediaContentLayout;
	private LinearLayout mCommentsContentLayout;

	private LinearLayout mRightScrollViewLayout;
	private ScrollView mBottomScrollView;
	
	
	private CardVideoPlayer mPlayer;
	private boolean mDescriptionExpanded = false;
	
	private int mDetailType = Profile;
	public static final int Profile = 0;
	public static final int MovieDetail = 1;
	public static final int TvShowsDetail = 2;
	public static final int LiveTvDetail = 3;
	public View rootView;
	public boolean mPlayStarted = false;
	

	

	private List<CardData> childSubList = new ArrayList<CardData>();
	private SeasonFetchHelper helper = null;
	private TVShowView mTVShowView = null;
	private NumberPicker seasonPicker,episodePicker;
	private LinearLayout mTvShowLinear;
	private LinearLayout mEPGLayout;
	private CustomScrollView mCustomScrollView;
	private LinearLayout mRightSideLayout;
	private LinearLayout mParentContentLayout;
	private LinearLayout mPlayerLogsLayout;
	
	private NumberPicker datePicker;
	private NumberPicker programmePicker;

	






	
	
	private CacheManager mCacheManager = new CacheManager();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			Analytics.createScreenGA(Analytics.SCREEN_CARDDETAILS);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(mDataObject != null && mDataObject instanceof CardData){
			mCardData = (CardData) mDataObject;
		}
		Log.d(TAG,"content ID ="+mCardData._id);
		mMainActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		mInflater = LayoutInflater.from(getContext());
		rootView = inflater.inflate(R.layout.carddetailstablet, container, false);
		mRightScrollViewLayout = (LinearLayout)rootView.findViewById(R.id.carddetailtablet_rightscrollviewlayout);
		mBottomScrollView = (ScrollView)rootView.findViewById(R.id.carddetailtablet_descriptionscroll_view); //this
		mDescriptionContentLayout = (LinearLayout)rootView.findViewById(R.id.carddetailtablet_descriptiondetaillayout);
		mTvShowLinear =(LinearLayout) rootView.findViewById(R.id.carddetailtablet_seasonEpisodePicker);
		mMediaContentLayout = (LinearLayout)rootView.findViewById(R.id.carddetailtablet_multimedialayout); // mm 
		mCommentsContentLayout = (LinearLayout)rootView.findViewById(R.id.carddetailtablet_commentlayout);
		
		removePreviousViews();
		
		RelativeLayout videoLayout = (RelativeLayout)rootView.findViewById(R.id.carddetailtablet_videolayout);
		mPlayer = new CardVideoPlayer(mContext, mCardData);
		mPlayer.setFullScreenListener(this);
		videoLayout.addView(mPlayer.CreateTabletPlayerView(videoLayout));
		mCardDetailViewFactory = new CardDetailViewFactory(getContext());
		mCardDetailViewFactory.setOnCardDetailExpandListener(this);
		mCardDetailViewFactory.setParent(rootView);
		mMainActivity.setSearchBarVisibilty(View.INVISIBLE);
		
		mCustomScrollView = (CustomScrollView) rootView.findViewById(R.id.carddetailtable_right_scroll_view);
		mCustomScrollView.setVisibility(View.GONE);
		mRightSideLayout =(LinearLayout) rootView.findViewById(R.id.carddetailtablet_rightscrollviewlayout);
		
		
		mParentContentLayout = (LinearLayout) rootView
				.findViewById(R.id.carddetailtablet_rightlayout);
		
//		prepareContent();
		
		if(mCardData.generalInfo != null){
			mMainActivity.setActionBarTitle(mCardData.generalInfo.title.toLowerCase());
		}
	Analytics.mixPanelcardSelected(mCardData);
		
		if( mCardData.generalInfo.type != null && mCardData.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_TV_SERIES)){
			mBottomScrollView.setVisibility(View.GONE);

			helper  = new SeasonFetchHelper(mCardData,new TvShowManager());
			helper.fetchSeason();
			initialiseTVShow(rootView);
			
			seasonPicker = (NumberPicker)rootView.findViewById(R.id.numberPickerSeason);
			episodePicker = (NumberPicker)rootView.findViewById(R.id.numberPickerEpisode);
			initNumberPickerWithLoading(seasonPicker);
			initNumberPickerWithLoading(episodePicker);
			mRightSideLayout.setVisibility(View.GONE);
			mCustomScrollView.setVisibility(View.VISIBLE);
			fillDataForTV();			//mPickerLayout.setVisibility(View.VISIBLE);

		}
		
		if(mCardData.generalInfo.type != null && mCardData.generalInfo.type.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
			
		//	mBottomScrollView.setVisibility(View.GONE);
//			mPickerLayout.setVisibility(View.VISIBLE);
//			datePicker = (NumberPicker)rootView.findViewById(R.id.datePicker);
//			programmePicker = (NumberPicker)rootView.findViewById(R.id.programmPicker);		
//			datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//			programmePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//			initNumberPickerWithLoading(datePicker);
//			initNumberPickerWithLoading(programmePicker);
//			
			mRightSideLayout.setVisibility(View.GONE);
			mCustomScrollView.setVisibility(View.VISIBLE);
			

			createEPGView(rootView);
			fillDataForTV();

		}else if(mEPGLayout!=null){
			
				mEPGLayout.setVisibility(View.GONE);
				mBottomScrollView.setVisibility(View.VISIBLE);


		}		
		
		else if( mCardData.generalInfo.type != null && mCardData.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_TV_SEASON)){
			mBottomScrollView.setVisibility(View.GONE);
			seasonPicker = (NumberPicker)rootView.findViewById(R.id.numberPickerSeason);
			episodePicker = (NumberPicker)rootView.findViewById(R.id.numberPickerEpisode);
			initialiseTVShow(rootView);

			initNumberPickerWithLoading(seasonPicker);
			initNumberPickerWithLoading(episodePicker);
			mCustomScrollView.setVisibility(View.VISIBLE);
			mRightSideLayout.setVisibility(View.GONE);
			fillDataForTV();
			
			

			//mPickerLayout.setVisibility(View.VISIBLE);

		}
		
		prepareContent();
		return rootView;
	}
	@Override
	public void onPause() {		
		super.onPause();
		if(mPlayer!=null){
			if(mPlayer.isMediaPlaying()){
				mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
				Analytics.stoppedAt(); //when back button clicked
				Analytics.mixPanelVideoTimeCalculation(mCardData);
			}
			mPlayer.stopSportsStatusRefresh();
		}
	}

	private CardData mCardData,mSeasonData,mEpisodeData;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			mPlayer.playInLandscape();
		}else{
			mPlayer.playInPortrait();
		}
		super.onConfigurationChanged(newConfig);
	}
	public void onResume() {
		super.onResume();
		if(myplexapplication.getCardExplorerData().cardDataToSubscribe != null){
			mCardData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		}
		if(mCardDetailViewFactory != null){
			mCardDetailViewFactory.UpdateSubscriptionStatus(mCardData);
		}
	}
	private void prepareContent() {
		fillData();
	}

	private void addSpace(){
		Space gap = new Space(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)getContext().getResources().getDimension(R.dimen.margin_gap_8));
		gap.setLayoutParams(params);
	}
	private void fillData() {
		mDescriptionExpanded = false;
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
		if(v != null){
			mDescriptionContentLayout.addView(v);
		}
		v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BREIF_RELATED_MULTIMEDIA);
		if(v != null){
			addSpace();
			mMediaContentLayout.addView(v);
		}
		v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_COMMENTS);
		if(v != null){
			addSpace();
			mCommentsContentLayout.addView(v);	
		}
	}


	@Override
	public void OnItemExpand(View v) {
		if (v == null) {
			return;
		}
		if (v.getTag() instanceof CardDetailMediaListData) {
			CardDetailMediaListData mainData = (CardDetailMediaListData) v
					.getTag();
			mMediaList = mainData.mList;
			showAlbumDialog();
		}
	}

	private void showAlbumDialog() {
		Analytics.mixPanelCastCrewPopup(mCardData);
		mAlbumDialog = new CustomDialog(getContext());
		mAlbumDialog.setContentView(R.layout.albumview);
		setupJazziness(TransitionEffect.CubeOut);
		mAlbumDialog.setCancelable(true);
		mAlbumDialog.show();
	}

	private List<CardDetailMediaData> mMediaList = null;
	private List<CardDataRelatedCastItem> mRelatedCastList = null;
	private JazzyViewPager mJazzy;
	private CustomDialog mAlbumDialog;
	private String mSearchQuery;
	private int popupType = MainAdapter.PAGE_CASTVIEW;
	private void setupJazziness(TransitionEffect effect) {
		mJazzy = (JazzyViewPager) mAlbumDialog.findViewById(R.id.jazzy_pager);
		int width = RelativeLayout.LayoutParams.MATCH_PARENT;
		int height = RelativeLayout.LayoutParams.MATCH_PARENT;
		if(popupType == MainAdapter.PAGE_MEDIAVIEW){
			width = myplexapplication.getApplicationConfig().screenWidth - 2*((int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
			height = (width * 9)/16; 

		}else{
			height = (int)(mContext.getResources().getDimension(R.dimen.detailcastheight));
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mJazzy.setLayoutParams(params);
		
		mJazzy.setTransitionEffect(effect);
		MainAdapter adapter = new MainAdapter(popupType);
		mJazzy.setAdapter(adapter);
		 mJazzy.setPageMargin(30);
	}

	private class MainAdapter extends PagerAdapter {
		public static final int PAGE_CASTVIEW = 0;
		public static final int PAGE_MEDIAVIEW = 1;
		private int type = PAGE_CASTVIEW;
		public MainAdapter(int type){
			this.type = type;
		}
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View v = null;
			if(type == PAGE_MEDIAVIEW){
				CardDetailMediaData media =  mSelectedMediaGroup.mList.get(position);
				if(media.mThumbnailMime != null && media.mThumbnailMime =="Image/JPEG"){
					v = mInflater.inflate(R.layout.cardmediasubitemimage, null);
					((FadeInNetworkImageView)v).setImageUrl(media.mThumbnailUrl, MyVolley.getImageLoader());
				}else{
					docketVideoWidget videoWidget = new docketVideoWidget(mContext);
					v = videoWidget.CreateView(media);
				}
			}else{
				CardDataRelatedCastItem relatedCastItem = mRelatedCastList.get(position);
				v = mInflater.inflate(R.layout.fulldetailcastlayout, null);
				TextView name = (TextView)v.findViewById(R.id.fullcast_name);
				name.setTypeface(FontUtil.Roboto_Light);
				TextView rolesTextView = (TextView)v.findViewById(R.id.fullcast_roles);
				rolesTextView.setTypeface(FontUtil.Roboto_Medium);
				TextView typesTextView = (TextView)v.findViewById(R.id.fullcast_types);
				typesTextView.setTypeface(FontUtil.Roboto_Medium);
				FadeInNetworkImageView image = (FadeInNetworkImageView)v.findViewById(R.id.fullcast_image);
				image.setErrorImageResId(R.drawable.placeholder);
				
				String roleString = new String();
				if(relatedCastItem.roles != null ){
					for(String role:relatedCastItem.roles){
						if(roleString.length() > 0){
							roleString = ",";
						}
						roleString += role;
					}
				}
				rolesTextView.setText(roleString);
				
				String typeString = new String();
				if(relatedCastItem.types != null ){
					for(String role:relatedCastItem.types){
						if(typeString.length() > 0){
							typeString = ",";
						}
						typeString += role;
					}
				}
				typesTextView.setText(typeString);
				Random rnd = new Random();
				int Low = 100;
				int High = 196;
				int cardColor = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low);
				image.setBackgroundColor(cardColor);
				image.setErrorImageResId(R.drawable.placeholder);
				name.setText(relatedCastItem.name);
				if(relatedCastItem.images != null && relatedCastItem.images.values != null && relatedCastItem.images.values.size() > 0){
					for(CardDataImagesItem item: relatedCastItem.images.values){
						if(item.profile != null && item.profile.equalsIgnoreCase(myplexapplication.getApplicationConfig().type)){
							image.setImageUrl(item.link,MyVolley.getImageLoader());
							break;
						}
					}
				}
				image.setTag(relatedCastItem.name);
				image.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(v.getTag() instanceof String){
							onTextSelected((String)v.getTag());
							mAlbumDialog.dismiss();
						}
					}
				});
			}
			container.addView(v);
			mJazzy.setObjectForPosition(v, position);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(mJazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			if(type == PAGE_MEDIAVIEW){
				return mSelectedMediaGroup.mList.size();
			}else{
				return mRelatedCastList.size();
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			if (view instanceof OutlineContainer) {
				return ((OutlineContainer) view).getChildAt(0) == obj;
			} else {
				return view == obj;
			}
		}
	}


	@Override
	public void onTextSelected(String key) {
		
	mMainActivity.showActionBarProgressBar();
		
		String searchQuery = new String();
		List<CardData> searchString = new ArrayList<CardData>();
		CardData temp = new CardData();
//			temp._id = data.getButtonId() != null ? data.getButtonId() : data.getButtonName();
		temp._id =key;
		searchQuery = key;
		searchString.add(temp);
		mSearchQuery = searchQuery;
		mMainActivity.setActionBarTitle(mSearchQuery);
		mCacheManager.getCardDetails(searchString, IndexHandler.OperationType.FTSEARCH, CardDetailsTabletFrag.this);

		
		
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		
		CardExplorerData data = myplexapplication.getCardExplorerData();
		data.reset();
		data.requestType = CardExplorerData.REQUEST_SEARCH;
		data.searchQuery = key;
		getActivity().startActivity(new Intent(getActivity(),MultiPaneActivity.class));
		getActivity().finish();
	}

	private CardDetailMultiMediaGroup mSelectedMediaGroup;
	@Override
	public void onMediaGroupSelected(CardDetailMultiMediaGroup group) {
		mSelectedMediaGroup = group;
		showAlbumDialog();
	}

	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;
	private int mState = STATE_ONSCREEN;
	private int mMinRawY = 0;
	private int mQuickReturnHeight;
	private TranslateAnimation anim;
	@Override
	public void onDescriptionExpanded() {
		mDescriptionExpanded = true;
		mDescriptionContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_FULL_DESCRIPTION);
		if(v != null){
			mDescriptionContentLayout.addView(v);
		}
	}
	
	@Override
	public void onCommentsExpanded() {
		mCommentsContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_COMMENTS);
		if(v != null){
			addSpace();
			mCommentsContentLayout.addView(v);	
		}		
	}
	@Override
	public void onCommentsCollapsed() {
		mCommentsContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_COMMENTS);
		if(v != null){
			addSpace();
			mCommentsContentLayout.addView(v);	
		}			
	}
	@Override
	public void onDescriptionCollapsed() {
		mDescriptionExpanded = false;
		mDescriptionContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
		if(v != null){
			mDescriptionContentLayout.addView(v);
		}		
	}
	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) {
		if(obj == null){return;}
		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();
		
		dataBundle.reset();
		dataBundle.searchQuery = mSearchQuery;
		mMainActivity.setActionBarTitle(mSearchQuery);
		dataBundle.requestType = CardExplorerData.REQUEST_SEARCH;
		
		mMainActivity.addFilterData(new ArrayList<FilterMenudata>(), null);
		
		Set<String> keySet = obj.keySet(); 
		for(String key: keySet){
			CardData data =  obj.get(key);
//			dataBundle.mEntries.add(data);
//			if(dataBundle.mEntries.get(key) == null){
				dataBundle.mEntries.put(key,data);
				dataBundle.mMasterEntries.add(data);
//			}
			
			if(data.generalInfo !=null)
				Log.i(TAG,"adding "+data._id+":"+data.generalInfo.title+" from Cache");
		}
		mCacheManager.unRegisterCallback();
		mMainActivity.hideActionBarProgressBar();
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		mMainActivity.bringFragment(fragment);
		
	}
	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnOnlineError(VolleyError error) {
		mMainActivity.hideActionBarProgressBar();		
	}
	@Override
	public void onSimilarContentAction() {
		CardExplorerData data = myplexapplication.getCardExplorerData();
		data.reset();
		data.requestType = CardExplorerData.REQUEST_SIMILARCONTENT;
		data.searchQuery = mCardData._id;
//		data.mMasterEntries =  (ArrayList<CardData>) mCardData.similarContent.values;
		Analytics.mixPanelSimilarContent(mCardData);
		getActivity().startActivity(new Intent(getActivity(),MultiPaneActivity.class));
		getActivity().finish();
	}
	@Override
	public void scrollDirection(boolean value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void playerInFullScreen(boolean value) {
		if(value){
			mRightScrollViewLayout.setVisibility(View.GONE);
			mBottomScrollView.setVisibility(View.GONE);
		}else{
			mRightScrollViewLayout.setVisibility(View.VISIBLE);
			mBottomScrollView.setVisibility(View.VISIBLE);			
		}
	}
	@Override
	public void onFullDetailCastAction() {
		mRelatedCastList = mCardData.relatedCast.values;
		popupType = MainAdapter.PAGE_CASTVIEW;
		showAlbumDialog();		
		
	}
	@Override
	public void onProgressBarVisibility(int value) {
		// TODO Auto-generated method stub
		
	}
	//copied from CardDetails for Analytics
	@Override
	public boolean onBackClicked() {
		try{
			if(mPlayer.isFullScreen()){
				if (!mContext.getResources().getBoolean(R.bool.isTablet)) {
					if(mPlayer.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
						((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						mPlayer.resumePreviousOrientaionTimer();
					}
					else {
						((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						mPlayer.resumePreviousOrientaionTimer();
					} 
				}
				mPlayer.setFullScreen(!mPlayer.isFullScreen());
				return true;
			}
			if(mPlayer.isMediaPlaying()){
				mPlayer.closePlayer();
				Analytics.stoppedAt();
				Analytics.mixPanelVideoTimeCalculation(mCardData);
				Analytics.gaStopPauseMediaTime("stop",mPlayer.getStopPosition());
				return true;
			}
			return false;
		}catch(Throwable e){			
			return false;
		}
	}
	public void initNumberPickerWithLoading(NumberPicker np) {
		
		String seasonValues[]  = new String[] { "Loading...", "Loading...","Loading..." };
		int maxSeason  = np.getMaxValue();
		if( seasonValues.length > maxSeason){
			np.setMinValue(0);
			np.setDisplayedValues(seasonValues);
			np.setMaxValue(2);	
		}else{
			np.setMinValue(0);
			np.setMaxValue(2);
			np.setDisplayedValues(seasonValues);
			
		}
	}
	
	private void fillEpisodeData(CardData card){
		if(mCardData._id.equals(card._id))
			return;
		this.mCardData=card;		
		//mParentContentLayout.removeAllViews();
		removePreviousViews();
		fillData();
		mPlayer.updateCardPreviewImage(card);
	}
	private void removePreviousViews(){
		mDescriptionContentLayout.removeAllViews(); //1
		mMediaContentLayout.removeAllViews(); //2

		mCommentsContentLayout.removeAllViews(); //3
	}
	
	private void initialiseTVShow(View rootView) {
		
		LayoutTransition tvshowtransition = new LayoutTransition();
		tvshowtransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		mTvShowLinear.setLayoutTransition(tvshowtransition);
		//LinearLayout.LayoutParams params = (LayoutParams) mTvShowLinear.getLayoutParams();
				
		
		mTvShowLinear.setVisibility(View.VISIBLE);
	
	}
	private class TvShowManager implements ShowFetchListener{
		@Override
		public void onSeasonDataFetched(List<CardData> seasons) {
			childSubList.addAll(seasons);
			if(mTVShowView==null)
				mTVShowView = new TVShowView(mContext , childSubList , mTvShowLinear,new TvShowSelectorCallBack());
			mTVShowView.createTVShowView();
			mSeasonData = seasons.get(0);
			mCardDetailViewFactory.UpdateSubscriptionStatus(mSeasonData);
		}
		@Override
		public void onEpisodeFetched(CardData season, List<CardData> episodes) 
		{		
			if(mTVShowView.onEpisodeFetchComplete(season, episodes)){			
				mSeasonData = season;
				mEpisodeData = episodes.get(0);			
				fillEpisodeData(mEpisodeData);
				mCardDetailViewFactory.UpdateSubscriptionStatus(mSeasonData);			}
		}
		@Override
		public void onFailed(VolleyError error) {
			
		}
		
	}	
	private class TvShowSelectorCallBack implements TVShowSelectListener{

		@Override
		public void onEpisodeSelect(CardData carddata,CardData season) {
			fillEpisodeData(carddata);
			mCardDetailViewFactory.UpdateSubscriptionStatus(season);
		}

		@Override
		public void onSeasonChange(CardData season) {
			helper.fetchEpisodes(season);	
		}
	
	}	
	
	private void fillDataForTV() {
		mDescriptionExpanded = false;

		mPlayerLogsLayout = new LinearLayout(getContext());
		mPlayerLogsLayout.setBackgroundResource(R.drawable.card_background);
		LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		playParams.bottomMargin = (int) getContext().getResources()
				.getDimension(R.dimen.margin_gap_12);
		mPlayerLogsLayout.setLayoutParams(playParams);
		mPlayerLogsLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutTransition transition = new LayoutTransition();
		transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		mPlayerLogsLayout.setLayoutTransition(transition);
		mParentContentLayout.addView(mPlayerLogsLayout);

		mDescriptionContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mDescriptionContentLayout.setLayoutParams(descParams);

		mMediaContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams mediaParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mediaParams.topMargin = (int) getContext().getResources().getDimension(
				R.dimen.margin_gap_12);
		mMediaContentLayout.setLayoutParams(mediaParams);
		mMediaContentLayout.setBackgroundResource(R.drawable.card_background);

		mCommentsContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		commentParams.topMargin = (int) getContext().getResources()
				.getDimension(R.dimen.margin_gap_12);
		mCommentsContentLayout.setLayoutParams(commentParams);
		mCommentsContentLayout.setBackgroundResource(R.drawable.card_background);
		
		View v = mCardDetailViewFactory.CreateView(mCardData,
				CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
		if (v != null) {
			mParentContentLayout.addView(mDescriptionContentLayout);
			mDescriptionContentLayout.addView(v);
		}
		v = mCardDetailViewFactory.CreateView(mCardData,
				CardDetailViewFactory.CARDDETAIL_BREIF_RELATED_MULTIMEDIA);
		if (v != null) {
			mParentContentLayout.addView(mMediaContentLayout);
			addSpace();
			mMediaContentLayout.addView(v);
		}
		v = mCardDetailViewFactory.CreateView(mCardData,
				CardDetailViewFactory.CARDDETAIL_BRIEF_COMMENTS);
		if (v != null) {
			mParentContentLayout.addView(mCommentsContentLayout);
			addSpace();
			mCommentsContentLayout.addView(v);
		}
	}

	private void createEPGView(View rootView) {
		mEPGLayout = (LinearLayout) rootView.findViewById(R.id.epg_linear_layout);
		
		LayoutTransition epgtransition = new LayoutTransition();
		epgtransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		mEPGLayout.setLayoutTransition(epgtransition);
		//RelativeLayout.LayoutParams params = (LayoutParams) mEPGLayout.getLayoutParams();
				
		EpgView epgview =new EpgView(mCardData, mContext);
		epgview.setCardVideoPlayer(mPlayer);
		mEPGLayout.setVisibility(View.VISIBLE);
		
		View epgView  = epgview.createEPGView();
		
		mEPGLayout.addView(epgView);
		
		
		/*
		if(epgView != null){
			Log.d("amlan","card view visible");
			mEPGLayout.setVisibility(View.VISIBLE);
			
			params.topMargin = (int) getContext().getResources()
					.getDimension(R.dimen.margin_gap_12);
					
			mEPGLayout.addView(epgView);
		}else{
			mEPGLayout.setVisibility(View.GONE);
		}*/
	}
	
	
}
