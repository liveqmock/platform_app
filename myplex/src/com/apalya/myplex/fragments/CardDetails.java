package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Handler;

import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.R.dimen;
import com.apalya.myplex.R.id;
import com.apalya.myplex.R.layout;
import com.apalya.myplex.R.string;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.adapters.ScrollingDirection;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoView;
import com.apalya.myplex.media.VideoViewExtn;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MediaUtil;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.apalya.myplex.views.CardDetailViewFactory.CardDetailViewFactoryListener;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.CustomScrollView;
import com.apalya.myplex.views.FadeInNetworkImageView;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;
import com.apalya.myplex.views.JazzyViewPager;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.apalya.myplex.views.OutlineContainer;
import com.apalya.myplex.views.docketVideoWidget;

public class CardDetails extends BaseFragment implements
		ItemExpandListenerCallBackListener,CardDetailViewFactoryListener,PlayerListener,ScrollingDirection,CacheManagerCallback {
	public static final String TAG = "CardDetails";
	private LayoutInflater mInflater;
	private LinearLayout mParentContentLayout;
	private CardDetailViewFactory mCardDetailViewFactory;
	
	private LinearLayout mDescriptionContentLayout;
	private LinearLayout mMediaContentLayout;
	private LinearLayout mCommentsContentLayout;

	private CustomScrollView mScrollView;
	private RelativeLayout mBottomActionBar;
	
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
		
		mMainActivity.setPotrait();
		mInflater = LayoutInflater.from(getContext());
		rootView = inflater.inflate(R.layout.carddetails, container, false);
		mScrollView = (CustomScrollView)rootView.findViewById(R.id.carddetail_scroll_view);
		mBottomActionBar = (RelativeLayout)rootView.findViewById(R.id.carddetail_bottomactionbar);
		mScrollView.setDirectionListener(this);
		RelativeLayout videoLayout = (RelativeLayout)rootView.findViewById(R.id.carddetail_videolayout);
		videoLayout.addView(createVideoPreview());
		mParentContentLayout = (LinearLayout) rootView.findViewById(R.id.carddetail_detaillayout);
		mCardDetailViewFactory = new CardDetailViewFactory(getContext());
		mCardDetailViewFactory.setOnCardDetailExpandListener(this);
		mMainActivity.setSearchBarVisibilty(View.VISIBLE);
//		prepareContent();
//		if(mCardData.generalInfo != null){
//			mMainActivity.setActionBarTitle(mCardData.generalInfo.title);
//			mMainActivity.updateActionBarTitle();
//		}
		return rootView;
	}

	private CardData mCardData;
	private FadeInNetworkImageView mPreviewImage;
	private VideoViewExtn mVideoView;
	private boolean mPlaying = false;
	private TextView mPlay;
	private RelativeLayout mProgressBar;
	VideoViewPlayer mVideoViewPlayer;
	private RelativeLayout mVideoViewLayout;
	private int mPerBuffer = 0;
	private TextView mBufferPercentageTextView;
	private View createVideoPreview(){
		View v = mInflater.inflate(R.layout.cardmediasubitemvideo, null);
		mVideoViewLayout = (RelativeLayout)v;
		int width , height = 100;
		
		width = myplexapplication.getApplicationConfig().screenWidth;
		height = (width * 9)/16; 
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mPreviewImage = (FadeInNetworkImageView) v.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);
		mVideoView = (VideoViewExtn)v.findViewById(R.id.cardmediasubitemvideo_videopreview);
		mVideoView.setLayoutParams(params);
		mPlay = (TextView) v.findViewById(R.id.cardmediasubitemvideo_play);
		mPlay.setTypeface(FontUtil.ss_symbolicons_line);
		mBufferPercentageTextView = (TextView)v.findViewById(R.id.carddetaildesc_movename);
		Random rnd = new Random();
		int Low = 100;
		int High = 196;
		
        int color = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low); 
        mPreviewImage.setBackgroundColor(color);
		mProgressBar = (RelativeLayout) v.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);
		
		if(mCardData.images != null){
			for(CardDataImagesItem imageItem:mCardData.images.values){
				if(imageItem.profile != null && imageItem.profile.equalsIgnoreCase("xxhdpi")){
					if (imageItem.link == null || imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
						mPreviewImage.setImageResource(0);
					} else if (imageItem.link != null){
						mPreviewImage.setImageUrl(imageItem.link, MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
	
//		Util.showFeedback(mPlay);
		
		
		if(mCardData._id.equalsIgnoreCase("0"))
		{
			mPlay.setVisibility(View.GONE);
			mPreviewImage.setScaleType(ScaleType.FIT_XY);
		}
		else
		{
			v.setOnClickListener(mPlayListener);
		}
		//		mPlay.setOnClickListener(mPlayListener);
		return v;
	}
	private OnClickListener mPlayListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mPlaying){
				if(mVideoViewPlayer != null){
					mVideoViewPlayer.closeSession();
				}
				mProgressBar.setVisibility(View.GONE);
				showImagePreview();
				mPlaying = false;
				return;
			}
			mPlaying = true;
			hideImagePreview();
			mProgressBar.setVisibility(View.VISIBLE);
			MediaUtil.setUrlEventListener(new MediaUtilEventListener() {
				
				@Override
				public void urlReceived(boolean aStatus, String url) {
					if(!aStatus){
						if(mVideoViewPlayer != null){
							mVideoViewPlayer.closeSession();
						}
						mProgressBar.setVisibility(View.GONE);
						showImagePreview();
						mPlaying = false;
						Toast.makeText(getContext(), "Failed in fetching the url.", Toast.LENGTH_SHORT).show();
						return;
					}
					if(url == null){
						if(mVideoViewPlayer != null){
							mVideoViewPlayer.closeSession();
						}
						mProgressBar.setVisibility(View.GONE);
						showImagePreview();
						mPlaying = false;
						Toast.makeText(getContext(), "No url to play.", Toast.LENGTH_SHORT).show();
						return;
					}
					Uri uri = Uri.parse("rtsp://59.162.166.216:554/AAJTAK_QVGA.sdp");
					uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
					uri = Uri.parse(url);
//					Toast.makeText(getContext(), "URL:"+url, Toast.LENGTH_SHORT).show();
					VideoViewPlayer.StreamType streamType = StreamType.VOD;
					if(mVideoViewPlayer == null){
						mVideoViewPlayer = new VideoViewPlayer(mVideoView,mContext, uri, streamType);
						mVideoViewPlayer.openVideo();
					}else{
						mVideoViewPlayer.setUri(uri, streamType);
					}
					mVideoViewPlayer.hideMediaController();
					mVideoView.setOnTouchListener(new OnTouchListener() {
						
						@Override
						public boolean onTouch(View arg0, MotionEvent event) {
							// TODO Auto-generated method stub
							if(mPlayStarted){
								mVideoViewPlayer.onTouchEvent(event);
							}
							return false;
						}
					});
					mVideoViewPlayer.setPlayerListener(CardDetails.this);	
					
				}
			});
			MediaUtil.getVideoUrl(mCardData._id, "low");
		}
	};
	public void playStarted(){
		mPlayStarted = true;
//		mMainActivity.setSensor();
	}
	public void playStopped(){
		mPerBuffer = 0;
		mPlayStarted = false;
//		mMainActivity.setPotrait();
	}
	public void playInLandscape(){
		if(mVideoViewPlayer!= null){
			mVideoViewPlayer.showMediaController();
		}
		mVideoView.resizeVideo(myplexapplication.getApplicationConfig().screenHeight,myplexapplication.getApplicationConfig().screenWidth-Util.getStatusBarHeight(getContext()));
		mVideoView.requestLayout();
//		mVideoViewLayout.setLayoutParams(new LayoutParams(myplexapplication.getApplicationConfig().screenHeight,myplexapplication.getApplicationConfig().screenWidth-Util.getStatusBarHeight(getContext())));
//		mVideoViewLayout.requestLayout();
		mMainActivity.hideActionBar();
		mBottomActionBar.setVisibility(View.INVISIBLE);
	}
	public void playInPotrait(){
		if(mVideoViewPlayer!= null){
			mVideoViewPlayer.hideMediaController();
		}
		int width = myplexapplication.getApplicationConfig().screenWidth;
		int height = (width * 9)/16; 
		mVideoView.resizeVideo(width,height);
		mVideoView.requestLayout();
//		mVideoViewLayout.setLayoutParams(new LayoutParams(width,height));
//		mVideoViewLayout.requestLayout();
		mMainActivity.showActionBar();
		mBottomActionBar.setVisibility(View.VISIBLE);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//			playInLandscape();
		}else{
//			playInPotrait();
		}
		super.onConfigurationChanged(newConfig);
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {
		Log.e("player",perBuffer +" loading ");
		if(this.mPerBuffer <= perBuffer){
			this.mPerBuffer = perBuffer;
		}
		if(mBufferPercentageTextView != null){
			mBufferPercentageTextView.setText(""+mPerBuffer);
		}
		int currentseekposition = mVideoView.getCurrentPosition();
		if(currentseekposition < 0){
			currentseekposition = 510;
		}
		if(mVideoView.isPlaying() && currentseekposition > 500){
			mProgressBar.setVisibility(View.GONE);
			mVideoViewPlayer.deregisteronBufferingUpdate();
			playStarted();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int arg1, int arg2) {
		mProgressBar.setVisibility(View.GONE);
		showImagePreview();
		playStopped();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mProgressBar.setVisibility(View.GONE);
		showImagePreview();
		playStopped();
	}

	@Override
	public void onPlayerQualityClick() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	private void showImagePreview(){
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
		mPlay.setText(R.string.card_play);
		
	}
	private void hideImagePreview(){
		mPreviewImage.setVisibility(View.INVISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPlay.setText(R.string.card_pause);
	}
	@Override
	public void onResume() {
		prepareContent();
		super.onResume();
	}
	private void prepareContent() {
		fillData();
		prepareFilterData();
	}

	private void prepareFilterData() {
		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_DETAILS) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,CardDetailViewFactory.SECTION_DETAILS, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_DETAILS)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_DESCRIPTION) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,CardDetailViewFactory.SECTION_DESCRIPTION, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_DESCRIPTION)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_MYPLEXDESCRITION) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,CardDetailViewFactory.SECTION_MYPLEXDESCRITION, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_MYPLEXDESCRITION)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_STUDIODESCRITION) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,CardDetailViewFactory.SECTION_STUDIODESCRITION, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_STUDIODESCRITION)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_CREDITS) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,CardDetailViewFactory.SECTION_CREDITS, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_CREDITS)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_RELATEDMULTIMEDIA) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,CardDetailViewFactory.SECTION_RELATEDMULTIMEDIA, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_RELATEDMULTIMEDIA)));
		}
		if(mCardDetailViewFactory.getSubSection(CardDetailViewFactory.SECTION_COMMENTS) != null){
			filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,CardDetailViewFactory.SECTION_COMMENTS, mCardDetailViewFactory.getSectionHeight(CardDetailViewFactory.SECTION_COMMENTS)));
		}
		if(isVisible()){
			mMainActivity.addFilterData(filteroptions, mFilterMenuClickListener);
		}
	}
	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				FilterMenudata menuData = (FilterMenudata)v.getTag();
				int moveTo = mCardDetailViewFactory.getSectionHeight(menuData.label);
				int totalHeight = mParentContentLayout.getHeight();
				int currentY = mScrollView.getScrollY();
				int moveTo1 =  -currentY+moveTo;
//				int diff = Math.abs(moveTo - currentY);
//				if(currentY < moveTo){
//					moveTo = currentY + diff;
//				}else{
//					moveTo = currentY - diff;
//				}
				Log.d("CardDetail"," value for "+menuData.label+" = "+mCardDetailViewFactory.getSectionHeight(menuData.label)+" scrollY = "+mScrollView.getScrollY());
				mScrollView.smoothScrollTo(0, moveTo1);
			}
		}
	};
	private void addSpace(){
		Space gap = new Space(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)getContext().getResources().getDimension(R.dimen.margin_gap_8));
		gap.setLayoutParams(params);
		mParentContentLayout.addView(gap);
	}
	private void fillData() {
		mDescriptionExpanded = false;
		mDescriptionContentLayout  = new LinearLayout(getContext());
		LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mDescriptionContentLayout.setLayoutParams(descParams);
		mParentContentLayout.addView(mDescriptionContentLayout);
		
		mMediaContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams mediaParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mMediaContentLayout.setLayoutParams(mediaParams);
		mParentContentLayout.addView(mMediaContentLayout);
		
		mCommentsContentLayout = new LinearLayout(getContext());
		LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
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

		
		
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
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
			mDescriptionContentLayout.addView(v);
		}
		prepareFilterData();
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
		prepareFilterData();
	}
	@Override
	public void OnCacheResults(HashMap<String, CardData> obj) {
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
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
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
		BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER);
		CardExplorerData data = myplexapplication.getCardExplorerData();
		data.reset();
		data.requestType = CardExplorerData.REQUEST_SIMILARCONTENT;
		data.mMasterEntries =  (ArrayList<CardData>) mCardData.similarContent.values;
		mMainActivity.bringFragment(fragment);
	}
}