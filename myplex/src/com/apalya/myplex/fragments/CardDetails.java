package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.LayoutTransition;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;

import com.android.volley.VolleyError;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.adapters.ScrollingDirection;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.FavouriteUtil.FavouriteCallback;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.apalya.myplex.views.CardDetailViewFactory.CardDetailViewFactoryListener;
import com.apalya.myplex.views.CardVideoPlayer;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.CustomScrollView;
import com.apalya.myplex.views.FadeInNetworkImageView;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;
import com.apalya.myplex.views.JazzyViewPager;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.apalya.myplex.views.OutlineContainer;
import com.apalya.myplex.views.docketVideoWidget;

public class CardDetails extends BaseFragment implements
		ItemExpandListenerCallBackListener,CardDetailViewFactoryListener,ScrollingDirection,CacheManagerCallback {
	public static final String TAG = "CardDetails";
	private LayoutInflater mInflater;
	private LinearLayout mParentContentLayout;
	private CardDetailViewFactory mCardDetailViewFactory;
	
	private LinearLayout mDescriptionContentLayout;
	private LinearLayout mMediaContentLayout;
	private LinearLayout mCommentsContentLayout;

	private CustomScrollView mScrollView;
	private RelativeLayout mBottomActionBar;
	private ImageView mShareButton;
	private ImageView mFavButton;
	
	private boolean mDescriptionExpanded = false;
	private int mDetailType = Profile;
	public static final int Profile = 0;
	public static final int MovieDetail = 1;
	public static final int TvShowsDetail = 2;
	public static final int LiveTvDetail = 3;
	public View rootView;
	public boolean mPlayStarted = false;
	private CacheManager mCacheManager = new CacheManager();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(mDataObject instanceof CardData){
			mCardData = (CardData) mDataObject;
			Log.d(TAG,"content ID ="+mCardData._id);
		}
		
		mMainActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mInflater = LayoutInflater.from(getContext());
		rootView = inflater.inflate(R.layout.carddetails, container, false);
		mScrollView = (CustomScrollView)rootView.findViewById(R.id.carddetail_scroll_view);
		mBottomActionBar = (RelativeLayout)rootView.findViewById(R.id.carddetail_bottomactionbar);
		mShareButton = (ImageView) rootView.findViewById(R.id.carddetail_share);
		mFavButton = (ImageView) rootView.findViewById(R.id.carddetail_fav);
		if(mCardData.currentUserData != null && mCardData.currentUserData.favorite){
			mFavButton.setImageResource(R.drawable.card_iconheartblue);			
		}else{
			mFavButton.setImageResource(R.drawable.card_iconheart);
		}

		mScrollView.setDirectionListener(this);
		RelativeLayout videoLayout = (RelativeLayout)rootView.findViewById(R.id.carddetail_videolayout);
		mPlayer = new CardVideoPlayer(mContext, mCardData);
		videoLayout.addView(mPlayer.CreatePlayerView(videoLayout));
		mParentContentLayout = (LinearLayout) rootView.findViewById(R.id.carddetail_detaillayout);
		mCardDetailViewFactory = new CardDetailViewFactory(getContext());
		mCardDetailViewFactory.setParent(rootView);
		mCardDetailViewFactory.setOnCardDetailExpandListener(this);
		mMainActivity.setSearchBarVisibilty(View.VISIBLE);
		mMainActivity.enableFilterAction(false);
//		prepareContent();
		if(mCardData.generalInfo != null){
			mMainActivity.setActionBarTitle(mCardData.generalInfo.title);
			mMainActivity.updateActionBarTitle();
		}
		prepareContent();
		mFavButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mMainActivity.showActionBarProgressBar();
				int type = FavouriteUtil.FAVOURITEUTIL_ADD;
				if(mCardData.currentUserData != null && mCardData.currentUserData.favorite){
					type = FavouriteUtil.FAVOURITEUTIL_REMOVE;
				}
				FavouriteUtil favUtil = new FavouriteUtil();
				favUtil.addFavourite(type, mCardData, new FavouriteCallback() {
					
					@Override
					public void response(boolean value) {
						mMainActivity.hideActionBarProgressBar();
						if(value){
//							Toast.makeText(getContext(), "Chan", Toast.LENGTH_SHORT).show();
						}else{
//							Toast.makeText(getContext(), "Add as Favourite", Toast.LENGTH_SHORT).show();
						}
						if(mCardData.currentUserData != null && mCardData.currentUserData.favorite){
							mFavButton.setImageResource(R.drawable.card_iconheartblue);			
						}else{
							mFavButton.setImageResource(R.drawable.card_iconheart);
						}
					}
				});
				
			}
		});
		mShareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Map<String,String> params=new HashMap<String, String>();
				params.put("CardId", mCardData._id);
				params.put("CardType", mCardData.generalInfo.type);
				params.put("CardName", mCardData.generalInfo.title);
				params.put("Action", "Submit");
				Analytics.trackEvent(Analytics.cardDetailsShare,params);
				
				// TODO Auto-generated method stub
				Util.shareData(getContext(), 3, "", mCardData.generalInfo.title);
			}
		});
		
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", mCardData._id);
		params.put("CardType", mCardData.generalInfo.type);
		params.put("CardName", mCardData.generalInfo.title);
		Analytics.trackEvent(Analytics.cardDetailsScreen,params);
		
		return rootView;
	}

	private CardVideoPlayer mPlayer;
	private CardData mCardData;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			if(mBottomActionBar != null){
				mBottomActionBar.setVisibility(View.INVISIBLE);
			}
			mPlayer.playInLandscape();
		}else{
			if(mBottomActionBar != null){
				mBottomActionBar.setVisibility(View.VISIBLE);
			}
			mPlayer.playInPortrait();
		}
		super.onConfigurationChanged(newConfig);
	}
	@Override
	public void onResume() {
		super.onResume();
		if(myplexapplication.getCardExplorerData().cardDataToSubscribe != null){
			mCardData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		}
		if(mCardDetailViewFactory != null){
			mCardDetailViewFactory.UpdateSubscriptionStatus();
		}
	}
	private void prepareContent() {
		fillData();
	}

	private void addSpace(){
//		Space gap = new Space(getContext());
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)getContext().getResources().getDimension(R.dimen.margin_gap_12));
//		gap.setLayoutParams(params);
//		mParentContentLayout.addView(gap);
	}
	private void fillData() {
		mDescriptionExpanded = false;
		mDescriptionContentLayout  = new LinearLayout(getContext());
		LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mDescriptionContentLayout.setLayoutParams(descParams);
		mParentContentLayout.addView(mDescriptionContentLayout);
		
		mMediaContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams mediaParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mediaParams.topMargin = (int)getContext().getResources().getDimension(R.dimen.margin_gap_12);
		mMediaContentLayout.setLayoutParams(mediaParams);
		mParentContentLayout.addView(mMediaContentLayout);
		
		mCommentsContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		commentParams.topMargin = (int)getContext().getResources().getDimension(R.dimen.margin_gap_12);
		mCommentsContentLayout.setLayoutParams(commentParams);
		mParentContentLayout.addView(mCommentsContentLayout);
		
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
		if(v != null){
			mDescriptionContentLayout.addView(v);
		}
		v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BREIF_RELATED_MULTIMEDIA);
		if(v != null){
			addSpace();
			mMediaContentLayout.addView(v);
		}
		v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_COMMENTS);
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
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mCardData._id);
			params.put("CardType", mCardData.generalInfo.type);
			params.put("CardName", mCardData.generalInfo.title);
			params.put("Status", "Expand");
			Analytics.trackEvent(Analytics.cardDetailsMultimedia,params);
			
			CardDetailMediaListData mainData = (CardDetailMediaListData) v
					.getTag();
			mMediaList = mainData.mList;
			showAlbumDialog();
		}
	}

	private void showAlbumDialog() {
		mAlbumDialog = new CustomDialog(getContext());
		mAlbumDialog.setContentView(R.layout.albumview);
		setupJazziness(TransitionEffect.CubeOut);
		mAlbumDialog.setCancelable(true);
		mAlbumDialog.show();
	}

	private List<CardDetailMediaData> mMediaList = null;
	private JazzyViewPager mJazzy;
	private CustomDialog mAlbumDialog;
	private String mSearchQuery;

	private void setupJazziness(TransitionEffect effect) {
		mJazzy = (JazzyViewPager) mAlbumDialog.findViewById(R.id.jazzy_pager);
		int width , height = 100;
		width = myplexapplication.getApplicationConfig().screenWidth - 2*((int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
		height = (width * 9)/16; 
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mJazzy.setLayoutParams(params);
		
		mJazzy.setTransitionEffect(effect);
		mJazzy.setAdapter(new MainAdapter());
		// mJazzy.setPageMargin(30);
	}

	private class MainAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			CardDetailMediaData media =  mSelectedMediaGroup.mList.get(position);
			View v = null;
			if(media.mThumbnailMime != null && media.mThumbnailMime =="Image/JPEG"){
				v = mInflater.inflate(R.layout.cardmediasubitemimage, null);
				((FadeInNetworkImageView)v).setImageUrl(media.mThumbnailUrl, MyVolley.getImageLoader());
			}else{
				docketVideoWidget videoWidget = new docketVideoWidget(mContext);
				v = videoWidget.CreateView(media);
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
			return mSelectedMediaGroup.mList.size();
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
		mCacheManager.getCardDetails(searchString, IndexHandler.OperationType.FTSEARCH, CardDetails.this);

		
		
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		CardExplorerData data = myplexapplication.getCardExplorerData();
		data.reset();
		data.requestType = CardExplorerData.REQUEST_SEARCH;
		data.searchQuery = key;
		mMainActivity.bringFragment(fragment);
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
	public void scrollDirection(boolean value) {
		mQuickReturnHeight  = mBottomActionBar.getHeight();
		int translationY = 0;

		int mScrollY = mScrollView.getScrollY();
		int rawY = mScrollY;


		switch (mState) {
		case STATE_OFFSCREEN:
			if (rawY >= mMinRawY) {
				mMinRawY = rawY;
			} else {
				mState = STATE_RETURNING;
			}
			translationY = rawY;
			break;


		case STATE_ONSCREEN:
			if (rawY > mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			translationY = rawY;
			break;


		case STATE_RETURNING:


			translationY = (rawY - mMinRawY) + mQuickReturnHeight;


			System.out.println(translationY);
			if (translationY < 0) {
				translationY = 0;
				mMinRawY = rawY + mQuickReturnHeight;
			}


			if (rawY == 0) {
				mState = STATE_ONSCREEN;
				translationY = 0;
			}


			if (translationY > mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			break;
		}


		/** this can be used if the build is below honeycomb **/
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			anim = new TranslateAnimation(0, 0, translationY,
					translationY);
			anim.setFillAfter(true);
			anim.setDuration(0);
			 mBottomActionBar.startAnimation(anim);
		} else {
			 mBottomActionBar.setTranslationY(translationY);
		}
	}
	@Override
	public void onDescriptionExpanded() {
		
		mDescriptionExpanded = true;
		mDescriptionContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_FULL_DESCRIPTION);
		if(v != null){
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mCardData._id);
			params.put("CardType", mCardData.generalInfo.type);
			params.put("CardName", mCardData.generalInfo.title);
			params.put("Status", "Expand");
			Analytics.trackEvent(Analytics.cardDetailsDescription,params);
			mDescriptionContentLayout.addView(v);
		}
//		prepareFilterData();
	}
	
	@Override
	public void onCommentsExpanded() {
		mCommentsContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_COMMENTS);
		if(v != null){
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mCardData._id);
			params.put("CardType", mCardData.generalInfo.type);
			params.put("CardName", mCardData.generalInfo.title);
			params.put("Status", "Expand");
			Analytics.trackEvent(Analytics.cardDetailsComment,params);
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
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mCardData._id);
			params.put("CardType", mCardData.generalInfo.type);
			params.put("CardName", mCardData.generalInfo.title);
			params.put("Status", "Contract");
			Analytics.trackEvent(Analytics.cardDetailsComment,params);
		}			
	}
	@Override
	public void onDescriptionCollapsed() {
		mDescriptionExpanded = false;
		mDescriptionContentLayout.removeAllViews();
		View v  = mCardDetailViewFactory.CreateView(mCardData,CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
		if(v != null){
			mDescriptionContentLayout.addView(v);
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mCardData._id);
			params.put("CardType", mCardData.generalInfo.type);
			params.put("CardName", mCardData.generalInfo.title);
			params.put("Status", "Contract");
			Analytics.trackEvent(Analytics.cardDetailsDescription,params);
		}		
//		prepareFilterData();
	}
	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) {
		if(obj == null){return;}
		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();
		
		dataBundle.reset();
		dataBundle.searchQuery = mSearchQuery;
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
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		CardExplorerData data = myplexapplication.getCardExplorerData();
		data.reset();
		data.requestType = CardExplorerData.REQUEST_SIMILARCONTENT;
		data.mMasterEntries =  (ArrayList<CardData>) mCardData.similarContent.values;
		mMainActivity.bringFragment(fragment);
		
		
	}
}