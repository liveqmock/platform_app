package com.apalya.myplex.data;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class CardDataHolder {
	public RelativeLayout mDeleteLayout;
	public TextView mDelete;
	
	public RelativeLayout mFavLayout;
	public TextView mFavourite;
	public ProgressBar mFavProgressBar;
	
	public RelativeLayout mTitleLayout;
	public TextView mTitle;
	
	public RelativeLayout mPreviewLayout;
	public CardImageView mPreview;
	public ImageView mOverLayPlay;
	
	public RelativeLayout mRentLayout;
	public TextView mRentText;
	
	
	public TextView mComments;
	public TextView mReviews;
	public TextView mCommentsText;
	public TextView mReviewsText;
	
	
	public ProgressBar mESTDownloadBar;
	public TextView mESTDownloadStatus;
	public CardData mDataObject;
	public TextView mCardDescText;
	public TextView mVideoStatusText;
	public TextView mVideoDurationText;
}
