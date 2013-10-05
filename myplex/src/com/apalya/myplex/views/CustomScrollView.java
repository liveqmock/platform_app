package com.apalya.myplex.views;

import com.apalya.myplex.adapters.ScrollingDirection;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView{
	private ScrollingDirection mDirectionListener;
	public void setDirectionListener(ScrollingDirection mListener){
		this.mDirectionListener = mListener;
	}
	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CustomScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	private int mLastPosition = 0;
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mDirectionListener != null){
			mDirectionListener.scrollDirection((mLastPosition >= oldt)?true:false);
		}	
		mLastPosition = oldt;
	}

}
