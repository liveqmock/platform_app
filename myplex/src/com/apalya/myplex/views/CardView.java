package com.apalya.myplex.views;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.volley.toolbox.CardNetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardItemClickListener;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataHolder;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.CardImageLoader;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;

public class CardView extends ScrollView {
    private static final String TAG = "CardView";
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
	
	private Context mContext;
	LayoutInflater mInflater;
	private CardsLayout mCardsLayout;

	private List<CardData> mDataList = new ArrayList<CardData>();

	private Scroller mScroller;
	private int mMaxYOverscrollDistance;	

	private int mNumberofItems = 0;
	private int mCurrentSelectedIndex = 0;
	//private int mIndexofSelectedView = 0;

	private int mScreenHeight = 0;
	//private int mScreenWidth = 0;
	private int mActionBarHeight = 0;
	private int mAvailableScreenSize;
	public float mCardHeight = 0;
	private float mCardTitleHeight = 0;

	public int mCardPositions[] = {0, 263, 352, 415, 448, 481};
	
	private boolean mMotionDetected;
	
	private boolean mMotionConsumedByClick = false;

	private int mLoadMoreLastCalledNumberofItems;
	
    public CardView(Context context) {
    	super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public CardView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
        initScrollView();
    }

    private void initScrollView() {
	    setOverScrollMode(ScrollView.OVER_SCROLL_ALWAYS);
	    setSmoothScrollingEnabled(true); 

		mCardsLayout = new CardsLayout(mContext, this);
		
	    addView(mCardsLayout);

	    mScroller = new Scroller(mContext);
	    
	    
        mMaxYOverscrollDistance = (int) (MAX_Y_OVERSCROLL_DISTANCE * mContext.getResources().getDisplayMetrics().density);
    }
    
    public float getCardHeight() {
    	return mCardHeight;
    }
    
	private float getCardLayoutHeight() {
		return getResources().getDimension(R.dimen.cardtitleheight)
				+ getResources().getDimension(R.dimen.cardplayheight)
				+ getResources().getDimension(R.dimen.cardstatusheight)
				+ getResources().getDimension(R.dimen.cardmoreinfoheight);
	}

	private void init() {

		mScreenHeight = myplexapplication.getApplicationConfig().screenHeight;
		//mScreenWidth = dm.widthPixels;

		int margin = (int) (2 * (getResources().getDimension(R.dimen.cardmargin)));
		mAvailableScreenSize = (int) (mScreenHeight - mActionBarHeight - margin);

		mCardHeight = getCardLayoutHeight();
		mCardHeight = getResources().getDimension(R.dimen.cardHeight)+getResources().getDimension(R.dimen.margin_gap_6) ;
		mCardTitleHeight = getResources().getDimension(R.dimen.cardtitleheight);

		initScrollView();
	}
    
	public void setContext(Context cxt) {
		mContext = cxt;
		mInflater = LayoutInflater.from(mContext);
		init();
	}
	private float openStartXPosition = 0;
	private float openStartYPosition = 0;
	private float openEndXPosition = 0;
	private float openEndYPosition = 0;
	private void prepareViewPositions() {
		float firstItemStart = 0; // assuming first card starts at 0 position
		float secondItemStart = mCardHeight + getResources().getDimension(R.dimen.cardmargin);
		float remainingsize = mAvailableScreenSize - (secondItemStart + 4 * mCardTitleHeight);
		float thirdItemStart = secondItemStart + mCardTitleHeight + remainingsize * 0.65f;
		float fourthItemStart = thirdItemStart + mCardTitleHeight + remainingsize * 0.35f;
		float fifthItemStart = fourthItemStart + mCardTitleHeight;
		float sixthItemStart = fifthItemStart + mCardTitleHeight;
		
		openStartYPosition  = firstItemStart+mCardTitleHeight;
		openEndYPosition = firstItemStart+mCardTitleHeight+getResources().getDimension(R.dimen.cardplayheight);
		mCardPositions[0] = Math.round(firstItemStart);
		mCardPositions[1] = Math.round(secondItemStart);
		mCardPositions[2] = Math.round(thirdItemStart);
		mCardPositions[3] = Math.round(fourthItemStart);
		mCardPositions[4] = Math.round(fifthItemStart);
		mCardPositions[5] = Math.round(sixthItemStart);
	}
	
	public int[] getCardPositions() {
		return mCardPositions;
	}

	public void setActionBarHeight(int actionBarHeight) {
		mActionBarHeight = actionBarHeight;
		int margin = (int) (2 * getResources().getDimension(R.dimen.cardmargin));
		mAvailableScreenSize = (int) (mScreenHeight - (Util.getStatusBarHeight(mContext) + mActionBarHeight + margin));
		prepareViewPositions();
	}

	public void setData(List<CardData> dataList) {
		if (dataList != null) {
			for(CardData data: dataList){
				this.mDataList.add(data);
			}
			this.mDataList = dataList;
			mNumberofItems = mDataList.size();
		}
	}

	public List<CardData> getDataList() {
		return mDataList;
	}

	public CardData getData(int index) {
		if (index >= 0 && index < mNumberofItems) {
			return mDataList.get(index);
		}
		return null;
	}
	public void forceUpdateData(List<CardData> datalist){
		mDataList  = new ArrayList<CardData>();
		for(CardData data: datalist){
			this.mDataList.add(data);
		}
		mNumberofItems = mDataList.size();	
		show();
	}
	public void addData(CardData data) {
		this.mDataList.add(data);
		mNumberofItems = mDataList.size();
	}

	public void addData(List<CardData> datalist) {
		try {
			if(myplexapplication.mDownloadList == null){
				myplexapplication.mDownloadList = (CardDownloadedDataList) Util.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		mDataList = new ArrayList<CardData>();
		if (datalist != null) {
			for (CardData data : datalist) {
				this.mDataList.add(data);
			}
		}
		mNumberofItems = mDataList.size();
	}
	public void updateData(CardData data){
		int index = -1;
		index = mDataList.indexOf(data);
		Log.e(TAG, "updateData " +index);
		if(index == -1){return;}
		View localv = mCardsLayout.mCardViewReusePool.getView(index);
		applyData(localv, index);
	}

	public View createCardView() {
		return mInflater.inflate(R.layout.card, null);		
	}
	private String mPriceStarts = "Starts from ";
	private String mRupeeCode  = null;
	private HashMap<String,String> ImageUrlMap = new HashMap<String, String>();
	public void updateDownloadStatus(CardData data,CardDownloadData downloadData){
		
		CardDownloadData localDownloadData;
		if(myplexapplication.mDownloadList == null){
			return;
		}
		localDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(data._id); 
		if(localDownloadData == null){
			return;
		}
		int index = -1;
		index = mDataList.indexOf(data);
		Log.e(TAG, "updateData " +index);
		if(index == -1){return;}
		View localv = mCardsLayout.mCardViewReusePool.getView(index);
		if(localv == null){
			return;
		}
		CardDataHolder dataHolder = (CardDataHolder)localv.getTag();
		if(dataHolder == null){
			return;
		}
		if(downloadData != null){
			localDownloadData = downloadData;
		}
		dataHolder.mESTDownloadStatus.setVisibility(View.VISIBLE);
		dataHolder.mESTDownloadStatus.setTextSize(16f);
		if(!localDownloadData.mCompleted){
			if(localDownloadData.mPercentage==0)
			{
				dataHolder.mESTDownloadStatus.setText("Downloading "+localDownloadData.mPercentage+" %");
			}
			else
			{
				DecimalFormat dec = new DecimalFormat("0.00");
				dataHolder.mESTDownloadStatus.setTextSize(10f);
				dataHolder.mESTDownloadStatus.setText(dec.format(localDownloadData.mDownloadedBytes)+"MB / "+dec.format(localDownloadData.mDownloadTotalSize)+"MB   "+localDownloadData.mPercentage+" %");
				
			}
			dataHolder.mESTDownloadBar.setVisibility(View.VISIBLE);
			dataHolder.mESTDownloadBar.setProgress(localDownloadData.mPercentage);
		}else{
			dataHolder.mESTDownloadBar.setVisibility(View.INVISIBLE);
			if(localDownloadData.mPercentage==0)
			{
				dataHolder.mESTDownloadStatus.setText("Download Failed");
			}
			else
			{
				dataHolder.mESTDownloadStatus.setText("Download Complete");	
			}
			
		}
	}
	private int cardColor = -1;
	public void applyData(View v, int position) {
		if (v == null || !(position >= 0 && position < mNumberofItems)) {
			return;
		}
		CardData data = mDataList.get(position);
		v.setId(position);
		CardDataHolder dataHolder = (CardDataHolder)v.getTag();
		if(dataHolder == null){
			dataHolder = new CardDataHolder();
			dataHolder.mTitleLayout = (RelativeLayout)v.findViewById(R.id.card_title_layout); 
			dataHolder.mTitle = (TextView)v.findViewById(R.id.card_title_name);
			dataHolder.mDelete = (TextView)v.findViewById(R.id.card_title_deleteText);
			dataHolder.mFavourite = (TextView)v.findViewById(R.id.card_title_fav);
			dataHolder.mFavLayout = (RelativeLayout)v.findViewById(R.id.card_title_favlayout);
			dataHolder.mDeleteLayout = (RelativeLayout)v.findViewById(R.id.card_title_delete);
			dataHolder.mPreview = (CardImageView)v.findViewById(R.id.card_preview_image);
//			dataHolder.mPreviewLayout = (RelativeLayout)v.findViewById(R.id.card_preview_layout);
			dataHolder.mOverLayPlay = (ImageView)v.findViewById(R.id.card_play);
			dataHolder.mComments = (TextView)v.findViewById(R.id.card_status_comments);
			dataHolder.mReviews = (TextView)v.findViewById(R.id.card_status_people);
			dataHolder.mCommentsText = (TextView)v.findViewById(R.id.card_status_comments_text);
			dataHolder.mReviewsText = (TextView)v.findViewById(R.id.card_status_people_text);
			dataHolder.mRentLayout = (RelativeLayout)v.findViewById(R.id.card_rent_layout);
			dataHolder.mRentText = (TextView)v.findViewById(R.id.card_rent_text);
			dataHolder.mFavProgressBar = (ProgressBar) v.findViewById(R.id.card_title_fav_progress);
			dataHolder.mESTDownloadBar = (ProgressBar) v.findViewById(R.id.card_download_progressBar);
			dataHolder.mESTDownloadStatus = (TextView) v.findViewById(R.id.card_download_status);
			dataHolder.mFavProgressBar.getIndeterminateDrawable().setColorFilter(0xFF54B5E9, android.graphics.PorterDuff.Mode.MULTIPLY);
			// fonts
			Random rnd = new Random();
			int Low = 100;
			int High = 196;
			if(cardColor == -1){
				cardColor = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low);
			}
	        dataHolder.mPreview.setBackgroundColor(cardColor);
			
			dataHolder.mTitle.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mRentText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mCommentsText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mReviewsText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mESTDownloadStatus.setTypeface(FontUtil.Roboto_Medium);
			
//			dataHolder.mESTDownloadStatus.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mDelete.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mFavourite.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mComments.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mReviews.setTypeface(FontUtil.ss_symbolicons_line);
			
//			dataHolder.mOverLayPlay.setText(R.string.card_play);
//			dataHolder.mDelete.setText(R.string.card_delete);
//			dataHolder.mFavourite.setText(R.string.card_heart);
			dataHolder.mComments.bringToFront();
//			dataHolder.mReviews.setText(R.string.card_people);
			
//			CardData dataHolder.mDataObject = (TextView)v.findViewById(id);
			v.setTag(dataHolder);
		}
		dataHolder.mDataObject = data;
		dataHolder.mPreview.mCardId = position;
		dataHolder.mPreview.mImageUrl = null;
//		dataHolder.mTitleLayout.setBackgroundColor(color)
		
//		ImageUrlMap.put(""+position,null);
		
		if(data.generalInfo == null){
			return;
		}
		if(data.generalInfo != null && data.generalInfo.title != null){
			dataHolder.mTitle.setText(data.generalInfo.title);
		}
		

        dataHolder.mPreview.setImageBitmap(null);
//		Log.e(TAG,"Erasing "+position+" for "+dataHolder.mTitle.getTextSize()+"  "+dataHolder.mTitle.getWidth() );
		if(data.images != null){
			for(CardDataImagesItem imageItem:data.images.values){
				if(imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(myplexapplication.getApplicationConfig().type)){
					if (imageItem.link != null && !(imageItem.link.compareTo("Images/NoImage.jpg") == 0)) {
						dataHolder.mPreview.mImageUrl = imageItem.link;
//						Log.d("CardExplorer","imageItem.link ="+imageItem.link+" profile = "+imageItem.profile);
						CardImageLoader ImageLoader = new CardImageLoader(position,mContext);
						ImageLoader.loadImage(dataHolder.mPreview);
//						dataHolder.mPreview.setImageUrl(imageItem.link,MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		dataHolder.mFavProgressBar.setVisibility(View.INVISIBLE);
		dataHolder.mFavourite.setVisibility(View.VISIBLE);
		if(data.currentUserData != null && data.currentUserData.favorite){
			dataHolder.mFavourite.setText(R.string.card_filledheart);
			dataHolder.mFavourite.setTextColor(Color.parseColor("#58b4e5"));
		}else{
			dataHolder.mFavourite.setText(R.string.card_filledheart);
			dataHolder.mFavourite.setTextColor(Color.parseColor("#000000"));
		}
		if(data.userReviews != null){
			dataHolder.mReviewsText.setText(""+data.userReviews.values.size());	
		}else{
			dataHolder.mReviewsText.setText("0");
		}
		if(data.comments != null){
			dataHolder.mCommentsText.setText(""+data.comments.values.size());	
		}else{
			dataHolder.mCommentsText.setText("0");
		}
		
//		int reqsize = (int) mContext.getResources().getDimension(R.dimen.margin_gap_24);
//		reqsize = 40;
//		 ShapeDrawable biggerCircle= new ShapeDrawable( new OvalShape());
//	        biggerCircle.setIntrinsicHeight( reqsize );
//	        biggerCircle.setIntrinsicWidth( reqsize);
//	        biggerCircle.setBounds(new Rect(0, 0, reqsize, reqsize));
//	        biggerCircle.getPaint().setColor(Color.BLUE);
//
//	        dataHolder.mOverLayPlay.setBackgroundDrawable(biggerCircle);
		dataHolder.mDeleteLayout.setOnClickListener(mDeleteListener);
		dataHolder.mDeleteLayout.setTag(dataHolder);
		Util.showFeedback(dataHolder.mDeleteLayout);
		dataHolder.mDeleteLayout.setBackgroundColor(Color.TRANSPARENT);
		dataHolder.mOverLayPlay.setOnClickListener(mOpenCardListener);
		dataHolder.mOverLayPlay.setTag(dataHolder);
		dataHolder.mPreview.setOnClickListener(mOpenCardListener);
		dataHolder.mPreview.setTag(dataHolder);
		dataHolder.mRentLayout.setOnClickListener(mPurchaseListener);
		dataHolder.mRentLayout.setTag(dataHolder);
		dataHolder.mFavLayout.setOnClickListener(mFavListener);
		dataHolder.mFavLayout.setTag(dataHolder);
		dataHolder.mFavLayout.setBackgroundColor(Color.TRANSPARENT);
		Util.showFeedback(dataHolder.mFavLayout);
		//17 chars
		float price = 10000f;
		if(data.packages == null){
			dataHolder.mRentText.setText("Free");
			dataHolder.mRentLayout.setOnClickListener(null);
		}else{
			if(data.currentUserData != null && data.currentUserData.purchase != null && data.currentUserData.purchase.size() != 0){
				dataHolder.mRentText.setText("Watch now");
				dataHolder.mRentLayout.setOnClickListener(null);
			}else{
				for(CardDataPackages packageitem:data.packages){
					if(packageitem.priceDetails != null){
						for(CardDataPackagePriceDetailsItem priceDetailItem:packageitem.priceDetails){
							if(priceDetailItem.price < price){
								price = priceDetailItem.price;
							}
						}
						if(mRupeeCode == null){
							mRupeeCode = mContext.getResources().getString(R.string.price_rupeecode); 
						}
						if(price == 0)
						{
							dataHolder.mRentText.setText("Watch now for Free");
							dataHolder.mRentLayout.setOnClickListener(null);
						}
						else
							dataHolder.mRentText.setText(mPriceStarts + mRupeeCode + " "+price);
					}else{
						dataHolder.mRentText.setText("Free");
						dataHolder.mRentLayout.setOnClickListener(null);
					}
				}	
			}
		}
		updateDownloadStatus(data,null);
	}
	public void show() {
//		Log.e(TAG, "updateCardsPosition show =");
		mCardsLayout.updateCardsPosition(getScrollY());
	}
	
	public void setCurrentSelectedIndex(int currentSelectedIndex) {
//		Log.e(TAG, "setCurrentSelectedIndex ");
		if (mCurrentSelectedIndex != currentSelectedIndex) {
			mCurrentSelectedIndex = currentSelectedIndex;
//			Log.d(TAG, "setCurrentSelectedIndex applied");
			if (mCardActionListener != null) {
				if (mNumberofItems > mLoadMoreLastCalledNumberofItems && mCurrentSelectedIndex > mNumberofItems / 2) {
					mCardActionListener.loadmore(mNumberofItems);
					mLoadMoreLastCalledNumberofItems = mNumberofItems;
				}
				if(mDataList.size() > currentSelectedIndex){
					mCardActionListener.selectedCard(mDataList.get(currentSelectedIndex),currentSelectedIndex);
				}
			}
		}
	}

	@Override
    public void fling(int velocityY) {
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		int bottom = getChildAt(0).getHeight();
		
		int maxY = Math.max(0, bottom - height);
		int d = height * 10;
		
		mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0 - d, maxY + d);
		mScroller.setFinalY(mCardsLayout.getSnapPosition(mScroller.getFinalY()));
    }	
	
	@Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
		
		if(!mMotionConsumedByClick){
//			Log.e(TAG, "onScrollChanged show =");
			mCardsLayout.updateCardsPosition(y);
		}
	}
	private boolean trackClickForOpen(MotionEvent ev){
		if(ev.getY() >= openStartYPosition  && ev.getY() <= openEndYPosition){
			return true;
		}
		return false;
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mMotionConsumedByClick = false;
		boolean eventStealed = super.onInterceptTouchEvent(ev);
		if (!eventStealed) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				mMotionDetected = false;
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
	            }
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				if (!mMotionDetected) {
					View view = mCardsLayout.findCardViewAtPoint((int)ev.getRawX(), (int)ev.getRawY());
					if (view != null) {
			        	int i = view.getId();
			        	Log.e(TAG,"onInterceptTouchEvent actioup");
			        	if(trackClickForOpen(ev) && mCardActionListener != null){
			    			Log.e(TAG,"click on open");
			    			mCardActionListener.open(mDataList.get(mCurrentSelectedIndex));			
			    		}
//			        	customSmoothScroll(getScrollX(), i * mCardPositions[1]);
			        	smoothScrollTo(getScrollX(), i * mCardPositions[1]);
					}
				}				
			}
		}
		return eventStealed;
	}
	public void sendViewReadyMsg(boolean value){
		mCardsLayout.sendViewReadyMsg(value);
	}
	public void viewReady(){
		if(mCardActionListener!= null){
			mCardActionListener.viewReady();
		}
	}
	public void moveTo(int position){
		Log.e(TAG, "moveTo =");
		smoothScrollTo(getScrollX(), position * mCardPositions[1]);
	}
	private void customSmoothScroll(int dx,int dy){
		smoothScrollTo(dx, dy);
//		customSmoothScrollTo(dx,dy);
	}
	public void customSmoothScrollBy(int dx, int dy)
	{
	    if (mScroller == null)
	    {
	        smoothScrollBy(dx, dy);
	        return;
	    }

	    if (getChildCount() == 0)
	        return;


	    final int width = getWidth() - getPaddingRight() - getPaddingLeft();
	    final int right = getChildAt(0).getWidth();
	    final int maxX = Math.max(0, right - width);
	    final int scrollX = getScrollX();
	    
	    final int height = getHeight() - getPaddingTop() - getPaddingBottom();
	    final int bottom = getChildAt(0).getBottom();
	    final int maxY = Math.max(0, bottom - height);
	    final int scrollY = getScrollY();
	    dx = Math.max(0, Math.min(scrollX + dx, maxX)) - scrollX;
	    dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY;
 
	    Log.e(TAG,"customSmoothScrollBy mScroller "+mScroller.isFinished());
	    mScroller.startScroll(scrollX, scrollY, dx, dy, 1500);

//        mScroller.startScroll(getScrollX(), scrollY, 0, dy,500);
	    invalidate();
	}

	public void customSmoothScrollTo(int x, int y)
	{
	    customSmoothScrollBy(x - getScrollX(), y - getScrollY());
	}
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
		mMotionConsumedByClick = false;
		boolean eventConsumed = super.onTouchEvent(ev);

		if (eventConsumed) {
			if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				mMotionDetected = true;
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				Log.e(TAG,"onTouchEvent actioup");
//				if (mScroller.isFinished()) {
//					customSmoothScroll(getScrollX(), mCardsLayout.getSnapPosition(getScrollY()));
					smoothScrollTo(getScrollX(), mCardsLayout.getSnapPosition(getScrollY()));
//				}
			}
		}

		return eventConsumed;
	}
	
    @Override
    public void computeScroll() {
    	
    	super.computeScroll();

    	if (mScroller!= null && mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            scrollTo(x, y);
        }
    }
    
    
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
    	
    	if(mMotionConsumedByClick){
    		return mMotionConsumedByClick;
    	}
//    	Log.e(TAG, "overScrollBy " +mMotionConsumedByClick);
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
        		maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);  
    }

	

	// CallBack Listeners

	private CardItemClickListener mDeleteListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			mMotionConsumedByClick = true;
			Log.e(TAG, "mDeleteListener onClick");
			CardDataHolder dataHolder = null;
			if(v.getTag() instanceof CardDataHolder){
				dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
			}else{
				return;
			}
			int index = mDataList.indexOf(dataHolder.mDataObject);
			/*****************************DELTEING DOWNLOAD DATA************************************/
			
			if(myplexapplication.mDownloadList != null){
				CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mDataList.get(index)._id);
				if(mDownloadData!=null){
					Util.removeDownload(mDownloadData.mDownloadId,mContext);
					myplexapplication.mDownloadList.mDownloadedList.remove(mDataList.get(index)._id);
					Util.saveObject(myplexapplication.mDownloadList, myplexapplication.getApplicationConfig().downloadCardsPath);
				}
			}
			/***************************************************************************************/
			mDataList.remove(index);
			mNumberofItems = mDataList.size();
			View localv = mCardsLayout.mCardViewReusePool.getView(index);
			AnimatorSet set = new AnimatorSet();
			set.play(ObjectAnimator.ofFloat(localv, View.TRANSLATION_X, 0,-localv.getWidth()));
			set.setDuration(500);
			set.setInterpolator(new DecelerateInterpolator());
			set.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					mCardsLayout.removeAllViews();
					mCardsLayout.refresh();
					mCardsLayout.updateCardsPosition(getScrollY());
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			set.start();
		}
	};

	private CardItemClickListener mFavListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			mMotionConsumedByClick = true;
			Log.e(TAG, "mFavListener onClick");
			if (mCardActionListener == null){
				return;
			}
			if(v.getTag() instanceof CardDataHolder){
				CardDataHolder dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
				int type = FavouriteUtil.FAVOURITEUTIL_ADD;
				if(dataHolder.mDataObject.currentUserData != null && dataHolder.mDataObject.currentUserData.favorite){
					type = FavouriteUtil.FAVOURITEUTIL_REMOVE;
				}
				dataHolder.mFavProgressBar.setVisibility(View.VISIBLE);
				dataHolder.mFavourite.setVisibility(View.INVISIBLE);
				mCardActionListener.favouriteAction(dataHolder.mDataObject,type);
			}
		}
	};
	private CardItemClickListener mPurchaseListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if(v.getTag() instanceof CardDataHolder){
				CardDataHolder dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
				if(mCurrentSelectedIndex != mDataList.indexOf(dataHolder.mDataObject)){return;}
				mCardActionListener.purchase(dataHolder.mDataObject);
			}
		}
	};

	private CardItemClickListener mOpenCardListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			Log.e(TAG, "mOpenCardListener onClick");
			if (mCardActionListener != null){
				if(v.getTag() instanceof CardDataHolder){
					CardDataHolder dataHolder = (CardDataHolder) v.getTag();
					if(dataHolder == null){ Log.e(TAG, "mOpenCardListener dataholder null"); return;}
					if(dataHolder.mDataObject == null){ Log.e(TAG, "mOpenCardListener object null "); return;}
					if(mCurrentSelectedIndex != mDataList.indexOf(dataHolder.mDataObject)){ Log.e(TAG, "mOpenCardListener index not matching"); return;}
					mCardActionListener.open(dataHolder.mDataObject);
				}
			}
		}
	};

	public void setCardActionListener(CardActionListener listener) {
		this.mCardActionListener = listener;
	}

	public int getNumberOfItems() {
		return mNumberofItems;
	}

	private CardActionListener mCardActionListener;
}

class ViewReusePool {
	SparseArray<View> mViewsFromLastUpdate;
	SparseArray<View> mViewsFromThisUpdate;
	Stack<View> mViewsToReuse;
	
	ViewReusePool() {
		mViewsFromLastUpdate = new SparseArray<View>();
		mViewsFromThisUpdate = new SparseArray<View>();
		mViewsToReuse = new Stack<View>();
	}
	
	public View findView(int i) {
		View view = mViewsFromLastUpdate.get(i);
		if (view != null) {
			mViewsFromLastUpdate.remove(i);
			mViewsFromThisUpdate.put(i, view);
		}
		return view;
	}
	public View getView(int index){
		return mViewsFromLastUpdate.get(index);
	}
	public SparseArray<View> getViewList(){
		return mViewsFromLastUpdate;
	}
	public void refresh(){
		mViewsToReuse = new Stack<View>();
		mViewsFromLastUpdate =  new SparseArray<View>();
		mViewsFromThisUpdate = new SparseArray<View>();
	}
	public View reuseView(int i) {
		if (mViewsToReuse.size() == 0) {
			return null;
		}
		View view = mViewsToReuse.pop();
		mViewsFromThisUpdate.put(i, view);
		view.setVisibility(View.VISIBLE);
		return view;
	}

	public void addView(int i, View view) {
		mViewsFromThisUpdate.put(i, view);
	}
	
	public void finishUpdate () {
		for (int i = 0; i < mViewsFromLastUpdate.size(); ++i) {
			View view = mViewsFromLastUpdate.get(mViewsFromLastUpdate.keyAt(i));
			view.setVisibility(View.INVISIBLE);
			mViewsToReuse.push(view);
		}
		
		mViewsFromLastUpdate = new SparseArray<View>();

		SparseArray<View> temp = mViewsFromLastUpdate;
	    mViewsFromLastUpdate = mViewsFromThisUpdate;
	    mViewsFromThisUpdate = temp;
	}
}

class CardsLayout extends RelativeLayout {
    private static final String TAG = "CardView";
	static final int TOP_CARDS = 0;
	static final int TOP_CARD_VISIBLE_PT = 3;
	
	CardView mAlterCardView;
	public ViewReusePool mCardViewReusePool;

	int mTopCardVisiblePx;
	int mTopMargin;
	boolean sendViewReady = false;
	public void sendViewReadyMsg(boolean value){
		sendViewReady = value;
	}
	public CardsLayout(Context context, CardView alterCardView) {
		super(context);
		
		mAlterCardView = alterCardView;
		mCardViewReusePool = new ViewReusePool();

		float scale = getResources().getDisplayMetrics().density;
		mTopCardVisiblePx = (int)(TOP_CARD_VISIBLE_PT * scale);
		mTopMargin = TOP_CARDS * mTopCardVisiblePx;
	}
	
	int getParentHeight() {
		return mAlterCardView.getHeight() - mAlterCardView.getPaddingTop() - mAlterCardView.getPaddingBottom();	
	}
	
	int getContentHeight() {
		return (mAlterCardView.getNumberOfItems() - 1) * mAlterCardView.getCardPositions()[1];		
	}
	void refresh(){
		mCardViewReusePool.refresh();
	}
	void printTitle(View v,int i ){
		if(v != null){
			TextView txt = (TextView)v.findViewById(R.id.card_title_name);
			Log.e(TAG, "title "+txt.getText()+" for the view "+i);
		}
	}
	void changeCardViewPosition(int i, int pos) {
//		Log.e(TAG, "changeCardViewPosition i ="+i+" pos ="+getChildCount());
	    View cardView = mCardViewReusePool.findView(i);
	    if (cardView == null) {
	        cardView = mCardViewReusePool.reuseView(i);
	        if (cardView == null) {
	    		cardView = mAlterCardView.createCardView();
		    	addView(cardView);
		    	mCardViewReusePool.addView(i, cardView);
	        }
	        mAlterCardView.applyData(cardView, i);
	    }
//	    printTitle(cardView,i);
	    cardView.layout(0, pos, getWidth(), (int)(pos + mAlterCardView.getCardHeight()));
        bringChildToFront(cardView);
	}
	private boolean viewUpdated = false;
	public void updateCardsPosition(int scrollOffset) {
//		Log.e(TAG, "updateCardsPosition scrollOffset ="+scrollOffset);
	    scrollOffset = Math.min(getContentHeight(), scrollOffset);
	    scrollOffset = Math.max(0, scrollOffset);
	    
	    int offset = scrollOffset;
	    int iTopCard = offset / mAlterCardView.getCardPositions()[1];
	    int percent = offset % mAlterCardView.getCardPositions()[1];
	    
	    int y = 0;
	    for (int i = Math.max(0, iTopCard - TOP_CARDS); i < mAlterCardView.getNumberOfItems(); ++i) {
	    	int j = i - iTopCard;
	        if (j > 0) {
	            if (j < mAlterCardView.getCardPositions().length) {
	                y = (int) Math.round(mTopMargin + mAlterCardView.getCardPositions()[j] - (mAlterCardView.getCardPositions()[j] - mAlterCardView.getCardPositions()[j - 1]) * (double)percent / mAlterCardView.getCardPositions()[1]);
	            } else {
	                y += mAlterCardView.getCardPositions()[mAlterCardView.getCardPositions().length - 1] - mAlterCardView.getCardPositions()[mAlterCardView.getCardPositions().length - 2];
	            }
	        } else {
	            y = Math.max(mTopMargin + j * mTopCardVisiblePx, mTopMargin - TOP_CARDS * mTopCardVisiblePx);
	        }
	        
	        if (y > mAlterCardView.getHeight()) {
	            break;
	        }
	    	changeCardViewPosition(i, scrollOffset + Math.round(y));
	    	viewUpdated = true;
	    }
	    
	    mCardViewReusePool.finishUpdate();
	    
	    mAlterCardView.setCurrentSelectedIndex(iTopCard);
	}
	
	public int getSnapPosition(int y) {
		int iTopCard = y / mAlterCardView.getCardPositions()[1];

        int y1 = iTopCard * mAlterCardView.getCardPositions()[1];
        int y2 = (iTopCard + 1) * mAlterCardView.getCardPositions()[1];

        return Math.abs(y - y1) < Math.abs(y - y2) ? y1 : y2;
	}
	
	public View findCardViewAtPoint(int x, int y) {
	    for (int index = getChildCount() - 1; index >= 0; --index) {
	        View view = getChildAt(index);
	        if (view.getVisibility() == View.VISIBLE) {
		        int location[] = new int[2];
		        view.getLocationOnScreen(location);
		        int viewX = location[0];
		        int viewY = location[1];
		        
		        // is point is inside view bounds
		        if ((x > viewX && x < (viewX + view.getWidth())) && (y > viewY && y < (viewY + view.getHeight()))) {
		        	return view;
		        }
	        }
	    }
	    return null;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(viewUpdated){
//			Log.e(TAG, "onLayout show =");
			updateCardsPosition(mAlterCardView.getScrollY());
			viewUpdated = false;
		}
		if(sendViewReady){
			mAlterCardView.viewReady();
			sendViewReady = false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		if (mAlterCardView.getNumberOfItems() > 0) {
			int count = getChildCount();
		    for (int index = 0; index < count; ++index) {
		        View child = getChildAt(index);
		        child.measure(widthMeasureSpec, heightMeasureSpec);
		    }		
			
			h = getContentHeight() + getParentHeight(); 
		}

		setMeasuredDimension(w, h);
	}
}
