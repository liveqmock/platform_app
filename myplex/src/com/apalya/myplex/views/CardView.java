package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.utils.FontUtil;

import android.widget.LinearLayout;

public class CardView extends FrameLayout {

	private String TAG = "CardView";
	private OnLoadMoreListener mLoadMoreListener = null;

	private List<CardData> mDataList = new ArrayList<CardData>();

	private int mNumberofItems = 0;

	private List<View> mViewList = new ArrayList<View>();

	private int mCurrentSelectedIndex = 0;

	private int mShortAnimationDuration;

	private float mCardHeight = 0;

	private float mCardTitleHeight = 0;

	private int mScreenHeight = 0;

	private int mActionBarHeight = 0;

	private int mScreenWidth = 0;

	private Context mContext;

	private int mNumberofViewInMemory = 6;

	private int mLastItemAdded;

	private LayoutInflater mInflator;

	private int availableScreenSize;

	private double thirdRelativeUnitUp;
	private double fourthRelativeUnitUp;
	private double fifthRelativeUnitUp;
	private double sixthRelativeUnitUp;

	private float secondRelativeUnitDown;
	private float thirdRelativeUnitDown;
	private float fourthRelativeUnitDown;
	private float fifthRelativeUnitDown;
	private float sixthRelativeUnitDown;

	private float firstViewLastPosition;
	private float secondViewLastPosition;
	private float thirdViewLastPosition;
	private float fourthViewLastPosition;
	private float fifthViewLastPosition;
	private float sixthViewLastPosition;

	private View firstView;
	private View secondView;
	private View thirdView;
	private View fourthView;
	private View fifthView;
	private View sixthView;

	private int mTouchSlop;
	private int mTouchVelocityUnit;
	private boolean mIsBeingDragged = false;
	private float mLastMotionY;

	private int SWIPEDOWN = 1;
	private int SWIPEUP = 0;
	private float intialYPostion = 0;
	private float currentYPosition = 0;
	private int movingDirection = SWIPEUP;
	private float distanceMoved = (float) 0.0;

	private float firstItemStart;
	private float secondItemStart;
	private float thirdItemStart;
	private float fourthItemStart;
	private float fifthItemStart;
	private float sixthItemStart;

	private VelocityTracker mVelocityTracker;

	private OverScroller mScroller;

	private int mMinimumVelocity;
	private int mMaximumVelocity;

	private int mOverscrollDistance;
	private int mOverflingDistance;

	private int mActivePointerId = INVALID_POINTER;
	private static final int INVALID_POINTER = -1;
	private static final int MINIMUMVIEWS = 2;

	private OnLoadMoreListener mOnLoadMoreListener;
	private OnFavouriteListener mOnFavouriteListener;
	private OnPlayListener mOnPlayListener;
	private OnDeleteListener mOnDeleteListener;
	private OnMoreInfoListener mOnMoreInfoListener;
	private OnPurchaseListener mOnPurchaseListener;
	private OnItemSelectedListener mOnItemSelectedListener;

	public interface OnLoadMoreListener {
		public void loadmore();
	}

	public interface OnItemSelectedListener {
		public void selectedCard(int index);
	}

	public interface OnFavouriteListener {
		public void addFavourite(int index);

		public void removeFavourite(int index);
	}

	public interface OnDeleteListener {
		public void deletedCard(CardData data);
	}

	public interface OnMoreInfoListener {
		public void moreInfo(int index);
	}

	public interface OnPurchaseListener {
		public void purchase(int index);
	}

	public interface OnPlayListener {
		public void play(int index);
	}

	// CallBack Listeners

	private OnClickListener mDeleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.e(TAG, "mDeleteListener onClick");
			deleteCard(v);
		}
	};
	private OnClickListener mFavListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnFavouriteListener != null)
				mOnFavouriteListener.addFavourite(v.getId());
		}
	};
	private OnClickListener mPlayListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnPlayListener != null)
				mOnPlayListener.play(v.getId());
		}
	};
	private OnClickListener mPurchaseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnPurchaseListener != null)
				mOnPurchaseListener.purchase(v.getId());
		}
	};
	private OnClickListener mMoreInfoListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnMoreInfoListener != null)
				mOnMoreInfoListener.moreInfo(v.getId());
		}
	};

	/*
	 * Construction
	 */
	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CardView(Context context) {
		super(context);
	}

	public int getSelectedIndex() {
		return mCurrentSelectedIndex;
	}

	private void deleteCard(final View v) {
		if (v != null) {
			return;
		}
		CardData data = (CardData) v.getTag();
		if (data == null) {
			return;
		}
		int position = mDataList.indexOf(data);
		mDataList.remove(data);
		mNumberofItems = mDataList.size();
		switch (mIndexofSelectedView) {
		case 1: {
			applyData(firstView, mCurrentSelectedIndex+1);
			applyData(secondView, mCurrentSelectedIndex+2);
			applyData(thirdView, mCurrentSelectedIndex+3);
			applyData(fourthView, mCurrentSelectedIndex+4);
			applyData(fifthView, mCurrentSelectedIndex+5);
			applyData(sixthView, mCurrentSelectedIndex+6);
			break;
		}
		case 2: {
			applyData(secondView, mCurrentSelectedIndex+2);
			applyData(thirdView, mCurrentSelectedIndex+3);
			applyData(fourthView, mCurrentSelectedIndex+4);
			applyData(fifthView, mCurrentSelectedIndex+5);
			applyData(sixthView, mCurrentSelectedIndex+6);
			break;
		}
		case 3: {
			applyData(thirdView, mCurrentSelectedIndex+3);
			applyData(fourthView, mCurrentSelectedIndex+4);
			applyData(fifthView, mCurrentSelectedIndex+5);
			applyData(sixthView, mCurrentSelectedIndex+6);
			break;
		}
		case 4: {
			applyData(fourthView, mCurrentSelectedIndex+4);
			applyData(fifthView, mCurrentSelectedIndex+5);
			applyData(sixthView, mCurrentSelectedIndex+6);
			break;
		}
		case 5: {
			applyData(fifthView, mCurrentSelectedIndex+5);
			applyData(sixthView, mCurrentSelectedIndex+6);
			break;
		}

		}
		// switch (mIndexofSelectedView) {
		// case 2:
		// animate(secondItemStart,thirdView,0);
		// animate(thirdItemStart,fourthView,0);
		// animate(fourthItemStart,fifthView,0);
		// animate(fifthItemStart,sixthView,0);
		// break;
		// case 3:
		// animate(thirdItemStart,fourthView,0);
		// animate(fourthItemStart,fifthView,0);
		// animate(fifthItemStart,sixthView,0);
		// break;
		// case 4:
		// animate(fourthItemStart,fifthView,0);
		// animate(fifthItemStart,sixthView,0);
		// break;
		// case 5:
		// animate(fifthItemStart,sixthView,0);
		// break;
		// default:
		// break;
		// }

		// int numberofItems = Math.abs(mCurrentSelectedIndex - mNumberofItems);
		// if (mLastItemAdded + 1 < mNumberofItems) {
		// mLastItemAdded++;
		//
		// if (numberofItems >= mNumberofViewInMemory) {
		// removeView(v);
		// addView(v);
		// applyData(v, mLastItemAdded);
		// v.setY(sixthItemStart);
		// } else {
		// Log.w(TAG, "problem");
		// }
		// }
		// if (numberofItems < mNumberofViewInMemory
		// && getChildCount() > MINIMUMVIEWS) {
		// removeView(v);
		// mViewStack.push(v);
		// }

		// View v = getChildAt(mIndexofSelectedView);
		// removeView(v);
		// addView(v);

		// List<View> viewList = new ArrayList<View>();
		// final View v = getChildWithValue(position);
		// for (int i = 0; i < getChildCount(); i++) {
		// viewList.add(getChildAt(i));
		// }
		// // mDataList.remove(data);
		// // mNumberofItems = mDataList.size();
		// // movetoNext(true);
		// // saveIntialPositions();
		// // completeMovement(false);
		// // }
		// if (v == null) {
		// return;
		// }
		// AnimatorSet set = new AnimatorSet();
		// set.play(ObjectAnimator.ofFloat(v, View.X, 0, -mScreenWidth));
		// set.setDuration(mShortAnimationDuration);
		// set.setInterpolator(new DecelerateInterpolator());
		// set.addListener(new AnimatorListener() {
		//
		// @Override
		// public void onAnimationStart(Animator arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animator arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onAnimationEnd(Animator arg0) {
		//
		// mDataList.remove(data);
		// mNumberofItems = mDataList.size();
		// movetoNext(true);
		// saveIntialPositions();
		// completeMovement(false);
		// v.setX(0);
		// if (mOnDeleteListener != null) {
		// mOnDeleteListener.deletedCard(data);
		// }
		// }
		//
		// @Override
		// public void onAnimationCancel(Animator arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		// set.start();

		// movetoNext(true);

	}

	/*
	 * preparing
	 */
	private void init() {
		mInflator = LayoutInflater.from(mContext);

		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_longAnimTime);

		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;

		int margin = (int) (2 * (getResources()
				.getDimension(R.dimen.cardmargin)));
		availableScreenSize = (int) (mScreenHeight - mActionBarHeight - margin);

		this.mCardHeight = getCardLayoutHeight();
		this.mCardTitleHeight = getResources().getDimension(
				R.dimen.cardtitleheight);

		mScroller = new OverScroller(getContext());
		setFocusable(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);
		final ViewConfiguration configuration = ViewConfiguration.get(mContext);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mOverscrollDistance = configuration.getScaledOverscrollDistance();
		mOverflingDistance = configuration.getScaledOverflingDistance();
			configuration.getScrollFriction();
		mTouchVelocityUnit = configuration.getScaledMaximumFlingVelocity() / 5;

		// prepareViewPositions();
		// setOnTouchListener(new SwipeDismissCardListener(this,new
		// OnDeleteCardListener() {
		//
		// @Override
		// public void onDeleteCard(View dismissView) {
		// if (dismissView != null) {
		// CardData data = (CardData)dismissView.getTag();
		// mDataList.remove(data);
		// mNumberofItems = mDataList.size();
		// movetoNext(true);
		// saveIntialPositions();
		// completeMovement(false);
		// dismissView.setX(0);
		// if(mOnDeleteListener != null){
		// mOnDeleteListener.deletedCard(data);
		// }
		// // deleteCard();
		// }
		// }
		// }));
	}

	public void setDefaultViewCount(int count) {
		this.mNumberofViewInMemory = count;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener listener) {
		this.mOnLoadMoreListener = listener;
	}

	public void setOnFavouriteListener(OnFavouriteListener listener) {
		this.mOnFavouriteListener = listener;
	}

	public void setOnDeleteListener(OnDeleteListener listener) {
		this.mOnDeleteListener = listener;
	}

	public void setOnMoreInfoListener(OnMoreInfoListener listener) {
		this.mOnMoreInfoListener = listener;
	}

	public void setOnPurchaseListener(OnPurchaseListener listener) {
		this.mOnPurchaseListener = listener;
	}

	public void setOnPlayListener(OnPlayListener listener) {
		this.mOnPlayListener = listener;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.mOnItemSelectedListener = listener;
	}

	private float getCardLayoutHeight() {
		return getResources().getDimension(R.dimen.cardtitleheight)
				+ getResources().getDimension(R.dimen.cardplayheight)
				+ getResources().getDimension(R.dimen.cardstatusheight)
				+ getResources().getDimension(R.dimen.cardmoreinfoheight);
	}

	private void prepareViewPositions() {
		// Log.e(TAG, "prepareGaps start");
		mThreeshold = (int) (mCardHeight - (getResources().getDimension(
				R.dimen.cardmoreinfoheight) + getResources().getDimension(
				R.dimen.cardstatusheight)));
		firstItemStart = 0; // assuming first card starts at 0 position

		float firstItemEnd = mCardHeight; // first shows the full card

		secondItemStart = firstItemEnd
				+ getResources().getDimension(R.dimen.cardmargin);

		float remainingsize = (availableScreenSize)
				- (secondItemStart + 4 * mCardTitleHeight);

		float secondItemEnd = secondItemStart + mCardTitleHeight
				+ (float) (remainingsize * 0.65);

		thirdItemStart = secondItemEnd;

		float thirdItemEnd = thirdItemStart + mCardTitleHeight
				+ (float) (remainingsize * 0.35);

		fourthItemStart = thirdItemEnd;
		float fourthItemEnd = fourthItemStart + mCardTitleHeight;

		fifthItemStart = fourthItemEnd;
		float fifthItemEnd = fifthItemStart + mCardTitleHeight;

		sixthItemStart = fifthItemEnd;
		float sixthItemEnd = sixthItemStart + mCardTitleHeight;

		thirdRelativeUnitUp = (secondItemEnd - secondItemStart)
				/ secondItemStart;

		fourthRelativeUnitUp = (thirdItemEnd - thirdItemStart)
				/ secondItemStart;

		fifthRelativeUnitUp = (fourthItemEnd - fourthItemStart)
				/ secondItemStart;

		sixthRelativeUnitUp = (fifthItemEnd - fifthItemStart) / secondItemStart;

		secondRelativeUnitDown = (secondItemEnd - secondItemStart)
				/ secondItemStart;

		thirdRelativeUnitDown = (thirdItemEnd - thirdItemStart)
				/ secondItemStart;

		fourthRelativeUnitDown = (fourthItemEnd - fourthItemStart)
				/ secondItemStart;

		fifthRelativeUnitDown = (fifthItemEnd - fifthItemStart)
				/ secondItemStart;

		sixthRelativeUnitDown = (sixthItemEnd - sixthItemStart)
				/ secondItemStart;

		// Log.e(TAG, "prepareGaps end");
	}

	private float getGap(int index) {
		// Log.e(TAG, "getGap start");
		// prepareGaps();
		float gap = mCardHeight;
		switch (index) {
		case 0: {
			gap = firstItemStart;
		}
			break;
		case 1: {
			gap = secondItemStart;
		}
			break;
		case 2: {
			gap = thirdItemStart;
		}
			break;
		case 3: {
			gap = fourthItemStart;
		}
			break;
		case 4: {
			gap = fifthItemStart;
		}
			break;
		case 5: {
			gap = sixthItemStart;
		}
			break;
		default:
			View v = getChildAt(getChildCount() - 1);// mViewList.get(mViewList.size()
			gap = v.getY() + mCardTitleHeight;
			break;
		}
		// Log.e(TAG, "getGap end");
		return gap;
	}

	public void setContext(Context cxt) {
		mContext = cxt;
		init();
	}

	public void setActionBarHeight(int actionBarHeight) {
		this.mActionBarHeight = actionBarHeight;
		int margin = (int) (2 * (getResources()
				.getDimension(R.dimen.cardmargin)));
		availableScreenSize = (int) (mScreenHeight - (getStatusBarHeight()
				+ mActionBarHeight + margin));
		prepareViewPositions();
	}

	public int getStatusBarHeight() {
		// Log.e(TAG, "getStatusBarHeight start");
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		// Log.e(TAG, "getStatusBarHeight end");
		return result;
	}

	public void setData(List<CardData> dataList) {
		if (dataList != null) {
			this.mDataList = dataList;
			mNumberofItems = mDataList.size();
		}
	}

	public List<CardData> getDataList() {
		return mDataList;
	}

	public CardData getData(int index) {
		if (index < 0) {
			return null;
		}
		if (index > mNumberofItems) {
			return null;
		}
		return mDataList.get(index);
	}

	public void addData(CardData data) {
		this.mDataList.add(data);
		mNumberofItems = mDataList.size();
	}

	public void addData(List<CardData> datalist) {
		if (datalist != null) {
			for (CardData data : datalist) {
				this.mDataList.add(data);
			}
		}
		mNumberofItems = mDataList.size();
	}

	private boolean mRetainSamePosition = false;

	private void sendDelayedEvent() {
		mAnimationinProgress = false;
		mLastMotionY = 0;
		if (!mRetainSamePosition) {
			movetoNext(false);
		}
		Log.e(TAG, "sendDelayedEvent start");
		saveIntialPositions();
		isScrolling = false;
		Log.v(TAG, "isScrolling " + isScrolling);
		directionApplied = false;
		if (mNumberofViewsToMove > 0 && mAutoScroll) {
			completeScroll(false);
			mNumberofViewsToMove--;
		} else {
			mAutoScroll = false;
		}
	}

	private boolean mAnimationinProgress = false;
	int mThreeshold;

	private void animateUp() {
		mRetainSamePosition = false;
		mAnimationinProgress = true;
		animate(0, firstView, 0);
		if (secondView == null) {
			sendDelayedEvent();
			return;
		}
		animate(0, secondView, 0);
		if (thirdView == null) {
			sendDelayedEvent();
			return;
		}
		animate(secondItemStart, thirdView, 0);
		if (fourthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(thirdItemStart, fourthView, 0);
		if (fifthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fourthItemStart, fifthView, 0);
		if (sixthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fifthItemStart, sixthView, 1);
	}

	private void animateDown() {
		mRetainSamePosition = false;
		mAnimationinProgress = true;
		if (firstView == null) {
			sendDelayedEvent();
			return;
		}
		animate(secondItemStart, firstView, 0);
		if (secondView == null) {
			sendDelayedEvent();
			return;
		}
		animate(thirdItemStart, secondView, 0);
		if (thirdView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fourthItemStart, thirdView, 0);
		if (fourthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fifthItemStart, fourthView, 0);
		if (fifthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(sixthItemStart, fifthView, 0);
		if (sixthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fifthItemStart + mCardTitleHeight, sixthView, 1);
	}

	private void animateDownOnSamePosition() {
		mAnimationinProgress = true;
		mRetainSamePosition = true;
		animate(0, firstView, 0);
		if (secondView == null) {
			sendDelayedEvent();
			return;
		}
		animate(secondItemStart, secondView, 0);
		if (thirdView == null) {
			sendDelayedEvent();
			return;
		}
		animate(thirdItemStart, thirdView, 0);
		if (fourthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fourthItemStart, fourthView, 0);
		if (fifthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(fifthItemStart, fifthView, 0);
		if (sixthView == null) {
			sendDelayedEvent();
			return;
		}
		animate(sixthItemStart, sixthView, 1);
	}

	private void completeMovement(boolean fromfling) {
		if (mAnimationinProgress) {
			return;
		}
		if (mAutoScroll) {
			movingDirection = SWIPEUP;
			animateUp();
		} else {
			if (movingDirection == SWIPEUP) {
				/*
				 * if (fromfling) { animateUp(); } else
				 */if (secondView != null && secondView.getY() < mThreeshold) {
					animateUp();
				} else {
					Log.e(TAG, "completeMovement animateDownOnSamePosition");
					animateDownOnSamePosition();
				}
			} else {
				animateDown();
			}
		}
	}

	private void animate(float fromY, float toY, final View v, int speed) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromY, toY));
		set.setDuration(speed);
		set.setInterpolator(new DecelerateInterpolator());
		set.start();
	}

	private AnimatorListener mAnimationListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			sendDelayedEvent();
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
			// TODO Auto-generated method stub

		}
	};

	private void animate(float toY, final View v, final int updateView) {
		// Log.e(TAG, "animate start");
		if (v == null) {
			return;
		}
		float fromY = v.getY();
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromY, toY));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		if (updateView == 1) {
			set.addListener(mAnimationListener);
		}
		set.start();
		// Log.e(TAG, "animate end");
	}

	private Stack<View> mViewStack = new Stack<View>();

	public void forceUpdate() {
		removeAllViews();
		show();
	}

	public void show() {
		if (getChildCount() > 0) {
			return;
		}
		int count = 0;
		for (int i = 0; i < mNumberofItems; i++) {
			count++;
			if (count > mNumberofViewInMemory) {
				break;
			}
			mCurrentSelectedIndex = 0;
			mLastItemAdded = i;
			if (i == 0) {
				// extra view for recycle
				addViewCustom(i);
			}
			addViewCustom(i);
		}
	}

	private void sendDelayedCallBack() {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				if (mOnItemSelectedListener != null) {
					mOnItemSelectedListener.selectedCard(mCurrentSelectedIndex);
				}
			}
		});
	}

	private void movetoNext(boolean direct) {
		if (getChildCount() == 0) {
			return;
		}
		if (movingDirection == SWIPEUP) {
			if (mCurrentSelectedIndex + 1 < mNumberofItems) {
				mCurrentSelectedIndex++;
				sendDelayedCallBack();
			}
			int numberofItems = Math
					.abs(mCurrentSelectedIndex - mNumberofItems);
			if (mLastItemAdded + 1 < mNumberofItems) {
				mLastItemAdded++;

				if (numberofItems >= mNumberofViewInMemory) {
					View v = getChildAt(0);
					removeView(v);
					addView(v);
					applyData(v, mLastItemAdded);
					if (direct) {
						v.setY(sixthItemStart);
					} else {
						v.setY(sixthItemStart + mCardTitleHeight);
						animate(sixthItemStart + mCardTitleHeight,
								sixthItemStart, v, mShortAnimationDuration);
					}

				} else {
					Log.w(TAG, "problem");
				}
			}
			if (numberofItems < mNumberofViewInMemory
					&& getChildCount() > MINIMUMVIEWS) {
				View localv = getChildAt(0);
				removeView(localv);
				mViewStack.push(localv);
			}
		} else {
			if (mCurrentSelectedIndex - 1 >= 0) {
				mCurrentSelectedIndex--;
				mLastItemAdded--;
				sendDelayedCallBack();
				View v = null;
				if (!mViewStack.empty()) {
					mLastItemAdded++;
					v = mViewStack.pop();
					removeView(v);
				} else {
					v = getChildAt(getChildCount() - 1);
					removeView(v);
				}
				addView(v, 0);
				v.setY(firstItemStart);
				// prepare data for background view
				if (mCurrentSelectedIndex - 1 >= 0) {
					// localdata = mDataList.get(mCurrentSelectedIndex - 1);
					applyData(v, mCurrentSelectedIndex - 1);
				}

			}
		}
	}

	private void addViewCustom(int position) {
		if (position < 0) {
			return;
		}
		if (position > mNumberofItems) {
			return;
		}

		View v = null;
		v = mInflator.inflate(R.layout.card, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
				(int) mContext.getResources().getDimension(R.dimen.cardvideosize));
		v.setLayoutParams(params);
		addView(v);
		applyData(v, position);
		firstTimeAnimation(v, position);
	}

	private void firstTimeAnimation(View v, int position) {
		float gap = getGap(position);
		v.setY(availableScreenSize / 2);
		animate(availableScreenSize / 2, gap, v, 2000);
	}

	private void applyData(View v, int position) {
		if (v == null || position > mDataList.size()) {
			return;
		}
		CardData data = mDataList.get(position);
		v.setId(position);
		CardViewMeta tagData = (CardViewMeta) v.getTag();
		if (tagData == null) {
			tagData = new CardViewMeta();
			tagData.mUiHolder = new CardViewHolder();
			tagData.mUiHolder.mDelete = (LinearLayout) v
					.findViewById(R.id.card_title_delete);
			tagData.mUiHolder.mFavourite = (LinearLayout) v
					.findViewById(R.id.card_title_fav);
			tagData.mUiHolder.mRent = (ImageView) v
					.findViewById(R.id.card_info_rent);
			tagData.mUiHolder.mInfo = (ImageView) v
					.findViewById(R.id.card_info_more);
			tagData.mUiHolder.mText = (TextView) v
					.findViewById(R.id.card_title_name);
			tagData.mUiHolder.mText.setTypeface(FontUtil.Roboto_Medium);
			tagData.mUiHolder.mPlay = (ImageView) v
					.findViewById(R.id.card_preview_image);
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

		if (data.imageUrl == null) {
			tagData.mUiHolder.mPlay.setImageResource(data.resId);
		} else {
			// tagData.mUiHolder.mPlay.setImageUrl(data.imageUrl,MyVolley.getImageLoader());
		}

	}

	public View getChildWithValue(int value) {
		for (int i = 0; i < getChildCount(); i++) {
			// Log.d(TAG,
			// "child with id= "+getChildAt(i).getId()+" looking for "+value);
			if (getChildAt(i).getId() == value) {
				// Log.e(TAG, "getChildWithValue for "+value+ " is value");
				return getChildAt(i);
			}
		}
		Log.w(TAG, "getChildWithValue for " + value + " is null");
		return null;
	}

	private void verifyPositions() {
		// Log.e(TAG, "verifyPositions" );
		// firstView = getChildWithValue(mCurrentSelectedIndex);
		secondView = getChildWithValue(mCurrentSelectedIndex + 1);
		thirdView = getChildWithValue(mCurrentSelectedIndex + 2);
		fourthView = getChildWithValue(mCurrentSelectedIndex + 3);
		fifthView = getChildWithValue(mCurrentSelectedIndex + 4);
		sixthView = getChildWithValue(mCurrentSelectedIndex + 5);
		completeWithOutAnimate();
	}

	private void saveIntialPositions() {

		intialYPostion = currentYPosition;
		Log.w(TAG, "saveIntialPositions " + intialYPostion);
		firstView = null;
		secondView = null;
		thirdView = null;
		fourthView = null;
		fifthView = null;
		sixthView = null;
		// Log.e(TAG, "saveIntialPositions processing " +mCurrentSelectedIndex);

		if (movingDirection == SWIPEUP) {
			if (mCurrentSelectedIndex >= mDataList.size()) {
				return;
			}
		} else {
			if (mCurrentSelectedIndex <= 0) {
				verifyPositions();
				return;
			}
		}
		firstView = getChildWithValue(mCurrentSelectedIndex);
		secondView = getChildWithValue(mCurrentSelectedIndex + 1);
		thirdView = getChildWithValue(mCurrentSelectedIndex + 2);
		fourthView = getChildWithValue(mCurrentSelectedIndex + 3);
		fifthView = getChildWithValue(mCurrentSelectedIndex + 4);
		sixthView = getChildWithValue(mCurrentSelectedIndex + 5);
		completeWithOutAnimate();
		if (firstView != null) {
			firstViewLastPosition = firstView.getY();
		}
		if (secondView != null) {
			secondViewLastPosition = secondView.getY();
		}
		if (thirdView != null) {
			thirdViewLastPosition = thirdView.getY();
		}
		if (fourthView != null) {
			fourthViewLastPosition = fourthView.getY();
		}
		if (fifthView != null) {
			fifthViewLastPosition = fifthView.getY();
		}
		if (sixthView != null) {
			sixthViewLastPosition = sixthView.getY();
		}
		// Log.e(TAG, "saveIntialPositions end");
	}

	private void completeWithOutAnimate() {
		if (firstView != null) {
			firstView.setY(0);
		}
		if (secondView != null) {
			secondView.setY(secondItemStart);
		}
		if (thirdView != null) {
			thirdView.setY(thirdItemStart);
		}
		if (fourthView != null) {
			fourthView.setY(fourthItemStart);
		}
		if (fifthView != null) {
			fifthView.setY(fifthItemStart);
		}
		if (sixthView != null) {
			// sixthView.setY(sixthItemStart);
		}
	}

	private void updateViews() {
		if (mAnimationinProgress) {
			Log.e(TAG, "ignoring updateViews()");
			return;
		}
		distanceMoved = Math.abs(intialYPostion - currentYPosition);
		
		// Log.e(TAG, "updateViews start intialYPostion = " + intialYPostion
		// + " currentYPosition= " + currentYPosition);
		
		if(mFlingInAction){
			if (mCurrentVelocity > mTouchVelocityUnit
					&& mCurrentVelocity <= (2 * mTouchVelocityUnit)) {
				// Log.e(TAG, "updateViews speed low");
				distanceMoved = 10;
			} else if (mCurrentVelocity > (2 * mTouchVelocityUnit)
					&& mCurrentVelocity <= (3 * mTouchVelocityUnit)) {
				// Log.e(TAG, "updateViews speed medium low");
				distanceMoved = 20;
			}
		}
		Log.e(TAG, "updateViews  " + movingDirection +" distanceMoved "+distanceMoved);
		moveFirstView(firstView);
		if(moveSecondView(secondView)){
			moveThirdView(thirdView);
			moveFourthView(fourthView);
			moveFifthView(fifthView);
			moveSixthView(sixthView);
		}

		// Log.e(TAG, "updateViews end");
	}

	private void moveFirstView(View v) {
		if (v == null) {
			return;
		}
		float newposition = 0;
		boolean applymove = false;
		if (movingDirection == SWIPEUP) {
			// Log.e(TAG,
			// "moveFirstView swipe up old postion="+v.getY()+" distanceMoved ="+distanceMoved);
			newposition = v.getY() - distanceMoved;

		} else {

			double newCurrentYPosition = distanceMoved;
			// Log.e(TAG,
			// "moveFirstView old postion="+v.getY()+" distanceMoved ="+distanceMoved+" LastPosition="+firstViewLastPosition);
//			if (mFlingInAction) {
//				newposition = (float) (v.getY() + newCurrentYPosition);
//			} else {
				newposition = (float) (firstViewLastPosition + newCurrentYPosition);
//			}
			if (newposition < secondItemStart && mCurrentSelectedIndex >= 1) {
				applymove = true;
			}
			// Log.e(TAG,
			// "moveFirstView swipe down old postion="+v.getY()+" distanceMoved ="+distanceMoved);
		}
		if (applymove) {
			// Log.e(TAG,
			// "moveFirstView old postion="+v.getY()+" newposition ="+newposition);
			v.setY(newposition);
		}
	}

	private boolean moveSecondView(View v) {
		if (v == null) {
			return true;
		}
		float newposition = 0;
		boolean applymove = false;
		if (movingDirection == SWIPEUP) {
			 Log.e(TAG, "moveSecondView old postion="+v.getY()+" distanceMoved ="+distanceMoved);
//			if (mFlingInAction) {
//				newposition = v.getY() - distanceMoved;
//			} else {
				newposition = secondViewLastPosition - distanceMoved;
//			}
			if (newposition < 0) {
				newposition = 0;
			}
			if (v.getY() > firstItemStart) {
				applymove = true;
			} else {
				// completeMovement();
				movetoNext(true);
				saveIntialPositions();
				// Log.d(TAG, "moveSecondView new slot ");
				return false;
			}
		} else {
			double newCurrentYPosition = distanceMoved * secondRelativeUnitDown;
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (secondViewLastPosition + newCurrentYPosition);
			}
			if (newposition < thirdItemStart && mCurrentSelectedIndex >= 1) {
				applymove = true;
			} else {
				movetoNext(true);
				saveIntialPositions();
				// Log.d(TAG, "moveSecondView new swipe down slot ");
				return false;
			}
		}
		if (applymove) {
			// Log.e(TAG,
			// "moveSecondView old postion="+v.getY()+" newposition ="+newposition);
			v.setY(newposition);
		}
		return true;
	}

	private void moveThirdView(View v) {
		if (v == null) {
			return;
		}
		float newposition = 0;
		boolean applymove = false;

		if (movingDirection == SWIPEUP) {
			double newCurrentYPosition = distanceMoved * thirdRelativeUnitUp;
//			if (mFlingInAction) {
//				newposition = (float) (v.getY() - newCurrentYPosition);
//			} else {
				newposition = (float) (thirdViewLastPosition - newCurrentYPosition);
//			}
			if (newposition > secondItemStart) {
				applymove = true;
			}
		} else {
			double newCurrentYPosition = distanceMoved * thirdRelativeUnitDown;
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (thirdViewLastPosition + newCurrentYPosition);
			}
			if (newposition < fourthItemStart && mCurrentSelectedIndex >= 1) {
				applymove = true;
			} else {
				Log.e(TAG, " no third thirdViewLastPosition " + v.getY()
						+ " fourthItemStart=" + fourthItemStart
						+ " newposition= " + newposition);
			}
		}
		if (applymove) {
			// Log.e(TAG,
			// "moveThirdView old postion="+v.getY()+" newposition ="+newposition);
			v.setY(newposition);
		}
	}

	private void moveFourthView(View v) {
		if (v == null) {
			return;
		}
		float newposition = 0;
		boolean applymove = false;
		if (movingDirection == SWIPEUP) {
			double newCurrentYPosition = distanceMoved * fourthRelativeUnitUp;
//			if (mFlingInAction) {
//				newposition = (float) (v.getY() - newCurrentYPosition);
//			} else {
				newposition = (float) (fourthViewLastPosition - newCurrentYPosition);
//			}
			if (newposition > thirdItemStart) {
				applymove = true;
			}
		} else {
			double newCurrentYPosition = distanceMoved * fourthRelativeUnitDown;
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (fourthViewLastPosition + newCurrentYPosition);
			}

			if (newposition < fifthItemStart && mCurrentSelectedIndex >= 1) {
				applymove = true;
			} else {
				Log.e(TAG, " no movement for fourth view");
			}
		}
		if (applymove) {
			// Log.e(TAG,
			// "moveFourthView old postion="+v.getY()+" newposition ="+newposition);
			v.setY(newposition);
		}
	}

	private void moveFifthView(View v) {
		if (v == null) {
			return;
		}
		float newposition = 0;
		boolean applymove = false;
		if (movingDirection == SWIPEUP) {
			double newCurrentYPosition = distanceMoved * fifthRelativeUnitUp;
//			if (mFlingInAction) {
//				newposition = (float) (v.getY() - newCurrentYPosition);
//			} else {
				newposition = (float) (fifthViewLastPosition - newCurrentYPosition);
//			}
			if (newposition > fourthItemStart) {
				applymove = true;
			} else {
			}
		} else {
			double newCurrentYPosition = distanceMoved * fifthRelativeUnitDown;
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (fifthViewLastPosition + newCurrentYPosition);
			}

			if (newposition < fifthItemStart + mCardTitleHeight
					&& mCurrentSelectedIndex >= 1) {
				applymove = true;
			} else {
				Log.e(TAG, " %% no movement for fifth view");
			}
		}
		if (applymove) {
//			Log.e(TAG, "movement for fifth view");
			v.setY(newposition);
		}
	}

	private void moveSixthView(View v) {
		if (v == null) {
			return;
		}
		float newposition = 0;
		boolean applymove = false;
		if (movingDirection == SWIPEUP) {
			double newCurrentYPosition = distanceMoved * sixthRelativeUnitUp;
//			if (mFlingInAction) {
//				newposition = (float) (v.getY() - newCurrentYPosition);
//			} else {
				newposition = (float) (sixthViewLastPosition - newCurrentYPosition);
//			}
			if (newposition > fifthItemStart) {
				applymove = true;
			} else {
			}
		} else {
			double newCurrentYPosition = distanceMoved * sixthRelativeUnitDown;
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (sixthViewLastPosition + newCurrentYPosition);
			}

			if (newposition < sixthItemStart + mCardTitleHeight
					&& mCurrentSelectedIndex >= 1) {
				applymove = true;
			} else {
				Log.e(TAG, " %% no movement for sixth view");
			}
		}
		if (applymove) {
//			Log.e(TAG, "movement for sixth view");
			v.setY(newposition);
		}
	}

	// /**
	// * ID of the active pointer. This is used to retain consistency during
	// * drags/flings if multiple pointers are used.
	// */
	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	//
	// final int action = ev.getAction();
	// if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
	// return true;
	// }
	// switch (action & MotionEvent.ACTION_MASK) {
	// case MotionEvent.ACTION_MOVE: {
	// Log.e(TAG, "onInterceptTouchEvent MotionEvent.ACTION_MOVE");
	// /*
	// * mIsBeingDragged == false, otherwise the shortcut would have
	// * caught it. Check whether the user has moved far enough from his
	// * original down touch.
	// */
	//
	// /*
	// * Locally do absolute value. mLastMotionY is set to the y value of
	// * the down event.
	// */
	// final int activePointerId = mActivePointerId;
	// if (activePointerId == INVALID_POINTER) {
	// // If we don't have a valid id, the touch down wasn't on
	// // content.
	// break;
	// }
	//
	// final int pointerIndex = ev.findPointerIndex(activePointerId);
	// if (pointerIndex == -1) {
	// Log.e(TAG, "Invalid pointerId=" + activePointerId
	// + " in onInterceptTouchEvent");
	// break;
	// }
	//
	// final int y = (int) ev.getY(pointerIndex);
	// final float yDiff = Math.abs(y - mLastMotionY);
	// if (yDiff > mTouchSlop) {
	// mIsBeingDragged = true;
	// mLastMotionY = y;
	// initVelocityTrackerIfNotExists();
	// mVelocityTracker.addMovement(ev);
	//
	// final ViewParent parent = getParent();
	// if (parent != null) {
	// parent.requestDisallowInterceptTouchEvent(true);
	// }
	// }
	// break;
	// }
	//
	// case MotionEvent.ACTION_DOWN: {
	// Log.e(TAG, "onInterceptTouchEvent MotionEvent.ACTION_DOWN");
	// final int y = (int) ev.getY();
	// // if (!inChild((int) ev.getX(), (int) y)) {
	// // mIsBeingDragged = false;
	// // recycleVelocityTracker();
	// // break;
	// // }
	//
	// /*
	// * Remember location of down touch. ACTION_DOWN always refers to
	// * pointer index 0.
	// */
	// mLastMotionY = y;
	// mActivePointerId = ev.getPointerId(0);
	//
	// initOrResetVelocityTracker();
	// mVelocityTracker.addMovement(ev);
	// /*
	// * If being flinged and user touches the screen, initiate drag;
	// * otherwise don't. mScroller.isFinished should be false when being
	// * flinged.
	// */
	// mIsBeingDragged = !mScroller.isFinished();
	// setScrollParams(ev);
	// break;
	// }
	//
	// case MotionEvent.ACTION_CANCEL:
	// case MotionEvent.ACTION_UP:
	// Log.e(TAG, "onInterceptTouchEvent MotionEvent.ACTION_UP");
	// /* Release the drag */
	// mIsBeingDragged = false;
	// mActivePointerId = INVALID_POINTER;
	// recycleVelocityTracker();
	// // if (mScroller.springBack(mScrollX, mScrollY, 0, 0, 0,
	// // getScrollRange())) {
	// // postInvalidateOnAnimation();
	// // }
	// completeScroll(false);
	// break;
	// case MotionEvent.ACTION_POINTER_UP:
	// onSecondaryPointerUp(ev);
	// break;
	// }
	// return mIsBeingDragged;
	//
	// }
	//
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// // if (event.getAction() == MotionEvent.ACTION_DOWN &&
	// // event.getEdgeFlags() != 0) {
	// // // Don't handle edge touches immediately -- they may actually belong
	// // to one of our
	// // // descendants.
	// // return false;
	// // }
	//
	// if (mVelocityTracker == null) {
	// mVelocityTracker = VelocityTracker.obtain();
	// }
	// mVelocityTracker.addMovement(event);
	//
	// initVelocityTrackerIfNotExists();
	// mVelocityTracker.addMovement(event);
	//
	// final int action = event.getAction();
	// float y = event.getRawY();
	// switch (action & MotionEvent.ACTION_MASK) {
	// case MotionEvent.ACTION_DOWN:
	// Log.e(TAG, "onTouchEvent ACTION_DOWN");
	// if ((mIsBeingDragged = !mScroller.isFinished())) {
	// final ViewParent parent = getParent();
	// if (parent != null) {
	// parent.requestDisallowInterceptTouchEvent(true);
	// }
	// }
	// if (!mScroller.isFinished()) {
	// mScroller.abortAnimation();
	// }
	// setScrollParams(event);
	//
	// break;
	// case MotionEvent.ACTION_UP:
	// Log.e(TAG, "onTouchEvent ACTION_UP");
	// if (mIsBeingDragged) {
	// final VelocityTracker velocityTracker = mVelocityTracker;
	// velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
	// int initialVelocity = (int) velocityTracker
	// .getYVelocity(mActivePointerId);
	//
	// if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
	// fling(-initialVelocity, y);
	// }
	// mActivePointerId = INVALID_POINTER;
	// endDrag();
	// }
	// mIsBeingDragged = false;
	// if (!mFlingInAction) {
	// completeScroll(false);
	// }
	// break;
	// case MotionEvent.ACTION_MOVE:
	// Log.e(TAG, "onTouchEvent ACTION_MOVE");
	// final int activePointerIndex = event
	// .findPointerIndex(mActivePointerId);
	// if (activePointerIndex == INVALID_POINTER) {
	// Log.e(TAG, "Invalid pointerId=" + mActivePointerId
	// + " in onTouchEvent");
	// break;
	// }
	// Log.e(TAG, "onTouchEvent ACTION_MOVE ");
	// final int localy = (int) event.getY(activePointerIndex);
	// int deltaY = (int) (mLastMotionY - localy);
	// if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
	// final ViewParent parent = getParent();
	// if (parent != null) {
	// parent.requestDisallowInterceptTouchEvent(true);
	// }
	// mIsBeingDragged = true;
	// }
	// if (mIsBeingDragged) {
	// scroll(y);
	// }
	// break;
	// case MotionEvent.ACTION_CANCEL:
	// if (mIsBeingDragged && getChildCount() > 0) {
	// mActivePointerId = INVALID_POINTER;
	// endDrag();
	// }
	// break;
	// case MotionEvent.ACTION_POINTER_DOWN: {
	// final int index = event.getActionIndex();
	// mLastMotionY = (int) event.getY(index);
	// mActivePointerId = event.getPointerId(index);
	// break;
	// }
	// case MotionEvent.ACTION_POINTER_UP:
	// onSecondaryPointerUp(event);
	// if (mActivePointerId != INVALID_POINTER) {
	// mLastMotionY = (int) event.getY(event
	// .findPointerIndex(mActivePointerId));
	// }
	// break;
	// default:
	// break;
	// }
	// return true;
	// }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onMotionEvent will be called and we do the actual
		 * scrolling there.
		 */

		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
			// Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE ");
			return true;
		}

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			// Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE " + ev.getY());
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have
			 * caught it. Check whether the user has moved far enough from his
			 * original down touch.
			 */

			/*
			 * Locally do absolute value. mLastMotionY is set to the y value of
			 * the down event.
			 */
			final int activePointerId = mActivePointerId;
			if (activePointerId == INVALID_POINTER) {
				// If we don't have a valid id, the touch down wasn't on
				// content.
				break;
			}

			final int pointerIndex = ev.findPointerIndex(activePointerId);
			final float y = ev.getY(pointerIndex);
			final int yDiff = (int) Math.abs(y - mLastMotionY);
			if (yDiff > mTouchSlop) {
				mIsBeingDragged = true;
				mLastMotionY = y;
			}

			// scroll(ev.getY());
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			// Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN ");
			final float y = ev.getY();
			// if (!inChild((int) ev.getX(), (int) y)) {
			// mIsBeingDragged = false;
			// break;
			// }

			/*
			 * Remember location of down touch. ACTION_DOWN always refers to
			 * pointer index 0.
			 */
			mLastMotionY = y;
			mActivePointerId = ev.getPointerId(0);

			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			mIsBeingDragged = !mScroller.isFinished();
			findFocusedChild(ev);
			setScrollParams(ev);
			mAutoScroll = true;
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			/* Release the drag */
			// Log.e(TAG, "onInterceptTouchEvent ACTION_UP ");
			mIsBeingDragged = false;
			mActivePointerId = INVALID_POINTER;
			completeScroll(false);
			// Log.e(TAG, "onInterceptTouchEvent ACTION_UP ");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// Log.e(TAG, "onInterceptTouchEvent ACTION_POINTER_UP ");
			onSecondaryPointerUp(ev);
			break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			// Don't handle edge touches immediately -- they may actually belong
			// to one of our
			// descendants.
			return false;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			// Log.e(TAG, "onTouchEvent ACTION_DOWN ");
			final float y = ev.getY();
			// if (!(mIsBeingDragged = inChild((int) ev.getX(), (int) y))) {
			// return false;
			// }

			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionY = y;
			mActivePointerId = ev.getPointerId(0);
			findFocusedChild(ev);
			setScrollParams(ev);
			mAutoScroll = true;
			// Log.e(TAG, "onTouchEvent ACTION_DOWN ");
			break;
		}
		case MotionEvent.ACTION_MOVE:
			// Log.e(TAG, "onTouchEvent ACTION_MOVE "+ev.getY());
			if (mIsBeingDragged) {
				// Scroll to follow the motion event
				final int activePointerIndex = ev
						.findPointerIndex(mActivePointerId);
				final float y = ev.getY(activePointerIndex);
				final int deltaY = (int) (mLastMotionY - y);
				mLastMotionY = y;
				// scrollBy(0, deltaY);

			}
			currentYPosition = ev.getRawY();
			if (Math.abs(currentYPosition - intialYPostion) > mTouchSlop) {
				mAutoScroll = false;
			}
			// setScrollParams(ev);
			scroll(ev.getRawY());
			break;
		case MotionEvent.ACTION_UP:
			// Log.e(TAG, "onTouchEvent ACTION_UP ");
			boolean flingintiated = false;
			if (mIsBeingDragged) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) velocityTracker
						.getYVelocity(mActivePointerId);

				if (getChildCount() > 0
						&& Math.abs(initialVelocity) > mTouchVelocityUnit) {
					fling(-initialVelocity, ev.getY());
					flingintiated = true;
				}

				mActivePointerId = INVALID_POINTER;
				mIsBeingDragged = false;

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				// if (!mFlingInAction) {
				// completeScroll(false);
				// }
				// Log.e(TAG, "onTouchEvent ACTION_UP ");
			}
			if (!flingintiated) {
				completeScroll(false);
			}

			break;
		case MotionEvent.ACTION_CANCEL:
			// Log.e(TAG, "onTouchEvent ACTION_CANCEL ");
			if (mIsBeingDragged && getChildCount() > 0) {
				mActivePointerId = INVALID_POINTER;
				mIsBeingDragged = false;
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// Log.e(TAG, "onTouchEvent ACTION_POINTER_UP ");
			onSecondaryPointerUp(ev);
			break;
		}
		return true;
	}

	private int mNumberofViewsToMove = 0;
	private int mIndexofSelectedView = 0;

	private void findFocusedChild(MotionEvent motionEvent) {
		int[] listViewCoords = new int[2];
		getLocationOnScreen(listViewCoords);
		int y = (int) motionEvent.getRawY() - listViewCoords[1];
		if (y > 0 && y <= secondItemStart) {
			Log.v(TAG, "#firstitem selected");
			mNumberofViewsToMove = 0;
			mIndexofSelectedView = 1;
		} else if (y > secondItemStart && y <= thirdItemStart) {
			Log.v(TAG, "#secondItem selected");
			mNumberofViewsToMove = 0;
			mIndexofSelectedView = 2;
		} else if (y > thirdItemStart && y <= fourthItemStart) {
			Log.v(TAG, "#thirdItem selected");
			mNumberofViewsToMove = 1;
			mIndexofSelectedView = 3;
		} else if (y > fourthItemStart && y <= fifthItemStart) {
			Log.v(TAG, "#fourthItem selected");
			mNumberofViewsToMove = 2;
			mIndexofSelectedView = 4;
		} else if (y > fifthItemStart && y <= sixthItemStart) {
			Log.v(TAG, "#fifth selected");
			mNumberofViewsToMove = 3;
			mIndexofSelectedView = 5;
		}
	}

	private void endDrag() {
		mIsBeingDragged = false;
		recycleVelocityTracker();
	}

	/**
	 * Fling the scroll view
	 * 
	 * @param velocityY
	 *            The initial velocity in the Y direction. Positive numbers mean
	 *            that the finger/cursor is moving down the screen, which means
	 *            we want to scroll towards the top.
	 */

	public void fling(int velocityY, float y) {
		int speed = (int) Math.abs(velocityY);
		if (speed < mTouchVelocityUnit) {
			return;
		}
		int height = getHeight();
		Log.e(TAG, "fling before " + velocityY);
		mFlingInAction = true;
		mCurrentVelocity = velocityY;
		intialYPostion  = y;
		mScroller.fling(0, 0, 0, velocityY, 0, mScreenWidth, 0, mScreenHeight);//, 0, height / 2);
		invalidate();
	}

	private float mOldScrollY;

	@Override
	public void computeScroll() {
		 Log.e(TAG, "computeScroll start getCurrY() " +mScroller.getCurrY());
		float localCurrentY = mScroller.getCurrY();
		// Log.e(TAG, "computeScroll start " + localCurrentY);
		boolean more = mScroller.computeScrollOffset();
		if (mFlingInAction) {
			if (more) {
//				distanceMoved = Math.abs(mOldScrollY - localCurrentY);
//				mOldScrollY = localCurrentY;
//				if (mAnimationinProgress) {
//					Log.e(TAG, "ignoring updateViews()");
//					return;
//				}
//				currentYPosition = localCurrentY;
				mAutoScroll = false;
				scroll(localCurrentY);
				postInvalidate();
			} else {
				mFlingInAction = false;
				completeScroll(true);
			}
		}
	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionY = (int) ev.getY(newPointerIndex);
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	boolean directionApplied = false;

	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private boolean isScrolling = false;

	private void setScrollParams(MotionEvent event) {
		if (isScrolling) {
			Log.v(TAG, "setScrollParams fail");
			return;
		}

		isScrolling = true;
		float y = event.getRawY();

		intialYPostion = y;
		directionApplied = false;

		mLastMotionY = (int) y;

		currentYPosition = y;

		saveIntialPositions();
		// Log.w(TAG,
		// "setScrollParams "+intialYPostion+" currentYPosition ="+currentYPosition);
		mActivePointerId = event.getPointerId(0);
	}

	private void completeScroll(boolean fromfling) {
		Log.w(TAG, "completeScroll");
		mLastMotionY = 0;
		completeMovement(fromfling);
	}

	private boolean mAutoScroll = true;

	private void scroll(float y) {
		if (mAnimationinProgress) {
			Log.e(TAG, "ignoring updateViews()");
			return;
		}
		currentYPosition = y;
		if (!directionApplied) {
			if (currentYPosition > intialYPostion) {
				Log.e(TAG, " direction= SWIPEDOWN currentYPosition "
						+ currentYPosition + " intialYPostion "
						+ intialYPostion);
				movingDirection = SWIPEDOWN;
			} else {
				Log.e(TAG, " direction= SWIPEUP currentYPosition "
						+ currentYPosition + " intialYPostion "
						+ intialYPostion);
				movingDirection = SWIPEUP;
			}
			directionApplied = true;
		}
		updateViews();
	}

	private int mCurrentVelocity;
	private boolean mFlingInAction = false;
}
