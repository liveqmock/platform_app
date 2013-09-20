package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

public class CardGoogleLayoutAdapater extends BaseAdapter{
	private static final String TAG = "CardGoogleLayoutAdapater";
	private List<CardData> mDataList = new ArrayList<CardData>();
	private Context mContext;
	private LayoutInflater mInflater;
	private CardActionListener mCardActionListener;
	private int mNumberofItems;
	public CardGoogleLayoutAdapater(Context context){
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	public void setData(List<CardData> datalist){
		if(datalist == null){return;}
		mDataList = new ArrayList<CardData>();
		for(CardData data: datalist){
			this.mDataList.add(data);
		}
		mNumberofItems  = mDataList.size();
		notifyDataSetChanged();
	}
	public void forceUpdateData(List<CardData> datalist){
//		mDataList.clear();
		mDataList = new ArrayList<CardData>();
		for(CardData data: datalist){
			this.mDataList.add(data);
		}
		mNumberofItems = mDataList.size();	
		notifyDataSetChanged();
	}
	public void setCardActionListener(CardActionListener listener) {
		this.mCardActionListener = listener;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDataList.get(position);	
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		
		CardData data = mDataList.get(position);
		
		if(v == null){
			v  = mInflater.inflate(R.layout.card, null);	
		}	
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
		tagData.mUiHolder.mDelete.setTag(v);
		tagData.mUiHolder.mFavourite.setOnClickListener(mFavListener);
		tagData.mUiHolder.mFavourite.setTag(v);
		tagData.mUiHolder.mRent.setOnClickListener(mPurchaseListener);
		tagData.mUiHolder.mRent.setTag(v);
		tagData.mUiHolder.mInfo.setOnClickListener(mMoreInfoListener);
		tagData.mUiHolder.mInfo.setTag(v);
		tagData.mUiHolder.mText.setText(data.title);
		tagData.mUiHolder.mPlay.setOnClickListener(mPlayListener);
		tagData.mUiHolder.mPlay.setTag(v);
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
		if (position == mDataList.size() - 1) {
			if (mCardActionListener != null) {
				mCardActionListener.loadmore(mDataList.size());
			}
		}
		return v;
	}
	
	private CardItemClickListener mDeleteListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			Log.e(TAG, "mDeleteListener onClick");
			View parentView  = (View)v.getTag();
			if(parentView == null){
				return;
			}
			CardViewMeta metadata = (CardViewMeta) parentView.getTag();
			if(metadata == null ){
				return;
			}
			CardData data = (CardData)metadata.mObj;
			
			int index = mDataList.indexOf(data);
			mDataList.remove(index);
//			AnimatorSet set = new AnimatorSet();
//			set.play(ObjectAnimator.ofFloat(parentView, View.TRANSLATION_X, 0,-parentView.getWidth()));
//			set.setDuration(2000);
//			set.setInterpolator(new DecelerateInterpolator());
//			set.addListener(new AnimatorListener() {
//				
//				@Override
//				public void onAnimationStart(Animator animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationRepeat(Animator animation) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onAnimationEnd(Animator animation) {
//					// TODO Auto-generated method stub
//					mCardsLayout.removeAllViews();
//					mCardsLayout.refresh();
//					mCardsLayout.updateCardsPosition(getScrollY());
//				}
//				
//				@Override
//				public void onAnimationCancel(Animator animation) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
//			set.start();
		}
	};

	private CardItemClickListener mFavListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener == null){
				return;
			}
			Log.e(TAG, "mFavListener onClick");
			View parentView  = (View)v.getTag();
			if(parentView == null){
				return;
			}
			CardViewMeta metadata = (CardViewMeta) parentView.getTag();
			if(metadata == null ){
				return;
			}
			CardData data = (CardData)metadata.mObj;
			if(data == null ){
				return;
			}
			data.applyFavoriteInProgress = true;
			int index = mDataList.indexOf(data);
			if(metadata.mUiHolder != null){
				metadata.mUiHolder.mFavourite.setVisibility(View.INVISIBLE);
				metadata.mUiHolder.mFavouriteProgress.setVisibility(View.VISIBLE);	
			}
			if(!data.isFavorite){
				mCardActionListener.addFavourite(data);	
			}else{
				mCardActionListener.removeFavourite(data);
			}
			parentView.requestLayout();
			
		}
	};

	private CardItemClickListener mPlayListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener != null){
				View parentView  = (View)v.getTag();
				if(parentView == null){
					return;
				}
				CardViewMeta metadata = (CardViewMeta) parentView.getTag();
				if(metadata == null ){
					return;
				}
				CardData data = (CardData)metadata.mObj;
				if(data == null ){
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
				View parentView  = (View)v.getTag();
				if(parentView == null){
					return;
				}
				CardViewMeta metadata = (CardViewMeta) parentView.getTag();
				if(metadata == null ){
					return;
				}
				CardData data = (CardData)metadata.mObj;
				if(data == null){
					return;
				}
				mCardActionListener.play(data);
			}
		}
	};

}
