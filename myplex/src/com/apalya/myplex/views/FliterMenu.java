package com.apalya.myplex.views;

import com.apalya.myplex.R;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class FliterMenu extends RelativeLayout{
	private boolean isOpened = false;
	private String TAG = "FliterMenu";
	public FliterMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FliterMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FliterMenu(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	private void completeAnimation(){
		
	}
	private int mScreenWidth;
	private int mScreenHeight;
	private float mOrginalYPos;
	private Context mContext;
	public void init(Context cxt){
		mContext = cxt;
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		mHideposition = (mScreenHeight-((int)mContext.getResources().getDimension(R.dimen.actionbarheight)+getStatusBarHeight()+20)); 
		setY(-mHideposition);
		mOrginalYPos =  getY();
	}
	private float mIntialOffset = 0;
	private float mCurrentOffset = 0;
	private float mDistanceMoved = 0;
	private int mHideposition;
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
	private void moveView(){
		mDistanceMoved = Math.abs(mIntialOffset - mCurrentOffset);
		setY(mOrginalYPos+mDistanceMoved);
	}
	private void completeMovement(){
		float y = Math.abs(getY());
		float value = mScreenHeight - y;
		if( value > mScreenHeight/2){
			show(); 
		}else{
			hide();
		}
	}
	public void hide(){
		animate(getY(),-mHideposition,this,1000);
	}
	public void show(){
		animate(getY(),0,this,1000); 
	}
	private void animate(float fromY, float toY, final View v,int speed) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromY, toY));
		set.setDuration(speed);
		set.setInterpolator(new DecelerateInterpolator());
		set.start();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e(TAG, "onTouchEvent start");
		final int action = event.getAction();
		float y = event.getRawY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:{
			mIntialOffset = y;
			break;
		}case MotionEvent.ACTION_UP:{
			completeMovement();
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			mCurrentOffset = y;
			moveView();
			break;
		}
		}
		return true;
	}

}
