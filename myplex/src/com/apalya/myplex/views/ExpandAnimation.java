package com.apalya.myplex.views;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
public class ExpandAnimation extends Animation {
	private View mAnimatedView;
	private int mEndHeight;
	private int mType;
	public final static int COLLAPSE = 1;
	public final static int EXPAND = 0;
	private LinearLayout.LayoutParams mLinearLayoutParams = null;
	private RelativeLayout.LayoutParams mRelativeLayoutParams= null;

	/**
	 * Initializes expand collapse animation, has two types, collapse (1) and expand (0).
	 * @param view The view to animate
	 * @param type The type of animation: 0 will expand from gone and 0 size to visible and layout size defined in xml.
	 * 1 will collapse view and set to gone
	 */
	public ExpandAnimation(View view, int type,int gap) {

		mAnimatedView = view;
		
		if(view.getLayoutParams() instanceof LinearLayout.LayoutParams){
			mLinearLayoutParams = ((LinearLayout.LayoutParams) view.getLayoutParams());	
			mEndHeight = gap;
		}else{
			mRelativeLayoutParams = ((RelativeLayout.LayoutParams) view.getLayoutParams());
			mEndHeight = mAnimatedView.getMeasuredHeight();
		}
		mType = type;
		if(mType == EXPAND) {
			if(mLinearLayoutParams != null){
				mLinearLayoutParams.bottomMargin = -mEndHeight;
			}else if(mRelativeLayoutParams != null){
				mRelativeLayoutParams.bottomMargin = -mEndHeight;
			}
		} else {
			if(mLinearLayoutParams != null){
				mLinearLayoutParams.bottomMargin = 0;
			}else if(mRelativeLayoutParams != null){
				mRelativeLayoutParams.bottomMargin = 0;
			}
		}
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {

		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {
			if(mType == EXPAND) {
				if(mLinearLayoutParams != null){
					mLinearLayoutParams.bottomMargin =  -mEndHeight + (int) (mEndHeight * interpolatedTime);
				}else if(mRelativeLayoutParams != null){
					mRelativeLayoutParams.bottomMargin =  -mEndHeight + (int) (mEndHeight * interpolatedTime);
				}
				
			} else {
				if(mLinearLayoutParams != null){
					mLinearLayoutParams.bottomMargin = - (int) (mEndHeight * interpolatedTime);
				}else if(mRelativeLayoutParams != null){
					mRelativeLayoutParams.bottomMargin = - (int) (mEndHeight * interpolatedTime);
				}
				
			}
			mAnimatedView.requestLayout();
		} else {
			if(mType == EXPAND) {
				if(mLinearLayoutParams != null){
					mLinearLayoutParams.bottomMargin = 0;
				}else if(mRelativeLayoutParams != null){
					mRelativeLayoutParams.bottomMargin = 0;
				}
				
				mAnimatedView.requestLayout();
			} else {
				if(mLinearLayoutParams != null){
					mLinearLayoutParams.bottomMargin = -mEndHeight;
				}else if(mRelativeLayoutParams != null){
					mRelativeLayoutParams.bottomMargin = -mEndHeight;
				}
				mAnimatedView.setVisibility(View.GONE);
				mAnimatedView.requestLayout();
			}
		}
	}
}