package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FliterMenu extends RelativeLayout {
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

	private int mScreenWidth;
	private int mScreenHeight;
	private float mOrginalYPos;
	private Context mContext;
	private static LayoutInflater mInflator;

	public void init(Context cxt) {
		mInflator = LayoutInflater.from(cxt);
		mContext = cxt;
		mScreenHeight = myplexUtils.mScreenHeight;
		mScreenWidth = myplexUtils.mScreenWidth;
		mHideposition = (mScreenHeight - ((int) mContext.getResources()
				.getDimension(R.dimen.actionbarheight) + getStatusBarHeight() + 20));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, mHideposition);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// setLayoutParams(layoutParams);
		setY(-mHideposition);
		mOrginalYPos = getY();
		int ScrollViewHeight = mHideposition - 40;
		listView = new PinnedSectionListView(mContext);
		RelativeLayout.LayoutParams scrolllayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, ScrollViewHeight);
		scrolllayoutParams.bottomMargin = 20;
		scrolllayoutParams.leftMargin = 30;
		scrolllayoutParams.rightMargin = 30;
		interpolator = new DecelerateInterpolator();
		listView.setLayoutParams(scrolllayoutParams);
		mScrollListener = new SpeedScrollListener();
		listView.setOnScrollListener(mScrollListener);
		listView.setBackgroundColor(Color.parseColor("#303030"));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				if (mOnMenuItemSelectedListener != null && v.getTag() != null) {
					mOnMenuItemSelectedListener.menuItemSelected((FilterMenudata) v
							.getTag());
				}
				if(mOnMenuOpenedListener != null){
					mOnMenuOpenedListener.menuOpened(false);
				}
				hide();
			}
		});

		addView(listView);
				
	}
	 private int previousPostition;
	 protected Interpolator interpolator;
	  protected double speed;
	  protected long animDuration;
	  protected static final long ANIM_DEFAULT_SPEED = 1000L;
	  protected SparseBooleanArray positionsMapper;
	 private SpeedScrollListener mScrollListener;
	private PinnedSectionListView listView;
	private float mIntialOffset = 0;
	private float mCurrentOffset = 0;
	private float mDistanceMoved = 0;
	private int mHideposition;

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		if(result >100){
			result = 48;
		}
		return result;
	}

	private void moveView() {
		mDistanceMoved = Math.abs(mIntialOffset - mCurrentOffset);
		if(mCurrentOffset > mIntialOffset){
			if(mOrginalYPos + mDistanceMoved  < mScreenHeight){
				setY(mOrginalYPos + mDistanceMoved);	
			}
		}else{
			if(mOrginalYPos - mDistanceMoved  > -mHideposition){
				setY(mOrginalYPos - mDistanceMoved);
			}
		}
		Log.e(TAG, "moveView mDistanceMoved= " +mDistanceMoved+" y = "+getY() +" mOrginalYPos ="+mOrginalYPos);
		
	}

	private void completeMovement() {
		float y = Math.abs(getY());
		float value = mScreenHeight - y;
		if (value < mScreenHeight / 2) {
			if(mOnMenuOpenedListener != null){
				mOnMenuOpenedListener.menuOpened(true);
			}
			show();
		} else {
			if(mOnMenuOpenedListener != null){
				mOnMenuOpenedListener.menuOpened(false);
			}
			hide();
		}
	}

	public void setData(List<FilterMenudata> datalist) {
		if (datalist == null) {
			return;
		}
		for (FilterMenudata data : datalist) {
			mMenuDataList.add(data);
		}
		prepareUI();
	}

	public interface onMenuItemSelected {
		public void menuItemSelected(FilterMenudata data);
	}
	public interface onMenuOpened {
		public void menuOpened(boolean value);
	}

	private onMenuItemSelected mOnMenuItemSelectedListener;
	private onMenuOpened mOnMenuOpenedListener;

	public void setonMenuItemSelectedListener(onMenuItemSelected listener) {
		mOnMenuItemSelectedListener = listener;
	}
	public void setonMenuOpened(onMenuOpened listener) {
		mOnMenuOpenedListener = listener;
	}
	private void prepareUI() {
		MyPinnedSectionListAdapter adapter = new MyPinnedSectionListAdapter(
				mContext, android.R.layout.simple_list_item_1, android.R.id.text1, mMenuDataList);
		listView.setAdapter(adapter);
		positionsMapper = new SparseBooleanArray(mMenuDataList.size());
	}

	private LinearLayout mLayout;
	private static List<FilterMenudata> mMenuDataList = new ArrayList<FilterMenudata>();

	public void hide() {
		animate(getY(), -mHideposition, this, 1000);
	}

	public void show() {
		animate(getY(), 0, this, 1000);
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e(TAG, "onTouchEvent start");
		final int action = event.getAction();
		float y = event.getRawY();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mOrginalYPos = getY();
			mIntialOffset = y;
			break;
		}
		case MotionEvent.ACTION_UP: {
			completeMovement();
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			mCurrentOffset = y;
			moveView();
			break;
		}
		}
		return true;
	}

	private  class MyPinnedSectionListAdapter extends ArrayAdapter<FilterMenudata>
			implements PinnedSectionListAdapter {


		public MyPinnedSectionListAdapter(Context context, int resource,
				int textViewResourceId, List<FilterMenudata> objects) {
			super(context, resource, textViewResourceId, objects);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
//			TextView view = (TextView) super.getView(position, convertView,
//					parent);
//			view.setTextColor(Color.DKGRAY);
			View v = null;
			if (getItem(position).type == FilterMenudata.SECTION) {
				v = mInflator.inflate(R.layout.filtermenuitem, null);
				TextView txt = (TextView) v.findViewById(R.id.filtermenutext);
				txt.setText(mMenuDataList.get(position).label);
				v.setTag(mMenuDataList.get(position));
			}else if(getItem(position).type == FilterMenudata.ITEM){
				v = mInflator.inflate(R.layout.filtersubmenuitem, null);
				TextView txt = (TextView) v.findViewById(R.id.filtersubmenutext);
				txt.setText(mMenuDataList.get(position).label);
				v.setTag(mMenuDataList.get(position));
			}
//			if (v != null /*&& !positionsMapper.get(position) && position > previousPostition*/) {
//
//			      speed = mScrollListener.getSpeed();
//
//			      animDuration = (((int) speed) == 0) ? ANIM_DEFAULT_SPEED : (long) (1 / speed * 15000);
//
//			      if (animDuration > ANIM_DEFAULT_SPEED)
//			        animDuration = ANIM_DEFAULT_SPEED;
//
//			      previousPostition = position;
//
//			      v.setTranslationY(mScreenHeight);
//			      v.setPivotX(mScreenWidth / 2);
//			      v.setPivotY(mScreenHeight / 2);
//			      v.setAlpha(0.0F);
//
//			      if (position % 2 == 0) {
//			        v.setTranslationX(-(mScreenWidth / 1.2F));
//			        v.setRotation(50);
//			      } else {
//			        v.setTranslationX((mScreenWidth / 1.2F));
//			        v.setRotation(-50);
//			      }
//
//			      ViewPropertyAnimator localViewPropertyAnimator =
//			          v.animate().rotation(0.0F).translationX(0).translationY(0).setDuration(animDuration).alpha(1.0F)
//			              .setInterpolator(interpolator);
//
//			      localViewPropertyAnimator.setStartDelay(500).start();
//
//			      positionsMapper.put(position, true);
//			    }
			return v;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).type;
		}

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType == FilterMenudata.SECTION;
		}
	}
	public class SpeedScrollListener implements OnScrollListener {

		  private int previousFirstVisibleItem = 0;
		  private long previousEventTime = 0, currTime, timeToScrollOneElement;
		  private double speed = 0;

		  @Override
		  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		    if (previousFirstVisibleItem != firstVisibleItem) {
		      currTime = System.currentTimeMillis();
		      timeToScrollOneElement = currTime - previousEventTime;
		      speed = ((double) 1 / timeToScrollOneElement) * 1000;

		      previousFirstVisibleItem = firstVisibleItem;
		      previousEventTime = currTime;

		    }
		  }

		  @Override
		  public void onScrollStateChanged(AbsListView view, int scrollState) {
		  }

		  public double getSpeed() {
		    return speed;
		  }

		}
}
