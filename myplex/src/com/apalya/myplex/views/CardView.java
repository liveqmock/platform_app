package com.apalya.myplex.views;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardItemClickListener;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

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

		mScreenHeight = myplexUtils.mScreenHeight;
		//mScreenWidth = dm.widthPixels;

		int margin = (int) (2 * (getResources().getDimension(R.dimen.cardmargin)));
		mAvailableScreenSize = (int) (mScreenHeight - mActionBarHeight - margin);

		mCardHeight = getCardLayoutHeight();
		mCardTitleHeight = getResources().getDimension(R.dimen.cardtitleheight);

		initScrollView();
	}
    
	public void setContext(Context cxt) {
		mContext = cxt;
		mInflater = LayoutInflater.from(mContext);
		init();
	}

	private void prepareViewPositions() {
		float firstItemStart = 0; // assuming first card starts at 0 position
		float secondItemStart = mCardHeight + getResources().getDimension(R.dimen.cardmargin);
		float remainingsize = mAvailableScreenSize - (secondItemStart + 4 * mCardTitleHeight);
		float thirdItemStart = secondItemStart + mCardTitleHeight + remainingsize * 0.65f;
		float fourthItemStart = thirdItemStart + mCardTitleHeight + remainingsize * 0.35f;
		float fifthItemStart = fourthItemStart + mCardTitleHeight;
		float sixthItemStart = fifthItemStart + mCardTitleHeight;

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
		mAvailableScreenSize = (int) (mScreenHeight - (getStatusBarHeight() + mActionBarHeight + margin));
		prepareViewPositions();
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		if (result > 100) {
			result = 48;
		}
		return result;
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
		View localv = mCardsLayout.mCardViewReusePool.findView(index);
		applyData(localv, index);
	}

	public View createCardView() {
		return mInflater.inflate(R.layout.card, null);		
	}
	
	public void applyData(View v, int position) {
		if (v == null || !(position >= 0 && position < mNumberofItems)) {
			return;
		}

		CardData data = mDataList.get(position);
		v.setId(position);

		CardViewMeta tagData = (CardViewMeta) v.getTag();
		if (tagData == null) {
			tagData = new CardViewMeta();
			tagData.mUiHolder = new CardViewHolder();
			tagData.mUiHolder.mDelete = (LinearLayout) v.findViewById(R.id.card_title_delete);
			tagData.mUiHolder.mFavourite = (LinearLayout) v.findViewById(R.id.card_title_fav);
			tagData.mUiHolder.mFavouriteProgress = (LinearLayout) v.findViewById(R.id.card_title_fav_progress);
			tagData.mUiHolder.mFavourite_Image = (ImageView) v.findViewById(R.id.card_title_fav_image);
			tagData.mUiHolder.mRent = (ImageView) v.findViewById(R.id.card_info_rent);
			tagData.mUiHolder.mInfo = (LinearLayout) v.findViewById(R.id.card_info_more);
			tagData.mUiHolder.mText = (TextView) v.findViewById(R.id.card_title_name);
			tagData.mUiHolder.mText.setTypeface(FontUtil.Roboto_Medium);
			tagData.mUiHolder.mPlay = (NetworkImageView) v.findViewById(R.id.card_preview_image);
			v.setTag(tagData);
		}

		tagData.mObj = data;
		tagData.mUiHolder.mDelete.setOnClickListener(mDeleteListener);
		tagData.mUiHolder.mDelete.setTag(data);
		tagData.mUiHolder.mFavourite.setOnClickListener(mFavListener);
		tagData.mUiHolder.mFavourite.setTag(data);
		tagData.mUiHolder.mRent.setOnClickListener(mPurchaseListener);
		tagData.mUiHolder.mRent.setTag(data);
		tagData.mUiHolder.mInfo.setOnClickListener(mMoreInfoListener);
		tagData.mUiHolder.mInfo.setTag(data);
		tagData.mUiHolder.mText.setText(data.title);
		tagData.mUiHolder.mPlay.setOnClickListener(mPlayListener);
		tagData.mUiHolder.mPlay.setTag(data);
		if(data.applyFavoriteInProgress){
			tagData.mUiHolder.mFavourite.setVisibility(View.INVISIBLE);
			tagData.mUiHolder.mFavouriteProgress.setVisibility(View.VISIBLE);
		}else{
			tagData.mUiHolder.mFavourite.setVisibility(View.VISIBLE);
			tagData.mUiHolder.mFavouriteProgress.setVisibility(View.INVISIBLE);
		}
		if(data.isFavorite){
			tagData.mUiHolder.mFavourite_Image.setImageResource(R.drawable.card_fav);
		}else{
			tagData.mUiHolder.mFavourite_Image.setImageResource(R.drawable.card_unfav);
		}
		
		if (data.imageUrl == null || data.imageUrl.compareTo("Images/NoImage.jpg") == 0) {
			tagData.mUiHolder.mPlay.setImageResource(data.resId);
		} else if (data.imageUrl != null){
			tagData.mUiHolder.mPlay.setImageUrl(data.imageUrl,MyVolley.getImageLoader());
		}
	}
	public void show() {
		mCardsLayout.updateCardsPosition(getScrollY());
	}
	
	public void setCurrentSelectedIndex(int currentSelectedIndex) {
		if (mCurrentSelectedIndex != currentSelectedIndex) {
			mCurrentSelectedIndex = currentSelectedIndex;
	
			if (mCardActionListener != null) {
				if (mNumberofItems > mLoadMoreLastCalledNumberofItems && mCurrentSelectedIndex > mNumberofItems / 2) {
					mCardActionListener.loadmore(mNumberofItems);
					mLoadMoreLastCalledNumberofItems = mNumberofItems;
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
			mCardsLayout.updateCardsPosition(y);
		}
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
			        	smoothScrollTo(getScrollX(), i * mCardPositions[1]);
					}
				}				
			}
		}
		return eventStealed;
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
		mMotionConsumedByClick = false;
		boolean eventConsumed = super.onTouchEvent(ev);

		if (eventConsumed) {
			if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				mMotionDetected = true;
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
//				if (mScroller.isFinished()) {
					smoothScrollTo(getScrollX(), mCardsLayout.getSnapPosition(getScrollY()));
//				}
			}
		}

		return eventConsumed;
	}
	
    @Override
    public void computeScroll() {
    	
    	super.computeScroll();

    	if (mScroller.computeScrollOffset()) {
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
    	Log.e(TAG, "overScrollBy " +mMotionConsumedByClick);
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
        		maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);  
    }

	

	// CallBack Listeners

	private CardItemClickListener mDeleteListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			mMotionConsumedByClick = true;
			Log.e(TAG, "mDeleteListener onClick");
			CardData data = (CardData) v.getTag();
			if(data == null ){
				return;
			}
			int index = mDataList.indexOf(data);
			mDataList.remove(index);
			mNumberofItems = mDataList.size();
			View localv = mCardsLayout.mCardViewReusePool.getView(index);
			AnimatorSet set = new AnimatorSet();
			set.play(ObjectAnimator.ofFloat(localv, View.TRANSLATION_X, 0,-localv.getWidth()));
			set.setDuration(2000);
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
			if (mCardActionListener == null){
				return;
			}
			Log.e(TAG, "mFavListener onClick");
			CardData data = (CardData) v.getTag();
			if(data == null ){
				return;
			}
			data.applyFavoriteInProgress = true;
			int index = mDataList.indexOf(data);
			View localv = mCardsLayout.mCardViewReusePool.getView(index);
			if(localv != null){
				CardViewMeta tagData = (CardViewMeta)localv.getTag();
				if(tagData != null){
					tagData.mUiHolder.mFavourite.setVisibility(View.INVISIBLE);
					tagData.mUiHolder.mFavouriteProgress.setVisibility(View.VISIBLE);	
				}
			}
			if(!data.isFavorite){
				mCardActionListener.addFavourite(data);	
			}else{
				mCardActionListener.removeFavourite(data);
			}
			localv.requestLayout();
			
		}
	};

	private CardItemClickListener mPlayListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener != null){
				CardData data = (CardData) v.getTag();
				if(data == null ){
					return;
				}
				if(mCurrentSelectedIndex != mDataList.indexOf(data)){
					return;
				}
				mCardActionListener.play(data);
			}
		}
	};

	private CardItemClickListener mPurchaseListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener != null)
				mCardActionListener.purchase(v.getId());
		}
	};

	private CardItemClickListener mMoreInfoListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener != null){
				CardData data = (CardData) v.getTag();
				if(data == null){
					return;
				}
				if(mCurrentSelectedIndex != mDataList.indexOf(data)){
					return;
				}
				mCardActionListener.play(data);
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
	public void refresh(){
		mViewsToReuse.clear();
		mViewsFromLastUpdate.clear();
		mViewsFromThisUpdate.clear();
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
		
		mViewsFromLastUpdate.clear();

		SparseArray<View> temp = mViewsFromLastUpdate;
	    mViewsFromLastUpdate = mViewsFromThisUpdate;
	    mViewsFromThisUpdate = temp;
	}
}

class CardsLayout extends FrameLayout {
	static final int TOP_CARDS = 0;
	static final int TOP_CARD_VISIBLE_PT = 3;
	
	CardView mAlterCardView;
	public ViewReusePool mCardViewReusePool;

	int mTopCardVisiblePx;
	int mTopMargin;
	
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
			Log.e("CardView", "title "+txt.getText()+" for the view "+i);
		}
	}
	void changeCardViewPosition(int i, int pos) {
//		Log.e("CardView", "changeCardViewPosition i ="+i+" pos ="+getChildCount());
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
	
	public void updateCardsPosition(int scrollOffset) {
//		Log.e("CardView", "updateCardsPosition scrollOffset ="+scrollOffset);
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
		updateCardsPosition(mAlterCardView.getScrollY());
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
