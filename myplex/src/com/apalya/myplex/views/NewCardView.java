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
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
public class NewCardView extends FrameLayout {
    static final int ANIMATED_SCROLL_GAP = 250;

    static final float MAX_SCROLL_FACTOR = 0.5f;

    private Scroller mScroller;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionY;

    /**
     * True when the layout has changed but the traversal has not come through yet.
     * Ideally the view hierarchy would keep track of this for us.
     */
//    private boolean mIsLayoutDirty = true;


    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;


    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;


	private Context mContext;

    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    public NewCardView(Context context) {
    	super(context);
    }

    public NewCardView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public NewCardView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
        initScrollView();
    }

    private void initScrollView() {
    	Log.e(TAG, "4");
        mScroller = new Scroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private boolean inChild = true;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
        	Log.e(TAG, "11");
            return true;
        }
        if(mAnimationinProgress){
        	Log.e(TAG, "invalid event");
        	return false;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
            	Log.e(TAG, "onInterceptTouchEvent ACTION_MOVE()");
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                final float y = ev.getY(pointerIndex);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
            	Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN()");
                final float y = ev.getY();
                if (!inChild) {
                    mIsBeingDragged = false;
                    break;
                }

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);

                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */
                mIsBeingDragged = !mScroller.isFinished();
                findFocusedChild(ev);
                setScrollParams(ev);
               
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	Log.e(TAG, "onInterceptTouchEvent ACTION_UP()");
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                completeScroll(false);
                break;
            case MotionEvent.ACTION_POINTER_UP:
            	Log.e(TAG, "onInterceptTouchEvent ACTION_POINTER_UP()");
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
            // Don't handle edge touches immediately -- they may actually belong to one of our
        	Log.e(TAG, "12");
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
            	Log.e(TAG, "onTouchEvent ACTION_DOWN()");
                final float y = ev.getY();
                if (!(mIsBeingDragged = inChild)) {
                    return false;
                }
                
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
                break;
            }
            case MotionEvent.ACTION_MOVE:
//            	Log.e(TAG, "onTouchEvent ACTION_MOVE()");
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float y = ev.getY(activePointerIndex);
                    mLastMotionY = y;
                    currentYPosition = ev.getRawY();
        			if (Math.abs(currentYPosition - intialYPostion) > mTouchSlop) {
        				mAutoScroll = false;
        			}
//                    scrollBy(0, deltaY);
                    scroll(ev.getRawY());
                }
                break;
            case MotionEvent.ACTION_UP:
            	Log.e(TAG, "onTouchEvent ACTION_UP()");
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        fling(-initialVelocity,ev.getRawY());
                    }

                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    
                }
                if(!mFlingInAction){
                	completeScroll(false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
//            	Log.e(TAG, "onTouchEvent ACTION_CANCEL()");
                if (mIsBeingDragged ) {
                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
//            	Log.e(TAG, "onTouchEvent ACTION_POINTER_UP()");
                onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
    	Log.e(TAG, "13");
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

//    @Override
//    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
//            int parentHeightMeasureSpec, int heightUsed) {
//    	
//        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
//
//        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
//                mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
//                        + widthUsed, lp.width);
//        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
//                lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
//
//        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//    }

    int oldScrollY;
    @Override
    public void computeScroll() {
    	if(!mFlingInAction){
    		return;
    	}
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            
            distanceMoved = Math.abs(oldScrollY - y);
            oldScrollY = y;
            if (mAnimationinProgress) {
    			Log.e(TAG, "ignoring computeScroll()");
    			return;
    		}
    		moveFirstView(firstView);
    		if(moveSecondView(secondView)){
    			moveThirdView(thirdView);
    			moveFourthView(fourthView);
    			moveFifthView(fifthView);
    			moveSixthView(sixthView);
    		}
            postInvalidate();
        }else{
        	oldScrollY = 0;
        	mFlingInAction = false;
        	completeScroll(true);
        }
    }


//    @Override
//    public void requestLayout() {
//    	Log.e(TAG, "33");
//        mIsLayoutDirty = true;
//        super.requestLayout();
//    }


    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY,float currentY) {
    	
    	if (mAnimationinProgress) {
			Log.e(TAG, "ignoring fling()");
			return;
		}
    	final boolean movingDown = velocityY > 0;
		if (!directionApplied) {
			if (!movingDown) {
				Log.e(TAG, "fling direction= SWIPEDOWN currentYPosition "
						+ currentYPosition + " intialYPostion "
						+ intialYPostion);
				movingDirection = SWIPEDOWN;
			} else {
				Log.e(TAG, "fling direction= SWIPEUP currentYPosition "
						+ currentYPosition + " intialYPostion "
						+ intialYPostion);
				movingDirection = SWIPEUP;
			}
			directionApplied = true;
		}
		mFlingInAction = true;
		mAutoScroll = false;
		if(movingDown){
			oldScrollY = 0;
			Log.e(TAG, " fling() "+velocityY+" moving up");
        	 mScroller.fling(0, 0, 0, velocityY, 0, 0, 0, 2*mScreenHeight);//, 0, height / 2);
        }else{
        	oldScrollY = 2*mScreenHeight;
        	Log.e(TAG, " fling() "+velocityY+" moving down");
        	 mScroller.fling(0, 2*mScreenHeight, 0, velocityY, 0, 0, 0,2*mScreenHeight);//, 0, height / 2);
        }
		invalidate();
    }
    
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
		initScrollView();

	}

	private float getCardLayoutHeight() {
		return getResources().getDimension(R.dimen.cardtitleheight)
				+ getResources().getDimension(R.dimen.cardplayheight)
				+ getResources().getDimension(R.dimen.cardstatusheight)
				+ getResources().getDimension(R.dimen.cardmoreinfoheight);
	}
	private void prepareViewPositions() {
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
		
		Log.d(TAG,"firstviewpos ="+firstItemStart+" secondviewpos ="+secondItemStart+" thirdviewpos ="+thirdItemStart+" fourthviewpos ="+fourthItemStart+" fifthviewpos ="+fifthItemStart+" sixthviewpos ="+sixthItemStart);

	}

	private float getGap(int index) {
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
			View v = getChildAt(getChildCount() - 1);
			gap = v.getY() + mCardTitleHeight;
			break;
		}
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
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		if(result > 100){
			result = 48;
		}
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
		saveIntialPositions();
		isScrolling = false;
		directionApplied = false;
		if (mNumberofViewsToMove > 0 && mAutoScroll) {
			mNumberofViewsToMove--;
			completeScroll(false);
		} else {
			mAutoScroll = false;
		}
		Log.v(TAG, "sendDelayedEvent complete" );
	}
	private void sendDelayedCallBack() {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				if (mOnLoadMoreListener != null && mCurrentSelectedIndex > mNumberofItems/2) {
					mOnLoadMoreListener.loadmore(mNumberofItems);
				}
			}
		});
	}
	private void movetoNext(boolean direct) {
		if (getChildCount() == 0) {
			return;
		}
		if (movingDirection == SWIPEUP) {
//			Log.e(TAG,"movenext ");
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
				(int) mContext.getResources().getDimension(R.dimen.cardHeight));
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
			tagData.mUiHolder.mInfo = (LinearLayout) v
					.findViewById(R.id.card_info_more);
			tagData.mUiHolder.mText = (TextView) v
					.findViewById(R.id.card_title_name);
			tagData.mUiHolder.mText.setTypeface(FontUtil.Roboto_Medium);
			tagData.mUiHolder.mPlay = (NetworkImageView) v
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

		
//		Random rnd = new Random();
//		int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(128),
//				rnd.nextInt(64));
//		tagData.mUiHolder.mPlay.setBackgroundColor(color);
		if (data.imageUrl == null) {
			tagData.mUiHolder.mPlay.setImageResource(data.resId);
		} else if (data.imageUrl != null){
			 tagData.mUiHolder.mPlay.setImageUrl(data.imageUrl,MyVolley.getImageLoader());
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
//		Log.w(TAG, "getChildWithValue for " + value + " is null");
		return null;
	}

	private void verifyPositions() {
		 Log.e(TAG, "verifyPositions" );
		// firstView = getChildWithValue(mCurrentSelectedIndex);
		secondView = getChildWithValue(mCurrentSelectedIndex + 1);
		thirdView = getChildWithValue(mCurrentSelectedIndex + 2);
		fourthView = getChildWithValue(mCurrentSelectedIndex + 3);
		fifthView = getChildWithValue(mCurrentSelectedIndex + 4);
		sixthView = getChildWithValue(mCurrentSelectedIndex + 5);
		completeWithOutAnimate();
		sqeezeAnimation();
	}
	private void sqeezeAnimation(){
//		AnimatorSet set = new AnimatorSet();
//		set.play(ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0, 100)).with(ObjectAnimator.ofFloat(this, View.TRANSLATION_Y,
//                110, 0));
//		set.setDuration(500);
//		set.setInterpolator(new DecelerateInterpolator());
//		set.start();
	}
	private void saveIntialPositions() {
		
		intialYPostion = currentYPosition;
		firstView = null;
		secondView = null;
		thirdView = null;
		fourthView = null;
		fifthView = null;
		sixthView = null;

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
		Log.e(TAG, "saveIntialPositions" );
		firstView = getChildWithValue(mCurrentSelectedIndex);
		secondView = getChildWithValue(mCurrentSelectedIndex + 1);
		thirdView = getChildWithValue(mCurrentSelectedIndex + 2);
		fourthView = getChildWithValue(mCurrentSelectedIndex + 3);
		fifthView = getChildWithValue(mCurrentSelectedIndex + 4);
		sixthView = getChildWithValue(mCurrentSelectedIndex + 5);
//		completeWithOutAnimate();
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
	}

	private void completeWithOutAnimate() {
		if (mAnimationinProgress) {
			Log.e(TAG, "ignoring completeWithOutAnimate()");
			return;
		}
		Log.e(TAG, "continu completeWithOutAnimate" );
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
			if (mFlingInAction) {
				newposition = (float) (v.getY() + newCurrentYPosition);
			} else {
				newposition = (float) (firstViewLastPosition + newCurrentYPosition);
			}
			if (newposition < secondItemStart && mCurrentSelectedIndex >= 1) {
				applymove = true;
			}
			// Log.e(TAG,
			// "moveFirstView swipe down old postion="+v.getY()+" distanceMoved ="+distanceMoved);
		}
		if (applymove) {
//			 Log.w(TAG, "moveFirstView old postion="+v.getY()+" newposition ="+newposition);
			v.setY(newposition);
		}
	}

	private boolean moveSecondView(View v) {
		if (v == null) {
			return true;
		}
		float newposition = 0;
		boolean applymove = false;
//		Log.e(TAG, "moveSecondView old postion="+v.getY()+" distanceMoved ="+distanceMoved);
		if (movingDirection == SWIPEUP) {
			 
			if (mFlingInAction) {
				newposition = v.getY() - distanceMoved;
			} else {
				newposition = secondViewLastPosition - distanceMoved;
			}
			if (newposition < 0) {
				newposition = 0;
			}
			if (v.getY() > firstItemStart) {
				applymove = true;
			} else {
				// completeMovement();
				movetoNext(true);
				saveIntialPositions();
//				 Log.d(TAG, "moveSecondView new slot ");
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
//				 Log.d(TAG, "moveSecondView new swipe down slot ");
				return false;
			}
		}
		if (applymove) {
//			 Log.d(TAG,	 "moveSecondView old postion="+v.getY()+" newposition ="+newposition);
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
//		Log.e(TAG, "moveThirdView old postion="+v.getY()+" distanceMoved ="+distanceMoved);
		if (movingDirection == SWIPEUP) {
			double newCurrentYPosition = (distanceMoved * thirdRelativeUnitUp);
			if (mFlingInAction) {
				newposition = (float) (v.getY() - newCurrentYPosition);
			} else {
				newposition = (float) (thirdViewLastPosition - newCurrentYPosition);
			}
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
			if(mCurrentSelectedIndex >= 1){
				if (newposition < fourthItemStart) {
					applymove = true;
				} else {
					newposition = fourthItemStart;
					applymove = true;
					Log.e(TAG, " no third thirdViewLastPosition " + v.getY()
							+ " fourthItemStart=" + fourthItemStart
							+ " newposition= " + newposition);
				}
			}
			
		}
		if (applymove) {
//			 Log.e(TAG, "moveThirdView old postion="+v.getY()+" newposition ="+newposition);
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
			if (mFlingInAction) {
				newposition = (float) (v.getY() - newCurrentYPosition);
			} else {
				newposition = (float) (fourthViewLastPosition - newCurrentYPosition);
			}
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
			if(mCurrentSelectedIndex >= 1){
				if (newposition < fifthItemStart) {
					applymove = true;
				} else {
					newposition = fifthItemStart;
					applymove = true;
					Log.e(TAG, " no movement for fourth view");
				}
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
			if (mFlingInAction) {
				newposition = (float) (v.getY() - newCurrentYPosition);
			} else {
				newposition = (float) (fifthViewLastPosition - newCurrentYPosition);
			}
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
			if(mCurrentSelectedIndex >= 1){
				if (newposition < sixthItemStart) {
					applymove = true;
				} else {
					newposition = sixthItemStart;
					applymove = true;
					Log.e(TAG, " %% no movement for fifth view");
				}
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
			if (mFlingInAction) {
				newposition = (float) (v.getY() - newCurrentYPosition);
			} else {
				newposition = (float) (sixthViewLastPosition - newCurrentYPosition);
			}
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
			if(mCurrentSelectedIndex >= 1){
				if (newposition < sixthItemStart + mCardTitleHeight ) {
					applymove = true;
				} else {
					newposition = sixthItemStart + mCardTitleHeight;
					applymove = true;
					Log.e(TAG, " %% no movement for sixth view");
				}
			}
		}
		if (applymove) {
//			Log.e(TAG, "movement for sixth view");
			v.setY(newposition);
		}
	}

	private int mNumberofViewsToMove = 0;
	private int mIndexofSelectedView = 0;

	private void findFocusedChild(MotionEvent motionEvent) {
		 mAutoScroll = true;
		int[] listViewCoords = new int[2];
		getLocationOnScreen(listViewCoords);
		int y = (int) motionEvent.getRawY() - listViewCoords[1];
		if (y > 0 && y <= secondItemStart) {
//			Log.v(TAG, "#firstitem selected");
			mAutoScroll = false;
			mNumberofViewsToMove = 0;
			mIndexofSelectedView = 1;
		} else if (y > secondItemStart && y <= thirdItemStart) {
//			Log.v(TAG, "#secondItem selected");
			mNumberofViewsToMove = 0;
			mIndexofSelectedView = 2;
		} else if (y > thirdItemStart && y <= fourthItemStart) {
//			Log.v(TAG, "#thirdItem selected");
			mNumberofViewsToMove = 1;
			mIndexofSelectedView = 3;
		} else if (y > fourthItemStart && y <= fifthItemStart) {
//			Log.v(TAG, "#fourthItem selected");
			mNumberofViewsToMove = 2;
			mIndexofSelectedView = 4;
		} else if (y > fifthItemStart && y <= sixthItemStart) {
//			Log.v(TAG, "#fifth selected");
			mNumberofViewsToMove = 3;
			mIndexofSelectedView = 5;
		}
	}
	private void setScrollParams(MotionEvent event) {
		Log.v(TAG, "setScrollParams start");
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
//		Log.w(TAG, "completeScroll");
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
				Log.e(TAG,"scroll SWIPEDOWN");
				movingDirection = SWIPEDOWN;
			} else {
				Log.e(TAG,"scroll SWIPEUP");
				movingDirection = SWIPEUP;
			}
			directionApplied = true;
		}
		updateViews();
	}

	private int mCurrentVelocity;
	private boolean mFlingInAction = false;
	boolean directionApplied = false;

	private boolean isScrolling = false;

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
	public interface OnLoadMoreListener {
		public void loadmore(int value);
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
		public void play(CardData object);
	}

	// CallBack Listeners

	private OnClickListener mDeleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
//			mAutoScroll = false;
			Log.e(TAG, "mDeleteListener onClick");
//			deleteCard(v);
		}
	};
	private OnClickListener mFavListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
//			mAutoScroll = false;
			if (mOnFavouriteListener != null)
				mOnFavouriteListener.addFavourite(v.getId());
		}
	};
	private OnClickListener mPlayListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (mOnPlayListener != null){
				Log.e("pref","Item Clicked");
				CardData data = (CardData) v.getTag();
				if(data == null ){
					Log.e("pref","Item Clicked fail");
					return;
				}
				if(mCurrentSelectedIndex != mDataList.indexOf(data)){
					Log.e("pref","Item Clicked fail 1");
					return;
				}
				if(mIndexofSelectedView != 1){
					Log.e("pref","Item Clicked fail 2");
					return;
				}
				mAutoScroll = false;
				mOnPlayListener.play(data);
			}
		}
	};
	private OnClickListener mPurchaseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
//			mAutoScroll = false;
			if (mOnPurchaseListener != null)
				mOnPurchaseListener.purchase(v.getId());
		}
	};
	private OnClickListener mMoreInfoListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnPlayListener != null){
				Log.e("pref","Item Clicked");
				CardData data = (CardData) v.getTag();
				if(data == null){
					Log.e("pref","Item Clicked fail");
					return;
				}
				if(mCurrentSelectedIndex != mDataList.indexOf(data)){
					Log.e("pref","Item Clicked fail 1");
					return;
				}
				if(mIndexofSelectedView != 1){
					Log.e("pref","Item Clicked fail 2");
					return;
				}
				mAutoScroll = false;
				mOnPlayListener.play(data);
			}
		}
	};
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
    private String TAG = "CardView";

	private List<CardData> mDataList = new ArrayList<CardData>();

	private int mNumberofItems = 0;

	private int mCurrentSelectedIndex = 0;

	private int mShortAnimationDuration;

	private float mCardHeight = 0;

	private float mCardTitleHeight = 0;

	private int mScreenHeight = 0;

	private int mActionBarHeight = 0;

	private int mScreenWidth = 0;

	private int mNumberofViewInMemory = 6;

	private int mLastItemAdded;

	private LayoutInflater mInflator;

	private int availableScreenSize;

	private double thirdRelativeUnitUp;
	private double fourthRelativeUnitUp;
	private double fifthRelativeUnitUp;
	private double sixthRelativeUnitUp;

	private double secondRelativeUnitDown;
	private double thirdRelativeUnitDown;
	private double fourthRelativeUnitDown;
	private double fifthRelativeUnitDown;
	private double sixthRelativeUnitDown;

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

	private int mTouchVelocityUnit;

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

	private static final int MINIMUMVIEWS = 2;

	private OnLoadMoreListener mOnLoadMoreListener;
	private OnFavouriteListener mOnFavouriteListener;
	private OnPlayListener mOnPlayListener;
	private OnDeleteListener mOnDeleteListener;
	private OnMoreInfoListener mOnMoreInfoListener;
	private OnPurchaseListener mOnPurchaseListener;
	private OnItemSelectedListener mOnItemSelectedListener;
}

