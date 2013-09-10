package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardDetailCast;
import com.apalya.myplex.data.CardDetailCommentListData;
import com.apalya.myplex.data.CardDetailDataHolder;
import com.apalya.myplex.data.CardDetailDescriptionData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.CustomFastScrollView.SectionIndexer;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;

public class CardDetailsAdapter extends BaseAdapter implements SectionIndexer {
	private List<CardDetailDataHolder> mCardDetails = new ArrayList<CardDetailDataHolder>();
	private HashMap<String, View> mViewList = new HashMap<String, View>();
	private SectionIndexer sectionIndexer;
	private LayoutInflater mInflater;
	public Context mContext;
	private ItemExpandListenerCallBackListener mItemExpandListenerCallBackListener;
	private int mScreenHeight;
	private int mScreenWidth;
	private int mImageGap;
	private int mImageWidth;

	public void setItemExpandListener(
			ItemExpandListenerCallBackListener listener) {
		this.mItemExpandListenerCallBackListener = listener;
	}

	public CardDetailsAdapter(Context cxt) {
		this.mContext = cxt;
		mInflater = LayoutInflater.from(cxt);
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		mImageGap = (int) mContext.getResources().getDimension(
				R.dimen.carddetailmultimediathumnnailmargin);
		mImageWidth = (mScreenWidth - (6 * mImageGap)) / 4;
	}

	public HashMap<String, View> getViewList() {
		return mViewList;
	}

	public void setData(List<CardDetailDataHolder> mCardDetails) {
		this.mCardDetails = mCardDetails;
	}

	@Override
	public int getCount() {
		return mCardDetails.size();
	}

	@Override
	public Object getItem(int position) {
		return mCardDetails.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View v = null;
		// v = mViewList.get(""+position);
		if (v == null) {
			v = createView(position);
			// mViewList.put(""+position,v);

		}
		return v;
	}

	public View createView(int position) {
		View v = null;
		CardDetailDataHolder data = mCardDetails.get(position);
		if (data == null) {
			return v;
		}
		data.mViewPosition = position;
		if (data.mData instanceof CardDetailDescriptionData) {
			CardDetailDescriptionData localData = (CardDetailDescriptionData) data.mData;
			v = mInflater.inflate(R.layout.carddetaildescription, null);
			v.setTag(data);
			TextView description = (TextView) v
					.findViewById(R.id.carddetaildesc_description);
			if (!localData.mAlreadyExpanded) {
				description.setText(localData.mContentBriefDescription);
			} else {
				description.setText(localData.mContentFullDescription);
			}
			description.setTypeface(FontUtil.Roboto_Medium);
			RatingBar rating = (RatingBar)v.findViewById(R.id.carddetaildesc_setRating);
			rating.setRating(localData.mRating);
			TextView movename = (TextView) v
					.findViewById(R.id.carddetaildesc_movename);
			movename.setText(localData.mTitle);

			LinearLayout loadmore = (LinearLayout) v
					.findViewById(R.id.carddetaildesc_loadmoretext);
			loadmore.setTag(v);
			loadmore.setOnClickListener(mLoadMoreListener);
			if (localData.mAlreadyExpanded) {
				loadmore.setVisibility(View.INVISIBLE);
			}
		} else if (data.mData instanceof CardDetailCast) {

		} else if (data.mData instanceof CardDetailCommentListData) {
			CardDetailCommentListData localData = (CardDetailCommentListData) data.mData;
			v = mInflater.inflate(R.layout.carddetailcomment, null);
			
		} else if (data.mData instanceof CardDetailMediaListData) {
			CardDetailMediaListData localData = (CardDetailMediaListData) data.mData;
			v = mInflater.inflate(R.layout.carddetailmedia, null);
			v.setTag(data);
			TextView text = (TextView) v
					.findViewById(R.id.carddetailmedia_textView1);
			text.setText(data.mLabel);
			LinearLayout contentLayout = (LinearLayout) v
					.findViewById(R.id.carddetailmedia_contentlayout);
			createMediaitem(contentLayout, localData);
			LinearLayout loadmore = (LinearLayout) v
					.findViewById(R.id.carddetailmedia_loadmoretext);
			loadmore.setTag(v);
			loadmore.setOnClickListener(mLoadMoreListener);
			if (localData.mLastShownIndex >= localData.mList.size()) {
				loadmore.setVisibility(View.INVISIBLE);
			}
		}
		return v;
	}

	public void createMediaitem(LinearLayout mContentLayout,
			CardDetailMediaListData localData) {

		int index = 0, j = 0;
		for (int i = 0; i < localData.mNumberofBlockAdded; i++) {
			int Count = 0;
			LinearLayout sublayout = new LinearLayout(mContext);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
//			params.gravity = Gravity.CENTER_HORIZONTAL;
			sublayout.setLayoutParams(params);
			sublayout.setOrientation(LinearLayout.HORIZONTAL);
			mContentLayout.addView(sublayout);
			for (j = index; j < localData.mList.size(); j++) {
				if (Count > 3) {
					break;
				}

				Count++;
				View v = mInflater.inflate(R.layout.cardmediasubitem, null);
				FadeInNetworkImageView imageView = (FadeInNetworkImageView) v
						.findViewById(R.id.cardmediasubitem_imageView1);
				LinearLayout.LayoutParams imageparams = new LinearLayout.LayoutParams(
						mImageWidth, mImageWidth);
				params.setMargins(mImageGap, mImageGap, mImageGap, mImageGap);
				imageView.setLayoutParams(imageparams);
				Random rnd = new Random();
				int color = Color.argb(5, rnd.nextInt(128), rnd.nextInt(128),
						rnd.nextInt(64));
				imageView.setTag(localData);
				if(mItemExpandListenerCallBackListener != null){
					imageView.setOnClickListener(new ItemExpandListener(mItemExpandListenerCallBackListener));
				}
				imageView.setBackgroundColor(color);
				imageView.setImageUrl(localData.mList.get(j).mThumbnailUrl,
						MyVolley.getImageLoader());
				sublayout.addView(v);
			}
			index = j;
		}
		localData.mLastShownIndex = j;
	}

	public void createMediaitem(LinearLayout mContentLayout,
			CardDetailMediaListData localData, boolean value) {

		int Count = 0;
		LinearLayout sublayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
//		params.gravity = Gravity.CENTER_HORIZONTAL;
		sublayout.setLayoutParams(params);
		sublayout.setOrientation(LinearLayout.HORIZONTAL);
		mContentLayout.addView(sublayout);

		for (int i = localData.mLastShownIndex; i < localData.mList.size(); i++) {
			if (Count > 3) {
				break;
			}
			localData.mLastShownIndex = i;
			Count++;
			View v = mInflater.inflate(R.layout.cardmediasubitem, null);
			FadeInNetworkImageView imageView = (FadeInNetworkImageView) v
					.findViewById(R.id.cardmediasubitem_imageView1);

			LinearLayout.LayoutParams imageparams = new LinearLayout.LayoutParams(
					mImageWidth, mImageWidth);
			params.setMargins(mImageGap, mImageGap, mImageGap, mImageGap);
			imageView.setLayoutParams(imageparams);
			Random rnd = new Random();
			int color = Color.argb(255, rnd.nextInt(128), rnd.nextInt(128),
					rnd.nextInt(64));
			imageView.setBackgroundColor(color);
			imageView.setTag(localData);
			if(mItemExpandListenerCallBackListener != null){
				imageView.setOnClickListener(new ItemExpandListener(mItemExpandListenerCallBackListener));
			}
			imageView.setImageUrl(localData.mList.get(i).mThumbnailUrl,
					MyVolley.getImageLoader());
			sublayout.addView(v);
		}
		sublayout.requestLayout();
		Animation anim = new ExpandAnimation(sublayout, ExpandAnimation.EXPAND,
				mImageWidth);
		anim.setDuration(500);
		sublayout.startAnimation(anim);
		localData.mNumberofBlockAdded++;
	}

	private OnClickListener mLoadMoreListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == null) {
				return;
			}
			Log.e("pref", "OnClickListener");
			View localv = (View) v.getTag();
			CardDetailDataHolder data = (CardDetailDataHolder) localv.getTag();

			if (data.mData instanceof CardDetailDescriptionData) {
				CardDetailDescriptionData localData = (CardDetailDescriptionData) data.mData;
				TextView description = (TextView) localv
						.findViewById(R.id.carddetaildesc_description);
				description.setTypeface(FontUtil.Roboto_Medium);
				description.setText(localData.mContentFullDescription);
				localData.mAlreadyExpanded = true;
				LinearLayout loadmore = (LinearLayout) v
						.findViewById(R.id.carddetaildesc_loadmoretext);
				loadmore.setVisibility(View.INVISIBLE);
				Animation anim = new ExpandAnimation(description, ExpandAnimation.EXPAND,
						mImageWidth);
				anim.setDuration(500);
				description.startAnimation(anim);

			} else if (data.mData instanceof CardDetailCast) {

			} else if (data.mData instanceof CardDetailCommentListData) {

			} else if (data.mData instanceof CardDetailMediaListData) {
				CardDetailMediaListData localData = (CardDetailMediaListData) data.mData;
				LinearLayout contentLayout = (LinearLayout) localv
						.findViewById(R.id.carddetailmedia_contentlayout);
				createMediaitem(contentLayout, localData, true);
				LinearLayout loadmore = (LinearLayout) localv
						.findViewById(R.id.carddetailmedia_loadmoretext);
				loadmore.setTag(localv);
				loadmore.setOnClickListener(mLoadMoreListener);
				if (localData.mLastShownIndex >= localData.mList.size()) {
					loadmore.setVisibility(View.INVISIBLE);
				}

			}
			// localv.requestLayout();
		}
	};

	@Override
	public Object[] getSections() {
		return getSectionIndexer().getSections();
	}

	@Override
	public int getPositionForSection(int section) {
		return getSectionIndexer().getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return getSectionIndexer().getSectionForPosition(position);
	}

	private SectionIndexer getSectionIndexer() {
		if (sectionIndexer == null) {
			sectionIndexer = createSectionIndexer();
		}
		return sectionIndexer;
	}

	private SectionIndexer createSectionIndexer() {

		List<String> sections = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();

		for (int i = 0; i < mCardDetails.size(); i++) {
			if (mCardDetails.get(i).mShowinQuickLaunch) {
				sections.add(mCardDetails.get(i).mFilterName);
				sectionsToPositions.add(i);
			}
			positionsToSections.add(sections.size() - 1);
		}

		final String[] sectionsArray = sections.toArray(new String[sections
				.size()]);

		return new SectionIndexer() {

			@Override
			public Object[] getSections() {
				return sectionsArray;
			}

			@Override
			public int getSectionForPosition(int position) {
				return positionsToSections.get(position);
			}

			@Override
			public int getPositionForSection(int section) {
				return sectionsToPositions.get(section);
			}
		};
	}

}
