package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataHolder;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.utils.CardImageLoader;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

public class CardTabletAdapater extends BaseAdapter{
	private static final String TAG = "CardGoogleLayoutAdapater";
	private List<CardData> mDataList = new ArrayList<CardData>();
	private Context mContext;
	private LayoutInflater mInflater;
	private CardActionListener mCardActionListener;
	private int mNumberofItems;
	public CardTabletAdapater(Context context){
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	public void setData(List<CardData> datalist){
		mDataList = new ArrayList<CardData>();
		if (datalist != null) {
			for (CardData data : datalist) {
				this.mDataList.add(data);
			}
		}
		mNumberofItems = mDataList.size();
		notifyDataSetChanged();
	}
	public void forceUpdateData(List<CardData> datalist){
		mDataList  = new ArrayList<CardData>();
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
		return mNumberofItems;
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
	private String mPriceStarts = "Starts from ";
	private String mRupeeCode  = null;
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		CardData data = mDataList.get(position);
		CardDataHolder dataHolder = null;
		if(v == null){
			v = mInflater.inflate(R.layout.card, null);
			dataHolder = new CardDataHolder();
			dataHolder.mTitleLayout = (LinearLayout)v.findViewById(R.id.card_title_layout); 
			dataHolder.mTitle = (TextView)v.findViewById(R.id.card_title_name);
			dataHolder.mDelete = (TextView)v.findViewById(R.id.card_title_delete);
			dataHolder.mFavourite = (TextView)v.findViewById(R.id.card_title_fav);
			dataHolder.mPreview = (CardImageView)v.findViewById(R.id.card_preview_image);
			dataHolder.mOverLayPlay = (ImageView)v.findViewById(R.id.card_play);
			dataHolder.mComments = (TextView)v.findViewById(R.id.card_status_comments);
			dataHolder.mReviews = (TextView)v.findViewById(R.id.card_status_people);
			dataHolder.mCommentsText = (TextView)v.findViewById(R.id.card_status_comments_text);
			dataHolder.mReviewsText = (TextView)v.findViewById(R.id.card_status_people_text);
//			dataHolder.mRentLayout = (LinearLayout)v.findViewById(R.id.card_rent_layout);
			dataHolder.mRentText = (TextView)v.findViewById(R.id.card_rent_text);
			dataHolder.mFavProgressBar = (ProgressBar) v.findViewById(R.id.card_title_fav_progress);
			
			// fonts
			
			
			dataHolder.mTitle.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mRentText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mCommentsText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mReviewsText.setTypeface(FontUtil.Roboto_Medium);
			
			
			dataHolder.mDelete.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mFavourite.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mComments.setTypeface(FontUtil.ss_symbolicons_line);
			dataHolder.mReviews.setTypeface(FontUtil.ss_symbolicons_line);
			
//			dataHolder.mOverLayPlay.setText(R.string.card_play);
//			dataHolder.mDelete.setText(R.string.card_delete);
//			dataHolder.mFavourite.setText(R.string.card_heart);
			dataHolder.mComments.bringToFront();
//			dataHolder.mReviews.setText(R.string.card_people);
			
//			CardData dataHolder.mDataObject = (TextView)v.findViewById(id);
			v.setTag(dataHolder);
		}else{
			dataHolder = (CardDataHolder)v.getTag();
		}
		v.setId(position);
		dataHolder.mDataObject = data;
		dataHolder.mPreview.mCardId = position;
		dataHolder.mPreview.mImageUrl = null;
//		dataHolder.mTitleLayout.setBackgroundColor(color)
		
//		ImageUrlMap.put(""+position,null);
		
		if(data.generalInfo != null && data.generalInfo.title != null){
			dataHolder.mTitle.setText(data.generalInfo.title);
		}
		
		Random rnd = new Random();
		int Low = 100;
		int High = 196;
		
//        int color = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low); 
//        dataHolder.mPreview.setBackgroundColor(color);
//        dataHolder.mPreview.setImageBitmap(null);
		Log.e("CardView","Erasing "+position+" for "+dataHolder.mTitle.getText());
		if(data.images != null){
			for(CardDataImagesItem imageItem:data.images.values){
				if(imageItem.profile != null && imageItem.profile.equalsIgnoreCase("xxhdpi")){
					if (imageItem.link != null && !(imageItem.link.compareTo("Images/NoImage.jpg") == 0)) {
						dataHolder.mPreview.mImageUrl = imageItem.link;
//						Log.d("CardExplorer","imageItem.link ="+imageItem.link+" profile = "+imageItem.profile);
						CardImageLoader ImageLoader = new CardImageLoader(position,mContext);
						ImageLoader.loadImage(dataHolder.mPreview);
//						dataHolder.mPreview.setImageUrl(imageItem.link,MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		dataHolder.mFavProgressBar.setVisibility(View.INVISIBLE);
		dataHolder.mFavourite.setVisibility(View.VISIBLE);
		if(data.currentUserData != null && data.currentUserData.favorite){
			dataHolder.mFavourite.setText(R.string.card_filledheart);
		}else{
			dataHolder.mFavourite.setText(R.string.card_heart);
		}
		if(data.userReviews != null){
			dataHolder.mReviewsText.setText(""+data.userReviews.values.size());	
		}else{
			dataHolder.mReviewsText.setText("0");
		}
		if(data.comments != null){
			dataHolder.mCommentsText.setText(""+data.comments.values.size());	
		}else{
			dataHolder.mCommentsText.setText("0");
		}
		float price = 10000f;
		if(data.packages == null){
			dataHolder.mRentText.setText("free");
		}else{
			for(CardDataPackages packageitem:data.packages){
				if(packageitem.priceDetails != null){
					for(CardDataPackagePriceDetailsItem priceDetailItem:packageitem.priceDetails){
						if(priceDetailItem.price < price){
							price = priceDetailItem.price;
						}
					}
					if(mRupeeCode == null){
						mRupeeCode = mContext.getResources().getString(R.string.price_rupeecode); 
					}
					dataHolder.mRentText.setText(mPriceStarts + mRupeeCode + " "+price);
				}else{
					dataHolder.mRentText.setText("Free");
				}
			}	
		}
//		int reqsize = (int) mContext.getResources().getDimension(R.dimen.margin_gap_24);
//		reqsize = 40;
//		 ShapeDrawable biggerCircle= new ShapeDrawable( new OvalShape());
//	        biggerCircle.setIntrinsicHeight( reqsize );
//	        biggerCircle.setIntrinsicWidth( reqsize);
//	        biggerCircle.setBounds(new Rect(0, 0, reqsize, reqsize));
//	        biggerCircle.getPaint().setColor(Color.BLUE);
//
//	        dataHolder.mOverLayPlay.setBackgroundDrawable(biggerCircle);
		dataHolder.mDelete.setOnClickListener(mDeleteListener);
		dataHolder.mDelete.setTag(dataHolder);
		dataHolder.mOverLayPlay.setOnClickListener(mOpenCardListener);
		dataHolder.mOverLayPlay.setTag(dataHolder);
		dataHolder.mPreview.setOnClickListener(mOpenCardListener);
		dataHolder.mPreview.setTag(dataHolder);
		dataHolder.mRentText.setOnClickListener(mPurchaseListener);
		dataHolder.mRentText.setTag(dataHolder);
		dataHolder.mFavourite.setOnClickListener(mFavListener);
		dataHolder.mFavourite.setTag(dataHolder);
		return v;
	}
	private CardItemClickListener mDeleteListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
//			Log.e(TAG, "mDeleteListener onClick");
//			CardDataHolder dataHolder = null;
//			if(v.getTag() instanceof CardDataHolder){
//				dataHolder = (CardDataHolder) v.getTag();
//				if(dataHolder == null){return;}
//				if(dataHolder.mDataObject == null){return;}
//			}else{
//				return;
//			}
//			int index = mDataList.indexOf(dataHolder.mDataObject);
//			mDataList.remove(index);
//			mNumberofItems = mDataList.size();
//			View localv = mCardsLayout.mCardViewReusePool.getView(index);
//			AnimatorSet set = new AnimatorSet();
//			set.play(ObjectAnimator.ofFloat(localv, View.TRANSLATION_X, 0,-localv.getWidth()));
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
			if(v.getTag() instanceof CardDataHolder){
				CardDataHolder dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
				int type = FavouriteUtil.FAVOURITEUTIL_ADD;
				if(dataHolder.mDataObject.currentUserData != null && dataHolder.mDataObject.currentUserData.favorite){
					type = FavouriteUtil.FAVOURITEUTIL_REMOVE;
				}
				dataHolder.mFavProgressBar.setVisibility(View.VISIBLE);
				dataHolder.mFavourite.setVisibility(View.INVISIBLE);
				mCardActionListener.favouriteAction(dataHolder.mDataObject,type);
			}
		}
	};
	private CardItemClickListener mPurchaseListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if(v.getTag() instanceof CardDataHolder){
				CardDataHolder dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
				mCardActionListener.purchase(dataHolder.mDataObject);
			}
		}
	};

	private CardItemClickListener mOpenCardListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			if (mCardActionListener != null){
				if(v.getTag() instanceof CardDataHolder){
					CardDataHolder dataHolder = (CardDataHolder) v.getTag();
					if(dataHolder == null){return;}
					if(dataHolder.mDataObject == null){return;}
					mCardActionListener.open(dataHolder.mDataObject);
				}
			}
		}
	};
}
