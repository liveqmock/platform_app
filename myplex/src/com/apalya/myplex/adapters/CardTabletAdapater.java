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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.apalya.myplex.utils.Util;

public class CardTabletAdapater extends BaseAdapter implements OnScrollListener{
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
	private int cardColor = -1;
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if(v ==null)
			v = mInflater.inflate(R.layout.card, null);
		CardData data = mDataList.get(position);
		v.setId(position);
		CardDataHolder dataHolder = (CardDataHolder)v.getTag();
		if(dataHolder == null){
			dataHolder = new CardDataHolder();
			dataHolder.mTitleLayout = (RelativeLayout)v.findViewById(R.id.card_title_layout); 
			dataHolder.mTitle = (TextView)v.findViewById(R.id.card_title_name);
			dataHolder.mDelete = (TextView)v.findViewById(R.id.card_title_deleteText);
			dataHolder.mFavourite = (TextView)v.findViewById(R.id.card_title_fav);
			dataHolder.mFavLayout = (RelativeLayout)v.findViewById(R.id.card_title_favlayout);
			dataHolder.mDeleteLayout = (RelativeLayout)v.findViewById(R.id.card_title_delete);
			dataHolder.mPreview = (CardImageView)v.findViewById(R.id.card_preview_image);
//			dataHolder.mPreviewLayout = (RelativeLayout)v.findViewById(R.id.card_preview_layout);
			dataHolder.mOverLayPlay = (ImageView)v.findViewById(R.id.card_play);
			dataHolder.mComments = (TextView)v.findViewById(R.id.card_status_comments);
			dataHolder.mReviews = (TextView)v.findViewById(R.id.card_status_people);
			dataHolder.mCommentsText = (TextView)v.findViewById(R.id.card_status_comments_text);
			dataHolder.mReviewsText = (TextView)v.findViewById(R.id.card_status_people_text);
			dataHolder.mRentLayout = (RelativeLayout)v.findViewById(R.id.card_rent_layout);
			dataHolder.mRentText = (TextView)v.findViewById(R.id.card_rent_text);
			dataHolder.mFavProgressBar = (ProgressBar) v.findViewById(R.id.card_title_fav_progress);
//			dataHolder.mESTDownloadBar = (ProgressBar) v.findViewById(R.id.card_eststatus);
//			dataHolder.mESTDownloadStatus = (TextView) v.findViewById(R.id.card_eststatus_text);
			dataHolder.mFavProgressBar.getIndeterminateDrawable().setColorFilter(0xFF54B5E9, android.graphics.PorterDuff.Mode.MULTIPLY);
			// fonts
			Random rnd = new Random();
			int Low = 100;
			int High = 196;
			if(cardColor == -1){
				cardColor = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low);
			}
	        dataHolder.mPreview.setBackgroundColor(cardColor);
			
			dataHolder.mTitle.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mRentText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mCommentsText.setTypeface(FontUtil.Roboto_Medium);
			dataHolder.mReviewsText.setTypeface(FontUtil.Roboto_Medium);
			
//			dataHolder.mESTDownloadStatus.setTypeface(FontUtil.Roboto_Medium);
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
		}
		dataHolder.mDataObject = data;
		dataHolder.mPreview.mCardId = position;
		dataHolder.mPreview.mImageUrl = null;
//		dataHolder.mTitleLayout.setBackgroundColor(color)
		
//		ImageUrlMap.put(""+position,null);
		
		if(data.generalInfo != null && data.generalInfo.title != null){
			dataHolder.mTitle.setText(data.generalInfo.title);
		}
		

        dataHolder.mPreview.setImageBitmap(null);
//		Log.e(TAG,"Erasing "+position+" for "+dataHolder.mTitle.getTextSize()+"  "+dataHolder.mTitle.getWidth() );
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
			dataHolder.mFavourite.setTextColor(Color.parseColor("#58b4e5"));
		}else{
			dataHolder.mFavourite.setText(R.string.card_filledheart);
			dataHolder.mFavourite.setTextColor(Color.parseColor("#000000"));
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
		
//		int reqsize = (int) mContext.getResources().getDimension(R.dimen.margin_gap_24);
//		reqsize = 40;
//		 ShapeDrawable biggerCircle= new ShapeDrawable( new OvalShape());
//	        biggerCircle.setIntrinsicHeight( reqsize );
//	        biggerCircle.setIntrinsicWidth( reqsize);
//	        biggerCircle.setBounds(new Rect(0, 0, reqsize, reqsize));
//	        biggerCircle.getPaint().setColor(Color.BLUE);
//
//	        dataHolder.mOverLayPlay.setBackgroundDrawable(biggerCircle);
		dataHolder.mDeleteLayout.setOnClickListener(mDeleteListener);
		dataHolder.mDeleteLayout.setTag(dataHolder);
		Util.showFeedback(dataHolder.mDeleteLayout);
		dataHolder.mDeleteLayout.setBackgroundColor(Color.TRANSPARENT);
		dataHolder.mOverLayPlay.setOnClickListener(mOpenCardListener);
		dataHolder.mOverLayPlay.setTag(dataHolder);
		dataHolder.mPreview.setOnClickListener(mOpenCardListener);
		dataHolder.mPreview.setTag(dataHolder);
		dataHolder.mRentLayout.setOnClickListener(mPurchaseListener);
		dataHolder.mRentLayout.setTag(dataHolder);
		dataHolder.mFavLayout.setOnClickListener(mFavListener);
		dataHolder.mFavLayout.setTag(dataHolder);
		dataHolder.mFavLayout.setBackgroundColor(Color.TRANSPARENT);
		Util.showFeedback(dataHolder.mFavLayout);
		//17 chars
		float price = 10000f;
		if(data.packages == null){
			dataHolder.mRentText.setText("Free");
			dataHolder.mRentLayout.setOnClickListener(null);
		}else{
			if(data.currentUserData != null && data.currentUserData.purchase != null && data.currentUserData.purchase.size() != 0){
				dataHolder.mRentText.setText("Watch now");
				dataHolder.mRentLayout.setOnClickListener(null);
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
						dataHolder.mRentLayout.setOnClickListener(null);
					}
				}	
			}
		}
//		updateDownloadStatus(data);
		return v;
	}
	private CardItemClickListener mDeleteListener = new CardItemClickListener() {

		@Override
		public void onDelayedClick(View v) {
			Log.e(TAG, "mDeleteListener onClick");
			CardDataHolder dataHolder = null;
			if(v.getTag() instanceof CardDataHolder){
				dataHolder = (CardDataHolder) v.getTag();
				if(dataHolder == null){return;}
				if(dataHolder.mDataObject == null){return;}
			}else{
				return;
			}
			int index = mDataList.indexOf(dataHolder.mDataObject);
			mDataList.remove(index);
			mNumberofItems = mDataList.size();
			notifyDataSetChanged();
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
	private int mLoadMoreLastCalledNumberofItems;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mCardActionListener != null) {
			if (mNumberofItems > mLoadMoreLastCalledNumberofItems && firstVisibleItem > mNumberofItems / 2) {
				mCardActionListener.loadmore(mNumberofItems);
				mLoadMoreLastCalledNumberofItems = mNumberofItems;
			}
			/*if(mDataList.size() > currentSelectedIndex){
				mCardActionListener.selectedCard(mDataList.get(currentSelectedIndex),currentSelectedIndex);
			}*/
		}
		
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
}
