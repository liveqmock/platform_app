package com.apalya.myplex.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmInfoEvent;
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
import com.apalya.myplex.data.ApplicationConfig;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataHolder;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.data.CardViewHolder;
import com.apalya.myplex.data.CardViewMeta;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.CardImageLoader;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FavouriteUtil;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.WidevineDrm;

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
	
//	private CardDownloadData mDownloadData = null;
//	private CardData mCardDatatoUpdate = null;
	private HashMap<CardData,CardDownloadData> mDownloaddataMap = new HashMap<CardData, CardDownloadData>();
	
	public void setDownloadStatus(CardData data,CardDownloadData downloadData)
	{
		mDownloaddataMap.put(data,downloadData);
//		mCardDatatoUpdate = data;
//		mDownloadData = downloadData;
	}
	
	private void updateDownloadStatus(CardData data,CardDownloadData downloadData,View v){
		CardDownloadData localDownloadData;
		if(myplexapplication.mDownloadList == null || data ==null){
			return;
		}
		localDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(data._id); 
		if(localDownloadData == null){
			return;
		}
		int index = -1;
		index = mDataList.indexOf(data);
		Log.e(TAG, "updateData " +index);
		if(index == -1){return;}
		if(v == null){
			return;
		}
		CardDataHolder dataHolder = (CardDataHolder)v.getTag();
		if(dataHolder == null){
			return;
		}
		if(downloadData != null){
			localDownloadData = downloadData;
		}
		dataHolder.mESTDownloadStatus.setVisibility(View.VISIBLE);
		dataHolder.mESTDownloadStatus.setTextSize(16f);
		if(!localDownloadData.mCompleted){
			if(localDownloadData.mDownloadedBytes==0)
			{
				dataHolder.mESTDownloadStatus.setText("Downloading "+localDownloadData.mPercentage+" %");
			}
			else
			{
				DecimalFormat dec = new DecimalFormat("0.00");
				dataHolder.mESTDownloadStatus.setTextSize(12f);
				dataHolder.mESTDownloadStatus.setText(dec.format(localDownloadData.mDownloadedBytes)+"MB / "+dec.format(localDownloadData.mDownloadTotalSize)+"MB   "+localDownloadData.mPercentage+" %");
				
			}
			dataHolder.mESTDownloadBar.setVisibility(View.VISIBLE);
			dataHolder.mESTDownloadBar.setProgress(localDownloadData.mPercentage);
		}else{
			dataHolder.mESTDownloadBar.setVisibility(View.INVISIBLE);
			if(localDownloadData.mPercentage==0)
			{
				dataHolder.mESTDownloadStatus.setText("Download Failed");
			}
			else
			{
				dataHolder.mESTDownloadStatus.setText("Download Complete");	
			}
		}
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
			dataHolder.mESTDownloadBar = (ProgressBar) v.findViewById(R.id.card_download_progressBar);
			dataHolder.mESTDownloadStatus = (TextView) v.findViewById(R.id.card_download_status);
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
			
			dataHolder.mESTDownloadStatus.setTypeface(FontUtil.Roboto_Medium);
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
			dataHolder.mTitle.setText(data.generalInfo.title.toLowerCase());
		}
		

        dataHolder.mPreview.setImageBitmap(null);
//		Log.e(TAG,"Erasing "+position+" for "+dataHolder.mTitle.getTextSize()+"  "+dataHolder.mTitle.getWidth() );
		if(data.images != null){
			for(CardDataImagesItem imageItem:data.images.values){
				if(imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)){
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
		float price = 10000.99f;
		if(data.packages == null|| data.packages.size() == 0){
			dataHolder.mRentText.setText(mContext.getString(R.string.cardstatusfree));
			dataHolder.mRentLayout.setOnClickListener(null);
		}else{
			if(data.currentUserData != null && data.currentUserData.purchase != null && data.currentUserData.purchase.size() != 0){
				String validity = "";
				for(CardDataPurchaseItem item : data.currentUserData.purchase){
					 validity = Util.getExpiry(item.validity);
//					 validity = item.validity;
				}
				if(validity.length()>0)
					dataHolder.mRentText.setText(validity);
				else
					dataHolder.mRentText.setText(mContext.getString(R.string.cardstatuspaid));
//				dataHolder.mRentText.setText(mContext.getString(R.string.cardstatuspaid));
				dataHolder.mRentLayout.setOnClickListener(null);
			}else{
				for(CardDataPackages packageitem:data.packages){
					if(packageitem.priceDetails != null){
						for(CardDataPackagePriceDetailsItem priceDetailItem:packageitem.priceDetails){
							if(!priceDetailItem.paymentChannel.equalsIgnoreCase(ConsumerApi.PAYMENT_CHANNEL_INAPP) && priceDetailItem.price < price){
								price = priceDetailItem.price;
							}
						}
						if(mRupeeCode == null){
							mRupeeCode = mContext.getResources().getString(R.string.price_rupeecode); 
						}
						if(price == 0)
						{
							dataHolder.mRentText.setText(mContext.getString(R.string.cardstatustempfree));
							dataHolder.mRentLayout.setOnClickListener(null);
						}else if(price  == 10000.99f){
							dataHolder.mRentText.setText(mContext.getString(R.string.cardstatusfree));
							dataHolder.mRentLayout.setOnClickListener(null);
						}
						else
							dataHolder.mRentText.setText(mPriceStarts + mRupeeCode + " "+price);
					}else{
						dataHolder.mRentText.setText(mContext.getString(R.string.cardstatusfree));
						dataHolder.mRentLayout.setOnClickListener(null);
					}
				}	
			}
		}
		if(mDownloaddataMap.containsKey(data))
			updateDownloadStatus(data,mDownloaddataMap.get(data),v);
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
			
			/*****************************DELTEING DOWNLOAD DATA************************************/
			
			if(myplexapplication.mDownloadList != null){
				CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mDataList.get(index)._id);
				if(mDownloadData!=null){
					Util.removeDownload(mDownloadData.mDownloadId,mContext);
					myplexapplication.mDownloadList.mDownloadedList.remove(mDataList.get(index)._id);
					try {
						Log.i(TAG, "prepareDrmManager ");
						prepareDrmManager(mDownloadData.mDownloadPath);
						Log.i(TAG, "removerights "+mDownloadData.mDownloadPath);
						mDrmManagar.removeRights(mDownloadData.mDownloadPath);
						Log.i(TAG, "removerights Sucess");
					} catch (Exception e) {
						Log.e(TAG, "Failed during remove rights");
					}
					Util.saveObject(myplexapplication.mDownloadList, myplexapplication.getApplicationConfig().downloadCardsPath);
				}
			}
			/***************************************************************************************/
			
			mDataList.remove(index);
			mNumberofItems = mDataList.size();
			notifyDataSetChanged();
		}
	};

private void prepareDrmManager(String url){
		
		mDrmManagar = new WidevineDrm(mContext);
		
		
		mDrmManagar.logBuffer.append("Asset Uri: " + url + "\n");
		mDrmManagar.logBuffer.append("Drm Server: " + WidevineDrm.Settings.DRM_SERVER_URI + "\n");
		mDrmManagar.logBuffer.append("Device Id: " + WidevineDrm.Settings.DEVICE_ID + "\n");
		mDrmManagar.logBuffer.append("Portal Name: " + WidevineDrm.Settings.PORTAL_NAME + "\n");
		
        // Set log update listener
        WidevineDrm.WidevineDrmLogEventListener drmLogListener =
            new WidevineDrm.WidevineDrmLogEventListener() {
            public void logUpdated(int status,int value) {
            	updateLogs(status,value);
            }
        };
		
        mDrmManagar.setLogListener(drmLogListener);
        mDrmManagar.registerPortal(WidevineDrm.Settings.PORTAL_NAME);
		}
	
	private WidevineDrm mDrmManagar;

	protected void updateLogs(int status,int value) {
			if(status==0 && value== DrmInfoEvent.TYPE_RIGHTS_INSTALLED)
			{
				Map<String,String> params=new HashMap<String, String>();
				params.put("Status", "PlayerRightsAcqusition");
				//Analytics.trackEvent(Analytics.PlayerRightsAcqusition,params);
				//Util.showToast(mContext,"RIGHTS INSTALLED",Util.TOAST_TYPE_INFO);
			}
			if(status==1 ){
				String errMsg = "Error while playing";
				switch (value) {
				case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
					errMsg="No Internet Connection";
					break;
				case DrmErrorEvent.TYPE_NOT_SUPPORTED:
					errMsg="Device Not Supported";
					break;
				case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
					errMsg="Out of Memory";
					break;
				case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
					errMsg="Process DRM Info failed";
					break;
				case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
					errMsg="Remove All Rights failed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
					errMsg="Rights not installed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
					errMsg="Rights renewal not allowed";
					break;
				}
				Util.showToast(mContext,errMsg,Util.TOAST_TYPE_INFO);
				//drmManager.
			}
			if(mDrmManagar!=null)
				mDrmManagar.unRegisterLogListener();
	}
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
			if (mNumberofItems > mLoadMoreLastCalledNumberofItems && firstVisibleItem > mNumberofItems / 3) {
				Log.d(TAG, "LoadMore called, Checking if " +mNumberofItems+ ">"+mLoadMoreLastCalledNumberofItems + " &&"+firstVisibleItem+">"+mNumberofItems/3);
				mCardActionListener.loadmore(mNumberofItems);
				mLoadMoreLastCalledNumberofItems = mNumberofItems;
			}
		}
		
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
}
