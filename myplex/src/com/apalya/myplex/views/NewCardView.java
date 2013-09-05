package com.apalya.myplex.views;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.views.CardView.OnDeleteListener;
import com.apalya.myplex.views.CardView.OnFavouriteListener;
import com.apalya.myplex.views.CardView.OnItemSelectedListener;
import com.apalya.myplex.views.CardView.OnLoadMoreListener;
import com.apalya.myplex.views.CardView.OnMoreInfoListener;
import com.apalya.myplex.views.CardView.OnPlayListener;
import com.apalya.myplex.views.CardView.OnPurchaseListener;

/**
 * Layout container for a view hierarchy that can be scrolled by the user,
 * allowing it to be larger than the physical display.  A ScrollView
 * is a {@link FrameLayout}, meaning you should place one child in it
 * containing the entire contents to scroll; this child may itself be a layout
 * manager with a complex hierarchy of objects.  A child that is often used
 * is a {@link LinearLayout} in a vertical orientation, presenting a vertical
 * array of top-level items that the user can scroll through.
 *
 * <p>The {@link TextView} class also
 * takes care of its own scrolling, so does not require a ScrollView, but
 * using the two together is possible to achieve the effect of a text view
 * within a larger container.
 * 
 * <p>ScrollView only supports vertical scrolling.
 */
public class NewCardView extends FrameLayout {
    static final int ANIMATED_SCROLL_GAP = 250;

    static final float MAX_SCROLL_FACTOR = 0.5f;


    private long mLastScroll;

    private final Rect mTempRect = new Rect();
    private Scroller mScroller;

    /**
     * Flag to indicate that we are moving focus ourselves. This is so the
     * code that watches for focus changes initiated outside this ScrollView
     * knows that it does not have to do anything.
     */
    private boolean mScrollViewMovedFocus;

    /**
     * Position of the last motion event.
     */
    private float mLastMotionY;

    /**
     * True when the layout has changed but the traversal has not come through yet.
     * Ideally the view hierarchy would keep track of this for us.
     */
    private boolean mIsLayoutDirty = true;

    /**
     * The child to give focus to in the event that a child has requested focus while the
     * layout is dirty. This prevents the scroll from being wrong if the child has not been
     * laid out before requesting focus.
     */
    private View mChildToScrollTo = null;

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

    /**
     * When set to true, the scroll view measure its child to make it fill the currently
     * visible area.
     */
    private boolean mFillViewport;

    /**
     * Whether arrow scrolling is animated.
     */
    private boolean mSmoothScrollingEnabled = true;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

	private int mScrollY;

	private int mPaddingBottom;

	private int mBottom;

	private int mTop;
	private Context mContext;

	private int mPaddingTop;

	private int mPaddingRight;

	private int mPaddingLeft;

	private int mScrollX;
    
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
    @Override
    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        if (mScrollY < length) {
            return mScrollY / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int bottomEdge = getHeight() - mPaddingBottom;
        final int span = getChildAt(0).getBottom() - mScrollY - bottomEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    /**
     * @return The maximum amount this scroll view will scroll in response to
     *   an arrow event.
     */
    public int getMaxScrollAmount() {
        return (int) (MAX_SCROLL_FACTOR * (mBottom - mTop));
    }


    private void initScrollView() {
        mScroller = new Scroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

   

    /**
     * @return Returns true this ScrollView can be scrolled
     */
    private boolean canScroll() {
        return true;
    }

    /**
     * Indicates whether this ScrollView's content is stretched to fill the viewport.
     *
     * @return True if the content fills the viewport, false otherwise.
     */
    public boolean isFillViewport() {
        return mFillViewport;
    }

    /**
     * Indicates this ScrollView whether it should stretch its content height to fill
     * the viewport or not.
     *
     * @param fillViewport True to stretch the content's height to the viewport's
     *        boundaries, false otherwise.
     */
    public void setFillViewport(boolean fillViewport) {
        if (fillViewport != mFillViewport) {
            mFillViewport = fillViewport;
            requestLayout();
        }
    }

    /**
     * @return Whether arrow scrolling will animate its transition.
     */
    public boolean isSmoothScrollingEnabled() {
        return mSmoothScrollingEnabled;
    }

    /**
     * Set whether arrow scrolling will animate its transition.
     * @param smoothScrollingEnabled whether arrow scrolling will animate its transition
     */
    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        if (!mFillViewport) {
//            return;
//        }
//
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (heightMode == MeasureSpec.UNSPECIFIED) {
//            return;
//        }
//
//        if (getChildCount() > 0) {
//            final View child = getChildAt(0);
//            int height = getMeasuredHeight();
//            if (child.getMeasuredHeight() < height) {
//                final FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
//    
//                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, mPaddingLeft
//                        + mPaddingRight, lp.width);
//                height -= mPaddingTop;
//                height -= mPaddingBottom;
//                int childHeightMeasureSpec =
//                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
//    
//                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//            }
//        }
//    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        // Let the focused view and/or our descendants get the key first
//        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
//    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
//    public boolean executeKeyEvent(KeyEvent event) {
//        mTempRect.setEmpty();
//
//        if (!canScroll()) {
//            if (isFocused() && event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
//                View currentFocused = findFocus();
//                if (currentFocused == this) currentFocused = null;
//                View nextFocused = FocusFinder.getInstance().findNextFocus(this,
//                        currentFocused, View.FOCUS_DOWN);
//                return nextFocused != null
//                        && nextFocused != this
//                        && nextFocused.requestFocus(View.FOCUS_DOWN);
//            }
//            return false;
//        }
//
//        boolean handled = false;
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            switch (event.getKeyCode()) {
//                case KeyEvent.KEYCODE_DPAD_UP:
//                    if (!event.isAltPressed()) {
//                        handled = arrowScroll(View.FOCUS_UP);
//                    } else {
//                        handled = fullScroll(View.FOCUS_UP);
//                    }
//                    break;
//                case KeyEvent.KEYCODE_DPAD_DOWN:
//                    if (!event.isAltPressed()) {
//                        handled = arrowScroll(View.FOCUS_DOWN);
//                    } else {
//                        handled = fullScroll(View.FOCUS_DOWN);
//                    }
//                    break;
//                case KeyEvent.KEYCODE_SPACE:
//                    pageScroll(event.isShiftPressed() ? View.FOCUS_UP : View.FOCUS_DOWN);
//                    break;
//            }
//        }
//
//        return handled;
//    }

    private boolean inChild(int x, int y) {
//        if (getChildCount() > 0) {
//            final int scrollY = mScrollY;
//            final View child = getChildAt(0);
//            return !(y < child.getTop() - scrollY
//                    || y >= child.getBottom() - scrollY
//                    || x < child.getLeft()
//                    || x >= child.getRight());
//        }
        return true;
    }

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
            return true;
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
                if (!inChild((int) ev.getX(), (int) y)) {
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
                if (!(mIsBeingDragged = inChild((int) ev.getX(), (int) y))) {
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
            	Log.e(TAG, "onTouchEvent ACTION_MOVE()");
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float y = ev.getY(activePointerIndex);
                    final int deltaY = (int) (mLastMotionY - y);
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
            	Log.e(TAG, "onTouchEvent ACTION_CANCEL()");
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
            	Log.e(TAG, "onTouchEvent ACTION_POINTER_UP()");
                onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
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
    
    private int getScrollRange() {
        int scrollRange = 0;
        scrollRange = Math.max(0,getHeight() - mPaddingBottom - mPaddingTop);
        return scrollRange;
    }

    /**
     * <p>
     * Finds the next focusable component that fits in this View's bounds
     * (excluding fading edges) pretending that this View's top is located at
     * the parameter top.
     * </p>
     *
     * @param topFocus           look for a candidate is the one at the top of the bounds
     *                           if topFocus is true, or at the bottom of the bounds if topFocus is
     *                           false
     * @param top                the top offset of the bounds in which a focusable must be
     *                           found (the fading edge is assumed to start at this position)
     * @param preferredFocusable the View that has highest priority and will be
     *                           returned if it is within my bounds (null is valid)
     * @return the next focusable component in the bounds or null if none can be
     *         found
     */
    private View findFocusableViewInMyBounds(final boolean topFocus,
            final int top, View preferredFocusable) {
        /*
         * The fading edge's transparent side should be considered for focus
         * since it's mostly visible, so we divide the actual fading edge length
         * by 2.
         */
        final int fadingEdgeLength = getVerticalFadingEdgeLength() / 2;
        final int topWithoutFadingEdge = top + fadingEdgeLength;
        final int bottomWithoutFadingEdge = top + getHeight() - fadingEdgeLength;

        if ((preferredFocusable != null)
                && (preferredFocusable.getTop() < bottomWithoutFadingEdge)
                && (preferredFocusable.getBottom() > topWithoutFadingEdge)) {
            return preferredFocusable;
        }

        return findFocusableViewInBounds(topFocus, topWithoutFadingEdge,
                bottomWithoutFadingEdge);
    }

    /**
     * <p>
     * Finds the next focusable component that fits in the specified bounds.
     * </p>
     *
     * @param topFocus look for a candidate is the one at the top of the bounds
     *                 if topFocus is true, or at the bottom of the bounds if topFocus is
     *                 false
     * @param top      the top offset of the bounds in which a focusable must be
     *                 found
     * @param bottom   the bottom offset of the bounds in which a focusable must
     *                 be found
     * @return the next focusable component in the bounds or null if none can
     *         be found
     */
    private View findFocusableViewInBounds(boolean topFocus, int top, int bottom) {

        List<View> focusables = getFocusables(View.FOCUS_FORWARD);
        View focusCandidate = null;

        /*
         * A fully contained focusable is one where its top is below the bound's
         * top, and its bottom is above the bound's bottom. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
        boolean foundFullyContainedFocusable = false;

        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewTop = view.getTop();
            int viewBottom = view.getBottom();

            if (top < viewBottom && viewTop < bottom) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */

                final boolean viewIsFullyContained = (top < viewTop) &&
                        (viewBottom < bottom);

                if (focusCandidate == null) {
                    /* No candidate, take this one */
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    final boolean viewIsCloserToBoundary =
                            (topFocus && viewTop < focusCandidate.getTop()) ||
                                    (!topFocus && viewBottom > focusCandidate
                                            .getBottom());

                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            /*
                             * We're dealing with only fully contained views, so
                             * it has to be closer to the boundary to beat our
                             * candidate
                             */
                            focusCandidate = view;
                        }
                    } else {
                        if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
                            focusCandidate = view;
                            foundFullyContainedFocusable = true;
                        } else if (viewIsCloserToBoundary) {
                            /*
                             * Partially contained view beats another partially
                             * contained view if it's closer
                             */
                            focusCandidate = view;
                        }
                    }
                }
            }
        }

        return focusCandidate;
    }

    /**
     * <p>Handles scrolling in response to a "page up/down" shortcut press. This
     * method will scroll the view by one page up or down and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.</p>
     *
     * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
     *                  to go one page up or
     *                  {@link android.view.View#FOCUS_DOWN} to go one page down
     * @return true if the key event is consumed by this method, false otherwise
     */
//    public boolean pageScroll(int direction) {
//        boolean down = direction == View.FOCUS_DOWN;
//        int height = getHeight();
//
//        if (down) {
//            mTempRect.top = getScrollY() + height;
//            int count = getChildCount();
//            if (count > 0) {
//                View view = getChildAt(count - 1);
//                if (mTempRect.top + height > view.getBottom()) {
//                    mTempRect.top = view.getBottom() - height;
//                }
//            }
//        } else {
//            mTempRect.top = getScrollY() - height;
//            if (mTempRect.top < 0) {
//                mTempRect.top = 0;
//            }
//        }
//        mTempRect.bottom = mTempRect.top + height;
//
//        return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom);
//    }

    /**
     * <p>Handles scrolling in response to a "home/end" shortcut press. This
     * method will scroll the view to the top or bottom and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.</p>
     *
     * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
     *                  to go the top of the view or
     *                  {@link android.view.View#FOCUS_DOWN} to go the bottom
     * @return true if the key event is consumed by this method, false otherwise
     */
//    public boolean fullScroll(int direction) {
//        boolean down = direction == View.FOCUS_DOWN;
//        int height = getHeight();
//
//        mTempRect.top = 0;
//        mTempRect.bottom = height;
//
//        if (down) {
//            int count = getChildCount();
//            if (count > 0) {
//                View view = getChildAt(count - 1);
//                mTempRect.bottom = view.getBottom();
//                mTempRect.top = mTempRect.bottom - height;
//            }
//        }
//
//        return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom);
//    }

    /**
     * <p>Scrolls the view to make the area defined by <code>top</code> and
     * <code>bottom</code> visible. This method attempts to give the focus
     * to a component visible in this area. If no component can be focused in
     * the new visible area, the focus is reclaimed by this scrollview.</p>
     *
     * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
     *                  to go upward
     *                  {@link android.view.View#FOCUS_DOWN} to downward
     * @param top       the top offset of the new area to be made visible
     * @param bottom    the bottom offset of the new area to be made visible
     * @return true if the key event is consumed by this method, false otherwise
     */
    private boolean scrollAndFocus(int direction, int top, int bottom) {
    	Log.w(TAG, "scrollAndFocus");
        boolean handled = true;

        int height = getHeight();
        int containerTop = getScrollY();
        int containerBottom = containerTop + height;
        boolean up = direction == View.FOCUS_UP;

        View newFocused = findFocusableViewInBounds(up, top, bottom);
        if (newFocused == null) {
            newFocused = this;
        }

        if (top >= containerTop && bottom <= containerBottom) {
            handled = false;
        } else {
            int delta = up ? (top - containerTop) : (bottom - containerBottom);
            doScrollY(delta);
        }

        if (newFocused != findFocus() && newFocused.requestFocus(direction)) {
            mScrollViewMovedFocus = true;
            mScrollViewMovedFocus = false;
        }

        return handled;
    }

    /**
     * Handle scrolling in response to an up or down arrow click.
     *
     * @param direction The direction corresponding to the arrow key that was
     *                  pressed
     * @return True if we consumed the event, false otherwise
     */
//    public boolean arrowScroll(int direction) {
//
//        View currentFocused = findFocus();
//        if (currentFocused == this) currentFocused = null;
//
//        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
//
//        final int maxJump = getMaxScrollAmount();
//
//        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump, getHeight())) {
//            nextFocused.getDrawingRect(mTempRect);
//            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
//            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
//            doScrollY(scrollDelta);
//            nextFocused.requestFocus(direction);
//        } else {
//            // no new focus
//            int scrollDelta = maxJump;
//
//            if (direction == View.FOCUS_UP && getScrollY() < scrollDelta) {
//                scrollDelta = getScrollY();
//            } else if (direction == View.FOCUS_DOWN) {
//                if (getChildCount() > 0) {
//                    int daBottom = getChildAt(0).getBottom();
//    
//                    int screenBottom = getScrollY() + getHeight();
//    
//                    if (daBottom - screenBottom < maxJump) {
//                        scrollDelta = daBottom - screenBottom;
//                    }
//                }
//            }
//            if (scrollDelta == 0) {
//                return false;
//            }
//            doScrollY(direction == View.FOCUS_DOWN ? scrollDelta : -scrollDelta);
//        }
//
//        if (currentFocused != null && currentFocused.isFocused()
//                && isOffScreen(currentFocused)) {
//            // previously focused item still has focus and is off screen, give
//            // it up (take it back to ourselves)
//            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
//            // sure to
//            // get it)
//            final int descendantFocusability = getDescendantFocusability();  // save
//            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//            requestFocus();
//            setDescendantFocusability(descendantFocusability);  // restore
//        }
//        return true;
//    }

    /**
     * @return whether the descendant of this scroll view is scrolled off
     *  screen.
     */
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0, getHeight());
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     *  pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.bottom + delta) >= getScrollY()
                && (mTempRect.top - delta) <= (getScrollY() + height);
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param delta the number of pixels to scroll by on the Y axis
     */
    private void doScrollY(int delta) {
    	Log.w(TAG, "doScrollY");
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(0, delta);
            } else {
                scrollBy(0, delta);
            }
        }
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param dx the number of pixels to scroll by on the X axis
     * @param dy the number of pixels to scroll by on the Y axis
     */
    public final void smoothScrollBy(int dx, int dy) {
    	Log.w(TAG, "smoothScrollBy");
        if (getChildCount() == 0) {
            // Nothing to do.
            return;
        }
        long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
        if (duration > ANIMATED_SCROLL_GAP) {
            final int height = getHeight() - mPaddingBottom - mPaddingTop;
            final int bottom = getChildAt(0).getHeight();
            final int maxY = Math.max(0, bottom - height);
            final int scrollY = mScrollY;
            dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY;

            mScroller.startScroll(mScrollX, scrollY, 0, dy);
            invalidate();
        } else {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            scrollBy(dx, dy);
        }
        mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    /**
     * Like {@link #scrollTo}, but scroll smoothly instead of immediately.
     *
     * @param x the position where to scroll on the X axis
     * @param y the position where to scroll on the Y axis
     */
    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - mScrollX, y - mScrollY);
    }

    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */
    @Override
    protected int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int contentHeight = getHeight() - mPaddingBottom - mPaddingTop;
        if (count == 0) {
            return contentHeight;
        }
        
        return getChildAt(0).getBottom();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, mPaddingLeft
                + mPaddingRight, lp.width);

        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    int oldScrollY;
    @Override
    public void computeScroll() {
    	if(!mFlingInAction){
    		return;
    	}
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where mScrollX/Y is different from what the app
            //         thinks it is.
            //
            int oldX = mScrollX;
            int oldY = mScrollY;
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            
            distanceMoved = Math.abs(oldScrollY - mScroller.getCurrY());
            oldScrollY = mScroller.getCurrY();
            if (mAnimationinProgress) {
    			Log.e(TAG, "ignoring computeScroll()");
    			return;
    		}
//    		
//    		if(mFlingInAction){
//    			if (mCurrentVelocity > mTouchVelocityUnit
//    					&& mCurrentVelocity <= (2 * mTouchVelocityUnit)) {
//    				// Log.e(TAG, "updateViews speed low");
//    				distanceMoved = 10;
//    			} else if (mCurrentVelocity > (2 * mTouchVelocityUnit)
//    					&& mCurrentVelocity <= (3 * mTouchVelocityUnit)) {
//    				// Log.e(TAG, "updateViews speed medium low");
//    				distanceMoved = 20;
//    			}
//    		}
//    		Log.e(TAG, "computeScroll  " +distanceMoved);
    		moveFirstView(firstView);
    		if(moveSecondView(secondView)){
    			moveThirdView(thirdView);
    			moveFourthView(fourthView);
    			moveFifthView(fifthView);
    			moveSixthView(sixthView);
    		}
            
//            oldScrollY = mScroller.getCurrY();
//            Log.w(TAG, "computeScroll "+y+" count "+count+" distanceM "+distanceMoved);
//            if (getChildCount() > 0) {
//                View child = getChildAt(0);
//                x = clamp(x, getWidth() - mPaddingRight - mPaddingLeft, child.getWidth());
//                y = clamp(y, getHeight() - mPaddingBottom - mPaddingTop, child.getHeight());
//                if (x != oldX || y != oldY) {
//                    mScrollX = x;
//                    mScrollY = y;
//                    onScrollChanged(x, y, oldX, oldY);
//                }
//            }
//            awakenScrollBars();

            // Keep on drawing until the animation has finished.
            count++;
            postInvalidate();
        }else{
        	oldScrollY = 0;
        	mFlingInAction = false;
        	completeScroll(true);
        	count = 0;
        }
    }
    private int count = 0;

    /**
     * Scrolls the view to the given child.
     *
     * @param child the View to scroll to
     */
    private void scrollToChild(View child) {
        child.getDrawingRect(mTempRect);

        /* Offset from child's local coordinates to ScrollView coordinates */
        offsetDescendantRectToMyCoords(child, mTempRect);

        int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);

        if (scrollDelta != 0) {
            scrollBy(0, scrollDelta);
        }
    }

    /**
     * If rect is off screen, scroll just enough to get it (or at least the
     * first screen size chunk of it) on screen.
     *
     * @param rect      The rectangle.
     * @param immediate True to scroll immediately without animation
     * @return true if scrolling was performed
     */
    private boolean scrollToChildRect(Rect rect, boolean immediate) {
        final int delta = computeScrollDeltaToGetChildRectOnScreen(rect);
        final boolean scroll = delta != 0;
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta);
            } else {
                smoothScrollBy(0, delta);
            }
        }
        return scroll;
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
     *
     * @param rect The rect.
     * @return The scroll delta.
     */
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return 0;

        int height = getHeight();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;

        int fadingEdge = getVerticalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < getChildAt(0).getHeight()) {
            screenBottom -= fadingEdge;
        }

        int scrollYDelta = 0;

        if (rect.bottom > screenBottom && rect.top > screenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.height() > height) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - screenTop);
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - screenBottom);
            }

            // make sure we aren't scrolling beyond the end of our content
            int bottom = getChildAt(0).getBottom();
            int distanceToBottom = bottom - screenBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.height() > height) {
                // screen size chunk
                scrollYDelta -= (screenBottom - rect.bottom);
            } else {
                // entire rect at top
                scrollYDelta -= (screenTop - rect.top);
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = Math.max(scrollYDelta, -getScrollY());
        }
        return scrollYDelta;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (!mScrollViewMovedFocus) {
            if (!mIsLayoutDirty) {
                scrollToChild(focused);
            } else {
                // The child may not be laid out yet, we can't compute the scroll yet
                mChildToScrollTo = focused;
            }
        }
        super.requestChildFocus(child, focused);
    }


    /**
     * When looking for focus in children of a scroll view, need to be a little
     * more careful not to give focus to something that is scrolled off screen.
     *
     * This is more expensive than the default {@link android.view.ViewGroup}
     * implementation, otherwise this behavior might have been made the default.
     */
    @Override
    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {

        // convert from forward / backward notation to up / down / left / right
        // (ugh).
        if (direction == View.FOCUS_FORWARD) {
            direction = View.FOCUS_DOWN;
        } else if (direction == View.FOCUS_BACKWARD) {
            direction = View.FOCUS_UP;
        }

        final View nextFocus = previouslyFocusedRect == null ?
                FocusFinder.getInstance().findNextFocus(this, null, direction) :
                FocusFinder.getInstance().findNextFocusFromRect(this,
                        previouslyFocusedRect, direction);

        if (nextFocus == null) {
            return false;
        }

        if (isOffScreen(nextFocus)) {
            return false;
        }

        return nextFocus.requestFocus(direction, previouslyFocusedRect);
    }    

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
            boolean immediate) {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.getLeft() - child.getScrollX(),
                child.getTop() - child.getScrollY());

        return scrollToChildRect(rectangle, immediate);
    }

    @Override
    public void requestLayout() {
        mIsLayoutDirty = true;
        super.requestLayout();
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//        mIsLayoutDirty = false;
//        // Give a child focus if it needs it 
//        if (mChildToScrollTo != null && isViewDescendantOf(mChildToScrollTo, this)) {
//                scrollToChild(mChildToScrollTo);
//        }
//        mChildToScrollTo = null;
//
//        // Calling this with the present values causes it to re-clam them
//        scrollTo(mScrollX, mScrollY);
//    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        View currentFocused = findFocus();
//        if (null == currentFocused || this == currentFocused)
//            return;
//
//        // If the currently-focused view was visible on the screen when the
//        // screen was at the old height, then scroll the screen to make that
//        // view visible with the new screen height.
//        if (isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
//            currentFocused.getDrawingRect(mTempRect);
//            offsetDescendantRectToMyCoords(currentFocused, mTempRect);
//            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
//            doScrollY(scrollDelta);
//        }
//    }    

    /**
     * Return true if child is an descendant of parent, (or equal to the parent).
     */
    private boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }

        final ViewParent theParent = child.getParent();
        return (theParent instanceof ViewGroup) && isViewDescendantOf((View) theParent, parent);
    }    

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
//        if (getChildCount() > 0) {
//            int height = availableScreenSize;
//            int bottom = (int) mCardHeight;
//            Log.v(TAG,"fling "+velocityY+ " mScrollY " +mScrollY+" mScrollX "+ mScrollX);
//            
////            mScroller.fling(mScrollX, mScrollY, 0, velocityY, 0, 0, 0, 
////                    Math.max(0, bottom - height));
//           
//            int y = mScreenHeight;
//           
////            mScroller.fling(0, 0, 0, velocityY, 0, mScreenWidth, 0, y);//, 0, height / 2);
//    
//           
//    
////            View newFocused =
////                    findFocusableViewInMyBounds(movingDown, mScroller.getFinalY(), findFocus());
////            if (newFocused == null) {
////                newFocused = this;
////            }
////    
////            if (newFocused != findFocus()
////                    && newFocused.requestFocus(movingDown ? View.FOCUS_DOWN : View.FOCUS_UP)) {
////                mScrollViewMovedFocus = true;
////                mScrollViewMovedFocus = false;
////            }
//    
//            
//        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This version also clamps the scrolling to the bounds of our child.
     */
//    @Override
//    public void scrollTo(int x, int y) {
////    	Log.d(TAG,"scrollTo "+y);
////    	scroll(y);
//        // we rely on the fact the View.scrollBy calls scrollTo.
//        if (getChildCount() > 0) {
//            View child = getChildAt(0);
//            x = clamp(x, getWidth() - mPaddingRight - mPaddingLeft, child.getWidth());
//            y = clamp(y, getHeight() - mPaddingBottom - mPaddingTop, child.getHeight());
//            if (x != mScrollX || y != mScrollY) {
//                super.scrollTo(x, y);
//            }
//        }
//    }
//
//    private int clamp(int n, int my, int child) {
//        if (my >= child || n < 0) {
//            /* my >= child is this case:
//             *                    |--------------- me ---------------|
//             *     |------ child ------|
//             * or
//             *     |--------------- me ---------------|
//             *            |------ child ------|
//             * or
//             *     |--------------- me ---------------|
//             *                                  |------ child ------|
//             *
//             * n < 0 is this case:
//             *     |------ me ------|
//             *                    |-------- child --------|
//             *     |-- mScrollX --|
//             */
//            return 0;
//        }
//        if ((my+n) > child) {
//            /* this case:
//             *                    |------ me ------|
//             *     |------ child ------|
//             *     |-- mScrollX --|
//             */
//            return child-my;
//        }
//        return n;
//    }
    
    
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
		if(result > 100){
			result = 48;
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
			mNumberofViewsToMove--;
			completeScroll(false);
		} else {
			mAutoScroll = false;
		}
	}
	private void movetoNext(boolean direct) {
		if (getChildCount() == 0) {
			return;
		}
		if (movingDirection == SWIPEUP) {
			if (mCurrentSelectedIndex + 1 < mNumberofItems) {
				mCurrentSelectedIndex++;
//				sendDelayedCallBack();
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
//				sendDelayedCallBack();
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
//		Log.e(TAG, "updateViews  " + movingDirection +" distanceMoved "+distanceMoved);
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
		Log.e(TAG, "moveSecondView old postion="+v.getY()+" distanceMoved ="+distanceMoved);
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
				 Log.d(TAG, "moveSecondView new slot ");
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
				 Log.d(TAG, "moveSecondView new swipe down slot ");
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

	private int mNumberofViewsToMove = 0;
	private int mIndexofSelectedView = 0;

	private void findFocusedChild(MotionEvent motionEvent) {
		 mAutoScroll = true;
		int[] listViewCoords = new int[2];
		getLocationOnScreen(listViewCoords);
		int y = (int) motionEvent.getRawY() - listViewCoords[1];
		if (y > 0 && y <= secondItemStart) {
			Log.v(TAG, "#firstitem selected");
			mAutoScroll = false;
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
		public void play(CardData object);
	}

	// CallBack Listeners

	private OnClickListener mDeleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mAutoScroll = false;
			Log.e(TAG, "mDeleteListener onClick");
//			deleteCard(v);
		}
	};
	private OnClickListener mFavListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mAutoScroll = false;
			if (mOnFavouriteListener != null)
				mOnFavouriteListener.addFavourite(v.getId());
		}
	};
	private OnClickListener mPlayListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnPlayListener != null){
				mOnPlayListener.play((CardData) v.getTag());
			}
		}
	};
	private OnClickListener mPurchaseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mAutoScroll = false;
			if (mOnPurchaseListener != null)
				mOnPurchaseListener.purchase(v.getId());
		}
	};
	private OnClickListener mMoreInfoListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mAutoScroll = false;
			if (mOnMoreInfoListener != null)
				mOnMoreInfoListener.moreInfo(v.getId());
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


	private int mOverscrollDistance;
	private int mOverflingDistance;

	private static final int MINIMUMVIEWS = 2;

	private OnLoadMoreListener mOnLoadMoreListener;
	private OnFavouriteListener mOnFavouriteListener;
	private OnPlayListener mOnPlayListener;
	private OnDeleteListener mOnDeleteListener;
	private OnMoreInfoListener mOnMoreInfoListener;
	private OnPurchaseListener mOnPurchaseListener;
	private OnItemSelectedListener mOnItemSelectedListener;
}

